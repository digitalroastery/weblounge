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

import ch.entwine.weblounge.common.security.AccessControlEntry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A tuple of role, action, and whether the combination is to be allowed.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ace", namespace = "ch.entwine.weblounge.security")
@XmlRootElement(name = "ace", namespace = "ch.entwine.weblounge.security")
public final class AccessControlEntryImpl implements AccessControlEntry {

  /** The role */
  private String role = null;

  /** The action */
  private String action = null;

  /** Whether this role is allowed to take this action */
  private boolean allow = false;

  /**
   * Courtesy of JAXB
   */
  public AccessControlEntryImpl() {
  }

  /**
   * Constructs an access control entry for a role, action, and allow tuple
   * 
   * @param role
   *          the role
   * @param action
   *          the action
   * @param allow
   *          Whether this role is allowed to take this action
   */
  public AccessControlEntryImpl(String role, String action, boolean allow) {
    this.role = role;
    this.action = action;
    this.allow = allow;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.AccessControlEntry#getRole()
   */
  public String getRole() {
    return role;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.AccessControlEntry#getAction()
   */
  public String getAction() {
    return action;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.AccessControlEntry#isAllowed()
   */
  public boolean isAllowed() {
    return allow;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AccessControlEntryImpl) {
      AccessControlEntryImpl other = (AccessControlEntryImpl) obj;
      return this.allow == other.allow && this.role.equals(other.role) && this.action.equals(other.action);
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (role + action + Boolean.toString(allow)).hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(role).append(" is ");
    if (!allow)
      sb.append("not ");
    sb.append("allowed ");
    sb.append(action);
    return sb.toString();
  }

}