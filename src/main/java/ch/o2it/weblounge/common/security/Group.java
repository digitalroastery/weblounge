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

import ch.o2it.weblounge.common.language.Localizable;

/**
 * Methods for a group. A group has zero or more members and may tell if a given
 * user or group is member of this group.
 */
public interface Group extends GroupMember, RoleOwner, Localizable {

  /**
   * Returns the group context.
   * 
   * @return the group context
   */
  String getContext();

  /**
   * Returns the group identifier.
   * 
   * @return the group identifier
   */
  String getIdentifier();

  /**
   * Adds <code>member</code> to this group.
   * 
   * @param member
   *          the member to add
   */
  void addMember(GroupMember member);

  /**
   * Removes <code>member</code> from this group.
   * 
   * @param member
   *          the member to remove
   */
  void removeMember(GroupMember member);

  /**
   * Returns <code>true</code> if this <code>member</code> is a member of this
   * group.
   * 
   * @param member
   *          the meber to test for membership
   * @return <code>true</code> if member is part of this group
   */
  boolean hasMember(GroupMember member);

  /**
   * Returns all members of this group.
   * 
   * @return all group members
   */
  GroupMember[] getMembers();

}