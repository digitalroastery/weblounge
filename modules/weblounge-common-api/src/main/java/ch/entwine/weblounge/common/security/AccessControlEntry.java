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

/**
 * A rule that defines whether a certain roles may perform an actions.
 */
public interface AccessControlEntry {

  /**
   * Returns the role identifier.
   * 
   * @return the role
   */
  String getRole();

  /**
   * Returns the action.
   * 
   * @return the action
   */
  String getAction();

  /**
   * Returns <code>true</code> if the role returned by {@link #getRole()} is
   * allowed the action defined by {@link #getAction()}.
   * 
   * @return <code>true</code> if the action is allowed by the role
   */
  boolean isAllowed();

}