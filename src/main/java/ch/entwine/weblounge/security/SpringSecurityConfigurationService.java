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

package ch.entwine.weblounge.security;

import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.Security;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Filter;

/**
 * Read the <code>security.xml</code> defining the general security constraints
 * from the bundle resources.
 * <p>
 * After the configuration is read, the service registers a {@link Filter} which
 * enforces the security policy at runtime.
 */
public class SpringSecurityConfigurationService implements BundleListener, ManagedService {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SpringSecurityConfigurationService.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.entwine.weblounge.security";

  /** Name of the configuration file */
  public static final String SECURITY_CONFIG_FILE = "/security/security.xml";

  /** Configuration key for the enabled/disabled configuration */
  public static final String OPT_ENABLED = "security.enabled";

  /** Configuration key for the password encoding configuration */
  public static final String OPT_ENCODING = "security.passwordencoding";

  /** The default password encoding */
  public static final DigestType DEFAULT_ENCODING = DigestType.md5;

  /** The current bundle */
  protected Bundle bundle = null;

  /** The spring security filter */
  protected Filter securityFilter = null;

  /** The system password encoding */
  protected DigestType passwordEncoding = DEFAULT_ENCODING;

  /** Reference to the security marker */
  protected ServiceRegistration securityMarker = null;

  /** Is security enabled */
  protected boolean securityEnabled = true;

  /** The security filter registration */
  protected Map<Bundle, ServiceRegistration> securityFilterRegistrations = new HashMap<Bundle, ServiceRegistration>();

  /**
   * Callback from the OSGi environment on service activation.
   * 
   * @param ctx
   *          the component context
   * @throws IOException
   *           if reading the service configuration fails
   * @throws ConfigurationException
   *           if the service configuration is malformed
   */
  void activate(ComponentContext ctx) throws IOException,
      ConfigurationException {
    BundleContext bundleCtx = ctx.getBundleContext();
    bundle = ctx.getBundleContext().getBundle();
    securityFilterRegistrations = new HashMap<Bundle, ServiceRegistration>();

    // Try to get hold of the service configuration
    ServiceReference configAdminRef = bundleCtx.getServiceReference(ConfigurationAdmin.class.getName());
    if (configAdminRef != null) {
      ConfigurationAdmin configAdmin = (ConfigurationAdmin) bundleCtx.getService(configAdminRef);
      Dictionary<?, ?> config = configAdmin.getConfiguration(SERVICE_PID).getProperties();
      if (config != null) {
        updated(config);
      } else {
        logger.debug("No customized security configuration found");
      }
    } else {
      logger.debug("No configuration admin service found while looking for security configuration");
    }

    // Create the spring security context
    URL securityConfig = bundleCtx.getBundle().getResource(SECURITY_CONFIG_FILE);
    ConfigurableOsgiBundleApplicationContext springContext = null;
    springContext = new OsgiBundleXmlApplicationContext(new String[] { securityConfig.toExternalForm() });
    springContext.setBundleContext(bundleCtx);
    springContext.refresh();

    // Get the security filter chain from the spring context
    securityFilter = (Filter) springContext.getBean("springSecurityFilterChain");

    // Activate the security filters
    if (securityEnabled) {
      logger.info("Activating spring security");
      startSecurity();
    } else {
      logger.warn("Security is turned off by configuration");
    }

    // Register the security marker
    publishSecurityMarker();
  }

  /**
   * Callback from OSGi environment on service inactivation.
   * 
   * @param ctx
   *          the component context
   */
  void deactivate(ComponentContext ctx) {
    ctx.getBundleContext().removeBundleListener(this);

    logger.info("Tearing down spring security");

    // Unregister the security filters
    if (securityFilterRegistrations != null) {
      stopSecurity();
    }

    // Remove the security marker
    if (securityMarker != null) {
      try {
        securityMarker.unregister();
      } catch (Throwable t) {
        logger.error("Unregistering security context for bundle '{}' failed: {}", bundle.getSymbolicName(), t.getMessage());
      }
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    String enabledProperty = (String) properties.get(OPT_ENABLED);
    boolean isEnabled = ConfigurationUtils.isTrue(enabledProperty, true);

    // Enable/disable security
    if (isEnabled != this.securityEnabled) {
      if (isEnabled) {
        startSecurity();
      } else {
        stopSecurity();
      }
    }

    this.securityEnabled = isEnabled;

    // Password encoding
    String passwordEncodingProperty = StringUtils.trimToNull((String) properties.get(OPT_ENCODING));
    DigestType digestType = DigestType.md5;
    if (passwordEncodingProperty != null) {
      try {
        digestType = DigestType.valueOf(passwordEncodingProperty);
      } catch (IllegalArgumentException e) {
        throw new ConfigurationException(OPT_ENCODING, "'" + passwordEncodingProperty + "' is not a valid encoding");
      }
    }
    if (!digestType.equals(passwordEncoding) && securityMarker != null) {
      passwordEncoding = digestType;
      securityMarker.unregister();
      publishSecurityMarker();
    }

    this.securityEnabled = isEnabled;
  }

  /**
   * Activates security by registering a security filter with active bundles.
   */
  private void startSecurity() {
    logger.info("Enabling spring security");
    for (Bundle b : bundle.getBundleContext().getBundles()) {
      if (b.getState() == Bundle.ACTIVE || b.getState() == Bundle.STARTING)
        registerSecurityFilter(b);
    }
    bundle.getBundleContext().addBundleListener(this);
  }

  /**
   * Registers the security marker in the OSGi registry.
   */
  private void publishSecurityMarker() {
    Dictionary<String, String> securityProperties = new Hashtable<String, String>();
    securityProperties.put(OPT_ENCODING, passwordEncoding.toString().toLowerCase());
    bundle.getBundleContext().registerService(Security.class.getName(), new Security() {
    }, securityProperties);
  }

  /**
   * Activates security by registering a security filter with active bundles.
   */
  private void stopSecurity() {
    logger.info("Disabling spring security");
    bundle.getBundleContext().removeBundleListener(this);
    if (securityFilterRegistrations != null) {
      for (Map.Entry<Bundle, ServiceRegistration> entry : securityFilterRegistrations.entrySet()) {
        Bundle bundle = entry.getKey();
        ServiceRegistration r = entry.getValue();
        try {
          r.unregister();
          logger.debug("Spring security context unregistered for bundle '{}'", bundle.getSymbolicName());
        } catch (Throwable t) {
          logger.error("Unregistering security context for bundle '{}' failed: {}", bundle.getSymbolicName(), t.getMessage());
        }
      }
    }
  }

  /**
   * Removes the registered filter from our list of filter registrations. As
   * this method is called only if the bundle is unregistered, all the services
   * associated with it will have been unregistered already, so there is no work
   * for us to do.
   * 
   * @param bundle
   *          the stopped bundle
   */
  private void unregisterSecurityFilter(Bundle bundle) {
    ServiceRegistration r = securityFilterRegistrations.remove(bundle);
    if (r == null)
      return;
    logger.debug("Spring security context unregistered for bundle '{}'", bundle.getSymbolicName());
  }

  /**
   * Registers a new security filter instance for the bundle that just started.
   * 
   * @param bundle
   *          the bundle
   */
  private void registerSecurityFilter(Bundle bundle) {
    BundleContext ctx = bundle.getBundleContext();

    // Only care about other weblounge bundles
    if (bundle == this.bundle || !bundle.getSymbolicName().startsWith("ch.entwine.weblounge"))
      return;

    Dictionary<String, String> props = new Hashtable<String, String>();
    props.put("urlPatterns", "/*");
    props.put("security", "Weblounge");
    try {
      ServiceRegistration registration = ctx.registerService(Filter.class.getName(), securityFilter, props);
      securityFilterRegistrations.put(bundle, registration);
      logger.debug("Spring security context registered for bundle '{}'", bundle.getSymbolicName());
    } catch (Throwable t) {
      logger.error("Error registering security context for bundle '{}': {}", bundle.getSymbolicName(), t.getMessage());
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
        registerSecurityFilter(event.getBundle());
        break;
      case BundleEvent.STOPPED:
        unregisterSecurityFilter(event.getBundle());
        break;
      default:
        break;
    }
  }

}
