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

import ch.o2it.weblounge.common.security.Authority;

/**
 * General implementation for an authority.
 */
public class AuthorityImpl implements Authority {

  /** The authorization type */
  protected String type = null;

  /** The authorization id */
  protected String id = null;

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
   * Returns the authorization identifier.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#getAuthorityId()
   */
  public String getAuthorityId() {
    return id;
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
   * Returns <code>true<code> if the authority matches <code>o</code> with
   * respect to type and identifier.
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof Authority) {
      return isAuthorizedBy((Authority) o);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This default implementation only authorizes authorities with matching type
   * and id.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#isAuthorizedBy(ch.o2it.weblounge.common.security.Authority)
   */
  public boolean isAuthorizedBy(Authority authority) {
    if (authority != null) {
      return type.equals(authority.getAuthorityType()) && id.equals(authority.getAuthorityId());
    }
    return false;
  }

}