/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.security.AbstractSecurityContext;
import ch.o2it.weblounge.common.impl.security.AuthorityImpl;
import ch.o2it.weblounge.common.impl.security.PermissionImpl;
import ch.o2it.weblounge.common.impl.security.PermissionSecurityContext;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility class used to parse generic content including language sensitive
 * information and a security context.
 */
public class WebloungeContentReader extends DefaultHandler {

  /** The name of the object that is currently being read (used for logging) */
  protected String name;

  /** The associated site */
  protected Site site;

  /** The security context */
  protected PermissionSecurityContext securityCtx;

  /** The publishing context */
  protected PublishingContextImpl publishingCtx;

  /** The modification context */
  protected ModificationContextImpl modificationCtx;

  /** The attribute and element clipboard */
  protected Map<String, Object> clipboard;

  /** The date format used to parse dates */
  protected final static WebloungeDateFormat dateFormat = new WebloungeDateFormat();

  /** The original content language */
  protected Language originalLanguage;

  /** The characters */
  protected StringBuffer characters;

  /** The editor */
  protected User editor;

  /** The owner */
  protected User owner;

  /** The content */
  protected HashMap<String, String> content;

  /** The keywords */
  protected Keywords keywords;

  /** The parser contexts */
  protected static final int CTXT_UNKNOWN = -1;
  protected static final int CTXT_SECURITY = 1;
  protected static final int CTXT_WORKFLOW = 2;
  protected static final int CTXT_CREATION = 3;
  protected static final int CTXT_MODIFICATION = 4;
  protected static final int CTXT_PUBLISH = 5;
  protected static final int CTXT_KEYWORDS = 6;
  protected static final int CTXT_CONTENT = 7;

  /** The initial parser context */
  protected int contentReaderContext = CTXT_UNKNOWN;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = WebloungeContentReader.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new content sax event reader that will parse the sax data.
   * 
   * @param site
   *          the associated site
   * @param name
   *          the name that is used for logging output
   */
  public WebloungeContentReader(Site site, String name) {
    this.site = site;
    this.name = name;
    clipboard = new HashMap<String, Object>();
  }

  /**
   * Starts the sax parser and reads in the data.
   * 
   * @param data
   *          the xml data resource
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  protected void read(InputStream data) throws IOException, SAXException,
      ParserConfigurationException {
    XMLUtilities.parse(data, this);

    // Apply default permissions if none have been set
    if (securityCtx == null) {
      securityCtx = getDefaultSecurityContext();
    }

    // Apply default modification if none have been set
    if (modificationCtx == null) {
      modificationCtx = getDefaultModificationContext();
    }

    // Apply default publishing context if none have been set
    if (publishingCtx == null) {
      publishingCtx = getDefaultPublishingContext();
    }
  }

  /**
   * Returns the default security context, which is applied if no specific
   * security context information could be read.
   * <p>
   * Subclasses should overwrite this method to apply their specialized default
   * security constraints.
   * 
   * @return the default security context
   */
  protected PermissionSecurityContext getDefaultSecurityContext() {
    PermissionSecurityContext ctxt = new PermissionSecurityContext();
    ctxt.setOwner(site.getAdministrator());
    ctxt.allow(SystemPermission.READ, SystemRole.GUEST);
    ctxt.allow(SystemPermission.TRANSLATE, SystemRole.TRANSLATOR);
    ctxt.allow(SystemPermission.WRITE, SystemRole.EDITOR);
    ctxt.allow(SystemPermission.MANAGE, SystemRole.EDITOR);
    ctxt.allow(SystemPermission.PUBLISH, SystemRole.PUBLISHER);
    return ctxt;
  }

  /**
   * Returns the security context. If no context has been read in so far, the
   * default security context is being returned. Note that the context is
   * being cleared (set to <code>null</code>) after every call to this method.
   * 
   * @return the security context
   */
  public PermissionSecurityContext getSecurityContext() {
    if (securityCtx == null) {
      securityCtx = getDefaultSecurityContext();
    }
    PermissionSecurityContext ctxt = securityCtx;
    securityCtx = null;
    return ctxt;
  }

  /**
   * Returns the default publishing context, which is applied if no specific
   * publishing context information could be read.
   * <p>
   * Subclasses should overwrite this method to apply their specialized default
   * publishing constraints.
   * 
   * @return the default publishing context
   */
  protected PublishingContextImpl getDefaultPublishingContext() {
    return new PublishingContextImpl();
  }

  /**
   * Returns the publishing context. If no context has been read in so far, the
   * default publishing context is being returned. Note that the context is
   * being cleared (set to <code>null</code>) after every call to this method.
   * 
   * @return the publishing context
   */
  public PublishingContextImpl getPublishingContext() {
    if (publishingCtx == null) {
      publishingCtx = getDefaultPublishingContext();
    }
    PublishingContextImpl ctxt = publishingCtx;
    publishingCtx = null;
    return ctxt;
  }

  /**
   * Returns the default publishing context, which is applied if no specific
   * publishing context information could be read.
   * <p>
   * Subclasses should overwrite this method to apply their specialized default
   * publishing constraints.
   * 
   * @return the default publishing context
   */
  protected ModificationContextImpl getDefaultModificationContext() {
    return new ModificationContextImpl();
  }

  /**
   * Returns the publishing context. If no context has been read in so far, the
   * default publishing context is being returned. Note that the context is
   * being cleared (set to <code>null</code>) after every call to this method.
   * 
   * @return the publishing context
   */
  public ModificationContextImpl getModificationContext() {
    if (modificationCtx == null) {
      modificationCtx = getDefaultModificationContext();
    }
    ModificationContextImpl ctxt = modificationCtx;
    modificationCtx = null;
    return ctxt;
  }

  /**
   * Sets the object's creation date.
   * 
   * @param date
   *          the creation date
   */
  protected void setCreationDate(Date date) {
    if (modificationCtx == null)
      modificationCtx = new ModificationContextImpl();
    modificationCtx.setCreationDate(date);
  }

  /**
   * Sets the object's creator.
   * 
   * @param user
   *          the creator
   */
  protected void setCreator(User user) {
    if (modificationCtx == null)
      modificationCtx = new ModificationContextImpl();
    modificationCtx.setCreator(user);
  }

  /**
   * Sets the object's modification date.
   * 
   * @param date
   *          the modification date
   */
  protected void setModificationDate(Date date) {
    if (modificationCtx == null)
      modificationCtx = new ModificationContextImpl();
    modificationCtx.setModificationDate(date);
  }

  /**
   * Sets the object's modifier.
   * 
   * @param user
   *          the modifier
   */
  protected void setModifier(User user) {
    if (modificationCtx == null)
      modificationCtx = new ModificationContextImpl();
    modificationCtx.setModifier(user);
  }

  /**
   * This method is called if the owner of the object is found.
   * 
   * @param user
   *          the owner
   */
  protected void setOwner(User user) {
    if (securityCtx == null)
      securityCtx = new PermissionSecurityContext();
    securityCtx.setOwner(user);
  }

  /**
   * This method is called if the reader found the original content language.
   * This language can later be obtained using
   * <code>{@link #getOriginalLanguage()}</code>.
   * 
   * @param language
   *          the original content
   */
  protected void setOriginalLanguage(Language language) {
    originalLanguage = language;
  }

  /**
   * Returns the content's original language or <code>null</code> if no original
   * language was specified.
   * 
   * @return the original language
   */
  public Language getOriginalLanguage() {
    return originalLanguage;
  }

  /**
   * Adds a keyword to the list of keywords.
   * 
   * @param keyword
   *          the new keyword
   */
  protected void addKeyword(String keyword) {
    if (keywords == null) {
      keywords = new Keywords();
    }
    keywords.add(keyword);
  }

  /**
   * Returns the keywords that have been registered for this item.
   * 
   * @return the keywords
   */
  public Keywords getKeywords() {
    if (keywords == null) {
      keywords = new Keywords();
    }
    return keywords;
  }

  /**
   * The parser found the start of an element. Information about this element as
   * well as the attached attributes are passed to this method.
   * <p>
   * <b>Note:</b> This implementation is not suitable for mixed content, since
   * the characters buffer is cleared at the beginning of each tag.
   * 
   * @param uri
   *          information about the namespace
   * @param local
   *          the local name of the element
   * @param raw
   *          the raw name of the element
   * @param attrs
   *          the element's attributes
   */
  public void startElement(String uri, String local, String raw,
      Attributes attrs) throws SAXException {
    characters = new StringBuffer();

    // security context
    if ("security".equals(local)) {
      securityCtx = null;
      contentReaderContext = CTXT_SECURITY;
      return;
    }

    // permission
    else if (contentReaderContext == CTXT_SECURITY && "permission".equals(local)) {
      String id = attrs.getValue("id");
      String type = attrs.getValue("type");
      clipboard.put("id", id);
      clipboard.put("type", type);
    }

    // history context
    else if ("workflow".equals(local)) {
      contentReaderContext = CTXT_WORKFLOW;
      if (modificationCtx == null) {
        modificationCtx = new ModificationContextImpl();
      }
      return;
    }

    // creation context
    else if ("created".equals(local)) {
      contentReaderContext = CTXT_CREATION;
      return;
    }

    // modification context
    else if ("modified".equals(local)) {
      contentReaderContext = CTXT_MODIFICATION;
      return;
    }

    // publishing context
    else if ("publish".equals(local)) {
      publishingCtx = null;
      contentReaderContext = CTXT_PUBLISH;
      return;
    }

    // content
    else if ("content".equals(local)) {
      contentReaderContext = CTXT_CONTENT;
      String language = attrs.getValue("language");
      Language l = LanguageSupport.getLanguage(language);
      clipboard.put("language", l);
      if ("true".equals(attrs.getValue("original"))) {
        setOriginalLanguage(l);
      }
      content = new HashMap<String, String>();
    }

    // keywords
    else if ("keywords".equals(local)) {
      contentReaderContext = CTXT_KEYWORDS;
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    /** security owner */
    if (contentReaderContext == CTXT_SECURITY && "owner".equals(local)) {
      String login = characters.toString();
      owner = site.getUsers().getUser(login);
    }

    /** security permission */
    else if (contentReaderContext == CTXT_SECURITY && "permission".equals(local)) {
      String id = (String) clipboard.get("id");
      Permission permission = new PermissionImpl(id);
      String type = (String) clipboard.get("type");
      if (type != null) {
        if (securityCtx == null) {
          securityCtx = new PermissionSecurityContext();
          securityCtx.setOwner(site.getAdministrator());
        }
        type = AbstractSecurityContext.resolveAuthorityTypeShortcut(type);
        StringTokenizer tok = new StringTokenizer(characters.toString(), " ,;");
        while (tok.hasMoreTokens()) {
          String authorityId = tok.nextToken();
          Authority authority = new AuthorityImpl(type, authorityId);
          securityCtx.allow(permission, authority);
        }
      }
    }

    /** publishing from */
    else if (contentReaderContext == CTXT_PUBLISH && "from".equals(local)) {
      if (publishingCtx == null) {
        publishingCtx = new PublishingContextImpl();
      }
      if (characters.length() > 0) {
        try {
          Date d = dateFormat.parse(characters.toString());
          publishingCtx.setPublishFrom(d);
        } catch (Exception e) {
          publishingCtx.setPublishFrom(new Date());
          log_.warn("The publishing 'from' date on " + this + " is malformed: '" + characters.toString() + "'");
        }
      } else {
        publishingCtx.setPublishFrom(new Date());
      }
    }

    /** publishing to */
    else if (contentReaderContext == CTXT_PUBLISH && "to".equals(local)) {
      if (publishingCtx == null) {
        publishingCtx = new PublishingContextImpl();
      }
      if (characters.length() > 0) {
        try {
          Date d = dateFormat.parse(characters.toString());
          publishingCtx.setPublishTo(d);
        } catch (Exception e) {
          publishingCtx.setPublishTo(new Date(Long.MAX_VALUE));
          log_.warn("The publishing 'to' date on " + this + " is malformed: '" + characters.toString() + "'");
        }
      } else {
        publishingCtx.setPublishTo(new Date(Long.MAX_VALUE));
      }
    }

    /** keywords */
    else if (contentReaderContext == CTXT_KEYWORDS && "keyword".equals(local)) {
      String keywords = characters.toString();
      StringTokenizer tok = new StringTokenizer(keywords, ",");
      while (tok.hasMoreTokens()) {
        addKeyword(tok.nextToken().trim());
      }
    }

    /** creating user */
    else if (contentReaderContext == CTXT_CREATION && "user".equals(local)) {
      String login = characters.toString();
      User user = site.getUsers().getUser(login);
      setCreator(user);
    }

    /** creation date */
    else if (contentReaderContext == CTXT_CREATION && "date".equals(local)) {
      if (characters.length() > 0) {
        try {
          Date d = dateFormat.parse(characters.toString());
          setCreationDate(d);
        } catch (Exception e) {
          log_.warn("The modification date on " + this + " is malformed: '" + characters.toString() + "'");
        }
      }
    }

    /** modifying user */
    else if (contentReaderContext == CTXT_MODIFICATION && "user".equals(local)) {
      String login = characters.toString();
      User user = site.getUsers().getUser(login);
      setModifier(user);
    }

    /** modification date */
    else if (contentReaderContext == CTXT_MODIFICATION && "date".equals(local)) {
      if (characters.length() > 0) {
        try {
          Date d = dateFormat.parse(characters.toString());
          setModificationDate(d);
        } catch (Exception e) {
          log_.warn("The modification date on " + this + " is malformed: '" + characters.toString() + "'");
        }
      }
    }

    /** Security */
    else if (contentReaderContext == CTXT_SECURITY && "security".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

    /** History */
    else if (contentReaderContext == CTXT_WORKFLOW && "history".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

    /** Creation */
    else if (contentReaderContext == CTXT_CREATION && "created".equals(local)) {
      contentReaderContext = CTXT_WORKFLOW;
    }

    /** Modification */
    else if (contentReaderContext == CTXT_MODIFICATION && "modified".equals(local)) {
      contentReaderContext = CTXT_WORKFLOW;
    }

    /** Publishing */
    else if (contentReaderContext == CTXT_PUBLISH && "publish".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

    /** Content */
    else if (contentReaderContext == CTXT_CONTENT && "content".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

    /** Keywords */
    else if (contentReaderContext == CTXT_KEYWORDS && "keywords".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] chars, int start, int end) throws SAXException {
    characters.append(chars, start, end);
  }

  /**
   * Resets this reader.
   */
  protected void reset() {
    characters = null;
    clipboard.clear();
    contentReaderContext = CTXT_UNKNOWN;
    editor = null;
    keywords = null;
    modificationCtx = null;
    name = null;
    originalLanguage = null;
    owner = null;
    publishingCtx = null;
    securityCtx = null;
  }

  /**
   * The parser encountered problems while parsing. The warning is printed out
   * but the parsing process continues.
   * 
   * @param e
   *          information about the warning
   */
  public void warning(SAXParseException e) throws SAXException {
    log_.warn("Warning while decoding " + this + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void error(SAXParseException e) throws SAXException {
    log_.warn("Error while decoding " + this + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void fatalError(SAXParseException e) throws SAXException {
    log_.warn("Fatal error while decoding " + this + ": " + e.getMessage());
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return name;
  }

}