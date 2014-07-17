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
 * This interface defines the methods to deal with a set of actions.
 */
public interface ActionSet {

  /** Value identifying conditions that have to match all */
  int MATCH_ALL = 0;

  /** Value identifying conditions where one of the have to be matched */
  int MATCH_SOME = 1;

  /**
   * Returns the actions all of which have to be matched.
   * 
   * @return the actions to match
   */
  Action[] all();

  /**
   * Returns the actions that require one or more to be matched.
   * 
   * @return the actions to match
   */
  Action[] some();

}