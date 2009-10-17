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

import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.security.Permission;

/**
 * This class represents the permission to do something that might be restricted
 * by the security subsystem.
 * <p>
 * In addition, it defines common permissions to be used throughout the security
 * system.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class PermissionImpl extends LocalizableContent<String> implements Permission {

  /** PermissionImpl identifier */
  private String identifier_ = null;

  /** PermissionImpl identifier */
  private String context_ = null;

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
    context_ = permission.substring(0, divider);
    identifier_ = permission.substring(divider + 1);
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
    context_ = context;
    identifier_ = id;
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
    return context_;
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
      return ((Permission) obj).getIdentifier().equals(identifier_) && ((Permission) obj).getContext().equals(context_);
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
    return context_ + ":" + identifier_;
  }

}