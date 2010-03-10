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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.util.config.OptionsHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.SearchResult;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.ModuleException;
import ch.o2it.weblounge.common.site.ModuleListener;
import ch.o2it.weblounge.common.site.PageletRenderer;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Base implementation for a module. It is recommended that individual
 * <code>Module</code> implementations extend this class.
 */
public class ModuleImpl implements Module {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(ModuleImpl.class);

  /** Regular expression to test the validity of a module identifier */
  private static final String MODULE_IDENTIFIER_REGEX = "^[a-zA-Z0-9-_.]*$";

  /** The module identifier */
  protected String identifier = null;
  
  /** The url that is used to reach module assets */
  protected WebUrl url = null;

  /** Module enabled state */
  protected boolean enabled = false;

  /** Module running state */
  private boolean running = false;

  /** Can this module be searched? */
  protected boolean searchable = false;

  /** The hosting site */
  protected Site site = null;

  /** Option handling support */
  protected OptionsHelper options = null;

  /** Localized module title */
  protected LocalizableContent<String> title = null;

  /** Module pagelet renderers */
  protected Map<String, PageletRenderer> renderers = null;

  /** Module actions */
  protected Map<String, Action> actions = null;

  /** Module image styles */
  protected Map<String, ImageStyle> imagestyles = null;

  /** List of module listeners */
  protected List<ModuleListener> moduleListeners = null;

  /**
   * Creates a new module.
   */
  public ModuleImpl() {
    title = new LocalizableContent<String>();
    renderers = new HashMap<String, PageletRenderer>();
    actions = new HashMap<String, Action>();
    imagestyles = new HashMap<String, ImageStyle>();
    options = new OptionsHelper();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#setIdentifier(java.lang.String)
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
   * @see ch.o2it.weblounge.common.site.Module#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.Module#getUrl()
   */
  public WebUrl getUrl() {
    if (url != null)
      return url;
    if (site == null)
      throw new IllegalStateException("Site has not yet been set");
    url = new WebUrlImpl(site, UrlSupport.concat(new String[] {
        site.getUrl().getPath(),
        "module",
        identifier
    }));
    return url;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#addModuleListener(ch.o2it.weblounge.common.site.ModuleListener)
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
   * @see ch.o2it.weblounge.common.site.Module#removeModuleListener(ch.o2it.weblounge.common.site.ModuleListener)
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
   * @see ch.o2it.weblounge.common.site.Module#init(ch.o2it.weblounge.common.site.Site)
   */
  public void init(Site site) throws ModuleException {
    this.site = site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#destroy()
   */
  public void destroy() {
    this.site = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#getAction(java.lang.String)
   */
  public Action getAction(String id) {
    return actions.get(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#getActions()
   */
  public Action[] getActions() {
    return actions.values().toArray(new Action[actions.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#getImageStyles()
   */
  public ImageStyle[] getImageStyles() {
    return imagestyles.values().toArray(new ImageStyle[imagestyles.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#getImageStyle(java.lang.String)
   */
  public ImageStyle getImageStyle(String id) {
    return imagestyles.get(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#getRenderer(java.lang.String)
   */
  public PageletRenderer getRenderer(String id) {
    return renderers.get(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#getRenderers()
   */
  public PageletRenderer[] getRenderers() {
    return renderers.values().toArray(new PageletRenderer[renderers.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#setTitle(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    this.title.put(title, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#getTitle(ch.o2it.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    return title.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#setSearchable(boolean)
   */
  public void setSearchable(boolean searchable) {
    this.searchable = searchable;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#isSearchable()
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
   * @see ch.o2it.weblounge.common.site.Module#search(java.lang.String)
   */
  public SearchResult[] search(String query) {
    return new SearchResult[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#start()
   */
  public void start() throws ModuleException {
    log_.debug("Starting module {}", this);
    if (running)
      throw new IllegalStateException("Module is already running");
    if (!enabled)
      throw new IllegalStateException("Cannot start a disabled module");

    // TODO: Implement module start

    // Finally, mark this module as running
    running = true;
    log_.info("Module {} started", this);

    // Tell listeners
    fireModuleStarted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Module#stop()
   */
  public void stop() throws ModuleException {
    log_.debug("Stopping site {}", this);
    if (!running)
      throw new IllegalStateException("Site is not running");

    // TODO: Implement module shutdown

    // Finally, mark this module as stopped
    running = false;
    log_.info("Module {} stopped", this);

    // Tell listeners
    fireModuleStopped();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String name, String value) {
    options.setOption(name, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    options.removeOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name) {
    return options.getOptionValue(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValue(java.lang.String,
   *      java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    return options.getOptionValue(name, defaultValue);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValues(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    return options.getOptionValues(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return options.hasOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptions()
   */
  public Map<String, List<String>> getOptions() {
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
