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

import ch.o2it.weblounge.common.site.Action;

/**
 * A <code>PageTemplate</code> is a renderer that is used to render a whole
 * page.
 */
public interface PageTemplate extends Renderer {

  /** Default name of the main composer */
  String DEFAULT_STAGE = "main";

  /**
   * Defines the main composer for this template, which will be used when
   * calling
   * {@link Action#startStage(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)}
   * on action handler.
   * 
   * @param stage
   *          name of the main composer
   */
  void setStage(String stage);

  /**
   * Returns the stage, which is the identifier of a composer in the template
   * that takes the main content when used by an <code>Action</code>.
   * 
   * @return name of the main composer
   * @see
   */
  String getStage();

  /**
   * Sets the default page layout for this template.
   * 
   * @param layout
   *          the default layout
   */
  void setDefaultLayout(String layout);

  /**
   * Returns the default page layout for this template or <code>null</code> if
   * no default layout has been specified.
   * 
   * @return the default page layout
   */
  String getDefaultLayout();

  /**
   * Makes this the default template.
   * 
   * @param v
   *          <code>true</code> to make this the default template
   */
  void setDefault(boolean v);

  /**
   * Returns <code>true</code> if this is the default template.
   * 
   * @return <code>true</code> if this is the default template
   */
  boolean isDefault();

}
