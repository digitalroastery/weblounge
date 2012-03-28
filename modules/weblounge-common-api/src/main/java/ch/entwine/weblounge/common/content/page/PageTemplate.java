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

import ch.entwine.weblounge.common.content.Renderer;
import ch.entwine.weblounge.common.site.Site;

/**
 * A <code>PageTemplate</code> is a renderer that is used to render a whole
 * page.
 */
public interface PageTemplate extends Renderer {

  /** Default name of the main composer */
  String DEFAULT_STAGE = "main";

  /**
   * Sets the associated site.
   * 
   * @param site
   *          the site
   */
  void setSite(Site site);

  /**
   * Defines the main composer for this template, which will be used when
   * calling
   * {@link ch.entwine.weblounge.common.site.Action#startStage(ch.entwine.weblounge.common.request.WebloungeRequest, ch.entwine.weblounge.common.request.WebloungeResponse)}
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
