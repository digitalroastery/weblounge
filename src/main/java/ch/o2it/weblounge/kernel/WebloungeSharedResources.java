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

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component registers this bundle's <code>/html</code> resource directory
 * with the http service under <code>/system/shared/</code> so that images,
 * cascading stylesheets and javascripts are available locally.
 */
public class WebloungeSharedResources {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeSharedResources.class);

  /** Bundle directory containing the shared resources */
  public static final String SHARED_RESOURCES_BUNDLE_DIR = "/html";

  /** Mountpoint to use when registering the shared resources */
  public static final String SHARED_RESOURCES_MOUNTPOINT = "/system/resources";
  
  /** The http service */
  private HttpService httpService = null;

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void activate(ComponentContext context) throws Exception {
    logger.info("Starting to serve shared weblounge resources at {}", SHARED_RESOURCES_MOUNTPOINT);
    HttpContext httpContext = httpService.createDefaultHttpContext();
    httpService.registerResources(SHARED_RESOURCES_MOUNTPOINT, SHARED_RESOURCES_BUNDLE_DIR, httpContext);
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
    logger.info("Stopping serving of shared weblounge resources", this);
    httpService.unregister(SHARED_RESOURCES_MOUNTPOINT);
  }

  /**
   * Callback for OSGi's declarative services to set a reference to the OSGi
   * <code>HttpService</code>.
   * 
   * @param httpService
   *          the http service
   */
  void setHttpService(HttpService httpService) {
    this.httpService = httpService;
  }

}
