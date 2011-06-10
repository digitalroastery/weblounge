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

import static ch.entwine.weblounge.common.security.SecurityConstants.DEFAULT_ORGANIZATION_ID;

import ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SiteDirectory;
import ch.entwine.weblounge.common.security.User;

import org.osgi.service.component.ComponentContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory user directory containing the users and roles used by the
 * system.
 */
public class InMemoryUserAndRoleProvider implements SiteDirectory {

  /** The known roles */
  private static final Set<Role> SYSTEM_ROLES = new HashSet<Role>(4);

  /** Spring oauth role */
  private static final Role DOMAIN_ADMIN_ROLE = new RoleImpl("system", "ROLE_ADMIN");

  /** Spring admin role */
  private static final Role SITE_ADMIN_ROLE = new RoleImpl("system", "ROLE_SITE_ADMIN");

  /** Spring user role */
  private static final Role USER_ROLE = new RoleImpl("system", "ROLE_USER");

  /** Spring oauth role */
  private static final Role OAUTH_USER_ROLE = new RoleImpl("system", "ROLE_OAUTH_USER");

  static {
    SYSTEM_ROLES.add(DOMAIN_ADMIN_ROLE);
    SYSTEM_ROLES.add(SITE_ADMIN_ROLE);
    SYSTEM_ROLES.add(USER_ROLE);
    SYSTEM_ROLES.add(OAUTH_USER_ROLE);
  }

  /** Well-known accounts */
  protected Map<String, User> internalAccounts = null;

  /**
   * Callback to activate the component.
   * 
   * @param cc
   *          the declarative services component context
   */
  void activate(ComponentContext cc) {
    internalAccounts = new HashMap<String, User>();

    // Admin
    String adminUsername = cc.getBundleContext().getProperty("org.opencastproject.security.demo.admin.user");
    String adminUserPass = cc.getBundleContext().getProperty("org.opencastproject.security.demo.admin.pass");
    User administrator = new AuthenticatedUserImpl(adminUsername);
    internalAccounts.put(adminUsername, administrator);
    administrator.addPrivateCredentials(adminUserPass);
    for (Role role : SYSTEM_ROLES) {
      administrator.addPublicCredentials(role);
    }

    // Digest user
    String digestUsername = cc.getBundleContext().getProperty("org.opencastproject.security.digest.user");
    String digestUserPass = cc.getBundleContext().getProperty("org.opencastproject.security.digest.pass");
    User systemAccount = new AuthenticatedUserImpl(digestUsername);
    internalAccounts.put(digestUsername, systemAccount);
    systemAccount.addPrivateCredentials(digestUserPass);
    for (Role role : SYSTEM_ROLES) {
      systemAccount.addPublicCredentials(role);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.security.api.RoleProvider#getRoles()
   */
  public Role[] getRoles() {
    return SYSTEM_ROLES.toArray(new Role[SYSTEM_ROLES.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.SiteDirectory.security.api.UserProvider#loadUser(java.lang.String)
   */
  public User loadUser(String userName) {
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
   * @see org.SiteDirectory.security.api.UserProvider#getSite()
   */
  public String getSite() {
    return DEFAULT_ORGANIZATION_ID;
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
