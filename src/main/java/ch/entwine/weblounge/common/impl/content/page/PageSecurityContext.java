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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.impl.security.SecurityContextImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.security.SystemPermission;


/**
 * Specialized security context for a page. This implementation adds the proper
 * name and default values.
 */
public class PageSecurityContext extends SecurityContextImpl {

  /**
   * Creates a new security context for a page.
   */
  public PageSecurityContext() {
    addDefaultValues();
  }

  /**
   * Adds the default authorities to their respective permissions.
   */
  private void addDefaultValues() {
    allowDefault(SystemPermission.READ, SystemRole.GUEST);
    allowDefault(SystemPermission.TRANSLATE, SystemRole.TRANSLATOR);
    allowDefault(SystemPermission.WRITE, SystemRole.EDITOR);
    allowDefault(SystemPermission.MANAGE, SystemRole.EDITOR);
    allowDefault(SystemPermission.PUBLISH, SystemRole.PUBLISHER);
  }

}