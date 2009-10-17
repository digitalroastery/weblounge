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
 * The <code>Secured</code> interface defines the required methods for a secured
 * object.
 */
public interface Securable {

  /**
   * Returns the owner of this object.
   * 
   * @return the owner
   */
  User getOwner();

  /**
   * Checks whether the authorization satisfy the constraints of this context on
   * the given permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authority
   *          the object used to obtain the permission
   * @return <code>true</code> if the authorization is sufficient
   */
  boolean check(Permission permission, Authority authority);

  /**
   * Returns <code>true</code> if the authorization <code>authorization</code>
   * is sufficient to act on the secured object in a way that requires the given
   * {@link PermissionSet} <code>p</code>.
   * 
   * @param permissions
   *          the required set of permissions
   * @param authority
   *          the object claiming the permissions
   * @return <code>true</code> if the object may obtain the permissions
   */
  boolean check(PermissionSet permissions, Authority authority);

  /**
   * Checks whether at least one of the given authorities pass with respect to
   * the given permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authorities
   *          the objects claiming the permission
   * @return <code>true</code> if all authorities pass
   */
  boolean checkOne(Permission permission, Authority[] authorities);

  /**
   * Checks whether all of the given authorities pass with respect to the given
   * permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authorities
   *          the objects claiming the permission
   * @return <code>true</code> if all authorities pass
   */
  boolean checkAll(Permission permission, Authority[] authorities);

  /**
   * Returns the permissions that may be acquired on this object.
   * 
   * @return the available permissions
   */
  Permission[] permissions();

  /**
   * Adds <code>listener</code> to the list of security listeners that will be
   * notified in case of ownership or permission changes.
   * 
   * @param listener
   *          the new security listener
   */
  void addSecurityListener(SecurityListener listener);

  /**
   * Removes <code>listener</code> from the list of security listeners.
   * 
   * @param listener
   *          the security listener to be removed
   */
  void removeSecurityListener(SecurityListener listener);

}