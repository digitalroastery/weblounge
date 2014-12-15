/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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
 * An access rule relates an authority (e. g. a user or a role) to a given
 * action and in addition defines whether that authority is allowed or denied
 * that action.
 */
public interface AccessRule {

  /**
   * Returns the authority.
   * 
   * @return the authority
   */
  Authority getAuthority();

  /**
   * Returns the action.
   * 
   * @return the action
   */
  Action getAction();

  /**
   * Returns the rule.
   * 
   * @return the rule
   */
  Rule getRule();

}
