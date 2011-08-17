/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.kernel.publisher;

import ch.entwine.weblounge.kernel.http.BundleResourceHttpContext;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
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

  /** Mapping of registered endpoints */
  protected Map<String, ServiceRegistration> resourceRegistrations = new HashMap<String, ServiceRegistration>();

  /** Mapping of http context for endpoints */
  protected Map<String, ServiceRegistration> contextRegistrations = new HashMap<String, ServiceRegistration>();

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
   * OSGi callback on component inactivation.
   */
  protected void deactivate() {
    bundleTracker.close();
    synchronized (resourceRegistrations) {
      for (Map.Entry<String, ServiceRegistration> entry : resourceRegistrations.entrySet()) {
        String bundleSymbolicName = entry.getKey();
        ServiceRegistration context = contextRegistrations.get(bundleSymbolicName);
        ServiceRegistration servlet = entry.getValue();
        logger.debug("Unpublishing resources at {}", bundleSymbolicName);
        context.unregister();
        servlet.unregister();
      }
      resourceRegistrations.clear();
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

    BundleContext bundleCtx = bundle.getBundleContext();
    String httpContextId = "weblounge." + bundle.getSymbolicName().toLowerCase() + "-static";

    // Create and register the bundle http context
    try {
      BundleResourceHttpContext bundleHttpContext = new BundleResourceHttpContext(bundle, resourcePath);
      Dictionary<String, String> contextRegistrationProperties = new Hashtable<String, String>();
      contextRegistrationProperties.put("httpContext.id", httpContextId);
      ServiceRegistration contextRegistration = bundleCtx.registerService(HttpContext.class.getName(), bundleHttpContext, contextRegistrationProperties);
      contextRegistrations.put(bundle.getSymbolicName(), contextRegistration);
    } catch (Throwable t) {
      logger.error("Error publishing resources service at " + contextPath, t);
    }

    // We use the newly added bundle's context to register this service, so
    // when that bundle shuts down, it brings down this servlet with it
    logger.info("Publishing resources from bundle://{} at {}", bundle.getSymbolicName(), contextPath);

    // Create and register the resource servlet
    try {
      Servlet servlet = new ResourcesServlet(bundle, resourcePath, welcomeFile);
      Dictionary<String, String> servletRegistrationProperties = new Hashtable<String, String>();
      servletRegistrationProperties.put("alias", contextPath);
      servletRegistrationProperties.put("servlet-name", bundle.getSymbolicName() + "-static");
      servletRegistrationProperties.put("httpContext.id", httpContextId);
      ServiceRegistration servletRegistration = bundleCtx.registerService(Servlet.class.getName(), servlet, servletRegistrationProperties);
      resourceRegistrations.put(bundle.getSymbolicName(), servletRegistration);
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
    if (StringUtils.isBlank(contextPath))
      return;

    synchronized (resourceRegistrations) {
      ServiceRegistration context = contextRegistrations.remove(bundle.getSymbolicName());
      ServiceRegistration servlet = resourceRegistrations.remove(bundle.getSymbolicName());
      if (servlet == null)
        return;

      logger.info("Unpublishing resources at {}", contextPath);

      // Remove the servlet from the http service
      try {
        context.unregister();
        servlet.unregister();
      } catch (Throwable t) {
        logger.error("Unable to unregister bundle resources at " + contextPath, t);
      }
    }
  }

}
