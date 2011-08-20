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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.impl.content.WebloungeContentReader;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Permission;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse page data.
 */
public class PageReader extends WebloungeContentReader implements ResourceReader<ResourceContent, Page> {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PageReader.class);

  /** Parser factory */
  private static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  /** The SAX parser */
  private WeakReference<SAXParser> parserRef = null;

  /** The page object */
  private PageImpl page = null;

  /** Reader used to process pagelet data */
  private PageletReader pageletReader = null;

  /** The composer name */
  private String composer = null;

  /** The pagelet position within the composer */
  private int position = 0;

  /** Flag to indicate whether the page header should be read */
  private boolean readHeader = true;

  /** Flag to indicate whether the page body should be read */
  private boolean readBody = true;

  private enum ParserContext {
    Document, Page, Head, Body, Pagelet
  };

  /** The parser context */
  private ParserContext parserContext = ParserContext.Document;

  /**
   * Creates a new page data reader that will parse the XML data and store it in
   * the <code>Page</code> object that is returned by the {@link #read()} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public PageReader() throws ParserConfigurationException, SAXException {
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
    pageletReader = new PageletReader();
  }

  /**
   * This method is called when a <code>Page</code> object is instantiated.
   * 
   * @param is
   *          the xml input stream
   * @param site
   *          the page's site
   * 
   * @throws IOException
   *           if reading the input stream fails
   */
  public PageImpl read(InputStream is, Site site) throws SAXException,
      IOException, ParserConfigurationException {
    reset();
    page = new PageImpl(new PageURIImpl(site, "/"));
    readHeader = true;
    readBody = true;
    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return page;
  }

  /**
   * This method is called when a <code>Page</code> object is instantiated.
   * 
   * @param is
   *          the xml input stream
   * @param site
   *          the page's site
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public PageImpl readHeader(InputStream is, Site site)
      throws SAXException, IOException, ParserConfigurationException {
    if (page == null) {
      page = new PageImpl(new PageURIImpl(site, "/"));
    }
    readHeader = true;
    readBody = false;
    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return page;
  }

  /**
   * This method is called when a <code>Page</code> object is instantiated.
   * 
   * @param is
   *          the xml input stream
   * @param site
   *          the page's site
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public PageImpl readBody(InputStream is, Site site)
      throws SAXException, IOException, ParserConfigurationException {
    if (page == null) {
      page = new PageImpl(new PageURIImpl(site, "/"));
    }
    readHeader = false;
    readBody = true;
    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return page;
  }

  /**
   * Sets the page that needs to be further enriched with content from an xml
   * document.
   * 
   * @param page
   *          the page
   */
  public void init(PageImpl page) {
    this.page = page;
  }

  /**
   * Resets this parser instance.
   */
  public void reset() {
    super.reset();
    this.page = null;
    this.composer = null;
    this.parserContext = ParserContext.Document;
    this.position = 0;
    this.pageletReader.reset();
    SAXParser parser = parserRef.get();
    if (parser != null)
      parser.reset();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setOwner(ch.entwine.weblounge.common.security.User)
   */
  @Override
  protected void setOwner(User owner) {
    if (parserContext.equals(ParserContext.Pagelet))
      pageletReader.setOwner(owner);
    else
      page.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#allow(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  protected void allow(Permission permission, Authority authority) {
    if (parserContext.equals(ParserContext.Pagelet))
      pageletReader.allow(permission, authority);
    else
      page.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  @Override
  protected void setCreated(User user, Date date) {
    if (parserContext.equals(ParserContext.Pagelet))
      pageletReader.setCreated(user, date);
    else
      page.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  @Override
  protected void setModified(User modifier, Date date) {
    if (parserContext.equals(ParserContext.Pagelet))
      pageletReader.setModified(modifier, date);
    else
      page.setModified(modifier, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setPublished(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, java.util.Date)
   */
  @Override
  protected void setPublished(User publisher, Date startDate, Date endDate) {
    if (parserContext.equals(ParserContext.Pagelet))
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

    // read the page url
    if ("page".equals(raw)) {
      parserContext = ParserContext.Page;
      page.getURI().setIdentifier(attrs.getValue("id"));
      if (attrs.getValue("path") != null)
        page.getURI().setPath(attrs.getValue("path"));
      if (attrs.getValue("version") != null) {
        long version = ResourceUtils.getVersion(attrs.getValue("version"));
        page.getURI().setVersion(version);
      }
    }

    // in the header
    else if ("head".equals(raw)) {
      parserContext = ParserContext.Head;
    }

    // in the body
    else if ("body".equals(raw)) {
      parserContext = ParserContext.Body;
    }

    if (readHeader) {

      // title, subject and the like
      if ("title".equals(raw) || "subject".equals(raw) || "description".equals(raw) || "coverage".equals(raw) || "rights".equals(raw)) {
        String language = attrs.getValue("language");
        if (language != null) {
          Language l = LanguageUtils.getLanguage(language);
          clipboard.put("language", l);
        } else {
          clipboard.remove("language");
        }
      }

    }

    if (readBody) {

      // composer
      if ("composer".equals(raw)) {
        composer = attrs.getValue("id");
        position = 0;
      }

      // pagelet
      else if ("pagelet".equals(raw)) {
        parserContext = ParserContext.Pagelet;
        PageletURI l = new PageletURIImpl(page.getURI(), composer, position);
        pageletReader.setPageletLocation(l);
      }

    }

    // Forward to pagelet reader if the context matches
    if (parserContext.equals(ParserContext.Pagelet)) {
      if (readBody)
        pageletReader.startElement(uri, local, raw, attrs);
    } else {
      super.startElement(uri, local, raw, attrs);
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    if (readBody && parserContext == ParserContext.Pagelet) {

      // Pagelet
      if ("pagelet".equals(raw)) {
        page.addPagelet(pageletReader.getPagelet(), composer, position);
        position++;
        parserContext = ParserContext.Body;
      }

    }

    if (readHeader && parserContext.equals(ParserContext.Head)) {

      // Template
      if ("template".equals(raw)) {
        page.template = characters.toString();
      }

      // Layout
      else if ("layout".equals(raw)) {
        page.layout = characters.toString();
      }

      // Indexed
      else if ("index".equals(raw)) {
        page.setIndexed("true".equals(characters.toString()));
      }

      // Promote
      else if ("promote".equals(raw)) {
        page.setPromoted("true".equals(characters.toString()));
      }

      // Type
      else if ("type".equals(raw)) {
        page.setType(characters.toString());
      }

      // Title
      else if ("title".equals(raw)) {
        Language l = (Language) clipboard.get("language");
        page.setTitle(characters.toString(), l);
      }

      // Description
      else if ("description".equals(raw)) {
        Language l = (Language) clipboard.get("language");
        page.setDescription(characters.toString(), l);
      }

      // Coverage
      else if ("coverage".equals(raw)) {
        Language l = (Language) clipboard.get("language");
        page.setCoverage(characters.toString(), l);
      }

      // Rights
      else if ("rights".equals(raw)) {
        Language l = (Language) clipboard.get("language");
        page.setRights(characters.toString(), l);
      }

      // Subject
      else if ("subject".equals(raw)) {
        page.addSubject(characters.toString());
      }

      // Pagelock
      else if ("locked".equals(raw)) {
        User user = (User) clipboard.get("user");
        if (user != null)
          page.lock(user);
      }

    }

    // Head
    if ("head".equals(raw)) {
      parserContext = ParserContext.Page;
    }

    // Body
    else if ("body".equals(raw)) {
      parserContext = ParserContext.Page;
    }

    // Forward to pagelet reader if the context matches
    if (parserContext.equals(ParserContext.Pagelet)) {
      if (readBody)
        pageletReader.endElement(uri, local, raw);
    } else {
      super.endElement(uri, local, raw);
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] chars, int start, int end) throws SAXException {
    if (parserContext.equals(ParserContext.Pagelet)) {
      if (readBody)
        pageletReader.characters(chars, start, end);
    } else {
      super.characters(chars, start, end);
    }
  }

  /**
   * The parser encountered problems while parsing. The warning is printed out
   * but the parsing process continues.
   * 
   * @param e
   *          information about the warning
   */
  public void warning(SAXParseException e) {
    logger.warn("Warning while reading {}: {}", page, e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void error(SAXParseException e) {
    logger.warn("Error while reading {}: {}", page, e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void fatalError(SAXParseException e) {
    logger.warn("Fatal error while reading {}: {}", page, e.getMessage());
  }

}