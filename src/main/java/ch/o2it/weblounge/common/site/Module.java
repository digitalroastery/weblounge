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
import ch.o2it.weblounge.common.scheduler.Job;
import ch.o2it.weblounge.common.url.WebUrl;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;

/**
 * The module interface defines the method that may be called on weblounge
 * module objects.
 */
public interface Module extends Customizable {

  /** Module descriptor */
  public static final String CONFIG_FILE = "module.xml";

  /**
   * Sets the module identifier.
   * <p>
   * <b>Note:</b> the identifier may be used in file paths, database table names
   * and the like, so make sure it does not contain spaces or weird characters,
   * i. e. it matches this regular expression: <code>^[a-zA-Z0-9-_.]*$</code>.
   * 
   * @param identifier
   *          the module identifier
   */
  void setIdentifier(String identifier);

  /**
   * Returns the module name.
   * 
   * @return the module identifier
   */
  String getIdentifier();

  /**
   * Enables or disables the module.
   * 
   * @param enabled
   *          <code>true</code> to enable the module
   */
  void setEnabled(boolean enabled);

  /**
   * Returns <code>true</code> if the module is enabled.
   * 
   * @return <code>true</code> if the module is enabled
   */
  boolean isEnabled();

  /**
   * Initializes this module by passing it a reference to the site that it will
   * run in. Implementations should overwrite this method to do their
   * initialization work.
   * <p>
   * Note that this method will be called <i>prior</i> to a call to
   * {@link #start()}.
   * 
   * @param site
   *          the site
   * @throws ModuleException
   *           if module initialization fails
   */
  void setSite(Site site) throws ModuleException;

  /**
   * Tells this module that it has been taken off duty and will no longer be
   * part of the site. Implementations should overwrite this method to properly
   * dispose off resources they might be holding.
   * <p>
   * Note that this method will be called <i>after</i> a call to {@link #stop()}
   * if the site that the module was running in has been started at all.
   */
  void destroy();

  /**
   * Returns the associated site.
   * 
   * @return the associated site
   */
  Site getSite();

  /**
   * Returns the url that is used to link to elements inside the module, e. g.
   * scripts, style definitions or images.
   * 
   * @return the module url
   */
  WebUrl getUrl();

  /**
   * Starts the module. This includes starting the module services and sending a
   * <code>moduleStarted</code> event to registered module listeners.
   * 
   * @throws ModuleException
   *           if the module cannot be started
   */
  void start() throws ModuleException;

  /**
   * Shuts down this module. This includes stopping the module services and
   * sending a <code>moduleStopped</code> event to registered module listeners.
   * <p>
   * If the module throws a {@link ModuleException} while being stopped, the
   * system makes sure it is not started again but rather disposed, and a new
   * instance will be created afterwards.
   * 
   * @throws ModuleException
   *           if the module cannot be stopped
   */
  void stop() throws ModuleException;

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
   * Specifies whether the module can provide search results. If that is the
   * case, the module will be included when a search is performed on the site.
   * 
   * @param searchable
   *          <code>true</code> if this module can be searched
   */
  void setSearchable(boolean searchable);

  /**
   * Returns <code>true</code> if the module can be searched.
   * 
   * @return <code>true</code> if the module can be searched
   */
  boolean isSearchable();

  /**
   * Returns the result of the search query or an empty array if the search did
   * not yield any output.
   * 
   * @param query
   *          the search query
   * @return the search result
   */
  SearchResult[] search(String query);

  /**
   * Adds the renderer to the set of renderers.
   * 
   * @param renderer
   *          the renderer to add
   */
  void addRenderer(PageletRenderer renderer);

  /**
   * Removes the renderer from the set of renderers.
   * 
   * @param renderer
   *          the renderer to remove
   */
  void removeRenderer(PageletRenderer renderer);

  /**
   * Returns all of this module's renderers.
   * 
   * @return the renderers
   */
  PageletRenderer[] getRenderers();

  /**
   * Returns the renderer identified by <code>id</code> or <code>null</code> if
   * no renderer with this identifier exists.
   * 
   * @param id
   *          the renderer identifier
   * @return the renderer
   */
  PageletRenderer getRenderer(String id);

  /**
   * Adds the image style to the set of image styles.
   * 
   * @param imagestyle
   *          the image style to add
   */
  void addImageStyle(ImageStyle imagestyle);

  /**
   * Removes the image style from the set of image styles.
   * 
   * @param imagestyle
   *          the image style to remove
   */
  void removeImageStyle(ImageStyle imagestyle);

  /**
   * Returns all of the image styles defined by this module.
   * 
   * @return the image styles
   */
  ImageStyle[] getImageStyles();

  /**
   * Returns the image style identified by <code>id</code> or <code>null</code>
   * if no image style with this identifier exists.
   * 
   * @param id
   *          the style identifier
   * @return the image style
   */
  ImageStyle getImageStyle(String id);

  /**
   * Adds the action to the set of actions.
   * 
   * @param action
   *          the action to add
   */
  void addAction(Action action);

  /**
   * Removes the action from the set of actions.
   * 
   * @param action
   *          the action to remove
   */
  void removeAction(Action action);

  /**
   * Returns a list of all actions.
   * 
   * @return the module actions
   */
  Action[] getActions();

  /**
   * Returns the action identified by <code>id</code> or <code>null</code> if no
   * action with this identifier exists.
   * 
   * @param action
   *          the action identifier
   * @return the action
   */
  Action getAction(String action);

  /**
   * Adds the job to the set of jobs.
   * 
   * @param job
   *          the job to add
   */
  void addJob(Job job);

  /**
   * Removes the job from the set of jobs.
   * 
   * @param job
   *          the job to remove
   */
  void removeJob(Job job);

  /**
   * Returns all of this module's jobs.
   * 
   * @return the jobs
   */
  Job[] getJobs();

  /**
   * Returns the job identified by <code>id</code> or <code>null</code> if
   * no job with this identifier exists.
   * 
   * @param id
   *          the job identifier
   * @return the job
   */
  Job getJob(String id);
  
  /**
   * Sets the module name in the given language.
   * 
   * @param name
   *          the module name
   * @param language
   *          the language
   */
  void setName(String name, Language language);

  /**
   * Returns the module name in the given language or, if it doesn't exist, in
   * the site default language.
   * 
   * @param language
   *          the language
   * @return the name in the given language
   */
  String getName(Language language);

  /**
   * Returns an <code>XML</code> representation of the module, which will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;module id="mymodule"&gt;
   * TODO: Finish example
   * &lt;/module&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>Module</code> from the serialized output of this method.
   * 
   * @return the <code>XML</code> representation of the module
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
   */
  String toXml();

}