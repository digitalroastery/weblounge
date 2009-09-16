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

import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.User;

public final class WebloungeAdmin extends AuthenticatedUserImpl {

  /** Teh singleton instance */
  private static WebloungeAdmin instance = null;

  /**
   * Creates a new weblounge administrator. The default login name is
   * <code>weblounge</code>, as is the password.
   * <p>
   * Use {@link #setLogin(String)} and {@link #setPassword(String)} to set them
   * according to your needs.
   */
  private WebloungeAdmin(String login) {
    super(login);
  }

  /**
   * Initializes the administrator user.
   * 
   * @param login
   *          the login name
   * @param password
   *          the password
   * @param email
   *          the email address
   */
  public static void init(String login, String password, String email) {
    instance = new WebloungeAdmin(login);
    instance.password = password;
    instance.email = email;
  }

  /**
   * Returns the singleton instance of this class. If the user has not yet been
   * initialized, a <code>IllegalStateException</code> will be thrown.
   * 
   * @return the weblounge administrator user
   * @throws IllegalStateException
   *           if the user has not yet been initialized
   */
  public static WebloungeAdmin getInstance() throws IllegalStateException {
    if (instance != null) {
      return instance;
    }
    throw new IllegalStateException("The administrator user has not yet been initialized!");
  }

  /**
   * Sets the login name for the weblounge system administator user. The login
   * name will be set by the system configurator and is read from
   * <code>/WEB-INF/conf/weblounge.xml</code>.
   * 
   * <b>Note:</b> You are only allowed to set the login name once! Any
   * subsequent attempts to set the login name will result in an
   * <code>IllegalStateException</code>.
   * 
   * @param login
   *          the login name
   */
  public void setLogin(String login) throws IllegalStateException {
    throw new IllegalStateException("The administrator login name must be changed!");
  }

  /**
   * Sets the password for the weblounge system administator user. It will be
   * set by the system configurator and is read from
   * <code>/WEB-INF/conf/weblounge.xml</code>.
   * 
   * <b>Note:</b> You are only allowed to set the login name once! Any
   * subsequent attempts to set the login name will result in an
   * <code>IllegalStateException</code>.
   * 
   * @param password
   *          the password
   */
  public void setPassword(String password) throws IllegalStateException {
    throw new IllegalStateException("The administrator password must be changed!");
  }

  /**
   * Returns the login name of the weblounge system administrator user.
   * 
   * @return the admin user's login
   */
  public String getPassword() {
    return password != null ? password : "";
  }

  /**
   * Sets the email address for the weblounge system administator user. It will
   * be set by the system configurator and is read from
   * <code>/WEB-INF/conf/weblounge.xml</code>.
   * 
   * <b>Note:</b> You are only allowed to set the email address once! Any
   * subsequent attempts to set it will result in an
   * <code>IllegalStateException</code>.
   * 
   * @param email
   *          the email address
   */
  public void setEmail(String email) throws IllegalStateException {
    throw new IllegalStateException("The administrator email address must not be changed!");
  }

  /**
   * Returns the lastname.
   * 
   * @return the lastname
   */
  public String getName() {
    if (firstName == null && lastName == null) {
      return "Weblounge System Administrator";
    }
    return super.getName();
  }

  /**
   * Returns <code>true</code> if <code>authority</code> represents the same
   * user.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#equals(ch.o2it.weblounge.common.security.Authority)
   */
  public boolean equals(Authority authority) {
    if (authority != null && authority instanceof User) {
      return this == authority;
    }
    return false;
  }

}