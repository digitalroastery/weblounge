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

package ch.entwine.weblounge.kernel.shared;

import ch.entwine.weblounge.dispatcher.SharedHttpContext;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;

/**
 * This component registers this bundle's <code>/html</code> resource directory
 * with the http service under <code>/system/shared/</code> so that images,
 * cascading stylesheets and javascripts are available locally.
 */
public class WebloungeSharedResources implements ManagedService {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeSharedResources.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.entwine.weblounge.resources";

  /** Bundle directory containing the shared resources */
  public static final String RESOURCES_BUNDLE_DIR = "/html";

  /** Mountpoint to use when registering the shared resources */
  public static final String DEFAULT_RESOURCES_MOUNTPOINT = "/weblounge-shared";

  /** Option name for the shared resources mountpoint */
  public static final String OPT_RESOURCES_MOUNTPOINT = "resources.path";

  /** Option name for the external location of shared resources */
  public static final String OPT_EXT_RESOURCES = "resources.external.dir";

  /** Name of the current jQuery version */
  public static final String JQUERY_VERSION = "1.6.4";

  /** Name of the current jQuery Tools version */
  public static final String JQUERY_TOOLS_VERSION = "1.2.5";

  /** Directory with external resources */
  private File externalResourcesDir = null;

  /** Actual mountpoint for the shared resources */
  private String resourcesMountpoint = null;

  /** The http servlet registration */
  private ServiceRegistration servletRegistration = null;

  /** Bundle context */
  private BundleContext bundleContext = null;

  /**
   * Callback for OSGi's declarative services component inactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void activate(ComponentContext context) throws Exception {
    bundleContext = context.getBundleContext();

    // Try to get hold of the service configuration
    ServiceReference configAdminRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
    if (configAdminRef != null) {
      ConfigurationAdmin configAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminRef);
      Dictionary<?, ?> config = configAdmin.getConfiguration(SERVICE_PID).getProperties();
      if (config != null) {
        updated(config);
      } else {
        logger.debug("No customized configuration for content repository found");
        resourcesMountpoint = DEFAULT_RESOURCES_MOUNTPOINT;
      }
    } else {
      logger.debug("No configuration admin service found while looking for content repository configuration");
    }

    if (servletRegistration == null) {
      register(resourcesMountpoint, externalResourcesDir, bundleContext.getBundle());
    }
  }

  /**
   * Callback for OSGi's declarative services component inactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) throws Exception {
    logger.info("Stopping serving of shared resources", this);
    if (servletRegistration != null) {
      try {
        servletRegistration.unregister();
      } catch (IllegalStateException e) {
        // Never mind, the service has been unregistered already
      } catch (Throwable t) {
        logger.error("Unregistering shared resource servlet failed: {}", t.getMessage());
      }
      servletRegistration = null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;

    // Mountpoint
    String mountpoint = (String) properties.get(OPT_RESOURCES_MOUNTPOINT);
    if (StringUtils.isNotBlank(mountpoint)) {
      resourcesMountpoint = mountpoint;
      logger.debug("Configured value for the shared resource mountpoint is '{}'", mountpoint);
    } else {
      resourcesMountpoint = DEFAULT_RESOURCES_MOUNTPOINT;
      logger.debug("Using default mountpoint {} for shared resources", resourcesMountpoint);
    }

    // Optional external resources directory
    String externalResources = (String) properties.get(OPT_EXT_RESOURCES);
    if (StringUtils.isNotBlank(externalResources)) {
      externalResourcesDir = new File(externalResources);
      logger.debug("Configured external shared resources directory at '{}'", externalResources);
    } else {
      logger.debug("Shared resources will be served from bundle '{}'", bundleContext.getBundle().getSymbolicName());
    }

    // Register the new servlet
    if (servletRegistration != null) {
      try {
        servletRegistration.unregister();
      } catch (IllegalStateException e) {
        // Never mind, the service has been unregistered already
      } catch (Throwable t) {
        logger.error("Unregistering shared resources servlet failed: {}", t.getMessage());
      }
    }
    servletRegistration = register(resourcesMountpoint, externalResourcesDir, bundleContext.getBundle());
    logger.info("Serving shared resources at {}", resourcesMountpoint);
  }

  /**
   * Returns the path to the root of the shared resources.
   * 
   * @return the shared resources mountpoint
   */
  public String getResourcesMountpoint() {
    return resourcesMountpoint;
  }

  /**
   * Registers the shared resources directory under the given context.
   * 
   * @param context
   *          the context path
   * @param resourcesDir
   *          directory containing the resources to serve
   * @param the
   *          contextual bundle
   */
  private ServiceRegistration register(String context, File resourcesDir,
      Bundle bundle) {
    Dictionary<String, String> registrationProperties = new Hashtable<String, String>();
    registrationProperties.put(SharedHttpContext.CONTEXT_ID, SharedHttpContext.WEBLOUNGE_CONTEXT_ID);
    registrationProperties.put(SharedHttpContext.ALIAS, context);
    registrationProperties.put(SharedHttpContext.SERVLET_NAME, "weblounge.sharedresources");
    Servlet servlet = new WebloungeResourcesServlet(resourcesDir, bundle, RESOURCES_BUNDLE_DIR);
    return bundleContext.registerService(Servlet.class.getName(), servlet, registrationProperties);
  }

}
