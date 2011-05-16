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

import java.util.Set;

/**
 * Defines a user with login, realm and name.
 */
public interface User extends Cloneable {

  /** Default realm for weblounge users */
  String DefaultRealm = "weblounge";

  /** Default realm for system users */
  String SystemRealm = "system";

  /**
   * Returns the name of this user.
   * 
   * @returns the user name
   */
  String getName();

  /**
   * Returns the login name of this user.
   * 
   * @return the login
   */
  String getLogin();

  /**
   * Returns the realm where this user can be looked up.
   * 
   * @return the realm
   */
  String getRealm();

  /**
   * Adds the private credentials to this user. A private credentials would be
   * for example the private key of an RSA keypair.
   * 
   * @param credentials
   *          the private credentials
   */
  void addPrivateCredentials(Object... credentials);

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
   * Adds the private credentials to this user. A public credentials would be
   * for example the public key of an rsa keypair.
   * 
   * @param credentials
   *          the public credentials
   */
  void addPublicCredentials(Object... credentials);

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
   * Creates a clone of this user.
   * 
   * @return the cloned user
   */
  Object clone() throws CloneNotSupportedException;

  /**
   * Returns the <code>XML</code> representation of this user.
   * 
   * @return the <code>XML</code> representation
   */
  String toXml();

}