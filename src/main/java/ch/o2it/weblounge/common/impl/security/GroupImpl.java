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
 * Default implementation of a group.
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
   * 
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Group#getContext()
   */
  public String getContext() {
    return context_;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Group#getIdentifier()
   */
  public String getIdentifier() {
    return id_;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.security.Principal#getName()
   */
  public String getName() {
    return id_;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Group#addMember(ch.o2it.weblounge.common.security.GroupMember)
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Group#removeMember(ch.o2it.weblounge.common.security.GroupMember)
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Group#hasMember(ch.o2it.weblounge.common.security.GroupMember)
   */
  public boolean hasMember(GroupMember member) {
    return members != null && members.contains(member);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Group#getMembers()
   */
  public GroupMember[] getMembers() {
    if (members == null) {
      return new GroupMember[] {};
    }
    return members.toArray(new GroupMember[members.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.GroupMember#addMembership(ch.o2it.weblounge.common.security.Group)
   */
  public void addMembership(Group group) {
    synchronized (groups) {
      groups.add(group);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.GroupMember#removeMembership(ch.o2it.weblounge.common.security.Group)
   */
  public void removeMembership(Group group) {
    if (groups != null) {
      synchronized (groups) {
        groups.remove(group);
      }
    }
  }

  /**
   * {@inheritDoc}
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
   * {@inheritDoc}
   * 
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.GroupMember#isMemberOf(ch.o2it.weblounge.common.security.Group)
   */
  public boolean isMemberOf(Group group) {
    return groups != null && groups.contains(group);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.RoleOwner#assignRole(ch.o2it.weblounge.common.security.Role)
   */
  public void assignRole(Role role) {
    synchronized (roles) {
      roles.add(role);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.RoleOwner#unassignRole(ch.o2it.weblounge.common.security.Role)
   */
  public void unassignRole(Role role) {
    if (roles != null) {
      synchronized (roles) {
        roles.remove(role);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.RoleOwner#getRoleClosure()
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.RoleOwner#getRoles()
   */
  public Role[] getRoles() {
    Set<Role> roles = new HashSet<Role>();
    if (this.roles != null)
      roles.addAll(this.roles);
    return roles.toArray(new Role[roles.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.RoleOwner#hasRole(java.lang.String,
   *      java.lang.String)
   */
  public boolean hasRole(String context, String id) {
    return hasRole(new RoleImpl(context, id));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.RoleOwner#hasRole(ch.o2it.weblounge.common.security.Role)
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
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return context_.hashCode() | id_.hashCode() >> 16;
  }

  /**
   * Returns <code>true</code> if <code>o</code> is equal to this group by means
   * of context and identifier.
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
   * Returns the string representation of this group in the form
   * <code>&lt;context&gt;:&lt;identifier&gt;</code>.
   * 
   * @return the string representation
   */
  public String toString() {
    return context_ + ":" + id_;
  }

}