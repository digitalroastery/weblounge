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

import ch.entwine.weblounge.common.impl.language.LocalizableContent;
import ch.entwine.weblounge.common.security.Action;

/**
 * This class represents an action that might be restricted by the security
 * subsystem.
 */
public class ActionImpl extends LocalizableContent<String> implements Action {

  /** PermissionImpl identifier */
  private String identifier = null;

  /** PermissionImpl identifier */
  private String context = null;

  /**
   * Creates a new action from the parameter context::id.
   * 
   * @param action
   *          the action
   */
  public ActionImpl(String action) {
    assert action != null;
    int divider = action.indexOf(':');
    if (divider <= 0 || divider >= (action.length() - 1))
      throw new IllegalArgumentException("Action must be of the form 'context:id'!");
    this.context = action.substring(0, divider);
    this.identifier = action.substring(divider + 1);
  }

  /**
   * Creates a new action with the given context and identifier.
   * 
   * @param context
   *          the action context
   * @param id
   *          the action identifier
   */
  public ActionImpl(String context, String id) {
    this.context = context;
    this.identifier = id;
  }

  /**
   * Returns the action identifier.
   * 
   * @return the action identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Returns the action context.
   * 
   * @return the action context
   */
  public String getContext() {
    return context;
  }

  /**
   * Returns the hash code for this action object.
   * 
   * @return the hash code
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
   * @return <code>true</code> if <code>obj</code> represents the same action
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Action) {
      return ((Action) obj).getIdentifier().equals(identifier) && ((Action) obj).getContext().equals(context);
    }
    return false;
  }

  /**
   * Returns the string representation of this action object, which is equal
   * to the action identifier.
   * 
   * @return the action identifier
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return context + ":" + identifier;
  }

}
