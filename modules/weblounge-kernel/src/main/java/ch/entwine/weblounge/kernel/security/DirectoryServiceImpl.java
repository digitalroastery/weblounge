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
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.security.DirectoryService;
import ch.entwine.weblounge.common.security.LoginListener;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.security.SiteDirectory;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
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
  protected Map<String, List<DirectoryProvider>> siteDirectories = new HashMap<String, List<DirectoryProvider>>();

  /** The list of system directories */
  protected List<DirectoryProvider> systemDirectories = new ArrayList<DirectoryProvider>();

  /** The list of login listeners */
  protected List<LoginListener> loginListeners = new ArrayList<LoginListener>();

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

    List<DirectoryProvider> providers = new ArrayList<DirectoryProvider>();

    // Assemble a list of all possible directories
    List<DirectoryProvider> siteProviders = this.siteDirectories.get(site.getIdentifier());
    if (siteProviders != null)
      providers.addAll(siteProviders);
    providers.addAll(systemDirectories);

    // Collect roles from all directories registered for this site
    SortedSet<Role> roles = new TreeSet<Role>();
    for (DirectoryProvider directory : providers) {
      for (Role role : directory.getRoles()) {
        roles.add(role);
      }
    }
    return roles.toArray(new Role[roles.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#loadUser(java.lang.String,
   *      Site)
   */
  public User loadUser(String login, Site site) throws IllegalStateException {
    List<DirectoryProvider> providers = new ArrayList<DirectoryProvider>();

    // Assemble a list of all possible directories
    List<DirectoryProvider> siteProviders = this.siteDirectories.get(site.getIdentifier());
    if (siteProviders != null)
      providers.addAll(siteProviders);
    providers.addAll(systemDirectories);

    // Find a user principal to use for login
    for (DirectoryProvider directory : providers) {
      try {
        User user = directory.loadUser(login, site);
        if (user != null) {
          logger.debug("User directory '{}' returned a user to login '{}' into site '{}'", new String[] {
              directory.getIdentifier(),
              login,
              site.getIdentifier() });
          return user;
        }
      } catch (Throwable t) {
        logger.warn("Error looking up user from {}: {}", directory, t.getMessage());
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
   */
  public UserDetails loadUserByUsername(String name)
      throws UsernameNotFoundException, DataAccessException {

    Site site = securityService.getSite();
    if (site == null) {
      logger.error("Site context not available during user lookup");
      throw new UsernameNotFoundException("No site context available");
    }

    User user = loadUser(name, site);
    if (user == null) {
      throw new UsernameNotFoundException(name);
    } else {

      // By default, add the anonymous role so the user is able to access
      // publicly available resources
      user.addPublicCredentials(SystemRole.GUEST);

      // Collect the set of roles (granted authorities) for this users
      Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
      for (Object o : user.getPublicCredentials(Role.class)) {
        Role masterRole = (Role) o;
        for (Role r : masterRole.getClosure()) {
          authorities.add(new GrantedAuthorityImpl(r.getContext() + ":" + r.getIdentifier()));

          // Every role may or may not be a system role or - in case of non-
          // system roles, may or may not be including one or more of those
          // roles. Let's ask for a translation and then add those roles
          // to the set of granted authorities
          Role[] systemEquivalents = getSystemRoles(r);
          for (Role systemRole : systemEquivalents) {
            authorities.add(new GrantedAuthorityImpl(systemRole.getContext() + ":" + systemRole.getIdentifier()));
            user.addPublicCredentials(systemRole);
          }
        }
      }

      // Make sure there is no ambiguous information with regards to passwords
      Set<Object> passwords = user.getPrivateCredentials(Password.class);
      if (passwords.size() > 1) {
        logger.warn("User '{}@{}' has more than one password'", name, site.getIdentifier());
        throw new DataRetrievalFailureException("User '" + user + "' has more than one password");
      } else if (passwords.size() == 0) {
        logger.warn("User '{}@{}' has no password", name, site.getIdentifier());
        throw new DataRetrievalFailureException("User '" + user + "' has no password");
      }

      // Create the password according to the site's and Spring Security's
      // digest policy
      Password p = (Password) passwords.iterator().next();
      String password = null;
      switch (site.getDigestType()) {
        case md5:
          if (!DigestType.md5.equals(p.getDigestType())) {
            logger.debug("Creating digest password for '{}@{}'", name, site.getIdentifier());
            password = PasswordEncoder.encode(p.getPassword());
          } else {
            password = p.getPassword();
          }
          break;
        case plain:
          if (!DigestType.plain.equals(p.getDigestType())) {
            logger.warn("User '{}@{}' does not have a plain text password'", name, site.getIdentifier());
            return null;
          }
          password = p.getPassword();
          break;
        default:
          throw new IllegalStateException("Unknown digest type '" + site.getDigestType() + "'");
      }

      // Notifiy login listeners of the initiated login attempt
      for (LoginListener listener : loginListeners) {
        try {
          listener.beforeLogin(site, user);
        } catch (Throwable t) {
          logger.warn("Login listener '{}' failed to preoprly process before login callback", listener, t);
        }
      }

      // Provide the user to Spring Security
      return new SpringSecurityUser(user, password, true, true, true, true, authorities);
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

    List<DirectoryProvider> providers = new ArrayList<DirectoryProvider>();

    // Assemble a list of all possible directories
    List<DirectoryProvider> siteProviders = this.siteDirectories.get(site.getIdentifier());
    if (siteProviders != null)
      providers.addAll(siteProviders);
    providers.addAll(systemDirectories);

    for (DirectoryProvider directory : providers) {
      Role localRole = directory.getLocalRole(role);
      if (localRole != null) {
        return localRole;
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getSystemRoles(ch.entwine.weblounge.common.security.Role)
   */
  public Role[] getSystemRoles(Role role) {
    Site site = securityService.getSite();

    if (site == null)
      throw new IllegalStateException("No site set in security context");

    List<DirectoryProvider> providers = new ArrayList<DirectoryProvider>();

    // Assemble a list of all possible directories
    List<DirectoryProvider> siteProviders = this.siteDirectories.get(site.getIdentifier());
    if (siteProviders != null)
      providers.addAll(siteProviders);
    providers.addAll(systemDirectories);

    Set<Role> systemRoles = new HashSet<Role>();
    for (DirectoryProvider directory : providers) {
      Role[] roleMappings = directory.getSystemRoles(role);
      if (roleMappings != null && roleMappings.length > 0) {
        systemRoles.addAll(Arrays.asList(roleMappings));
      }
    }

    return systemRoles.toArray(new Role[systemRoles.size()]);
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
    logger.debug("Registering directory provider '{}'", directory.getIdentifier());
    if (directory instanceof SiteDirectory) {
      List<DirectoryProvider> directoryProviders = siteDirectories.get(directory.getIdentifier());
      if (directoryProviders == null) {
        directoryProviders = new ArrayList<DirectoryProvider>();
        siteDirectories.put(directory.getIdentifier(), directoryProviders);
      }
      directoryProviders.add(directory);
    } else {
      systemDirectories.add(directory);
    }

  }

  /**
   * Removes the directory service provider from the list of providers.
   * 
   * @param directory
   *          the directory service provider
   */
  void removeDirectoryProvider(DirectoryProvider directory) {
    logger.debug("Unregistering directory provider '{}'", directory.getIdentifier());
    if (directory instanceof SiteDirectory) {
      List<DirectoryProvider> directoryProviders = this.siteDirectories.get(directory.getIdentifier());
      if (directoryProviders != null) {
        directoryProviders.remove(directory);
        if (directoryProviders.size() == 0) {
          directoryProviders.remove(directory.getIdentifier());
        }
      }
    } else {
      systemDirectories.remove(directory);
    }
  }

  /**
   * Adds the login listener to the list of login listeners.
   * 
   * @param loginListener
   *          the login listener
   */
  void addLoginListener(LoginListener loginListener) {
    logger.debug("Registering login listener '{}'", loginListener);
    loginListeners.add(loginListener);
  }

  /**
   * Removes the login listener from the list of login listeners.
   * 
   * @param loginListener
   *          the login listener
   */
  void removeLoginListener(LoginListener loginListener) {
    logger.debug("Removing login listener '{}'", loginListener);
    loginListeners.remove(loginListener);
  }

}
