/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.taglib.content;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.taglib.WebloungeTag;

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
    User user = request.getUser();
    Page page = (Page) request.getAttribute(WebloungeRequest.PAGE);
    return page.getLockOwner().equals(user) && request.getVersion() == Page.WORK;
  }

}