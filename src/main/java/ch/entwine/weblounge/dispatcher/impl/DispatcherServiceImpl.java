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

package ch.entwine.weblounge.dispatcher.impl;

import ch.entwine.weblounge.cache.CacheService;
import ch.entwine.weblounge.dispatcher.DispatcherService;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.SiteDispatcherService;

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

  /**
   * Creates a new instance of the dispatcher service.
   */
  public DispatcherServiceImpl() {
    dispatcher = new WebloungeDispatcherServlet();
  }

  /**
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;
    logger.debug("Updating dispatcher service properties");
  }

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component activation fails
   */
  void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();
    logger.debug("Activating weblounge dispatcher");

    // Create an http tracker and make sure it forwards to our servlet
    logger.trace("Start looking for http service implementations");
    httpTracker = new HttpServiceTracker(bundleContext, dispatcher);
    httpTracker.open();

    logger.debug("Weblounge dispatcher activated");
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) throws Exception {
    logger.debug("Deactivating weblounge dispatcher");

    // Get rid of the http tracker
    httpTracker.close();
    httpTracker = null;

    logger.debug("Weblounge dispatcher deactivated");
  }

  /**
   * Callback from the OSGi environment when the site dispatcher is activated.
   * 
   * @param siteDispatcher
   *          the site dispatcher
   */
  void setSiteDispatcher(SiteDispatcherService siteDispatcher) {
    dispatcher.setSiteDispatcher(siteDispatcher);
  }

  /**
   * Callback from the OSGi environment when the site dispatcher is deactivated.
   * 
   * @param siteDispatcher
   *          the site dispatcher service
   */
  void removeSiteDispatcher(SiteDispatcherService siteDispatcher) {
    dispatcher.setSiteDispatcher(null);
  }

  /**
   * Registers the request handler with the main dispatcher servlet.
   * 
   * @param handler
   *          the request handler
   */
  void addRequestHandler(RequestHandler handler) {
    logger.info("Registering {}", handler);
    dispatcher.addRequestHandler(handler);
  }

  /**
   * Removes the request handler from the main dispatcher servlet.
   * 
   * @param handler
   *          the request handler
   */
  void removeRequestHandler(RequestHandler handler) {
    logger.info("Unregistering {}", handler);
    dispatcher.removeRequestHandler(handler);
  }

  /**
   * Registers the response cache with the main dispatcher servlet.
   * 
   * @param cache
   *          the response cache
   */
  void addCacheService(CacheService cache) {
    dispatcher.addResponseCache(cache);
  }

  /**
   * Removes the response cache from the main dispatcher servlet.
   * 
   * @param cache
   *          the response cache
   */
  void removeCacheService(CacheService cache) {
    dispatcher.removeResponseCache(cache);
  }

}