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

package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.language.Language;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

/**
 * A reader for resource content.
 */
public interface ResourceContentReader<T extends ResourceContent> {

  /**
   * This method is called to parse the serialized XML of a
   * {@link ResourceContent}.
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
  T createFromXml(InputStream is) throws SAXException, IOException,
      ParserConfigurationException;

  /**
   * Creates a new {@link ResourceContent} object by looking at the content
   * data.
   * 
   * @param is
   *          the content data
   * @param size
   *          the content length
   * @param language
   *          the language
   * @param fileName
   *          the file name
   * @param mimeType
   *          the content type
   * @return the resource content object
   */
  T createFromContent(InputStream is, Language language, long size,
      String fileName, String mimeType) throws IOException;

  /**
   * Resets the parser.
   */
  void reset();

}