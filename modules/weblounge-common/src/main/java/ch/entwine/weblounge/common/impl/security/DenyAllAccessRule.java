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
 * Rule describing that anyone and anything is denied the given action.
 */
public class DenyAllAccessRule extends AccessRuleImpl {

  /** Singleton for any authority */
  private static final Authority ANY_AUTHORITY = new AnyAuthority();

  /**
   * Creates an access rule specifying that anyone and anything is denied the
   * given action.
   * 
   * @param action
   *          the action
   * @throws IllegalArgumentException
   *           if the action is <code>null</code>
   */
  public DenyAllAccessRule(Action action) throws IllegalArgumentException {
    super(ANY_AUTHORITY, action, Rule.Deny);
  }

}
