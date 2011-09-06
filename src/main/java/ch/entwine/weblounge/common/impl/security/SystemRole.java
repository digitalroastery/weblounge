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

package ch.entwine.weblounge.common.impl.security;

import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.security.Role;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * This class declares the roles used by the weblounge system.
 */
public final class SystemRole extends RoleImpl {

  /** The System role context */
  public static final String CONTEXT = "system";

  /** Guest */
  public static final Role GUEST = new SystemRole("guest", "Guest");

  /** Editor */
  public static final Role EDITOR = new SystemRole("editor", GUEST, "Editor");

  /** Publisher */
  public static final Role PUBLISHER = new SystemRole("publisher", EDITOR, "Publisher");

  /** Site administrator */
  public static final Role SITEADMIN = new SystemRole("siteadmin", PUBLISHER, "Site Administrator");

  /** Remove role */
  public static final Role SYSTEMADMIN = new SystemRole("systemadmin", SITEADMIN, "System Administrator");

  /** The system roles collection */
  private static Set<Role> roles = new HashSet<Role>();

  static {
    roles.add(SystemRole.GUEST);
    roles.add(SystemRole.EDITOR);
    roles.add(SystemRole.PUBLISHER);
    roles.add(SystemRole.SITEADMIN);
    roles.add(SystemRole.SYSTEMADMIN);
  }

  /**
   * Creates a new system Role.
   * 
   * @param role
   *          the role name
   */
  private SystemRole(String role) {
    super(CONTEXT, role);
  }

  /**
   * Creates a new system Role.
   * 
   * @param role
   *          the role name
   * @param name
   *          the English role name
   */
  private SystemRole(String role, String name) {
    super(CONTEXT, role);
    setName(name, LanguageUtils.getLanguage(Locale.ENGLISH));
    setDefaultLanguage(LanguageUtils.getLanguage(Locale.ENGLISH));
  }

  /**
   * Creates a new system Role which extends the <code>baseRole</code> role.
   * 
   * @param role
   *          the role name
   * @param baseRole
   *          the role to extend
   */
  private SystemRole(String role, Role baseRole) {
    super(CONTEXT, role, baseRole);
  }

  /**
   * Creates a new system Role which extends the <code>baseRole</code> role.
   * 
   * @param role
   *          the role name
   * @param baseRole
   *          the role to extend
   * @param name
   *          the role name
   */
  private SystemRole(String role, Role baseRole, String name) {
    super(CONTEXT, role, baseRole);
    setName(name, LanguageUtils.getLanguage(Locale.ENGLISH));
    setDefaultLanguage(LanguageUtils.getLanguage(Locale.ENGLISH));
  }

  /**
   * Returns the corresponding system role or <code>null</code> if no such role
   * exists.
   * 
   * @param roleId
   *          the role identifier of the form &lt;context&gt;:&lt;id&gt;
   * @return the role or <code>null</code> if no such role exists
   */
  public static Role getRole(String roleId) {
    if (roleId == null)
      return null;

    // Check id
    if (roleId.equals(GUEST.getIdentifier()))
      return GUEST;
    else if (roleId.equalsIgnoreCase(EDITOR.getIdentifier()))
      return EDITOR;
    else if (roleId.equalsIgnoreCase(PUBLISHER.getIdentifier()))
      return PUBLISHER;
    else if (roleId.equalsIgnoreCase(SITEADMIN.getIdentifier()))
      return SITEADMIN;
    else
      return null;
  }

  /**
   * Returns an iteration of all system roles.
   * 
   * @return all system roles
   */
  public static Iterator<Role> roles() {
    return roles.iterator();
  }

}