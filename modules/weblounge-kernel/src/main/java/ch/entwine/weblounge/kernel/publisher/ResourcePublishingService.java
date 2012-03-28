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

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
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
public class ResourcePublishingService implements BundleListener {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourcePublishingService.class);

  /** The bundle header identifying the alias to mount the static resource to */
  public static final String HTTP_CONTEXT = "Http-Context";

  /** The bundle header identifying the path to the static resource */
  public static final String HTTP_RESOURCE = "Http-Resource";

  /** The bundle header identifying the welcome file */
  public static final String HTTP_WELCOME = "Http-Welcome";

  /** Mapping of registered endpoints */
  protected Map<String, ServiceRegistration> resourceRegistrations = new HashMap<String, ServiceRegistration>();

  /**
   * OSGi callback on component activation.
   * 
   * @param ctx
   *          the component context
   */
  protected void activate(ComponentContext ctx) {
    BundleContext bundleCtx = ctx.getBundleContext();

    // Process bundles that have already been started
    for (Bundle bundle : bundleCtx.getBundles()) {
      if (bundle.getState() == Bundle.ACTIVE)
        registerResources(bundle);
    }

    bundleCtx.addBundleListener(this);
  }

  /**
   * OSGi callback on component inactivation.
   * 
   * @param ctx
   *          the component context
   */
  protected void deactivate(ComponentContext ctx) {
    ctx.getBundleContext().removeBundleListener(this);

    synchronized (resourceRegistrations) {
      for (Map.Entry<String, ServiceRegistration> entry : resourceRegistrations.entrySet()) {
        String bundleSymbolicName = entry.getKey();
        ServiceRegistration servlet = entry.getValue();
        logger.debug("Unpublishing resources at {}", bundleSymbolicName);
        servlet.unregister();
      }
      resourceRegistrations.clear();
    }
  }

  /**
   * Registers any static resources that are declared in the bundle's manifest
   * as a servlet.
   * 
   * @param bundle
   *          the active bundle
   */
  public void registerResources(Bundle bundle) {

    String resourcePath = (String) bundle.getHeaders().get(HTTP_RESOURCE);
    String contextPath = (String) bundle.getHeaders().get(HTTP_CONTEXT);
    String welcomeFile = (String) bundle.getHeaders().get(HTTP_WELCOME);

    // Are there any relevant manifest headers?
    if (StringUtils.isBlank(resourcePath) || StringUtils.isBlank(contextPath)) {
      logger.debug("No resource manifest headers found in bundle {}", bundle.getSymbolicName());
      return;
    }

    BundleContext bundleCtx = bundle.getBundleContext();

    logger.info("Publishing resources from bundle://{} at {}", bundle.getSymbolicName(), contextPath);

    // Create and register the resource servlet
    try {
      Servlet servlet = new ResourcesServlet(bundle, resourcePath, welcomeFile);
      Dictionary<String, String> servletRegistrationProperties = new Hashtable<String, String>();
      servletRegistrationProperties.put("alias", contextPath);
      servletRegistrationProperties.put("servlet-name", bundle.getSymbolicName() + "-static");
      ServiceRegistration servletRegistration = bundleCtx.registerService(Servlet.class.getName(), servlet, servletRegistrationProperties);
      resourceRegistrations.put(bundle.getSymbolicName(), servletRegistration);
    } catch (Throwable t) {
      logger.error("Error publishing resources service at " + contextPath, t);
    }

  }

  /**
   * Removes any resource registrations associated with this bundle.
   * 
   * @param bundle
   *          the stopped bundle
   */
  public void unregisterResources(Bundle bundle) {
    String contextPath = (String) bundle.getHeaders().get(HTTP_CONTEXT);
    if (StringUtils.isBlank(contextPath)) {
      logger.debug("Bundle '{}' does not expose static resources", bundle.getSymbolicName());
      return;
    }

    // The services were registered using the bundle's context. Since that
    // bundle has been stopped now, the service has been unregistered by the
    // OSGi service registry already
    synchronized (resourceRegistrations) {
      resourceRegistrations.remove(bundle.getSymbolicName());
      logger.info("Unpublishing resources at {}", contextPath);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
   */
  public void bundleChanged(BundleEvent event) {
    switch (event.getType()) {
      case BundleEvent.STARTED:
        registerResources(event.getBundle());
        break;
      case BundleEvent.STOPPED:
        unregisterResources(event.getBundle());
        break;
      default:
        break;
    }
  }

}
