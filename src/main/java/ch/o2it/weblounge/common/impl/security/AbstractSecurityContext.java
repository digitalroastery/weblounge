/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation for a security context.
 */
public abstract class AbstractSecurityContext {

  /** The owner */
  protected User owner = null;

  /** The security listeners */
  private List<SecurityListener> listeners_ = null;

  /** Type mappings */
  protected static final Map<String, String> shortcuts = new HashMap<String, String>();

  /** Type mappings */
  protected static final Map<String, String> qualifier = new HashMap<String, String>();

  /**
   * Initializes the static shortcut mappings.
   */
  static {
    addAuthorityTypeShortcut(User.class.getName(), "user");
    addAuthorityTypeShortcut(Role.class.getName(), "role");
    addAuthorityTypeShortcut(Group.class.getName(), "group");
  }

  /**
   * Creates a default restriction set with no restrictions.
   */
  public AbstractSecurityContext() {
    this(null);
  }

  /**
   * Creates a default restriction set with no restrictions.
   * 
   * @param owner
   *          the secured object owner
   */
  public AbstractSecurityContext(User owner) {
    this.owner = owner;
  }

  /**
   * Adds the specified shortcut for authority type resolving to all security
   * contexts.
   * 
   * @param fullName
   *          the fully qualified authority type name
   * @param shortcut
   *          the shortcut to add
   */
  public static void addAuthorityTypeShortcut(String fullName, String shortcut) {
    shortcuts.put(shortcut, fullName);
    qualifier.put(fullName, shortcut);
  }

  /**
   * Removes the specified shortcut for authority type resolving from all
   * security contexts.
   * 
   * @param shortcut
   *          the shortcut to remove
   */
  public static void removeAuthorityTypeShortcut(String shortcut) {
    String fullName = shortcuts.remove(shortcut);
    qualifier.remove(fullName);
  }

  /**
   * Returns the shortcut for the given full authority type name or the full
   * name itself if no corresponding mapping has been registered.
   * 
   * @param fullName
   *          the fully qualified authority type name
   * @return the shortcut
   */
  public static String getAuthorityTypeShortcut(String fullName) {
    if (fullName == null)
      return null;
    String shortcut = qualifier.get(fullName);
    return (shortcut != null) ? shortcut : fullName;
  }

  /**
   * Returns the full authority type name for the given shortcut or the shortcut
   * itself if no corresponding mapping has been registered.
   * 
   * @param shortcut
   *          the shortcut
   * @return the fully qualified authority type name
   */
  public static String resolveAuthorityTypeShortcut(String shortcut) {
    if (shortcut == null)
      return null;
    String fullName = shortcuts.get(shortcut);
    return (fullName != null) ? fullName : shortcut;
  }

  /**
   * Sets a new owner for this context.
   * 
   * @param owner
   *          the context owner
   */
  public void setOwner(User owner) {
    this.owner = owner;
  }

  /**
   * Returns the context owner.
   * 
   * @return the owner
   */
  public User getOwner() {
    return owner;
  }

  /**
   * Returns <code>true</code> if <code>user</code> owns this context.
   * 
   * @param user
   *          the user
   * @return <code>true</code> if <code>user</code> owns this context
   */
  public boolean isOwnedBy(User user) {
    if (owner == null)
      return false;
    return owner.equals(user);
  }

  /**
   * Adds <code>authorities</code> to the authorized authorities regarding the
   * given permission.
   * 
   * @param permission
   *          the permission
   * @param authorities
   *          the authorities that are allowed
   */
  public void allow(Permission permission, Authority[] authorities) {
    if (authorities == null)
      throw new IllegalStateException("Authorities set is null!");
    for (Authority authority : authorities)
      allow(permission, authority);
  }
  
  public abstract void allow(Permission permission, Authority authoriy);

  /**
   * Adds <code>authorities</code> to the denied authorities regarding the given
   * permission.
   * 
   * @param permission
   *          the permission
   * @param authorities
   *          the authorities to deny
   */
  public void deny(Permission permission, Authority[] authorities) {
    if (authorities == null)
      throw new IllegalStateException("Authorities set is null!");
    for (Authority authority : authorities)
      deny(permission, authority);
  }

  public abstract void deny(Permission permission, Authority authoriy);

  /**
   * Checks whether at least one of the given authorities pass with respect to
   * the given permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authorities
   *          the object claiming the permission
   * @return <code>true</code> if all authorities pass
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    if (authorities == null || authorities.length == 0)
      return true;
    for (Authority authority : authorities) {
      if (check(permission, authority))
        return true;
    }
    return false;
  }
  
  public abstract boolean check(Permission permission, Authority authority);

  /**
   * Checks whether all of the given authorities pass with respect to the given
   * permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authorities
   *          the object claiming the permission
   * @return <code>true</code> if all authorities pass
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    if (authorities == null || authorities.length == 0)
      return true;
    for (Authority authority : authorities) {
      if (!check(permission, authority))
        return false;
    }
    return true;
  }

  /**
   * Adds the security listener to the pagelets security context.
   * 
   * @param listener
   *          the security listener
   */
  public void addSecurityListener(SecurityListener listener) {
    if (listeners_ == null)
      listeners_ = new ArrayList<SecurityListener>();
    listeners_.add(listener);
  }

  /**
   * Removes the security listener from the pagelets security context.
   * 
   * @param listener
   *          the security listener
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (listeners_ == null)
      return;
    listeners_.remove(listener);
  }

}