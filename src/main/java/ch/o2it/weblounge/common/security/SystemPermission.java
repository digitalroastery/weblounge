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

package ch.o2it.weblounge.common.security;

/**
 * Special permission for the system context.
 * 
 * TODO Look over these permissions
 */
public class SystemPermission implements Permission {

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

  /** Translate permission */
  public static final Permission TRANSLATE = new SystemPermission("translate");

  /** Publish permission */
  public static final Permission PUBLISH = new SystemPermission("publish");

  /** Permission identifier */
  private String identifier_;

  /**
   * Creates a new system permission. Use the defined constants to access
   * instances of this class.
   * 
   * @param permission
   *          the permission name
   */
  private SystemPermission(String permission) {
    identifier_ = permission;
  }

  /**
   * Returns the permission identifier.
   * 
   * @return the permission identifier
   */
  public String getIdentifier() {
    return identifier_;
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
      return ((Permission) obj).getIdentifier().equals(identifier_) && ((Permission) obj).getContext().equals(CONTEXT);
    }
    return false;
  }

  /**
   * Returns the string representaton of this permission object, which is equal
   * to the permission identifier.
   * 
   * @return the permission identifier
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return CONTEXT + ":" + identifier_;
  }

}