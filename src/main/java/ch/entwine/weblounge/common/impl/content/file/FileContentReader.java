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

package ch.entwine.weblounge.common.impl.content.file;

import ch.entwine.weblounge.common.content.file.FileContent;
import ch.entwine.weblounge.common.impl.content.ResourceContentReaderImpl;
import ch.entwine.weblounge.common.language.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse <code>Content</code> data for simple files.
 */
public class FileContentReader extends ResourceContentReaderImpl<FileContent> {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FileContentReader.class);

  /** Parser factory */
  protected static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  /** The SAX parser */
  protected WeakReference<SAXParser> parserRef = null;

  /**
   * Creates a new file content reader that will parse serialized XML version of
   * the file content and store it in the {@link FileContent} that is returned
   * by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   * 
   * @see #createFromXml(InputStream)
   */
  public FileContentReader() throws ParserConfigurationException, SAXException {
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.ResourceContentReaderImpl#createContent()
   */
  @Override
  protected FileContent createContent() {
    return new FileContentImpl();
  }

  /**
   * This method is called to parse the serialized XML of a {@link FileContent}.
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
  public FileContent createFromXml(InputStream is) throws SAXException,
      IOException, ParserConfigurationException {

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
   * @see ch.entwine.weblounge.common.content.ResourceContentReader#createFromContent(java.io.InputStream,
   *      ch.entwine.weblounge.common.language.Language, long, java.lang.String,
   *      java.lang.String)
   */
  public FileContent createFromContent(InputStream is, Language language,
      long size, String fileName, String mimeType) throws IOException {
    return new FileContentImpl(fileName, language, mimeType, size);
  }

  /**
   * Resets the pagelet parser.
   */
  public void reset() {
    content = null;
    SAXParser parser = parserRef.get();
    if (parser != null)
      parser.reset();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String local, String raw,
      Attributes attrs) throws SAXException {

    // start of a new content element
    if ("content".equals(raw)) {
      logger.debug("Started reading file content {}", content);
    }

    super.startElement(uri, local, raw, attrs);
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

    // mimetype
    else if ("mimetype".equals(raw)) {
      content.setMimetype(getCharacters());
      logger.trace("File content's mimetype is '{}'", content.getMimetype());
    }

    // size
    else if ("size".equals(raw)) {
      content.setSize(Long.parseLong(getCharacters()));
      logger.trace("File content's filesize is '{}'", content.getSize());
    }

    super.endElement(uri, local, raw);
  }

}