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

import java.util.Date;

/**
 * This interface extends the common <code>User</code> such that it is able to
 * store additional data for registered users.
 * 
 * TODO Think about how users should be modeled
 */
public interface ExtendedWebloungeUser extends WebloungeUser {

  /**
   * Returns the challenge that is used if the user has forgotten his or her
   * password.
   * 
   * @return the challenge
   * @see #getResponse()
   */
  String getChallenge();

  /**
   * The response that is expected to be given for the challenge.
   * 
   * @return the response
   * @see #getChallenge()
   */
  String getResponse();

  /**
   * Returns the person's title or <code>null</code> if no title was provided.
   * 
   * @return the title
   */
  String getSalutation();

  /**
   * Returns the person's street or <code>null</code> if no street was provided.
   * 
   * @return the street
   */
  String getStreet();

  /**
   * Returns the person's zip or <code>null</code> if no zip was provided.
   * 
   * @return the city
   */
  String getZip();

  /**
   * Returns the person's city or <code>null</code> if no city was provided.
   * 
   * @return the city
   */
  String getCity();

  /**
   * Returns the person's country or <code>null</code> if no country was
   * provided.
   * 
   * @return the country
   */
  String getCountry();

  /**
   * Returns the person's birthday or <code>null</code> if no birthday was
   * provided.
   * 
   * @return the birthday
   */
  Date getBirthday();

  /**
   * Returns the person's phone number or <code>null</code> if no no phone
   * number was provided.
   * 
   * @return the phone number
   */
  String getPhone();

  /**
   * Returns the person's mobile phone number or <code>null</code> if no no
   * mobile phone number was provided.
   * 
   * @return the mobile phone number
   */
  String getMobilePhone();

}