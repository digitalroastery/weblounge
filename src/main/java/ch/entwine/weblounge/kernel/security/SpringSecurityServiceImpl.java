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

package ch.entwine.weblounge.kernel.security;

import ch.entwine.weblounge.common.impl.security.WebloungeAdminImpl;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

/**
 * A Spring Security implementation of {@link SecurityService}.
 */
public class SpringSecurityServiceImpl implements SecurityService {


  /** Holds the site associated with the current thread */
  private static final ThreadLocal<Site> siteHolder = new ThreadLocal<Site>();

  /** Holds the user associated with the current thread */
  private static final ThreadLocal<User> userHolder = new ThreadLocal<User>();

  /** The default system administrator */
  private User systemAdmin = new WebloungeAdminImpl("admin");

  /** Whether the system administrator has configured a no-security policy */
  private boolean enabled = true;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#getSite()
   */
  public Site getSite() {
    return siteHolder.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#setSite(ch.entwine.weblounge.common.site.Site)
   */
  public void setSite(Site site) {
    siteHolder.set(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#setUser(ch.entwine.weblounge.common.security.User)
   */
  public void setUser(User user) {
    userHolder.set(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#getUser()
   */
  public User getUser() {
    if (enabled)
      return userHolder.get();
    else
      return systemAdmin;
  }

  /**
   * Whether the user has configured a no-security policy.
   * 
   * @param enabled
   *          <code>true</code> if there is a security policy in place
   */
  void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.SecurityService#isEnabled()
   */
  public boolean isEnabled() {
    return this.enabled;
  }

}
