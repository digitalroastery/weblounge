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

package ch.entwine.weblounge.common.security;

/**
 * This interface is used by objects that are used to access that implement the
 * {@link Securable} interface.
 * <p>
 * In order to be able to restrict access to an object to either roles, a group
 * of users or even a single user, all of these objects implement this
 * interface, thereby allowing for one signature on the secured objects.
 * <p>
 * The implementations then need to make sure they can recognize the authority
 * and resolve the permissions that were applied. For example, if a secured
 * object <code>o</code> is told to only allow access to {@link Role}
 * <code>r</code>, and then <code>o</code> is being accessed by
 * {@link ch.entwine.weblounge.common.security.User} <code>u</code> who happens to have
 * role <code>r</code> assigned, access should be granted.
 * <p>
 * Known authorities are:
 * <ul>
 * <li><code>ch.entwine.weblounge.common.security.Role</code></li>
 * <li><code>ch.entwine.weblounge.common.security.Group</code></li>
 * <li><code>ch.entwine.weblounge.common.user.User</code></li>
 * </ul>
 */
public interface Authority {

  /**
   * Returns the authority type, e. g.
   * <code>ch.entwine.weblounge.common.security.Role</code> for a {@link Role}
   * authorization.
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
   * match by means of the respective authority.
   * <p>
   * Like this, a {@link Securable} that is configured to allow access to
   * {@link RoleOwner}s owning {@link Role} <code>r</code> only may call
   * <code>isAuthorizedBy(r)</code> on a {@link User} that wants to access the
   * object. The method will return <code>true</code> if the user owns the role
   * or belongs to a group that does.
   * 
   * @param authority
   *          the authority to test
   * @return <code>true</code> if the authorities match
   */
  boolean isAuthorizedBy(Authority authority);

}