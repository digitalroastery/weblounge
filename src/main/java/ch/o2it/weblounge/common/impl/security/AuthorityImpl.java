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
 * General implementation for an authority.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class AuthorityImpl implements Authority {

  /** The authorization type */
  protected String type;

  /** The authorization id */
  protected String id;

  /**
   * Creates a new authorization with the given type and id.
   * 
   * @param id
   *          the authorization identifier
   * @param type
   *          the authorization type
   */
  public AuthorityImpl(String type, String id) {
    this.type = type;
    this.id = id;
  }

  /**
   * Returns the authorization type.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#getAuthorityType()
   */
  public String getAuthorityType() {
    return type;
  }

  /**
   * Returns the authority's hash code.
   * 
   * @return the hash code
   */
  public int hashCode() {
    return type.hashCode() >> 16 | id.hashCode();
  }

  /**
   * Returns <code>true<code> if the authoritiy matches <code>o</code> with
   * respect to type and identifier.
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof Authority) {
      return equals((Authority) o);
    }
    return false;
  }

  /**
   * Returns <code>true</code> if this authority and <code>authority</code>
   * match by means of the respective authority. Like this, a <code>Role</code>
   * may check if <code>authority</code> is of type role and if is an inherited
   * role, which would be possible by calling the equals() method of
   * <code>Role</code> with the authority as the parameter because the
   * <code>instanceof</code> test will usually fail.
   * 
   * @param authority
   *          the authority to test
   * @return <code>true</code> if the authorities match
   */
  public boolean equals(Authority authority) {
    if (authority != null) {
      return type.equals(authority.getAuthorityType()) && id.equals(authority.getAuthorityId());
    }
    return false;
  }

  /**
   * Returns the authorization identifier.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#getAuthorityId()
   */
  public String getAuthorityId() {
    return id;
  }

}