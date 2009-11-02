/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.site;

import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.SearchResult;
import ch.o2it.weblounge.common.renderer.Renderer;

/**
 * The module interface defines the method that may be called on weblounge
 * module objects.
 */
public interface Module extends Customizable {

  /** Module descriptor */
  public static final String CONFIG_FILE = "module.xml";

  /** The default load factor */
  public static final int DEFAULT_LOAD_FACTOR = 1;

  /**
   * Returns the module name.
   * 
   * @return the module identifier
   */
  String getIdentifier();

  /**
   * Returns <code>true</code> if the module is enabled.
   * 
   * @return <code>true</code> if the module is enabled
   */
  boolean isEnabled();

  /**
   * Returns the module's document base.
   * 
   * @return the module's document base
   */
  String getPath();

  /**
   * Returns the module's load factor, which is <code>1</code> for normal
   * modules This factor can be configured in the
   * <code>&lt;performance&gt;</code> section of the module configuration.
   * 
   * @return the module's load factor
   */
  int getLoadFactor();

  /**
   * Returns the associated site.
   * 
   * @return the associated site
   */
  Site getSite();

  /**
   * Initializes and starts the module. This is done after configuration and
   * mainly registers and initializes the various module registries.
   */
  void start();

  /**
   * Method to shut down this module. This includes stopping the module services
   * and sending a <code>shutdown</code> event to registered module listeners.
   */
  void stop();

  /**
   * Adds <code>listener</code> to the list of module listeners.
   * 
   * @param listener
   *          the module listener
   */
  void addModuleListener(ModuleListener listener);

  /**
   * Removes <code>listener</code> from the list of module listeners.
   * 
   * @param listener
   *          the module listener
   */
  void removeModuleListener(ModuleListener listener);

  /**
   * Returns <code>true</code> if the module may be searched.
   * 
   * @return <code>true</code> if the module is searchable
   */
  boolean isSearchable();

  /**
   * Returns <code>true</code> if the module should be included in the default
   * searched by default.
   * 
   * @return <code>true</code> if the module is searchable
   */
  boolean searchByDefault();

  /**
   * Returns the result of the search query.
   * 
   * @param query
   *          the search query
   * @return the result set
   */
  SearchResult[] search(String query);

  /**
   * Returns this module's job registry which keeps track of the defined jobs.
   * 
   * @return the job registry
   */
  Job[] getJobs();

  /**
   * Returns this module's renderer registry which keeps track of the defined
   * renderer bundles.
   * 
   * @return the renderer registry
   */
  Renderer[] getRenderers();

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
  Renderer getRenderer(String renderer, String method);

  /**
   * Returns the module renderer identified by <code>renderer</code> or
   * <code>null</code> if no such renderer is available. The renderer is
   * returned for the default output method.
   * 
   * @param renderer
   *          the renderer identifier
   * @return the renderer
   */
  Renderer getRenderer(String renderer);

  /**
   * Returns the renderer to the renderer pool. Returning the renderers is
   * important, since otherwise, a new renderer instance has to be instantiated
   * everytime it is needed, which is a costly operation and a waste of memory.
   * 
   * @param r
   *          the renderer
   */
  void returnRenderer(Renderer r);

  /**
   * Returns the image styles defined by this module.
   * 
   * @return the image styles
   */
  ImageStyle[] getImageStyles();

  /**
   * Returns the action identified by <code>action</code>.
   * 
   * @param action
   *          the action identifier
   * @param method
   *          the rendering method
   * @return the action object
   */
  Action getAction(String action, String method);

  /**
   * Returns the action handler to the registry.
   * 
   * @param handler
   *          the action handler
   */
  void returnAction(Action handler);

  /**
   * Returns the action registry containing all registered actions.
   * 
   * @return the module actions
   */
  Action[] getActions();

  /**
   * Returns the real path on the server for a given virtual path relative to
   * the module's root directory.
   * 
   * @param path
   *          the virtual (module-relative) path
   * @return the real (physical) path on the server
   */
  String getPhysicalPath(String path);

  /**
   * Returns the virtual path on the server relative to the web application.
   * Using this path e. g. for a renderer <code>jsp/myjsp.jsp</code> will
   * produce </code>/sites/mysite/modules/mymodule/jsp/myjsp.jsp</code>.
   * 
   * @param path
   *          the virtual path relative to the site
   * @param webapp
   *          <code>true</code> to preprend the webapp url
   * @return the virtual work path relative to the webapp
   */
  String getVirtualPath(String path, boolean webapp);

  /**
   * Returns the module title in the given language or, if it doesn't exist in
   * that language, in the site default language.
   * 
   * @param language
   *          the language
   * @return the title in the given language
   */
  String getTitle(Language language);

}