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

package ch.entwine.weblounge.common.impl.security;

import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Securable;
import ch.entwine.weblounge.common.security.SecurityListener;
import ch.entwine.weblounge.common.security.User;

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

  /** Order in which to evaluate allow and deny rules */
  protected Order evaluationOrder = Order.AllowDeny;

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
   * Sets the order in which to evaluate allow and deny access rules.
   * 
   * @param order
   *          the evaluation order
   * @throws IllegalArgumentException
   *           if <code>order</code> is <code>null</code>
   */
  protected void setAllowDenyOrder(Order order) {
    if (order == null)
      throw new IllegalArgumentException("Order must not be null");
    this.evaluationOrder = order;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAllowDenyOrder()
   */
  @Override
  public Order getAllowDenyOrder() {
    return evaluationOrder;
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
   * Returns the actions that may be acquired on this object.
   * 
   * @return the available actions
   */
  public Action[] actions() {
    return securityCtx.actions();
  }

  /**
   * Sets the action to require the object <code>item</code>.
   * 
   * @param action
   *          the action
   * @param authorization
   *          the item that is allowed to obtain the action
   */
  public void allow(Action action, Authority authorization) {
    if (action == null)
      throw new IllegalArgumentException("Action must not be null");
    securityCtx.allow(action, authorization);
    firePermissionChanged(action);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isAllowed(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  public boolean isAllowed(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Action must not be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority must not be null");
    return securityCtx.isAllowed(action, authority);
  }
  
  /**
   * Removes the action and any associated role requirements from this context.
   * 
   * @param action
   *          the action
   */
  public void deny(Action action, Authority authorization) {
    securityCtx.deny(action, authorization);
    firePermissionChanged(action);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDenied(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  public boolean isDenied(Action action, Authority authority) {
    if (action == null)
      throw new IllegalArgumentException("Action must not be null");
    if (authority == null)
      throw new IllegalArgumentException("Authority must not be null");
    return securityCtx.isDenied(action, authority);
  }

  /**
   * Adds <code>listener</code> to the list of security listeners that will be
   * notified in case of ownership or action changes.
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
   * Fires the <code>actionChanged</code> event to all registered security
   * listeners.
   * 
   * @param action
   *          the changing action
   */
  protected void firePermissionChanged(Action action) {
    if (listeners == null)
      return;
    for (int i = 0; i < listeners.size(); i++) {
      (listeners.get(i)).actionChanged(this, action);
    }
  }

}