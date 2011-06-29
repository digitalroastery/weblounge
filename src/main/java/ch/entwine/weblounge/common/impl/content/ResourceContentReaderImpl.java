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

package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.util.xml.WebloungeSAXHandler;
import ch.entwine.weblounge.common.security.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse <code>Content</code> data for simple files.
 */
public abstract class ResourceContentReaderImpl<T extends ResourceContent> extends WebloungeSAXHandler implements ResourceContentReader<T> {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourceContentReaderImpl.class);

  /** Parser factory */
  protected static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  /** The SAX parser */
  protected WeakReference<SAXParser> parserRef = null;

  /** The file content data */
  protected T content = null;

  /**
   * Creates a new file content reader that will parse serialized XML version of
   * the file content and store it in the {@link ResourceContent} that is
   * returned by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   * 
   * @see #createFromXml(InputStream)
   */
  public ResourceContentReaderImpl() throws ParserConfigurationException,
      SAXException {
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContentReader#createFromXml(java.io.InputStream)
   */
  public T createFromXml(InputStream is) throws SAXException, IOException,
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContentReader#reset()
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
  public T getContent() {
    return content;
  }

  /**
   * Creates an empty resource content object that will be populated by the
   * parser.
   * 
   * @return the new content object
   */
  protected abstract T createContent();

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
      content = createContent();
      content.setLanguage(LanguageUtils.getLanguage(languageId));
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
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // content
    if ("content".equals(raw)) {
      logger.debug("Finished reading content {}", content);
    }

    // filename
    else if ("filename".equals(raw)) {
      content.setFilename(getCharacters());
      logger.trace("Content's filename is '{}'", content.getFilename());
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
      } catch (ParseException e) {
        throw new IllegalStateException("Reading date failed: '" + getCharacters() + "'");
      }
    }

    // created
    else if ("created".equals(raw)) {
      User owner = (User) clipboard.remove("user");
      Date date = (Date) clipboard.remove("date");
      if (date == null)
        throw new IllegalStateException("Creation date not found");
      content.setCreationDate(date);
      if (owner != null)
        content.setCreator(owner);
    }

    super.endElement(uri, local, raw);
  }

}