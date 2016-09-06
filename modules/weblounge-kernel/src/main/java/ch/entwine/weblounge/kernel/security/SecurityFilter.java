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

import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.site.SiteManager;
import ch.entwine.weblounge.kernel.site.SiteServiceListener;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public final class SecurityFilter implements Filter, SiteServiceListener {

  /** The logging facility */
  public static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

  /** The default security configuration */
  private final Filter defaultSecurityFilter;

  /** Site specific security configurations */
  private final Map<Site, Filter> siteFilters = new ConcurrentHashMap<>();

  /** The sites that are online */
  private final SiteManager sites;

  /**
   * Creates a new security filter that will apply the default filter to those
   * sites that don't register a filter on their own.
   * 
   * @param sites
   *          the sites manager
   * @param filter
   *          the filter
   */
  public SecurityFilter(SiteManager sites, Filter filter) {
    this.sites = sites;
    this.defaultSecurityFilter = filter;
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    this.sites.addSiteListener(this);
    Iterator<Site> si = sites.sites();
    while (si.hasNext()) {
      Site site = si.next();
      Bundle siteBundle = sites.getSiteBundle(site);
      registerSecurity(site, siteBundle);
    }
  }

  @Override
  public void destroy() {
    siteFilters.clear();
  }

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
      SecurityUtils.setSite(site);

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
      SecurityUtils.setSite(null);
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

  @Override
  public void siteAppeared(Site site, ServiceReference reference) {
    registerSecurity(site, reference.getBundle());
  }

  @Override
  public void siteDisappeared(Site site) {
    siteFilters.remove(site);
  }

  /**
   * Registers a security filter for the given site.
   * 
   * @param site
   *          the site
   * @param bundle
   *          the site's bundle
   */
  private void registerSecurity(Site site, Bundle bundle) {
    URL securityConfiguration = site.getSecurity();
    if (securityConfiguration != null) {
      // Test availability of the security configuration

      String configPath = securityConfiguration.toExternalForm();
      if (configPath.startsWith("file://${bundle.root}")) {
        String bundlePath = configPath.substring(21);
        securityConfiguration = bundle.getResource(bundlePath);
      } else if (configPath.startsWith("file://${site.root}")) {
        String bundlePath = UrlUtils.concat("/site", configPath.substring(19));
        securityConfiguration = bundle.getResource(bundlePath);
      }

      // Is the configuration available?
      if (securityConfiguration == null) {
        throw new IllegalStateException("The security configuration of site '" + site.getIdentifier() + "' cannot be found at " + securityConfiguration);
      }

      try (InputStream is = securityConfiguration.openStream()) {
        // Turn the stream into a Spring Security filter chain
        ConfigurableOsgiBundleApplicationContext springContext = null;
        springContext = new OsgiBundleXmlApplicationContext(new String[] { securityConfiguration.toExternalForm() });
        springContext.setBundleContext(bundle.getBundleContext());
        springContext.refresh();

        // Register the security filter chain
        Filter siteSecurityFilter = (Filter) springContext.getBean("springSecurityFilterChain");
        logger.info("Registering custom security filter for site '{}'", site.getIdentifier());
        siteFilters.put(site, siteSecurityFilter);
      } catch (IOException e) {
        throw new IllegalStateException("Security configuration " + securityConfiguration + " of site '" + site.getIdentifier() + "' cannot be read: " + e.getMessage(), e);
      } catch (Exception e) {
        throw new IllegalStateException("Error registering security configuration " + securityConfiguration + " of site '" + site.getIdentifier() + "': " + e.getMessage(), e);
      }
    }
  }

  @Override
  public String toString() {
    return "Security filter";
  }

}
