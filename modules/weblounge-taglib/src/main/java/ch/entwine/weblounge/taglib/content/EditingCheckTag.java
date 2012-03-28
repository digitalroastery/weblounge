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

package ch.entwine.weblounge.taglib.content;

import ch.entwine.weblounge.common.editor.EditingState;
import ch.entwine.weblounge.taglib.WebloungeTag;

import javax.servlet.http.Cookie;

/**
 * Tag to provide support for &lt;ifediting&gt; and &lt;ifnotediting&gt; tag
 * implementations.
 */
public abstract class EditingCheckTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -2273594543408646163L;

  /**
   * Returns <code>true</code> if the page is currently being edited.
   * 
   * @return <code>true</code> if the page is being edited
   */
  protected boolean isEditing() {
    // Check if ?edit parameter is present
    if (request.getParameter(EditingState.WORKBENCH_PARAM) != null)
      return true;

    // Check if editing cookie is present
    if (request.getCookies() == null)
      return false;
    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(EditingState.STATE_COOKIE) && "true".equals(cookie.getValue())) {
        return true;
      }
    }
    return false;
  }

}