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
import ch.o2it.weblounge.common.security.LoginContext;
import ch.o2it.weblounge.common.site.Site;

public class AuthenticatedUserImpl extends AbstractAuthenticatedUser {

  /** the first name of the person */
  protected String firstName;

  /** the family name of the person */
  protected String lastName;

  /** the person's email address */
  protected String email;

  /** the preferred language */
  protected Language language;

  /**
   * Creates a user.
   * 
   * @param site
   *          the site where the user logged in
   */
  public AuthenticatedUserImpl(String login, LoginContext context, Site site) {
    super(login, context, site);
  }

  /**
   * Creates a user.
   * 
   * @param site
   *          the site where the user logged in
   */
  AuthenticatedUserImpl(String login, Site site) {
    this(login, null, site);
  }

  /**
   * Creates a new authenticated user.
   * 
   * @param login
   *          the login name
   */
  protected AuthenticatedUserImpl(String login) {
    this(login, null, null);
  }

  /**
   * Sets this person's first name.
   * 
   * @param firstname
   *          the first name
   */
  public void setFirstName(String firstname) {
    this.firstName = firstname;
  }

  /**
   * Returns the first name of this person.
   * 
   * @return the person's first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets this person's last name.
   * 
   * @param lastname
   *          the last name
   */
  public void setLastName(String lastname) {
    this.lastName = lastname;
  }

  /**
   * Returns the last name of this person.
   * 
   * @return the person's last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the person's email.
   * 
   * @param email
   *          the email address
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the email address of this person.
   * 
   * @return the person's email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the person's preferred language.
   * 
   * @param language
   *          the preferred language
   */
  public void setLanguage(Language language) {
    this.language = language;
  }

  /**
   * Returns the preferred language of this person.
   * 
   * @return the person's preferred language
   */
  public Language getLanguage() {
    return language;
  }

}