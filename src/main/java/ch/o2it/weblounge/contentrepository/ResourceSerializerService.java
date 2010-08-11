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
  ResourceSerializer<?, ?> getSerializer(String resourceType);

}
