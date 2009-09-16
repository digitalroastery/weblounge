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

package ch.o2it.weblounge.common.language;

import java.util.Iterator;

/**
 * This interface defines methods for multilingual object. Such an object is
 * capable of representing it's content in various languages.
 */
public interface Localizable {

  /**
   * Returns <code>true</code> if the given language is supported, i. e. if the
   * can be represented using the given language.
   * 
   * @param language
   *          a language
   * @return <code>true</code> if the language is supported
   */
  boolean supportsLanguage(Language language);

  /**
   * Returns an iteration of all languages that are supported by the
   * multilingual object.
   * 
   * @return the supported languages
   */
  Iterator<Language> languages();

  /**
   * Returns the object using <code>language</code> as the output language.
   * 
   * @param language
   *          the language
   * @return the object's string representation in the required language
   */
  String toString(Language language);

  /**
   * Returns the object using <code>language</code> as the output language. If
   * no content can be found in that language, then it will be looked up in the
   * default language (unless <code>force</code> is set to <code>true</code>). <br>
   * If this doesn't produce a result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language
   * @param force
   *          <code>true</code> to force the language
   * @return the object's string representation in the given language
   */
  String toString(Language language, boolean force);

  /**
   * Compares this object to <code>o</code> with respect to the given language
   * <code>language</code>.
   * 
   * @param o
   *          the object to compare to
   * @param language
   *          the language
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  int compareTo(Localizable o, Language language);

}