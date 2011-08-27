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

package ch.entwine.weblounge.common.security;

import ch.entwine.weblounge.common.language.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Special permission for the system context.
 * 
 * TODO Look over these permissions
 */
public final class SystemPermission implements Permission {

  /** System context identifier */
  public static final String CONTEXT = "system";

  /** Read permission */
  public static final Permission READ = new SystemPermission("read");

  /** Write permission */
  public static final Permission WRITE = new SystemPermission("write");

  /** Append permission */
  public static final Permission APPEND = new SystemPermission("append");

  /** Delete permission */
  public static final Permission DELETE = new SystemPermission("delete");

  /** Modify permission */
  public static final Permission MODIFY = new SystemPermission("modify");

  /** Modify permission */
  public static final Permission LIST = new SystemPermission("list");

  /** Manage permission */
  public static final Permission MANAGE = new SystemPermission("manage");

  /** Publish permission */
  public static final Permission PUBLISH = new SystemPermission("publish");

  /** Permission identifier */
  private String identifier = null;

  /** The permission titles */
  private Map<Language, String> titles = null;

  /** The selected language */
  private Language selectedLanguage = null;

  /**
   * Creates a new system permission. Use the defined constants to access
   * instances of this class.
   * 
   * @param permission
   *          the permission name
   */
  private SystemPermission(String permission) {
    identifier = permission;
    titles = new HashMap<Language, String>();
  }

  /**
   * Returns the permission identifier.
   * 
   * @return the permission identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Returns the permission context.
   * 
   * @return the permission context
   */
  public String getContext() {
    return CONTEXT;
  }

  /**
   * Returns the hash code for this permission object.
   * 
   * @return the hashcode
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Returns <code>true</code> if <code>obj</code> is of type
   * <code>PermissionImpl</code> object literally representing the same instance
   * than this one.
   * 
   * @param obj
   *          the object to test for equality
   * @return <code>true</code> if <code>obj</code> represents the same
   *         <code>Permission</code>
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Permission) {
      return ((Permission) obj).getIdentifier().equals(identifier) && ((Permission) obj).getContext().equals(CONTEXT);
    }
    return false;
  }

  /**
   * Returns the string representation of this permission object, which is equal
   * to the permission identifier.
   * 
   * @return the permission identifier
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return CONTEXT + ":" + identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    return titles.keySet();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#supportsLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    return titles.containsKey(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    if (titles.containsKey(language))
      selectedLanguage = language;
    return selectedLanguage;
  }

}