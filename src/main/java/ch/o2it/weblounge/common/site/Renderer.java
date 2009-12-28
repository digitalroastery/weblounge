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

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

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
   * Initializes the renderer using the given configuration.
   * 
   * @param configuration
   *          the renderer configuration
   */
  void init(RendererConfiguration configuration);

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
   * Returns <code>true</code> if the given flavor is supported by the renderer.
   * 
   * @param flavor
   *          the flavor name
   * @return <code>true</code> if the renderer supports the rendering method
   */
  boolean supportsFlavor(String flavor);

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
   * Returns the amount of time in milliseconds that output using this renderer
   * will be valid.
   * 
   * @return the valid time
   */
  long getValidTime();

  /**
   * Returns the amount of time in milliseconds that output using this renderer
   * is likely to still be valid. However, clients should check to make sure
   * that this actually is the case.
   * 
   * @return the recheck time
   */
  long getRecheckTime();

  /**
   * This method is called after the rendering request has been accomplished by
   * the renderer. Use this method to release any resources that might have been
   * acquired.
   */
  void cleanup();

}
