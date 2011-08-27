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

package ch.entwine.weblounge.common.security;

import ch.entwine.weblounge.common.site.Site;

/**
 * A directory provides user and role information. Note that more than one
 * directory can be registered in the system.
 */
public interface DirectoryService {

  /**
   * Loads a user by its login name for the given site or returns
   * <code>null</code> if this user is not known.
   * 
   * @param login
   *          the login
   * @param site
   *          the site to load the user for
   * @return the user
   */
  User loadUser(String login, Site site);

  /**
   * Return all roles.
   * 
   * @return the roles
   */
  Role[] getRoles();

  /**
   * Returns the local role name for the abstract role or <code>null</code> if
   * undefined.
   * <p>
   * This method is used to translate roles that are referred to by weblounge
   * (e. g. <code>abstract_admin</code>) to each individual site, since these
   * roles will have different names depending on the site's directory.
   * 
   * @param role
   *          the abstract role
   * @return the local role
   */
  Role getLocalRole(Role role);

  /**
   * Returns the system roles that are represented by the local role
   * <code>role</code>. This supports translation of roles defined in given
   * directory structures to Weblounge roles.
   * <p>
   * For example, a directory provider may return a user with a local role
   * <code>SPECIAL_LOCAL_ROLE</code>. This locally defined role may include the
   * right to edit content in Weblounge, so this method should return the system
   * role <code>ROLE_EDITOR</code>.
   * 
   * @param role
   *          the local role
   * @return zero, one or more system roles
   */
  Role[] getSystemRoles(Role role);

}
