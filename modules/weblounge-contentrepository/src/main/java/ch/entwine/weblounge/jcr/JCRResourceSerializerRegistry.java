/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.jcr;

import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.jcr.serializer.JCRResourceSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds references to all kind of {@link JCRResourceSerializer}
 */
public class JCRResourceSerializerRegistry {

  private Map<Class<?>, JCRResourceSerializer> resourceSerializers = new HashMap<Class<?>, JCRResourceSerializer>();

  /** The logging facility */
  private Logger log = LoggerFactory.getLogger(JCRResourceSerializerRegistry.class);

  /**
   * OSGi callback to bind a new resource serializer to the registry
   * 
   * @param serializer
   *          the new resource serializer service
   */
  protected void bindJCRResourceSerializer(JCRResourceSerializer serializer) {
    for (Class<?> type : serializer.getSerializableTypes()) {
      if (resourceSerializers.containsKey(type))
        log.warn("There's already a resource serializer for the type '{}' registered!", type);
      else {
        resourceSerializers.put(type, serializer);
        log.info("New resource serializer cappable of serializing resources of type '{}' added.", type);
      }
    }
  }

  /**
   * OSGi callback to unbind a resource serializer from this registry
   * 
   * @param serializer
   *          the resource serializer to remove
   */
  protected void unbindJCRResourceSerializer(JCRResourceSerializer serializer) {
    for (Class<?> type : serializer.getSerializableTypes()) {
      if (serializer.equals(resourceSerializers.get(type))) {
        resourceSerializers.remove(type);
        log.info("Resource serializer for resources of type '{}' removed.", type);
      }
    }
  }

  /**
   * Checks, if there's a registered resource serializer, which is able to
   * serialize resources of the given type.
   * 
   * @param type
   * @return
   * @throws ContentRepositoryException
   */
  public JCRResourceSerializer getSerializer(Class<?> type)
      throws ContentRepositoryException {
    if (!resourceSerializers.containsKey(type)) {
      log.error("No serializer found for resource type '{}'", type);
      throw new ContentRepositoryException("No serializer found for resource type '" + type + "'");
    }

    return resourceSerializers.get(type);
  }

}
