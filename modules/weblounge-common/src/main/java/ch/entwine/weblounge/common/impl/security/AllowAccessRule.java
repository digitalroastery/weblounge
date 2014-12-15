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

import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Rule;

/**
 * Rule describing which authority is allowed to apply a given action.
 */
public class AllowAccessRule extends AccessRuleImpl {

  /**
   * Creates an access rule specifying which authority may be allowed applying a
   * certain action.
   * 
   * @param authority
   *          the authority
   * @param action
   *          the action
   * @throws IllegalArgumentException
   *           if any of the arguments are <code>null</code>
   */
  public AllowAccessRule(Authority authority, Action action)
      throws IllegalArgumentException {
    super(authority, action, Rule.Allow);
  }

}
