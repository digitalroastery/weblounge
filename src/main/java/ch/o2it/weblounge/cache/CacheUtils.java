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

package ch.o2it.weblounge.cache;

import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.site.Site;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <code>CacheUtils</code> are meant to provide an easy way to gain access to a
 * site's cache service instance.
 */
public class CacheUtils {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(CacheUtils.class);

  /** The cache service */
  private static Map<String, CacheService> cacheServices = new HashMap<String, CacheService>();

  /** The cache service tracker */
  private ServiceTracker cacheServiceTracker = null;

  /**
   * Callback from OSGi declarative services on component startup.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    cacheServiceTracker = new CacheServiceTracker(ctx.getBundleContext());
    cacheServiceTracker.open();
  }

  /**
   * Callback from OSGi declarative services on component shutdown.
   */
  void deactivate() {
    cacheServiceTracker.close();
  }

  /**
   * Returns the cache instance with the given identifier or <code>null</code>
   * if no such cache is found.
   * 
   * @param id
   *          the identifier
   * @return the cache
   */
  public static CacheService getCache(String id) {
    return cacheServices.get(id);
  }

  /**
   * Removes those entries from the cache that is associated with the given site
   * that match all of the given tags.
   * 
   * @param site
   *          the site
   * @param tags
   *          the tags
   * @throws IllegalArgumentException
   *           if either one of <code>site</code> or <code>tags</code> is
   *           <code>null</code>
   */
  public static void invalidate(Site site, CacheTag... tags) {
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    if (tags == null || tags.length == 0)
      throw new IllegalArgumentException("Cache tags must not be null");

    CacheService cache = cacheServices.get(site.getIdentifier());
    if (cache != null) {
      logger.debug("Invalidating entries that are tagged {}", tags);
      cache.invalidate(tags);
    }
  }

  /**
   * Adds the cache service instance to the list of cache services.
   * 
   * @param cache
   *          the cache service
   */
  void addCacheService(CacheService cache) {
    String id = cache.getIdentifier();
    cacheServices.put(id, cache);
  }

  /**
   * Removes the cache service instance from the list of cache services.
   * 
   * @param cache
   *          the cache service
   */
  void removeCacheService(CacheService cache) {
    String id = cache.getIdentifier();
    cacheServices.remove(id);
  }

  /**
   * Implementation of a <code>ServiceTracker</code> that is tracking instances
   * of type {@link CacheService}.
   */
  private class CacheServiceTracker extends ServiceTracker {

    /**
     * Creates a new cache service tracker that is using the given bundle
     * context to look up service instances.
     * 
     * @param ctx
     *          the bundle context
     */
    CacheServiceTracker(BundleContext ctx) {
      super(ctx, CacheService.class.getName(), null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
      CacheService cache = (CacheService) super.addingService(reference);
      addCacheService(cache);
      return cache;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
      removeCacheService((CacheService) service);
    }

  }

}
