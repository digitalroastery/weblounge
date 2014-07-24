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

import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

/**
 * Utility methods around security.
 */
public final class WebloungeSecurityUtils {

  /** Holds the site associated with the current thread */
  static final ThreadLocal<Site> siteHolder = new ThreadLocal<Site>();

  /** Holds the user associated with the current thread */
  static final ThreadLocal<User> userHolder = new ThreadLocal<User>();

  /** Holds the extended user associated with the current thread */
  static final ThreadLocal<User> extendedUserHolder = new ThreadLocal<User>();

  /** Whether the security status is determined */
  private static boolean configured = false;

  /** Whether the system administrator has configured a no-security policy */
  private static boolean restricted = true;

  /**
   * Private constructor to prevent instantiation.
   */
  private WebloungeSecurityUtils() {
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
    WebloungeSecurityUtils.configured = configured;
  }

  /**
   * Whether the security policy for this Weblounge installation has been
   * determined.
   * 
   * TODO: Should be determined per site
   */
  public static boolean isConfigured() {
    return WebloungeSecurityUtils.configured;
  }

  /**
   * Whether the administrator has configured an open door security policy or
   * whether a security policy is in place.
   * 
   * @param enabled
   *          <code>true</code> if there is a security policy in place
   */
  public static void setRestricted(boolean enabled) {
    WebloungeSecurityUtils.restricted = enabled;
  }

  /**
   * Returns <code>false</code> if the administrator has configured an open door
   * security policy, <code>true</code> if a security policy is in place.
   * 
   * @return <code>true</code> if security constraints have been configured
   */
  public static boolean isRestricted() {
    return WebloungeSecurityUtils.restricted;
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
    return userHolder.get();
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

}
