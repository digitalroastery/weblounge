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
 * A <code>ResourceReader</code> is able to parse xml for a given type of
 * resources and return it's object representation.
 */
public interface ResourceReader<T extends Resource> {

  /**
   * Reads the serialized resource from the input stream and returns the
   * deserialized object representation.
   * 
   * @param uri
   *          the resource uri
   * @param is
   *          the input stream
   * @return the resource
   * @throws SAXException
   *           if parsing the resource fails
   * @throws ParserConfigurationException
   *           if the parser setup fails
   * @throws IOException
   *           if reading the input stream fails
   */
  T read(ResourceURI uri, InputStream is) throws SAXException, IOException,
      ParserConfigurationException;

}
