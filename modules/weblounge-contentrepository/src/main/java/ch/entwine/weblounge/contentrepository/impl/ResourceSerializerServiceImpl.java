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

import ch.entwine.weblounge.common.repository.ResourceSerializer;
import ch.entwine.weblounge.common.repository.ResourceSerializerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service that will watch out for resource serializers and then register it
 * with the <code>ResourceSerializerFactory</code> to allow for static lookup of
 * resource serializers.
 */
public class ResourceSerializerServiceImpl implements ResourceSerializerService {

  /** The logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(ResourceSerializerServiceImpl.class);

  /** The registered content repositories */
  private final Map<String, ResourceSerializer<?, ?>> serializers = new HashMap<String, ResourceSerializer<?, ?>>();

  /**
   * Adds the new serializer to the list of registered resource serializer
   * implementations.
   * <p>
   * Note that existing registrations for the same key as returned by
   * {@link ResourceSerializer#getType()} will be replaced.
   * 
   * @param serializer
   *          the serializer
   */
  public void addSerializer(ResourceSerializer<?, ?> serializer) {
    synchronized (serializers) {
      String type = serializer.getType();
      if (serializers.containsKey(type)) {
        ResourceSerializer<?, ?> current = serializers.get(type);
        logger.warn("Replacing existing resource serializer implementation {} for type '{}'", current, type);
      }
      serializers.put(type, serializer);
    }
  }

  /**
   * Adds the new serializer to the list of registered resource serializer
   * implementations.
   * <p>
   * Note that existing registrations for the same key as returned by
   * {@link ResourceSerializer#getType()} will be replaced.
   * 
   * @param serializer
   *          the serializer
   */
  public void removeSerializer(ResourceSerializer<?, ?> serializer) {
    synchronized (serializers) {
      String type = serializer.getType();
      serializers.remove(type);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.repository.ResourceSerializerService#getSerializerByType(java.lang.String)
   */
  public ResourceSerializer<?, ?> getSerializerByType(String resourceType) {
    synchronized (serializers) {
      return serializers.get(resourceType);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.repository.ResourceSerializerService#getSerializerByMimeType(java.lang.String)
   */
  public ResourceSerializer<?, ?> getSerializerByMimeType(String mimeType) {
    ResourceSerializer<?, ?> serializerForMimeType = null;
    synchronized (serializers) {
      for (ResourceSerializer<?, ?> serializer : serializers.values()) {
        if (serializer.supports(mimeType)) {
          serializerForMimeType = serializer;
          break;
        }
      }
    }
    return serializerForMimeType;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.repository.ResourceSerializerService#getSerializers()
   */
  public Set<ResourceSerializer<?, ?>> getSerializers() {
    Set<ResourceSerializer<?, ?>> set = new HashSet<ResourceSerializer<?, ?>>();
    synchronized (serializers) {
      set.addAll(serializers.values());
    }
    return set;
  }

}
