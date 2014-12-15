/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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

import ch.entwine.weblounge.common.security.AccessRule;
import ch.entwine.weblounge.common.security.Rule;
import ch.entwine.weblounge.common.security.Securable;

import java.util.Comparator;

/**
 * Comparator used to sort
 */
public final class AccessRuleComparator implements Comparator<AccessRule> {

  /** The sort order */
  private Securable.Order order = null;

  /**
   * Creates a new comparator.
   * 
   * @param order
   *          the sort order
   */
  public AccessRuleComparator(Securable.Order order) {
    if (order == null)
      throw new IllegalArgumentException("Order must not be null");
    this.order = order;
  }

  /**
   * Specifies the order.
   * 
   * @param order
   *          the sort order
   */
  public void setOrder(Securable.Order order) {
    this.order = order;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(AccessRule ruleA, AccessRule ruleB) {
    switch (order) {
      case AllowDeny:
        if (Rule.Allow.equals(ruleA.getRule()))
          return Rule.Allow.equals(ruleB.getRule()) ? ruleA.toString().compareTo(ruleB.toString()) : 1;
        else
          return Rule.Allow.equals(ruleB.getRule()) ? -1 : ruleA.toString().compareTo(ruleB.toString());
      case DenyAllow:
        if (Rule.Deny.equals(ruleA.getRule()))
          return Rule.Deny.equals(ruleB.getRule()) ? ruleA.toString().compareTo(ruleB.toString()) : 1;
        else
          return Rule.Deny.equals(ruleB.getRule()) ? -1 : ruleA.toString().compareTo(ruleB.toString());
      default:
        return ruleA.toString().compareTo(ruleB.toString());
    }
  }

}
