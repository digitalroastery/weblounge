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

import ch.o2it.weblounge.common.url.WebUrl;

/**
 * This interface describes the callbacks that have to be provided by an
 * implementing class to get notified about user movements.
 */
public interface UserListener {

  /**
   * This method is called if the user moves from one url to another. Note that
   * moving does not include calling actions. Only movements that are detected
   * by the <code>SimpleRequestHandler</code> are noted.
   * 
   * @param user
   *          the moving user
   * @param url
   *          the url that the user moved to
   */
  void userMoved(User user, WebUrl url);

  /**
   * This method is called if a user logs in.
   * 
   * @param user
   *          the user that logged in
   */
  void userLoggedIn(User user);

  /**
   * This method is called if a user logs out.
   * 
   * @param user
   *          the user that logged out
   */
  void userLoggedOut(User user);

}