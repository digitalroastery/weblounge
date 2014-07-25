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

package ch.entwine.weblounge.common.security;

/**
 * The <code>Secured</code> interface defines the required methods for a secured
 * object.
 */
public interface Securable {

  /** The order in which to evaluate allow and deny rules */
  public enum Order {
    AllowDeny, DenyAllow
  };

  /**
   * Sets the object owner.
   * 
   * @param owner
   *          the object owner
   */
  void setOwner(User owner);

  /**
   * Returns the owner of this object.
   * 
   * @return the owner
   */
  User getOwner();

  /**
   * Sets the {@link Order} in which allow and deny rules are to be evaluated.
   * 
   * @param order
   *          the order
   */
  void setAllowDenyOrder(Order order);

  /**
   * Returns the {@link Order} in which allow and deny rules are to be
   * evaluated.
   * 
   * @return the order
   */
  Order getAllowDenyOrder();

  /**
   * Adds <code>authority</code> to the authorized authorities regarding the
   * given action.
   * <p>
   * <b>Note:</b> Calling this method replaces any default authorities on the
   * given action, so if you want to keep them, add them here explicitly.
   * 
   * @param action
   *          the action
   * @param authority
   *          the item that is allowed to obtain the action
   */
  void allow(Action action, Authority authority);

  /**
   * Returns <code>true</code> if <code>authority</code> is authorized to apply
   * the the given action.
   * 
   * @param action
   *          the action
   * @param authority
   *          the item that is allowed to obtain the action
   * @return <code>true</code> if the authority is authorized
   */
  boolean isAllowed(Action action, Authority authority);

  /**
   * Removes <code>authority</code> from the denied authorities regarding the
   * given action. This method will remove the authority from both the
   * explicitly allowed and the default authorities.
   * 
   * @param action
   *          the action
   * @param authority
   *          the authorization to deny
   */
  void deny(Action action, Authority authority);

  /**
   * Returns <code>true</code> if <code>authority</code> is denied to apply the
   * the given action.
   * 
   * @param action
   *          the action
   * @param authority
   *          the item that is allowed to obtain the action
   * @return <code>true</code> if the authority is denied
   */
  boolean isDenied(Action action, Authority authority);

  /**
   * Returns the actions that may be acquired on this object.
   * 
   * @return the available actions
   */
  Action[] actions();

  /**
   * Adds <code>listener</code> to the list of security listeners that will be
   * notified in case of ownership or permission changes.
   * 
   * @param listener
   *          the new security listener
   */
  void addSecurityListener(SecurityListener listener);

  /**
   * Removes <code>listener</code> from the list of security listeners.
   * 
   * @param listener
   *          the security listener to be removed
   */
  void removeSecurityListener(SecurityListener listener);

}