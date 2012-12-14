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

package ch.entwine.weblounge.kernel.security;

import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.Security;
import ch.entwine.weblounge.dispatcher.SharedHttpContext;
import ch.entwine.weblounge.kernel.site.SiteManager;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.webconsole.WebConsoleSecurityProvider;
import org.osgi.framework.BundleContext;
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
import java.util.Hashtable;

import javax.servlet.Filter;

/**
 * Read the <code>security.xml</code> defining the general security constraints
 * from the bundle resources.
 * <p>
 * After the configuration is read, the service registers a {@link Filter} which
 * enforces the security policy at runtime.
 */
public class SpringSecurityConfigurationService implements ManagedService {

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

  /** The related spring security service */
  protected SpringSecurityServiceImpl securityService = null;

  /** The current bundle context */
  protected BundleContext bundleCtx = null;

  /** The spring security filter */
  protected SecurityFilter securityFilter = null;

  /** The web console security */
  protected WebConsoleSecurityProvider webConsoleProvider = null;

  /** The system password encoding */
  protected DigestType passwordEncoding = DEFAULT_ENCODING;

  /** Reference to the security marker */
  protected ServiceRegistration securityMarker = null;

  /** Is security enabled */
  protected boolean securityEnabled = true;

  /** The security filter registration */
  protected ServiceRegistration securityFilterRegistration = null;

  /** The registration for the web console security provider */
  protected ServiceRegistration webConsoleSecurityRegistration = null;

  /** The sites that are online */
  protected SiteManager sites = null;

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
    bundleCtx = ctx.getBundleContext();

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
    Filter defaultSecurityFilter = (Filter) springContext.getBean("springSecurityFilterChain");
    securityFilter = new SecurityFilter(securityService, sites, defaultSecurityFilter);

    // Create the web console security provider
    webConsoleProvider = new WebloungeWebConsoleSecurityProvider(securityService);

    // Activate the security filters
    if (securityEnabled) {
      startSecurity();
    } else {
      logger.info("Security is turned off by configuration");
    }

    // Tell the security service abut the current policy
    securityService.setEnabled(securityEnabled);

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
    logger.info("Tearing down spring security");

    // Unregister the security filters
    if (securityFilterRegistration != null) {
      stopSecurity();
    }

    // Remove the security marker
    if (securityMarker != null) {
      try {
        securityMarker.unregister();
      } catch (IllegalStateException e) {
        // Never mind, the service has been unregistered already
      } catch (Throwable t) {
        logger.error("Unregistering security context failed: {}", t.getMessage());
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
    if (properties == null) {
      logger.debug("No customized security configuration found");
      return;
    }

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

    // Tell the security service abut the current policy
    securityService.setEnabled(isEnabled);

    // Store the security enabled setting
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
      try {
        securityMarker.unregister();
      } catch (IllegalStateException e) {
        // Never mind, the service has been unregistered already
      } catch (Throwable t) {
        logger.error("Unregistering security marker failed: {}", t.getMessage());
      }
      publishSecurityMarker();
    }

    // Store the password encoding setting
    this.passwordEncoding = digestType;
  }

  /**
   * Activates security by registering both a security filter and a web console
   * security provider.
   */
  private void startSecurity() {
    logger.info("Enabling spring security");
    Dictionary<String, String> props = new Hashtable<String, String>();
    props.put("pattern", ".*");
    props.put(SharedHttpContext.PROPERTY_OSGI_HTTP_CONTEXT_ID, SharedHttpContext.HTTP_CONTEXT_ID);
    props.put("security", "weblounge");
    try {
      securityFilterRegistration = bundleCtx.registerService(Filter.class.getName(), securityFilter, props);
      logger.debug("Spring security context registered");
    } catch (Throwable t) {
      logger.error("Error registering security context: {}", t.getMessage());
    }

    logger.info("Securing the Felix management console");
    try {
      webConsoleSecurityRegistration = bundleCtx.registerService(WebConsoleSecurityProvider.class.getName(), webConsoleProvider, null);
      logger.debug("Web console security provider registered");
    } catch (Throwable t) {
      logger.error("Error registering web console security provider: {}", t.getMessage());
    }
  }

  /**
   * Registers the security marker in the OSGi registry.
   */
  private void publishSecurityMarker() {
    Dictionary<String, String> securityProperties = new Hashtable<String, String>();
    securityProperties.put(OPT_ENCODING, passwordEncoding.toString().toLowerCase());
    bundleCtx.registerService(Security.class.getName(), new Security() {
    }, securityProperties);
  }

  /**
   * Deactivates security by unregistering both the security filter and the web
   * console security provider.
   */
  private void stopSecurity() {
    logger.info("Disabling spring security");
    if (securityFilterRegistration != null) {
      try {
        securityFilterRegistration.unregister();
        logger.debug("Spring security context unregistered");
      } catch (IllegalStateException e) {
        // Never mind, the service has been unregistered already
      } catch (Throwable t) {
        logger.error("Unregistering security context", t.getMessage());
      }
    }

    logger.info("Disabling web console security provider");
    if (webConsoleSecurityRegistration != null) {
      try {
        webConsoleSecurityRegistration.unregister();
        logger.debug("Web console security provider unregistered");
      } catch (IllegalStateException e) {
        logger.debug("Web console security provider was already unregistered");
      } catch (Throwable t) {
        logger.error("Unregistering web console security provider", t.getMessage());
      }
    }
  }

  /**
   * Callback from OSGi to set the spring security service.
   * 
   * @param securityService
   *          the security service
   */
  void setSecurityService(SpringSecurityServiceImpl securityService) {
    this.securityService = securityService;
  }

  /**
   * Callback for OSGi to set the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void setSiteManager(SiteManager siteManager) {
    this.sites = siteManager;
  }

  /**
   * Callback for OSGi to remove the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void removeSiteManager(SiteManager siteManager) {
    this.sites = null;
  }

}
