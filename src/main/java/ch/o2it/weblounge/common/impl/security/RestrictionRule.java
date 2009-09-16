/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.security.Authority;

/**
 * A restriction rule represents the <code>allow</code> and <code>deny</code>
 * entries of a restriction definition. It features a type and a value, e. g.
 * the type <code>ch.o2it.weblounge.api.security.Role</code> and <code>
 * system:editor</code>
 * , meaning that this rule will match the system role <code>editor</code>.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public final class RestrictionRule extends AuthorityImpl {

  /**
   * Creates a new allow or deny rule with the given type and id.
   * 
   * @param id
   *          the authorization identifier
   * @param type
   *          the authorization type
   */
  public RestrictionRule(String type, String id) {
    super(type, id);
  }

  /**
   * Returns <code>true</code> if the authorization identifier of this
   * authorization equals the string representation of <code>o</code>.
   * <p>
   * <strong>Note:</strong> this implementation also returns <code>true</code>
   * if either the authorization type is different from the type specified in
   * this rule or if <code>authorization</code> is <code>null</code>.
   * 
   * @param authority
   *          the authority trying to evaluate
   * @return <code>true</code> if this rule matches the authorization
   */
  public boolean evaluate(Authority authority) {
    return authority == null || type.equals(authority.getAuthorityType()) || id.equals(authority.getAuthorityId());
  }

}