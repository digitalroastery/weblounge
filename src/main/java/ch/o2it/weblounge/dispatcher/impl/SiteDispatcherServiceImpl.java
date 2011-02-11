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

import ch.o2it.weblounge.common.content.repository.ContentRepository;
import ch.o2it.weblounge.common.impl.url.PathUtils;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
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
import ch.o2it.weblounge.kernel.SiteManager;
import ch.o2it.weblounge.kernel.SiteServiceListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

/**
 * The site dispatcher watches sites coming and going and registers them with
 * the weblounge dispatcher.
 */
public class SiteDispatcherServiceImpl implements SiteDispatcherService, SiteListener, SiteServiceListener, ManagedService {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SiteDispatcherServiceImpl.class);

  /** Default value for the <code>WEBAPP_CONTEXT_ROOT</code> property */
  public static final String DEFAULT_WEBAPP_CONTEXT_ROOT = "/";

  /** Default value for the <code>BUNDLE_ROOT_URI</code> property */
  public static final String DEFAULT_BUNDLE_CONTEXT_ROOT_URI = "/weblounge-sites";

  /** Default value for the <code>BUNDLE_ENTRY</code> property */
  public static final String DEFAULT_BUNDLE_ENTRY = "/site";

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.o2it.weblounge.sitedispatcher";

  /** Configuration key prefix for jsp precompilation */
  public static final String OPT_PRECOMPILE = "precompile";

  /** Configuration key prefix for jsp precompilation logging */
  public static final String OPT_PRECOMPILE_LOGGING = "precompile.logerrors";

  /** Configuration key prefix for jasper configuration values */
  public static final String OPT_JASPER_PREFIX = "jasper.";

  /** Configuration option for jasper's scratch directory */
  public static final String OPT_JASPER_SCRATCHDIR = "scratchdir";

  /** Default value for jasper's <code>scratchDir</code> compiler context */
  public static final String DEFAULT_JASPER_SCRATCH_DIR = "jasper";

  /** The http service */
  private WebContainer paxHttpService = null;

  /** The action request handler */
  private ActionRequestHandler actionRequestHandler = null;

  /** The site manager */
  private SiteManager siteManager = null;

  /** Init parameters for jetty */
  private TreeMap<String, String> jasperConfig = new TreeMap<String, String>();

  /** Maps sites to site servlets */
  private Map<Site, SiteServlet> siteServlets = new HashMap<Site, SiteServlet>();

  /** The site registrations */
  private Map<Site, WebXml> httpRegistrations = null;

  private TreeMap<String, Properties> filterInitParamsMap = new TreeMap<String, Properties>();

  private TreeMap<String, Filter> filterNameInstances = new TreeMap<String, Filter>();

  private TreeMap<String, ArrayList<String>> filterNameMappings = new TreeMap<String, ArrayList<String>>();

  /** The OSGi component context */
  private ComponentContext componentContext = null;

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

    componentContext = context;
    BundleContext bundleContext = context.getBundleContext();
    logger.info("Starting site dispatcher");

    // Configure the default jasper work directory where compiled java classes
    // go
    String tmpDir = System.getProperty("java.io.tmpdir");
    String scratchDir = PathUtils.concat(tmpDir, DEFAULT_JASPER_SCRATCH_DIR);
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

    // Create the scratch directory if specified, otherwise jasper will complain
    scratchDir = jasperConfig.get(OPT_JASPER_SCRATCHDIR);
    if (scratchDir != null) {
      try {
        FileUtils.forceMkdir(new File(PathUtils.trim(scratchDir)));
        logger.debug("Temporary jsp source files and classes go to {}", scratchDir);
      } catch (IOException e) {
        throw new ConfigurationException(OPT_JASPER_SCRATCHDIR, "Unable to create jasper scratch directory at " + scratchDir + ": " + e.getMessage());
      }
    }

    httpRegistrations = new HashMap<Site, WebXml>();

    logger.debug("Site dispatcher activated");

    // Register for changing sites
    siteManager.addSiteListener(this);

    // Process sites that have already been registered
    for (Iterator<Site> si = siteManager.sites(); si.hasNext();) {
      addSite(si.next());
    }

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

    // Don't listen to new sites anymore
    siteManager.removeSiteListener(this);

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

    logger.debug("Configuring the site registration service");
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
        FileUtils.forceMkdir(new File(PathUtils.trim(scratchDir)));
        logger.debug("Temporary jsp source files and classes go to {}", scratchDir);
      } catch (IOException e) {
        throw new ConfigurationException(OPT_JASPER_SCRATCHDIR, "Unable to create jasper scratch directory at " + scratchDir + ": " + e.getMessage());
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
    if (siteManager == null)
      return;
    for (Iterator<Site> si = siteManager.sites(); si.hasNext();) {
      for (Module module : si.next().getModules()) {
        for (Action action : module.getActions()) {
          actionRequestHandler.register(action);
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
    if (siteManager == null)
      return;
    for (Iterator<Site> si = siteManager.sites(); si.hasNext();) {
      for (Module module : si.next().getModules()) {
        for (Action action : module.getActions()) {
          actionRequestHandler.unregister(action);
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
    return siteManager.findSiteByIdentifier(identifier);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#findSiteByURL(java.lang.String)
   */
  public Site findSiteByURL(URL siteURL) {
    return siteManager.findSiteByURL(siteURL);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.SiteDispatcherService#findSiteByRequest(javax.servlet.http.HttpServletRequest)
   */
  public Site findSiteByRequest(HttpServletRequest request) {
    if (request == null)
      throw new IllegalArgumentException("Request must not be null");
    return findSiteByURL(UrlUtils.toURL(request, false, false));
  }

  /**
   * Adds a new site.
   * 
   * @param site
   *          the site
   */
  private void addSite(Site site) {
    Bundle siteBundle = siteManager.getSiteBundle(site);
    WebXml webXml = createWebXml(site, siteBundle);
    Properties initParameters = new Properties();

    // Prepare the init parameters
    initParameters.putAll(webXml.getContextParams());
    initParameters.putAll(jasperConfig);

    // Create the site URI
    String contextRoot = webXml.getContextParam(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT, DEFAULT_WEBAPP_CONTEXT_ROOT);
    String bundleEntry = webXml.getContextParam(DispatcherConfiguration.BUNDLE_ENTRY, DEFAULT_BUNDLE_ENTRY);
    String bundleURI = webXml.getContextParam(DispatcherConfiguration.BUNDLE_URI, site.getIdentifier());
    String siteContextURI = webXml.getContextParam(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI, DEFAULT_BUNDLE_CONTEXT_ROOT_URI);
    String siteContextRoot = UrlUtils.concat(contextRoot, siteContextURI);
    String siteRoot = UrlUtils.concat(siteContextRoot, bundleURI);

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

      // Register the site servlet as a service
      Dictionary<String, String> registrationProperties = new Hashtable<String, String>();
      registrationProperties.put(Site.class.getName().toLowerCase(), site.getIdentifier());
      componentContext.getBundleContext().registerService(Servlet.class.getName(), siteServlet, registrationProperties);

      logger.info("Site '{}' registered under site://{}", site, siteRoot);

      // Did we already miss the "siteStarted()" event? If so, we trigger it
      // for ourselves, so the modules are being started.
      site.addSiteListener(this);
      if (site.isOnline()) {
        siteStarted(site);
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
   * Removes a site from the dispatcher.
   * 
   * @param site
   *          the site to remove
   */
  private void removeSite(Site site) {
    // Remove site dispatcher servlet
    WebXml webXml = httpRegistrations.remove(site);
    String siteRoot = webXml.getContextParam(DispatcherConfiguration.BUNDLE_ROOT);
    paxHttpService.unregister(siteRoot);
    Map<String, WebXmlServlet> webXmlServlets = webXml.getServlets();
    for (String name : webXmlServlets.keySet()) {
      for (String mapping : webXmlServlets.get(name).getServletMappings()) {
        paxHttpService.unregister(UrlUtils.concat(siteRoot, mapping));
      }
    }

    // We are no longer interested in site events
    site.removeSiteListener(this);

    siteServlets.remove(site);

    // Remote the site servlet from the OSGi registry
    String filterProperties = "(" + Servlet.class.getName().toLowerCase() + "=" + site.getIdentifier() + ")";
    try {
      ServiceReference[] refs = componentContext.getBundleContext().getServiceReferences(Servlet.class.getName(), filterProperties);
      // TODO: Unregister the servlet
    } catch (InvalidSyntaxException e) {
      logger.error("Error in site servlet lookup: {}", e.getMessage());
      // Should not happen, we created it ourselves
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
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.SiteListener#repositoryConnected(ch.o2it.weblounge.common.site.Site, ch.o2it.weblounge.common.content.repository.ContentRepository)
   */
  public void repositoryConnected(Site site, ContentRepository repository) { }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.SiteListener#repositoryDisconnected(ch.o2it.weblounge.common.site.Site, ch.o2it.weblounge.common.content.repository.ContentRepository)
   */
  public void repositoryDisconnected(Site site, ContentRepository repository) { }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.kernel.SiteServiceListener#siteAppeared(ch.o2it.weblounge.common.site.Site,
   *      org.osgi.framework.ServiceReference)
   */
  public void siteAppeared(Site site, ServiceReference reference) {
    addSite(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.kernel.SiteServiceListener#siteDisappeared(ch.o2it.weblounge.common.site.Site)
   */
  public void siteDisappeared(Site site) {
    removeSite(site);
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
  public WebXml createWebXml(Site site, Bundle siteBundle) {
    ServiceReference reference = null;
    ServiceReference[] references = siteBundle.getRegisteredServices();
    for (ServiceReference ref : references) {
      if (site.equals(siteBundle.getBundleContext().getService(ref))) {
        reference = ref;
        break;
      }
    }

    WebXml webXml = new WebXml();
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_NAME, siteBundle.getSymbolicName());
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_ENTRY, DEFAULT_BUNDLE_ENTRY);

    // Webapp context root
    String webappRoot = null;
    if (reference != null && reference.getProperty(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT) != null)
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
    if (reference != null && reference.getProperty(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI) != null)
      sitesRoot = (String) reference.getProperty(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI);
    else if (System.getProperty(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI) != null)
      sitesRoot = System.getProperty(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI);
    if (sitesRoot == null)
      sitesRoot = DEFAULT_BUNDLE_CONTEXT_ROOT_URI;
    if (!sitesRoot.startsWith("/"))
      sitesRoot = "/" + sitesRoot;
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI, sitesRoot);

    // Bundle context root
    sitesRoot = UrlUtils.concat(webappRoot, sitesRoot);
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT, sitesRoot);

    // Bundle uri
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_URI, site.getIdentifier());

    // Bundle root
    String bundleRoot = UrlUtils.concat(sitesRoot, site.getIdentifier());
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_ROOT, bundleRoot);

    // Bundle entry
    String bundleEntry = null;
    if (reference != null && reference.getProperty(DispatcherConfiguration.BUNDLE_ENTRY) != null)
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
   * OSGi callback that will set the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void setSiteManager(SiteManager siteManager) {
    this.siteManager = siteManager;
  }

  /**
   * OSGi callback that will unset the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void removeSiteManager(SiteManager siteManager) {
    this.siteManager = null;
  }

}