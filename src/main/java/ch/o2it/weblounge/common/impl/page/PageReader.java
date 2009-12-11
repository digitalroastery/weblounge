/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.impl.content.WebloungeContentReader;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.PageManager;
import ch.o2it.weblounge.common.page.PageletURI;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse page data.
 */
public final class PageReader extends WebloungeContentReader {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(PageReader.class);

  /** The page object */
  private PageImpl page = null;

  /** Reader used to process pagelet data */
  private PageletReader pageletReader = null;

  /** The composer name */
  private String composer = null;

  /** The pagelet position within the composer */
  private int position = 0;

  /** True if reading pagelets in the headline section */
  private boolean isHeadline = false;

  private enum ParserContext {
    Document, Page, Head, Body, Pagelet
  };

  /** The parser context */
  private ParserContext context_ = ParserContext.Document;

  /**
   * Creates a new page data reader that will parse the SAX data and store it in
   * the page object.
   * 
   * @param site
   *          the associated site
   */
  public PageReader(Site site) {
    super(site);
  }

  /**
   * This method is called, when a <code>Page</code> object is instantiated by
   * the {@link PageManager}.
   * 
   * @param data
   *          the document data
   * @param site
   *          the associated site
   * @param version
   *          the page version
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public PageImpl read(InputStream is, PageURIImpl uri) throws SAXException,
      IOException, ParserConfigurationException {
    page = new PageImpl(uri);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.newSAXParser().parse(is, this);
    return page;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setOwner(ch.o2it.weblounge.common.user.User)
   */
  @Override
  protected void setOwner(User owner) {
    if (context_.equals(ParserContext.Pagelet))
      pageletReader.setOwner(owner);
    else
      page.securityCtx.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  @Override
  protected void allow(Permission permission, Authority authority) {
    if (context_.equals(ParserContext.Pagelet))
      pageletReader.allow(permission, authority);
    else
      page.securityCtx.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setCreated(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setCreated(User user, Date date) {
    if (context_.equals(ParserContext.Pagelet))
      pageletReader.setCreated(user, date);
    else
      page.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setModified(User modifier, Date date) {
    if (context_.equals(ParserContext.Pagelet))
      pageletReader.setModified(modifier, date);
    else
      page.setModified(modifier, date, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setPublished(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, java.util.Date)
   */
  @Override
  protected void setPublished(User publisher, Date startDate, Date endDate) {
    if (context_.equals(ParserContext.Pagelet))
      pageletReader.setPublished(publisher, startDate, endDate);
    else
      page.setPublished(publisher, startDate, endDate);
  }

  /**
   * The parser found the start of an element. Information about this element as
   * well as the attached attributes are passed to this method.
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
    super.startElement(uri, local, raw, attrs);

    // read the page url
    if ("page".equals(raw)) {
      context_ = ParserContext.Page;
      page.uri.id = attrs.getValue("id");
    }

    // in the header
    else if ("head".equals(raw)) {
      context_ = ParserContext.Head;
    }

    // in the body
    else if ("body".equals(raw)) {
      context_ = ParserContext.Body;
    }

    // pagelet or headline
    else if ("pagelet".equals(raw) || "headline".equals(raw)) {
      context_ = ParserContext.Pagelet;
      PageletURI l = new PageletURIImpl(page.getURI(), composer, position);
      pageletReader.setPageletLocation(l);
      if ("headline".equals(raw))
        isHeadline = true;
    }

    // composer
    else if ("composer".equals(raw)) {
      composer = attrs.getValue("id");
      position = 0;
    }

    // title
    else if ("title".equals(raw)) {
      String language = attrs.getValue("language");
      Language l = site.getLanguage(language);
      if (l != null) {
        clipboard.put("language", l);
        if (attrs.getValue("original") != null && attrs.getValue("original").equals("true")) {
          log_.info("Found original title language");
          page.setOriginalLanguage(l);
        }
      } else {
        clipboard.remove("language");
      }
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // Pagelet
    if ("pagelet".equals(raw)) {
      if (isHeadline)
        page.headlines.add(pageletReader.getPagelet());
      else {
        page.addPagelet(pageletReader.getPagelet(), composer, position);
        position++;
      }
      context_ = isHeadline ? ParserContext.Page : ParserContext.Body;
    }

    // Headline
    if ("headline".equals(raw)) {
      context_ = ParserContext.Page;
      isHeadline = false;
    }

    // Template
    else if (context_.equals(ParserContext.Head) && "template".equals(raw)) {
      page.template = characters.toString();
    }

    // Layout
    else if (context_.equals(ParserContext.Head) && "layout".equals(raw)) {
      page.layout = characters.toString();
    }

    // Type
    else if (context_.equals(ParserContext.Head) && "type".equals(raw)) {
      page.type = characters.toString();
    }

    // Title
    else if ("title".equals(raw)) {
      Language l = (Language) clipboard.get("language");
      page.setTitle(characters.toString(), l);
    }

    // Pagelock
    else if (context_.equals(ParserContext.Head) && "pagelock".equals(raw)) {
      String login = characters.toString();
      User editor = site.getUser(login);
      if (page != null)
        page.lockOwner = editor;
    }

    else if (context_.equals(ParserContext.Head) && "head".equals(raw))
      context_ = ParserContext.Page;

    else if (context_.equals(ParserContext.Head) && "body".equals(raw))
      context_ = ParserContext.Page;

    super.endElement(uri, local, raw);
  }

  /**
   * The parser encountered problems while parsing. The warning is printed out
   * but the parsing process continues.
   * 
   * @param e
   *          information about the warning
   */
  public void warning(SAXParseException e) {
    log_.warn("Warning while reading " + page + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void error(SAXParseException e) {
    log_.warn("Error while reading " + page + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void fatalError(SAXParseException e) {
    log_.warn("Fatal error while reading " + page + ": " + e.getMessage());
  }

}