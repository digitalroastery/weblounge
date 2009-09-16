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

import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.LanguageManager;
import ch.o2it.weblounge.common.language.UnsupportedLanguageException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * <code>LanguageRegistry</code> provides <code>Factory</code> behaviour for
 * <code>Language</code> objects. Besides listing of all available languages,
 * <code>LanguageRegistry</code> provides methods to retreive
 * <code>Language</code> objects and to get/set the default language for the
 * system.
 * </p>
 * <p>
 * <b>Note:</b>The <code>LanguageRegistry</code> will throw a
 * <code>LanguageException</code> if no language can be found in the community
 * database. So at least one languge must be defined.
 * </p>
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since WebLounge 2.0
 */

public class LanguageManagerImpl implements LanguageManager {

  /** the default language */
  private Language defaultLanguage_;

  /** the list of languages */
  private Map<String, Language> languages_;

  /**
   * Creates a language registry.
   */
  public LanguageManagerImpl() {
    languages_ = new HashMap<String, Language>();
  }

  /**
   * Creates a language registry for a specific site.
   * 
   * @param defaultLanguage
   *          the default language
   */
  public LanguageManagerImpl(Language defaultLanguage) {
    this();
    setDefaultLanguage(defaultLanguage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ch.o2it.weblounge.common.language.LanguageManager#getLanguage(java.lang
   * .String)
   */
  public Language getLanguage(String identifier)
      throws UnsupportedLanguageException {
    Arguments.checkNull(identifier, "identifier");
    Language language = languages_.get(identifier);
    if (language == null)
      throw new UnsupportedLanguageException(identifier);
    return language;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ch.o2it.weblounge.common.language.LanguageManager#getDefaultLanguage()
   */
  public Language getDefaultLanguage() {
    return defaultLanguage_;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ch.o2it.weblounge.common.language.LanguageManager#languages()
   */
  public Iterator<Language> languages() {
    return languages_.values().iterator();
  }

  /**
   * Adds a language to the registry. This method has package access because it
   * should only be called at configuration time by the <code>SiteManager</code>
   * .
   * 
   * @param language
   *          the language to be added
   */
  void addLanguage(Language language) {
    Arguments.checkNull(language, "language");
    languages_.put(language.getIdentifier(), language);
  }

  /**
   * Removes a language from the registry. This method has package access
   * because it should only be called at configuration time by the
   * <code>SiteManager</code>.
   * 
   * @param language
   *          the language to be removed
   */
  void removeLanguage(Language language) {
    Arguments.checkNull(language, "language");
    languages_.remove(language.getIdentifier());
  }

  /**
   * Sets the default language. If the language was not registered before then
   * it is added to the registry now. This method has package access because it
   * should only be called at configuration time by the <code>SiteManager</code>
   * .
   * 
   * @param language
   *          the default language
   */
  void setDefaultLanguage(Language language) {
    if (!languages_.containsValue(language))
      addLanguage(language);
    defaultLanguage_ = language;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ch.o2it.weblounge.common.language.LanguageManager#getLanguageCount()
   */
  public int getLanguageCount() {
    return languages_.size();
  }

}