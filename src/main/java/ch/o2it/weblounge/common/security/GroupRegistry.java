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
 * This registry keeps track of the registered groups per site.
 */
public interface GroupRegistry {

  /** The registry identifier */
  public static final String ID = "groups";

  /**
   * Adds a group this registry.
   * 
   * @param group
   *          the group to add
   */
  void addGroup(Group group);

  /**
   * Adds a group this registry.
   * 
   * @param group
   *          the group to add
   */
  void removeGroup(Group group);

  /**
   * Returns the specified group or <code>null</code> if no such group is part
   * of the registry.
   * 
   * @param context
   *          the group context
   * @param identifier
   *          the group identifier
   * @return the group
   */
  Group getGroup(String context, String identifier);

}