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

package ch.entwine.weblounge.kernel.security;

import static ch.entwine.weblounge.common.security.SecurityConstants.SYSTEM_ID;

import ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl;
import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory user directory containing the users and roles used by the
 * system.
 */
public class SystemDirectoryProvider implements DirectoryProvider, ManagedService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SystemDirectoryProvider.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.entwine.weblounge.systemdirectory";

  /** Configuration key for the system username */
  public static final String OPT_ADMIN_LOGIN = "systemdirectory.login";

  /** Configuration key for the system user's password */
  public static final String OPT_ADMIN_PASSWORD = "systemdirectory.password";

  /** The known roles */
  private static final Set<Role> SYSTEM_ROLES = new HashSet<Role>(4);

  /** Spring oauth role */
  private static final Role DOMAIN_ADMIN_ROLE = new RoleImpl("system", "ROLE_ADMIN");

  /** Spring admin role */
  private static final Role SITE_ADMIN_ROLE = new RoleImpl("system", "ROLE_SITE_ADMIN");

  /** Spring user role */
  private static final Role USER_ROLE = new RoleImpl("system", "ROLE_USER");

  /** Spring user role */
  private static final Role ANONYMOUS_ROLE = new RoleImpl("system", "ROLE_ANONYMOUS");

  static {
    SYSTEM_ROLES.add(DOMAIN_ADMIN_ROLE);
    SYSTEM_ROLES.add(SITE_ADMIN_ROLE);
    SYSTEM_ROLES.add(USER_ROLE);
    SYSTEM_ROLES.add(ANONYMOUS_ROLE);
  }

  /** Well-known accounts */
  protected Map<String, User> internalAccounts = new HashMap<String, User>();

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    String login = null;
    String pass = "";

    if (properties != null) {
      login = StringUtils.trimToNull((String) properties.get(OPT_ADMIN_LOGIN));
      pass = StringUtils.trimToEmpty((String) properties.get(OPT_ADMIN_PASSWORD));
    }

    if (login == null || "".equals(pass)) {
      if (internalAccounts.size() > 0)
        logger.info("Deactivating system admin account");
      internalAccounts.clear();
      return;
    }

    // Remove previous administrators
    internalAccounts.clear();

    // Register the new one
    logger.info("Activating system admin user '{}'", login);
    User administrator = new AuthenticatedUserImpl(login);
    internalAccounts.put(login, administrator);
    Password password = new PasswordImpl(StringUtils.trimToEmpty(pass), DigestType.plain);
    administrator.addPrivateCredentials(password);
    for (Role role : SYSTEM_ROLES) {
      administrator.addPublicCredentials(role);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getRoles()
   */
  public Role[] getRoles() {
    return SYSTEM_ROLES.toArray(new Role[SYSTEM_ROLES.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#loadUser(java.lang.String,
   *      Site)
   */
  public User loadUser(String userName, Site site) {
    return internalAccounts.get(userName);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getLocalRole(ch.entwine.weblounge.common.security.Role)
   */
  public Role getLocalRole(Role role) {
    if (role.equals(DOMAIN_ADMIN_ROLE)) {
      return SITE_ADMIN_ROLE;
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryProvider#getIdentifier()
   */
  public String getIdentifier() {
    return SYSTEM_ID;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getClass().getName();
  }

}
