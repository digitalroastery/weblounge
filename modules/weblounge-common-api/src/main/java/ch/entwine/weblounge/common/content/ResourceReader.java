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

import ch.entwine.weblounge.common.site.Site;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

/**
 * A <code>ResourceReader</code> is able to parse xml for a given type of
 * resources and return it's object representation.
 */
public interface ResourceReader<C extends ResourceContent, R extends Resource<C>> {

  /**
   * Reads the serialized resource from the input stream and returns the
   * deserialized object representation.
   * 
   * @param is
   *          the input stream
   * @param site
   *          the resource's site
   * @return the resource
   * @throws SAXException
   *           if parsing the resource fails
   * @throws ParserConfigurationException
   *           if the parser setup fails
   * @throws IOException
   *           if reading the input stream fails
   */
  R read(InputStream is, Site site) throws SAXException, IOException,
      ParserConfigurationException;

}
