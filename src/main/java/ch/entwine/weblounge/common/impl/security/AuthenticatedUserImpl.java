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

package ch.entwine.weblounge.common.impl.security;

import ch.entwine.weblounge.common.security.AuthenticatedUser;
import ch.entwine.weblounge.common.security.User;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * This class represents the authentication part of the implementation of a
 * weblounge user by providing support for the login context, the credentials
 * and the logout operation.
 */
public class AuthenticatedUserImpl extends UserImpl implements AuthenticatedUser {

  /** The login context */
  protected LoginContext loginContext = null;

  /** Authority identity */
  protected String authorityId = null;

  /**
   * Creates a new abstract authenticated user.
   * 
   * @param login
   *          the username
   * @param realm
   *          the login domain
   */
  public AuthenticatedUserImpl(String login, String realm) {
    super(login, realm);
    authorityId = realm + ":" + login;
  }

  /**
   * Creates a new abstract authenticated user without any reference to a site.
   * The login context has to be set using
   * {@link #setLoginContext(LoginContext)}.
   * 
   * @param login
   *          the login
   */
  public AuthenticatedUserImpl(String login) {
    this(login, null);
    authorityId = "*:" + login;
  }

  /**
   * Creates a user from an existing user. This constructor is intended to
   * transform special users such as the site administrator to regular ones.
   * 
   * @param user
   *          the user
   */
  public AuthenticatedUserImpl(User user) {
    this(user.getLogin(), user.getRealm());
    setName(user.getName());
  }

  /**
   * Sets the login context which is later used to perform a clean logout
   * operation.
   * 
   * @param context
   *          the login context
   */
  public void setLoginContext(LoginContext context) {
    loginContext = context;
  }

  /**
   * Returns the user's login context. This method returns <code>null</code> if
   * the user is not authenticated.
   * 
   * @return the login context
   */
  public LoginContext getLoginContext() {
    return loginContext;
  }

  /**
   * This method is called by the authentication service when the user is logged
   * out of the site.
   * 
   * @throws LoginException
   *           if logout fails for some reason
   */
  public void logout() throws LoginException {
    if (loginContext != null) {
      loginContext.logout();
      loginContext = null;
    }
  }

  /**
   * Sets the login.
   * 
   * @param login
   *          the login
   */
  public void setLogin(String login) {
    this.login = login;
  }

  /**
   * Returns the login name of this user.
   * 
   * @return the username
   */
  public final String getLogin() {
    return login;
  }

  /**
   * Returns the authorization type, which is equal to
   * <code>ch.entwine.weblounge.api.staff.User</code>.
   * 
   * @return the authorization type
   */
  public String getAuthorityType() {
    return User.class.getName();
  }

  /**
   * Returns the authorization identifier, which is equal to the users's login.
   * 
   * @return the authorization identifier
   */
  public String getAuthorityId() {
    return authorityId;
  }

  /**
   * Returns a hash code for this User. The hash code is calculated on the login
   * name, which should be unique per site.
   * 
   * @return the users hash code
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return (login != null) ? login.hashCode() : super.hashCode();
  }

  /**
   * Returns <code>true</code> if the given object is a <code>User</code> and
   * has the same login than this User.
   * 
   * @param obj
   *          the object to test for equality
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      User u = (User) obj;
      if (!login.equals(u.getLogin()))
        return false;
      if (!realm.equals(u.getRealm()))
        return false;
      return true;
    }
    return super.equals(obj);
  }

  /**
   * Returns a String representation of this user object, which is equal to the
   * persons login.
   * 
   * @return the login
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return (login != null) ? login : super.toString();
  }

}