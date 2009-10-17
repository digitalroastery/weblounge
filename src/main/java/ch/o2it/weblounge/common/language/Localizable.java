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

import java.util.Set;

/**
 * This interface defines methods for multilingual object. Such an object is
 * capable of representing it's content in various languages.
 */
public interface Localizable {

  /**
   * The behavior that is chosen when a decision needs to be made about which
   * language to take if the requested language version is not available.
   */
  public enum LanguageResolution {
    Original, Default
  };

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
  Set<Language> languages();

  /**
   * Makes <code>language</code> the current language for this object.
   * <p>
   * Depending on whether the localizable supports the language, a fall back
   * might be chosen. This can be determined by comparing the returned language
   * to <code>returned</code>.
   * 
   * @param language
   *          the language to switch to
   * @param language
   *          the selected language
   * @throws IllegalArgumentException
   *           if the language argument is <code>null</code>
   * @throws IllegalStateException
   *           if no fall back language can be determined
   */
  Language switchTo(Language language);

}