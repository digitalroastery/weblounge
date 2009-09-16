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

import org.w3c.dom.Node;

/**
 * TODO: Comment WebloungeUser
 */
public interface WebloungeUser extends AuthenticatedUser {

  /**
   * Returns <code>true</code> if the user is enabled, <code>false</code>
   * otherwise.
   * 
   * @return <code>true</code> for enabled users
   */
  boolean isEnabled();

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
  String getLastLoginSource();

  /**
   * Returns an xml representation of this user.
   * 
   * @return the user as an xml document fragment
   */
  Node toXml();

}