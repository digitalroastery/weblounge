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

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import java.net.URL;

/**
 * A <code>PageletRenderer</code> is used to render small pieces of content
 * within a <code>Page</code>.
 */
public interface PageletRenderer extends Renderer {

  /**
   * Adds the link to the list of links that need to be included in the page's
   * header section.
   * 
   * @param link
   *          the link
   */
  void addLink(Include link);

  /**
   * Removes the link from the list of links that need to be included in the
   * page's header section.
   * 
   * @param link
   *          the link
   */
  void removeLink(Include link);

  /**
   * Returns the &lt;link&gt; elements that have been defined for this renderer.
   * 
   * @return the links
   */
  Include[] getLinks();

  /**
   * Adds the script to the list of scripts that need to be included in the
   * page's header section.
   * 
   * @param script
   *          the script
   */
  void addScript(ScriptInclude script);

  /**
   * Removes the script from the list of scripts that need to be included in the
   * page's header section.
   * 
   * @param script
   *          the script
   */
  void removeScript(ScriptInclude script);

  /**
   * Returns the &lt;script&gt; elements that have been defined for this
   * renderer.
   * 
   * @return the scripts
   */
  ScriptInclude[] getScripts();

  /**
   * Sets the module that defined this renderer.
   * 
   * @param module
   *          the module
   */
  void setModule(Module module);

  /**
   * Returns the module that this renderer belongs to.
   * 
   * @return the module
   */
  Module getModule();

  /**
   * Sets the url to the editor.
   * 
   * @param editor
   *          the editor
   */
  void setEditor(URL editor);

  /**
   * Returns the url to the editor or <code>null</code> if no editor is defined.
   * 
   * @return the url to the editor
   */
  URL getEditor();

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

}
