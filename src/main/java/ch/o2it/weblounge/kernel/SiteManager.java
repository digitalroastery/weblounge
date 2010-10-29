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

package ch.o2it.weblounge.kernel;

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteException;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * The site manager watches site services coming and going and makes them
 * available by id, server name etc.
 */
public class SiteManager {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SiteManager.class);

  /** The site tracker */
  private SiteTracker siteTracker = null;

  /** The sites */
  private List<Site> sites = new ArrayList<Site>();

  /** Maps server names to sites */
  private Map<String, Site> sitesByServerName = new HashMap<String, Site>();

  /** Maps sites to osgi bundles */
  private Map<Site, Bundle> siteBundles = new HashMap<Site, Bundle>();

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component activation fails
   */
  void activate(ComponentContext context) throws Exception {
    logger.info("Starting site dispatcher");

    BundleContext bundleContext = context.getBundleContext();
    siteTracker = new SiteTracker(this, bundleContext);
    siteTracker.open();

    logger.debug("Site manager activated");
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) {
    logger.debug("Deactivating site manager");

    siteTracker.close();
    siteTracker = null;

    logger.info("Site manager stopped");
  }

  /**
   * Returns the site with the given site identifier or <code>null</code> if no
   * such site is currently registered.
   * 
   * @param identifier
   *          the site identifier
   * @return the site
   */
  public Site findSiteByIdentifier(String identifier) {
    for (Site site : sites) {
      if (site.getIdentifier().equals(identifier)) {
        return site;
      }
    }
    return null;
  }

  /**
   * Returns the site associated with the given server name.
   * <p>
   * Note that the server name is expected to not end with a trailing slash, so
   * please pass in <code>www.o2it.ch</code> instead of
   * <code>www.o2it.ch/</code>.
   * 
   * @param serverName
   *          the server name, e.g. <code>www.o2it.ch</code>
   * @return the site
   */
  public Site findSiteByName(String serverName) {
    Site site = sitesByServerName.get(serverName);
    if (site != null)
      return site;

    // There is obviously no direct match. Therefore, try to find a
    // wildcard match
    for (Map.Entry<String, Site> e : sitesByServerName.entrySet()) {
      String alias = e.getKey();

      try {
        // convert the host wildcard (ex. *.domain.tld) to a valid regex (ex.
        // .*\.domain\.tld)
        alias = alias.replace(".", "\\.");
        alias = alias.replace("*", ".*");
        if (serverName.matches(alias)) {
          site = e.getValue();
          logger.info("Registering {} for site ", serverName, site);
          sitesByServerName.put(serverName, site);
          return site;
        }
      } catch (PatternSyntaxException ex) {
        logger.warn("Error while trying to find a host wildcard match: ".concat(ex.getMessage()));
      }
    }

    logger.debug("Lookup for {} did not match any site", serverName);
    return null;
  }

  /**
   * Returns the OSGi bundle that contains the given site or <code>null</code>
   * if no such site has been registered.
   * 
   * @param site
   *          the site
   * @return the site's OSGi bundle
   */
  public Bundle getSiteBundle(Site site) {
    if (site == null)
      throw new IllegalArgumentException("Parameter 'site' must not be null");
    return siteBundles.get(site);
  }

  /**
   * Returns an iteration of all currently registered sites.
   * 
   * @return the sites
   */
  public Iterator<Site> sites() {
    List<Site> site = new ArrayList<Site>();
    site.addAll(sites);
    return site.iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#addSite(ch.o2it.weblounge.common.site.Site,
   *      org.osgi.framework.ServiceReference)
   */
  void addSite(Site site, ServiceReference reference) {
    synchronized (sites) {
      sites.add(site);
      siteBundles.put(site, reference.getBundle());
      for (String name : site.getHostNames()) {
        if (site.equals(sitesByServerName.get(name))) {
          logger.error("Another site is already registered to " + name);
          continue;
        }
        sitesByServerName.put(name, site);
      }
    }

    logger.debug("Site '{}' registered", site);

    // Start the site
    if (site.isStartedAutomatically()) {
      try {
        logger.debug("Starting site '{}'", site);
        // TODO: Make sure there is a *running* content repository for this site
        site.start();
      } catch (IllegalStateException e) {
        logger.error("Site '{}' could not be started: {}", e.getMessage(), e);
      } catch (SiteException e) {
        logger.error("Site '{}' could not be started: {}", e.getMessage(), e);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#removeSite(ch.o2it.weblounge.common.site.Site)
   */
  void removeSite(Site site) {

    // Stop the site if it's running
    try {
      if (site.isRunning())
        site.stop();
    } catch (Throwable t) {
      logger.error("Error stopping site '{}'", site.getIdentifier(), t);
    }

    // Remove it from the registry
    synchronized (sites) {
      sites.remove(site);
      siteBundles.remove(site);
      List<String> namesToRemove = new ArrayList<String>();
      for (Map.Entry<String, Site> entry : sitesByServerName.entrySet()) {
        if (site.equals(entry.getValue())) {
          namesToRemove.add(entry.getKey());
        }
      }
      for (String serverName : namesToRemove) {
        sitesByServerName.remove(serverName);
      }
    }
    logger.debug("Site {} unregistered", site);
  }

  /**
   * This tracker is used to track <code>Site</code> services. Once a site is
   * detected, it registers that site with the
   * <code>SiteDispatcherService</code>.
   */
  private final class SiteTracker extends ServiceTracker {

    /** The site dispatcher */
    private SiteManager siteManager = null;

    /**
     * Creates a new <code>SiteTracker</code>.
     * 
     * @param siteManager
     *          the site dispatcher
     * @param context
     *          the site dispatcher's bundle context
     */
    public SiteTracker(SiteManager siteManager, BundleContext context) {
      super(context, Site.class.getName(), null);
      this.siteManager = siteManager;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public Object addingService(ServiceReference reference) {
      Site site = (Site) super.addingService(reference);
      siteManager.addSite(site, reference);
      return site;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    public void removedService(ServiceReference reference, Object service) {
      Site site = (Site) service;
      siteManager.removeSite(site);
      super.removedService(reference, service);
    }

  }

}