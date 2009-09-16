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

import ch.o2it.weblounge.common.impl.language.MultilingualTreeSet;
import ch.o2it.weblounge.common.security.Role;

/**
 * A <code>Roleset</code> contains an aribtrary number of roles.
 * 
 * @author Tobias Wunden
 * @version 1.0 Thu Jul 25 2002
 * @since WebLounge 1.0
 */

public class RoleSet extends MultilingualTreeSet<Role> {

  /**
   * Creates an empty roleset that may be used to group an aribtrary set of
   * roles for the specified user.
   */
  public RoleSet(Role role) {
    if (role != null) {
      super.add(role);
    }
  }

  /**
   * Creates an empty roleset that may be used to group an aribtrary set of
   * roles.
   */
  public RoleSet(Role[] roles) {
    if (roles != null) {
      for (int i = 0; i < roles.length; add(roles[i++]))
        ;
    }
  }

}