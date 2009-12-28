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

/**
 * A <code>PageletRenderer</code> is used to render small pieces of content
 * within a <code>Page</code>.
 */
public interface PageletRenderer extends Renderer {

  /**
   * Returns <code>true</code> if this renderer has an editor defined.
   * 
   * @return <code>true</code> if there is an editor for this renderer
   */
  boolean hasEditor();

  /**
   * Returns <code>true</code> if this renderer is composeable.
   * 
   * @return <code>true</code> if this renderer is composeable
   */
  boolean isComposeable();

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
   * Sets the associated site if this is a site related renderer configuration.
   * 
   * @param site
   *          the associated site
   */
  void setSite(Site site);

  /**
   * Returns the associated site or <code>null</code> if no site has been set.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Sets the associated module if this is a module renderer configuration.
   * 
   * @param module
   *          the associated module
   */
  void setModule(Module module);

  /**
   * Returns the associated module or <code>null</code> if no module has been
   * set.
   * 
   * @return the module
   */
  Module getModule();

}
