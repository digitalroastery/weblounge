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

import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteException;
import ch.o2it.weblounge.common.site.SiteListener;
import ch.o2it.weblounge.dispatcher.ActionRequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>SiteObserver</code> is listening for site events such as the
 * startup and the shutdown of sites and registers or unregisters various site
 * components with system components.
 */
public final class SiteManagerService implements SiteListener {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(SiteManagerService.class);

  /** The action request handler */
  private ActionRequestHandler actionRequestHandler = null;

  /** Running sites */
  private List<Site> runningSites = new ArrayList<Site>();

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.SiteListener#siteStarted(ch.o2it.weblounge.common.site.Site)
   */
  public void siteStarted(Site site) {
    runningSites.add(site);
    if (actionRequestHandler != null) {
      registerSite(site);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.SiteListener#siteStopped(ch.o2it.weblounge.common.site.Site)
   */
  public void siteStopped(Site site) {
    runningSites.remove(site);
    if (actionRequestHandler != null) {
      unregisterSite(site);
    }
  }

  /**
   * Callback from the OSGi environment when a new site is activated.
   * 
   * @param site
   *          the site
   */
  void addSite(Site site) {
    site.addSiteListener(this);
    if (site.isStartedAutomatically()) {
      try {
        logger.debug("Starting site '{}'", site);
        site.start();
      } catch (IllegalStateException e) {
        logger.error("Site '{}' could not be started: {}", e.getMessage(), e);
      } catch (SiteException e) {
        logger.error("Site '{}' could not be started: {}", e.getMessage(), e);
      }
    }
  }

  /**
   * Callback from the OSGi environment when a site is deactivated.
   * 
   * @param site
   *          the site
   */
  void removeSite(Site site) {
    site.removeSiteListener(this);
    try {
      if (site.isRunning())
        site.stop();
    } catch (Exception e) {
      logger.error("Error stopping site '{}'", e);
    }
  }

  /**
   * Callback from the OSGi environment which registers the request handler with
   * the site observer.
   * 
   * @param handler
   *          the action request handler
   */
  void addActionRequestHandler(ActionRequestHandler handler) {
    logger.debug("Registering {}", handler);
    actionRequestHandler = handler;
    registerAllSites();
  }

  /**
   * Callback from the OSGi environment which removes the action request handler
   * from the site observer.
   * 
   * @param handler
   *          the action request handler
   */
  void removeActionRequestHandler(ActionRequestHandler handler) {
    logger.debug("Unregistering {}", handler);
    unregisterAllSites();
    actionRequestHandler = null;
  }

  /**
   * Registers all sites with the action request handler.
   */
  private void registerAllSites() {
    for (Site site : runningSites) {
      registerSite(site);
    }
  }

  /**
   * Registers the site with the action request handler.
   */
  private void registerSite(Site site) {
    for (Module module : site.getModules()) {
      for (Action action : module.getActions()) {
        actionRequestHandler.register(action);
      }
    }
  }

  /**
   * Unregisters all sites from the action request handler.
   */
  private void unregisterAllSites() {
    for (Site site : runningSites) {
      unregisterSite(site);
    }
  }

  /**
   * Unregisters the site from the action request handler.
   */
  private void unregisterSite(Site site) {
    for (Module module : site.getModules()) {
      for (Action action : module.getActions()) {
        actionRequestHandler.unregister(action);
      }
    }
  }

}
