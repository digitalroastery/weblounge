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

import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.DirectoryService;
import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Federated user and role providers, and exposes a spring UserDetailsService so
 * user lookups can be used by spring security.
 */
public class DirectoryServiceImpl implements DirectoryService, UserDetailsService {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(DirectoryServiceImpl.class);

  /** The list of directories */
  protected Map<String, List<DirectoryProvider>> directories = new HashMap<String, List<DirectoryProvider>>();

  /** The security service */
  protected SecurityService securityService = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getRoles()
   */
  public Role[] getRoles() throws IllegalStateException {
    Site site = securityService.getSite();

    if (site == null)
      throw new IllegalStateException("No site set in security context");

    List<DirectoryProvider> siteDirectories = directories.get(site.getIdentifier());
    if (siteDirectories == null) {
      logger.debug("No directories found for '{}'", site.getIdentifier());
      return new Role[] {};
    }

    // Collect roles from all directories registered for this site
    SortedSet<Role> roles = new TreeSet<Role>();
    for (DirectoryProvider directory : siteDirectories) {
      for (Role role : directory.getRoles()) {
        roles.add(role);
      }
    }
    return roles.toArray(new Role[roles.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#loadUser(java.lang.String)
   */
  public User loadUser(String login) throws IllegalStateException {
    Site site = securityService.getSite();
    if (site == null)
      throw new IllegalStateException("No site set in security context");

    List<DirectoryProvider> siteDirectories = directories.get(site.getIdentifier());
    if (siteDirectories == null) {
      logger.debug("No directories found for '{}'", site.getIdentifier());
      return null;
    }

    // Collect all of the roles from each of the directories for this user
    User user = null;
    for (DirectoryProvider directory : siteDirectories) {
      User u = directory.loadUser(login);
      if (u == null) {
        continue;
      } else if (user == null) {
        user = u;
      } else {
        for (Object c : u.getPublicCredentials()) {
          user.addPublicCredentials(c);
        }
        for (Object c : u.getPrivateCredentials()) {
          user.addPrivateCredentials(c);
        }
      }
    }
    return user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
   */
  public UserDetails loadUserByUsername(String name)
      throws UsernameNotFoundException,
      org.springframework.dao.DataAccessException {
    User user = loadUser(name);
    if (user == null) {
      throw new UsernameNotFoundException(name);
    } else {
      Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
      for (Object role : user.getPublicCredentials(Role.class)) {
        authorities.add(new GrantedAuthorityImpl(((Role) role).getIdentifier()));
      }
      Set<Object> passwords = user.getPrivateCredentials(Password.class);
      String password = null;
      for (Object o : passwords) {
        Password p = (Password) o;
        if (DigestType.plain.equals(p.getDigestType())) {
          password = p.getPassword();
          break;
        }
      }
      return new org.springframework.security.core.userdetails.User(user.getLogin(), password, true, true, true, true, authorities);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getLocalRole(ch.entwine.weblounge.common.security.Role)
   */
  public Role getLocalRole(Role role) {
    Site site = securityService.getSite();

    if (site == null)
      throw new IllegalStateException("No site set in security context");

    List<DirectoryProvider> siteDirectories = directories.get(site.getIdentifier());
    if (siteDirectories == null) {
      logger.debug("No directories registered for site '{}'", site.getIdentifier());
      return null;
    }

    for (DirectoryProvider directory : siteDirectories) {
      Role localRole = directory.getLocalRole(role);
      if (localRole != null) {
        return localRole;
      }
    }

    return null;
  }

  /**
   * Sets the security service.
   * 
   * @param securityService
   *          the security service
   */
  void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }

  /**
   * Adds the directory to the list of site directories.
   * 
   * @param directory
   *          the site directory
   */
  void addDirectoryProvider(DirectoryProvider directory) {
    logger.debug("Adding {} to the list of site directories", directory);
    List<DirectoryProvider> directoryProvider = directories.get(directory.getIdentifier());
    if (directoryProvider == null) {
      directoryProvider = new ArrayList<DirectoryProvider>();
      directories.put(directory.getIdentifier(), directoryProvider);
    }
    directoryProvider.add(directory);
  }

  /**
   * Removes the directory service provider from the list of providers.
   * 
   * @param directory
   *          the directory service provider
   */
  void removeDirectoryProvidery(DirectoryProvider directory) {
    logger.debug("Removing site directory {}", directory);
    List<DirectoryProvider> siteDirectories = directories.get(directory.getIdentifier());
    if (siteDirectories != null) {
      siteDirectories.remove(directory);
      if (siteDirectories.size() == 0) {
        directories.remove(directory.getIdentifier());
      }
    }
  }

}
