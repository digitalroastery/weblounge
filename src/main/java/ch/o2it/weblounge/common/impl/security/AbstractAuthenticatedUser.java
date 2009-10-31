/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.security.AuthenticatedUser;
import ch.o2it.weblounge.common.security.LoginContext;
import ch.o2it.weblounge.common.site.Site;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the authentication part of the implementation of a
 * weblounge user by providing support for the login context, the credentials
 * and the logout operation.
 */
public abstract class AbstractAuthenticatedUser extends AbstractUser implements AuthenticatedUser {

  /** The login context */
  protected LoginContext loginContext = null;

  /** the password */
  protected byte[] password = null;

  /** the password type, either plain or md5 */
  protected int passwordType = PASSWORD_TYPE_PLAIN;

  /** the public credential set */
  protected Set<Object> publicCredentials = null;

  /** the private credential set */
  protected Set<Object> privateCredentials = null;

  /**
   * Creates a new abstract authenticated user.
   * 
   * @param login
   *          the username
   * @param context
   *          the login context
   * @param site
   *          the site
   */
  public AbstractAuthenticatedUser(String login, LoginContext context, Site site) {
    super(login, site);
    loginContext = context;
  }

  /**
   * Creates a new abstract authenticated user. The login context has to be set
   * using {@link #setLoginContext(LoginContext)}.
   * 
   * @param login
   *          the username
   * @param site
   *          the site
   */
  public AbstractAuthenticatedUser(String login, Site site) {
    this(login, null, site);
  }

  /**
   * Creates a new abstract authenticated user whithout any reference to a site.
   * The login context has to be set using
   * {@link #setLoginContext(LoginContext)}.
   * 
   * @param login
   *          the login
   */
  public AbstractAuthenticatedUser(String login) {
    this(login, null, null);
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
   * Sets the user's password. The argument <code>type</code> specifies the
   * password encoding, which is one of
   * <ul>
   * <li>{@link AuthenticatedUserImpl#PASSWORD_TYPE_PLAIN}</li>
   * <li>{@link AuthenticatedUserImpl#PASSWORD_TYPE_MD5}</li>
   * </ul>
   * 
   * @param password
   *          the password
   * @param type
   *          the password type
   */
  public void setPassword(byte[] password, int type) {
    passwordType = type;
    this.password = password;
  }

  /**
   * Sets the user's password. The password will be md5 encoded.
   * 
   * @param password
   *          the password
   */
  public void setPassword(byte[] password) {
    setPassword(DigestUtils.md5(password), PASSWORD_TYPE_MD5);
  }

  /**
   * Returns the password that was used to login.
   * 
   * @return the password
   */
  public byte[] getPassword() {
    return password;
  }

  /**
   * Checks <code>password</code> for equality with the users's password.
   * <p>
   * <b>Note:</b> This method returns <code>true</code> if <code>password</code>
   * is <code>null</code>.
   * 
   * @param password
   *          the password to check
   * @return <code>true</code> if the password matches
   */
  public boolean checkPassword(String password) {
    if (password == null) {
      return true;
    }
    switch (passwordType) {
    case PASSWORD_TYPE_PLAIN:
      return this.password.equals(password);
    case PASSWORD_TYPE_MD5:
      return this.password.equals(DigestUtils.md5(password));
    }
    return false;
  }

  /**
   * Returns the password type, which is either {@link #PASSWORD_TYPE_PLAIN} or
   * {@link #PASSWORD_TYPE_MD5}. The default is <code>PASSWORD_TYPE_PLAIN</code>
   * .
   * 
   * @return the password type
   */
  public int getPasswordType() {
    return passwordType;
  }

  /**
   * Returns <code>true</code> if the user is authenticated. This is the case if
   * the user possesses a valid login context.
   * 
   * @see ch.o2it.weblounge.common.security.AuthenticatedUser#isAuthenticated()
   */
  public boolean isAuthenticated() {
    return loginContext != null;
  }

  /**
   * This method is called by the authentication service when the user is logged
   * out of the site.
   */
  public void logout() {
    if (loginContext != null) {
      loginContext.logout();
      loginContext = null;
    }
  }

  /**
   * Adds the private credential to this user. A public credential would be for
   * example the public key of an rsa keypair.
   * 
   * @param credential
   *          the public credential
   */
  public void addPublicCredential(Object credential) {
    if (publicCredentials == null) {
      publicCredentials = new HashSet<Object>();
    }
    publicCredentials.add(credential);
  }

  /**
   * Returns the set of all public credentials of this user.
   * 
   * @return the public credentials
   */
  public Set<Object> getPublicCredentials() {
    if (publicCredentials != null) {
      return publicCredentials;
    } else {
      return new HashSet<Object>();
    }
  }

  /**
   * Returns the user's set of all public credentials of the given type.
   * 
   * @return the public credentials
   */
  public Set<Object> getPublicCredentials(Class<?> type) {
    Set<Object> set = new HashSet<Object>();
    if (publicCredentials != null) {
      for (Object c : publicCredentials) {
        set.add(c);
      }
    }
    return set;
  }

  /**
   * Adds the private credential to this user. A private credential would be for
   * example the private key of an rsa keypair.
   * 
   * @param credential
   *          the private credential
   */
  public void addPrivateCredential(Object credential) {
    if (privateCredentials == null) {
      privateCredentials = new HashSet<Object>();
    }
    privateCredentials.add(credential);
  }

  /**
   * Returns the set of all private credentials of this user.
   * 
   * @return the private credentials
   */
  public Set<Object> getPrivateCredentials() {
    if (privateCredentials != null) {
      return privateCredentials;
    } else {
      return new HashSet<Object>();
    }
  }

  /**
   * Returns the user's set of all private credentials of the given type.
   * 
   * @return the private credentials
   */
  public Set<Object> getPrivateCredentials(Class<?> type) {
    Set<Object> set = new HashSet<Object>();
    if (privateCredentials != null) {
      for (Object c : privateCredentials) {
        set.add(c);
      }
    }
    return set;
  }

}