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

package ch.o2it.weblounge.common.content;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import java.net.URL;

/**
 * A <code>Renderer</code> is an object that is capable of rendering content
 * into a desired output format, usually <code>HTML</code>.
 */
public interface Renderer extends Composeable {

  /** The default valid time for a renderer */
  long DEFAULT_VALID_TIME = Times.MS_PER_WEEK;

  /** The default recheck time for a renderer */
  long DEFAULT_RECHECK_TIME = Times.MS_PER_DAY;

  /** Enumeration of well-known renderer types */
  public enum RendererType {
    Page, Search, Feed
  }

  /**
   * Adds a flavor to the list of supported flavors.
   * 
   * @param flavor
   *          the flavor to add
   */
  void addFlavor(RequestFlavor flavor);

  /**
   * Adds a flavor to the list of supported flavors.
   * 
   * @param flavor
   *          the flavor to add
   */
  void removeFlavor(RequestFlavor flavor);

  /**
   * Returns the supported output flavors.
   * 
   * @return the supported flavors
   * @see #supportsFlavor(String)
   * @see RequestFlavor
   */
  RequestFlavor[] getFlavors();

  /**
   * Returns <code>true</code> if the given flavor is supported by the renderer.
   * 
   * @param flavor
   *          the flavor name
   * @return <code>true</code> if the renderer supports the rendering method
   */
  boolean supportsFlavor(RequestFlavor flavor);

  /**
   * Sets the url of the default renderer with a renderer type of
   * {@link RendererType#Page}.
   * 
   * @param renderer
   *          url to the renderer
   */
  void setRenderer(URL renderer);

  /**
   * Sets the url of the renderer that is used for output of type
   * <code>type</code>. This will usually be a file path to a java server page
   * (JSP) file or an http link to an XML style sheet (XSL).
   * <p>
   * It is up to the author of the site to define suitable renderer types and
   * call them where appropriate. The enum {@link RendererType} defines those
   * that are well-known by weblounge.
   * 
   * @param renderer
   *          url to the renderer
   * @param type
   *          the renderer output type
   */
  void addRenderer(URL renderer, String type);

  /**
   * Returns the url to the renderer that is used to render the content. This
   * implementation assumes a content type of {@link RendererType#Page}.
   * 
   * @return the renderer url
   */
  URL getRenderer();

  /**
   * Returns the url to the renderer that is used to render content into the
   * given type.
   * 
   * @return the renderer url
   */
  URL getRenderer(String type);

  /**
   * Performs the rendering by including any renderer output in the response's
   * output stream.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @throws RenderException
   *           if the renderer is not able to process the request due to
   *           resource limitations, configuration or authorization issues and
   *           the like
   */
  void render(WebloungeRequest request, WebloungeResponse response)
      throws RenderException;

  /**
   * Returns a serialized version of this renderer.
   * 
   * @return the renderer
   */
  String toXml();

}
