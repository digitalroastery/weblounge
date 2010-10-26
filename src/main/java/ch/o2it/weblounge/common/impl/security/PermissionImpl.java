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
import ch.o2it.weblounge.common.security.Permission;

/**
 * This class represents the permission to do something that might be restricted
 * by the security subsystem.
 * <p>
 * In addition, it defines common permissions to be used throughout the security
 * system.
 */
public class PermissionImpl extends LocalizableContent<String> implements Permission {

  /** PermissionImpl identifier */
  private String identifier = null;

  /** PermissionImpl identifier */
  private String context = null;

  /**
   * Creates a new permission from the parameter context::id.
   * 
   * @param permission
   *          the permission
   */
  public PermissionImpl(String permission) {
    assert permission != null;
    int divider = permission.indexOf(':');
    if (divider <= 0 || divider >= (permission.length() - 1))
      throw new IllegalArgumentException("Permission must be of the form 'context:id'!");
    this.context = permission.substring(0, divider);
    this.identifier = permission.substring(divider + 1);
  }

  /**
   * Creates a new permission with the given context and identifier.
   * 
   * @param context
   *          the permission context
   * @param id
   *          the permission identifier
   */
  public PermissionImpl(String context, String id) {
    this.context = context;
    this.identifier = id;
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
    return context;
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
      return ((Permission) obj).getIdentifier().equals(identifier) && ((Permission) obj).getContext().equals(context);
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
    return context + ":" + identifier;
  }

}