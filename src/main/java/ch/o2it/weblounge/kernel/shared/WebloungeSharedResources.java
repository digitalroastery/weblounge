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

package ch.o2it.weblounge.kernel.shared;


import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * This component registers this bundle's <code>/html</code> resource directory
 * with the http service under <code>/system/shared/</code> so that images,
 * cascading stylesheets and javascripts are available locally.
 */
public class WebloungeSharedResources implements ManagedService {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeSharedResources.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.o2it.weblounge.resources";

  /** Bundle directory containing the shared resources */
  public static final String RESOURCES_BUNDLE_DIR = "/html";

  /** Mountpoint to use when registering the shared resources */
  public static final String DEFAULT_RESOURCES_MOUNTPOINT = "/weblounge-shared";

  /** Option name for the shared resources mountpoint */
  public static final String OPT_RESOURCES_MOUNTPOINT = "resources.path";

  /** Option name for the external location of shared resources */
  public static final String OPT_EXT_RESOURCES = "resources.external.dir";

  /** Directory with external resources */
  private File externalResourcesDir = null;
  
  /** Actual mountpoint for the shared resources */
  private String resourcesMountpoint = null;

  /** The http service */
  private HttpService httpService = null;

  /** The http context */
  private HttpContext httpContext = null;
  
  /** The servlet used to serve both bundle and external resources */
  private HttpServlet servlet = null;
  
  /** Bundle context */
  private BundleContext bundleContext = null;

  /**
   * Callback for OSGi's declarative services component dactivation.
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

    logger.info("Starting to serve shared weblounge resources at {}", resourcesMountpoint);
    
    try {
      httpContext = httpService.createDefaultHttpContext();
      if (servlet != null) {
        httpService.registerServlet(resourcesMountpoint, servlet, null, httpContext);
        logger.info("Added {} to shared weblounge resources", externalResourcesDir);
      } else {
        httpService.registerResources(resourcesMountpoint, RESOURCES_BUNDLE_DIR, httpContext);
      }
    } catch (ServletException e) {
      logger.error("Error registering shared resources servlet at " + resourcesMountpoint, e);
    }
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
    httpService.unregister(resourcesMountpoint);
    httpContext = null;
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
    String currentMountpoint = resourcesMountpoint;
    String mountpoint = (String) properties.get(OPT_RESOURCES_MOUNTPOINT);
    if (StringUtils.trimToNull(mountpoint) != null) {
      resourcesMountpoint = mountpoint;
      logger.debug("Configured value for the shared resource mountpoint is '{}'", mountpoint);
    }

    // Mountpoint
    String externalResources = (String) properties.get(OPT_EXT_RESOURCES);
    if (StringUtils.trimToNull(externalResources) != null) {
      externalResourcesDir = new File(externalResources);
      servlet = new WebloungeResourcesServlet(externalResourcesDir, bundleContext.getBundle(), RESOURCES_BUNDLE_DIR);
      logger.debug("Configured external shared resources directory at '{}'", externalResources);
    } else {
      servlet = null;
    }

    // Update the registration
    if (currentMountpoint != null) {
      try {
        httpService.unregister(currentMountpoint);
        logger.info("Serving shared weblounge resources at {}", resourcesMountpoint);
        if (servlet != null) {
          httpService.registerServlet(resourcesMountpoint, servlet, null, httpContext);
          logger.info("Added {} to shared weblounge resources", externalResourcesDir);
        } else {
          httpService.registerResources(resourcesMountpoint, RESOURCES_BUNDLE_DIR, httpContext);
        }
      } catch (NamespaceException e) {
        logger.error("Error registering shared resources at " + resourcesMountpoint, e);
      } catch (ServletException e) {
        logger.error("Error registering shared resources servlet at " + resourcesMountpoint, e);
      }
    }
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
