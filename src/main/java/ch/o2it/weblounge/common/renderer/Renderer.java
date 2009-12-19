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

package ch.o2it.weblounge.common.renderer;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import java.net.URL;

/**
 * A <code>Renderer</code> is an object that is capable to render given
 * pageContent into a desired output format.
 * <p>
 * The lifecycle of a <code>Renderer</code> is as follows:
 * 
 * <ul>
 * <li>The renderer is created by the <code>RendererFactory</code> using
 * reflection</li>
 * <li>After instantiation it is initialized by calling the <code>init</code>
 * method
 * <li>The rendering process is prepared by setting the pageContent and
 * rendering method in <code>configure</code></li>
 * <li>Now the rendering itself takes place in the <code>render</code> method.
 * After that, the renderer is either collected by the garbage collector or is
 * pooled and restarts a rendering cycle by a call to the <code>configure</code>
 * method</li>
 * </ul>
 * </p>
 */
public interface Renderer extends Localizable {

  /** The template used for rendering */
  String TEMPLATE = "template";

  /** Rendering method identifier specifying HTML output */
  String HTML = "html";

  /** Rendering method identifier specifying XML output */
  String XML = "xml";

  /** The default valid time for a renderer */
  long VALID_TIME_DEFAULT = Times.MS_PER_WEEK;

  /** The default recheck time for a renderer */
  long RECHECK_TIME_DEFAULT = Times.MS_PER_DAY;

  /**
   * Returns the renderer identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Sets the associated site if this is a site related renderer configuration.
   * 
   * @param site
   *          the associated site
   */
  void setSite(Site site);

  /**
   * Sets the associated module if this is a module renderer configuration.
   * 
   * @param module
   *          the associated module
   */
  void setModule(Module module);

  /**
   * Returns the associated site or <code>null</code> if no site has been set.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Returns the associated module or <code>null</code> if no module has been
   * set.
   * 
   * @return the module
   */
  Module getModule();

  /**
   * Returns the supported rendering methods. The meaning of methods is the
   * possible output format of a renderer. Therefore, the methods usually
   * include <tt>html</tt>, <tt>pdf</tt> and so on.
   * 
   * @return the supported methods
   */
  String[] methods();

  /**
   * Returns the module title in the given language or, if it doesn't exist in
   * that language, in the site default language.
   * 
   * @param language
   *          the language
   * @return the title in the given language
   */
  String getTitle(Language language);

  /**
   * Return the path leading to the renderer.
   * 
   * @return the renderer url
   */
  URL getURL();

  /**
   * Initializes the renderer using the given configuration.
   * 
   * @param configuration
   *          the renderer configuration
   */
  void init(RendererConfiguration configuration);

  /**
   * Returns <code>true</code> if this renderer is composeable.
   * 
   * @return <code>true</code> if this renderer is composeable
   */
  boolean isComposeable();

  /**
   * This method is called just before the call to
   * {@link #render(WebloungeRequest, WebloungeResponse)} is made. Here, the
   * desired rendering method is set, that is, the requested rendering result.
   * 
   * @param method
   *          the quested rendering method
   * @param data
   *          the data to render
   */
  void configure(String method, Object data);

  /**
   * Returns <code>true</code> if the given method is supported by the renderer.
   * The method is used to lookup a rendering method for a given renderer id.
   * 
   * @param method
   *          the method name
   * @return <code>true</code> if the renderer supports the rendering method
   */
  boolean provides(String method);

  /**
   * Returns <code>true</code> if this renderer has an editor defined.
   * 
   * @return <code>true</code> if there is an editor for this renderer
   */
  boolean hasEditor();

  /**
   * Performs the actual rendering.
   * 
   * @param request
   *          the request object
   * @param response
   *          the http servlet response object
   * @throws RenderException
   *           if rendering fails
   */
  void render(WebloungeRequest request, WebloungeResponse response)
      throws RenderException;

  /**
   * Performs the actual rendering by showing the editor.
   * 
   * @param request
   *          the request object
   * @param response
   *          the http servlet response object
   * @throws RenderException
   *           if rendering fails
   */
  void renderAsEditor(WebloungeRequest request, WebloungeResponse response)
      throws RenderException;

  /**
   * This method is called after the rendering request has been accomplished by
   * the renderer. Use this method to release any resources that might have been
   * acquired.
   */
  void cleanup();

  /**
   * Returns the renderer configuration as read in from the renderer
   * configuration section of the configuration file.
   * 
   * @return the renderer configuration
   */
  RendererConfiguration getConfiguration();

}