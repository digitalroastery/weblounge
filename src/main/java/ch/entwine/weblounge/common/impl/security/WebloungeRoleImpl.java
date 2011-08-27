/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.security;

import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Security;

/**
 * A role inside the weblounge context.
 */
public class WebloungeRoleImpl extends RoleImpl {

  /**
   * Creates a new role with the given identifier.
   * 
   * @param roleId
   */
  public WebloungeRoleImpl(String roleId) {
    super(Security.SYSTEM_CONTEXT, roleId);
  }

  /**
   * Creates a new role which is extending <code>ancestor</code> role with the
   * given identifier.
   * 
   * @param roleId
   *          the role identifier
   * @param ancestor
   *          the ancestor role
   */
  public WebloungeRoleImpl(String roleId, Role ancestor) {
    super(Security.SYSTEM_CONTEXT, roleId, ancestor);
  }

}
