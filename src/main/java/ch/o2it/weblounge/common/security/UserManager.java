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
 * This manager handles user for a site.
 */
public interface UserManager {

  /**
   * Adds the listener to the list of user listeners.
   * 
   * @param listener
   *          the user listener to add
   */
  void addUserListener(UserListener listener);

  /**
   * Removes the listener from the list of user listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  void removeUserListener(UserListener listener);

  /**
   * Returns the user with the given login name or <code>null</code> if no such
   * user exists.
   * 
   * @param login
   *          the user's login name
   * @return the user
   */
  WebloungeUser getUser(String login);

  /**
   * Returns <code>true</code> if a registered user with the given login exists
   * in the database.
   * 
   * @param login
   *          the login
   * @return <code>true</code> if the user exists
   */
  boolean exists(String login);

  /**
   * Adds the given user to the user database. Make sure that the login is
   * unique over the given site.
   * 
   * @param user
   *          the user to add
   * @return <code>true</code> if the user could be added
   * @throws MalformedLoginException
   *           if the login name is malformed
   */
  boolean addUser(WebloungeUser user) throws MalformedLoginException;

  /**
   * Enables the user.
   * 
   * @param user
   *          the user to enable
   * @return <code>true</code> if the user could be enabled
   */
  boolean enableUser(WebloungeUser user);

  /**
   * Disables the user.
   * 
   * @param user
   *          the user to disable
   * @return <code>true</code> if the user could be disabled
   */
  boolean disableUser(WebloungeUser user);

  /**
   * Updates the given user in the user database.
   * 
   * @param user
   *          the user to update
   * @return the user
   */
  boolean updateUser(WebloungeUser user);

  /**
   * Removes the given user from the user database. All files belonging to this
   * user will be owned by the administrator.
   * 
   * @param user
   *          the user to be removed
   * @return the removed user
   */
  WebloungeUser removeUser(WebloungeUser user);

  /**
   * Loads the given user from the user database and returns it. If no such user
   * exists, <code>null</code> is returned.
   * 
   * @param login
   *          the user login
   * @param site
   *          the site
   * @return the user
   */
  WebloungeUser loadUser(String login);

  /**
   * Loads the given user from the user database and returns it. If no such user
   * exists, <code>null</code> is returned.
   * 
   * @param login
   *          the user login
   * @return the user
   */
  ExtendedWebloungeUser getExtendedUser(String login);

  /**
   * Loads the given user from the user database and returns it. If no such user
   * exists, <code>null</code> is returned.
   * 
   * @param login
   *          the user login
   * @return the user
   */
  WebloungeUser[] getUserList();

  /**
   * Returns <code>true</code> if the given login name matches the minimum
   * criterias for weblounge login names, such as:
   * <ul>
   * <li>The login name must start with a letter [a-z]</li>
   * <li>The login name must be at least 4 characters in length</li>
   * <li>The login name must consist of characters [a-z], [A-Z], [ . | - | _ | @
   * ] and digits</li>
   * </ul>
   * // DOCUMENT
   * 
   * @param login
   *          the login to test
   * @return <code>true</code> for valid login names
   */
  boolean checkLogin(String login);

  /**
   * Returns <code>true</code> if the specified user is currently logged in.
   * 
   * @param login
   *          the user login
   * @return <code>true</code> if the user is online
   */
  boolean isLoggedIn(String login);

  /**
   * Returns a list of currently active users.
   * 
   * @return active users
   */
  WebloungeUser[] getActiveUsers();

}