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
 * The authorization provider is used to create objects from the system
 * configuration that can be authorized by weblounge. For example in a module
 * file, the following security constraints may be found:
 * 
 * <pre>
 * 		&lt;security&gt;
 * 			&lt;permission id=&quot;system:read&quot; evaluate=&quot;allow,deny&quot;&gt;
 * 				&lt;allow type=&quot;ch.entwine.weblounge.api.security.Authorization&quot;&gt;main&lt;/allow&gt;
 * 				&lt;allow type=&quot;ch.entwine.weblounge.api.security.Role&quot;&gt;system:editor&lt;/allow&gt;
 * 				&lt;allow type=&quot;ch.entwine.weblounge.api.security.User&quot;&gt;tobias.wunden&lt;/allow&gt;
 * 				&lt;deny&gt;all&lt;/deny&gt;
 * 			&lt;/permission&gt;
 * 		&lt;/security&gt;
 * </pre>
 * 
 * For each <code>allow</code> line, weblounge will try to get a suitable
 * provider to create the corresponding Authorization, e. g. the system editor
 * role. Later on, if this object is being accessed by a user, the system knows
 * that it has to ask for the <code>editor</code> role in order to grant read
 * access.
 */
public interface AuthorizationProvider {

  /**
   * Returns the authorization for the given type and object. For example,
   * requesting authorization for the type
   * <code>ch.entwine.weblounge.api.security.Role</code> and id
   * <code>system:editor</code>, then the provider will return the system role
   * <code>editor</code>.
   * 
   * @param type
   *          the authorization type
   * @param id
   *          the authorization id
   * @return the authorized object
   */
  Authority getAuthorization(String type, String id);

}