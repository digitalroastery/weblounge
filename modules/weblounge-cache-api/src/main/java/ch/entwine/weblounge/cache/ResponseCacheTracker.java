/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.cache;

import ch.entwine.weblounge.common.request.ResponseCache;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>ResponseCacheTracker</code> watches instances of
 * {@link CacheService} in the OSGi registry and registers and unregisters them
 * with the content repository if the site is matched.
 */
public class ResponseCacheTracker extends ServiceTracker {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(ResponseCacheTracker.class);

  /** The response cache */
  protected CacheService cache = null;

  /** The site identifier */
  protected String siteId = null;

  /**
   * Creates a new tracker for {@link CacheService} instances.
   * 
   * @param context
   *          the bundle context
   * @param siteId
   *          the site identifier
   */
  public ResponseCacheTracker(BundleContext context, String siteId) {
    super(context, CacheService.class.getName(), null);
    this.siteId = siteId;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
   */
  @Override
  public Object addingService(ServiceReference reference) {
    CacheService cache = (CacheService) context.getService(reference);
    logger.trace("Found response cache '{}'", cache.getIdentifier());
    if (siteId.equals(cache.getIdentifier())) {
      this.cache = cache;
    }
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
    CacheService cache = (CacheService) service;
    logger.trace("Response cache '{}' went away", cache.getIdentifier());
    if (siteId.equals(cache.getIdentifier())) {
      this.cache = null;
    }
    if (reference.getBundle() != null) {
      try {
        super.removedService(reference, service);
      } catch (IllegalStateException e) {
        // The service has been removed, probably due to bundle shutdown
      } catch (Throwable t) {
        logger.warn("Error removing service: {}", t.getMessage());
      }
    }
  }

  /**
   * Returns the response cache or <code>null</code> if no cache is currently
   * registered.
   * 
   * @return the cache
   */
  public ResponseCache getCache() {
    return cache;
  }

}
