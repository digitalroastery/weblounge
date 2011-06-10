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

package ch.entwine.weblounge.dispatcher.impl.http;

import ch.entwine.weblounge.dispatcher.DispatcherConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class WelcomeFileFilter implements javax.servlet.Filter {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WelcomeFileFilter.class);

  /** The application root path */
  private String appRoot = null;

  /** The bundle uri namespace */
  private String bundleUriNamespace = null;

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest req, ServletResponse res,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest hreq = (HttpServletRequest) req;
    String uri = hreq.getRequestURI();
    String contextPath = hreq.getContextPath();
    String servletPath = hreq.getServletPath();
    logger.debug("WelcomeFileFilter.URI=" + uri);
    logger.debug("WelcomeFileFilter.contextPath=" + contextPath);
    logger.debug("WelcomeFileFilter.servletPath=" + servletPath);
    if ((appRoot + bundleUriNamespace).equals(uri)) {
      logger.debug(uri + " is welcomeURL");
    }
    chain.doFilter(req, res);
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
    appRoot = config.getInitParameter(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT);
    if (appRoot == null || "/".equals(appRoot))
      appRoot = "";
    bundleUriNamespace = config.getInitParameter(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI);
    if (bundleUriNamespace == null || "/".equals(bundleUriNamespace))
      bundleUriNamespace = "";
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

}
