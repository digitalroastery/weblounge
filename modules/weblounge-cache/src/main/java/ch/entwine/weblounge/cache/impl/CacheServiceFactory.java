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

package ch.entwine.weblounge.cache.impl;

import ch.entwine.weblounge.cache.CacheService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Service factory that will return a cache for each configuration that is
 * published to the {@link org.osgi.service.cm.ConfigurationAdmin}.
 * <p>
 * The following properties need to be present in order for a cache instance to
 * be started.
 * <ul>
 * <li><code>cache.id</code> - identifier that needs to match the site
 * identifier</li>
 * <li><code>cache.name</code> - a human readable name for this cache</li>
 * <li><code>cache.diskStorePath</code> - path to the cache extension on disk</li>
 * </ul>
 * <p>
 * For additional configuration, take a look at the sample configuration that
 * comes with Weblounge. When registered with the system using the pid
 * <code>ch.entwine.weblounge.cache</code>, it will be used as the basis for
 * configuration objects.
 */
public class CacheServiceFactory implements ManagedServiceFactory {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(CacheServiceFactory.class);

  /** The factory's service pid */
  static final String SERVICE_PID = "ch.entwine.weblounge.cache.factory";

  /** Service registrations per configuration pid */
  private Map<String, ServiceRegistration> services = new HashMap<String, ServiceRegistration>();

  /** This service factory's bundle context */
  private BundleContext bundleCtx = null;

  /**
   * Sets a reference to the service factory's component context.
   * <p>
   * This method is called from the OSGi context upon service creation.
   * 
   * @param ctx
   *          the component context
   */
  protected void activate(ComponentContext ctx) {
    this.bundleCtx = ctx.getBundleContext();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#getName()
   */
  public String getName() {
    return "Cache service factory";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String,
   *      java.util.Dictionary)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void updated(String pid, Dictionary properties)
      throws ConfigurationException {

    // is this an update to an existing service?
    if (services.containsKey(pid)) {
      ServiceRegistration registration = services.get(pid);
      ManagedService service = (ManagedService) bundleCtx.getService(registration.getReference());
      service.updated(properties);
    }

    // Create a new cache service instance
    else {

      String id = (String) properties.get(CacheServiceImpl.OPT_ID);
      String name = (String) properties.get(CacheServiceImpl.OPT_NAME);
      String diskStorePath = (String) properties.get(CacheServiceImpl.OPT_DISKSTORE_PATH);

      try {
        CacheServiceImpl cache = new CacheServiceImpl(id, name, diskStorePath);
        cache.updated(properties);

        // Register the service
        String serviceType = CacheService.class.getName();
        properties.put("service.pid", pid);
        services.put(pid, bundleCtx.registerService(serviceType, cache, properties));
      } catch (Throwable t) {
        logger.error("Failed to create cache for site '{}': {}", id, t.getMessage());
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
   */
  public void deleted(String pid) {
    ServiceRegistration registration = services.remove(pid);
    if (registration == null) {
      logger.debug("Cache service with pid '{}' was never registered and can therefore not be deleted", pid);
      return;
    }
    CacheService cache = (CacheService) bundleCtx.getService(registration.getReference());
    try {
      registration.unregister();
    } catch (IllegalStateException e) {
      // Never mind, the service has been unregistered already
    } catch (Throwable t) {
      logger.error("Unregistering cache service failed: {}", t.getMessage());
    }
    cache.shutdown();
  }

}
