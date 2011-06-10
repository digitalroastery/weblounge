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

package ch.entwine.weblounge.contentrepository.impl;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Base implementation for resource serializers.
 */
public abstract class AbstractResourceSerializer<S extends ResourceContent, T extends Resource<S>> implements ResourceSerializer<S, T> {

  /** The type */
  protected String type = null;

  /**
   * Creates a new resource serializer for the given type. Note that the type
   * resembles the name of the root tag in the resource's serialized form.
   * 
   * @param type
   *          the resource type
   */
  protected AbstractResourceSerializer(String type) {
    if (type == null)
      throw new IllegalArgumentException("Argument 'type' must not be null");
    this.type = type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getType()
   */
  public String getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getReader()
   */
  public ResourceReader<S, T> getReader() throws ParserConfigurationException, SAXException {
    return createNewReader();
  }

  /**
   * Creates a new reader instance.
   * 
   * @return the new reader
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   */
  protected abstract ResourceReader<S, T> createNewReader()
      throws ParserConfigurationException, SAXException;

}
