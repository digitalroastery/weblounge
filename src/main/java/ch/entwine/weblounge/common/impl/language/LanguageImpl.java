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

package ch.entwine.weblounge.common.impl.language;

import ch.entwine.weblounge.common.language.Language;

import java.util.Locale;

/**
 * A <code>Language</code> mainly consists of a language identifier (e.g.
 * <code>de</code> to identify the German language), and of the language names
 * in the various supported languages.
 */
public class LanguageImpl implements Language {

  /** Serial version uid */
  private static final long serialVersionUID = 9213260651821758529L;

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
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.language.Language#getLocale()
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.language.Language#getDescription()
   */
  public String getDescription() {
    return locale.getDisplayLanguage();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.language.Language#getDescription(ch.entwine.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    return locale.getDisplayLanguage(language.getLocale());
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.language.Language#getIdentifier()
   */
  public String getIdentifier() {
    return locale.getLanguage();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Language l) {
    return locale.getISO3Language().compareTo(l.getLocale().getISO3Language());
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof Language) {
      return ((Language) obj).getLocale().equals(locale);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return locale.hashCode();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return locale.getDisplayLanguage();
  }

}