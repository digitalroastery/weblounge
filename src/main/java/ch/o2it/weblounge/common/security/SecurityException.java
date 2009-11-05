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
 * This exception is thrown if a secured object is being accessed with
 * insufficient rights.
 */
public class SecurityException extends java.lang.SecurityException {

  /** The serial id */
  private static final long serialVersionUID = 1L;

  /** The permission that was used to access the object */
  private Permission permission_;

  /** The user accessing the secured object */
  private User user_;

  /** The secured object */
  private Securable object_;

  /**
   * Creates a new security permission that was raised for user <code>u</code>
   * trying to access <code>s</code> with a permission <code>p</code>.
   * 
   * @param s
   *          the secured object
   * @param u
   *          the user
   * @param p
   *          the permission
   */
  public SecurityException(Securable s, User u, Permission p) {
    super("Access denied for user " + u + " when trying to access " + s + " with permission " + p);
    object_ = s;
    user_ = u;
    permission_ = p;
  }

  /**
   * Creates a new security permission that was raised for user <code>u</code>
   * trying to access <code>s</code> with insufficient permissions.
   * 
   * @param s
   *          the secured object
   * @param u
   *          the user
   */
  public SecurityException(Securable s, User u) {
    super("Access denied for user " + u + " when trying to access " + s);
    object_ = s;
    user_ = u;
  }

  /**
   * Returns the object that was unsuccessfully accessed by the user.
   * 
   * @return the secured object
   */
  public Securable getSource() {
    return object_;
  }

  /**
   * Returns the user that wanted access to the secured object.
   * 
   * @return the accessing user
   */
  public User getUser() {
    return user_;
  }

  /**
   * Returns the permission that was requested but denied.
   * 
   * @return the requested permission
   */
  public Permission getPermission() {
    return permission_;
  }

}