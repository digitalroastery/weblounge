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

package ch.entwine.weblounge.kernel.runtime;

import ch.entwine.weblounge.common.site.Environment;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * The environment service provides the default environment for a Weblounge
 * instance.
 * <p>
 * The environment can be configured in the configuration with pid
 * <code>ch.entwine.weblounge.environment</code>
 */
public class EnvironmentService implements ManagedService {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(EnvironmentService.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.entwine.weblounge.environment";

  /** Name of the option to look up the default environment */
  public static final String OPT_ENVIRONMENT = "environment";

  /** Default environment setting */
  public static final Environment DEFAULT_ENVIRONMENT = Environment.Production;

  /** The current environment */
  protected Environment environment = Environment.Production;

  /** The environment registration */
  private ServiceRegistration registration = null;

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
      }
    } else {
      logger.debug("No configuration admin service found while looking for runtime environment configuration");
    }

    // Make sure we use some environment
    if (environment == null) {
      environment = DEFAULT_ENVIRONMENT;
      logger.info("Runtime environment defaults to '{}'", environment.toString().toLowerCase());
    }

    // Register the default environment
    if (registration == null) {
      logger.debug("Registering default runtime environment");
      registration = bundleContext.registerService(Environment.class.getName(), environment, null);
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
    logger.debug("Unregistering default runtime environment", this);
    if (registration != null) {
      try {
        registration.unregister();
      } catch (IllegalStateException e) {
        // Never mind, the service has been unregistered already
      } catch (Throwable t) {
        logger.error("Unregistering runtime environment failed: {}", t.getMessage());
      }
      registration = null;
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

    // Environment
    Environment env = null;
    String environmentValue = StringUtils.trimToNull((String) properties.get(OPT_ENVIRONMENT));
    if (StringUtils.isNotBlank(environmentValue)) {
      try {
        env = Environment.valueOf(StringUtils.capitalize(environmentValue));
        logger.debug("Configured value for the default runtime environment is '{}'", env.toString().toLowerCase());
      } catch (IllegalArgumentException e) {
        throw new ConfigurationException(OPT_ENVIRONMENT, environmentValue);
      }
    } else {
      env = DEFAULT_ENVIRONMENT;
      logger.debug("Using default value '{}' for runtime environment", env.toString().toLowerCase());
    }

    // Did the setting change?
    if (!env.equals(environment)) {
      this.environment = env;
      if (registration != null) {
        try {
          registration.unregister();
        } catch (IllegalStateException e) {
          // Never mind, the service has been unregistered already
        } catch (Throwable t) {
          logger.error("Unregistering runtime environment failed: {}", t.getMessage());
        }
      }
      registration = bundleContext.registerService(Environment.class.getName(), environment, null);
      logger.info("Runtime environment set to '{}'", environment.toString().toLowerCase());
    }
  }

}
