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

package ch.entwine.weblounge.common.security;

import ch.entwine.weblounge.common.language.Language;

import java.util.Date;

/**
 * This class represents a user that has been authenticated and identified by
 * weblounge.
 */
public interface WebloungeUser extends User {

  /**
   * Sets the person's email.
   * 
   * @param email
   *          the email address
   */
  void setEmail(String email);

  /**
   * Sets this person's first name.
   * 
   * @param firstname
   *          the first name
   */
  void setFirstName(String firstname);

  /**
   * Returns the first name of this person.
   * 
   * @return the person's first name
   */
  String getFirstName();

  /**
   * Sets this person's last name.
   * 
   * @param lastname
   *          the last name
   */
  void setLastName(String lastname);

  /**
   * Returns the last name of this person.
   * 
   * @return the person's last name
   */
  String getLastName();

  /**
   * Returns the email address of this person.
   * 
   * @return the person's email address
   */
  String getEmail();

  /**
   * Sets the person's preferred language.
   * 
   * @param language
   *          the preferred language
   */
  void setLanguage(Language language);

  /**
   * Returns the preferred language of this person.
   * 
   * @return the person's preferred language
   */
  Language getLanguage();

  /**
   * Sets the person's initials.
   * 
   * @param initials
   *          the person's initials
   */
  void setInitials(String initials);

  /**
   * Returns the short version of the persons name.
   * 
   * @return the persons initials
   */
  String getInitials();

  /**
   * Sets the enabled flag. Set it to <code>true</code> to enable the login.
   * 
   * @param enabled
   *          <code>true</code> to enable this login
   */
  void setEnabled(boolean enabled);

  /**
   * Returns <code>true</code> if the user is enabled, <code>false</code>
   * otherwise.
   * 
   * @return <code>true</code> for enabled users
   */
  boolean isEnabled();

  /**
   * Returns <code>true</code> if the user can log in. This is the case only if
   * both the user is enabled and has a password set.
   * 
   * @return <code>true</code> if the user can log in
   */
  boolean canLogin();

  /**
   * Property to set on this user.
   * <p>
   * Well known property object types, such as {@link org.w3c.dom.Node} will be
   * serialized and deserialized with care. Others will be serialized using
   * {@link Object#toString()}.
   * 
   * @param name
   *          the property name
   * @param value
   *          the property value
   */
  void setProperty(String name, Object value);

  /**
   * Returns the deserialized property or <code>null</code> if no such property
   * was defined.
   * 
   * @param name
   *          the property name
   * @return the property value
   */
  Object getProperty(String name);

  /**
   * Removes and returns the deserialized property.
   * 
   * @param name
   *          the property name
   * @return the property value
   */
  Object removeProperty(String name);

  /**
   * Sets the last login date.
   * 
   * @param date
   *          the login date
   * @param src
   *          the login source
   */
  void setLastLogin(Date date, String src);

  /**
   * Returns the data where the user logged in for the last time.
   * 
   * @return the last login
   */
  Date getLastLogin();

  /**
   * Returns the last login source. The source can be either an ip address or a
   * host name.
   * 
   * @return the source of the last login
   */
  String getLastLoginFrom();

}