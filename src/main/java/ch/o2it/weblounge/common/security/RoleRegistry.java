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
 * This registry keeps track of the registered roles per site.
 */
public interface RoleRegistry {

  /** The registry identifier */
  public static final String ID = "roles";

  /**
   * Adds a role this registry.
   * 
   * @param role
   *          the role to add
   */
  void addRole(Role role);

  /**
   * Adds a role this registry.
   * 
   * @param role
   *          the role to add
   */
  void removeRole(Role role);

  /**
   * Returns the specified role or <code>null</code> if no such role is part of
   * the registry.
   * 
   * @param context
   *          the role context
   * @param identifier
   *          the role identifier
   * @return the role
   */
  Role getRole(String context, String identifier);

  /**
   * Returns the specified role or <code>null</code> if no such role is part of
   * the registry. Note that <code>id</code> must consist of a context and an
   * id, formulated as <code>context:id</code>.
   * 
   * @param id
   *          the role identification
   * @return the role
   */
  Role getRole(String id);

}