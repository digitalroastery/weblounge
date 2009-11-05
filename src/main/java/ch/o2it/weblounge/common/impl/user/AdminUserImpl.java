/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.common.impl.user;

/**
 * TODO: Comment AdminUserImpl
 */
public class AdminUserImpl extends AuthenticatedUserImpl {

  /** the first name of the person */
  protected String firstName = null;

  /** the family name of the person */
  protected String lastName = null;

  /** the person's email address */
  protected String email = null;

  /**
   * @param login
   */
  public AdminUserImpl(String login) {
    super(login);
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
   * Returns the name of this user. If possible, the value returned consists of
   * type <first name><last name>.
   * 
   * @returns the full user name
   */
  public String getName() {
    String name = "";
    if (getFirstName() != null && !getFirstName().trim().equals("")) {
      if (getLastName() != null && !getLastName().trim().equals("")) {
        name = getFirstName() + " " + getLastName();
      } else {
        name = getFirstName();
      }
    } else if (getLastName() != null && !getLastName().trim().equals("")) {
      name = getLastName();
    } else {
      name = getLogin();
    }
    return name;
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

}
