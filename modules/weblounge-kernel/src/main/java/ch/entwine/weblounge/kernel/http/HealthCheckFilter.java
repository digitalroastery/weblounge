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

import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.kernel.site.SiteManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter that will answer health checks typically sent by load balancers or
 * service monitors.
 */
public class HealthCheckFilter implements Filter {

  /** The logger */
  protected static final Logger logger = LoggerFactory.getLogger(HealthCheckFilter.class);

  /** URI for the health of this instance */
  private static final String HEALTHCHECK_URI = "/weblounge-health";

  /** The sites that are online */
  protected SiteManager sites = null;

  /**
   * Creates a new weblounge health request filter.
   */
  public HealthCheckFilter() {
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    // Is not being called
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    // Make sure the site manager has been set
    if (sites == null) {
      logger.trace("All sites are currently offline");
      ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_CONFLICT);
      return;
    }

    // Extract site from the request
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      if (HEALTHCHECK_URI.equals(httpRequest.getRequestURI())) {
        Iterator<Site> si = sites.sites();
        while (si.hasNext()) {
          Site site = si.next();
          if (!site.isOnline()) {
            logger.trace("Site {} is marked as offline", site.getIdentifier());
            logger.trace("Reporting bad health to {}", httpRequest.getRemoteHost());
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_CONFLICT);
            return;
          }
        }
        logger.trace("Reporting good health to {}", httpRequest.getRemoteHost());
        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
        return;
      }
    }

    // No health check, resume as usual
    chain.doFilter(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    // Nothing to do
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
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Healtch check filter";
  }

}
