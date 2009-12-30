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

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator specifically designed for <code>Multilingual</code> objects.
 */
public class MultilingualComparator<Type extends Localizable> implements Comparator<Type>, Serializable {

  /** Serial version uid */
  private static final long serialVersionUID = 1086161748432134590L;

  /** The language used for comparison */
  private Language l = null;

  /**
   * Creates a new comparator for the given language.
   * 
   * @param language
   *          the language
   */
  public MultilingualComparator(Language language) {
    l = language;
  }

  /**
   * Sets the language used to perform the comparison;
   * 
   * @param language
   *          the language
   * @throws IllegalArgumentException
   *           if <code>language</code> is null
   */
  public void setLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");
    l = language;
  }

  /**
   * Returns the current language.
   * 
   * @return the language
   */
  public Language getLanguage() {
    return l;
  }

  /**
   * Compares <code>a</code> to <code>b</code> with respect to the
   * <code>Multilingual</code> interface.
   * 
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Type a, Type b) {
    if (a == null && b == null) {
      return 0;
    } else if (a == null) {
      return 1;
    } else if (b == null) {
      return -1;
    } else {
      return a.compareTo(b, l);
    }
  }

}