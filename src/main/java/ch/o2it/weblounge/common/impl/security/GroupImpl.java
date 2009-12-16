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

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.GroupMember;
import ch.o2it.weblounge.common.security.Role;

import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of a weblounge group.
 */
public class GroupImpl extends LocalizableContent<String> implements Group {

  /** The group context */
  private String context_ = null;

  /** The group identifier */
  private String id_ = null;

  /** The group members */
  final Set<GroupMember> members = new HashSet<GroupMember>();

  /** Group memberships */
  final Set<Group> groups = new HashSet<Group>();

  /** the roles directly owned by this group */
  final Set<Role> roles = new HashSet<Role>();

  /**
   * Creates a new group with the given context and identifier.
   * @param id
   *          the group identifier
   * @param context
   *          the group context
   */
  public GroupImpl(String id, String context) {
    context_ = context;
    id_ = id;
  }

  /**
   * Returns the group context.
   * 
   * @return the group context
   */
  public String getContext() {
    return context_;
  }

  /**
   * Returns the group identifier.
   * 
   * @return the group identifier
   */
  public String getIdentifier() {
    return id_;
  }

  /**
   * Adds <code>member</code> to this group and tells the member that is may
   * register this membership by calling <code>addMembership()</code>.
   * 
   * @param member
   *          the member to add
   */
  public void addMember(GroupMember member) {
    if (!members.contains(member)) {
      synchronized (members) {
        members.add(member);
      }
    }
    if (!member.isMemberOf(this)) {
      member.addMembership(this);
    }
  }

  /**
   * Removes <code>member</code> from this group and tells the member to remove
   * the membership by calling <code>removeMembership()</code>.
   * 
   * @param member
   *          the member to remove
   */
  public void removeMember(GroupMember member) {
    if (members == null)
      return;
    if (members.contains(member)) {
      synchronized (members) {
        members.remove(member);
      }
    }
    if (member.isMemberOf(this)) {
      member.removeMembership(this);
    }
  }

  /**
   * Returns <code>true</code> if this <code>member</code> is a member of this
   * group.
   * 
   * @param member
   *          the member to test for membership
   * @return <code>true</code> if member is part of this group
   */
  public boolean hasMember(GroupMember member) {
    return members != null && members.contains(member);
  }

  /**
   * Returns all members of this group.
   * 
   * @return all group members
   */
  public GroupMember[] getMembers() {
    if (members == null) {
      return new GroupMember[] {};
    }
    return members.toArray(new GroupMember[members.size()]);
  }

  /**
   * Registers a membership with <code>group</code>.
   * 
   * @param group
   *          the group that has been joined
   */
  public void addMembership(Group group) {
    synchronized (groups) {
      groups.add(group);
    }
  }

  /**
   * Removes the membership with <code>group</code>.
   * 
   * @param group
   *          the group that has been left
   */
  public void removeMembership(Group group) {
    if (groups != null) {
      synchronized (groups) {
        groups.remove(group);
      }
    }
  }

  /**
   * Returns an array of all groups that have this user as a member.
   * 
   * @see ch.o2it.weblounge.common.security.GroupMember#getGroupClosure()
   */
  public Group[] getGroupClosure() {
    Set<Group> result = new HashSet<Group>();
    if (groups != null) {
      result.addAll(groups);
      for (Group g : groups) {
        for (Group g2 : g.getGroupClosure())
          result.add(g2);
      }
    }
    return result.toArray(new Group[result.size()]);
  }

  /**
   * @see ch.o2it.weblounge.common.security.GroupMember#getGroups()
   */
  public Group[] getGroups() {
    Set<Group> result = new HashSet<Group>();
    if (groups != null) {
      result.addAll(groups);
    }
    return result.toArray(new Group[result.size()]);
  }

  /**
   * Returns <code>true</code> if the user is member of the given group,
   * <code>false</code> otherwise.
   * 
   * @param group
   *          the group to test for membership
   * @return <code>true</code> if the user is member of this group
   */
  public boolean isMemberOf(Group group) {
    return groups != null && groups.contains(group);
  }

  /**
   * Removes <code>role</code> from the list of roles assigned to this user.
   * 
   * @param role
   *          the role to remove
   */
  public void assignRole(Role role) {
    synchronized (roles) {
      roles.add(role);
    }
  }

  /**
   * Removes <code>role</code> from the list of roles assigned to this user.
   * 
   * @param role
   *          the role to remove
   */
  public void unassignRole(Role role) {
    if (roles != null) {
      synchronized (roles) {
        roles.remove(role);
      }
    }
  }

  /**
   * Returns all roles that this user owns directly.
   * 
   * @return the roles that this user owns
   */
  public Role[] getRoleClosure() {
    Set<Role> roles = new HashSet<Role>();
    if (this.roles != null)
      roles.addAll(this.roles);
    if (this.groups != null) {
      for (Group g : groups) {
        for (Role r : g.getRoleClosure())
          roles.add(r);
      }
    }
    return roles.toArray(new Role[roles.size()]);
  }

  /**
   * Returns all roles that this user directly owns.
   * 
   * @return the user's primary roles
   */
  public Role[] getRoles() {
    Set<Role> roles = new HashSet<Role>();
    if (this.roles != null)
      roles.addAll(this.roles);
    return roles.toArray(new Role[roles.size()]);
  }

  /**
   * @see ch.o2it.weblounge.common.security.RoleOwner#hasRole(java.lang.String,
   *      java.lang.String)
   */
  public boolean hasRole(String context, String id) {
    return hasRole(new RoleImpl(context, id));
  }

  /**
   * Returns <code>true</code> if this object owns the specified role.
   * 
   * @param role
   *          the role
   * @return <code>true</code> if the role is owned
   */
  public boolean hasRole(Role role) {
    if (roles != null) {
      if (roles.contains(role))
        return true;

      // Obviously, the user does not directly own the given role.
      // In this case, we first check indirect role ownership:

      synchronized (roles) {
        for (Role r : roles) {
          if (r.isExtensionOf(role)) {
            return true;
          }
        }
      }
    }

    // The user does not directly or indirectly own the given role.
    // So our last chance is to check via the group memberships:

    if (groups != null) {
      synchronized (groups) {
        for (Group g : groups) {
          if (g.hasRole(role)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return context_.hashCode() | id_.hashCode() >> 16;
  }
  
  /**
   * Returns <code>true</code> if <code>o</code> is equal to this group by
   * means of context and identifier.
   * 
   * @return <code>true</code> if <code>o</code> is equal to <code>this</code>
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof Group) {
      Group g = (Group) o;
      return context_.equals(g.getContext()) && id_.equals(g.getIdentifier());
    }
    return false;
  }

  /**
   * Returns the string representation of this group.
   * 
   * @return the string representation
   */
  public String toString() {
    return context_ + ":" + id_;
  }

}