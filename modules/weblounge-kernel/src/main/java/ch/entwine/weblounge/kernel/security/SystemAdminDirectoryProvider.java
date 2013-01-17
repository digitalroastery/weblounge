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

import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.security.WebloungeUserImpl;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Security;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * An in-memory user directory containing the users and roles used by the
 * system.
 */
public class SystemAdminDirectoryProvider implements DirectoryProvider, ManagedService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SystemAdminDirectoryProvider.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.entwine.weblounge.systemdirectory";

  /** Configuration key for the system username */
  public static final String OPT_ADMIN_LOGIN = "systemdirectory.login";

  /** Configuration key for the system user's password */
  public static final String OPT_ADMIN_PASSWORD = "systemdirectory.password";

  /** Configuration key for the system user's password digest */
  public static final String OPT_ADMIN_DIGEST = "systemdirectory.password.digest";

  /** Configuration key for the system user's name */
  public static final String OPT_ADMIN_NAME = "systemdirectory.name";

  /** Configuration key for the system user's email */
  public static final String OPT_ADMIN_EMAIL = "systemdirectory.email";

  /** Well-known accounts */
  protected WebloungeUserImpl administrator = null;
  
  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    String login = null;
    String pass = "";
    String digest = null;
    String name = null;
    String email = null;

    if (properties != null) {
      login = StringUtils.trimToNull((String) properties.get(OPT_ADMIN_LOGIN));
      pass = StringUtils.trimToEmpty((String) properties.get(OPT_ADMIN_PASSWORD));
      digest = StringUtils.trimToEmpty((String) properties.get(OPT_ADMIN_DIGEST));
      name = StringUtils.trimToEmpty((String) properties.get(OPT_ADMIN_NAME));
      email = StringUtils.trimToEmpty((String) properties.get(OPT_ADMIN_EMAIL));
    }

    // If no user can be found
    if (login == null || "".equals(pass)) {
      logger.info("No system accounts have been defined");
      if (administrator != null)
        logger.info("Deactivating system admin account");
      administrator = null;
      return;
    }

    // Register the new one
    logger.info("Activating system admin user '{}'", login);
    administrator = new WebloungeUserImpl(login, Security.SYSTEM_CONTEXT);
    if (StringUtils.isNotBlank(name))
      administrator.setName(name);
    if (StringUtils.isNotBlank(email))
      administrator.setEmail(email);

    DigestType digestType = DigestType.plain;
    if (StringUtils.isNotBlank(digest)) {
      try {
        digestType = DigestType.valueOf(digest);
      } catch (IllegalArgumentException e) {
        logger.error("Digest type '{}' is unknown", digest);
        throw new ConfigurationException(OPT_ADMIN_DIGEST, digest);
      }
    }

    if (StringUtils.isNotBlank(pass)) {
      Password password = new PasswordImpl(StringUtils.trimToEmpty(pass), digestType);
      administrator.addPrivateCredentials(password);
    }

    // Add the roles
    for (Role role : SystemRole.SYSTEMADMIN.getClosure()) {
      administrator.addPublicCredentials(role);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getRoles()
   */
  public Role[] getRoles() {
    return SystemRole.SYSTEMADMIN.getClosure();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#loadUser(java.lang.String,
   *      Site)
   */
  public User loadUser(String userName, Site site) {
    if (administrator == null)
      return null;
    if (!administrator.getLogin().equals(userName))
      return null;
    return administrator;
  }

  /**
   * {@inheritDoc}
   * 
   * Since this directory does not represent a local directory but a system
   * directory already, there is no need to transform roles into local roles.
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getLocalRole(ch.entwine.weblounge.common.security.Role)
   */
  public Role getLocalRole(Role role) {
    return role;
  }

  /**
   * {@inheritDoc}
   * 
   * Every role issued by this provider already represents system roles,
   * therefore no translation is needed.
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getSystemRoles(ch.entwine.weblounge.common.security.Role)
   */
  public Role[] getSystemRoles(Role role) {
    return new Role[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryProvider#getIdentifier()
   */
  public String getIdentifier() {
    return Security.SYSTEM_CONTEXT;
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
