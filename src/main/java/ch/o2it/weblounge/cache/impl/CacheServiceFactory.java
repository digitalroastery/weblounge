/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.cache.CacheService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Service factory that will return a cache for each configuration that is
 * published to the {@link ConfigurationAdmin}.
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
 * <code>ch.o2it.weblounge.cache</code>, it will be used as the basis for
 * configuration objects.
 */
public class CacheServiceFactory implements ManagedServiceFactory {

  /** The factory's service pid */
  static final String SERVICE_PID = "ch.o2it.weblounge.cache.factory";
  
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

    // Create a new cache service instance
    String id = (String) properties.get(CacheServiceImpl.OPT_ID);
    String name = (String) properties.get(CacheServiceImpl.OPT_NAME);
    String diskStorePath = (String) properties.get(CacheServiceImpl.OPT_DISKSTORE_PATH);
    CacheServiceImpl cache = new CacheServiceImpl(id, name, diskStorePath);
    cache.updated(properties);

    // Register the service
    String serviceType = CacheService.class.getName();
    properties.put("service.pid", pid);
    services.put(pid, bundleCtx.registerService(serviceType, cache, properties));
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
   */
  public void deleted(String pid) {
    ServiceRegistration registration = services.remove(pid);
    registration.unregister();
  }

}
