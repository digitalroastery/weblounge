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
package ch.entwine.weblounge.common.impl.security;

import static ch.entwine.weblounge.common.security.SystemAction.READ;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.PermissionException;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Securable;
import ch.entwine.weblounge.common.security.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A security manager that can handle access to Weblounge resources.
 */
public class WebloungePermissionUtils {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WebloungePermissionUtils.class);

  /**
   * Checks read access to a Weblounge resource by the given user.
   *
   * @param user
   *          the user whose permissions need to be checked
   * @param resource
   *          the resource to check
   * @throws PermissionException
   *           if access to the resource is denied
   */
  public static void checkResourceReadPermission(final User user, final Resource<?> resource) throws PermissionException {
    checkResourcePermission(user, new SecurablePermission(resource, READ));
  }

  /**
   * Checks access to a Weblounge resource by the given user.
   *
   * @param user
   *          the user whose permissions need to be checked
   * @param permission
   *          the resource permission
   * @throws PermissionException
   *           if access to the resource is denied
   */
  public static void checkResourcePermission(final User user, final SecurablePermission permission)
      throws PermissionException {
    Action action = permission.getAction();
    Securable resource = permission.getSecurable();

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
    throw new PermissionException(user, action, permission.getSecurable());
  }
}
