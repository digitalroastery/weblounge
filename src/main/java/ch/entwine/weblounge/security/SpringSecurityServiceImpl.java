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
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A Spring Security implementation of {@link SecurityService}.
 */
public class SpringSecurityServiceImpl implements SecurityService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SpringSecurityServiceImpl.class);

  /** Name of the anonymous user */
  public static final String ANONYMOUS_USER = "anonymous";

  /** Holds the site associated with the current thread */
  private static final ThreadLocal<Site> siteHolder = new ThreadLocal<Site>();

  /** Holds the user associated with the current thread */
  private static final ThreadLocal<User> userHolder = new ThreadLocal<User>();

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
    Set<Role> roles = new TreeSet<Role>();

    if (auth == null) {
      logger.warn("No spring security context available, setting current user to anonymous");
      user = new UserImpl(ANONYMOUS_USER, site.getIdentifier());
      roles.add(new RoleImpl(site.getAnonymousRole()));
    } else {
      Object principal = auth.getPrincipal();
      if (principal == null) {
        logger.warn("No principal found in spring security context, setting current user to anonymous");
        user = new UserImpl(ANONYMOUS_USER, site.getIdentifier());
        roles.add(new RoleImpl(site.getAnonymousRole()));
      } else if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        user = new UserImpl(userDetails.getUsername());

        Collection<GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities != null && authorities.size() > 0) {
          for (GrantedAuthority ga : authorities) {
            roles.add(new RoleImpl(ga.getAuthority()));
          }
        }
        logger.debug("Principal was identified as '{}'", user.getLogin());
      } else {
        logger.warn("Principal was not compatible with spring security, setting current user to anonymous");
        user = new UserImpl(ANONYMOUS_USER, site.getIdentifier());
        roles.add(new RoleImpl(site.getAnonymousRole()));
      }
    }

    for (Role role : roles) {
      user.addPublicCredentials(role);
    }

    return user;
  }

}
