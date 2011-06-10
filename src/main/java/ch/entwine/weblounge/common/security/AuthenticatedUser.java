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

import java.security.Principal;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Interface defining the fields and methods for an authenticated user.
 */
public interface AuthenticatedUser extends User, Principal {

  /**
   * Sets the login context which is later used to perform a clean logout
   * operation.
   * 
   * @param context
   *          the login context
   */
  void setLoginContext(LoginContext context);

  /**
   * Returns the user's login context. This method returns <code>null</code> if
   * the user is not authenticated.
   * 
   * @return the login context
   */
  LoginContext getLoginContext();

  /**
   * This method is called by the authentication service when the user is logged
   * out of the site.
   * 
   * @throws LoginException
   *           if logout fails for some reason
   */
  void logout() throws LoginException;

}