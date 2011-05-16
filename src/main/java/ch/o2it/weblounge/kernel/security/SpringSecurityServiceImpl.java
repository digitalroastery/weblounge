/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.kernel.security;

import ch.o2it.weblounge.common.impl.security.RoleImpl;
import ch.o2it.weblounge.common.impl.security.UserImpl;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.SecurityService;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

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

  /** Name of the anonymous user */
  public static final String ANONYMOUS_USER = "anonymous";

  /**
   * Holds delegates users for new threads that have been spawned from
   * authenticated threads
   */
  private static final ThreadLocal<User> delegatedUserHolder = new ThreadLocal<User>();

  /** Holds the site associated with the current thread */
  private static final ThreadLocal<Site> site = new ThreadLocal<Site>();

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.SecurityService#getSite()
   */
  public Site getSite() {
    return site.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.SecurityService#setSite(ch.o2it.weblounge.common.site.Site)
   */
  public void setSite(Site site) {
    SpringSecurityServiceImpl.site.set(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.SecurityService#getUser()
   */
  public User getUser() {
    Site site = getSite();
    User delegatedUser = delegatedUserHolder.get();
    if (delegatedUser != null) {
      return delegatedUser;
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = null;
    Set<Role> roles = new TreeSet<Role>();

    if (auth == null) {
      user = new UserImpl(ANONYMOUS_USER, site.getIdentifier());
      roles.add(new RoleImpl(site.getAnonymousRole()));
    } else {
      Object principal = auth.getPrincipal();
      if (principal == null) {
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
      } else {
        user = new UserImpl(ANONYMOUS_USER, site.getIdentifier());
        roles.add(new RoleImpl(site.getAnonymousRole()));
      }
    }
    
    for (Role role : roles) {
      user.addPublicCredentials(role);
    }
    
    return user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.SecurityService#setUser(ch.o2it.weblounge.common.security.User)
   */
  public void setUser(User user) {
    delegatedUserHolder.set(user);
  }

}
