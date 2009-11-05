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

package ch.o2it.weblounge.common.impl.user;

import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteAdmin;

/**
 * This class represents the administrator user for a single site.
 */
public class SiteAdminImpl extends AdminUserImpl implements SiteAdmin {

  /**
   * Creates a new SiteAdminImpl user with the <code>administrator</code> role
   * assigned.
   */
  public SiteAdminImpl(String login, byte[] password, String email) {
    super(login);
    // TODO: Pass to superconstructor
    this.password = password;
    this.email = email;
    assignRole(SystemRole.SITEADMIN);
  }

  /**
   * Initializes this user.
   * 
   * @param site
   *          the associated site
   */
  public void init(Site site) {
    this.site = site;
  }

  /**
   * Returns the full user name.
   * 
   * @return the name
   * @see ch.o2it.weblounge.common.impl.user.core.security.WebloungeUserImpl#getName()
   */
  public String getName() {
    if (firstName == null && lastName == null) {
      return getSite().getIdentifier() + " Site Administrator";
    }
    return super.getName();
  }

  /**
   * Returns <code>true</code> if <code>authority</code> represents the same
   * user.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#equals(ch.o2it.weblounge.common.security.Authority)
   */
  public boolean equals(Authority authority) {
    if (authority != null && authority instanceof SiteAdminImpl) {
      return ((SiteAdminImpl) authority).getSite() == getSite();
    }
    return false;
  }

}