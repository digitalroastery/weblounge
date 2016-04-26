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

package ch.entwine.weblounge.bridge.mail;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Dictionary;

/**
 * Base implementation of an e-mail account.
 */
public class Account implements Serializable {

  /** Serial version uid */
  private static final long serialVersionUID = -4001408289343223305L;

  /** Configuration key for the e-mail hostname */
  public static final String OPT_HOST = "host";

  /** Configuration key for the e-mail account login */
  public static final String OPT_LOGIN = "login";

  /** Configuration key for the e-mail account password */
  public static final String OPT_PASSWORD = "password";

  /** The mail server */
  protected String host = null;

  /** Account name */
  protected String login = null;

  /** Account password */
  protected String password = null;

  /**
   * Creates a new account from what's found within the properties.
   * 
   * @param properties
   *          the account properties if either one of the properties
   *          <code>host</code>, <code>account</code> or <code>password</code>
   *          is <code>null</code>
   */
  Account(Dictionary<?, ?> properties) {
    this((String) properties.get(OPT_HOST), (String) properties.get(OPT_LOGIN), (String) properties.get(OPT_PASSWORD));
  }

  /**
   * Creates a new e-mail account for the given password.
   * 
   * @param host
   *          the server host name
   * @param login
   *          the account login
   * @param password
   *          the account password
   * @throws IllegalArgumentException
   *           if either one of <code>host</code>, <code>account</code> or
   *           <code>password</code> is <code>null</code>
   */
  public Account(String host, String login, String password)
      throws IllegalArgumentException {
    if (StringUtils.isBlank(host))
      throw new IllegalArgumentException("E-mail host cannot be blank");
    if (StringUtils.isBlank(login))
      throw new IllegalArgumentException("E-mail account login cannot be blank");
    if (StringUtils.isBlank(password))
      throw new IllegalArgumentException("E-mail account password cannot be blank");
    this.host = host;
    this.login = login;
    this.password = password;
  }

  /**
   * Returns the e-mail host.
   * 
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * Returns the account name.
   * 
   * @return the account name
   */
  public String getLogin() {
    return login;
  }

  /**
   * Returns the account password.
   * 
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return login + "@" + host;
  }

}
