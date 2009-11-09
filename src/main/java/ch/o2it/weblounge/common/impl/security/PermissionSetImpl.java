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

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to represent an arbitrary combination of permissions. It
 * is possible to have permissions that are all required and others where only
 * one of them have to match.
 * <p>
 * The permission set evaluates to <code>true</code> if the user owns all of the
 * required permissions and at least one of the optional ones.
 */
public class PermissionSetImpl implements PermissionSet {

  /** the permissions to be checked */
  private Set<Permission> oneOf_;

  /** the permissions to be checked */
  private Set<Permission> allOf_;

  /**
   * Constructor for an emtpy permission set.
   */
  public PermissionSetImpl() {
    this(null);
  }

  /**
   * Creates a set of permissions with the given permission as a starting point.
   * 
   * @param p
   *          the permission
   */
  public PermissionSetImpl(Permission p) {
    oneOf_ = new HashSet<Permission>();
    allOf_ = new HashSet<Permission>();
    if (p != null) {
      addPermission(p, MATCH_ALL);
    }
  }

  /**
   * Adds permision <code>p</code> to the set of permissions. The
   * <code>type</code> parameter specifies what kind of permission this is:
   * <ul>
   * <li>{@link #MATCH_ALL}</li>
   * <li>{@link #MATCH_SOME}</li>
   * </ul>
   * 
   * @param p
   *          the permission
   * @param type
   *          the permission type
   */
  public void addPermission(Permission p, int type) {
    switch (type) {
    case MATCH_SOME:
      oneOf_.add(p);
      break;
    default:
      allOf_.add(p);
      break;
    }
  }

  /**
   * Removes permision <code>p</code> from the set of permissions. The
   * <code>type</code> parameter specifies what kind of permission this is:
   * <ul>
   * <li>{@link #MATCH_ALL}</li>
   * <li>{@link #MATCH_SOME}</li>
   * </ul>
   * 
   * @param p
   *          the permission to remove
   * @param type
   *          the permission type
   */
  public void removePermission(Permission p, int type) {
    switch (type) {
    case MATCH_SOME:
      oneOf_.remove(p);
      break;
    default:
      allOf_.remove(p);
      break;
    }
  }

  /**
   * Returns the permissions that have to be matched exactly.
   * 
   * @return the permissions to be exactly matched
   */
  public Permission[] all() {
    Permission[] p = new Permission[allOf_.size()];
    return allOf_.toArray(p);
  }

  /**
   * Returns the permissions that need to be matched one at least.
   * 
   * @return the permissions to match one
   */
  public Permission[] some() {
    Permission[] p = new Permission[oneOf_.size()];
    return oneOf_.toArray(p);
  }

  /**
   * Clears the permissions that have been set on this object.
   */
  public void clear() {
    oneOf_.clear();
    allOf_.clear();
  }

}