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

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletRendererImpl;
import ch.entwine.weblounge.common.impl.scheduler.QuartzJob;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.impl.util.config.OptionsHelper;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.impl.util.xml.XPathNamespaceContext;
import ch.entwine.weblounge.common.scheduler.Job;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.ModuleException;
import ch.entwine.weblounge.common.site.ModuleListener;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteURL;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.common.url.WebUrl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Base implementation for a module. It is recommended that individual
 * <code>Module</code> implementations extend this class.
 */
public class ModuleImpl implements Module {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(ModuleImpl.class);

  /** Regular expression to test the validity of a module identifier */
  private static final String MODULE_IDENTIFIER_REGEX = "^[a-zA-Z0-9]+[a-zA-Z0-9-_.]*$";

  /** Xml namespace for the module */
  public static final String MODULE_XMLNS = "http://www.entwinemedia.com/weblounge/3.0/module";

  /** The module identifier */
  protected String identifier = null;

  /** The url that is used to reach module assets */
  protected WebUrl url = null;

  /** Module enabled state */
  protected boolean enabled = true;

  /** Module running state */
  private boolean running = false;

  /** Can this module be searched? */
  protected boolean searchable = false;

  /** The hosting site */
  protected Site site = null;

  /** Option handling support */
  protected OptionsHelper options = null;

  /** Localized module title */
  protected String name = null;

  /** Module pagelet renderers */
  protected Map<String, PageletRenderer> renderers = null;

  /** Module actions */
  protected Map<String, Action> actions = null;

  /** Module image styles */
  protected Map<String, ImageStyle> imagestyles = null;

  /** Module jobs */
  protected Map<String, Job> jobs = null;

  /** List of module listeners */
  protected List<ModuleListener> moduleListeners = null;

  /** The environment */
  protected Environment environment = Environment.Production;

  /**
   * Creates a new module.
   */
  public ModuleImpl() {
    renderers = new HashMap<String, PageletRenderer>();
    actions = new HashMap<String, Action>();
    imagestyles = new HashMap<String, ImageStyle>();
    jobs = new HashMap<String, Job>();
    options = new OptionsHelper();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#initialize(ch.entwine.weblounge.common.site.Environment)
   */
  public void initialize(Environment environment) {
    this.environment = environment;

    // Tell the renderers about the environment
    for (PageletRenderer renderer : renderers.values()) {
      renderer.setEnvironment(environment);
    }

    // Tell the actions about the environment
    for (Action action : actions.values()) {
      action.setEnvironment(environment);
    }

    // Tell the jobs about the environment
    for (Job job : jobs.values()) {
      job.setEnvironment(environment);
    }

    // Switch the options to the new environment
    options.setEnvironment(environment);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    if (identifier == null)
      throw new IllegalArgumentException("Module identifier must not be null");
    else if (!Pattern.matches(MODULE_IDENTIFIER_REGEX, identifier))
      throw new IllegalArgumentException("Module identifier '" + identifier + "' is malformed");
    this.identifier = identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getUrl()
   */
  public WebUrl getUrl() {
    return getUrl(environment);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getUrl(ch.entwine.weblounge.common.site.Environment)
   */
  public WebUrl getUrl(Environment environment) {
    if (url != null)
      return url;
    if (site == null)
      throw new IllegalStateException("Site has not yet been set");
    SiteURL siteURL = site.getHostname(environment);
    url = new WebUrlImpl(site, UrlUtils.concat(siteURL.toExternalForm(), "module", identifier));
    return url;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#addModuleListener(ch.entwine.weblounge.common.site.ModuleListener)
   */
  public void addModuleListener(ModuleListener listener) {
    if (moduleListeners == null)
      moduleListeners = new ArrayList<ModuleListener>();
    synchronized (moduleListeners) {
      moduleListeners.add(listener);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#removeModuleListener(ch.entwine.weblounge.common.site.ModuleListener)
   */
  public void removeModuleListener(ModuleListener listener) {
    if (moduleListeners != null) {
      synchronized (moduleListeners) {
        moduleListeners.remove(listener);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#setSite(ch.entwine.weblounge.common.site.Site)
   */
  public void setSite(Site site) throws ModuleException {
    this.site = site;

    // Initialize actions
    for (Action action : actions.values()) {
      action.setSite(site);
    }

    // Initialize renderers
    for (PageletRenderer renderer : renderers.values()) {
      renderer.setModule(this);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#destroy()
   */
  public void destroy() {
    this.site = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#addAction(ch.entwine.weblounge.common.site.Action)
   */
  public void addAction(Action action) {
    actions.put(action.getIdentifier(), action);
    action.setModule(this);
    if (site != null)
      action.setSite(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#removeAction(ch.entwine.weblounge.common.site.Action)
   */
  public void removeAction(Action action) {
    actions.remove(action.getIdentifier());
    action.setModule(null);
    action.setSite(null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getAction(java.lang.String)
   */
  public Action getAction(String id) {
    return actions.get(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getActions()
   */
  public Action[] getActions() {
    return actions.values().toArray(new Action[actions.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#addImageStyle(ch.entwine.weblounge.common.content.image.ImageStyle)
   */
  public void addImageStyle(ImageStyle imagestyle) {
    imagestyles.put(imagestyle.getIdentifier(), imagestyle);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#removeImageStyle(ch.entwine.weblounge.common.content.image.ImageStyle)
   */
  public void removeImageStyle(ImageStyle imagestyle) {
    imagestyles.remove(imagestyle.getIdentifier());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getImageStyles()
   */
  public ImageStyle[] getImageStyles() {
    return imagestyles.values().toArray(new ImageStyle[imagestyles.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getImageStyle(java.lang.String)
   */
  public ImageStyle getImageStyle(String id) {
    return imagestyles.get(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#addRenderer(ch.entwine.weblounge.common.content.page.PageletRenderer)
   */
  public void addRenderer(PageletRenderer renderer) {
    renderers.put(renderer.getIdentifier(), renderer);
    if (site != null) {
      renderer.setModule(this);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#removeRenderer(ch.entwine.weblounge.common.content.page.PageletRenderer)
   */
  public void removeRenderer(PageletRenderer renderer) {
    renderers.remove(renderer.getIdentifier());
    renderer.setModule(null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getRenderer(java.lang.String)
   */
  public PageletRenderer getRenderer(String id) {
    return renderers.get(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getRenderers()
   */
  public PageletRenderer[] getRenderers() {
    return renderers.values().toArray(new PageletRenderer[renderers.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#addJob(ch.entwine.weblounge.common.scheduler.Job)
   */
  public void addJob(Job job) {
    jobs.put(job.getIdentifier(), job);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#removeJob(ch.entwine.weblounge.common.scheduler.Job)
   */
  public void removeJob(Job job) {
    jobs.remove(job.getIdentifier());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getJob(java.lang.String)
   */
  public Job getJob(String id) {
    return jobs.get(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getJobs()
   */
  public Job[] getJobs() {
    return jobs.values().toArray(new Job[jobs.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#setName(java.lang.String)
   */
  public void setName(String title) {
    this.name = title;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#setSearchable(boolean)
   */
  public void setSearchable(boolean searchable) {
    this.searchable = searchable;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#isSearchable()
   */
  public boolean isSearchable() {
    return searchable;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This default implementation will always return an empty result set.
   * Subclasses returning <code>true</code> in {@link #isSearchable()} should
   * therefore overwrite this method.
   * 
   * @see ch.entwine.weblounge.common.site.Module#search(java.lang.String)
   */
  public SearchResultItem[] search(String query) {
    return new SearchResultItem[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#start()
   */
  public void start() throws ModuleException {
    logger.debug("Starting module {}", this);
    if (running)
      throw new IllegalStateException("Module is already running");
    if (!enabled)
      throw new IllegalStateException("Cannot start a disabled module");

    // Finally, mark this module as running
    running = true;
    logger.debug("Module '{}' started", this);

    // Tell listeners
    fireModuleStarted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#stop()
   */
  public void stop() throws ModuleException {
    logger.debug("Stopping module {}", this);
    if (!running)
      throw new IllegalStateException("Module is not running");

    // Finally, mark this module as stopped
    running = false;
    logger.debug("Module '{}' stopped", this);

    // Tell listeners
    fireModuleStopped();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String name, String value) {
    options.setOption(name, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String, ch.entwine.weblounge.common.site.Environment)
   */
  public void setOption(String name, String value, Environment environment) {
    options.setOption(name, value, environment);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    options.removeOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name) {
    return options.getOptionValue(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValue(java.lang.String,
   *      java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    return options.getOptionValue(name, defaultValue);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValues(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    return options.getOptionValues(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return options.hasOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionNames()
   */
  public String[] getOptionNames() {
    return options.getOptionNames();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptions()
   */
  public Map<String, Map<Environment, List<String>>> getOptions() {
    return options.getOptions();
  }

  /**
   * Method to fire a <code>moduleStarted()</code> message to all registered
   * <code>ModuleListener</code>s.
   */
  protected void fireModuleStarted() {
    if (moduleListeners == null)
      return;
    synchronized (moduleListeners) {
      for (ModuleListener listener : moduleListeners) {
        listener.moduleStarted(this);
      }
    }
  }

  /**
   * Method to fire a <code>moduleStopped()</code> message to all registered
   * <code>ModuleListener</code>s.
   */
  protected void fireModuleStopped() {
    if (moduleListeners == null)
      return;
    synchronized (moduleListeners) {
      for (ModuleListener listener : moduleListeners) {
        listener.moduleStopped(this);
      }
    }
  }

  /**
   * Initializes this module from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param config
   *          the module node
   * @throws IllegalStateException
   *           if the module cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static Module fromXml(Node config) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();

    // Define the xml namespace
    XPathNamespaceContext nsCtx = new XPathNamespaceContext(false);
    nsCtx.defineNamespaceURI("m", MODULE_XMLNS);
    xpath.setNamespaceContext(nsCtx);

    return fromXml(config, xpath);
  }

  /**
   * Initializes this module from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param config
   *          the module node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the module cannot be parsed
   * @see #toXml()
   */
  @SuppressWarnings("unchecked")
  public static Module fromXml(Node config, XPath xpathProcessor)
      throws IllegalStateException {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    // identifier
    String identifier = XPathHelper.valueOf(config, "@id", xpathProcessor);
    if (identifier == null)
      throw new IllegalStateException("Unable to create module without identifier");

    // class
    Module module = null;
    String className = XPathHelper.valueOf(config, "m:class", xpathProcessor);
    if (className != null) {
      try {
        Class<? extends Module> c = (Class<? extends Module>) classLoader.loadClass(className);
        module = c.newInstance();
        module.setIdentifier(identifier);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Implementation " + className + " for module '" + identifier + "' not found", e);
      } catch (InstantiationException e) {
        throw new IllegalStateException("Error instantiating impelementation " + className + " for module '" + identifier + "'", e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Access violation instantiating implementation " + className + " for module '" + identifier + "'", e);
      } catch (Throwable t) {
        throw new IllegalStateException("Error loading implementation " + className + " for module '" + identifier + "'", t);
      }
    } else {
      module = new ModuleImpl();
      module.setIdentifier(identifier);
    }
    
    // Check if module is enabled
    Boolean enabled = Boolean.valueOf(XPathHelper.valueOf(config, "m:enable", xpathProcessor));
    module.setEnabled(enabled);
    
    // name
    String name = XPathHelper.valueOf(config, "m:name", xpathProcessor);
    module.setName(name);

    // pagelets
    NodeList pageletNodes = XPathHelper.selectList(config, "m:pagelets/m:pagelet", xpathProcessor);
    for (int i = 0; i < pageletNodes.getLength(); i++) {
      PageletRenderer pagelet = PageletRendererImpl.fromXml(pageletNodes.item(i), xpathProcessor);
      module.addRenderer(pagelet);
    }

    // actions
    NodeList actionNodes = XPathHelper.selectList(config, "m:actions/m:action", xpathProcessor);
    for (int i = 0; i < actionNodes.getLength(); i++) {
      module.addAction(ActionSupport.fromXml(actionNodes.item(i), xpathProcessor));
    }

    // image styles
    NodeList imagestyleNodes = XPathHelper.selectList(config, "m:imagestyles/m:imagestyle", xpathProcessor);
    for (int i = 0; i < imagestyleNodes.getLength(); i++) {
      module.addImageStyle(ImageStyleImpl.fromXml(imagestyleNodes.item(i), xpathProcessor));
    }

    // jobs
    NodeList jobNodes = XPathHelper.selectList(config, "m:jobs/m:job", xpathProcessor);
    for (int i = 0; i < jobNodes.getLength(); i++) {
      module.addJob(QuartzJob.fromXml(jobNodes.item(i), xpathProcessor));
    }

    // options
    Node optionsNode = XPathHelper.select(config, "m:options", xpathProcessor);
    OptionsHelper.fromXml(optionsNode, module, xpathProcessor);

    return module;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Module#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<module id=\"");
    b.append(identifier);
    b.append("\" ");

    // namespace and schema
    b.append("xmlns=\"http://www.entwinemedia.com/weblounge/3.0/module\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.entwinemedia.com/weblounge/3.0/module http://www.entwinemedia.com/xsd/weblounge/3.0/module.xsd\"");

    b.append(">");

    // enable
    b.append("<enable>").append(enabled).append("</enable>");

    // Names
    if (StringUtils.isNotBlank(name)) {
      b.append("<name><![CDATA[");
      b.append(name);
      b.append("]]></name>");
    }

    // class
    if (!this.getClass().equals(ModuleImpl.class))
      b.append("<class>").append(getClass().getName()).append("</class>");

    // pagelets
    if (renderers.size() > 0) {
      b.append("<pagelets>");
      for (PageletRenderer renderer : renderers.values()) {
        b.append(renderer.toXml());
      }
      b.append("</pagelets>");
    }

    // actions
    if (actions.size() > 0) {
      b.append("<actions>");
      for (Action action : actions.values()) {
        b.append(action.toXml());
      }
      b.append("</actions>");
    }

    // jobs
    if (jobs.size() > 0) {
      b.append("<jobs>");
      for (Job job : jobs.values()) {
        b.append(job.toXml());
      }
      b.append("</jobs>");
    }

    // image styles
    if (imagestyles.size() > 0) {
      b.append("<imagestyles>");
      for (ImageStyle imagestyle : imagestyles.values()) {
        b.append(imagestyle.toXml());
      }
      b.append("</imagestyles>");
    }

    // Options
    b.append(options.toXml());

    b.append("</module>");
    return b.toString();
  }

  /**
   * Returns <code>true</code> if <code>obj</code> is a module featuring the
   * same module identifier than this one.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Module) {
      Module m = (Module) obj;
      if (identifier == null || m.getIdentifier() == null || !identifier.equals(m.getIdentifier()))
        return false;
      if (site == null && m.getSite() != null)
        return false;
      if (site != null && !site.equals(m.getSite()))
        return false;
      return true;
    }
    return false;
  }

  /**
   * Returns the module identifier's hash code.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (identifier != null)
      return identifier.hashCode();
    else
      return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (identifier != null)
      return identifier;
    else
      return super.toString();
  }

}
