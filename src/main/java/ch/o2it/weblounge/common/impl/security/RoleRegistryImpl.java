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

import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.RoleRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This registry keeps track of the registered roles per site.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 1.0
 */

public class RoleRegistryImpl extends HashMap<String, Role> implements RoleRegistry {

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = RoleRegistryImpl.class.getName();

  /** Logging facility */
  final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new role registry.
   */
  public RoleRegistryImpl() {
    init();
  }

  /**
   * Initializes this role registry by adding the system defined roles like
   * guest, administrator etc.
   */
  private void init() {
    Iterator i = SystemRole.roles();
    while (i.hasNext()) {
      addRole((Role) i.next());
    }
  }

  /**
   * Adds a role this registry.
   * 
   * @param role
   *          the role to add
   */
  public void addRole(Role role) {
    if (!values().contains(role))
      put(getKey(role.getContext(), role.getIdentifier()), role);
  }

  /**
   * Adds a role to this registry.
   * 
   * @param role
   *          the role to add
   */
  public void removeRole(Role role) {
    remove(getKey(role.getContext(), role.getIdentifier()));
  }

  /**
   * Returns the specified role or <code>null</code> if no such role is part of
   * the registry.
   * 
   * @param context
   *          the role context
   * @param identifier
   *          the role identifier
   * @return the role
   */
  public Role getRole(String context, String identifier) {
    Role r = get(getKey(context, identifier));
    if (r != null)
      return r;
    else
      return SystemRole.getRole(context, identifier);
  }

  /**
   * Returns the specified role or <code>null</code> if no such role is part of
   * the registry. Note that <code>id</code> must consist of a context and an
   * id, formulated as <code>context:id</code>.
   * 
   * @param id
   *          the role identification
   * @return the role
   */
  public Role getRole(String id) {
    try {
      String context = RoleImpl.extractContext(id);
      String identifier = RoleImpl.extractIdentifier(id);
      return getRole(context, identifier);
    } catch (IllegalArgumentException e) {
      log_.warn("Unable to extract role context and/or id from '" + id + "'");
      return null;
    }
  }

  /**
   * Returns the key used to store the role in the registry.
   * 
   * @param context
   *          the role context
   * @param id
   *          the role identifier
   * @return the key
   */
  private static String getKey(String context, String id) {
    return (new StringBuffer(context)).append(":").append(id).toString();
  }

}