/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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
package ch.entwine.weblounge.kernel.security;

import java.security.Permission;

/**
 * A security manager that can handle access to Weblounge resources.
 */
public class WebloungeSecurityManager extends SecurityManager {

  /** The security manager to delegate to */
  private SecurityManager deleageSecurityManager = null;

  /**
   * Creates a new security manager that implements the security constraints
   * specific to Weblounge and otherwise delegates to the system security
   * manager.
   * 
   * @param delegate
   *          the security manager to delegate to
   */
  public WebloungeSecurityManager(SecurityManager delegate) {
    this.deleageSecurityManager = delegate;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
   */
  @Override
  public void checkPermission(Permission permission) {

    //
    // if (permission instanceof ResourcePermission) {
    // User user = SecurityUtils.getUser();
    // ResourcePermission pm = (ResourcePermission)permission;
    // ResourcePermission action = pm.getAction();
    // Page page = pm.getPage();
    // page.checkOne(pm.getAction(), user.getPrivateCredentials(Role.class));
    // } else if (permission instanceof ImagePermission) {
    //
    // }

    // Actions

    if (deleageSecurityManager != null)
      deleageSecurityManager.checkPermission(permission);

  }

}
