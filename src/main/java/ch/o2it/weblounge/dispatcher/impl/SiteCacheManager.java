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
import ch.o2it.weblounge.common.impl.url.PathUtils;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.kernel.SiteManager;
import ch.o2it.weblounge.kernel.SiteServiceListener;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * The <code>CacheServiceTracker</code> watches OSGi services that implement the
 * {@link ch.o2it.weblounge.cache.CacheService} interface and registers and
 * unregisters the weblounge dispatcher with the first service implementation to
 * come.
 */
public class SiteCacheManager implements SiteServiceListener {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(SiteCacheManager.class);

  /** The site manager */
  protected SiteManager siteManager = null;
  
  /**
   * Registers an instance of the response cache for the given site.
   * 
   * @param site
   *          the site to register a cache for
   * @param bundle
   *          the site's bundle
   */
  private void registerCache(Site site, Bundle bundle) {
    BundleContext ctx = bundle.getBundleContext();
    String filter = "(site=" + site.getIdentifier() + ")";
    try {
      if (ctx.getServiceReferences(CacheService.class.getName(), filter) != null) {
        logger.warn("Found existing cache service for site '{}'", site);
        return;
      }

      // Have the service factory create a new reference
      ServiceReference serviceReference = ctx.getServiceReference(CacheService.class.getName());
      if (serviceReference == null) {
        logger.warn("Unable to create a response cache for site '{}'. Is the cache service running?", site);
        return;
      }

      String siteId = site.getIdentifier();
      String siteName = site.getName();
      String diskStorePath = PathUtils.concat(System.getProperty("java.io.tmpdir"), "weblounge", "sites", siteId, "cache");

      // Initialize the cache
      CacheService cache = (CacheService) ctx.getService(serviceReference);
      if (cache == null) {
        logger.error("Unable to locate cache service for service reference");
        return;
      }
      cache.init(siteId, siteName, diskStorePath);

      // Register the cache
      Dictionary<String, Object> serviceProps = new Hashtable<String, Object>();
      serviceProps.put("site", site);
      ctx.registerService(CacheService.class.getName(), cache, serviceProps);
      logger.info("Registered cache service for site '{}'", site);

    } catch (InvalidSyntaxException e) {
      logger.error("Error looking up cache service for site '" + site.getIdentifier() + "'", e);
    }
  }

  /**
   * Unregisters the response cache for this site.
   * 
   * @param site
   *          the site
   * @param bundle
   *          the site's bundle
   */
  private void unregisterCache(Site site, Bundle bundle) {
    BundleContext ctx = bundle.getBundleContext();
    String filter = "(site=" + site.getIdentifier() + ")";
    ServiceReference[] serviceReferences;
    try {
      serviceReferences = ctx.getServiceReferences(CacheService.class.getName(), filter);
      if (serviceReferences != null) {
        for (ServiceReference ref : serviceReferences) {
          ctx.ungetService(ref);
        }
        logger.info("Unregistered cache service for site '{}'", site);
      }
    } catch (InvalidSyntaxException e) {
      logger.error("Error looking up cache service for site '" + site.getIdentifier() + "'", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.kernel.SiteServiceListener#siteAppeared(ch.o2it.weblounge.common.site.Site,
   *      org.osgi.framework.ServiceReference)
   */
  public void siteAppeared(Site site, ServiceReference reference) {
    registerCache(site, reference.getBundle());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.kernel.SiteServiceListener#siteDisappeared(ch.o2it.weblounge.common.site.Site)
   */
  public void siteDisappeared(Site site) {
    unregisterCache(site, siteManager.getSiteBundle(site));
  }

  /**
   * OSGi callback that will set the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void setSiteManager(SiteManager siteManager) {
    this.siteManager = siteManager;

    // Register a cache for all current site
    Iterator<Site> sites = siteManager.sites();
    while (sites.hasNext()) {
      Site site = sites.next();
      registerCache(site, siteManager.getSiteBundle(site));
    }

    // Ask for notification of new and removed sites
    siteManager.addSiteListener(this);
  }

  /**
   * OSGi callback that removes the reference to the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void removeSiteManager(SiteManager siteManager) {
    this.siteManager = null;

    // Stop notifications of new and removed sites
    siteManager.removeSiteListener(this);

    // Unregister caches for all current sites
    Iterator<Site> sites = siteManager.sites();
    while (sites.hasNext()) {
      Site site = sites.next();
      unregisterCache(site, siteManager.getSiteBundle(site));
    }
  }

}