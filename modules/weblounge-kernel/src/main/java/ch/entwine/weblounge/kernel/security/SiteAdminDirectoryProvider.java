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

import ch.entwine.weblounge.common.impl.security.PasswordEncoder;
import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Security;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.security.WebloungeUser;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This directory provider implements login of the site administrator.
 */
public class SiteAdminDirectoryProvider implements DirectoryProvider {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SiteAdminDirectoryProvider.class);

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getRoles()
   */
  public Role[] getRoles() {
    return SystemRole.SITEADMIN.getClosure();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#loadUser(java.lang.String,
   *      Site)
   */
  public User loadUser(String userName, Site site) {
    if (userName == null)
      throw new IllegalArgumentException("Username cannot be null");
    if (site == null)
      return null;

    WebloungeUser siteAdmin = site.getAdministrator();

    if (siteAdmin == null || !userName.equals(siteAdmin.getLogin()))
      return null;

    // Check for the existence of a digest password
    boolean hasDigestPassword = false;
    Password plainPassword = null;
    for (Object o : siteAdmin.getPrivateCredentials(Password.class)) {
      Password p = (Password) o;
      switch (p.getDigestType()) {
        case md5:
          hasDigestPassword = true;
          break;
        case plain:
          plainPassword = p;
          break;
        default:
          break;
      }

      // If there is no digest password, create one
      if (!hasDigestPassword && plainPassword != null) {
        String digestPassword = PasswordEncoder.encode(plainPassword.getPassword());
        siteAdmin.addPrivateCredentials(new PasswordImpl(digestPassword, DigestType.md5));
        logger.debug("Creating digest password for site admin '{}@{}'", siteAdmin.getLogin(), site.getIdentifier());
      }

    }

    return siteAdmin;
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
