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

import static ch.o2it.weblounge.common.language.Localizable.LanguageResolution.Default;
import static ch.o2it.weblounge.common.language.Localizable.LanguageResolution.Original;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.language.LocalizationListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents an abstract implementation of a {@link Localizable}
 * object.
 */
public abstract class LocalizableObject implements Localizable {

  /** list of added languages */
  protected Set<Language> languages = null;

  /** the language that has been provided first */
  protected Language originalLanguage = null;

  /** the default language */
  protected Language defaultLanguage = null;

  /** the language that is currently set for this object */
  protected Language currentLanguage = null;

  /** the selector for the default language selection */
  protected LanguageResolution behavior = Original;

  /** list of objects interested in language switches */
  protected List<LocalizationListener> localizationListeners = null;

  /**
   * Constructor for class AbstractMultilingual with a default behavior of
   * {@link LanguageBehaviour#Original}.
   */
  public LocalizableObject() {
    this(null);
  }

  /**
   * Constructor for class AbstractMultilingual with the given language as the
   * default language.
   * 
   * @param defaultLanguage
   *          the default language
   */
  public LocalizableObject(Language defaultLanguage) {
    this.defaultLanguage = defaultLanguage;
    languages = new HashSet<Language>();
    behavior = (defaultLanguage != null) ? Default : Original;
    localizationListeners = new ArrayList<LocalizationListener>();
  }

  /**
   * Adds the listener to the list of localization listeners.
   * 
   * @param listener
   *          the listener to add
   */
  public void addLocalizationListener(LocalizationListener listener) {
    synchronized (localizationListeners) {
      localizationListeners.add(listener);
    }
  }

  /**
   * Removes the listener from the list of localization listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  public void removeLocalizationListener(LocalizationListener listener) {
    synchronized (localizationListeners) {
      localizationListeners.remove(listener);
    }
  }

  /**
   * Resets all language settings, including original, default, selected and
   * active language.
   */
  protected void reset() {
    originalLanguage = null;
    defaultLanguage = null;
    currentLanguage = null;
    languages.clear();
  }

  /**
   * Enables the given language for this object.
   * 
   * @param language
   *          the language to enable
   */
  public void enableLanguage(Language language) {
    if (!languages.contains(language)) {
      languages.add(language);
      if (languages.size() == 1) {
        originalLanguage = language;
        currentLanguage = language;
      }
    }
  }

  /**
   * Removes the given language from this object.
   * 
   * @param language
   *          the language to be removed
   */
  public void remove(Language language) {
    if (behavior.equals(Original) && language.equals(originalLanguage))
      throw new IllegalStateException("Cannot disable original language!");
    else if (behavior.equals(Default) && language.equals(defaultLanguage))
      throw new IllegalStateException("Cannot disable default language!");

    if (language.equals(currentLanguage))
      currentLanguage = null;
    languages.remove(language);
  }

  /**
   * Makes <code>language</code> the currently selected language for this
   * object.
   * 
   * @param language
   *          the language to switch to
   * @throws IllegalArgumentException
   *           if the language argument is <code>null</code>
   * @throws IllegalStateException
   *           if no fall back language can be determined
   */
  public Language switchTo(Language language) {
    return switchTo(language, false);
  }

  /**
   * Makes <code>language</code> the currently selected language for this
   * object.
   * 
   * @param language
   *          the language to switch to
   * @param force
   *          force the language and don't fall back to either original or
   *          default language
   * @throws IllegalArgumentException
   *           if the language argument is <code>null</code>
   * @throws IllegalStateException
   *           if <code>force</code> is <code>false</code> and no fall back
   *           language can be determined
   */
  public Language switchTo(Language language, boolean force) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");

    // Remember the current language
    Language original = currentLanguage;

    // If the language is available, then simply select it
    if (supportsLanguage(language)) {
      currentLanguage = language;
    }

    // If the language is forced but not available, then throw an exception
    else if (force) {
      throw new IllegalStateException(this + " is not localized to " + language.getLocale().getDisplayLanguage());
    }

    // The selected language is not available. Use the original language instead
    else if (behavior.equals(Original)) {
      if (getOriginalLanguage() != null)
        currentLanguage = getOriginalLanguage();
    }

    // The selected language is not available. Use the default language instead
    else if (behavior.equals(Default)) {
      if (getDefaultLanguage() != null)
        currentLanguage = getDefaultLanguage();
    }

    // Check the resolution process outcome
    if (currentLanguage == null && languages.size() > 0)
      throw new IllegalStateException("Language resolution failed for " + this);

    // Notify interested parties
    if (original == null || !original.equals(currentLanguage)) {
      synchronized (localizationListeners) {
        for (LocalizationListener l : localizationListeners) {
          l.switchedTo(language);
        }
      }
    }

    return currentLanguage;
  }

  /**
   * Returns the language that is currently used to display the object.
   * 
   * @return the currently active language
   */
  public Language getLanguage() {
    return currentLanguage;
  }

  /**
   * Returns a fall back language according to the current
   * {@link LanguageResolution}.
   * 
   * @return the fall back language
   */
  protected Language resolveLanguage() {
    if (behavior.equals(Original) && originalLanguage != null)
      return originalLanguage;
    else if (behavior.equals(Default) && defaultLanguage != null)
      return defaultLanguage;
    else
      throw new IllegalStateException("Language resolution failed");
  }

  /**
   * Sets the behavior of this localizable object in the case that the object
   * description is requested in a language that has not been provided.<br>
   * There are two known behaviors:
   * <ul>
   * <li>{@link LanguageResolution#Original}: If the requested language is not
   * in the list of supported languages, then the language is chosen that was
   * used to first create this object.</li>
   * <li>{@link LanguageResolution#Default}: If the requested language is not in
   * the list of supported languages, then the language is chosen that was set
   * using {@link #setDefaultLanguage(Language)}.
   * </ul>
   * The default for this setting is {@link LanguageResolution#Original}.
   * 
   * @param behavior
   *          the language behavior
   * @throws IllegalStateException
   *           if a default language has not been specified but
   *           {@link LanguageResolution#Default} has been chosen for language
   *           resolution
   */
  public void setLanguageResolution(LanguageResolution behavior)
      throws IllegalStateException {
    if (Default.equals(behavior) && defaultLanguage == null)
      throw new IllegalStateException("Must specify default language first");
    this.behavior = behavior;
  }

  /**
   * Returns the behavior of this multilingual object regarding it's default
   * language.
   * 
   * @return the default language behavior
   * @see #setLanguageResolution(int)
   */
  public LanguageResolution getLanguageResolution() {
    return behavior;
  }

  /**
   * Explicitly sets the default language.
   * <p>
   * If the language has not yet been enabled, this method will enable it.
   * 
   * @param language
   *          the default language
   * @throws IllegalArgumentException
   *           if the argument <code>language</code> was null
   */
  public void setDefaultLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language may not be null!");

    defaultLanguage = language;
  }

  /**
   * Returns the default language.
   * 
   * @return the default language for this object
   */
  public Language getDefaultLanguage() {
    return defaultLanguage;
  }

  /**
   * Returns the original language of this object or <code>null</code>, if no
   * original language exists.
   * 
   * @return the original language
   */
  public Language getOriginalLanguage() {
    return originalLanguage;
  }

  /**
   * Sets the original language for this object. The original language is
   * considered to be the language that was first used to describe the object
   * (the native language).
   * <p>
   * If the language has not yet been enabled, this method will enable it.
   * 
   * @param language
   *          the original language for this object
   * @throws IllegalArgumentException
   *           if the argument <code>language</code> was null
   */
  public void setOriginalLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language may not be null!");

    originalLanguage = language;
  }

  /**
   * Returns <code>true</code> if the given language is supported, i. e. if the
   * language has been added using <code>addLanguage()</code>.
   * 
   * @param language
   *          the language to be supported
   * @return <code>true</code> if the language is supported
   */
  public boolean supportsLanguage(Language language) {
    return languages.contains(language);
  }

  /**
   * Returns the languages currently supported by this object.
   * 
   * @return a supported language iteration.
   */
  public Set<Language> languages() {
    return languages;
  }

}