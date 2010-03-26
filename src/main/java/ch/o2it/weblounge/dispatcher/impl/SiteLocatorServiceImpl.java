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

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.dispatcher.SiteLocatorService;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The dispatcher coordinates dispatching of requests in weblounge. It first
 * registers with the http service if available and then starts the tracking of
 * sites, so the weblounge dispatcher servlet knows where to dispatch requests
 * to.
 * <p>
 * This means that by deactivating this service, no dispatching will be done in
 * weblounge and all sites will effectively be offline.
 */
public class SiteLocatorServiceImpl implements SiteLocatorService, ManagedService {

  /** Logging instance */
  private static final Logger log_ = LoggerFactory.getLogger(SiteLocatorServiceImpl.class);

  /** The sites */
  private List<Site> sites = new ArrayList<Site>();

  /** Maps server names to sites */
  private Map<String, Site> sitesByServerName = new HashMap<String, Site>();

  /**
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary properties) throws ConfigurationException {
    log_.debug("Updating site locator service properties");
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.dispatcher.SiteLocatorService#findSiteByIdentifier(java.lang.String)
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
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.dispatcher.SiteLocatorService#findSiteByName(java.lang.String)
   */
  public Site findSiteByName(String serverName) {
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
   * @see ch.o2it.weblounge.dispatcher.SiteLocatorService#findSiteByRequest(javax.servlet.http.HttpServletRequest)
   */
  public Site findSiteByRequest(HttpServletRequest request) {
    if (request == null)
      throw new IllegalArgumentException("Request must not be null");
    return findSiteByName(request.getServerName());
  }

  /**
   * Callback from the OSGi environment when a new site is activated.
   * 
   * @param site
   *          the site
   */
  public void addSite(Site site) {
    synchronized (sites) {
      sites.add(site);
      for (String name : site.getHostNames()) {
        if (site.equals(sitesByServerName.get(name))) {
          log_.error("Another site is already registered to " + name);
          continue;
        }
        sitesByServerName.put(name, site);
      }
    }
    log_.debug("Site {} registered", site);
  }

  /**
   * Callback from the OSGi environment when a site is deactivated.
   * 
   * @param site
   *          the site
   */
  public void removeSite(Site site) {
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
    log_.debug("Site {} unregistered", site);
  }

}