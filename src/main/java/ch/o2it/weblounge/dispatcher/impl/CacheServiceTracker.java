/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.dispatcher.impl;

import ch.o2it.weblounge.cache.CacheService;
import ch.o2it.weblounge.common.request.ResponseCache;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CacheServiceTracker</code> watches OSGi services that implement the
 * {@link CacheService} interface and registers and unregisters the weblounge
 * dispatcher with the first service implementation to come.
 */
public class CacheServiceTracker extends ServiceTracker {

  /** Logger */
  private static final Logger log_ = LoggerFactory.getLogger(CacheServiceTracker.class);

  /** Main dispatcher */
  private WebloungeDispatcherServlet dispatcher = null;

  /**
   * Creates a new <code>CacheServiceTracker</code> that will, upon an appearing
   * <code>CacheService</code> implementation, register the cache service with
   * the dispatcher.
   * 
   * @param context
   *          the bundle context
   * @param dispatcher
   *          the dispatcher
   */
  CacheServiceTracker(BundleContext context,
      WebloungeDispatcherServlet dispatcher) {
    super(context, ResponseCache.class.getName(), null);
    this.dispatcher = dispatcher;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
   */
  @Override
  public Object addingService(ServiceReference reference) {
    log_.debug("Registering weblounge dispatcher with {}", reference.getBundle().getSymbolicName());
    ResponseCache cache = (ResponseCache) context.getService(reference);
    dispatcher.setResponseCache(cache);
    log_.info("Enabled response caching through {}", reference.getBundle().getSymbolicName());
    return cache;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#modifiedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void modifiedService(ServiceReference reference, Object service) {
    log_.info("Cache service was modified");
    super.modifiedService(reference, service);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void removedService(ServiceReference reference, Object service) {
    log_.info("Response caching disabled ({} disappeared)", reference.getBundle().getSymbolicName());
    dispatcher.setResponseCache(null);
    super.removedService(reference, service);
  }

}