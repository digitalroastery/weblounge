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

package ch.o2it.weblounge.common.content;

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
  T read(InputStream is) throws SAXException, IOException,
      ParserConfigurationException;

  /**
   * Resets the parser.
   */
  void reset();

}