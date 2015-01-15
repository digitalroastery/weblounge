/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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
package ch.entwine.weblounge.kernel.http;

import ch.entwine.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.entwine.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.site.SiteManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This abstract servlet filter wraps the given {@link ServletRequest} /
 * {@link ServletResponse} in appropriate {@link WebloungeRequest} and
 * {@link ch.entwine.weblounge.common.request.WebloungeResponse} objects and passes them to the
 * {@link #doFilter(WebloungeRequestImpl, WebloungeResponseImpl, FilterChain)} method.
 * <p>
 * Filters, which need to have access to Weblounge request/response objects
 * should inherit from this abstract base class.
 */
public abstract class AbstractWebloungeFilter implements Filter {
  
  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(AbstractWebloungeFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Nothing to do here
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res,
      FilterChain chain) throws IOException, ServletException {

    // Wrap request & response
    WebloungeRequestImpl request = new WebloungeRequestImpl((HttpServletRequest) req, getEnvironment());
    WebloungeResponseImpl response = new WebloungeResponseImpl((HttpServletResponse) res);

    // Initialize request with site
    Site site = getSiteByRequest(request);
    if (site != null) {
      request.init(site);
      logger.trace("Weblounge request found, pass on to doFilter()");
      doFilter(request, response, chain);
    } else {
      logger.trace("No Weblounge request found, pass on to the filter chain");
      chain.doFilter(req, res);
    }
  }

  @Override
  public void destroy() {
    // Nothing to do here
  }

  /**
   * Tries to find a site by the given request.
   * 
   * @param request
   *          the request
   * @return the site
   */
  private Site getSiteByRequest(WebloungeRequest request) {
    return getSiteManager().findSiteByURL(UrlUtils.toURL(request, false, false));
  }

  /**
   * The <code>doFilter</code> method is called every time the
   * {@link ServletRequest} & {@link ServletResponse} objects could be wrapped
   * in {@link WebloungeRequest} & {@link ch.entwine.weblounge.common.request.WebloungeResponse} objects.
   * 
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   * 
   * @param request
   *          the Weblounge request
   * @param response
   *          the Weblounge response
   * @param chain
   *          the servlet filter chain
   * @throws IOException
   * @throws ServletException
   */
  public abstract void doFilter(WebloungeRequestImpl request,
      WebloungeResponseImpl response, FilterChain chain) throws IOException,
      ServletException;

  /**
   * Get the environment in which this Weblounge instance is currently running
   * in.
   * 
   * @return the environment
   */
  protected abstract Environment getEnvironment();

  /**
   * Get the site manager used to have access to all registered sites of this
   * Weblounge instance.
   * 
   * @return the site manager
   */
  protected abstract SiteManager getSiteManager();

}
