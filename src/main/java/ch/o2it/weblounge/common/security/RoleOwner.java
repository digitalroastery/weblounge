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

package ch.o2it.weblounge.common.security;


/**
 * Interface for objects that may take roles.
 */
public interface RoleOwner {

  /**
   * Removes <code>role</code> from the list of roles assigned to this user.
   * 
   * @param role
   *          the role to remove
   */
  void assignRole(Role role);

  /**
   * Removes <code>role</code> from the list of roles assigned to this user.
   * 
   * @param role
   *          the role to remove
   */
  void unassignRole(Role role);

  /**
   * Returns <code>true</code> if this object owns the specified role.
   * 
   * @param role
   *          the role
   * @return <code>true</code> if the role is owned
   */
  boolean hasRole(Role role);

  /**
   * Returns <code>true</code> if this object owns the specified role.
   * 
   * @param context
   *          the role context
   * @param id
   *          the role identifier
   * @return <code>true</code> if the role is owned
   */
  boolean hasRole(String context, String id);

  /**
   * Returns all roles that this user may have, either directly or indirectly by
   * group memberships.
   * 
   * @return the user's roles
   */
  Role[] getRoleClosure();

  /**
   * Returns all roles that this user directly owns.
   * 
   * @return the user's roles
   */
  Role[] getRoles();

}