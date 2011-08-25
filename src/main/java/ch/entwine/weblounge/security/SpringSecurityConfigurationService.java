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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

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
public class SpringSecurityConfigurationService implements SynchronousBundleListener {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SpringSecurityConfigurationService.class);

  /** Name of the configuration file */
  public static final String SECURITY_CONFIG_FILE = "/security/security.xml";

  /** The current bundle */
  protected Bundle bundle = null;

  /** The spring security filter */
  protected Filter securityFilter = null;

  /** The security filter registration */
  protected Map<Bundle, ServiceRegistration> securityFilterRegistrations = new HashMap<Bundle, ServiceRegistration>();

  /**
   * Callback from the OSGi environment on service activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    BundleContext bundleCtx = ctx.getBundleContext();
    bundle = ctx.getBundleContext().getBundle();
    securityFilterRegistrations = new HashMap<Bundle, ServiceRegistration>();

    // Create the spring security context
    URL securityConfig = bundleCtx.getBundle().getResource(SECURITY_CONFIG_FILE);
    ConfigurableOsgiBundleApplicationContext springContext = null;
    springContext = new OsgiBundleXmlApplicationContext(new String[] { securityConfig.toExternalForm() });
    springContext.setBundleContext(bundleCtx);
    springContext.refresh();

    // Get the security filter chain from the spring context
    securityFilter = (Filter) springContext.getBean("springSecurityFilterChain");

    logger.info("Activating spring security");

    // Process existing bundles
    for (Bundle b : ctx.getBundleContext().getBundles()) {
      if (b.getState() == Bundle.ACTIVE || b.getState() == Bundle.STARTING)
        registerSecurityFilter(b);
    }

    // Register for new ones
    bundleCtx.addBundleListener(this);
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
      case BundleEvent.STARTING:
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
