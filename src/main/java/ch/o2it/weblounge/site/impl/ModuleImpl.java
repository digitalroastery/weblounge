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

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.ServletConfiguration;
import ch.o2it.weblounge.common.impl.util.ServletMapping;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Layout;
import ch.o2it.weblounge.common.page.PageListener;
import ch.o2it.weblounge.common.page.SearchResult;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.repository.Collection;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.Job;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.ModuleConfiguration;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
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

public class ModuleImpl implements Module {

  /** Logging facility */
  final static Logger log_ = LoggerFactory.getLogger(ModuleImpl.class);

  /** The module configuration */
  protected ModuleConfiguration config = null;

  /** The renderers */
  protected Map<String, Renderer> renderers = null;

  /** The actions */
  protected Map<String, Action> actions = null;

  /** The jobs */
  protected Map<String, Job> jobs = null;

  /** The image styles */
  protected Map<String, ImageStyle> imagestyles = null;

  /** The module listeners */
  protected List<ModuleListener> listeners = null;

  /** The class loader */
  protected ClassLoader classLoader = null;

  /** The associated site */
  protected Site site = null;

  /**
   * Constructor for class ModuleImpl. Constructor has package access because
   * module objects should only be created via the <code>ModuleManager</code>.
   * To obtain a reference to a specific module object, use the
   * <code>ModuleRegistry</code>.
   */
  protected ModuleImpl() {
    classLoader = Thread.currentThread().getContextClassLoader();
    listeners = new ArrayList<ModuleListener>();
    renderers = new HashMap<String, Renderer>();
    actions = new HashMap<String, Action>();
    jobs = new HashMap<String, Job>();
    imagestyles = new HashMap<String, ImageStyle>();
  }

  /**
   * Returns the module name, which is equal to the identifier configured in the
   * module descriptor.
   * 
   * @return the module identifier
   */
  public String getIdentifier() {
    return config.getIdentifier();
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
   * Returns this module's renderers which keeps track of the defined
   * renderer bundles.
   * 
   * @return the renderers
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
   * Returns this module's services which keeps track of the defined
   * services.
   * 
   * @return the services
   */
  public Service[] getServices() {
    return services;
  }

  /**
   * Returns this module's jobs which keeps track of the defined jobs.
   * 
   * @return the jobs
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
    ActionHandlerBundle bundle = (ActionHandlerBundle) actions.get(action);
    if (bundle != null) {
      return bundle.getAction(method);
    }
    return null;
  }

  /**
   * Returns the action handler to thes.
   * 
   * @param handler
   *          the action handler
   */
  public void returnAction(Action handler) {
    getActions().returnHandler(handler);
  }

  /**
   * Returns the actions containing all registered actions.
   * 
   * @return the actions
   */
  public Action[] getActions() {
    return actions;
  }

  /**
   * Returns the module's image styles.
   * 
   * @see ch.o2it.weblounge.common.site.api.module.Module#getImageStyles()
   */
  public ImageStyle[] getImageStyles() {
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
    configureJobs(config);
    configureImageStyles(config);
  }

  /**
   * Configures the module renderers, considering any renderer definitions from
   * the base module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureRenderers(ModuleConfiguration moduleconfig) {

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
   * Configures the module actions, considering any action definitions from the
   * base module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureActions(ModuleConfiguration moduleconfig) {

    // Create action bundles
    Iterator ci = moduleconfig.getActions().iterator();
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
   * Configures the module jobs, considering any job definitions from the base
   * module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureJobs(ModuleConfiguration moduleconfig) {
    for (Job job : moduleconfig.getJobs()) {
      if (job instanceof ModuleJob)
        ((ModuleJob)job).setModule(this);
      jobs.put(job.getIdentifier(), job);
    }
  }

  /**
   * Configures the module image styles, considering any style definitions from
   * the base module, if given.
   * 
   * @param config
   *          the module configuration
   */
  private void configureImageStyles(ModuleConfiguration moduleconfig) {
    Iterator ci = moduleconfig.getImageStyles().values().iterator();
    while (ci.hasNext()) {
      imagestyles.addStyle((ImageStyle) ci.next());
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

   s handlerRegistry = SiteRegistries.get(RequestHandlerRegistry.ID, site);

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
   s handlers = SiteRegistries.get(RequestHandlerRegistry.ID, site);
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
    return config.getDescription(language);
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