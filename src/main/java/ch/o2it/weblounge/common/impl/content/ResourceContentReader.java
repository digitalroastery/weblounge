/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.impl.util.xml.WebloungeSAXHandler;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse <code>Content</code> data for simple files.
 */
public class ResourceContentReader extends WebloungeSAXHandler {

  /** Logging facility */
  private final static Logger logger = LoggerFactory.getLogger(ResourceContentReader.class);

  /** Parser factory */
  protected static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  /** The SAX parser */
  protected WeakReference<SAXParser> parserRef = null;

  /** The file content data */
  protected ResourceContentImpl content = null;
  
  /**
   * Creates a new file content reader that will parse serialized XML version of
   * the file content and store it in the {@link ResourceContent} that is returned
   * by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   * 
   * @see #read(InputStream)
   */
  public ResourceContentReader() throws ParserConfigurationException, SAXException {
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
  }

  /**
   * This method is called to parse the serialized XML of a {@link ResourceContent}.
   * 
   * @param is
   *          the content data
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public ResourceContent read(InputStream is) throws SAXException, IOException,
      ParserConfigurationException {

    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return content;
  }

  /**
   * Resets the parser.
   */
  public void reset() {
    super.reset();
    content = null;
    SAXParser parser = parserRef.get();
    if (parser != null)
      parser.reset();
  }

  /**
   * Returns the content that has been read in.
   * 
   * @return the content
   */
  ResourceContent getResourceContent() {
    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String local, String raw,
      Attributes attrs) throws SAXException {

    super.startElement(uri, local, raw, attrs);

    // start of a new content element
    if ("content".equals(raw)) {
      String languageId = attrs.getValue("language");
      content = new ResourceContentImpl(LanguageSupport.getLanguage(languageId));
      logger.debug("Started reading file content {}", content);
    } 

    // creator
    else if ("user".equals(raw)) {
      clipboard.put("user", attrs.getValue("id"));
      clipboard.put("realm", attrs.getValue("realm"));
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // content
    if ("content".equals(raw)) {
      logger.debug("Finished reading content {}", content);
    }
    
    // user
    else if ("user".equals(raw)) {
      String login = (String) clipboard.remove("user");
      String realm = (String) clipboard.remove("realm");
      String name = getCharacters();
      User user = new UserImpl(login, realm, name);
      clipboard.put("user", user);
    }
    
    // date
    else if ("date".equals(raw)) {
      try {
        Date d = dateFormat.parse(getCharacters());
        clipboard.put("date", d);
      } catch (Exception e) {
        throw new IllegalStateException("Reading date failed: '" + getCharacters() + "'");
      }
    }
    
    // created
    else if ("created".equals(raw)) {
      User owner = (User) clipboard.remove("user");
      if (owner == null)
        throw new IllegalStateException("Creator not found");
      Date date = (Date) clipboard.remove("date");
      if (date == null)
        throw new IllegalStateException("Creation date not found");
      content.setCreated(date, owner);
    }

    super.endElement(uri, local, raw);
  }

}