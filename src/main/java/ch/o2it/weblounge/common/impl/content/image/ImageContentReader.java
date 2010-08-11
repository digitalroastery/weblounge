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

package ch.o2it.weblounge.common.impl.content.image;

import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.impl.content.file.FileContentReader;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

/**
 * Utility class used to parse <code>Content</code> data for simple files.
 */
public class ImageContentReader extends FileContentReader {

  /** Logging facility */
  private final static Logger logger = LoggerFactory.getLogger(ImageContentReader.class);

  /** The image content data */
  protected ImageContentImpl imageContent = null;

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
  public ImageContentReader() throws ParserConfigurationException, SAXException {
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
  public ImageContent read(InputStream is) throws SAXException, IOException,
      ParserConfigurationException {

    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return imageContent;
  }

  /**
   * Resets the pagelet parser.
   */
  public void reset() {
    super.reset();
    imageContent = null;
  }

  /**
   * Returns the content that has been read in.
   * 
   * @return the content
   */
  ImageContent getImageContent() {
    return imageContent;
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
      String languageId = attrs.getValue("language");
      imageContent = new ImageContentImpl(LanguageSupport.getLanguage(languageId));
      content = imageContent;
      logger.debug("Started reading image content {}", content);
    } else {
      super.startElement(uri, local, raw, attrs);
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

    // width
    if ("width".equals(raw)) {
      imageContent.setWidth(Integer.parseInt(getCharacters()));
      logger.trace("Image's width is '{}'", imageContent.getWidth());
    }

    // height
    else if ("height".equals(raw)) {
      imageContent.setHeight(Integer.parseInt(getCharacters()));
      logger.trace("Image's height is '{}'", imageContent.getHeight());
    }
    
    else {
      super.endElement(uri, local, raw);
    }
  }

}