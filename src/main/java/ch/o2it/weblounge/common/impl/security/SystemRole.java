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

import ch.o2it.weblounge.common.security.Role;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class declares the roles used by the weblounge system.
 */
public class SystemRole extends RoleImpl {

  /** The System role context */
  public static final String CONTEXT = "system";

  /** Guest */
  public static final Role GUEST = new SystemRole("guest");

  /** Translator */
  public static final Role TRANSLATOR = new SystemRole("translator", GUEST);

  /** Editor */
  public static final Role EDITOR = new SystemRole("editor", TRANSLATOR);

  /** Publisher */
  public static final Role PUBLISHER = new SystemRole("publisher", EDITOR);

  /** Domain administrator */
  public static final Role DOMAINADMIN = new SystemRole("domainadmin", PUBLISHER);

  /** Site administrator */
  public static final Role SITEADMIN = new SystemRole("siteadmin", DOMAINADMIN);

  /** Remove role */
  public static final Role SYSTEMADMIN = new SystemRole("systemadmin", SITEADMIN);

  /** The system roles collection */
  private static Set<Role> roles_ = new HashSet<Role>();
  
  static {
    roles_.add(SystemRole.GUEST);
    roles_.add(SystemRole.TRANSLATOR);
    roles_.add(SystemRole.EDITOR);
    roles_.add(SystemRole.PUBLISHER);
    roles_.add(SystemRole.DOMAINADMIN);
    roles_.add(SystemRole.SITEADMIN);
    roles_.add(SystemRole.SYSTEMADMIN);
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
   * Creates a new system Role which extends the <code>ancestor</code> role.
   * 
   * @param role
   *          the role name
   * @param ancestor
   *          the role to extend
   */
  private SystemRole(String role, Role ancestor) {
    super(CONTEXT, role, ancestor);
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

    String context = extractContext(roleId);
    String id = extractIdentifier(roleId);
    return getRole(context, id);
  }

  /**
   * Returns the corresponding system role or <code>null</code> if no such role
   * exists.
   * 
   * @param context
   *          the role context, e. g. <code>system</code>
   * @param id
   *          the role identifier, e. g. <code>publisher</code>
   * @return the role or <code>null</code> if no such role exists
   */
  public static Role getRole(String context, String id) {
    // Check if context is "system"
    if (!"system".equalsIgnoreCase(context))
      return null;

    // Check id
    if (id.equals(GUEST.getIdentifier()))
      return GUEST;
    else if (id.equalsIgnoreCase(TRANSLATOR.getIdentifier()))
      return TRANSLATOR;
    else if (id.equalsIgnoreCase(EDITOR.getIdentifier()))
      return EDITOR;
    else if (id.equalsIgnoreCase(PUBLISHER.getIdentifier()))
      return PUBLISHER;
    else if (id.equalsIgnoreCase(DOMAINADMIN.getIdentifier()))
      return DOMAINADMIN;
    else if (id.equalsIgnoreCase(SITEADMIN.getIdentifier()))
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
    return roles_.iterator();
  }

}