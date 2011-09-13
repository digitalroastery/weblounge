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

package ch.entwine.weblounge.security;

import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A Spring Security implementation of {@link SecurityService}.
 */
public class SpringSecurityServiceImpl implements SecurityService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SpringSecurityServiceImpl.class);

  /** Spring configuration guest identifier */
  private static final String SPRING_GUEST = "spring:guest";
  
  /** Name of the generic anonymous user */
  public static final String ANONYMOUS_USER = "anonymous";

  /** Name of the generic admin user */
  public static final String ADMIN_USER = "admin";

  /** Holds the site associated with the current thread */
  private static final ThreadLocal<Site> siteHolder = new ThreadLocal<Site>();

  /** Holds the user associated with the current thread */
  private static final ThreadLocal<User> userHolder = new ThreadLocal<User>();

  /** Whether the system administrator has configured a no-security policy */
  private boolean wideOpen = false;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#getSite()
   */
  public Site getSite() {
    return siteHolder.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#setSite(ch.entwine.weblounge.common.site.Site)
   */
  public void setSite(Site site) {
    siteHolder.set(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#setUser(ch.entwine.weblounge.common.security.User)
   */
  public void setUser(User user) {
    userHolder.set(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#getUser()
   */
  public User getUser() {
    Site site = getSite();
    User delegatedUser = userHolder.get();
    if (delegatedUser != null) {
      return delegatedUser;
    }

    logger.trace("Looking up user from spring security context");
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = null;
    Set<Role> roles = new HashSet<Role>();

    if (wideOpen) {
      user = new UserImpl(ADMIN_USER, "world");
      roles.add(SystemRole.SYSTEMADMIN);
      // user = new UserImpl(ADMIN_USER, site.getIdentifier());
      // roles.add(getLocalRole(site, SystemRole.SYSTEMADMIN));
    } else if (auth == null) {
      logger.debug("No spring security context available, setting current user to anonymous");
      String realm = site != null ? site.getIdentifier() : "world";
      user = new UserImpl(ANONYMOUS_USER, realm);
      roles.add(SystemRole.GUEST);
      // roles.add(getLocalRole(site, SystemRole.GUEST));
    } else {
      Object principal = auth.getPrincipal();
      if (principal == null) {
        logger.warn("No principal found in spring security context, setting current user to anonymous");
        user = new UserImpl(ANONYMOUS_USER, site.getIdentifier());
        roles.add(getLocalRole(site, SystemRole.GUEST));
      } else if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        user = new UserImpl(userDetails.getUsername());

        Collection<GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities != null && authorities.size() > 0) {
          for (GrantedAuthority ga : authorities) {
            roles.add(new RoleImpl(SystemRole.CONTEXT, ga.getAuthority()));
          }
        }
        logger.debug("Principal was identified as '{}'", user.getLogin());
        
      } else if (SPRING_GUEST.equals(principal)) {
        user = new UserImpl(ANONYMOUS_USER, site.getIdentifier());
        roles.add(SystemRole.GUEST);
        //roles.add(getLocalRole(site, SystemRole.GUEST));
      } else {
        logger.warn("Principal was not compatible with spring security, setting current user to anonymous");
        user = new UserImpl(ANONYMOUS_USER, site.getIdentifier());
        roles.add(SystemRole.GUEST);
        //roles.add(getLocalRole(site, SystemRole.GUEST));
      }
    }

    for (Role role : roles) {
      user.addPublicCredentials(role);
    }

    return user;
  }

  /**
   * Returns the local role for the given system role or the system role itself,
   * if no local role was defined.
   * 
   * @param site
   *          the site
   * @param role
   *          the system role
   * @return the local role
   */
  private Role getLocalRole(Site site, Role role) {
    String localRole = site.getLocalRole(role.getIdentifier());
    if (StringUtils.isNotBlank(localRole))
      return new RoleImpl(site.getIdentifier(), localRole);
    return role;
  }

  /**
   * Whether the user has configured a no-security policy.
   * 
   * @param wideOpen
   *          <code>true</code> if there is no security in place
   */
  void setWideOpen(boolean wideOpen) {
    this.wideOpen = wideOpen;
  }

}
