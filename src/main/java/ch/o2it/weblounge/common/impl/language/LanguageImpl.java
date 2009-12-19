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

import java.util.Locale;

/**
 * A <code>Language</code> mainly consists of a language identifier (e.g.
 * <code>de</code> to identify the German language), and of the language names
 * in the various supported languages.
 */
public class LanguageImpl implements Language {

  /** The backing locale */
  protected Locale locale = null;

  /**
   * Constructor for class Language. The constructor has <code>
	 * package</code> access,
   * because language objects should be instantiated using
   * <code>getLanguage</code> of class <code>LanguageRegistry</code>.
   * 
   * @param identifier
   *          the identifier for this language, e.g. <code>en</code>
   */
  public LanguageImpl(Locale locale) {
    if (locale == null)
      throw new IllegalArgumentException("The locale cannot be null");
    this.locale = locale;
  }

  /**
	 * 
	 */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Returns the name of this language in its own language, e.g
   * <ul>
   * <li><code>English</code> for English</li>
   * <li><code>Deutsch</code> for German</li>
   * <li><code>Français</code> for French</li>
   * </ul>
   * 
   * @return the language name in its own language
   */
  public String getDescription() {
    return locale.getDisplayLanguage();
  }

  /**
   * Returns the name of this language in the specified language, e.g given that
   * <code>language</code> were <code>English</code>, the output would be
   * <ul>
   * <li><code>English</code> for English</li>
   * <li><code>German</code> for German</li>
   * <li><code>French</code> for French</li>
   * </ul>
   * 
   * @param language
   *          the language version of this language
   * @return the language name in the specified language
   */
  public String getDescription(Language language) {
    return locale.getDisplayLanguage(language.getLocale());
  }

  /**
   * Returns the language's identifier, which corresponds to the systems
   * internal name for this language.
   * 
   * @return the language identifier
   */
  public String getIdentifier() {
    return locale.getLanguage();
  }

  /**
   * Returns true if <code>obj</code> is a language object representing the same
   * language than this one.
   * 
   * @param obj
   *          the object to test for equality
   * @return true if <code>obj</code> represents the same language
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof Language) {
      return ((Language) obj).getLocale().equals(locale);
    }
    return false;
  }

  /**
   * Returns the hash code for the language which equals the database key,
   * truncated to an <code>int</code>
   * 
   * @return the language hash code
   */
  public int hashCode() {
    return locale.hashCode();
  }

  /**
   * Returns a string representation of this language.
   * 
   * @return a string representation of this language
   */
  public String toString() {
    return locale.getDisplayLanguage();
  }

}