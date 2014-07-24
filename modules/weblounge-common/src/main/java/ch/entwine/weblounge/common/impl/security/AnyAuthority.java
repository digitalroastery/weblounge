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

/**
 * Special implementation for an authority that, when compared using
 * {@link #equals(Object)}, will return for any object of type
 * {@link ch.entwine.weblounge.common.security.Authority} .
 */
public final class AnyAuthority extends AuthorityImpl {

  /**
   * Creates a new authorization with the given type and id.
   * 
   * @param id
   *          the authorization identifier
   * @param type
   *          the authorization type
   */
  public AnyAuthority() {
    super(ANY_TYPE, ANY_AUTHORITY);
  }

}