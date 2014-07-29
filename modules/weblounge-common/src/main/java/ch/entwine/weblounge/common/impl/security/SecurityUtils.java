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

package ch.entwine.weblounge.common.impl.security;

import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Securable;
import ch.entwine.weblounge.common.security.Security;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility methods around security.
 */
public final class SecurityUtils {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

  /** Holds the site associated with the current thread */
  static final ThreadLocal<Site> siteHolder = new ThreadLocal<Site>();

  /** Holds the user associated with the current thread */
  static final ThreadLocal<User> userHolder = new ThreadLocal<User>();

  /** Holds the extended user associated with the current thread */
  static final ThreadLocal<User> extendedUserHolder = new ThreadLocal<User>();

  /** The default system administrator */
  private static final User systemAdmin = new WebloungeAdminImpl("admin");

  /** The default system administrator */
  private static final User anonymous = new Guest();

  /** Whether the security status is determined */
  private static boolean configured = false;

  /** Whether the system administrator has configured a no-security policy */
  private static boolean enabled = true;

  /**
   * Private constructor to prevent instantiation.
   */
  private SecurityUtils() {
    // Nothing to do
  }

  /**
   * Define whether the security policy has been determined.
   * 
   * TODO: Should be determined per site
   * 
   * @param configured
   *          <code>true</code> if the security policy has been determined
   */
  public static void setConfigured(boolean configured) {
    SecurityUtils.configured = configured;
  }

  /**
   * Whether the security policy for this Weblounge installation has been
   * determined.
   * 
   * TODO: Determine per site
   */
  public static boolean isConfigured() {
    return SecurityUtils.configured;
  }

  /**
   * Whether the user has configured a no-security policy.
   * 
   * @param enabled
   *          <code>true</code> if there is a security policy in place
   */
  public static void setEnabled(boolean enabled) {
    SecurityUtils.enabled = enabled;
  }

  /**
   * Returns <code>true</code> if the user has enabled a security policy. When
   * <code>false</code> is returned, no security policy is enforced.
   * 
   * TODO: Determine per site
   * 
   * @return <code>true</code> if a security policy has been defined
   */
  public static boolean isEnabled() {
    return SecurityUtils.enabled;
  }

  /**
   * Sets the current thread's user context to another user. This is useful when
   * spawning new threads that must contain the parent thread's user context.
   * 
   * @param user
   *          the user to set for the current user context
   */
  public static void setUser(User user) {
    userHolder.set(user);
  }

  /**
   * Gets the current user in a generic form ({@link User}), or the local
   * organization's anonymous user if the user has not been authenticated.
   * 
   * @return the user
   */
  public static User getUser() {
    if (!configured)
      return anonymous;
    if (enabled)
      return userHolder.get();
    else
      return systemAdmin;
  }

  /**
   * Gets the current user including all the details, or the local
   * organization's anonymous user if the user has not been authenticated.
   * 
   * @return the user
   */
  public static User getExtendedUser() {
    return extendedUserHolder.get();
  }

  /**
   * Sets the site for the calling thread.
   * 
   * @param site
   *          the site
   */
  public static void setSite(Site organization) {
    siteHolder.set(organization);
  }

  /**
   * Gets the site associated with the current thread context.
   * 
   * @return the site
   */
  public static Site getSite() {
    return siteHolder.get();
  }

  /**
   * Returns <code>true</code> if the user is authenticated. In other words,
   * <code>true</code> is returned if the user is <b>not<b> an anonymous user.
   * <p>
   * Generally speaking, a user is considered an anonymous user if he owns the
   * {@link SystemRole#GUEST} role.
   * 
   * @param user
   *          the user
   * @return <code>true</code> if the user is authenticated
   */
  public static boolean isAuthenticated(User user) {
    if (user == null)
      throw new IllegalArgumentException("User must not be null");
    return user.isAuthenticated();
  }

  /**
   * Returns <code>true</code> if <code>user</code> has role <code>role</code>
   * amongst its public credentials.
   * 
   * @param user
   *          the user
   * @param role
   *          the role
   * @return <code>true</code> if the user has the given role
   */
  public static boolean userHasRole(User user, Role role) {
    if (user == null)
      throw new IllegalArgumentException("User cannot be null");
    if (role == null)
      throw new IllegalArgumentException("Role cannot be null");
    for (Object o : user.getPublicCredentials(Role.class)) {
      Role masterRole = (Role) o;
      for (Role r : masterRole.getClosure()) {
        if (role.equals(r))
          return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if <code>user</code> has the role with identifier
   * <code>roleId</code> from the weblounge context
   * {@link SecurityConstants#SYSTEM_CONTEXT} amongst its public credentials.
   * 
   * @param user
   *          the user
   * @param role
   *          the weblounge role identifier
   * @return <code>true</code> if the user has the given role
   */
  public static boolean userHasRole(User user, String roleId) {
    if (user == null)
      throw new IllegalArgumentException("User cannot be null");
    if (roleId == null)
      throw new IllegalArgumentException("Role identifier cannot be null");
    for (Object o : user.getPublicCredentials(Role.class)) {
      Role masterRole = (Role) o;
      for (Role r : masterRole.getClosure()) {
        String ctx = r.getContext();
        String id = r.getIdentifier();
        if (ctx.equals(Security.SYSTEM_CONTEXT) && id.equals(roleId))
          return true;
      }
    }
    return false;
  }

  /**
   * Returns a user's roles.
   * 
   * @param user
   *          the user
   * @return the roles
   */
  public static Role[] getRoles(User user) {
    if (user == null)
      throw new IllegalArgumentException("User cannot be null");
    List<Role> roles = new ArrayList<Role>();
    for (Object o : user.getPublicCredentials(Role.class)) {
      roles.add((Role) o);
    }
    return roles.toArray(new Role[roles.size()]);
  }

  /**
   * Checks whether an action may be performed on a resource based on an
   * authority like a role.
   * 
   * @param securable
   *          the resource that is about to be actioned on
   * @param action
   *          the action to perform
   * @param authority
   *          the credential used to obtain access
   * @return <code>true</code> if <code>action</code> may be performed
   */
  public static boolean checkAuthorization(Securable securable, Action action,
      Authority authority) {
    if (securable == null)
      throw new IllegalArgumentException("Securable cannot be null");
    if (action == null)
      throw new IllegalArgumentException("Action cannot be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority cannot be null");
    logger.trace("Request to check action '{}' for authority '{}' on {}", new Object[] {
        action,
        authority,
        securable });

    switch (securable.getAllowDenyOrder()) {
      case AllowDeny:
        if (securable.isAllowed(action, authority))
          return true;
        if (securable.isDenied(action, authority))
          return false;
        return false;
      case DenyAllow:
        if (securable.isDenied(action, authority))
          return false;
        if (securable.isAllowed(action, authority))
          return true;
        return false;
      default:
        throw new IllegalStateException("Allow/deny order " + securable.getAllowDenyOrder() + " unsupported");
    }

  }

  /**
   * Returns <code>true</code> if the authorization is sufficient to obtain the
   * "oneof" action set.
   * 
   * @param securable
   *          the resource that is about to be actioned on
   * @param actions
   *          the set of actions
   * @param authorization
   *          the authorization to check
   * @return <code>true</code> if the user has one of the actions
   */
  public static boolean checkAuthorizationForSome(Securable securable,
      Set<Action> actions, Authority authorization) {
    if (actions.size() == 0)
      return true;
    for (Action action : actions) {
      if (checkAuthorization(securable, action, authorization)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the authorization is sufficient to obtain the
   * "allof" action set.
   * 
   * @param securable
   *          the resource that is about to be actioned on
   * @param actions
   *          the set of actions
   * @param authorization
   *          the authorization to check
   * @return <code>true</code> if the user has all of the actions
   */
  public static boolean checkAuthorizationForAll(Securable securable,
      Set<Action> actions, Authority authorization) {
    if (actions.size() == 0)
      return true;
    for (Action action : actions) {
      if (!checkAuthorization(securable, action, authorization)) {
        return false;
      }
    }
    return true;
  }

}
