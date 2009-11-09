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

package ch.o2it.weblounge.common.impl.user;

import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.common.user.WebloungeAdmin;

/**
 * TODO: Comment WebloungeAdminImpl
 */
public final class WebloungeAdminImpl extends WebloungeUserImpl implements WebloungeAdmin {

  /** The singleton instance */
  private static WebloungeAdminImpl instance = null;

  /**
   * Creates a new weblounge administrator.
   * <p>
   * Note that only <i>one</i> weblounge administrator may be created. Every
   * subsequent call to this constructor will result in an
   * {@link IllegalStateException}. Use {@link #getInstance()} to get hold of
   * the system administrator user.
   * <p>
   * Use {@link #setLogin(String)} and {@link #setPassword(String)} to set them
   * according to your needs.
   */
  public WebloungeAdminImpl(String login) {
    super(login, User.SystemRealm, null);
    assignRole(SystemRole.SYSTEMADMIN);
    setName("Weblounge Administrator");
    if (instance != null)
      throw new IllegalStateException("Please use getInstance() to get hold of the system admin user");
    instance = this;
  }

  /**
   * Returns the singleton instance of this class. If the user has not yet been
   * initialized, a <code>IllegalStateException</code> will be thrown.
   * 
   * @return the weblounge administrator user
   * @throws IllegalStateException
   *           if the user has not yet been initialized
   */
  public static WebloungeAdminImpl getInstance() throws IllegalStateException {
    if (instance != null) {
      return instance;
    }
    throw new IllegalStateException("The administrator user has not yet been initialized!");
  }

  /**
   * Returns <code>true</code> if <code>authority</code> represents the same
   * user.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#equals(ch.o2it.weblounge.common.security.Authority)
   */
  public boolean equals(Authority authority) {
    return authority instanceof WebloungeAdmin;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.user.UserImpl#setRealm(java.lang.String)
   */
  @Override
  public void setRealm(String realm) {
    throw new UnsupportedOperationException("The admin user realm cannot be changed");
  }

}