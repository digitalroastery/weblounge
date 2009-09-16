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

import java.util.HashSet;

/**
 * A <code>AuthoritySet</code> contains an aribtrary number of authorities.
 * 
 * @author Tobias Wunden
 * @version 1.0 14 Jun 25 2005
 * @since WebLounge 2.0
 */

public class AuthoritySet extends HashSet<Authority> {

  /** The serial version id */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an empty roleset that may be used to group an aribtrary set of
   * roles for the specified user.
   */
  public AuthoritySet(Authority authority) {
    if (authority != null) {
      super.add(authority);
    }
  }

  /**
   * Creates an empty roleset that may be used to group an aribtrary set of
   * roles.
   */
  public AuthoritySet(Authority[] authorities) {
    if (authorities != null) {
      for (int i = 0; i < authorities.length; add(authorities[i++]))
        ;
    }
  }

}