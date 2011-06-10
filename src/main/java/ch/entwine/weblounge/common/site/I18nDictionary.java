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

package ch.entwine.weblounge.common.site;

import ch.entwine.weblounge.common.language.Language;

/**
 * The dictionary contains all <code>i18n</code> strings for a given site.
 * <p>
 * The dictionary is populated from i18n property files that are found in the
 * site's and module's <code>i18n</code> folders. The files are expected to be
 * named according to the pattern
 * <code>filename_&lt;language&gt;.properties</code>.
 */
public interface I18nDictionary {

  /**
   * Adds the entry to the dictionary's default values.
   * 
   * @param key
   *          the <code>18n</code> key
   * @param value
   *          the <code>18n</code> value
   */
  void add(String key, String value);

  /**
   * Adds the entry to the given language of the dictionary.
   * 
   * @param key
   *          the <code>18n</code> key
   * @param value
   *          the <code>18n</code> value
   * @param language
   *          the language
   */
  void add(String key, String value, Language language);

  /**
   * Removes the entry from the given language dictionary.
   * 
   * @param key
   *          the <code>18n</code> key
   * @param language
   *          the language
   */
  void remove(String key, Language language);

  /**
   * Removes the entry from the dictionary in all available languages.
   * 
   * @param key
   *          the <code>18n</code> key
   */
  void remove(String key);

  /**
   * Returns the localized message identified by <code>key</code> from the
   * default dictionary. If no value for <code>key</code> exists, the key itself
   * is returned.
   * 
   * @param key
   *          the key into the resource bundle
   * @param language
   *          the language variant to return
   * @return the result
   */
  String get(String key);

  /**
   * Returns the localized message identified by <code>key</code>. If no value
   * for <code>key</code> exists, the key itself is returned.
   * 
   * @param key
   *          the key into the resource bundle
   * @param language
   *          the language variant to return
   * @return the result
   */
  String get(String key, Language language);

  /**
   * Returns the localized and HTML-encoded message identified by
   * <code>key</code> from the default entries. If no value for <code>key</code>
   * exists, the key itself is returned.
   * 
   * @param key
   *          the key into the resource bundle
   * @param language
   *          the language variant to return
   * @return the result
   */
  String getAsHTML(String key);

  /**
   * Returns the localized and HTML-encoded message identified by
   * <code>key</code> from the set of entries specified by <code>language</code>
   * . If no value for <code>key</code> exists, the key itself is returned.
   * 
   * @param key
   *          the key into the resource bundle
   * @param language
   *          the language variant to return
   * @return the result
   */
  String getAsHTML(String key, Language language);

}
