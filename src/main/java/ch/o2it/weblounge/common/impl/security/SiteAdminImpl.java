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

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteAdmin;

/**
 * This class represents the administrator user for a single site.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 1.0
 */

public class SiteAdminImpl extends AuthenticatedUserImpl implements SiteAdmin {

  /**
   * Creates a new SiteAdminImpl user with the <code>administrator</code> role
   * assigned.
   */
  public SiteAdminImpl(String login, String password, String email) {
    super(login);
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
   * @see ch.o2it.weblounge.core.security.WebloungeUserImpl#getName()
   */
  public String getName() {
    if (firstName == null && lastName == null) {
      return getSite().getIdentifier() + " Site Administrator";
    }
    return super.getName();
  }

  /**
   * Returns the preferred language of this person.
   * 
   * @return the person's preferred language
   */
  public Language getLanguage() {
    if (language == null) {
      language = getSite().getDefaultLanguage();
    }
    return language;
  }

  /**
   * Sets the login name for the site administator user. The login name will be
   * set by the site configurator and is read from <code>site.xml</code>.
   * 
   * <b>Note:</b> You are only allowed to set the login name once! Any
   * subsequent attempts to set the login name will result in an
   * <code>IllegalStateException</code>.
   * 
   * @param login
   *          the login name
   */
  public void setLogin(String login) throws IllegalStateException {
    throw new IllegalStateException("The administrator login name must not be changed!");
  }

  /**
   * Sets the password for the site administator user. It will be set by the
   * site configurator and is read from <code>site.xml</code>.
   * 
   * <b>Note:</b> You are only allowed to set the login name once! Any
   * subsequent attempts to set the login name will result in an
   * <code>IllegalStateException</code>.
   * 
   * @param password
   *          the password
   */
  public void setPassword(String password) throws IllegalStateException {
    throw new IllegalStateException("The administrator password must not be changed!");
  }

  /**
   * Returns the login name of the weblounge system administrator user.
   * 
   * @return the admin user's login
   */
  public String getPassword() {
    return password != null ? password : "";
  }

  /**
   * Sets the email address for the site administator user. It will be set by
   * the site configurator and is read from <code>site.xml</code>.
   * 
   * <b>Note:</b> You are only allowed to set the email address once! Any
   * subsequent attempts to set it will result in an
   * <code>IllegalStateException</code>.
   * 
   * @param email
   *          the email address
   */
  public void setEmail(String email) throws IllegalStateException {
    throw new IllegalStateException("The administrator email address must not be changed!");
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