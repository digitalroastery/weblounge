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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.site.SiteService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The site tracker watches site services coming and going and registers them
 * with the weblounge dispatcher.
 */
public class SiteTracker extends ServiceTracker {

  /** Logging instance */
  private static final Logger log_ = LoggerFactory.getLogger(SiteTracker.class);

  /** The sites */
  private List<Site> sites = null;

  /** Maps server names to sites */
  private Map<String, Site> sitesByServerName = null;

  /**
   * Creates a site tracker.
   */
  SiteTracker(BundleContext context) {
    super(context, SiteService.class.getName(), null);
    sites = new ArrayList<Site>();
    sitesByServerName = new HashMap<String, Site>();
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
  Site getSiteByName(String serverName) {
    Site site = sitesByServerName.get(serverName);
    if (site != null)
      return site;

    // There is obviously no direct match. Therefore, try to find a
    // wildcard match
    for (Map.Entry<String, Site> e : sitesByServerName.entrySet()) {
      String alias = e.getKey();
      if (serverName.matches(alias)) {
        site = e.getValue();
        log_.info("Registering {} for site ", serverName, site);
        sitesByServerName.put(serverName, site);
        return site;
      }
    }
    
    log_.debug("Lookup for {} did not match any site", serverName);
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
   */
  public Object addingService(ServiceReference reference) {
    SiteService service = (SiteService) super.addingService(reference);
    Site site = service.getSite();
    registerSite(site);
    log_.info("Site {} started", site);
    return super.addingService(reference);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  public void modifiedService(ServiceReference reference, Object service) {
    SiteService s = (SiteService) service;
    Site site = s.getSite();
    log_.debug("Site {} modified", site);
    log_.info("Restarting site {}", site);
    unregisterSite(site);
    registerSite(site);
    log_.info("Site {} started", site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  public void removedService(ServiceReference reference, Object service) {
    SiteService s = (SiteService) service;
    Site site = s.getSite();
    unregisterSite(site);
    super.removedService(reference, service);
    log_.info("Site {} stopped", site);
  }

  /**
   * Registers a new site with the site tracker, so the site can be accessed
   * using the configured server names.
   * 
   * @param site
   *          the site to register
   * @throws ConfigurationException
   *           if a site tries to register with an already registered server
   *           name
   */
  private void registerSite(Site site) throws ConfigurationException {
    synchronized (sites) {
      sites.add(site);
      for (String name : site.getHostNames()) {
        if (sitesByServerName.get(name).equals(site))
          throw new ConfigurationException("Another site is already registered to " + name);
        sitesByServerName.put(name, site);
      }
    }
  }

  /**
   * Unregisters the given site by removing all the servernames that have been
   * registered for it.
   * 
   * @param site
   *          the site to remove
   */
  private void unregisterSite(Site site) {
    synchronized (sites) {
      sites.remove(site);
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
  }

}