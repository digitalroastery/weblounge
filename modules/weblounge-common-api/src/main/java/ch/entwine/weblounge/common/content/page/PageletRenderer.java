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

package ch.entwine.weblounge.common.content.page;

import ch.entwine.weblounge.common.content.RenderException;
import ch.entwine.weblounge.common.content.Renderer;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.Module;

import java.net.URL;

/**
 * A <code>PageletRenderer</code> is used to render small pieces of content
 * within a <code>Page</code>.
 */
public interface PageletRenderer extends Renderer {

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
   * Defines the pagelet's preview mode.
   * 
   * @param mode
   *          the preview mode
   */
  void setPreviewMode(PagePreviewMode mode);

  /**
   * Returns the preview mode, which defines whether a pagelet will be part of a
   * page preview. If no preview mode has been set explicitly, the return value
   * will default to {@link PagePreviewMode#None}.
   * 
   * @return the preview mode
   */
  PagePreviewMode getPreviewMode();

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
