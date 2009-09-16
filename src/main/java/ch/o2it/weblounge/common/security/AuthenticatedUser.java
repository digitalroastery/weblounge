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

import java.security.Principal;
import java.util.Set;

/**
 * Interface defining the fields and methods for an authenticated user.
 */
public interface AuthenticatedUser extends User, Principal {

  /** Identifies a plain password */
  int PASSWORD_TYPE_PLAIN = 0;

  /** Identifies a hashed password */
  int PASSWORD_TYPE_MD5 = 5;

  /**
   * Sets the login context which is later used to perform a clean logout
   * operation.
   * 
   * @param context
   *          the login context
   */
  void setLoginContext(LoginContext context);

  /**
   * Returns the password that was used to login.
   * 
   * @return the password
   */
  String getPassword();

  /**
   * Returns the password type, which is either {@link #PASSWORD_TYPE_PLAIN} or
   * {@link #PASSWORD_TYPE_MD5}.
   * 
   * @return the password type
   */
  int getPasswordType();

  /**
   * Checks the password for equality.
   * 
   * @param password
   *          the password to check
   * @return <code>true</code> if the password matches
   */
  boolean checkPassword(String password);

  /**
   * Returns <code>true</code> if the user is currently logged in. Note that
   * this is always <code>false</code> for the guest user.
   * 
   * @return <code>true</code> if the user is logged in
   */
  boolean isAuthenticated();

  /**
   * Adds the private credential to this user. A public credential would be for
   * example the public key of an rsa keypair.
   * 
   * @param credential
   *          the public credential
   */
  void addPublicCredential(Object credential);

  /**
   * Returns the set of all public credentials of this user.
   * 
   * @return the public credentials
   */
  Set<Object> getPublicCredentials();

  /**
   * Returns the user's set of all public credentials of the given type.
   * 
   * @return the public credentials
   */
  Set<Object> getPublicCredentials(Class<?> type);

  /**
   * Adds the private credential to this user. A private credential would be for
   * example the private key of an rsa keypair.
   * 
   * @param credential
   *          the private credential
   */
  void addPrivateCredential(Object credential);

  /**
   * Returns the set of all private credentials of this user.
   * 
   * @return the private credentials
   */
  Set<Object> getPrivateCredentials();

  /**
   * Returns the user's set of all private credentials of the given type.
   * 
   * @return the private credentials
   */
  Set<Object> getPrivateCredentials(Class<?> type);

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
   */
  void logout();

}