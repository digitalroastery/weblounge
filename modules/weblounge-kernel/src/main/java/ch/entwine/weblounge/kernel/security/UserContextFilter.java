/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.kernel.security;

import ch.entwine.weblounge.common.impl.security.Guest;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Security;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter that for every request will set the Site on the security service.
 */
public class UserContextFilter implements Filter {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(UserContextFilter.class);

  /**
   * Creates a new weblounge security filter, which is populating the required
   * fields for the current request in the security service.
   */
  public UserContextFilter() {
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    // Is not being called
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    // Make sure we have a site
    Site site = SecurityUtils.getSite();
    if (site == null)
      throw new IllegalStateException("Site context is not available at user lookup");

    User user = null;

    // Extract the user from the request
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      user = getUser(site);
      if (user != null)
        logger.debug("Processing {} as user '{}'", httpRequest.getPathInfo(), site.getIdentifier());
      else
        logger.debug("No user found for request to {}", httpRequest.getPathInfo());
    }

    // Set the site and the user on the request
    SecurityUtils.setUser(user);

    chain.doFilter(request, response);

    // Make sure the thread is not associated with the site or the user anymore
    SecurityUtils.setUser(null);
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    // Nothing to do
  }

  /**
   * Loads the user from the given site.
   *
   * @param site
   *          the site
   * @return the user
   */
  protected User getUser(Site site) {
    logger.trace("Looking up user from spring security context");
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = null;
    Set<Role> roles = new HashSet<Role>();

    if (!SecurityUtils.isEnabled()) {
      user = new UserImpl(Security.ADMIN_USER, Security.SYSTEM_CONTEXT, Security.ADMIN_NAME);
      roles.add(SystemRole.SYSTEMADMIN);
      roles.add(getLocalRole(site, SystemRole.SYSTEMADMIN));
    } else if (auth == null) {
      logger.debug("No spring security context available, setting current user to anonymous");
      String realm = site != null ? site.getIdentifier() : Security.SYSTEM_CONTEXT;
      user = new UserImpl(Security.ANONYMOUS_USER, realm, Security.ANONYMOUS_NAME);
      roles.add(SystemRole.GUEST);
      roles.add(getLocalRole(site, SystemRole.GUEST));
    } else {
      Object principal = auth.getPrincipal();
      if (principal == null) {
        logger.warn("No principal found in spring security context, setting current user to anonymous");
        user = new Guest(site.getIdentifier());
        roles.add(getLocalRole(site, SystemRole.GUEST));
      } else if (principal instanceof SpringSecurityUser) {
        user = ((SpringSecurityUser) principal).getUser();
        user.setAuthenticated(true);
        addRoles(roles, auth.getAuthorities());
        logger.debug("Principal was identified as '{}'", user.getLogin());
      } else if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        user = new UserImpl(userDetails.getUsername(), true);
        addRoles(roles, auth.getAuthorities());
        logger.debug("Principal was identified as '{}'", user.getLogin());
      } else if (Security.ANONYMOUS_USER.equals(principal)) {
        user = new Guest(site.getIdentifier());
        roles.add(getLocalRole(site, SystemRole.GUEST));
      } else {
        logger.warn("Principal was not compatible with spring security, setting current user to anonymous");
        user = new Guest(site.getIdentifier());
        roles.add(getLocalRole(site, SystemRole.GUEST));
      }
    }

    for (Role role : roles) {
      logger.debug("Principal '{}' gained role '{}'", user.getLogin(), role);
      user.addPublicCredentials(role);
    }

    return user;
  }

  /**
   * Translates SpringSecurity authorities from Spring Security to Weblounge
   * Roles.
   * 
   * @param roles
   *          the set of roles
   * @param authorities
   *          the authorities
   */
  protected void addRoles(Set<Role> roles,
      Collection<? extends GrantedAuthority> authorities) {
    if (authorities != null && authorities.size() > 0) {
      for (GrantedAuthority ga : authorities) {
        roles.add(new RoleImpl(ga.getAuthority()));
      }
    }
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
  protected Role getLocalRole(Site site, Role role) {
    String localRole = site.getLocalRole(role.getIdentifier());
    if (StringUtils.isNotBlank(localRole))
      return new RoleImpl(site.getIdentifier(), localRole);
    return role;
  }

}
