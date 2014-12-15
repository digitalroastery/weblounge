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

import ch.entwine.weblounge.common.security.Authority;

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
   * @see ch.entwine.weblounge.common.security.Authority#getAuthorityType()
   */
  public String getAuthorityType() {
    return type;
  }

  /**
   * Returns the authorization identifier.
   * 
   * @see ch.entwine.weblounge.common.security.Authority#getAuthorityId()
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
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Authority#matches(ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  public boolean matches(Authority authority) {
    if (ANY_TYPE.equals(type) && ANY_AUTHORITY.equals(id))
      return true;
    if (ANY_TYPE.equals(authority.getAuthorityType()) && ANY_AUTHORITY.equals(authority.getAuthorityId()))
      return true;
    return type.equals(authority.getAuthorityType()) && id.equals(authority.getAuthorityId());
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Authority#implies(ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  public boolean implies(Authority authority) {
    return false;
  }

  /**
   * Returns <code>true<code> if the authority matches <code>o</code> with
   * respect to type and identifier.
   */
  public boolean equals(Object o) {
    if (!(o instanceof Authority))
      return false;
    Authority authority = (Authority) o;
    return type.equals(authority.getAuthorityType()) && id.equals(authority.getAuthorityId());
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return type + ":" + id;
  }

}