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

package ch.entwine.weblounge.kernel.http;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.FilterChainProxy;

import java.io.IOException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class registers a shared servlet context in the service registry which
 * can be found using the <code>pcontextId</code> property, looking for the
 * value <code>weblounge</code>.
 */
public class SharedHttpContextImpl implements HttpContext {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SharedHttpContextImpl.class);

  /** The bundle context */
  protected BundleContext bundleContext = null;

  /** The security filter */
  protected Filter securityFilter = null;

  /**
   * Callback from OSGi on component activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    bundleContext = ctx.getBundleContext();
    logger.info("Publishing shared http context");
  }

  /**
   * Callback from OSGi on component activation.
   * 
   * @param ctx
   *          the component context
   */
  void deactivate(ComponentContext ctx) {
    logger.info("Retracting shared http context");
  }

  /**
   * {@inheritDoc}
   * 
   * This implementation makes sure the security filter is up and running.
   * 
   * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public boolean handleSecurity(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    return securityFilter != null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.http.HttpContext#getResource(java.lang.String)
   */
  public URL getResource(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
   */
  public String getMimeType(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Callback from OSGi to set the security filter once it has been published.
   * 
   * @param filter
   *          the spring security filter
   */
  void addSecurityFilter(Filter filter) {
    if (filter instanceof FilterChainProxy) {
      securityFilter = filter;
      logger.info("Enabling requests to protected resources");
    }
  }

  /**
   * Callback from OSGi to set the security filter once it has been published.
   * 
   * @param filter
   *          the spring security filter
   */
  void removeSecurityFilter(Filter filter) {
    if (filter == securityFilter) {
      securityFilter = null;
      logger.info("Disabling requests to protected resources");
    }
  }

}
