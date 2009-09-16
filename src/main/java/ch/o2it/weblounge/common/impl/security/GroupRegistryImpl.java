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

import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.GroupRegistry;

import java.util.HashMap;

/**
 * This registry keeps track of the registered groups per site.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 1.0
 */

public class GroupRegistryImpl extends HashMap<String, Group> implements GroupRegistry {

  /**
   * Creates a new group registry.
   */
  public GroupRegistryImpl() {
    init();
  }

  /**
   * Initializes this group registry by adding the system defined groups like
   * guests, administrators etc.
   */
  private void init() {
    // guests
    Group guests = new GroupImpl("system", "guests");
    guests.assignRole(SystemRole.GUEST);
    addGroup(guests);

    // translators
    Group translators = new GroupImpl("system", "translators");
    translators.assignRole(SystemRole.TRANSLATOR);
    addGroup(translators);

    // editors
    Group editors = new GroupImpl("system", "editors");
    editors.assignRole(SystemRole.EDITOR);
    addGroup(editors);

    // publishers
    Group publishers = new GroupImpl("system", "publishers");
    publishers.assignRole(SystemRole.PUBLISHER);
    addGroup(publishers);

    // domainadmin
    Group domainadmins = new GroupImpl("system", "domainadministrators");
    domainadmins.assignRole(SystemRole.DOMAINADMIN);
    addGroup(domainadmins);

    // siteadmin
    Group siteadmin = new GroupImpl("system", "siteadministrators");
    siteadmin.assignRole(SystemRole.SITEADMIN);
    addGroup(siteadmin);

    // system administrator
    Group systemadmins = new GroupImpl("system", "systemadministrators");
    systemadmins.assignRole(SystemRole.SYSTEMADMIN);
    addGroup(systemadmins);
  }

  /**
   * Adds a group this registry.
   * 
   * @param group
   *          the group to add
   */
  public void addGroup(Group group) {
    if (!values().contains(group))
      put(getKey(group.getContext(), group.getIdentifier()), group);
  }

  /**
   * Adds a group this registry.
   * 
   * @param group
   *          the group to add
   */
  public void removeGroup(Group group) {
    remove(getKey(group.getContext(), group.getIdentifier()));
  }

  /**
   * Returns the specified group or <code>null</code> if no such group is part
   * of the registry.
   * 
   * @param context
   *          the group context
   * @param identifier
   *          the group identifier
   * @return the group
   */
  public Group getGroup(String context, String identifier) {
    return get(getKey(context, identifier));
  }

  /**
   * Returns the key used to store the group in the registry.
   * 
   * @param context
   *          the group context
   * @param id
   *          the group identifier
   * @return the key
   */
  private static String getKey(String context, String id) {
    return (new StringBuffer(context)).append(":").append(id).toString();
  }

}