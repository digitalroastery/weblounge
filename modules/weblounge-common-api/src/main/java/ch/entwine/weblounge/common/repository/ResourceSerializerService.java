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

package ch.entwine.weblounge.common.repository;

import java.util.Set;

/**
 * The <code>ResourceSerializerService</code> provides access to registered
 * <code>ResourceSerializer</code> implementations.
 */
public interface ResourceSerializerService {

  /**
   * Returns the resource serializer for the given type or <code>null</code> if
   * no such serializer exists.
   * 
   * @param resourceType
   *          the resource type
   * @return the serializer
   */
  ResourceSerializer<?, ?> getSerializerByType(String resourceType);

  /**
   * Returns the resource serializer based on the content's mime type or
   * <code>null</code> if no suitable serializer was found.
   * 
   * @param mimeType
   *          the content mime type
   * @return the serializer
   */
  ResourceSerializer<?, ?> getSerializerByMimeType(String mimeType);

  /**
   * Returns all registered <code>ResourceSerializer</code>s in a set.
   * 
   * @return the set of serializers
   */
  Set<ResourceSerializer<?, ?>> getSerializers();

}
