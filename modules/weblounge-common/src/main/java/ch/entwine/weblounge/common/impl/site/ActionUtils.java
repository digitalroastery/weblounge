/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.common.impl.site;

/**
 * This class provides useful utility functions for for action implementations.
 */
public final class ActionUtils {

  /**
   * Utility classes must not be implemented.
   */
  private ActionUtils() {
  }

  /**
   * Returns an identifier pointing at an object in the current module.
   * <p>
   * This method is helpful for using <code>enum</code> constants such as
   * <code>SearchForm</code> and turn them into an identifier named
   * <code>search.form</code>, which is used as the corresponding
   * <code>module.xml</code>'s pagelet definition.
   * <p>
   * If <code>prefix</code> is not <code>null</code>, it will be prepended to
   * the identifier, separated by a dot, i. e. <code>prefix.search.form</code>.
   * 
   * @param prefix
   *          the object namespace
   * @param e
   *          the enumeration constant
   * @return the object identifier
   */
  public static String enumToId(String prefix, Enum<?> e) {
    String s = e.toString();
    StringBuffer b = new StringBuffer();
    if (prefix != null) {
      b.append(prefix);
      if (!prefix.endsWith("."))
        b.append(".");
    }
    for (int i = 0; i < s.length(); i++) {
      Character c = s.charAt(i);
      if (i != 0 && Character.isUpperCase(c)) {
        b.append(".");
      }
      b.append(Character.toLowerCase(c));
    }
    return b.toString();
  }

  /**
   * Returns an identifier pointing at an object in the current module.
   * <p>
   * This method is helpful for using <code>enum</code> constants such as
   * <code>SearchForm</code> and turn them into <code>search.form</code>, which
   * is used as the corresponding <code>module.xml</code>'s pagelet definition.
   * 
   * @param e
   *          the enumeration constant
   * @return the object identifier
   */
  public static String enumToId(Enum<?> e) {
    return enumToId(null, e);
  }

}
