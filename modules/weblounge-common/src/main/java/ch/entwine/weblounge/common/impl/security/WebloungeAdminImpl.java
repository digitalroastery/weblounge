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

import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Security;

/**
 * This class implements the system administrator for weblounge.
 */
public final class WebloungeAdminImpl extends UserImpl {

  /**
   * Creates a new weblounge administrator.
   * <p>
   * Use {@link #setLogin(String)} and {@link #setPassword(String)} to set them
   * according to your needs.
   */
  public WebloungeAdminImpl(String login) {
    super(login, Security.SYSTEM_CONTEXT);
    addPublicCredentials(SystemRole.SYSTEMADMIN);
    setName(Security.ADMIN_NAME);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.impl.security.UserImpl#setRealm(java.lang.String)
   */
  @Override
  public void setRealm(String realm) {
    throw new UnsupportedOperationException("The admin user realm cannot be changed");
  }

  @Override
  public boolean implies(Authority authority) {
    // The system administrator should have all authorities by default
    return true;
  }

}
