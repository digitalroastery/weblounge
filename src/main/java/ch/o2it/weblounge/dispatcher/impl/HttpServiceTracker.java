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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Properties;

import javax.servlet.ServletException;

/**
 * The <code>HttpServiceTracker</code> watches OSGi web service instances and
 * registers and unregisters the weblounge dispatcher with the first service
 * implementation to come.
 */
public class HttpServiceTracker extends ServiceTracker {

  /** Logger */
  private static final Logger log_ = LoggerFactory.getLogger(HttpServiceTracker.class);

  /** Main dispatcher */
  private WebloungeDispatcherServlet dispatcher = null;
  
  /** Context path of this dispatcher */
  private final static String contextPath = "/";

  /**
   * Creates a new HTTP service tracker that will, upon an appearing http
   * service, register the dispatcher servlet.
   * 
   * @param context
   *          the bundle context
   * @param dispatcher
   *          the dispatcher
   */
  HttpServiceTracker(BundleContext context,
      WebloungeDispatcherServlet dispatcher) {
    super(context, HttpService.class.getName(), null);
    this.dispatcher = dispatcher;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
   */
  @Override
  public Object addingService(ServiceReference reference) {
    HttpService httpService = null;

    // Register the weblounge dispatcher
    try {
      log_.debug("Registering weblounge dispatcher with http service {}", reference.getBundle().getSymbolicName());
      httpService = (HttpService) context.getService(reference);

      HttpContext httpContext = httpService.createDefaultHttpContext();
      Dictionary<?, ?> initParams = new Properties();
      httpService.registerServlet(contextPath, dispatcher, initParams, httpContext);
      log_.info("Weblounge dispatcher hooked up with http service {}", httpService.getClass().getName());

    } catch (ServletException e) {
      log_.error("Error registering weblounge dispatcher with {}: {}", httpService, e.getMessage());
      httpService = null;
    } catch (NamespaceException e) {
      log_.error("Namespace error registering weblounge dispatcher with {}: {}", httpService, e.getMessage());
      httpService = null;
    }

    return httpService;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#modifiedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void modifiedService(ServiceReference reference, Object service) {
    log_.info("Http service was modified");
    super.modifiedService(reference, service);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void removedService(ServiceReference reference, Object service) {
    log_.info("Weblounge dispatcher disconnected from {}", service.getClass().getName());
    ((HttpService) service).unregister(contextPath);
    super.removedService(reference, service);
  }

}
