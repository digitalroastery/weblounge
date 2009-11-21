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

package ch.o2it.weblounge.common.impl.user;

import ch.o2it.weblounge.common.impl.security.RoleImpl;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.DigestType;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.user.AuthenticatedUser;
import ch.o2it.weblounge.common.user.User;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * This class represents the authentication part of the implementation of a
 * weblounge user by providing support for the login context, the credentials
 * and the logout operation.
 */
public class AuthenticatedUserImpl extends UserImpl implements AuthenticatedUser {

  /** The groups where this user is a member */
  protected Set<Group> groups = null;

  /** The groups where this user is a member */
  protected Set<Role> roles = null;

  /** The login context */
  protected LoginContext loginContext = null;

  /** the password */
  protected byte[] password = null;

  /** Password digest type, either plain or md5 */
  protected DigestType passwordDigestType = DigestType.plain;

  /** the public credential set */
  protected Set<Object> publicCredentials = null;

  /** the private credential set */
  protected Set<Object> privateCredentials = null;

  /**
   * Creates a new abstract authenticated user.
   * 
   * @param login
   *          the username
   * @param realm
   *          the login domain
   */
  public AuthenticatedUserImpl(String login, String realm) {
    super(login, realm);
  }

  /**
   * Creates a new abstract authenticated user without any reference to a site.
   * The login context has to be set using
   * {@link #setLoginContext(LoginContext)}.
   * 
   * @param login
   *          the login
   */
  public AuthenticatedUserImpl(String login) {
    this(login, null);
  }

  /**
   * Sets the login context which is later used to perform a clean logout
   * operation.
   * 
   * @param context
   *          the login context
   */
  public void setLoginContext(LoginContext context) {
    loginContext = context;
  }

  /**
   * Returns the user's login context. This method returns <code>null</code> if
   * the user is not authenticated.
   * 
   * @return the login context
   */
  public LoginContext getLoginContext() {
    return loginContext;
  }

  /**
   * Sets the user's password. The argument <code>type</code> specifies the
   * password encoding, which is one of
   * <ul>
   * <li>{@link DigestType#plain}</li>
   * <li>{@link DigestType#md5}</li>
   * </ul>
   * 
   * @param password
   *          the password
   * @param type
   *          the digest type used to hash the password
   */
  public void setPassword(byte[] password, DigestType type) {
    passwordDigestType = type;
    this.password = password;
  }

  /**
   * Sets the user's password. The password will be md5 encoded.
   * 
   * @param password
   *          the password
   */
  public void setPassword(byte[] password) {
    if (password == null) {
      this.password = null;
      return;
    }
    setPassword(DigestUtils.md5(password), DigestType.md5);
  }

  /**
   * Returns the password that was used to login.
   * 
   * @return the password
   */
  public byte[] getPassword() {
    return password;
  }

  /**
   * Checks <code>password</code> for equality with the users's password.
   * <p>
   * <b>Note:</b> This method returns <code>true</code> if <code>password</code>
   * is <code>null</code>.
   * 
   * @param password
   *          the password to check
   * @return <code>true</code> if the password matches
   */
  public boolean checkPassword(String password) {
    if (password == null) {
      return true;
    }
    switch (passwordDigestType) {
      case plain:
        return this.password.equals(password);
      case md5:
        return this.password.equals(DigestUtils.md5(password));
    }
    return false;
  }

  /**
   * Returns the password type, which is either {@link DigestType#plain} or
   * {@link DigestType#md5}. The default is <code>plain</code> .
   * 
   * @return the password type
   */
  public DigestType getPasswordDigestType() {
    return passwordDigestType;
  }

  /**
   * Returns <code>true</code> if the user is authenticated. This is the case if
   * the user possesses a valid login context.
   * 
   * @see ch.o2it.weblounge.common.user.AuthenticatedUser#isAuthenticated()
   */
  public boolean isAuthenticated() {
    return loginContext != null;
  }

  /**
   * This method is called by the authentication service when the user is logged
   * out of the site.
   * 
   * @throws LoginException
   *           if logout fails for some reason
   */
  public void logout() throws LoginException {
    if (loginContext != null) {
      loginContext.logout();
      loginContext = null;
    }
  }

  /**
   * Adds the private credential to this user. A public credential would be for
   * example the public key of an rsa keypair.
   * 
   * @param credential
   *          the public credential
   */
  public void addPublicCredential(Object credential) {
    if (publicCredentials == null) {
      publicCredentials = new HashSet<Object>();
    }
    publicCredentials.add(credential);
  }

  /**
   * Returns the set of all public credentials of this user.
   * 
   * @return the public credentials
   */
  public Set<Object> getPublicCredentials() {
    if (publicCredentials != null) {
      return publicCredentials;
    } else {
      return new HashSet<Object>();
    }
  }

  /**
   * Returns the user's set of all public credentials of the given type.
   * 
   * @return the public credentials
   */
  public Set<Object> getPublicCredentials(Class<?> type) {
    Set<Object> set = new HashSet<Object>();
    if (publicCredentials != null) {
      for (Object c : publicCredentials) {
        set.add(c);
      }
    }
    return set;
  }

  /**
   * Adds the private credential to this user. A private credential would be for
   * example the private key of an rsa keypair.
   * 
   * @param credential
   *          the private credential
   */
  public void addPrivateCredential(Object credential) {
    if (privateCredentials == null) {
      privateCredentials = new HashSet<Object>();
    }
    privateCredentials.add(credential);
  }

  /**
   * Returns the set of all private credentials of this user.
   * 
   * @return the private credentials
   */
  public Set<Object> getPrivateCredentials() {
    if (privateCredentials != null) {
      return privateCredentials;
    } else {
      return new HashSet<Object>();
    }
  }

  /**
   * Returns the user's set of all private credentials of the given type.
   * 
   * @return the private credentials
   */
  public Set<Object> getPrivateCredentials(Class<?> type) {
    Set<Object> set = new HashSet<Object>();
    if (privateCredentials != null) {
      for (Object c : privateCredentials) {
        set.add(c);
      }
    }
    return set;
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
   * Registers a membership with <code>group</code>.
   * 
   * @param group
   *          the group that has been joined
   */
  public void addMembership(Group group) {
    if (groups == null) {
      groups = new HashSet<Group>();
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
      roles = new HashSet<Role>();
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
    return hasRole(new RoleImpl(id, context));
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
   * {@inheritDoc}
   * 
   * TODO: Same implementation than equals(Object). What is the idea behind
   * this?
   * 
   * @see ch.o2it.weblounge.common.security.Authority#equals(ch.o2it.weblounge.common.security.Authority)
   */
  public boolean equals(Authority authority) {
    if (authority instanceof AuthenticatedUser) {
      AuthenticatedUser u = (AuthenticatedUser) authority;
      if (!login.equals(u.getLogin()))
        return false;
      if (!realm.equals(u.getRealm()))
        return false;
      return true;
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
    if (obj instanceof User) {
      User u = (User)obj;
      if (!login.equals(u.getLogin()))
        return false;
      if (!realm.equals(u.getRealm()))
        return false;
      return true;
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