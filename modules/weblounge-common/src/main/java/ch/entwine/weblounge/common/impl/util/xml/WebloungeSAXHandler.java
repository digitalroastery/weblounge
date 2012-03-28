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

package ch.entwine.weblounge.common.impl.util.xml;

import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class used to parse generic content.
 */
public abstract class WebloungeSAXHandler extends DefaultHandler {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeSAXHandler.class);

  /** The attribute and element clipboard */
  protected Map<String, Object> clipboard = null;

  /** The date format used to parse dates */
  protected final WebloungeDateFormat dateFormat = new WebloungeDateFormat();

  /** The characters */
  protected StringBuffer characters = null;

  /** Parser context */
  protected enum Context {
    Unknown, Security, Creation, Modification, Publish, Content
  };

  /** The initial parser context */
  protected Context contentReaderContext = Context.Unknown;

  /**
   * Creates a new content reader that will parse the XML data.
   */
  public WebloungeSAXHandler() {
    clipboard = new HashMap<String, Object>();
    characters = new StringBuffer();
  }

  /**
   * Resets the parser.
   */
  public void reset() {
    characters = new StringBuffer();
    clipboard.clear();
    contentReaderContext = Context.Unknown;
  }

  /**
   * Returns the trimmed contents of the characters buffer.
   * 
   * @return the trimmed characters
   */
  protected String getCharacters() {
    return characters.toString().trim();
  }

  /**
   * {@inheritDoc}
   *
   * @see org.xml.sax.helpers.DefaultHandler#startDocument()
   */
  @Override
  public void startDocument() throws SAXException {
    super.startDocument();
    clipboard.clear();
    characters = new StringBuffer();
  }
  
  /**
   * {@inheritDoc}
   *
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    super.startElement(uri, localName, qName, attributes);
    characters = new StringBuffer();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  public void characters(char[] chars, int start, int end) throws SAXException {
    characters.append(chars, start, end);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException e) throws SAXException {
    logger.warn("Warning while decoding {}: {}", this, e.getMessage());
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException e) throws SAXException {
    logger.warn("Error while decoding {}: {}", this, e.getMessage());
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException e) throws SAXException {
    logger.warn("Fatal error while decoding {}: {}", this, e.getMessage());
  }

}