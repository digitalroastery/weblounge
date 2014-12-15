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

import ch.entwine.weblounge.common.impl.security.SecurablePermission;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Securable;
import ch.entwine.weblounge.common.security.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Permission;

/**
 * A security manager that can handle access to Weblounge resources.
 */
public class WebloungeSecurityManager extends NullSecurityManager {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeSecurityManager.class);

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
    if (permission instanceof SecurablePermission) {
      checkResourcePermission((SecurablePermission) permission);
    } else if (deleageSecurityManager != null) {
      deleageSecurityManager.checkPermission(permission);
    }
  }

  /**
   * Checks access to a Weblounge resource by the current user.
   * 
   * @param permission
   *          the resource permission
   * @throws SecurityException
   *           if access to the resource is denied
   */
  private void checkResourcePermission(SecurablePermission permission)
      throws SecurityException {
    User user = SecurityUtils.getUser();
    SecurablePermission pm = (SecurablePermission) permission;
    Action action = pm.getAction();
    Securable resource = pm.getSecurable();

    // Check the user itself
    if (SecurityUtils.checkAuthorization(resource, action, user)) {
      logger.debug("Access of type '{}' on {} granted to {}", new Object[] {
          action,
          resource,
          user });
      return;
    }

    // Check if the current user holds a credential that allows access to
    // the object in question as defined by the permission
    for (Object o : user.getPublicCredentials(Role.class)) {
      if (SecurityUtils.checkAuthorization(resource, action, (Role) o)) {
        logger.debug("Access of type '{}' on {} granted to {} due to role '{}'", new Object[] {
            action,
            resource,
            user,
            o });
        return;
      }
    }

    // If the above loop was exited other than
    logger.debug("Access of type '{}' on {} denied to {}", new Object[] {
        action,
        resource,
        user });
    throw new SecurityException("Access of type " + action + " denied to " + resource);
  }

}
