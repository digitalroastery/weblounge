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

package ch.o2it.weblounge.contentrepository;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceContentReader;
import ch.o2it.weblounge.common.content.ResourceMetadata;
import ch.o2it.weblounge.common.content.ResourceReader;

import org.xml.sax.SAXException;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Service component that assists the content repository in serializing and
 * deserializing resources.
 */
public interface ResourceSerializer<S extends ResourceContent, T extends Resource<S>> {

  /**
   * Returns the resource type that this serializer can handle. Usually, this
   * will reflect the class name of the serialized resource.
   * 
   * @return the resource type
   */
  String getType();

  /**
   * Returns a <code>ResourceReader</code> for the type of resources that is
   * supported by this serializer.
   * 
   * @return a new resource reader
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs during parser instantiation
   */
  ResourceReader<S, T> getReader() throws ParserConfigurationException,
      SAXException;

  /**
   * Returns the list of metadata, used to add the resource to the search index.
   * 
   * @param resource
   *          the resource
   * @return the resource metadata
   */
  List<ResourceMetadata<?>> getMetadata(Resource<?> resource);

  /**
   * Returns a <code>ResourceContentReader</code> for the type of resources that
   * is supported by this serializer.
   * 
   * @return the resource reader
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs during parser instantiation
   */
  ResourceContentReader<S> getContentReader()
      throws ParserConfigurationException, SAXException;

}
