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
 * TODO: Comment LanguageManager
 */
public interface LanguageManager {

  /**
   * Returns a language object representing the requested language, if found in
   * the community database, otherwise the method throws an
   * <code>UnsupportedLanguageException</code>.
   * <p>
   * The language identifier must match the system name of the language, as
   * configured in the system configuration file, e. g.
   * <ul>
   * <li><code>en</code> for English</li>
   * <li><code>de</code> for German</li>
   * <li><code>fr</code> for French</li>
   * <li><code>it</code> for Italian</li>
   * </ul>
   * 
   * @param identifier
   *          language identifier
   * @return a language object for the specified language
   * @throws UnsupportedLanguageException
   *           if the language is not found
   */
  public abstract Language getLanguage(String identifier)
      throws UnsupportedLanguageException;

  /**
   * <p>
   * Returns the default language of the community system. Then, if a user
   * enters the site, and it cannot be determined, which language he or she
   * likes, this language is provided.
   * </p>
   * 
   * @return the default language
   */
  public abstract Language getDefaultLanguage();

  /**
   * Returns an iterator providing all available system languages.
   * 
   * @return all available languages
   */
  public abstract Iterator<Language> languages();

  /**
   * Returns the number of registered languages.
   * 
   * @return the number of languages
   */
  public abstract int getLanguageCount();

}