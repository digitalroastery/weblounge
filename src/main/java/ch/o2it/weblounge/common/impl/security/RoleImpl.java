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

import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Role;

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
  private String identifier_ = null;

  /** The role context */
  private String context_ = null;

  /** Roles that are extended by this role */
  private Set<Role> ancestors_ = null;

  /**
   * Creates a new role from the parameter context::id.
   * 
   * @param role
   *          the role
   */
  public RoleImpl(String role) {
    assert role != null;
    context_ = extractContext(role);
    identifier_ = extractIdentifier(role);
    ancestors_ = new HashSet<Role>();
  }

  /**
   * Creates a role in the given context with the specified role identifier.
   * @param context
   *          the role context
   * @param identifier
   *          the role identifier
   */
  public RoleImpl(String context, String identifier) {
    context_ = context;
    identifier_ = identifier;
    ancestors_ = new HashSet<Role>();
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
    return identifier_;
  }

  /**
   * Makes this role an extension of <code>ancestor</code>.
   * 
   * @param ancestor
   *          the role to extend
   */
  public void extend(Role ancestor) {
    ancestors_.add(ancestor);
  }

  /**
   * Returns <code>true</code> if this role is a direct or indirect extension of
   * <code>ancestor</code>.
   * 
   * @param ancestor
   *          the extension in question
   * @return <code>true</code> if this role extends <code>ancestor</code>
   */
  public boolean isExtensionOf(Role ancestor) {
    if (ancestors_.contains(ancestor))
      return true;

    // Obviously, there is no direct extension and we never looked
    // up this ancestor. Do an indirect ancestor search

    for (Role role : ancestors_) {
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
   * @see ch.o2it.weblounge.common.security.Role#getExtendedRoles()
   */
  public Role[] getExtendedRoles() {
    return ancestors_.toArray(new Role[ancestors_.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Role#getClosure()
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
    return roles.toArray(new Role[ancestors_.size()]);
  }

  /**
   * Returns the role context.
   * 
   * @return the role context
   * @see ch.o2it.weblounge.common.security.Role#getContext()
   */
  public String getContext() {
    return context_;
  }

  /**
   * Returns the authorization type, e. g.
   * <code>ch.o2it.weblounge.api.security.Role</code> for a role authorization.
   * 
   * @return the authorization type
   */
  public String getAuthorityType() {
    return Role.class.getName();
  }

  /**
   * Returns the authorization identifier.
   * 
   * @return the authorization identifier
   */
  public String getAuthorityId() {
    return context_ + ":" + identifier_;
  }

  /**
   * Returns <code>true</code> if this authority and <code>authority</code>
   * match by means of the respective authority. This is the case if
   * <code>authority</code> represents the same role or
   * 
   * @param authority
   *          the authority to test
   * @return <code>true</code> if the authorities match
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
   * Returns the hash code for this role object.
   * 
   * @return the hash code
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return context_.hashCode() | identifier_.hashCode() >> 16; 
  }

  /**
   * Returns <code>true</code> if <code>obj</code> is of type <code>Role</code>
   * object literally representing the same instance than this one.
   * 
   * @param obj
   *          the object to test for equality
   * @return <code>true</code> if <code>obj</code> represents the same
   *         <code>Role</code>
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Role) {
      Role r = (Role) obj;
      return r.getIdentifier().equals(identifier_) && r.getContext().equals(context_);
    }
    return false;
  }

  /**
   * Returns the role identifier.
   * 
   * @return a role description
   */
  public String toString() {
    return context_ + ":" + identifier_;
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
  public static String extractContext(String role) {
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
  public static String extractIdentifier(String role) {
    assert role != null;
    int divider = role.indexOf(':');
    if (divider <= 0 || divider >= (role.length() - 1))
      throw new IllegalArgumentException("Role must be of the form 'context:id'!");
    return role.substring(divider + 1);
  }

}