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
import ch.o2it.weblounge.common.user.SiteAdmin;

/**
 * This class represents the administrator user for a single site.
 */
public class SiteAdminImpl extends WebloungeUserImpl implements SiteAdmin {
  
  /**
   * Creates a new SiteAdminImpl user with the <code>administrator</code> role
   * assigned.
   * 
   * @param login
   *          the login name
   * @param site
   *          the associated site
   */
  public SiteAdminImpl(String login, Site site) {
    super(login, SystemRealm, site);
    assignRole(SystemRole.SITEADMIN);
    setName("Site Administrator (" + site.getIdentifier() + ")");
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

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.impl.user.UserImpl#setRealm(java.lang.String)
   */
  @Override
  public void setRealm(String realm) {
    throw new UnsupportedOperationException("The admin user realm cannot be changed");
  }
  
}