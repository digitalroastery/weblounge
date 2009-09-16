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

import java.util.Locale;

/**
 * A <code>Language</code> mainly consists of a language identifier, e.g.
 * <code>de</code> to identify the German language, and of the language names in
 * the various supported languages.
 * 
 * TODO Think about a replacement by java locale
 */
public interface Language {

  /** Identifier to locate the language object in the request */
  public static final String ID = "language";

  /**
   * Returns the locale that is backing the language implementation.
   * 
   * @return the locale
   */
  Locale getLocale();

  /**
   * Returns the name of this language in its own language, e.g
   * <ul>
   * <li><code>en</code> for English</li>
   * <li><code>de</code> for German</li>
   * <li><code>fr</code> for French</li>
   * <li><code>it</code> for Italian</li>
   * </ul>
   * 
   * @return the language name in its own language
   */
  String getDescription();

  /**
   * Returns the name of this language in the specified language, e.g
   * <ul>
   * <li><code>en</code> for English</li>
   * <li><code>de</code> for German</li>
   * <li><code>fr</code> for French</li>
   * <li><code>it</code> for Italian</li>
   * </ul>
   * 
   * @param language
   *          the language version of this language
   * @return the language name in the specified language
   */
  String getDescription(Language language);

  /**
   * Returns the language's identifier, which corresponds to the systems
   * internal name for this language.
   * 
   * @return the language identifier
   */
  String getIdentifier();

}