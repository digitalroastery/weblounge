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

import java.util.SortedSet;

/**
 * The <code>Secured</code> interface defines the required methods for a secured
 * object.
 */
public interface Securable {

  /** The order in which to evaluate allow and deny rules */
  enum Order {
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
   * Returns <code>true</code> if the access rules enforced by this
   * {@link Securable} represent the default set of rules, i. e. the
   * {@link Securable} does not define any custom access rules of its own.
   * 
   * @return <code>true</code> if this object is secured by the default rather
   *         than a custom set of access rules
   */
  boolean isDefaultAccess();

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
   * Adds <code>rule</code> to the list of access rules that define which
   * authority is either allowed or denied access to the {@link Securable}
   * <p>
   * <b>Note:</b> Calling this method replaces any default rules on the action
   * specified by the rule.
   * 
   * @param action
   *          the action
   * @param authority
   *          the item that is allowed to obtain the action
   */
  void addAccessRule(AccessRule rule);

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
  Action[] getActions();

  /**
   * Returns the access rules in the order which has been specified by means of
   * {@link #setAllowDenyOrder(Order)}.
   * 
   * @return the rules
   */
  SortedSet<AccessRule> getAccessRules();

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