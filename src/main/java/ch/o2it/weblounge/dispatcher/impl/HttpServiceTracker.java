/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.dispatcher.impl;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.dispatcher.impl.http.WebInfFilterServlet;
import ch.o2it.weblounge.dispatcher.impl.http.WebXml;
import ch.o2it.weblounge.dispatcher.impl.http.WebXmlContextParam;
import ch.o2it.weblounge.dispatcher.impl.http.WebXmlFilter;
import ch.o2it.weblounge.dispatcher.impl.http.WebXmlServlet;

import org.eclipse.equinox.http.helper.BundleEntryHttpContext;
import org.eclipse.equinox.http.helper.ContextInitParametersServletAdaptor;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.eclipse.equinox.http.helper.FilterServletAdaptor;
import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

/**
 * The <code>HttpServiceTracker</code> watches OSGi web service instances and
 * registers and unregisters the weblounge dispatcher with the first service
 * implementation to come.
 */
public class HttpServiceTracker extends ServiceTracker {

  /** Logger */
  private static final Logger log_ = LoggerFactory.getLogger(HttpServiceTracker.class);

  /** Main dispatcher */
  private WebloungeDispatcherServlet dispatcher = null;

  /** The site tracker */
  private SiteTracker siteTracker = null;

  /**
   * Creates a new HTTP service tracker that will, upon an appearing http
   * service, register the dispatcher servlet.
   * 
   * @param context
   *          the bundle context
   * @param dispatcher
   *          the dispatcher
   */
  HttpServiceTracker(BundleContext context,
      WebloungeDispatcherServlet dispatcher) {
    super(context, HttpService.class.getName(), null);
    this.dispatcher = dispatcher;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
   */
  @Override
  public Object addingService(ServiceReference reference) {
    HttpService httpService = null;

    // Register the weblounge dispatcher
    try {
      log_.debug("Registering weblounge dispatcher with http service {}", reference.getBundle().getSymbolicName());
      httpService = (HttpService) context.getService(reference);

      HttpContext httpContext = httpService.createDefaultHttpContext();
      Dictionary<?, ?> initParams = new Properties();
      httpService.registerServlet("/weblounge", dispatcher, initParams, httpContext);
      log_.info("Weblounge dispatcher hooked up with {}", httpService.getClass().getName());

      // Register for jsp support
      // httpService.registerJsps(new String[] { "*.jsp" }, httpContext);
      // log_.info("Weblounge dispatcher successfully set up for java server pages (jsp)");

      // Start the site tracker
      siteTracker = new SiteTracker(context, httpService);
      siteTracker.open();

    } catch (ServletException e) {
      log_.error("Error registering weblounge dispatcher with {}: {}", httpService, e.getMessage());
      httpService = null;
    } catch (NamespaceException e) {
      log_.error("Namespace error registering weblounge dispatcher with {}: {}", httpService, e.getMessage());
      httpService = null;
    }

    return httpService;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#modifiedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void modifiedService(ServiceReference reference, Object service) {
    log_.info("Http service was modified");
    super.modifiedService(reference, service);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void removedService(ServiceReference reference, Object service) {
    log_.info("Weblounge dispatcher disconnected from {}", service.getClass().getName());
    ((HttpService) service).unregister("/");
    // if (service instanceof WebContainer) {
    // ((WebContainer) service).unregisterJsps(httpContext);
    // }
    super.removedService(reference, service);
  }

  /**
   * The site tracker watches site services coming and going and registers them
   * with the weblounge dispatcher.
   */
  public class SiteTracker extends ServiceTracker {

    public static final String WEBAPP_CONTEXTROOT = "weblounge.http.CONTEXT_ROOT";
    public static final String BUNDLE_URI_NAMESPACE = "weblounge.http.BUNDLE_URI_NAMESPACE";
    public static final String BUNDLE_ENTRY = "weblounge.http.BUNDLE_ENTRY";
    public static final String BUNDLE_NAME = "weblounge.http.BUNDLE_NAME";
    public static final String SITES_URI = "/weblounge-sites";
    public static final String BUNDLE_ENTRY_DEFAULT_PATH = "/site";

    protected String webappContextRoot = null;
    
    protected String siteRoot = null;

    protected WebXml webXml = new WebXml();

    boolean debug = true;

    private HttpService httpService = null;

    private TreeMap<String, Properties> filterInitParamsMap = new TreeMap<String, Properties>();
    
    private TreeMap<String, Filter> filterNameInstances = new TreeMap<String, Filter>();

    private TreeMap<String, ArrayList<String>> filterNameMappings = new TreeMap<String, ArrayList<String>>();

    /**
     * Creates a site tracker.
     */
    SiteTracker(BundleContext context, HttpService httpService) {
      super(context, Site.class.getName(), null);
      this.httpService = httpService;
      webXml = new WebXml();
      webXml.addContextParam(BUNDLE_NAME, context.getBundle().getSymbolicName());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public Object addingService(ServiceReference reference) {
      Site site = (Site) super.addingService(reference);

      try {
        Properties initParameters = buildApplicationContextInitParams(webXml);

        // Create the site URI
        String contextRoot = createContextRootURI(initParameters);
        String bundleURI = site.getIdentifier();
        String bundleEntry = BUNDLE_ENTRY_DEFAULT_PATH;
        String siteRoot = UrlSupport.concat(new String[] { contextRoot, bundleURI });

        // Create the common http context
        HttpContext commonContext = new BundleEntryHttpContext(reference.getBundle(), bundleEntry);

        // Setup the servlet filters
        buildFilters(webXml);

        // Register the site root
        try {
          log_.debug("Registering resources for site '{}' under {}", site.getIdentifier(), siteRoot);
          httpService.registerResources(siteRoot, "/", commonContext);
        } catch (Exception e) {
          log_.error("The alias '{}' is already in use", siteRoot);
        }

        // Register servlets
        TreeMap<String, WebXmlServlet> webXmlServlets = webXml.getWebXmlServlets();
        for (String name : webXmlServlets.keySet()) {
          WebXmlServlet servlet = webXmlServlets.get(name);
          ArrayList<String> servletMappings = servlet.getServletMappings();
          // if there are no mappings, we still want to register it, use class
          // name
          if (servletMappings == null || servletMappings.isEmpty()) {
            servlet.addMapping(siteRoot + "/" + servlet.getServletClass().getName());
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

            if (debug)
              System.out.println("registered servlet mapping:" + mapping + " for bundle " + reference.getBundle().getSymbolicName());

            // register the servlet
            httpService.registerServlet(mapping, servletInstance, servlet.getInitParams(), commonContext);
          }
        }

        // Register JSPs
        Servlet jspServlet = new JspServlet(reference.getBundle(), bundleEntry);
        jspServlet = new ContextPathServletAdaptor(jspServlet, siteRoot);
        
        // Apply init parameters to JSP's
        if (initParameters != null && !initParameters.isEmpty())
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

        log_.info("Site '{}' registered under site://{}", site, siteRoot);

      } catch (Exception e) {
        log_.error("Error setting up site '{}' for http requests: {}", site, e.getMessage());
      }

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
      String bundleURI = site.getIdentifier();
      String siteRoot = UrlSupport.concat(new String[] { webappContextRoot, bundleURI });
      httpService.unregister(siteRoot);
      httpService.unregister(siteRoot + "/*.jsp");
      TreeMap<String, WebXmlServlet> webXmlServlets = webXml.getWebXmlServlets();
      for (String name : webXmlServlets.keySet()) {
        for (String mapping : webXmlServlets.get(name).getServletMappings()) {
          mapping = siteRoot + mapping;
          httpService.unregister(mapping);
        }
      }
      super.removedService(reference, service);
    }

    protected Properties buildApplicationContextInitParams(WebXml webXml) {
      // build a list of application context initparams
      Properties applicationContextInitParams = null;
      ArrayList<WebXmlContextParam> contextParams = webXml.getContextParams();
      if (contextParams != null && !contextParams.isEmpty()) {
        applicationContextInitParams = new Properties();
        for (WebXmlContextParam contextParam : contextParams) {
          applicationContextInitParams.put(contextParam.getParamName(), contextParam.getParamValue());
        }
      }
      return applicationContextInitParams;
    }

    /**
     * Returns the system's context root path. By default, this will be
     * <code>/</code>. However, it might be overwritten by setting the
     * <code>weblounge.http.CONTEXT_ROOT</code> in the
     * <code>contextParams</code>.
     * 
     * @param contextParams
     *          the context configuration
     * @return the context root uri
     */
    protected String createContextRootURI(Properties contextParams) {
      if (webappContextRoot != null)
        return webappContextRoot;
      
      String contextRoot = null;
      if (contextParams != null) {
        contextRoot = System.getProperty(WEBAPP_CONTEXTROOT);
        if (contextRoot != null) {
          if (!contextRoot.startsWith("/"))
            contextRoot = "/" + contextRoot;
        }
      }
      if (contextRoot == null) {
        contextRoot = "/";
      }
      webappContextRoot = UrlSupport.concat(contextRoot, SITES_URI);
      return webappContextRoot;
    }

    public void buildFilters(WebXml webXml) throws IllegalAccessException,
        InstantiationException {
      // build a list of filters
      TreeMap<String, WebXmlFilter> webXmlFilters = webXml.getWebXmlFilters();
      // TreeMap<String,Filter> filterInstances = new TreeMap<String, Filter>();

      for (String name : webXmlFilters.keySet()) {
        WebXmlFilter filter = webXmlFilters.get(name);
        Filter filterInstance = (Filter) (filter.getFilterClass()).newInstance();

        filterNameInstances.put(filter.getFilterName(), filterInstance);

        ArrayList<String> filterMappings = filter.getFilterMappings();
        if (filterMappings != null && !filterMappings.isEmpty()) {
          for (String mapping : filterMappings) {
            // filterInstances.put(mapping, filterInstance);

            if (!filterNameMappings.containsKey(filter.getFilterName())) {
              filterNameMappings.put(filter.getFilterName(), new ArrayList<String>());
            }
            filterNameMappings.get(filter.getFilterName()).add(mapping);

            // build a list of filterInitParams
            Dictionary<String, String> filterInitParams = filter.getInitParams();
            Properties filterInitParamProperties = null;

            if (filterInitParams != null && !filterInitParams.isEmpty()) {
              filterInitParamProperties = new Properties();
              Enumeration<String> en = filterInitParams.keys();
              while (en.hasMoreElements()) {
                String key = en.nextElement();
                filterInitParamProperties.put(key, filterInitParams.get(key));

              }
              filterInitParamsMap.put(filterInstance.getClass().getName(), filterInitParamProperties);
              // System.out.println(filterInitParamsMap);
            }
          }
        }
      }
    }

  }

}