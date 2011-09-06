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

import ch.entwine.weblounge.common.impl.language.LocalizableContent;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Role;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * A role models properties that can be applied to persons or groups of persons.
 * If a person has a certain role, then he/she usually is allowed to do
 * something others aren't allowed to do.
 */
public class RoleImpl extends LocalizableContent<String> implements Role {

  /** Role identifier */
  protected String identifier = null;

  /** The role context */
  protected String context = null;

  /** Roles that are extended by this role */
  protected Set<Role> ancestors = null;

  /**
   * Creates a new role from the parameter context::id.
   * 
   * @param role
   *          the role
   */
  public RoleImpl(String role) {
    assert role != null;
    this.context = extractContext(role);
    this.identifier = extractIdentifier(role);
    this.ancestors = new HashSet<Role>();
  }

  /**
   * Creates a role in the given context with the specified role identifier.
   * 
   * @param context
   *          the role context
   * @param identifier
   *          the role identifier
   */
  public RoleImpl(String context, String identifier) {
    this.context = context;
    this.identifier = identifier;
    this.ancestors = new HashSet<Role>();
  }

  /**
   * Creates a role in the given context with the specified role identifier that
   * extends the <code>ancestor</code> role.
   * 
   * @param context
   *          the role context
   * @param identifier
   *          the role identifier
   */
  public RoleImpl(String context, String identifier, Role ancestor) {
    this(context, identifier);
    extend(ancestor);
  }

  /**
   * Returns the role identifier.
   * 
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.security.Principal#getName()
   */
  public String getName() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Role#extend(ch.entwine.weblounge.common.security.Role)
   */
  public void extend(Role ancestor) {
    ancestors.add(ancestor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Role#isExtensionOf(ch.entwine.weblounge.common.security.Role)
   */
  public boolean isExtensionOf(Role ancestor) {
    if (ancestors.contains(ancestor))
      return true;

    // Obviously, there is no direct extension and we never looked
    // up this ancestor. Do an indirect ancestor search

    for (Role role : ancestors) {
      if (role.isExtensionOf(ancestor)) {
        // Cache this lookup
        extend(ancestor);
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Role#getExtendedRoles()
   */
  public Role[] getExtendedRoles() {
    return ancestors.toArray(new Role[ancestors.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Role#getClosure()
   */
  public Role[] getClosure() {
    HashSet<Role> roles = new HashSet<Role>();
    Stack<Role> stack = new Stack<Role>();
    stack.push(this);
    while (!stack.empty()) {
      Role r = stack.pop();
      roles.add(r);
      for (Role extendedRole : r.getExtendedRoles()) {
        if (!stack.contains(extendedRole))
          stack.push(extendedRole);
      }
    }
    return roles.toArray(new Role[ancestors.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Role#getContext()
   */
  public String getContext() {
    return context;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Authority#getAuthorityType()
   */
  public String getAuthorityType() {
    return Role.class.getName();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Authority#getAuthorityId()
   */
  public String getAuthorityId() {
    return context + ":" + identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Authority#isAuthorizedBy(ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isAuthorizedBy(Authority authority) {
    if (authority != null && authority instanceof Role) {
      Role r = (Role) authority;
      return this.equals(r) || this.isExtensionOf(r);
    } else if (authority != null && authority.getAuthorityType().equals(Role.class.getName())) {
      String roleId = authority.getAuthorityId();
      Role r = new RoleImpl(roleId);
      return equals(r) || isExtensionOf(r);
    }
    return false;
  }

  /**
   * Sets the role name in the given language.
   * 
   * @param name
   *          the role name
   * @param language
   *          the language
   */
  public void setName(String name, Language language) {
    content.put(language, name);
  }

  /**
   * Returns the context for a role identifier. For example, given the role
   * <code>system:editor</code>, this method will return <code>editor</code>.
   * <p>
   * <b>Note:</b> This method will throw a <code>IllegalArgumentException</code>
   * if the role string does not follow the format <code>context:id</code>.
   * 
   * @param role
   *          the role
   * @return the context part of the role
   */
  protected static String extractContext(String role) {
    assert role != null;
    int divider = role.indexOf(':');
    if (divider <= 0 || divider >= (role.length() - 1))
      throw new IllegalArgumentException("Role must be of the form 'context:id'!");
    return role.substring(0, divider);
  }

  /**
   * Returns the context for a role identifier. For example, given the role
   * <code>system:editor</code>, this method will return <code>editor</code>.
   * <p>
   * <b>Note:</b> This method will throw a <code>IllegalArgumentException</code>
   * if the role string does not follow the format <code>context:id</code>.
   * 
   * @param role
   *          the role
   * @return the context part of the role
   */
  protected static String extractIdentifier(String role) {
    assert role != null;
    int divider = role.indexOf(':');
    if (divider <= 0 || divider >= (role.length() - 1))
      throw new IllegalArgumentException("Role must be of the form 'context:id'!");
    return role.substring(divider + 1);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return context.hashCode() | identifier.hashCode() >> 16;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Role) {
      Role r = (Role) obj;
      return r.getIdentifier().equals(identifier) && r.getContext().equals(context);
    }
    return false;
  }

  /**
   * Returns the string representation of this role in the form
   * <code>&lt;context&gt;:&lt;identifier&gt;</code>.
   * 
   * @return the role's string representation
   */
  public String toString() {
    return context + ":" + identifier;
  }

}