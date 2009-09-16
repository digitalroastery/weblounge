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

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

/**
 * This class implements a proxy for a user object. As long as operations on
 * this object affect the login only, this proxy is sufficient. If more of the
 * user's information is needed, the proxy will have the corresponding stub
 * instantiated.
 * 
 * TODO Is this still needed?
 */
public class UserProxy implements User {

  /** State flag for lazy initialization */
  protected boolean initialized = false;

  /** The login name */
  String login = null;

  /** The associated site */
  Site site = null;

  /** First name */
  String firstname = null;

  /** Last name */
  String lastname = null;

  /** Email */
  String email = null;

  /** The proxy stub */
  protected User stub = null;

  /**
   * Creates a new proxy for the user with login <code>login</code>.
   * 
   * @param login
   *          the username
   * @param site
   *          the site
   */
  protected UserProxy(Site site) {
    this(null, null, null, site);
  }

  /**
   * Creates a new proxy for the user with login <code>login</code>.
   * 
   * @param login
   *          the username
   * @param site
   *          the site
   */
  public UserProxy(String login, Site site) {
    this(login, null, null, site);
  }

  /**
   * Creates a new proxy for the user with login <code>login</code>.
   * 
   * @param login
   *          the username
   * @param firstname
   *          the firstname
   * @param lastname
   *          the lastname
   * @param site
   *          the site
   */
  public UserProxy(String login, String firstname, String lastname, Site site) {
    this.login = login;
    this.site = site;
    this.firstname = firstname;
    this.lastname = lastname;
    initialized = false;
  }

  /**
   * Returns the first name of this person.
   * 
   * @return the person's first name
   */
  public String getFirstName() {
    if (!initialized && login != null && firstname == null)
      init();
    return (initialized) ? stub.getFirstName() : firstname;
  }

  /**
   * Returns the last name of this person.
   * 
   * @return the person's last name
   */
  public String getLastName() {
    if (!initialized && login != null && lastname == null)
      init();
    return (initialized) ? stub.getLastName() : lastname;
  }

  /**
   * Returns the email address of this person.
   * 
   * @return the person's email address
   */
  public String getEmail() {
    if (!initialized && login != null && email == null)
      init();
    return (initialized) ? stub.getEmail() : email;
  }

  /**
   * Returns the preferred language of this person.
   * 
   * @return the person's preferred language
   */
  public Language getLanguage() {
    if (!initialized && login != null)
      init();
    return stub.getLanguage();
  }

  /**
   * Method to call for lazy initialization.
   */
  protected synchronized boolean init() {
    if (initialized)
      return true;
    stub = site.getUsers().loadUser(login);
    if (stub == null) {
      if (login.equals(WebloungeAdmin.getInstance().getLogin())) {
        stub = WebloungeAdmin.getInstance();
      } else {
        stub = site.getAdministrator();
      }
    }
    initialized = true;
    return initialized;
  }

  /**
   * Check whether the user is initialized.
   * 
   * @return <code>true</code> if the user has been initialized
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * @see ch.o2it.weblounge.common.security.User#getName()
   */
  public String getName() {
    if (!initialized && login != null)
      init();
    return stub.getName();
  }

  /**
   * @see ch.o2it.weblounge.common.security.User#getName(boolean)
   */
  public String getName(boolean reversed) {
    if (!initialized && login != null)
      init();
    return stub.getName(reversed);
  }

  /**
   * @see ch.o2it.weblounge.common.security.User#getLogin()
   */
  public String getLogin() {
    return login;
  }

  /**
   * @see ch.o2it.weblounge.common.security.User#getInitials()
   */
  public String getInitials() {
    if (!initialized && login != null)
      init();
    return stub.getInitials();
  }

  /**
   * @see ch.o2it.weblounge.common.security.GroupMember#addMembership(ch.o2it.weblounge.common.security.Group)
   */
  public void addMembership(Group group) {
    if (!initialized && login != null)
      init();
    stub.addMembership(group);
  }

  /**
   * @see ch.o2it.weblounge.common.security.GroupMember#removeMembership(ch.o2it.weblounge.common.security.Group)
   */
  public void removeMembership(Group group) {
    if (!initialized && login != null)
      init();
    stub.removeMembership(group);
  }

  /**
   * @see ch.o2it.weblounge.common.security.GroupMember#isMemberOf(ch.o2it.weblounge.common.security.Group)
   */
  public boolean isMemberOf(Group group) {
    if (!initialized && login != null)
      init();
    return stub.isMemberOf(group);
  }

  /**
   * @see ch.o2it.weblounge.common.security.GroupMember#getGroupClosure()
   */
  public Group[] getGroupClosure() {
    if (!initialized && login != null)
      init();
    return stub.getGroupClosure();
  }

  /**
   * @see ch.o2it.weblounge.common.security.GroupMember#getGroups()
   */
  public Group[] getGroups() {
    if (!initialized && login != null)
      init();
    return stub.getGroups();
  }

  /**
   * @see ch.o2it.weblounge.common.security.RoleOwner#assignRole(ch.o2it.weblounge.common.security.Role)
   */
  public void assignRole(Role role) {
    if (!initialized && login != null)
      init();
    stub.assignRole(role);
  }

  /**
   * @see ch.o2it.weblounge.common.security.RoleOwner#unassignRole(ch.o2it.weblounge.common.security.Role)
   */
  public void unassignRole(Role role) {
    if (!initialized && login != null)
      init();
    stub.unassignRole(role);
  }

  /**
   * @see ch.o2it.weblounge.common.security.RoleOwner#hasRole(ch.o2it.weblounge.common.security.Role)
   */
  public boolean hasRole(Role role) {
    if (!initialized && login != null)
      init();
    return stub.hasRole(role);
  }

  /**
   * @see ch.o2it.weblounge.common.security.RoleOwner#hasRole(java.lang.String,
   *      java.lang.String)
   */
  public boolean hasRole(String context, String id) {
    if (!initialized && login != null)
      init();
    return stub.hasRole(context, id);
  }

  /**
   * @see ch.o2it.weblounge.common.security.RoleOwner#getRoleClosure()
   */
  public Role[] getRoleClosure() {
    if (!initialized && login != null)
      init();
    return stub.getRoleClosure();
  }

  /**
   * @see ch.o2it.weblounge.common.security.RoleOwner#getRoles()
   */
  public Role[] getRoles() {
    if (!initialized && login != null)
      init();
    return stub.getRoles();
  }

  /**
   * @see ch.o2it.weblounge.common.security.Authority#getAuthorityType()
   */
  public String getAuthorityType() {
    if (!initialized && login != null)
      init();
    return stub.getAuthorityType();
  }

  /**
   * @see ch.o2it.weblounge.common.security.Authority#getAuthorityId()
   */
  public String getAuthorityId() {
    if (!initialized && login != null)
      init();
    return stub.getAuthorityId();
  }

  /**
   * @see ch.o2it.weblounge.common.security.Authority#equals(ch.o2it.weblounge.common.security.Authority)
   */
  public boolean equals(Authority authority) {
    if (!initialized && login != null)
      init();
    return stub.equals(authority);
  }

}