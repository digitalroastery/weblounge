/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
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

package ch.entwine.weblounge.kernel.site;

import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteListener;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.dispatcher.ActionRequestHandler;
import ch.entwine.weblounge.dispatcher.DispatcherConfiguration;
import ch.entwine.weblounge.dispatcher.SharedHttpContext;
import ch.entwine.weblounge.dispatcher.SiteDispatcherService;
import ch.entwine.weblounge.kernel.http.WebXml;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;

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

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.entwine.weblounge.sitedispatcher";

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

  /** Prefix for the precompilation date */
  public static final String X_COMPILE_PREFIX = "X-COMPILE-";

  /** Path to the scratch directory */
  private String jasperScratchDir = null;

  /** The action request handler */
  private ActionRequestHandler actionRequestHandler = null;

  /** The site manager */
  private SiteManager siteManager = null;

  /** The default environment */
  private Environment environment = Environment.Production;

  /** Init parameters for jetty */
  private final TreeMap<String, String> jasperConfig = new TreeMap<String, String>();

  /** Maps sites to site servlets */
  private final Map<Site, SiteServlet> siteServlets = new HashMap<Site, SiteServlet>();

  /** The site servlet registrations */
  private Map<Site, ServiceRegistration> servletRegistrations = null;

  /** The preferences service */
  private PreferencesService preferencesService = null;

  /** The precompiler for java server pages */
  private boolean precompile = true;

  /** Shutdown flag for running threads */
  private boolean shutdown = false;

  /** List of precompilers */
  private final WeakHashMap<Site, Precompiler> precompilers = new WeakHashMap<Site, Precompiler>();

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

    // Configure the jasper work directory where compiled java classes go
    String tmpDir = System.getProperty("java.io.tmpdir");
    jasperScratchDir = PathUtils.concat(tmpDir, DEFAULT_JASPER_SCRATCH_DIR);
    jasperConfig.put(OPT_JASPER_SCRATCHDIR, jasperScratchDir);

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

    servletRegistrations = new HashMap<Site, ServiceRegistration>();

    logger.debug("Site dispatcher activated");

    // Register for changing sites
    siteManager.addSiteListener(this);

    // Process sites that have already been registered
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        for (Iterator<Site> si = siteManager.sites(); si.hasNext();) {
          addSite(si.next());
        }
      }
    });
    t.setDaemon(true);
    t.start();
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

    // Tell everyone to finish their work
    shutdown = true;
    synchronized (servletRegistrations) {
      servletRegistrations.notifyAll();
    }

    // Stop precompilers
    for (Precompiler compiler : precompilers.values()) {
      if (compiler.isRunning()) {
        compiler.stop();
      } else if (preferencesService != null) {
        Preferences preferences = preferencesService.getSystemPreferences();
        String date = WebloungeDateFormat.formatStatic(new Date());
        preferences.put(compiler.getCompilerKey(), date);
        try {
          preferences.flush();
        } catch (BackingStoreException e) {
          logger.warn("Failed to store precompiler results: {}", e.getMessage());
        }
      } else if (preferencesService == null) {
        logger.warn("Unable to store precompiler results: preference service unavailable");
      }
    }

    logger.info("Site dispatcher stopped");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
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
  private boolean configure(Dictionary<?, ?> config)
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

    return configurationChanged;
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
   * @see ch.entwine.weblounge.dispatcher.SiteDispatcherService#getSiteServlet(ch.entwine.weblounge.common.site.Site)
   */
  public Servlet getSiteServlet(Site site) {
    return siteServlets.get(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.SiteDispatcherService#findSiteByIdentifier(java.lang.String)
   */
  public Site findSiteByIdentifier(String identifier) {
    return siteManager.findSiteByIdentifier(identifier);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.SiteDispatcherService#findSiteByURL(java.lang.String)
   */
  public Site findSiteByURL(URL siteURL) {
    return siteManager.findSiteByURL(siteURL);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.SiteDispatcherService#findSiteByRequest(javax.servlet.http.HttpServletRequest)
   */
  public Site findSiteByRequest(HttpServletRequest request) {
    if (request == null)
      throw new IllegalArgumentException("Request must not be null");
    return findSiteByURL(UrlUtils.toURL(request, false, false));
  }

  /**
   * Adds a new site.
   * 
   * This method may be long-running and therefore is executed in its own
   * thread.
   * 
   * @param site
   *          the site
   */
  private void addSite(final Site site) {
    Thread t = new Thread(new Runnable() {

      public void run() {

        Bundle siteBundle = siteManager.getSiteBundle(site);
        WebXml webXml = createWebXml(site, siteBundle);
        Properties initParameters = new Properties();

        // Prepare the init parameters
        // initParameters.put("load-on-startup", Integer.toString(1));
        initParameters.putAll(webXml.getContextParams());
        initParameters.putAll(jasperConfig);

        // Create the site URI
        String contextRoot = webXml.getContextParam(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT, DEFAULT_WEBAPP_CONTEXT_ROOT);
        String bundleURI = webXml.getContextParam(DispatcherConfiguration.BUNDLE_URI, site.getIdentifier());
        String siteContextURI = webXml.getContextParam(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI, DEFAULT_BUNDLE_CONTEXT_ROOT_URI);
        String siteRoot = UrlUtils.concat(contextRoot, siteContextURI, bundleURI);

        // Prepare the Jasper work directory
        String scratchDirPath = PathUtils.concat(jasperConfig.get(OPT_JASPER_SCRATCHDIR), site.getIdentifier());
        File scratchDir = new File(scratchDirPath);
        boolean jasperArtifactsExist = scratchDir.isDirectory() && scratchDir.list().length > 0;
        try {
          FileUtils.forceMkdir(scratchDir);
          logger.debug("Temporary jsp source files and classes go to {}", scratchDirPath);
        } catch (IOException e) {
          logger.warn("Unable to create jasper scratch directory at {}: {}", scratchDirPath, e.getMessage());
        }

        try {
          // Create and register the site servlet
          SiteServlet siteServlet = new SiteServlet(site, siteBundle, environment);
          Dictionary<String, String> servletRegistrationProperties = new Hashtable<String, String>();
          servletRegistrationProperties.put(Site.class.getName().toLowerCase(), site.getIdentifier());
          servletRegistrationProperties.put(SharedHttpContext.ALIAS, siteRoot);
          servletRegistrationProperties.put(SharedHttpContext.SERVLET_NAME, site.getIdentifier());
          servletRegistrationProperties.put(SharedHttpContext.CONTEXT_ID, SharedHttpContext.WEBLOUNGE_CONTEXT_ID);
          servletRegistrationProperties.put(SharedHttpContext.INIT_PREFIX + OPT_JASPER_SCRATCHDIR, scratchDirPath);
          ServiceRegistration servletRegistration = siteBundle.getBundleContext().registerService(Servlet.class.getName(), siteServlet, servletRegistrationProperties);
          servletRegistrations.put(site, servletRegistration);

          // We are using the Whiteboard pattern to register servlets. Wait for
          // the http service to pick up the servlet and initialize it
          synchronized (servletRegistrations) {
            boolean warnedOnce = false;
            while (!siteServlet.isInitialized()) {

              if (!warnedOnce) {
                logger.info("Waiting for site '{}' to be online", site.getIdentifier());
                warnedOnce = true;
              }

              logger.debug("Waiting for http service to pick up {}", siteServlet);
              servletRegistrations.wait(500);
              if (shutdown) {
                logger.info("Giving up waiting for registration of site '{}'");
                servletRegistrations.remove(site);
                return;
              }
            }
          }

          siteServlets.put(site, siteServlet);

          logger.info("Site '{}' is online and registered under site://{}", site, siteRoot);

          // Did we already miss the "siteStarted()" event? If so, we trigger it
          // for ourselves, so the modules are being started.
          site.addSiteListener(SiteDispatcherServiceImpl.this);
          if (site.isStarted()) {
            siteStarted(site);
          }

          // Start the precompiler if requested
          if (precompile) {
            String compilationKey = X_COMPILE_PREFIX + siteBundle.getBundleId();
            Date compileDate = null;

            boolean needsCompilation = true;

            // Check if this site has been precompiled already
            if (preferencesService != null) {
              Preferences preferences = preferencesService.getSystemPreferences();
              String compileDateString = preferences.get(compilationKey, null);
              if (compileDateString != null) {
                compileDate = WebloungeDateFormat.parseStatic(compileDateString);
                needsCompilation = false;
                logger.info("Site '{}' has already been precompiled on {}", site.getIdentifier(), compileDate);
              }
            } else {
              logger.info("Precompilation status cannot be determined, consider deploying a preferences service implementation");
            }

            // Does the scratch dir exist?
            if (!jasperArtifactsExist) {
              needsCompilation = true;
              logger.info("Precompiled artifacts for '{}' have been removed", site.getIdentifier());
            }

            // Let's do the work anyways
            if (needsCompilation) {
              Precompiler precompiler = new Precompiler(compilationKey, siteServlet, environment, logCompileErrors);
              precompilers.put(site, precompiler);
              precompiler.precompile();
            }
          }

          logger.debug("Site '{}' registered under site://{}", site, siteRoot);

        } catch (Throwable t) {
          logger.error("Error setting up site '{}' for http requests: {}", new Object[] {
              site,
              t.getMessage() });
          logger.error(t.getMessage(), t);
        }

      }

    });
    t.setDaemon(true);
    t.start();
  }

  /**
   * Removes a site from the dispatcher.
   * 
   * @param site
   *          the site to remove
   */
  private void removeSite(Site site) {
    // Remove site dispatcher servlet
    ServiceRegistration servletRegistration = servletRegistrations.remove(site);

    try {
      servletRegistration.unregister();
    } catch (IllegalStateException e) {
      // Never mind, the service has been unregistered already
    } catch (Throwable t) {
      logger.error("Unregistering site '{}' failed: {}", site.getIdentifier(), t);
    }

    // We are no longer interested in site events
    site.removeSiteListener(this);

    // Stop the site's precompiler
    Precompiler compiler = precompilers.get(site);
    if (compiler != null) {
      if (compiler.isRunning()) {
        compiler.stop();
      } else if (preferencesService != null) {
        Preferences preferences = preferencesService.getSystemPreferences();
        String date = WebloungeDateFormat.formatStatic(new Date());
        preferences.put(compiler.getCompilerKey(), date);
        try {
          preferences.flush();
        } catch (BackingStoreException e) {
          logger.warn("Failed to store precompiler results: {}", e.getMessage());
        }
      } else if (preferencesService == null) {
        logger.warn("Unable to store precompiler results: preference service unavailable");
      }
    }

    siteServlets.remove(site);

    // TODO: unregister site dispatcher

    logger.debug("Site {} unregistered", site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.SiteListener#siteStarted(ch.entwine.weblounge.common.site.Site)
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
   * @see ch.entwine.weblounge.common.site.SiteListener#siteStopped(ch.entwine.weblounge.common.site.Site)
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
   * @see ch.entwine.weblounge.common.site.SiteListener#repositoryConnected(ch.entwine.weblounge.common.site.Site,
   *      ch.entwine.weblounge.common.repository.ContentRepository)
   */
  public void repositoryConnected(Site site, ContentRepository repository) {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.SiteListener#repositoryDisconnected(ch.entwine.weblounge.common.site.Site,
   *      ch.entwine.weblounge.common.repository.ContentRepository)
   */
  public void repositoryDisconnected(Site site, ContentRepository repository) {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.site.SiteServiceListener#siteAppeared(ch.entwine.weblounge.common.site.Site,
   *      org.osgi.framework.ServiceReference)
   */
  public void siteAppeared(Site site, ServiceReference reference) {
    addSite(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.site.SiteServiceListener#siteDisappeared(ch.entwine.weblounge.common.site.Site)
   */
  public void siteDisappeared(Site site) {
    removeSite(site);
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
    webXml.addContextParam(DispatcherConfiguration.BUNDLE_ENTRY, Site.BUNDLE_PATH);

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
      bundleEntry = Site.BUNDLE_PATH;
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

  /**
   * Sets the default environment;
   * 
   * @param environment
   *          the environment
   */
  void setEnvironment(Environment environment) {
    this.environment = environment;
    for (SiteServlet servlet : siteServlets.values()) {
      servlet.setEnvironment(environment);
    }
  }

  /**
   * Sets the preferences service.
   * 
   * @param preferencesService
   *          the OSGi preferences service
   */
  void setPreferencesService(PreferencesService preferencesService) {
    this.preferencesService = preferencesService;
  }

}