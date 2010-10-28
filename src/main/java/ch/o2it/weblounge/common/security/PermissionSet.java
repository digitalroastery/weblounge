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

/**
 * This interface defines the methods to deal with a set of permissions.
 */
public interface PermissionSet {

  /** Value identifying conditions that have to match all */
  int MATCH_ALL = 0;

  /** Value identifying conditions where one of the have to be matched */
  int MATCH_SOME = 1;

  /**
   * Returns the permissions that have to be matched exactly.
   * 
   * @return the permissions to be exactly matched
   */
  Permission[] all();

  /**
   * Returns the permissions that need to be matched one at least.
   * 
   * @return the permissions to match one
   */
  Permission[] some();

}