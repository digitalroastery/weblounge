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

import ch.o2it.weblounge.dispatcher.DispatcherService;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.dispatcher.SiteRegistrationService;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * The dispatcher coordinates dispatching of requests in weblounge. It first
 * registers with the http service if available and then starts the tracking of
 * sites, so the weblounge dispatcher servlet knows where to dispatch requests
 * to.
 * <p>
 * This means that by deactivating this service, no dispatching will be done in
 * weblounge and all sites will effectively be offline.
 */
public class DispatcherServiceImpl implements DispatcherService, ManagedService {

  /** Logging instance */
  private static final Logger logger = LoggerFactory.getLogger(DispatcherServiceImpl.class);

  /** The main dispatcher servlet */
  private WebloungeDispatcherServlet dispatcher = null;
  
  /** Tracker for the http service */
  private HttpServiceTracker httpTracker = null;

  /** Tracker for the cache service */
  private CacheServiceTracker cacheTracker = null;

  /**
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;
    logger.debug("Updating dispatcher service properties");
  }

  /**
   * Callback from the OSGi environment to activate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  public void activate(ComponentContext context) {
    BundleContext bundleContext = context.getBundleContext();
    logger.debug("Activating weblounge dispatcher");

    // Create an http tracker and make sure it forwards to our servlet
    dispatcher = new WebloungeDispatcherServlet();
    logger.trace("Start looking for http service implementations");
    httpTracker = new HttpServiceTracker(bundleContext, dispatcher);
    httpTracker.open();

    // Start looking for a cache service
    logger.trace("Start looking for response cache service implementations");
    cacheTracker = new CacheServiceTracker(bundleContext, dispatcher);
    cacheTracker.open();

    logger.debug("Weblounge dispatcher activated");
  }

  /**
   * Callback from the OSGi environment to deactivate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  public void deactivate(ComponentContext context) {
    logger.debug("Deactivating weblounge dispatcher");

    // Get rid of the http tracker
    httpTracker.close();
    httpTracker = null;

    // Get rid of the http tracker
    cacheTracker.close();
    cacheTracker = null;

    logger.debug("Weblounge dispatcher deactivated");
  }

  /**
   * Callback from the OSGi environment when the site locator is activated.
   * 
   * @param siteLocator
   *          the site locator
   */
  public void setSiteLocator(SiteRegistrationService siteLocator) {
    dispatcher.setSiteLocator(siteLocator);
  }

  /**
   * Callback from the OSGi environment when the site locator is deactivated.
   * 
   * @param siteLocator
   *          the site locator service
   */
  public void removeSiteLocator(SiteRegistrationService siteLocator) {
    dispatcher.setSiteLocator(null);
  }

  /**
   * Registers the request handler with the main dispatcher servlet.
   * 
   * @param handler
   *          the request handler
   */
  public void addRequestHandler(RequestHandler handler) {
    logger.info("Registering {}", handler);
    dispatcher.addRequestHandler(handler);
  }

  /**
   * Removes the request handler from the main dispatcher servlet.
   * 
   * @param handler
   *          the request handler
   */
  public void removeRequestHandler(RequestHandler handler) {
    logger.info("Unregistering {}", handler);
    dispatcher.removeRequestHandler(handler);
  }

}