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
import ch.o2it.weblounge.common.security.AuthorizationProvider;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

/**
 * The system authorization provider is used to create objects from the system
 * configuration that can be authorized by weblounge. For example in a module
 * file, the following security constraints may be found:
 * 
 * <pre>
 * 		&lt;security&gt;
 * 			&lt;permission id=&quot;system:read&quot; evaluate=&quot;allow,deny&quot;&gt;
 * 				&lt;allow type=&quot;ch.o2it.weblounge.api.security.Authorization&quot;&gt;main&lt;/allow&gt;
 * 				&lt;allow type=&quot;ch.o2it.weblounge.api.security.Role&quot;&gt;system:editor&lt;/allow&gt;
 * 				&lt;allow type=&quot;ch.o2it.weblounge.api.security.User&quot;&gt;tobias.wunden&lt;/allow&gt;
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
 * <p>
 * This special provider implementation can handle user, role and composer
 * authorizations.
 */
public class DefaultAuthorizationProvider implements AuthorizationProvider {

  /** The site */
  private Site site_;

  /** The role type */
  private final static String TYPE_ROLE = Role.class.getName();

  /** The user type */
  private final static String TYPE_USER = User.class.getName();

  public DefaultAuthorizationProvider(Site site) {
    site_ = site;
  }

  /**
   * Returns the authorization for the given type and object or
   * <code>null</code> if <code>type</code> cannot be handled by this provider.
   * 
   * @param type
   *          the authorization type
   * @param id
   *          the authorization id
   * @return the authorized object
   */
  public Authority getAuthorization(String type, String id) {
    assert type != null;
    assert id != null;
    if (type.equals(TYPE_ROLE)) {
      site_.getRole(RoleImpl.extractContext(id), RoleImpl.extractIdentifier(id));
    } else if (type.equals(TYPE_USER)) {
      return site_.getUser(id);
    }
    return null;
  }

}