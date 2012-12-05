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

import ch.entwine.weblounge.common.site.Environment;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

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

  /** Maps server names to sites */
  private Map<String, Site> sitesByServerName = new HashMap<String, Site>();

  /** The site service tracker */
  private SiteTracker siteTracker = null;

  /** The environment service tracker */
  private EnvironmentTracker environmentTracker = null;

  /** The environment */
  private Environment environment = Environment.Production;

  /**
   * Creates a new weblounge health request filter.
   */
  public HealthCheckFilter() {

    Bundle bundle = FrameworkUtil.getBundle(getClass());
    BundleContext ctx = bundle.getBundleContext();

    // Start tracking the environment
    environmentTracker = new EnvironmentTracker(bundle.getBundleContext());
    environmentTracker.open();

    // Start tracking the sites
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

    // Extract site from the request
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      if (HEALTHCHECK_URI.equals(httpRequest.getRequestURI())) {
        for (Site site : sitesByServerName.values()) {
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
    sitesByServerName.clear();

    // Stop tracking sites
    if (siteTracker != null) {
      siteTracker.close();
      siteTracker = null;
    }

    // Stop tracking the environment
    if (environmentTracker != null) {
      environmentTracker.close();
      environmentTracker = null;
    }
  }

  /**
   * Callback from the OSGi environment when a new site has been added.
   * 
   * @param site
   *          the new site
   */
  void addSite(Site site) {
    registerSite(site, environment);
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
   * OSGi callback that passes in the environment.
   * 
   * @param environment
   *          the environment
   */
  void setEnvironment(Environment environment) {
    this.environment = environment;
    synchronized (sitesByServerName) {
      List<Site> sites = new ArrayList<Site>(sitesByServerName.values());
      sitesByServerName.clear();
      for (Site site : sites) {
        registerSite(site, environment);
      }
    }
  }

  /**
   * OSGi callback that removes the environment.
   * 
   * @param environment
   *          the environment
   */
  void removeEnvironment(Environment environment) {
    if (Environment.Production.equals(environment))
      return;

    // Re-register the site with the production environment
    this.environment = Environment.Production;
    logger.info("Changing site environments to {}", Environment.Production);
    synchronized (sitesByServerName) {
      List<Site> sites = new ArrayList<Site>(sitesByServerName.values());
      sitesByServerName.clear();
      for (Site site : sites) {
        registerSite(site, environment);
      }
    }
  }

  /**
   * Registers the site in the site registry.
   * 
   * @param site
   *          the site
   * @param environment
   *          the environment
   */
  private void registerSite(Site site, Environment environment) {

    // Register the url
    for (SiteURL url : site.getHostnames()) {
      if (!environment.equals(url.getEnvironment()))
        continue;
      synchronized (sitesByServerName) {
        String hostName = url.getURL().getHost();
        Site registeredFirst = sitesByServerName.get(hostName);
        if (registeredFirst != null && !site.equals(registeredFirst)) {
          logger.warn("Another site is already registered to " + url);
          continue;
        }
        sitesByServerName.put(hostName, site);
      }
    }

    // Initialize the site
    site.initialize(environment);
  }

  /**
   * This tracker is used to track <code>Site</code> services. Once a site is
   * detected, it registers that site with the <code>SiteContextFilter</code>.
   */
  private final class SiteTracker extends ServiceTracker {

    /**
     * Creates a new <code>SiteTracker</code>.
     * 
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
        try {
          super.removedService(reference, service);
        } catch (IllegalStateException e) {
          // The service has been removed, probably due to bundle shutdown
        } catch (Throwable t) {
          logger.warn("Error removing service: {}", t.getMessage());
        }
      }
    }

  }

  /**
   * This tracker is used to track <code>Environment</code> services. Once the
   * environment is detected, it is registered with the site context filter.
   */
  private final class EnvironmentTracker extends ServiceTracker {

    /**
     * Creates a new <code>EnvironmentTracker</code>.
     * 
     * @param context
     *          the site dispatcher's bundle context
     */
    public EnvironmentTracker(BundleContext context) {
      super(context, Environment.class.getName(), null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public Object addingService(ServiceReference reference) {
      Environment environment = (Environment) super.addingService(reference);
      setEnvironment(environment);
      return environment;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    public void removedService(ServiceReference reference, Object service) {
      Environment environment = (Environment) service;
      removeEnvironment(environment);
      if (reference.getBundle() != null) {
        try {
          super.removedService(reference, service);
        } catch (IllegalStateException e) {
          // The service has been removed already, probably due to bundle
          // shutdown
        } catch (Throwable t) {
          logger.warn("Error removing service: {}", t.getMessage());
        }
      }
    }

  }

}
