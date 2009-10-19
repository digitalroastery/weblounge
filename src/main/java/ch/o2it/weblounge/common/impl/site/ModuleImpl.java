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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.PathSupport;
import ch.o2it.weblounge.common.impl.util.ServletConfiguration;
import ch.o2it.weblounge.common.impl.util.ServletMapping;
import ch.o2it.weblounge.common.impl.util.cron.Daemon;
import ch.o2it.weblounge.common.impl.util.cron.JobRegistry;
import ch.o2it.weblounge.common.impl.util.registry.Registry;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Layout;
import ch.o2it.weblounge.common.page.PageListener;
import ch.o2it.weblounge.common.page.SearchResult;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.repository.Collection;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.service.ModuleService;
import ch.o2it.weblounge.common.service.Service;
import ch.o2it.weblounge.common.service.ServiceConfiguration;
import ch.o2it.weblounge.common.service.ServiceDependencyException;
import ch.o2it.weblounge.common.service.ServiceException;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.ImageStyleRegistry;
import ch.o2it.weblounge.common.site.Job;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.ModuleConfiguration;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;

/**
 * This is the default implementation for a module, which is instantiated for a
 * module descriptor if no custom module class is specified in the
 * <code>&lt;class&gt;</code> tag.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class ModuleImpl implements Module, PageListener, UserListener {

  /** The module configuration */
  protected ModuleConfiguration config;

  /** The renderer registry */
  protected RendererRegistry renderers;

  /** The action registry */
  protected ActionRegistry actions;

  /** The job registry */
  protected JobRegistry jobs;

  /** The image style registry */
  protected ImageStyleRegistry imagestyles;

  /** The module listeners */
  protected List<ModuleListener> listeners;

  /** The class loader */
  protected ClassLoader classLoader;

  /** The associated site */
  protected Site site;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = ModuleImpl.class.getName();

  /** Logging facility */
  final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Constructor for class ModuleImpl. Constructor has package access because
   * module objects should only be created via the <code>ModuleManager</code>.
   * To obtain a reference to a specific module object, use the
   * <code>ModuleRegistry</code>.
   */
  protected ModuleImpl() {
    classLoader = Thread.currentThread().getContextClassLoader();
    listeners = new ArrayList<ModuleListener>();
    renderers = new RendererRegistry();
    actions = new ActionRegistry();
    wizards = new WizardRegistry();
    services = new ServiceRegistry();
    jobs = new JobRegistry();
    controlpanels = new ControlPanelRegistry();
    imagestyles = new ImageStyleRegistryImpl();
  }

  /**
   * Creates a new module which is extending <code>base</code>.
   * 
   * @param base
   *          the base module
   */
  ModuleImpl(Module base, ClassLoader loader) {
    this();
    baseModule = base;
    if (loader != null)
      classLoader = loader;
  }

  /**
   * Returns the module name, which is equal to the identifier configured in the
   * module descriptor.
   * 
   * @return the module identifier
   */
  public String getIdentifier() {
    return (baseModule != null) ? baseModule.getIdentifier() : config.getIdentifier();
  }

  /**
   * Returns the module's document base.
   * 
   * @return the module's document base
   */
  public String getPath() {
    return config.getPath();
  }

  /**
   * Returns the associated site.
   * 
   * @return the associated site
   */
  public Site getSite() {
    return site;
  }

  /**
   * Returns <code>true</code> if <code>obj</code> is a module featuring the
   * same module identifier than this one.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Module) {
      Module m = (Module) obj;
      return m.getSite().equals(getSite()) && m.getIdentifier().equals(getIdentifier());
    }
    return false;
  }

  /**
   * Returns the module identifier's hash code.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return getIdentifier().hashCode();
  }

  /**
   * Adds <code>listener</code> to the list of module listeners.
   * 
   * @param listener
   *          the module listener
   */
  public void addModuleListener(ModuleListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes <code>listener</code> from the list of module listeners.
   * 
   * @param listener
   *          the module listener
   */
  public void removeModuleListener(ModuleListener listener) {
    listeners.remove(listener);
  }

  /**
   * Returns <code>true</code> if this module and any possible base module is
   * enabled.
   * 
   * @return <code>true</code> if the module is enabled
   */
  public boolean isEnabled() {
    return config.isEnabled();
  }

  /**
   * Returns the module's load factor, which is <code>1</code> for normal
   * modules This factor can be configured in the
   * <code>&lt;performance&gt;</code> section of the module configuration.
   * 
   * @return the module's load factor
   */
  public int getLoadFactor() {
    if (config.getLoadFactor() > 0)
      return config.getLoadFactor();
    return DEFAULT_LOAD_FACTOR;
  }

  /**
   * Returns the real path on the server for a given virtual path relative to
   * the module's root directory.
   * 
   * @param path
   *          the virtual (module-relative) path
   * @return the real (physical) path on the server
   */
  public String getPhysicalPath(String path) {
    return PathSupport.concat(config.getPath(), path);
  }

  /**
   * Returns the virtual path on the server relative to the webapplication.
   * Using this path e. g. for a renderer <code>jsp/myjsp.jsp</code> will
   * produce <code>/sites/mysite/modules/mymodule/jsp/myjsp.jsp</code>.
   * 
   * @param path
   *          the virtual path relative to the site
   * @param webapp
   *          <code>true</code> to preprend the webapp url
   * @return the virtual work path relative to the webapp
   */
  public String getVirtualPath(String path, boolean webapp) {
    return getSite().getVirtualPath("/" + ModuleManager.MODULE_DIR + "/" + this + "/" + path, webapp);
  }

  /**
   * Returns the database collection for <code>path</code>, which is interpreted
   * relative to the module's root collection.
   * <p>
   * For example, a module implementing a guestbook would request the collection
   * path <code>guestbook</code> which returns the database collection
   * <code>weblounge/sites/mysite/modules/guestbook</code>.
   * <p>
   * The method returns the collection either if it exists or can be created (
   * <code>
	 * create</code> is <code>true</code>.
   * 
   * @param path
   *          the module relative path
   * @param create
   *          <code>true</code> to create the collection
   * @return the collection or <code>null</code> if the collection does not
   *         exist
   */
  public Collection getCollection(String path, boolean create) {
    String collectionPath = "/modules/" + this + "/";
    collectionPath = UrlSupport.concat(collectionPath, path);
    return getSite().getCollection(collectionPath, create);
  }

  /**
   * Returns the database collection to the database.
   * 
   * @param c
   *          the collection to be returned
   */
  public void returnCollection(Collection c) {
    DBXMLDatabase db = (DBXMLDatabase) ServiceManager.getEnabledSystemService(DBXMLDatabase.ID);
    db.returnCollection(c);
  }

  /**
   * Returns <code>true</code>, since the default module will be searchable.
   * 
   * @return <code>true</code> if the module is searchable
   */
  public boolean isSearchable() {
    return true;
  }

  /**
   * Returns <code>true</code>, since by default, modules are searchable.
   * 
   * @return <code>true</code> if the module is searchable
   */
  public boolean searchByDefault() {
    return true;
  }

  /**
   * Returns the result of the search query.
   * 
   * @param query
   *          the search query
   * @return the result set
   */
  public SearchResult[] search(String query) {
    List result = new ArrayList();
    return (SearchResult[]) result.toArray(new SearchResult[result.size()]);
  }

  /**
   * Returns an iteration of all available option names.
   * 
   * @return the available option names
   * @see #hasOption(java.lang.String)
   * @see #getOption(java.lang.String)
   * @see #getOption(java.lang.String, java.lang.String)
   */
  public Iterator options() {
    return config.options();
  }

  /**
   * Returns <code>true</code> if the the option with name <code>name</code> has
   * been configured.
   * 
   * @param name
   *          the option name
   * @return <code>true</code> if an option with that name exists
   * @see #options()
   * @see #getOption(java.lang.String)
   * @see #getOption(java.lang.String, java.lang.String)
   */
  public boolean hasOption(String name) {
    return config.hasOption(name);
  }

  /**
   * Returns the option value for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * <p>
   * If the option is a multivalue option (that is, if the option has been
   * configured multiple times), this method returns the first value onyl. Use
   * {@link #getOptions(java.lang.String)} to get all option values.
   * 
   * @param name
   *          the option name
   * @return the option value
   * @see #options()
   * @see #hasOption(java.lang.String)
   * @see #getOption(java.lang.String, java.lang.String)
   */
  public String getOption(String name) {
    return config.getOption(name);
  }

  /**
   * Returns the option value for option <code>name</code> if it has been
   * configured, <code>defaultValue</code> otherwise.
   * 
   * @param name
   *          the option name
   * @param defaultValue
   *          the default value
   * @return the option value
   * @see #options()
   * @see #hasOption(java.lang.String)
   * @see #getOption(java.lang.String)
   */
  public String getOption(String name, String defaultValue) {
    String value = getOption(name);
    return (value != null) ? value : defaultValue;
  }

  /**
   * Returns the option values for option <code>name</code> if it has been
   * configured, <code>null</code> otherwise.
   * 
   * @param name
   *          the option name
   * @return the option values
   * @see #options()
   * @see #hasOption(java.lang.String)
   * @see #getOption(java.lang.String)
   */
  public String[] getOptions(String name) {
    return config.getOptions(name);
  }

  /**
   * @see ch.o2it.weblounge.common.api.util.Customizable#getOptions()
   */
  public Map getOptions() {
    return config.getOptions();
  }

  /**
   * Returns this module's renderer registry which keeps track of the defined
   * renderer bundles.
   * 
   * @return the renderer registry
   */
  public Renderer[] getRenderers() {
    return renderers;
  }

  /**
   * Returns the module renderer identified by <code>renderer</code> or
   * <code>null</code> if no such renderer is available.
   * 
   * @param renderer
   *          the renderer identifier
   * @param method
   *          the rendering method
   * @return the renderer
   */
  public Renderer getRenderer(String renderer, String method) {
    return renderers.getRenderer(renderer, method);
  }

  /**
   * @see ch.o2it.weblounge.common.site.api.module.Module#getRenderer(java.lang.String)
   */
  public Renderer getRenderer(String renderer) {
    return getRenderer(renderer, "html");
  }

  /**
   * Returns the renderer to the renderer pool. Returning the renderers is
   * important, since otherwise, a new renderer instance has to be instantiated
   * everytime it is needed, which is a costly operation and a waste of memory.
   * 
   * @param r
   *          the renderer
   */
  public void returnRenderer(Renderer r) {
    renderers.returnRenderer(r);
  }

  /**
   * Returns this module's service registry which keeps track of the defined
   * services.
   * 
   * @return the service registry
   */
  public Service[] getServices() {
    return services;
  }

  /**
   * Returns this module's job registry which keeps track of the defined jobs.
   * 
   * @return the job registry
   */
  public Job[] getJobs() {
    return jobs;
  }

  /**
   * Returns the action handler identified by <code>action</code> or
   * <code>null</code> if no such action is available.
   * 
   * @param action
   *          the action identifier
   * @param method
   *          the rendering method
   * @return the action handler
   */
  public Action getAction(String action, String method) {
    ActionHandlerBundle bundle = (ActionHandlerBundle) getActions().get(action);
    if (bundle != null) {
      return bundle.getAction(method);
    }
    return null;
  }

  /**
   * Returns the action handler to the registry.
   * 
   * @param handler
   *          the action handler
   */
  public void returnAction(Action handler) {
    getActions().returnHandler(handler);
  }

  /**
   * Returns the action registry containing all registered actions.
   * 
   * @return the actions
   */
  public ActionRegistry getActions() {
    return actions;
  }

  /**
   * Returns the module's image styles.
   * 
   * @see ch.o2it.weblounge.common.site.api.module.Module#getImageStyles()
   */
  public ImageStyleRegistry getImageStyles() {
    return imagestyles;
  }

  /**
   * This method is called by the <code>ModuleManager</code> to start
   * configuring the module.
   * 
   * @param config
   *          the module configuration
   * @param site
   *          the site
   */
  public void configure(ModuleConfiguration config, Site site)
      throws ConfigurationException {
    this.config = config;
    this.site = site;

    configureRenderers(config);
    configureActions(config);
    configureServices(config);
    configureJobs(config);
    configureImageStyles(config);
    configureControlpanels(config);
    configureWizards(config);
    // TODO: Add servlets
  }

  /**
   * Configures the module renderers, considering any renderer defintions from
   * the base module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureRenderers(ModuleConfiguration moduleconfig) {

    // Add renderer definitions from base module
    if (baseModule != null) {
      RendererRegistry baserenderers = baseModule.getRenderers();
      Iterator ri = baserenderers.values().iterator();
      while (ri.hasNext()) {
        RendererBundle r = (RendererBundle) ri.next();
        renderers.put(r.getIdentifier(), r);
      }
    }

    // Create renderer bundles
    Iterator ci = moduleconfig.getRenderers().values().iterator();
    while (ci.hasNext()) {
      RendererBundleConfiguration config = (RendererBundleConfiguration) ci.next();
      RendererBundle bundle = new RendererBundle(config.getIdentifier());
      bundle.init(config);
      renderers.put(config.getIdentifier(), bundle);
    }

    // Assign module
    Iterator ri = renderers.values().iterator();
    while (ri.hasNext()) {
      RendererBundle r = (RendererBundle) ri.next();
      r.setModule(this);
    }
  }

  /**
   * Configures the module actions, considering any action defintions from the
   * base module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureActions(ModuleConfiguration moduleconfig) {

    // Check base actions
    if (baseModule != null) {
      ActionRegistry baseactions = baseModule.getActions();
      Iterator ai = baseactions.values().iterator();
      while (ai.hasNext()) {
        ActionHandlerBundle a = (ActionHandlerBundle) ai.next();
        actions.put(a.getIdentifier(), a);
      }
    }

    // Create action bundles
    Iterator ci = moduleconfig.getActions().values().iterator();
    while (ci.hasNext()) {
      ActionBundleConfiguration config = (ActionBundleConfiguration) ci.next();
      ActionHandlerBundle bundle = new ActionHandlerBundle(config.getIdentifier(), moduleconfig.getLoadFactor());
      bundle.init(config);
      actions.put(config.getIdentifier(), bundle);
    }

    // Assign module
    Iterator ai = actions.values().iterator();
    while (ai.hasNext()) {
      ActionHandlerBundle a = (ActionHandlerBundle) ai.next();
      a.setModule(this);
    }
  }

  /**
   * Configures the module jobs, considering any job defintions from the base
   * module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureJobs(ModuleConfiguration moduleconfig) {

    // Check base jobs
    if (baseModule != null) {
      JobRegistry basejobs = baseModule.getJobs();
      Iterator ji = basejobs.values().iterator();
      while (ji.hasNext()) {
        Job j = (Job) ji.next();
        jobs.put(j.getIdentifier(), j);
      }
    }

    // Assign module
    Iterator ji = jobs.values().iterator();
    while (ji.hasNext()) {
      ModuleJob j = (ModuleJob) ji.next();
      j.setModule(this);
    }
  }

  /**
   * Configures the module image styles, considering any style defintions from
   * the base module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureImageStyles(ModuleConfiguration moduleconfig) {

    // Check base styles
    if (baseModule != null) {
      ImageStyleRegistry baseimagestyles = baseModule.getImageStyles();
      Iterator si = baseimagestyles.styles();
      while (si.hasNext()) {
        imagestyles.addStyle((ImageStyle) si.next());
      }
    }

    // Add module styles
    Iterator ci = moduleconfig.getImageStyles().values().iterator();
    while (ci.hasNext()) {
      imagestyles.addStyle((ImageStyle) ci.next());
    }
  }

  /**
   * Configures the module cotrolpanels, considering any controlpanel defintions
   * from the base module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureControlpanels(ModuleConfiguration moduleconfig) {

    // Check base jobs
    if (baseModule != null) {
      ControlPanelRegistry basecontrolpanels = baseModule.getControlPanels();
      Iterator ci = basecontrolpanels.values().iterator();
      while (ci.hasNext()) {
        ControlPanel c = (ControlPanel) ci.next();
        controlpanels.put(c.getIdentifier(), c);
      }
    }

    // Create controlpanels
    Iterator ci = moduleconfig.getControlPanels().values().iterator();
    while (ci.hasNext()) {
      ControlPanelConfiguration config = (ControlPanelConfiguration) ci.next();
      ModuleControlPanelImpl panel = new ModuleControlPanelImpl(config);
      controlpanels.put(config.getIdentifier(), panel);
    }

    // Assign module
    Iterator cpi = controlpanels.values().iterator();
    while (cpi.hasNext()) {
      ModuleControlPanel c = (ModuleControlPanel) cpi.next();
      c.setModule(this);
    }
  }

  /**
   * Configures the module wizards, considering any wizard defintions from the
   * base module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureWizards(ModuleConfiguration moduleconfig) {

    // Check base jobs
    if (baseModule != null) {
      WizardRegistry basewizards = baseModule.getWizards();
      Iterator ci = basewizards.values().iterator();
      while (ci.hasNext()) {
        WizardHandler wizard = (WizardHandler) ci.next();
        wizards.put(wizard.getIdentifier(), wizard);
      }
    }

    // Create controlpanels
    Iterator ci = moduleconfig.getWizards().values().iterator();
    while (ci.hasNext()) {
      WizardHandler wizard = (WizardHandler) ci.next();
      // TODO: Initialize wizard here, not in module config
      wizards.put(wizard.getIdentifier(), wizard);
    }

    // Assign module
    Iterator wi = wizards.values().iterator();
    while (wi.hasNext()) {
      WizardHandler wizard = (WizardHandler) wi.next();
      wizard.setModule(this);
    }
  }

  /**
   * Initializes and starts the module. This is done after configuration and
   * mainly registers and initializes the various module registries.
   */
  public void start() {

    Iterator options = config.options();
    while (options.hasNext()) {
      String o = (String) options.next();
      log_.debug("Found option " + o + ": " + config.getOption(o));
    }

    Registry handlerRegistry = SiteRegistries.get(RequestHandlerRegistry.ID, site);

    // Renderers

    ModuleRegistries.add(RendererRegistry.ID, this, renderers);
    Iterator ri = renderers.values().iterator();
    while (ri.hasNext()) {
      ((RendererBundle) ri.next()).setModule(this);
    }

    // Actions

    ActionRequestHandler actionRequestHandler = null;
    actionRequestHandler = (ActionRequestHandler) handlerRegistry.get(ActionRequestHandler.ID);
    if (actionRequestHandler == null) {
      actionRequestHandler = new ActionRequestHandler(getSite());
      handlerRegistry.put(ActionRequestHandler.ID, actionRequestHandler);
    }

    ModuleRegistries.add(ActionRegistry.ID, this, actions);
    if (actions.size() > 0) {
      getSite().addRequestHandler(actionRequestHandler);
      Iterator ai = actions.values().iterator();
      while (ai.hasNext()) {
        ActionHandlerBundle actionHandler = (ActionHandlerBundle) ai.next();
        actionHandler.setModule(this);
        actionRequestHandler.addHandler(actionHandler);
        log_.debug("Action handler '" + actionHandler + "' registered");
      }
    }

    // Wizards

    WizardRequestHandler wizardRequestHandler = null;
    wizardRequestHandler = (WizardRequestHandler) handlerRegistry.get(WizardRequestHandler.ID);
    if (wizardRequestHandler == null) {
      wizardRequestHandler = new WizardRequestHandler();
      handlerRegistry.put(WizardRequestHandler.ID, wizardRequestHandler);
    }

    ModuleRegistries.add(WizardRegistry.ID, this, wizards);
    if (wizards.size() > 0) {
      getSite().addRequestHandler(wizardRequestHandler);
      Iterator wi = wizards.values().iterator();
      while (wi.hasNext()) {
        WizardHandler wizardHandler = (WizardHandler) wi.next();
        wizardHandler.setModule(this);
        wizardRequestHandler.addHandler(wizardHandler);
        log_.debug("Wizard handler '" + wizardHandler + "' registered");
      }
    }

    // Services

    ModuleRegistries.add(ServiceRegistry.ID, this, services);
    Iterator si = services.values().iterator();
    while (si.hasNext()) {
      ModuleServiceImpl service = (ModuleServiceImpl) si.next();
      service.setModule(this);
      try {
        ServiceManager.startService(service);
      } catch (ServiceException e) {
        log_.error("Unable to start service '" + service + "' of module '" + this + "':" + e.getMessage());
      } catch (ServiceDependencyException e) {
        log_.error("Unable to start service '" + service + "' of module '" + this + "' due to cirular dependencies.");
      }
    }

    // Servlets

    ServletRequestHandler servletHandler = null;
    Registry handlers = SiteRegistries.get(RequestHandlerRegistry.ID, site);
    servletHandler = (ServletRequestHandler) handlers.get(ServletRequestHandler.ID);
    if (servletHandler != null) {
      for (Iterator servlets = ((ModuleConfigurationImpl) config).servlets.iterator(); servlets.hasNext();) {
        try {
          servletHandler.addServlet((ServletConfiguration) servlets.next(), classLoader);
        } catch (ConfigurationException e) {
          log_.debug("Error configuring servlet!", e);
          log_.error("Error configuring servlet: " + e.getMessage());
        }
      }
      for (Iterator mappings = ((ModuleConfigurationImpl) config).servletMappings.iterator(); mappings.hasNext();) {
        try {
          servletHandler.addServletMapping((ServletMapping) mappings.next());
        } catch (ConfigurationException e) {
          log_.debug("Error configuring servlet mapping!", e);
          log_.error("Error configuring servlet: " + e.getMessage());
        }
      }
    } else {
      log_.error("The servlet request handler for site '" + site + "' is not accessible!");
    }

    // Control Panels

    ModuleRegistries.add(ControlPanelRegistry.ID, this, controlpanels);
    if (controlpanels.size() > 0) {
      Iterator ci = controlpanels.values().iterator();
      while (ci.hasNext()) {
        ControlPanel cp = (ControlPanel) ci.next();
        getSite().getControlPanels().addControlPanel(cp);
        log_.debug("Control panel '" + cp + "' registered");
      }
    }

    // Image styles

    // Site is loading image styles itself.
    /*
     * Iterator ii = imagestyles.styles(); while (ii.hasNext()) { ImageStyle
     * style = (ImageStyle)ii.next();
     * getSite().getImageStyles().addStyle(style); log_.debug("Image style '" +
     * style + "' registered"); }
     */
    // Jobs

    Iterator ji = jobs.values().iterator();
    while (ji.hasNext()) {
      ModuleJob job = (ModuleJob) ji.next();
      job.setModule(this);
      Daemon.getInstance().addJob(job);
    }

    // Tell listeners
    fireModuleStartup();
  }

  /**
   * Method to restart this module. This includes restarting all services and
   * sending a <code>restart</code> event to registered module listeners.
   */
  public void restart() {
    stop();
    start();
  }

  /**
   * Method to shut down this module. This includes stopping the module services
   * and sending a <code>shutdown</code> event to registered module listeners.
   */
  public void stop() {
    Registry handlerRegistry = SiteRegistries.get(RequestHandlerRegistry.ID, site);

    // Actions

    ActionRequestHandler actionRequestHandler = null;
    actionRequestHandler = (ActionRequestHandler) handlerRegistry.get(ActionRequestHandler.ID);
    site.removeRequestHandler(actionRequestHandler);
    Iterator ai = actions.values().iterator();
    while (ai.hasNext()) {
      ActionHandlerBundle aHandler = (ActionHandlerBundle) ai.next();
      actionRequestHandler.removeHandler(aHandler);
    }

    // Wizards

    WizardRequestHandler wizardRequestHandler = null;
    wizardRequestHandler = (WizardRequestHandler) handlerRegistry.get(WizardRequestHandler.ID);
    site.removeRequestHandler(wizardRequestHandler);
    Iterator wi = wizards.values().iterator();
    while (wi.hasNext()) {
      WizardHandler wizardHandler = (WizardHandler) wi.next();
      wizardRequestHandler.removeHandler(wizardHandler);
    }

    // Services

    ServiceManager.stopAllServices(services);

    // Control Panels

    if (controlpanels.size() > 0) {
      Iterator ci = controlpanels.values().iterator();
      while (ci.hasNext()) {
        ControlPanel cp = (ControlPanel) ci.next();
        site.getControlPanels().removeControlPanel(cp);
        log_.debug("Control panel '" + cp + "' removed");
      }
    }

    // Tell the others

    fireModuleShutdown();
  }

  /*
   * ------------------------------------------------------------- I N T E R F A
   * C E UserListener
   * -------------------------------------------------------------
   */

  /**
   * This method is called if the user moves from one url to another. Note that
   * moving does not include calling actions. Only movements that are detected
   * by the <code>SimpleRequestHandler</code> are noted.
   * 
   * @param user
   *          the moving user
   * @param url
   *          the url that the user moved to
   */
  public void userMoved(User user, WebUrl url) {
  }

  /**
   * This method is called if a user logs in.
   * 
   * @param user
   *          the user that logged in
   */
  public void userLoggedIn(User user) {
  }

  /**
   * This method is called if a user logs out.
   * 
   * @param user
   *          the user that logged out
   */
  public void userLoggedOut(User user) {
  }

  /*
   * ------------------------------------------------------------- I N T E R F A
   * C E PageListener
   * -------------------------------------------------------------
   */

  /**
   * This method is called if the page at location <code>url</code> has been
   * created by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the creating user
   * @see ch.o2it.weblounge.api.content.PageListener#pageCreated(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageCreated(WebUrl url, User user) {
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * removed by user <code>user</code>.
   * 
   * @param url
   *          the page's former location
   * @param user
   *          the removing user
   * @see ch.o2it.weblounge.api.content.PageListener#pageRemoved(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.security.User)
   * @see ch.o2it.weblounge.api.content.PageListener#pageRemoved(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageRemoved(WebUrl url, User user) {
  }

  /**
   * This method is called if the page at location <code>from</code> has been
   * moved to <code>to</code> by user <code>user</code>.
   * 
   * @param from
   *          the page's former location
   * @param to
   *          the page's new location
   * @param user
   *          the user moving the page
   * @see ch.o2it.weblounge.api.content.PageListener#pageMoved(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.url.WebUrl, ch.o2it.weblounge.api.security.User)
   */
  public void pageMoved(WebUrl from, WebUrl to, User user) {
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * published by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user publishing the page
   * @see ch.o2it.weblounge.api.content.PageListener#pagePublished(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pagePublished(WebUrl url, User user) {
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * unpublished by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user unpublishing the page
   * @see ch.o2it.weblounge.api.content.PageListener#pageUnpublished(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageUnpublished(WebUrl url, User user) {
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * locked by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user locking the page
   * @see ch.o2it.weblounge.api.content.PageListener#pageLocked(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageLocked(WebUrl url, User user) {
  }

  /**
   * This method is called if the page at location <code>url</code> has been
   * released by user <code>user</code>.
   * 
   * @param url
   *          the page's location
   * @param user
   *          the user releasing the page lock
   * @see ch.o2it.weblounge.api.content.PageListener#pageUnlocked(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageUnlocked(WebUrl url, User user) {
  }

  /**
   * Notifies the listener about a new page renderer at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newRenderer
   *          the new renderer
   * @param oldRenderer
   *          the former renderer
   * @param user
   *          the editing user
   * @see ch.o2it.weblounge.api.content.PageListener#pageRendererChanged(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.api.renderer.Renderer,
   *      ch.o2it.weblounge.api.renderer.Renderer,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageRendererChanged(WebUrl url, Renderer newRenderer,
      Renderer oldRenderer, User user) {
  }

  /**
   * Notifies the listener about a new page layout at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newLayout
   *          the new layout
   * @param oldLayout
   *          the former layout
   * @param user
   *          the editing user
   * @see ch.o2it.weblounge.api.content.PageListener#pageLayoutChanged(ch.o2it.weblounge.api.url.WebUrl,
   *      ch.o2it.weblounge.core.content.Layout,
   *      ch.o2it.weblounge.core.content.Layout,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageLayoutChanged(WebUrl url, Layout newLayout, Layout oldLayout,
      User user) {
  }

  /**
   * Notifies the listener about a new page type at url <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newType
   *          the new page type
   * @param oldType
   *          the former page type
   * @param user
   *          the editing user
   * @see ch.o2it.weblounge.api.content.PageListener#pageTypeChanged(ch.o2it.weblounge.api.url.WebUrl,
   *      java.lang.String, java.lang.String,
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageTypeChanged(WebUrl url, String newType, String oldType,
      User user) {
  }

  /**
   * Notifies the listener about a change in the list of keywords at url
   * <code>url</code>.
   * 
   * @param url
   *          the page url
   * @param newKeywords
   *          the new keywords
   * @param oldKeywords
   *          the old keywords
   * @param user
   *          the editing user
   * @see ch.o2it.weblounge.api.content.PageListener#pageKeywordsChanged(ch.o2it.weblounge.api.url.WebUrl,
   *      java.lang.String[], java.lang.String[],
   *      ch.o2it.weblounge.api.security.User)
   */
  public void pageKeywordsChanged(WebUrl url, String[] newKeywords,
      String[] oldKeywords, User user) {
  }

  /**
   * Returns the string representation of the module.
   * 
   * @return the module identifier
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return config.getIdentifier();
  }

  /**
   * Returns the title in the given language or, if it doesn't exist in that
   * language, in the site default language.
   * 
   * @param language
   *          the language
   * @return the title in the given language
   */
  public String getTitle(Language language) {
    return (baseModule != null) ? baseModule.getTitle(language) : config.getDescription(language);
  }

  /**
   * This method notifies the listeners that the module has started.
   */
  protected void fireModuleStartup() {
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).moduleStarted(this);
    }
  }

  /**
   * This method notifies the listeners of a module shutdown.
   */
  protected void fireModuleShutdown() {
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).moduleStopped(this);
    }
  }

}