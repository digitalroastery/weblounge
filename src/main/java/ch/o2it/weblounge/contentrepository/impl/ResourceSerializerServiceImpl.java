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

package ch.o2it.weblounge.contentrepository.impl;

import ch.o2it.weblounge.contentrepository.ResourceSerializer;
import ch.o2it.weblounge.contentrepository.ResourceSerializerFactory;
import ch.o2it.weblounge.contentrepository.ResourceSerializerService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that will watch out for resource serializers and then register it
 * with the <code>ResourceSerializerFactory</code> to allow for static lookup of
 * resource serializers.
 */
public class ResourceSerializerServiceImpl implements ResourceSerializerService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourceSerializerServiceImpl.class);

  /** The registered content repositories */
  private Map<String, ResourceSerializer<?>> serializers = new HashMap<String, ResourceSerializer<?>>();

  /** The resource serializer tracker */
  private ResourceSerializerTracker siteTracker = null;

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component activation fails
   */
  void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();
    logger.info("Starting resource serializer service");
    ResourceSerializerFactory.setResourceSerializerService(this);
    siteTracker = new ResourceSerializerTracker(this, bundleContext);
    siteTracker.open();
    logger.debug("Content repository service activated");
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) throws Exception {
    logger.debug("Deactivating resource serializer service");
    ResourceSerializerFactory.setResourceSerializerService(null);
    siteTracker.close();
    siteTracker = null;
    logger.info("Content repository service stopped");
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
  public void registerSerializer(ResourceSerializer<?> serializer) {
    synchronized (serializers) {
      String type = serializer.getType();
      if (serializers.containsKey(type)) {
        ResourceSerializer<?> current = serializers.get(type);
        logger.warn("Replacing extisting resource serializer implementation {} for type '{}'", current, type);
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
  public void unregisterSerializer(ResourceSerializer<?> serializer) {
    synchronized (serializers) {
      String type = serializer.getType();
      serializers.remove(type);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ResourceSerializerService#getSerializer(java.lang.String)
   */
  public ResourceSerializer<?> getSerializer(String resourceType) {
    synchronized (serializers) {
      return serializers.get(resourceType);
    }
  }

  /**
   * Service tracker for {@link ResourceSerializer} instances.
   */
  private static class ResourceSerializerTracker extends ServiceTracker {

    /** The enclosing content repository factory */
    private ResourceSerializerServiceImpl serializerService = null;

    /**
     * Creates a new site tracker which will call back to the
     * <code>ContentRepositoryFactory</code> that created it.
     * 
     * @param service
     *          the serializer service
     * @param context
     *          the bundle context
     */
    public ResourceSerializerTracker(ResourceSerializerServiceImpl service,
        BundleContext context) {
      super(context, ResourceSerializer.class.getName(), null);
      this.serializerService = service;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
      ResourceSerializer<?> serializer = (ResourceSerializer<?>)super.addingService(reference);
      serializerService.registerSerializer(serializer);
      return serializer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
      serializerService.unregisterSerializer((ResourceSerializer<?>) service);
      super.removedService(reference, service);
    }

  }

}
