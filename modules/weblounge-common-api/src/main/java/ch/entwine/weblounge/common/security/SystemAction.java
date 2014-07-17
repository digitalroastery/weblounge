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
 */
public final class SystemAction implements Action {

  /** Read action */
  public static final Action READ = new SystemAction("read");

  /** Write action */
  public static final Action WRITE = new SystemAction("write");

  /** Append action */
  public static final Action APPEND = new SystemAction("append");

  /** Delete action */
  public static final Action DELETE = new SystemAction("delete");

  /** Modify action */
  public static final Action MODIFY = new SystemAction("modify");

  /** Modify action */
  public static final Action LIST = new SystemAction("list");

  /** Manage action */
  public static final Action MANAGE = new SystemAction("manage");

  /** Publish action */
  public static final Action PUBLISH = new SystemAction("publish");

  /** Action identifier */
  private String identifier = null;

  /** The action titles */
  private Map<Language, String> titles = null;

  /** The selected language */
  private Language selectedLanguage = null;

  /**
   * Creates a new system action. Use the defined constants to access instances
   * of this class.
   * 
   * @param action
   *          the action name
   */
  private SystemAction(String action) {
    identifier = action;
    titles = new HashMap<Language, String>();
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
    return Security.SYSTEM_CONTEXT;
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
   * @return <code>true</code> if <code>obj</code> represents the same
   *         <code>Action</code>
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Action) {
      return ((Action) obj).getIdentifier().equals(identifier) && ((Action) obj).getContext().equals(Security.SYSTEM_CONTEXT);
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
    return Security.SYSTEM_CONTEXT + ":" + identifier;
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