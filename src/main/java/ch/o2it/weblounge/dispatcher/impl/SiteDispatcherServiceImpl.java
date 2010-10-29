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

import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteListener;
import ch.o2it.weblounge.dispatcher.ActionRequestHandler;
import ch.o2it.weblounge.dispatcher.DispatcherConfiguration;
import ch.o2it.weblounge.dispatcher.SiteDispatcherService;
import ch.o2it.weblounge.dispatcher.impl.http.WebXml;
import ch.o2it.weblounge.dispatcher.impl.http.WebXmlFilter;
import ch.o2it.weblounge.dispatcher.impl.http.WebXmlServlet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

/**
 * The site dispatcher watches sites coming and going and registers them
 * with the weblounge dispatcher.
 */
public class SiteDispatcherServiceImpl implements SiteDispatcherService, SiteListener, ManagedService {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SiteDispatcherServiceImpl.class);

  /** Default value for the <code>WEBAPP_CONTEXT_ROOT</code> property */
  public static final String DEFAULT_WEBAPP_CONTEXT_ROOT = "/";

  /** Default value for the <code>BUNDLE_ROOT_URI</code> property */
  public static final String DEFAULT_BUNDLE_CONTEXT_ROOT_URI = "/weblounge-sites";

  /** Default value for the <code>BUNDLE_ENTRY</code> property */
  public static final String DEFAULT_BUNDLE_ENTRY = "/site";

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.o2it.weblounge.siteregistration";

  /** Configuration key prefix for jsp precompilation */
  public static final String OPT_PRECOMPILE = "precompile";

  /** Configuration key prefix for jsp precompilation logging */
  public static final String OPT_PRECOMPILE_LOGGING = "precompile.logerrors";

  /** Configuration key prefix for jasper configuration values */
  public static final String OPT_JASPER_PREFIX = "jasper.";

  /** Configuration option for jasper's scratch directory */
  public static final String OPT_JASPER_SCRATCHDIR = "scratchdir";

  /** Default value for jasper's <code>scratchDir</code> compiler context */
  public static final String DEFAULT_JASPER_WORK_DIR = "/weblounge/tmp/jasper";

  /** The http service */
  private WebContainer paxHttpService = null;

  /** The action request handler */
  private ActionRequestHandler actionRequestHandler = null;

  /** Init parameters for jetty */
  private TreeMap<String, String> jasperConfig = new TreeMap<String, String>();

  /** The site tracker */
  private SiteTracker siteTracker = null;

  /** The sites */
  private List<Site> sites = new ArrayList<Site>();

  /** Maps server names to sites */
  private Map<String, Site> sitesByServerName = new HashMap<String, Site>();

  /** Maps sites to site servlets */
  private Map<Site, SiteServlet> siteServlets = new HashMap<Site, SiteServlet>();

  /** The site registrations */
  private Map<Site, WebXml> httpRegistrations = null;

  private TreeMap<String, Properties> filterInitParamsMap = new TreeMap<String, Properties>();

  private TreeMap<String, Filter> filterNameInstances = new TreeMap<String, Filter>();

  private TreeMap<String, ArrayList<String>> filterNameMappings = new TreeMap<String, ArrayList<String>>();

  /** The precompiler for java server pages */
  private boolean precompile = true;

  /** List of precompilers */
  private WeakHashMap<Site, Precompiler> precompilers = new WeakHashMap<Site, Precompiler>();

  /** True to log errors that appear during precompilation */
  private boolean logCompileErrors = false;

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws IOException
   *           if reading from the configuration admin service fails
   * @throws ConfigurationException
   *           if service configuration fails
   */
  void activate(ComponentContext context) throws IOException,
      ConfigurationException {
    BundleContext bundleContext = context.getBundleContext();
    logger.info("Starting site dispatcher");

    // Configure the default jasper work directory where compiled java classes go
    String tmpDir = System.getProperty("java.io.tmpdir");
    String scratchDir = PathSupport.concat(tmpDir, DEFAULT_JASPER_WORK_DIR);
    jasperConfig.put(OPT_JASPER_SCRATCHDIR, scratchDir);

    // Try to get hold of the service configuration
    ServiceReference configAdminRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
    if (configAdminRef != null) {
      ConfigurationAdmin configAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminRef);
      Dictionary<?, ?> config = configAdmin.getConfiguration(SERVICE_PID).getProperties();
      if (config != null) {
        configure(config);
      } else {
        logger.debug("No customized configuration for site dispatcher found");
      }
    } else {
      logger.debug("No configuration admin service found while looking for site dispatcher configuration");
    }

    httpRegistrations = new HashMap<Site, WebXml>();
    siteTracker = new SiteTracker(this, bundleContext);
    siteTracker.open();

    logger.debug("Site dispatcher activated");
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) {
    logger.debug("Deactivating site dispatcher");

    siteTracker.close();
    siteTracker = null;

    // Stop precompilers
    for (Precompiler compiler : precompilers.values()) {
      if (compiler != null)
        compiler.stop();
    }

    logger.info("Site dispatcher stopped");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;
    configure(properties);
  }

  /**
   * Configures this service using the given configuration properties.
   * 
   * @param config
   *          the service configuration
   * @throws ConfigurationException
   *           if configuration fails
   */
  private synchronized boolean configure(Dictionary<?, ?> config)
      throws ConfigurationException {

    logger.info("Configuring the site registration service");
    boolean configurationChanged = true;

    // Activate precompilation?
    String precompileSetting = StringUtils.trimToNull((String) config.get(OPT_PRECOMPILE));
    precompile = precompileSetting == null || ConfigurationUtils.isTrue(precompileSetting);
    logger.debug("Jsp precompilation {}", precompile ? "activated" : "deactivated");

    // Log compilation errors?
    String logPrecompileErrors = StringUtils.trimToNull((String) config.get(OPT_PRECOMPILE_LOGGING));
    logCompileErrors = logPrecompileErrors != null && ConfigurationUtils.isTrue(logPrecompileErrors);
    logger.debug("Precompilation errors will {} logged", logCompileErrors ? "be" : "not be");

    // Store the jasper configuration keys
    Enumeration<?> keys = config.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      if (key.startsWith(OPT_JASPER_PREFIX) && key.length() > OPT_JASPER_PREFIX.length()) {
        String value = (String) config.get(key);
        if (StringUtils.trimToNull(value) == null)
          continue;
        value = ConfigurationUtils.processTemplate(value);
        key = key.substring(OPT_JASPER_PREFIX.length());

        boolean optionChanged = value.equalsIgnoreCase(jasperConfig.get(key));
        configurationChanged |= !optionChanged;
        if (optionChanged)
          logger.debug("Jetty jsp parameter '{}' configured to '{}'", key, value);

        jasperConfig.put(key, value);
        // This is a work around for jasper's horrible implementation of the
        // compiler context configuration. Some keys are camel case, others are
        // lower case.
        jasperConfig.put(key.toLowerCase(), value);
      }
    }

    // Create the scratch directory if specified, otherwise jasper will complain
    String scratchDir = jasperConfig.get(OPT_JASPER_SCRATCHDIR);
    if (scratchDir != null) {
      try {
        FileUtils.forceMkdir(new File(scratchDir));
        logger.debug("Temporary jsp source files and classes go to {}", scratchDir);
      } catch (IOException e) {
        throw new ConfigurationException(OPT_JASPER_SCRATCHDIR, "Unable to create jasper sratch directory at " + scratchDir + ": " + e.getMessage());
      }
    }

    return configurationChanged;
  }

  /**
   * Callback from the OSGi environment when the http service is activated.
   * 
   * @param paxHttpService
   *          the site locator
   */
  void setHttpService(WebContainer paxHttpService) {
    this.paxHttpService = paxHttpService;
  }

  /**
   * Callback from the OSGi environment when the http service is deactivated.
   * 
   * @param paxHttpService
   *          the http service
   */
  void removeHttpService(WebContainer paxHttpService) {
    this.paxHttpService = null;
  }

  /**
   * Callback from the OSGi environment which registers the request handler with
   * the site observer.
   * 
   * @param handler
   *          the action request handler
   */
  void setActionRequestHandler(ActionRequestHandler handler) {
    logger.debug("Registering {}", handler);
    this.actionRequestHandler = handler;
    synchronized (sites) {
      for (Site site : sites) {
        for (Module module : site.getModules()) {
          for (Action action : module.getActions()) {
            actionRequestHandler.register(action);
          }
        }
      }
    }
  }

  /**
   * Callback from the OSGi environment which removes the action request handler
   * from the site observer.
   * 
   * @param handler
   *          the action request handler
   */
  void removeActionRequestHandler(ActionRequestHandler handler) {
    logger.debug("Unregistering {}", handler);
    synchronized (sites) {
      for (Site site : sites) {
        for (Module module : site.getModules()) {
          for (Action action : module.getActions()) {
            actionRequestHandler.unregister(action);
          }
        }
      }
    }
    this.actionRequestHandler = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#getSiteServlet(ch.o2it.weblounge.common.site.Site)
   */
  public Servlet getSiteServlet(Site site) {
    return siteServlets.get(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#findSiteByIdentifier(java.lang.String)
   */
  public Site findSiteByIdentifier(String identifier) {
    for (Site site : sites) {
      if (site.getIdentifier().equals(identifier)) {
        return site;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#findSiteByName(java.lang.String)
   */
  public Site findSiteByName(String serverName) {
    Site site = sitesByServerName.get(serverName);
    if (site != null)
      return site;

    // There is obviously no direct match. Therefore, try to find a
    // wildcard match
    for (Map.Entry<String, Site> e : sitesByServerName.entrySet()) {
      String alias = e.getKey();

      try {
        // convert the host wildcard (ex. *.domain.tld) to a valid regex (ex.
        // .*\.domain\.tld)
        alias = alias.replace(".", "\\.");
        alias = alias.replace("*", ".*");
        if (serverName.matches(alias)) {
          site = e.getValue();
          logger.info("Registering {} for site {}", serverName, site);
          sitesByServerName.put(serverName, site);
          return site;
        }
      } catch (PatternSyntaxException ex) {
        logger.warn("Error while trying to find a host wildcard match: ".concat(ex.getMessage()));
      }
    }

    logger.debug("Lookup for {} did not match any site", serverName);
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#findSiteByRequest(javax.servlet.http.HttpServletRequest)
   */
  public Site findSiteByRequest(HttpServletRequest request) {
    if (request == null)
      throw new IllegalArgumentException("Request must not be null");
    return findSiteByName(request.getServerName());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#addSite(ch.o2it.weblounge.common.site.Site,
   *      org.osgi.framework.ServiceReference)
   */
  public void addSite(Site site, ServiceReference reference) {
    WebXml webXml = createWebXml(site, reference);
    Bundle siteBundle = reference.getBundle();
    Properties initParameters = new Properties();

    // Prepare the init parameters
    initParameters.putAll(webXml.getContextParams());
    initParameters.putAll(jasperConfig);

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
      BundleHttpContext bundleHttpContext = new BundleHttpContext(siteBundle, siteRoot, bundleEntry);

      // Setup the servlet filters
      buildFilters(webXml);

      // Register the site using jsp support (for tag libraries) and the site
      // servlet.
      SiteServlet siteServlet = new SiteServlet(site, bundleHttpContext);
      try {
        paxHttpService.registerServlet(siteRoot, siteServlet, initParameters, bundleHttpContext);
      } catch (NamespaceException e) {
        logger.error("The alias '{}' is already in use", siteRoot);
        return;
      } catch (Throwable t) {
        logger.error("Error registering resources for site '{}' at {}: {}", new Object[] {
            site,
            siteRoot,
            t.getMessage() });
        logger.error(t.getMessage(), t);
      }

      siteServlets.put(site, siteServlet);
      httpRegistrations.put(site, webXml);
      logger.info("Site '{}' registered under site://{}", site, siteRoot);

      // Register this site for the findByXYZ() methods
      synchronized (sites) {
        sites.add(site);
        site.addSiteListener(this);
        for (String name : site.getHostNames()) {
          if (site.equals(sitesByServerName.get(name))) {
            logger.error("Another site is already registered to " + name);
            continue;
          }
          sitesByServerName.put(name, site);
        }
      }

      // Start the precompiler if requested
      if (precompile) {
        Precompiler precompiler = new Precompiler(siteServlet, logCompileErrors);
        precompilers.put(site, precompiler);
        precompiler.precompile();
      }

      logger.debug("Site '{}' registered under site://{}", site, siteRoot);

    } catch (Throwable t) {
      logger.error("Error setting up site '{}' for http requests: {}", new Object[] {
          site,
          t.getMessage() });
      logger.error(t.getMessage(), t);
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#removeSite(ch.o2it.weblounge.common.site.Site)
   */
  public void removeSite(Site site) {
    // Remove site dispatcher servlet
    WebXml webXml = httpRegistrations.get(site);
    String siteRoot = webXml.getContextParam(DispatcherConfiguration.BUNDLE_ROOT);
    paxHttpService.unregister(siteRoot);
    Map<String, WebXmlServlet> webXmlServlets = webXml.getServlets();
    for (String name : webXmlServlets.keySet()) {
      for (String mapping : webXmlServlets.get(name).getServletMappings()) {
        paxHttpService.unregister(UrlSupport.concat(siteRoot, mapping));
      }
    }

    // Remove site registration
    synchronized (sites) {
      site.removeSiteListener(this);
      sites.remove(site);
      List<String> namesToRemove = new ArrayList<String>();
      for (Map.Entry<String, Site> entry : sitesByServerName.entrySet()) {
        if (site.equals(entry.getValue())) {
          namesToRemove.add(entry.getKey());
        }
      }
      for (String serverName : namesToRemove) {
        sitesByServerName.remove(serverName);
      }
    }

    // TODO: unregister site dispatcher

    logger.debug("Site {} unregistered", site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.SiteListener#siteStarted(ch.o2it.weblounge.common.site.Site)
   */
  public void siteStarted(Site site) {
    if (actionRequestHandler != null) {
      for (Module module : site.getModules()) {
        for (Action action : module.getActions()) {
          actionRequestHandler.register(action);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.SiteListener#siteStopped(ch.o2it.weblounge.common.site.Site)
   */
  public void siteStopped(Site site) {
    if (actionRequestHandler != null) {
      for (Module module : site.getModules()) {
        for (Action action : module.getActions()) {
          actionRequestHandler.unregister(action);
        }
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
   * This tracker is used to track <code>Site</code> services. Once a site is
   * detected, it registers that site with the
   * <code>SiteDispatcherService</code>.
   */
  private final class SiteTracker extends ServiceTracker {

    /** The site dispatcher */
    private SiteDispatcherServiceImpl dispatcher = null;

    /**
     * Creates a new <code>SiteTracker</code>.
     * 
     * @param dispatcher
     *          the site dispatcher
     * @param context
     *          the site dispatcher's bundle context
     */
    public SiteTracker(SiteDispatcherServiceImpl dispatcher,
        BundleContext context) {
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