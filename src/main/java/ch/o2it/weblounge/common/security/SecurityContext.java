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

import ch.o2it.weblounge.common.user.User;


/**
 * This class models the security constraints that apply to an arbitrary object
 * in the system.
 */
public interface SecurityContext extends Securable, Cloneable {

  /**
   * Sets a new owner for this context.
   * 
   * @param owner
   *          the context owner
   */
  void setOwner(User owner);

  /**
   * Returns <code>true</code> if <code>user</code> owns this context.
   * 
   * @param user
   *          the user
   * @return <code>true</code> if <code>user</code> owns this context
   */
  boolean isOwnedBy(User user);

  /**
   * Adds <code>authority</code> to the authorized authorities regarding the
   * given permission.
   * 
   * @param permission
   *          the permission
   * @param authority
   *          the authority that is allowed
   */
  void allow(Permission permission, Authority authority);

  /**
   * Adds <code>authorities</code> to the authorized authorities regarding the
   * given permission.
   * 
   * @param permission
   *          the permission
   * @param authorities
   *          the authorities that are allowed
   */
  void allow(Permission permission, Authority[] authorities);

  /**
   * Adds <code>authority</code> to the denied authorities regarding the given
   * permission.
   * 
   * @param permission
   *          the permission
   * @param authority
   *          the authority to deny
   */
  void deny(Permission permission, Authority authority);

  /**
   * Adds <code>authorities</code> to the denied authorities regarding the given
   * permission.
   * 
   * @param permission
   *          the permission
   * @param authorities
   *          the authorities to deny
   */
  void deny(Permission permission, Authority[] authorities);

  /**
   * Returns all possible authorities that are required to obtain the permission
   * <code>p</code>.
   * 
   * @param p
   *          the permission
   * @return the allowed authorities
   */
  Authority[] getAllowed(Permission p);

  /**
   * Returns all possible authorities that are explicitely denied.
   * 
   * @param p
   *          the permission
   * @return the denied authorities
   */
  Authority[] getDenied(Permission p);

  /**
   * Returns an XML representation of this context.
   * 
   * @return an XML representation of this context
   */
  String toXml();

}