/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.dispatcher.impl;

import ch.entwine.weblounge.dispatcher.SharedHttpContext;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * This class registers a shared servlet context in the service registry which
 * can be found using the <code>httpcontext.id</code> property, looking for the
 * value <code>weblounge</code>.
 */
public class SharedHttpContextRegistration {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SharedHttpContextRegistration.class);

  /** The bundle context */
  protected BundleContext bundleContext = null;

  /** The reference to the http context */
  protected HttpContext httpContext = null;

  /** The context service registration */
  protected ServiceRegistration contextServiceRegistration = null;

  /**
   * Callback from OSGi on component activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    bundleContext = ctx.getBundleContext();
    Properties properties = new Properties();
    properties.put(SharedHttpContext.PROPERTY_HTTP_CONTEXT_ID, SharedHttpContext.HTTP_CONTEXT_ID);
    contextServiceRegistration = bundleContext.registerService(HttpContext.class.getName(), httpContext, properties);
    logger.info("Publishing shared http context");
  }

  /**
   * Callback from OSGi on component activation.
   * 
   * @param ctx
   *          the component context
   */
  void deactivate(ComponentContext ctx) {
    if (contextServiceRegistration != null) {
      try {
        contextServiceRegistration.unregister();
        logger.info("Retracting shared http context");
      } catch (IllegalStateException e) {
        logger.info("Http context registration was invalid");
      }
    }
  }

  /**
   * Callback from OSGi to announce the availability of the http service.
   * 
   * @param httpService
   *          the http service
   */
  void setHttpService(HttpService httpService) {
    httpContext = httpService.createDefaultHttpContext();
  }

}
