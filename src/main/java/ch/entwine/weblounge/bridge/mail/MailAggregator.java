/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.entwine.weblounge.bridge.mail;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.scheduler.JobException;
import ch.entwine.weblounge.common.scheduler.JobWorker;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

/**
 * Content aggregator based on the <code>POP3</code> protocol.
 */
public class MailAggregator implements JobWorker {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(MailAggregator.class);

  /** Name of the inbox */
  public static final String INBOX = "INBOX";

  /** Configuration key for the e-mail provider */
  public static final String OPT_PROVIDER = "provider";

  /** Default mail provider */
  public static final String DEFAULT_PROVIDER = "pop3";

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.scheduler.JobWorker#execute(java.lang.String,
   *      java.util.Dictionary)
   */
  public void execute(String name, Dictionary<String, Serializable> ctx)
      throws JobException {

    Site site = (Site) ctx.get(Site.class.getName());

    // Make sure the site is ready to accept content
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Unable to publish e-mail messages to site '{}': repository is read only", site);
      return;
    }

    WritableContentRepository repository = (WritableContentRepository)site.getContentRepository();

    // Extract the configuration from the job properties
    String provider = (String) ctx.get(OPT_PROVIDER);
    Account account = null;
    try {
      if (StringUtils.isBlank(provider)) {
        provider = DEFAULT_PROVIDER;
      }
      account = new Account(ctx);
    } catch (IllegalArgumentException e) {
      throw new JobException(this, e);
    }

    // Connect to the server
    Properties sessionProperties = new Properties();
    Session session = Session.getDefaultInstance(sessionProperties, null);
    Store store = null;
    Folder inbox = null;

    try {

      // Connect to the server
      try {
        store = session.getStore(provider);
        store.connect(account.getHost(), account.getLogin(), account.getPassword());
      } catch (NoSuchProviderException e) {
        throw new JobException(this, "Unable to connect using unknown e-mail provider '" + provider + "'", e);
      } catch (MessagingException e) {
        throw new JobException(this, "Error connecting to " + provider + " account " + account, e);
      }

      // Open the account's inbox
      try {
        inbox = store.getFolder(INBOX);
        if (inbox == null)
          throw new JobException(this, "No inbox found at " + account);
        inbox.open(Folder.READ_WRITE);
      } catch (MessagingException e) {
        throw new JobException(this, "Error connecting to inbox at " + account, e);
      }

      // Get the messages from the server
      try {
        for (Message message : inbox.getMessages()) {
          if (!message.isSet(Flag.SEEN)) {
            try {
              Page page = aggregate(message, site);
              message.setFlag(Flag.DELETED, true);
              repository.put(page);
              logger.info("E-Mail message published at {}", page.getURI());
            } catch (Exception e) {
              logger.info("E-Mail message discarded: {}", e.getMessage());
              message.setFlag(Flag.SEEN, true);
              // TODO: Reply to sender if the "from" field exists
            }
          }
        }
      } catch (MessagingException e) {
        throw new JobException(this, "Error loading e-mail messages from inbox", e);
      }

      // Close the connection
      // but don't remove the messages from the server
    } finally {
      if (inbox != null) {
        try {
          inbox.close(true);
        } catch (MessagingException e) {
          throw new JobException(this, "Error closing inbox", e);
        }
      }
      if (store != null) {
        try {
          store.close();
        } catch (MessagingException e) {
          throw new JobException(this, "Error closing connection to e-mail server", e);
        }
      }
    }

  }

  /**
   * Aggregates the e-mail message by reading it and turning it either into a
   * page or a file upload.
   * 
   * @param message
   *          the e-mail message
   * @param site
   *          the site to publish to
   * @throws MessagingException
   *           if fetching the message data fails
   * @throws IOException
   *           if writing the contents to the output stream fails
   */
  protected Page aggregate(Message message, Site site) throws IOException,
      MessagingException, IllegalArgumentException {

    ResourceURI uri = new PageURIImpl(site, UUID.randomUUID().toString());
    Page page = new PageImpl(uri);
    Language language = site.getDefaultLanguage();

    // Extract title and subject. Without these two, creating a page is not
    // feasible, therefore both messages throw an IllegalArgumentException if
    // the fields are not present.
    String title = getSubject(message);
    String author = getAuthor(message);

    // Collect default settings
    PageTemplate template = site.getDefaultTemplate();
    if (template == null)
      throw new IllegalStateException("Missing default template in site '" + site + "'");
    String stage = template.getStage();
    if (StringUtils.isBlank(stage))
      throw new IllegalStateException("Missing stage definition in template '" + template.getIdentifier() + "'");

    // Standard fields
    page.setTitle(title, language);
    page.setTemplate(template.getIdentifier());
    page.setPublished(site.getAdministrator(), message.getReceivedDate(), null);

    // TODO: Translate e-mail "from" into site user and throw if no such
    // user can be found
    page.setCreated(site.getAdministrator(), message.getSentDate());

    // Start looking at the message body
    String contentType = message.getContentType();
    if (StringUtils.isBlank(contentType))
      throw new IllegalArgumentException("Message content type is unspecified");

    // Text body
    if (contentType.startsWith("text/plain")) {
      // TODO: Evaluate charset
      String body = null;
      if (message.getContent() instanceof String)
        body = (String)message.getContent();
      else if (message.getContent() instanceof InputStream)
        body = IOUtils.toString((InputStream)message.getContent());
      else 
        throw new IllegalArgumentException("Message body is of unknown type");
      return handleTextPlain(body, page, language);
    }

    // HTML body
    if (contentType.startsWith("text/html")) {
      // TODO: Evaluate charset
      return handleTextHtml((String) message.getContent(), page, null);
    }

    // Multipart body
    else if ("mime/multipart".equalsIgnoreCase(contentType)) {
      Multipart mp = (Multipart) message.getContent();
      for (int i = 0, n = mp.getCount(); i < n; i++) {
        Part part = mp.getBodyPart(i);
        String disposition = part.getDisposition();
        if (disposition == null) {
          MimeBodyPart mbp = (MimeBodyPart) part;
          if (mbp.isMimeType("text/plain")) {
            return handleTextPlain((String) mbp.getContent(), page, null);
          } else {
            // TODO: Implement special non-attachment cases here of
            // image/gif, text/html, ...
            throw new UnsupportedOperationException("Multipart message bodies of type '" + mbp.getContentType() + "' are not yet supported");
          }
        } else if (disposition.equals(Part.ATTACHMENT) || disposition.equals(Part.INLINE)) {
          logger.info("Skipping message attachment " + part.getFileName());
          // saveFile(part.getFileName(), part.getInputStream());
        }
      }

      throw new IllegalArgumentException("Multipart message did not contain any recognizable content");
    }

    // ?
    else {
      throw new IllegalArgumentException("Message body is of unknown type '" + contentType + "'");
    }
  }

  /**
   * Handles the creation of a page based on an e-mail body of type
   * <code>text/plain</code>.
   * 
   * @param content
   *          the message body
   * @param page
   *          the page
   * @param language
   *          the content language
   * @return the page
   */
  private Page handleTextPlain(String content, Page page, Language language) {
    for (String paragraph : content.split("\r\n")) {
      if (StringUtils.isBlank(paragraph))
        continue;
      PageletImpl p = new PageletImpl("text", "paragraph");
      p.setContent("text", StringUtils.trim(paragraph), language);
      page.addPagelet(p, page.getStage().getIdentifier());
    }
    return page;
  }

  /**
   * Handles the creation of a page based on an e-mail body of type
   * <code>text/html</code>.
   * 
   * @param content
   *          the message body
   * @param page
   *          the page
   * @param language
   *          the content language
   * @return the page
   */
  private Page handleTextHtml(String content, Page page, Language language) {
    // TODO: Implement HTML message handling
    throw new UnsupportedOperationException("Message bodies of type 'text/html' are not yet supported");
  }

  /**
   * Returns the author of this message. If the message does not have a
   * <code>from</code> field, an {@link IllegalArgumentException} is thrown.
   * 
   * @param message
   *          the e-mail message
   * @return the sender
   * @throws MessagingException
   *           if reading the message's author fails
   * @throws IllegalArgumentException
   *           if no author can be found
   */
  private String getAuthor(Message message) throws MessagingException,
      IllegalArgumentException {
    Address[] address = message.getFrom();
    if (address == null || address.length == 0)
      throw new MessagingException("Message has no author");
    return address[0].toString();
  }

  /**
   * Returns the subject of this message. If the message does not have a
   * <code>subject</code> field, an {@link IllegalArgumentException} is thrown.
   * 
   * @param message
   *          the e-mail message
   * @return the subject
   * @throws MessagingException
   *           if reading the message's subject fails
   * @throws IllegalArgumentException
   *           if no subject can be found
   */
  private String getSubject(Message message) throws MessagingException,
      IllegalArgumentException {
    String subject = message.getSubject();
    if (StringUtils.isBlank(subject))
      throw new MessagingException("Message has no subject");
    return subject;
  }

}
