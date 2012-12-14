/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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
package ch.entwine.weblounge.kernel.security;

import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.site.SiteManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter implementation injects Spring Security into the filter stack,
 * enforcing either the default security configuration or site specific security
 * rules.
 */
public final class SecurityFilter implements Filter {

  /** The logging facility */
  public static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

  /** The default security configuration */
  private Filter defaultSecurityFilter = null;

  /** Site specific security configurations */
  private Map<Site, Filter> siteFilters = null;

  /** The security service implementation */
  private SecurityService securityService = null;

  /** The sites that are online */
  protected SiteManager sites = null;

  /**
   * Creates a new security filter that will apply the default filter to those
   * sites that don't register a filter on their own.
   * 
   * @param securityService
   *          the security service
   * @param sites
   *          the sites manager
   * @param filter
   *          the filter
   */
  public SecurityFilter(SecurityService securityService, SiteManager sites,
      Filter filter) {
    this.securityService = securityService;
    this.sites = sites;
    this.defaultSecurityFilter = filter;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    siteFilters = new HashMap<Site, Filter>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    siteFilters.clear();
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    Site site = null;
    if (!(request instanceof HttpServletRequest)) {
      logger.warn("Received plain servlet request and don't know what to do with it");
      return;
    }

    // Try to map the request to a site
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    URL url = UrlUtils.toURL(httpRequest, false, false);
    site = sites.findSiteByURL(url);
    if (site == null) {
      logger.debug("Request for {} cannot be mapped to any site", httpRequest.getRequestURL());
      ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // Set the site in the security service
    try {
      logger.trace("Request to {} mapped to site '{}'", httpRequest.getRequestURL(), site.getIdentifier());
      securityService.setSite(site);

      // Select appropriate security filter and apply it
      Filter siteSecurityFilter = siteFilters.get(site);
      if (siteSecurityFilter != null) {
        logger.trace("Security for '{}' is handled by site specific security configuration");
        siteSecurityFilter.doFilter(request, response, chain);
      } else {
        logger.trace("Security for '{}' is handled by default security configuration");
        defaultSecurityFilter.doFilter(request, response, chain);
      }
    } finally {
      securityService.setSite(null);
    }

  }

  /**
   * Enforces a security configuration that is specific to this site.
   * 
   * @param site
   *          the site
   * @param filter
   *          the security filter
   */
  void addSecurityConfiguration(Site site, Filter filter) {
    siteFilters.put(site, filter);
  }

  /**
   * Removes the site specific security configuration. The site, should it still
   * be online, will now adhere to the default security settings.
   * 
   * @param site
   *          the site
   */
  void removeSecurityConfiguration(Site site) {
    siteFilters.remove(site);
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
