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

package ch.o2it.weblounge.common.security;

/**
 * This interface is used by objects that are used to access
 * <code>secured</code> objects. For example, a user is authorized with respect
 * to a given role if it actually owns this role.
 */
public interface Authority {

  /**
   * Returns the authority type, e. g.
   * <code>ch.o2it.weblounge.api.security.Role</code> for a role authorization.
   * 
   * @return the authority type
   */
  String getAuthorityType();

  /**
   * Returns the authority identifier.
   * 
   * @return the authority identifier
   */
  String getAuthorityId();

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
  boolean equals(Authority authority);

}