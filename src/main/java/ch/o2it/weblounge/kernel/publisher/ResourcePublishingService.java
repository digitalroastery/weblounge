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

package ch.o2it.weblounge.kernel.publisher;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Servlet;

/**
 * This service will publish static resources in bundles that expose the
 * following headers in their manifest:
 * <ul>
 * <li><code>Http-Alias</code> path to mount the resources to</li>
 * <li><code>Http-Resource</code> the resources to expose</li>
 * <li><code>Http-Welcome</code> the welcome file</li>
 * </ul>
 */
public class ResourcePublishingService implements BundleTrackerCustomizer {
  
  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourcePublishingService.class); 

  /** The bundle tracker */
  protected BundleTracker bundleTracker = null;

  /** The bundle header identifying the alias to mount the static resource to */
  public static final String HTTP_CONTEXT = "Http-Context";

  /** The bundle header identifying the path to the static resource */
  public static final String HTTP_RESOURCE = "Http-Resource";

  /** The bundle header identifying the welcome file */
  public static final String HTTP_WELCOME = "Http-Welcome";

  /** The OSGi http service */
  protected HttpService httpService = null;

  /** The default http context */
  protected HttpContext httpContext = null;

  /** Mapping of registered endpoints */
  protected Map<String, Servlet> servletMap = new HashMap<String, Servlet>();

  /**
   * OSGi callback on component activation.
   * 
   * @param ctx
   *          the component context
   */
  protected void activate(ComponentContext ctx) {
    int stateMask = Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING;
    bundleTracker = new BundleTracker(ctx.getBundleContext(), stateMask, this);
    bundleTracker.open();
  }

  /**
   * OSGi callback on component deactivation.
   */
  protected void deactivate() {
    bundleTracker.close();
    for (Map.Entry<String, Servlet> entry : servletMap.entrySet()) {
      String contextPath = entry.getKey();
      Servlet servlet = entry.getValue();
      logger.debug("Unpublishing resources at {}", contextPath);
      httpService.unregister(contextPath);
      servlet.destroy();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
   *      org.osgi.framework.BundleEvent)
   */
  public Object addingBundle(Bundle bundle, BundleEvent event) {
    String resourcePath = (String) bundle.getHeaders().get(HTTP_RESOURCE);
    String contextPath = (String) bundle.getHeaders().get(HTTP_CONTEXT);
    String welcomeFile = (String) bundle.getHeaders().get(HTTP_WELCOME);
    
    // Are there any relevant manifest headers?
    if (StringUtils.isBlank(resourcePath) || StringUtils.isBlank(contextPath)) {
      logger.debug("No resource manifest headers found in bundle {}", bundle.getSymbolicName());
      return bundle;
    }

    // Make sure nothing is mounted there already
    if (servletMap.containsKey(contextPath)) {
      logger.warn("Unable to publish resources from bundle {} at {}: context path is already in use", bundle.getSymbolicName(), contextPath);
      return bundle;
    }
    
    Servlet servlet = new ResourcesServlet(bundle, resourcePath, welcomeFile);

    // We use the newly added bundle's context to register this service, so
    // when that bundle shuts down, it brings
    // down this servlet with it
    logger.info("Publishing resources from bundle://{} at {}", bundle.getSymbolicName(), contextPath);
    
    try {
      httpService.registerServlet(contextPath, servlet, new Hashtable<String, String>(), httpContext);
      servletMap.put(contextPath, servlet);
    } catch (Throwable t) {
      logger.error("Error publishing resources service at " + contextPath, t);
    }      

    return bundle;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
   *      org.osgi.framework.BundleEvent, java.lang.Object)
   */
  public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
    // Nothing to do
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
   *      org.osgi.framework.BundleEvent, java.lang.Object)
   */
  public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
    String contextPath = (String) bundle.getHeaders().get(HTTP_CONTEXT);
    Servlet servlet = servletMap.remove(contextPath);

    if (StringUtils.isBlank(contextPath) || servlet == null)
      return;

    logger.debug("Unpublishing resources at {}", contextPath);

    // Remove the servlet from the http service
    try {
      httpService.unregister(contextPath);
    } catch (Throwable t) {
      logger.error("Unable to unregister rest endpoint " + contextPath, t);
    }

    // Destroy the servlet
    servlet.destroy();
  }

  /**
   * OSGi callback to set a reference to the <code>HttpService</code>.
   * 
   * @param httService
   *          the http service
   */
  void setHttpService(HttpService httpService) {
    this.httpService = httpService;
    this.httpContext = httpService.createDefaultHttpContext();
  }

}
