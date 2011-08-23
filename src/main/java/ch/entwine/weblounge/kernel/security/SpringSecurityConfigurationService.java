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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

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
public class SpringSecurityConfigurationService {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SpringSecurityConfigurationService.class);

  /** Name of the configuration file */
  public static final String SECURITY_CONFIG_FILE = "/security/security.xml";

  /** The spring context */
  protected ConfigurableOsgiBundleApplicationContext springContext = null;

  /** The security filter registration */
  protected ServiceRegistration securityFilterRegistration = null;

  /**
   * Callback from the OSGi environment on service activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    BundleContext bundleCtx = ctx.getBundleContext();

    // Load the configuration and create a spring security context
    URL securityConfig = bundleCtx.getBundle().getResource(SECURITY_CONFIG_FILE);
    springContext = new OsgiBundleXmlApplicationContext(new String[] { securityConfig.toExternalForm() });
    springContext.setBundleContext(bundleCtx);

    // Refresh the spring application context
    springContext.refresh();
    
    // Get the security filter chain from the spring context
    Object securityFilterChain = springContext.getBean("springSecurityFilterChain");

    registerDeferred(bundleCtx, securityFilterChain);
  }

  /**
   * @param securityFilterChain
   */
  private void registerDeferred(final BundleContext bundleCtx, final Object securityFilterChain) {
    Thread t = new Thread(new Runnable() {
      public void run() {
//        try {
//          Thread.sleep(10000);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
        // Register the filter as an OSGi service
        Dictionary<String, String> props = new Hashtable<String, String>();
        // props.put("context", "weblounge");
        props.put("urlPatterns", "/*");
        props.put("pattern", ".*");
        props.put("service.ranking", "1");
        securityFilterRegistration = bundleCtx.registerService(Filter.class.getName(), securityFilterChain, props);
        logger.info("Spring security context registered");
      };
    });
    t.start();    
  }

  /**
   * Callback from OSGi environment on service inactivation.
   */
  void deactivate() {
    if (securityFilterRegistration != null) {
      securityFilterRegistration.unregister();
      securityFilterRegistration = null;
    }
    if (springContext != null && springContext.isRunning()) {
      springContext.close();
    }
  }

}
