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
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
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
public class SpringSecurityConfigurationService implements BundleListener {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SpringSecurityConfigurationService.class);

  /** Name of the configuration file */
  public static final String SECURITY_CONFIG_FILE = "/security/security.xml";

  /** URL to the security configuration */
  protected URL securityConfig = null;

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
    securityFilterRegistrations = new HashMap<Bundle, ServiceRegistration>();
    securityConfig = ctx.getBundleContext().getBundle().getResource(SECURITY_CONFIG_FILE);
  }

  /**
   * Callback from OSGi environment on service inactivation.
   */
  void deactivate() {
    if (securityFilterRegistrations != null) {
      for (Map.Entry<Bundle, ServiceRegistration> entry : securityFilterRegistrations.entrySet()) {
        Bundle bundle = entry.getKey();
        ServiceRegistration r = entry.getValue();
        r.unregister();
        logger.info("Spring security context unregistered for bundle '{}'", bundle.getSymbolicName());
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
    securityFilterRegistrations.remove(bundle);
    logger.info("Spring security context unregistered for bundle '{}'", bundle.getSymbolicName());
  }

  /**
   * Registers a new security filter instance for the bundle that just started.
   * 
   * @param bundle
   *          the bundle
   */
  private void registerSecurityFilter(Bundle bundle) {
    BundleContext ctx = bundle.getBundleContext();
    ConfigurableOsgiBundleApplicationContext springContext = null;

    // Create a new spring security context
    springContext = new OsgiBundleXmlApplicationContext(new String[] { securityConfig.toExternalForm() });
    springContext.setBundleContext(ctx);

    // Refresh the spring application context
    springContext.refresh();

    // Get the security filter chain from the spring context
    Object securityFilterChain = springContext.getBean("springSecurityFilterChain");

    Dictionary<String, String> props = new Hashtable<String, String>();
    props.put("urlPatterns", "/*");
    props.put("pattern", ".*");
    props.put("service.ranking", "1");
    ServiceRegistration registration = ctx.registerService(Filter.class.getName(), securityFilterChain, props);
    securityFilterRegistrations.put(bundle, registration);
    logger.info("Spring security context registered for bundle '{}'", bundle.getSymbolicName());
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
