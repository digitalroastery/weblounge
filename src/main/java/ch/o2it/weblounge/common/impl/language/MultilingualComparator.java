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

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;

import java.util.Comparator;

/**
 * Comparator specifically designed for <code>Multilingual</code> objects.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public class MultilingualComparator<Type> implements Comparator<Type> {

  /** The language used to compare */
  private Language l;

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
   */
  public void setLanguage(Language language) {
    l = language;
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
    } else if (a instanceof LocalizableContent<?> && b instanceof LocalizableContent<?>) {
      return ((LocalizableContent<?>)a).get(l).toString().toLowerCase().compareTo(((LocalizableContent<?>) b).get(l).toString().toLowerCase());
    } else if (a instanceof Localizable && b instanceof Localizable) {
      ((Localizable)a).switchTo(l);
      ((Localizable)b).switchTo(l);
      return ((Localizable)a).toString().toLowerCase().compareTo(((Localizable) b).toString().toLowerCase());
    } else {
      return a.toString().toLowerCase().compareTo(b.toString().toLowerCase());
    }
  }

}