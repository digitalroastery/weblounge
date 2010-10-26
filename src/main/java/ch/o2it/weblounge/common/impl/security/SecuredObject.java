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
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base implementation for secured objects.
 */
public class SecuredObject implements Securable {

  /** The security context */
  protected SecurityContextImpl securityCtx = null;

  /** Security listener */
  protected List<SecurityListener> listeners = null;

  /**
   * Creates a secured object with no security constraints applied to it, except
   * that the owner may do anything with the object.
   * 
   * @param owner
   *          the object owner
   */
  public SecuredObject(User owner) {
    securityCtx = new SecurityContextImpl(owner);
  }

  /**
   * Creates a secured object with no security constraints applied to it.
   */
  public SecuredObject() {
    securityCtx = new SecurityContextImpl();
  }

  /**
   * Returns the associated security context.
   * 
   * @return the security context
   */
  public SecurityContextImpl getSecurityContext() {
    return securityCtx;
  }

  /**
   * Sets a new owner for this context.
   * 
   * @param owner
   *          the context owner
   */
  public void setOwner(User owner) {
    User oldOwner = securityCtx.getOwner();
    securityCtx.setOwner(owner);
    fireOwnerChanged(owner, oldOwner);
  }

  /**
   * Returns the context owner.
   * 
   * @return the owner
   */
  public User getOwner() {
    return securityCtx.getOwner();
  }

  /**
   * Checks whether the authorization satisfy the constraints of this context on
   * the given permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authorization
   *          the object used to obtain the permission
   * @return <code>true</code> if the authorization is sufficient
   */
  public boolean check(Permission permission, Authority authorization) {
    return securityCtx.check(permission, authorization);
  }

  /**
   * Returns <code>true</code> if the authorization <code>authorization</code>
   * is sufficient to act on the secured object in a way that requires the given
   * permissionset <code>p</code>.
   * 
   * @param permissions
   *          the required set of permissions
   * @param authorization
   *          the object claiming the permissions
   * @return <code>true</code> if the object may obtain the permissions
   */
  public boolean check(PermissionSet permissions, Authority authorization) {
    return securityCtx.check(permissions, authorization);
  }

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
   * Returns the permissions that may be acquired on this object.
   * 
   * @return the available permissions
   */
  public Permission[] permissions() {
    return securityCtx.permissions();
  }

  /**
   * Sets the permission <code>permission</code> to require the object
   * <code>item</code>.
   * 
   * @param permission
   *          the permission
   * @param authorization
   *          the item that is allowed to obtain the permission
   */
  public void allow(Permission permission, Authority authorization) {
    if (permission == null)
      throw new IllegalArgumentException("permission");
    securityCtx.allow(permission, authorization);
    firePermissionChanged(permission);
  }

  /**
   * Removes the permission and any associated role requirements from this
   * context.
   * 
   * @param permission
   *          the permission
   */
  public void deny(Permission permission, Authority authorization) {
    securityCtx.deny(permission, authorization);
    firePermissionChanged(permission);
  }

  /**
   * Adds <code>listener</code> to the list of security listeners that will be
   * notified in case of ownership or permission changes.
   * 
   * @param listener
   *          the new security listener
   */
  public void addSecurityListener(SecurityListener listener) {
    if (listeners == null)
      listeners = new ArrayList<SecurityListener>();
    listeners.add(listener);
  }

  /**
   * Removes <code>listener</code> from the list of security listeners.
   * 
   * @param listener
   *          the security listener to be removed
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (listeners == null)
      return;
    listeners.remove(listener);
  }

  /**
   * Fires the <code>ownerChanged</code> event to all registered security
   * listeners.
   * 
   * @param newOwner
   *          the new owner of this object
   * @param oldOwner
   *          the former object owner
   */
  protected void fireOwnerChanged(User newOwner, User oldOwner) {
    if (listeners == null)
      return;
    for (int i = 0; i < listeners.size(); i++) {
      (listeners.get(i)).ownerChanged(this, newOwner, oldOwner);
    }
  }

  /**
   * Fires the <code>permissionChanged</code> event to all registered security
   * listeners.
   * 
   * @param p
   *          the changing permission
   */
  protected void firePermissionChanged(Permission p) {
    if (listeners == null)
      return;
    for (int i = 0; i < listeners.size(); i++) {
      (listeners.get(i)).permissionChanged(this, p);
    }
  }

}