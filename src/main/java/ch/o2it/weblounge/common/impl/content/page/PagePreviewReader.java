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

package ch.o2it.weblounge.common.impl.content.page;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.PageletURI;
import ch.o2it.weblounge.common.impl.content.WebloungeContentReader;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.user.User;

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
 * Utility class used to parse page preview data, which basically consists of a
 * number of pagelets found in the <tt>stage</tt> composer.
 */
public final class PagePreviewReader extends WebloungeContentReader {

  /** Logging facility */
  private final static Logger logger = LoggerFactory.getLogger(PagePreviewReader.class);

  /** Parser factory */
  private static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  /** The SAX parser */
  private WeakReference<SAXParser> parserRef = null;

  /** The page uri */
  private ResourceURI pageURI = null;

  /** Reader used to process pagelet data */
  private PageletReader pageletReader = null;

  /** The composer name */
  private ComposerImpl composer = null;

  /** The pagelet position within the composer */
  private int position = 0;

  /**
   * Creates a new page data reader that will parse the XML data and store it in
   * the <code>Page</code> object that is returned by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public PagePreviewReader() throws ParserConfigurationException, SAXException {
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
    pageletReader = new PageletReader();
  }

  /**
   * Reads the page preview from the given input stream and returns the pagelets
   * contained therein in a composer.
   * 
   * @param is
   *          the xml input stream
   * @param uri
   *          the page uri
   * @return a composer containing the page preview
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public Composer read(InputStream is, ResourceURI uri) throws SAXException,
      IOException, ParserConfigurationException {
    reset();
    this.pageURI = uri;
    this.composer = new ComposerImpl("stage");
    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return composer;
  }

  /**
   * Resets this parser instance.
   */
  void reset() {
    this.pageURI = null;
    this.composer = null;
    this.position = 0;
    this.pageletReader.reset();
    SAXParser parser = parserRef.get();
    if (parser != null)
      parser.reset();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setOwner(ch.o2it.weblounge.common.user.User)
   */
  @Override
  protected void setOwner(User owner) {
    pageletReader.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  @Override
  protected void allow(Permission permission, Authority authority) {
    pageletReader.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setCreated(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setCreated(User user, Date date) {
    pageletReader.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setModified(User modifier, Date date) {
    pageletReader.setModified(modifier, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setPublished(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, java.util.Date)
   */
  @Override
  protected void setPublished(User publisher, Date startDate, Date endDate) {
    pageletReader.setPublished(publisher, startDate, endDate);
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

    // pagelet
    if ("pagelet".equals(raw)) {
      PageletURI l = new PageletURIImpl(pageURI, composer.getIdentifier(), position);
      pageletReader.setPageletLocation(l);
    }

    pageletReader.startElement(uri, local, raw, attrs);
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // Pagelet
    if ("pagelet".equals(raw)) {
      composer.addPagelet(pageletReader.getPagelet());
      position++;
    }

    // Forward to pagelet reader
    pageletReader.endElement(uri, local, raw);
  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] chars, int start, int end) throws SAXException {
    pageletReader.characters(chars, start, end);
  }

  /**
   * The parser encountered problems while parsing. The warning is printed out
   * but the parsing process continues.
   * 
   * @param e
   *          information about the warning
   */
  public void warning(SAXParseException e) {
    logger.warn("Warning while reading {} page preview: {}", pageURI, e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void error(SAXParseException e) {
    logger.warn("Error while reading {} page preview: {}", pageURI, e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void fatalError(SAXParseException e) {
    logger.warn("Fatal error while reading {} page preview: {}", pageURI, e.getMessage());
  }

}