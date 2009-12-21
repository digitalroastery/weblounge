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
 * A group member may be member of zero or more groups and can tell whether it
 * has certain memberships or not.
 */
public interface GroupMember {

  /**
   * Registers a membership with <code>group</code>.
   * 
   * @param group
   *          the group that has been joined
   */
  void addMembership(Group group);

  /**
   * Removes the membership with <code>group</code>.
   * 
   * @param group
   *          the group that has been left
   */
  void removeMembership(Group group);

  /**
   * Returns <code>true</code> if this member itself is a member of the given
   * group.
   * 
   * @param group
   *          the group to test
   * @return
   *         <code>true<code> if this group member is member of <code>group</code>
   */
  boolean isMemberOf(Group group);

  /**
   * Returns all groups that this member is a member of. This method also
   * returns all memberships of the returned group, so in the end, this is a
   * closure over all memberships of the group member.
   * 
   * @return all groups and subgroups that have this member as a member
   */
  Group[] getGroupClosure();

  /**
   * Returns all groups that this member is a member of.
   * 
   * @return all groups that have this member as a member
   */
  Group[] getGroups();

}