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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the <code>ResourceSerializerFactory</code>. The
 * factory will create instances of type <code>ResourceSerializer</code>
 * depending on the registered serializer implementations.
 */
public class ResourceSerializerFactory {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourceSerializerFactory.class);

  /** The resource serializer service */
  private static ResourceSerializerService serializerService = null;

  /**
   * This method is used to register the factory with a backing service
   * implementation.
   * 
   * @param service
   *          the resource serializer service
   */
  public static void setResourceSerializerService(ResourceSerializerService service) {
    serializerService = service;
  }

  /**
   * Returns the resource serializer for the given type or <code>null</code> if
   * no such serializer exists.
   * 
   * @param resourceType
   *          the resource type
   * @return the serializer
   */
  public static ResourceSerializer<?> getSerializer(String resourceType) {
    if (serializerService == null) {
      logger.warn("Tried to access resource serializer without a backing service being configured");
      return null;
    }
    return serializerService.getSerializer(resourceType);
  }

}
