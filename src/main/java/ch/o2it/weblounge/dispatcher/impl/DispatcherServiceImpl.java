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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
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
public class DispatcherServiceImpl implements DispatcherService, BundleActivator, ManagedService {

  /** Logging instance */
  private static final Logger log_ = LoggerFactory.getLogger(DispatcherServiceImpl.class);

  /** Tracker for the http service */
  private HttpServiceTracker httpTracker = null;

  /** Tracker for weblounge sites */
  private SiteTracker siteTracker = null;

  /**
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary properties) throws ConfigurationException {
    log_.debug("Updating dispatcher service properties");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    log_.debug("Weblounge dispatcher activating");

    // Start site service tracking
    siteTracker = new SiteTracker(context);
    siteTracker.open();

    // Create an http tracker and make sure it forwards to our servlet
    WebloungeDispatcherServlet dispatcher = new WebloungeDispatcherServlet(siteTracker);
    httpTracker = new HttpServiceTracker(context, dispatcher);
    httpTracker.open();

    log_.debug("Weblounge dispatcher activated");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    log_.debug("Weblounge dispatcher deactivating");

    // Get rid of the http tracker
    httpTracker.close();
    httpTracker = null;

    // Get rid of the site tracker
    siteTracker.close();
    siteTracker = null;

    log_.debug("Weblounge dispatcher deactivated");
  }

}