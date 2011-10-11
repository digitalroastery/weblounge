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

import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteURL;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter that for every request will set the Site on the security service.
 */
public class SiteContextFilter implements Filter {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(SiteContextFilter.class);

  /** The security service */
  protected SecurityService securityService = null;

  /** Maps server names to sites */
  private Map<String, Site> sitesByServerName = new HashMap<String, Site>();

  /** The site service tracker */
  private SiteTracker siteTracker = null;

  /**
   * Creates a new weblounge security filter, which is populating the required
   * fields for the current request in the security service.
   * 
   * @param securityService
   *          the security service
   */
  public SiteContextFilter(SecurityService securityService) {
    this.securityService = securityService;

    Bundle bundle = FrameworkUtil.getBundle(getClass());
    BundleContext ctx = bundle.getBundleContext();
    siteTracker = new SiteTracker(bundle.getBundleContext());
    siteTracker.open();

    // Look those sites up that are already registered
    try {
      ServiceReference[] references = ctx.getServiceReferences(Site.class.getName(), null);
      if (references != null) {
        for (ServiceReference ref : references) {
          Site site = (Site) ctx.getService(ref);
          addSite(site);
        }
      }
    } catch (InvalidSyntaxException e) {
      // Can't happen, as we are not providing a filter
    }

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

    Site site = null;

    // Extract site from the request
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      site = findSiteByRequest(httpRequest);
      if (site != null) {
        logger.debug("Mapped {} to site '{}'", httpRequest.getPathInfo(), site.getIdentifier());
      } else {
        logger.debug("No site available for request to {}", httpRequest.getPathInfo());
      }
    }

    // Set the site on the request
    securityService.setSite(site);

    chain.doFilter(request, response);

    // Make sure the thread is not associated with the site anymore
    securityService.setSite(null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    sitesByServerName.clear();
    if (siteTracker != null) {
      siteTracker.close();
      siteTracker = null;
    }
  }

  /**
   * Callback from the OSGi environment when a new site has been added.
   * 
   * @param site
   *          the new site
   */
  void addSite(Site site) {
    for (SiteURL connector : site.getConnectors()) {
      String hostName = connector.getURL().getHost();
      Site registeredFirst = sitesByServerName.get(hostName);
      if (registeredFirst != null && !site.equals(registeredFirst)) {
        logger.warn("Another site is already registered to " + connector);
        continue;
      }
      sitesByServerName.put(hostName, site);
    }
  }

  /**
   * Callback from the OSGi environment when a site went away.
   * 
   * @param site
   *          the site
   */
  void removeSite(Site site) {
    Iterator<Site> si = sitesByServerName.values().iterator();
    while (si.hasNext()) {
      Site s = si.next();
      if (site.equals(s)) {
        si.remove();
      }
    }
  }

  /**
   * Returns the site associated with the given server name.
   * <p>
   * Note that the server name is expected to not end with a trailing slash, so
   * please pass in <code>www.entwinemedia.com</code> instead of
   * <code>www.entwinemedia.com/</code>.
   * 
   * TODO: This is duplicate code that is used by the main dispatcher servlet as
   * well. Try to factor into a common utils method
   * 
   * @param request
   *          the request
   * @return the site
   */
  Site findSiteByRequest(HttpServletRequest request) {
    URL url = UrlUtils.toURL(request, false, false);

    String hostName = url.getHost();
    Site site = sitesByServerName.get(hostName);
    if (site != null)
      return site;

    // There is no direct match. Therefore, try to find a wildcard match
    for (Map.Entry<String, Site> e : sitesByServerName.entrySet()) {
      String siteUrl = e.getKey();

      try {
        // convert the host wildcard (ex. *.domain.tld) to a valid regex (ex.
        // .*\.domain\.tld)
        String alias = siteUrl.replace(".", "\\.");
        alias = alias.replace("*", ".*");
        if (hostName.matches(alias)) {
          site = e.getValue();
          logger.trace("Registering {} for site ", url, site);
          sitesByServerName.put(hostName, site);
          return site;
        }
      } catch (PatternSyntaxException ex) {
        logger.warn("Error while trying to find a host wildcard match: ".concat(ex.getMessage()));
      }
    }

    logger.debug("Lookup for {} did not match any site", url);
    return null;
  }

  /**
   * This tracker is used to track <code>Site</code> services. Once a site is
   * detected, it registers that site with the
   * <code>SiteDispatcherService</code>.
   */
  private final class SiteTracker extends ServiceTracker {

    /**
     * Creates a new <code>SiteTracker</code>.
     * 
     * @param siteManager
     *          the site dispatcher
     * @param context
     *          the site dispatcher's bundle context
     */
    public SiteTracker(BundleContext context) {
      super(context, Site.class.getName(), null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public Object addingService(ServiceReference reference) {
      Site site = (Site) super.addingService(reference);
      addSite(site);
      return site;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    public void removedService(ServiceReference reference, Object service) {
      Site site = (Site) service;
      removeSite(site);
      if (reference.getBundle() != null) {
        super.removedService(reference, service);
      }
    }

  }

}
