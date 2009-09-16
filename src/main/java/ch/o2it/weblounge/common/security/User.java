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

package ch.o2it.weblounge.common.security;

import java.security.Principal;

import ch.o2it.weblounge.common.language.Language;

/**
 * Defines a user in the system.
 */
public interface User extends GroupMember, RoleOwner, Authority, Principal {

  /** The user session identifier */
  String ID = "user";

  /**
   * Returns the first name of this person.
   * 
   * @return the person's first name
   */
  String getFirstName();

  /**
   * Returns the last name of this person.
   * 
   * @return the person's last name
   */
  String getLastName();

  /**
   * Returns the name of this user. If possible, the value returned consists of
   * type <first name><last name>.
   * 
   * @returns the full user name
   */
  String getName();

  /**
   * Returns the name of this user. If possible, the value returned consists of
   * type <tt>&lt;first name&gt; &lt;last name&gt;</tt> or
   * <tt>&lt;last name&gt;, &lt;first name&gt;</tt>, depending on the value of
   * <code>reversed</code>.
   * 
   * @param reversed
   *          <code>true</code> to return <tt>Lastname, Firstname</tt>
   * @returns the full user name
   */
  String getName(boolean reversed);

  /**
   * Returns the login name of this user.
   * 
   * @return the username
   */
  String getLogin();

  /**
   * Returns the email address of this person.
   * 
   * @return the person's email address
   */
  String getEmail();

  /**
   * Returns the preferred language of this person.
   * 
   * @return the person's preferred language
   */
  Language getLanguage();

  /**
   * Returns the short version of the persons name.
   * 
   * @return the persons initials
   */
  String getInitials();

}