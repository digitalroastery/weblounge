/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher.impl;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.dispatcher.DispatcherConfiguration;
import ch.o2it.weblounge.dispatcher.SiteDispatcherService;
import ch.o2it.weblounge.dispatcher.impl.http.WebInfFilterServlet;
import ch.o2it.weblounge.dispatcher.impl.http.WebXml;
import ch.o2it.weblounge.dispatcher.impl.http.WebXmlFilter;
import ch.o2it.weblounge.dispatcher.impl.http.WebXmlServlet;

import org.eclipse.equinox.http.helper.BundleEntryHttpContext;
import org.eclipse.equinox.http.helper.ContextInitParametersServletAdaptor;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.eclipse.equinox.http.helper.FilterServletAdaptor;
import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.Servlet;

/**
 * The site tracker watches site services coming and going and registers them
 * with the weblounge dispatcher.
 */
public class SiteDispatcherServiceImpl implements SiteDispatcherService {

  /** Logging facility */
  private static final Logger log_ = LoggerFactory.getLogger(SiteDispatcherServiceImpl.class);

  /** Default value for the <code>WEBAPP_CONTEXT_ROOT</code> property */
  public static final String DEFAULT_WEBAPP_CONTEXT_ROOT = "/";

  /** Default value for the <code>BUNDLE_ROOT_URI</code> property */
  public static final String DEFAULT_BUNDLE_CONTEXT_ROOT_URI = "/weblounge-sites";

  /** Default value for the <code>BUNDLE_ENTRY</code> property */
  public static final String DEFAULT_BUNDLE_ENTRY = "/site";

  /** The http service */
  private HttpService httpService = null;

  /** The site tracker */
  private SiteTracker siteTracker = null;

  /** The site registrations */
  private Map<Site, WebXml> httpRegistrations = null;

  private TreeMap<String, Properties> filterInitParamsMap = new TreeMap<String, Properties>();

  private TreeMap<String, Filter> filterNameInstances = new TreeMap<String, Filter>();

  private TreeMap<String, ArrayList<String>> filterNameMappings = new TreeMap<String, ArrayList<String>>();

  /**
   * Callback from the OSGi environment to activate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  public void activate(ComponentContext context) {
    BundleContext bundleContext = context.getBundleContext();
    log_.info("Starting site dispatcher");

    httpRegistrations = new HashMap<Site, WebXml>();
    siteTracker = new SiteTracker(this, bundleContext);
    siteTracker.open();

    log_.debug("Site dispatcher activated");
  }

  /**
   * Callback from the OSGi environment to deactivate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  public void deactivate(ComponentContext context) {
    log_.debug("Deactivating site dispatcher");

    siteTracker.close();
    siteTracker = null;

    log_.info("Site dispatcher stopped");
  }

  /**
   * Callback from the OSGi environment when the http service is activated.
   * 
   * @param httpService
   *          the site locator
   */
  public void setHttpService(HttpService httpService) {
    this.httpService = httpService;
  }

  /**
   * Callback from the OSGi environment when the http service is deactivated.
   * 
   * @param httpService
   *          the http service
   */
  public void removeHttpService(HttpService httpService) {
    this.httpService = null;
  }

  /**
   * Returns the <code>web.xml</code> representation that is used to register
   * the site dispatcher servlets with the <code>HttpService</code>.
   * <p>
   * The method registers the following init parameters in the
   * <code>web.xml</code> with appropriate default values:
   * <ul>
   * <li>weblounge.http.WEBAPP_CONTEXT_ROOT</li>
   * <li>weblounge.http.BUNDLE_CONTEXT_ROOT</li>
   * <li>weblounge.http.BUNDLE_CONTEXT_ROOT_URI</li>
   * <li>weblounge.http.BUNDLE_NAME</li>
   * <li>weblounge.http.BUNDLE_ROOT</li>
   * <li>weblounge.http.BUNDLE_URI</li>
   * <li>weblounge.http.BUNDLE_ENTRY</li>
   * </ul>
   * <p>
   * <b>Note:</b> almost all of these properties can be overwritten using either
   * the system properties or the service properties.
   */
  public WebXml createWebXml(Site site, ServiceReference reference) {
    Bundle siteBundle = reference.getBundle();
    WebXml webXml = new WebXml();
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_NAME, siteBundle.getSymbolicName());
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_ENTRY, DEFAULT_BUNDLE_ENTRY);

    // Webapp context root
    String webappRoot = null;
    if (reference.getProperty(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT) != null)
      webappRoot = (String) reference.getProperty(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT);
    else if (System.getProperty(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT) != null)
      webappRoot = System.getProperty(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT);
    if (webappRoot == null)
      webappRoot = DEFAULT_WEBAPP_CONTEXT_ROOT;
    if (!webappRoot.startsWith("/"))
      webappRoot = "/" + webappRoot;
    webXml.addContextParam(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT, webappRoot);

    // Bundle name
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_NAME, siteBundle.getSymbolicName().toLowerCase());

    // Bundle context root uri
    String sitesRoot = null;
    if (reference.getProperty(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI) != null)
      sitesRoot = (String) reference.getProperty(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI);
    else if (System.getProperty(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI) != null)
      sitesRoot = System.getProperty(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI);
    if (sitesRoot == null)
      sitesRoot = DEFAULT_BUNDLE_CONTEXT_ROOT_URI;
    if (!sitesRoot.startsWith("/"))
      sitesRoot = "/" + sitesRoot;
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI, sitesRoot);

    // Bundle context root
    sitesRoot = UrlSupport.concat(webappRoot, sitesRoot);
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT, sitesRoot);

    // Bundle uri
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_URI, site.getIdentifier());

    // Bundle root
    String bundleRoot = UrlSupport.concat(sitesRoot, site.getIdentifier());
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_ROOT, bundleRoot);

    // Bundle entry
    String bundleEntry = null;
    if (reference.getProperty(DispatcherConfiguration.BUNDLE_ENTRY) != null)
      bundleEntry = (String) reference.getProperty(DispatcherConfiguration.BUNDLE_ENTRY);
    else if (System.getProperty(DispatcherConfiguration.BUNDLE_ENTRY) != null)
      bundleEntry = System.getProperty(DispatcherConfiguration.BUNDLE_ENTRY);
    if (bundleEntry == null)
      bundleEntry = DEFAULT_BUNDLE_ENTRY;
    if (!bundleEntry.startsWith("/"))
      bundleEntry = "/" + bundleEntry;
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_ENTRY, bundleEntry);

    return webXml;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#addSite(ch.o2it.weblounge.common.site.Site, org.osgi.framework.ServiceReference)
   */
  public void addSite(Site site, ServiceReference reference) {
    WebXml webXml = createWebXml(site, reference);
    Bundle siteBundle = reference.getBundle();
    Properties initParameters = new Properties();

    // Prepare the init parameters
    initParameters.putAll(webXml.getContextParams());

    // Create the site URI
    String contextRoot = webXml.getContextParam(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT, DEFAULT_WEBAPP_CONTEXT_ROOT);
    String bundleEntry = webXml.getContextParam(DispatcherConfiguration.BUNDLE_ENTRY, DEFAULT_BUNDLE_ENTRY);
    String bundleURI = webXml.getContextParam(DispatcherConfiguration.BUNDLE_URI, site.getIdentifier());
    String siteContextURI = webXml.getContextParam(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI, DEFAULT_BUNDLE_CONTEXT_ROOT_URI);
    String siteContextRoot = UrlSupport.concat(contextRoot, siteContextURI);
    String siteRoot = UrlSupport.concat(new String[] {
        siteContextRoot,
        bundleURI });

    try {
      // Create the common http context
      HttpContext commonContext = new BundleEntryHttpContext(siteBundle, bundleEntry);

      // Setup the servlet filters
      buildFilters(webXml);

      log_.debug("Site '{}' registered under site://{}", site, siteRoot);

      // Register the site root
      try {
        httpService.registerResources(siteRoot, "/", commonContext);
      } catch (NamespaceException e) {
        log_.error("The alias '{}' is already in use", siteRoot);
      } catch (Exception e) {
        log_.error("Error registering resources for site '{}' at {}: {}", new Object[] { site, siteRoot, e.getMessage() });
        log_.error(e.getMessage(), e);
      }

      // Register servlets
      Map<String, WebXmlServlet> webXmlServlets = webXml.getServlets();
      for (String name : webXmlServlets.keySet()) {
        WebXmlServlet servlet = webXmlServlets.get(name);
        List<String> servletMappings = servlet.getServletMappings();
        // if there are no mappings, we still want to register it, use class
        // name
        if (servletMappings == null || servletMappings.isEmpty()) {
          servlet.addMapping(siteRoot + "/" + servlet.getServletClass().getName());
          servletMappings = servlet.getServletMappings();
        }

        for (String mapping : servletMappings) {
          Servlet servletInstance = (servlet.getServletClass()).newInstance();

          // apply namespace for bundle
          mapping = siteRoot + mapping;
          // new constructor
          // System.out.println("Applying contextPathServletAdaptor to "+servlet.getServletClass().getName());

          servletInstance = new ContextPathServletAdaptor(servletInstance, siteRoot);

          // apply application context init parameters
          // TODO: this overwrites context-param from outer web.xml
          if (initParameters != null && !initParameters.isEmpty()) {
            servletInstance = new ContextInitParametersServletAdaptor(servletInstance, initParameters);
          }

          // apply filters
          for (String filterName : servlet.getFilterNames()) {
            for (String filterNameMapping : filterNameMappings.get(filterName)) {
              filterNameMapping = "" + filterNameMapping;
              servletInstance = new FilterServletAdaptor(filterNameInstances.get(filterName), filterInitParamsMap.get(filterName), servletInstance);
            }
          }

          log_.debug("registered servlet mapping:" + mapping + " for bundle " + siteBundle.getSymbolicName());

          // register the servlet
          httpService.registerServlet(mapping, servletInstance, servlet.getInitParams(), commonContext);
        }
      }

      // Register JSPs
      Servlet jspServlet = new JspServlet(siteBundle, bundleEntry);
      jspServlet = new ContextPathServletAdaptor(jspServlet, siteRoot);
      jspServlet = new ContextInitParametersServletAdaptor(jspServlet, initParameters);

      // TODO: apply and test JSP filters
      /*
       * for (String filterName:filterNameInstances.keySet()) { for (String
       * filterNameMapping:filterNameMappings.get(filterName)) {
       * filterNameMapping = ""+filterNameMapping; jspServlet = new
       * FilterServletAdaptor(filterNameInstances.get(filterName),
       * filterInitParamsMap.get(filterName), jspServlet); } }
       */

      // Register the JspServlet
      httpService.registerServlet(siteRoot + "/*.jsp", jspServlet, null, commonContext);
      log_.debug("Registered jsp mapping {}/*.jsp for site '{}'", siteRoot, site);

      // block access to /WEB-INF resources and return a SC_FORBIDDEN
      httpService.registerServlet(siteRoot + "/WEB-INF", new WebInfFilterServlet(), null, commonContext);

      log_.debug("Site '{}' registered under site://{}", site, siteRoot);

    } catch (Exception e) {
      log_.error("Error setting up site '{}' for http requests: {}", new Object[] { site, e.getMessage() });
      log_.error(e.getMessage(), e);
    }
    
    httpRegistrations.put(site, webXml);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#removeSite(ch.o2it.weblounge.common.site.Site)
   */
  public void removeSite(Site site) {
    WebXml webXml = httpRegistrations.get(site);
    String siteRoot = webXml.getContextParam(DispatcherConfiguration.BUNDLE_ROOT);
    httpService.unregister(siteRoot);
    httpService.unregister(siteRoot + "/*.jsp");
    Map<String, WebXmlServlet> webXmlServlets = webXml.getServlets();
    for (String name : webXmlServlets.keySet()) {
      for (String mapping : webXmlServlets.get(name).getServletMappings()) {
        mapping = siteRoot + mapping;
        httpService.unregister(mapping);
      }
    }
  }

  /**
   * Creates a list of filters from the given web xml.
   * 
   * @param webXml
   *          the web xml
   * @throws IllegalAccessException
   *           if accessing the filter implementation fails
   * @throws InstantiationException
   *           if creating an instance of the filter implementation fails
   */
  public void buildFilters(WebXml webXml) throws IllegalAccessException,
      InstantiationException {
    for (WebXmlFilter filter : webXml.getFilters().values()) {
      Filter filterInstance = (Filter) (filter.getFilterClass()).newInstance();
      filterNameInstances.put(filter.getFilterName(), filterInstance);
      for (String mapping : filter.getFilterMappings()) {
        if (!filterNameMappings.containsKey(filter.getFilterName())) {
          filterNameMappings.put(filter.getFilterName(), new ArrayList<String>());
        }
        filterNameMappings.get(filter.getFilterName()).add(mapping);

        // build a list of filterInitParams
        Properties filterInitParamProperties = new Properties();
        filterInitParamProperties.putAll(filter.getInitParams());
        filterInitParamsMap.put(filterInstance.getClass().getName(), filterInitParamProperties);
      }
    }
  }

  /**
   * This tracker is used to track <code>Site</code> services. Once a site is
   * detected, it registers that site with the
   * <code>SiteDispatcherService</code>.
   */
  private final class SiteTracker extends ServiceTracker {

    /** The site dispatcher */
    private SiteDispatcherService dispatcher = null;

    /**
     * Creates a new <code>SiteTracker</code>.
     * 
     * @param dispatcher
     *          the site dispatcher
     * @param context
     *          the site dispatcher's bundle context
     */
    public SiteTracker(SiteDispatcherService dispatcher, BundleContext context) {
      super(context, Site.class.getName(), null);
      this.dispatcher = dispatcher;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public Object addingService(ServiceReference reference) {
      Site site = (Site) super.addingService(reference);
      dispatcher.addSite(site, reference);
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
      dispatcher.removeSite(site);
      super.removedService(reference, service);
    }

  }

}