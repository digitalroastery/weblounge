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

package ch.entwine.weblounge.common.language;

import java.io.Serializable;
import java.util.Locale;

/**
 * A <code>Language</code> consists of a language identifier, e.g.
 * <code>de</code> to identify the German language, and of the language
 * description in the various supported languages. There is also a connection
 * to the associate <code>Locale</code>.
 * 
 * @see Locale
 */
public interface Language extends Serializable, Comparable<Language> {

  /**
   * Returns the locale that is associated with the language.
   * 
   * @return the locale
   */
  Locale getLocale();

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
  String getDescription();

  /**
   * Returns the name of this language in the specified language, e.g given that
   * <code>language</code> was <code>German</code>, this method would return:
   * <ul>
   * <li><code>Englisch</code> for English</li>
   * <li><code>Deutsch</code> for German</li>
   * <li><code>Französisch</code> for French</li>
   * </ul>
   * 
   * @param language
   *          the language version of this language
   * @return the language name in the specified language
   */
  String getDescription(Language language);

  /**
   * Returns the language's identifier, which corresponds to the locales
   * name for this language.
   * 
   * @return the language identifier
   */
  String getIdentifier();

}