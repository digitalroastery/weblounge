/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.impl.language.MultilingualTreeSet;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents some part of the implementation of a weblounge user by
 * providing support for password checks, roles and group memberships.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public abstract class AbstractUser implements User {

  /** The associated site */
  protected Site site = null;

  /** The username */
  protected String login = null;

  /** The groups where this user is a member */
  protected Set<Group> groups = null;

  /** The groups where this user is a member */
  protected Set<Role> roles = null;

  /** The cached initials */
  private String initials_ = null;

  /**
   * Creates a new abstract user;
   * 
   * @param login
   *          the username
   * @param site
   *          the associated site
   */
  public AbstractUser(String login, Site site) {
    this.login = login;
    this.site = site;
  }

  /**
   * Creates a new abstract user for the specified site. Other properties such
   * as login, name etc. have to be filled in later.
   * 
   * @param site
   *          the associated site
   */
  protected AbstractUser(Site site) {
    this(null, site);
  }

  /**
   * Creates a new abstract user without references to a specific site.
   * 
   * @param login
   *          the username
   */
  public AbstractUser(String login) {
    this(login, null);
  }

  /**
   * Sets the login.
   * 
   * @param login
   *          the login
   */
  public void setLogin(String login) {
    this.login = login;
  }

  /**
   * Returns the login name of this user.
   * 
   * @return the username
   */
  public final String getLogin() {
    return login;
  }

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  public Site getSite() {
    return site;
  }

  /**
   * Returns the name of this user. If possible, the value returned consists of
   * type <first name><last name>.
   * 
   * @returns the full user name
   */
  public String getName() {
    String name = "";
    if (getFirstName() != null && !getFirstName().trim().equals("")) {
      if (getLastName() != null && !getLastName().trim().equals("")) {
        name = getFirstName() + " " + getLastName();
      } else {
        name = getFirstName();
      }
    } else if (getLastName() != null && !getLastName().trim().equals("")) {
      name = getLastName();
    } else {
      name = getLogin();
    }
    return name;
  }

  /**
   * Returns the name of this user. If possible, the value returned consists of
   * type <tt>&lt;first name&gt; &lt;last name&gt;</tt> or
   * <tt>&lt;last name&gt;, &lt;first name&gt;</tt>, depending on the value of
   * <code>reversed</code>.
   * 
   * @param reversed
   *          <code>true</code> to return <tt>Lastname, Firstname</tt>
   * @returns the full user name
   */
  public String getName(boolean reversed) {
    if (!reversed) {
      return getName();
    }
    String name = "";
    if (getLastName() != null && !getLastName().trim().equals("")) {
      if (getFirstName() != null && !getFirstName().trim().equals("")) {
        name = getLastName() + ", " + getFirstName();
      } else {
        name = getLastName();
      }
    } else if (getFirstName() != null && !getFirstName().trim().equals("")) {
      name = getFirstName();
    } else {
      name = getLogin();
    }
    return name;
  }

  /**
   * Returns the short version of the persons name, which are constructed from
   * the first and the last name of the user.
   * 
   * @return the persons initials
   */
  public String getInitials() {
    if (initials_ != null) {
      return initials_;
    }
    String firstName = getFirstName();
    String lastName = getLastName();
    if (firstName != null && lastName != null) {
      initials_ = firstName.substring(0, 1) + lastName.substring(0, 1);
    }
    return initials_;
  }

  /**
   * Sets the person's initials.
   * 
   * @param initials
   *          the person's initials
   */
  public void setInitials(String initials) {
    initials_ = initials;
  }

  /**
   * Registers a membership with <code>group</code>.
   * 
   * @param group
   *          the group that has been joined
   */
  public void addMembership(Group group) {
    if (groups == null) {
      groups = new MultilingualTreeSet<Group>();
    }
    if (!groups.contains(group)) {
      synchronized (groups) {
        groups.add(group);
      }
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
    Set<Group> result = new MultilingualTreeSet<Group>();
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
   * Returns an array of all groups that have this user as a member.
   * 
   * @see ch.o2it.weblounge.common.security.GroupMember#getGroupClosure()
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
    if (roles == null) {
      roles = new MultilingualTreeSet<Role>();
    }
    if (!roles.contains(role)) {
      synchronized (roles) {
        roles.add(role);
      }
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
    Set<Role> roles = new MultilingualTreeSet<Role>();
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
    Set<Role> roles = new MultilingualTreeSet<Role>();
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
   * Returns the authorization type, which is equal to
   * <code>ch.o2it.weblounge.api.staff.User</code>.
   * 
   * @return the authorization type
   */
  public String getAuthorityType() {
    return User.class.getName();
  }

  /**
   * Returns the authorization identifier, which is equal to the users's login.
   * 
   * @return the authorization identifier
   */
  public String getAuthorityId() {
    return getLogin();
  }

  /**
   * Returns <code>true</code> if <code>authority</code> represents the same
   * user.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#equals(ch.o2it.weblounge.common.security.Authority)
   */
  public boolean equals(Authority authority) {
    if (authority != null && authority instanceof User) {
      return equals((Object) authority);
    } else if (authority != null && site != null && authority.getAuthorityType().equals(User.class.getName())) {
      User u = site.getUsers().getUser(authority.getAuthorityId());
      return getLogin().equals(u.getLogin());
    }
    return false;
  }

  /**
   * Returns a hash code for this User. The hash code is calculated on the login
   * name, which should be unique per site.
   * 
   * @return the users hash code
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return (login != null) ? login.hashCode() : super.hashCode();
  }

  /**
   * Returns <code>true</code> if the given object is a <code>User</code> and
   * has the same login than this User.
   * 
   * @param obj
   *          the object to test for equality
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (login != null && obj != null && obj instanceof User) {
      return (login.equals(((User) obj).getLogin()));
    }
    return false;
  }

  /**
   * Returns a String representation of this user object, which is equal to the
   * persons login.
   * 
   * @return the login
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return (login != null) ? login : super.toString();
  }

}