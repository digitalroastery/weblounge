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
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Rule;

/**
 * An access rule relates an authority (e. g. a user or a role) to a given
 * action and in addition defines whether that authority is allowed or denied
 * that action.
 */
public class AccessRuleImpl implements AccessRule {

  /** The action to be executed */
  protected Action action;

  /** The authority */
  protected Authority authority;

  /** The rule */
  protected Rule rule = Rule.Allow;

  /** The serialized form of this rule */
  private String ruleString = null;

  /**
   * Creates an access rule specifying which authority may be allowed or denied
   * applying a certain action.
   * 
   * @param authority
   *          the authority
   * @param action
   *          the action
   * @param rule
   *          the rule
   * @throws IllegalArgumentException
   *           if any of the arguments are <code>null</code>
   */
  public AccessRuleImpl(Authority authority, Action action, Rule rule)
      throws IllegalArgumentException {
    this.action = action;
    this.authority = authority;
    this.rule = rule;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.AccessRule#getAuthority()
   */
  @Override
  public Authority getAuthority() {
    return authority;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.AccessRule#getAction()
   */
  @Override
  public Action getAction() {
    return action;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.AccessRule#getRule()
   */
  @Override
  public Rule getRule() {
    return rule;
  }

  /**
   * Returns a serialized version of this rule in the form
   * <code>rule:authority:action</code>.
   * 
   * @return the serialized rule
   */
  private String getSerializedRule(AccessRule rule) {
    return rule.toString().toLowerCase() + ":" + authority.toString().toLowerCase() + ":" + action.toString().toLowerCase();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (ruleString == null)
      ruleString = getSerializedRule(this);
    return ruleString.hashCode();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AccessRuleImpl))
      return false;
    if (ruleString == null)
      ruleString = getSerializedRule(this);
    return ruleString.equals(getSerializedRule((AccessRule) obj));
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (ruleString == null)
      ruleString = getSerializedRule(this);
    return ruleString;
  }

}
