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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents a basic implementation of a {@link Localizable} object
 * that brings support for all the language handling and dealing with language
 * resolution strategies.
 * <p>
 * To keep the implementation simple, localized content can only be returned as
 * a <code>String</code> using {@link #toString()}, {@link #toString(Language)}
 * or {@link #toString(Language, boolean)}. To manage more complex content, it
 * might be worth looking at {@link LocalizableContent}.
 * <p>
 * To be able to properly handle this object it should be noted that every
 * localizable content will have an <i>original</i> language, a
 * <code>current language</code> and possibly also a <i>default language</i>.
 * <ul>
 * <li><b>Original language</b>: the original language is automatically set as
 * soon as localized content is added and thus represents the language of that
 * first content.</li>
 * <li><b>Current language</b>: the current language is what the object has been
 * set to using the <code>switchTo(Language)</code> method. If that method has
 * never been called, then the current language is either the original language
 * or the default one if it has been set. In any case, as long as there is
 * <i>some</code> content in the object, the current language will always be
 * part of what is returned by <code>getSupportedLanguages()</code>, i. e.
 * <code>supportsLanguage(currentLanguage)</code> will be true as long as
 * <code>getCurrentLanguage()</code> does not return <code>null</code>.</li>
 * <li><b>Default language</b>: The default language can be specified on a
 * localized object as needed and yields an alternative way of getting content
 * that is not available in the requested language.</li>
 * </ul>
 * 
 * @see LocalizableContent
 */
public class LocalizableObject implements Localizable {

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
   * Creates a new localizable object with a default behavior of
   * {@link LanguageBehaviour#Original}.
   */
  public LocalizableObject() {
    this(null);
  }

  /**
   * Creates localizable object with a default language <code>language</code>
   * and the behavior set to {@link LanguageResolution#Default} as long as
   * <code>language</code> is not set to <code>null</code>, in which case the
   * behavior will be set to {@link LanguageResolution#Original}.
   * 
   * @param identifier
   *          the default language
   */
  public LocalizableObject(Language defaultLanguage) {
    this.defaultLanguage = defaultLanguage;
    languages = new TreeSet<Language>();
    behavior = (defaultLanguage != null) ? Default : Original;
    localizationListeners = new ArrayList<LocalizationListener>();
  }

  /**
   * Adds the listener to the list of localization listeners. The listeners will
   * be notified as soon as a call to {@link #switchTo(Language)} or
   * {@link #switchTo(Language, boolean)} has been made.
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
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");

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
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");

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
      fireLanguageChanged(currentLanguage, language);
    }

    return currentLanguage;
  }

  /**
   * Notifies the registered localization listeners about the newly selected
   * language.
   * 
   * @param language
   *          the language that has been switched to
   * @param requested
   *          the language that was originally requested
   */
  protected void fireLanguageChanged(Language language, Language requested) {
    synchronized (localizationListeners) {
      for (LocalizationListener l : localizationListeners) {
        l.switchedTo(language, requested);
      }
    }
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
    if (language == null && behavior.equals(Default))
      throw new IllegalArgumentException("Default language may not be null while language resolution is set to default");

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
      throw new IllegalArgumentException("Language must not be null!");

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
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");
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

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable)
   */
  public int compareTo(Localizable o, Language l) {
    if (o == null)
      throw new IllegalArgumentException("Localizable must not be null");
    if (l == null)
      throw new IllegalArgumentException("Language must not be null");

    if (o instanceof LocalizableObject) {
      return toString(l).compareTo(((LocalizableObject) o).toString(l));
    }
    return toString(l).compareTo(o.toString());
  }

  /**
   * Returns the string representation in the current language.
   * <p>
   * This implementation forwards the request to
   * {@link #toString(Language, boolean)}.
   * 
   * @return the component title.
   */
  public String toString() {
    return toString(resolveLanguage(), false);
  }

  /**
   * Returns the string representation in the requested language or
   * <code>null</code> if the title doesn't exist in that language.
   * <p>
   * This implementation forwards the request to
   * {@link #toString(Language, boolean)}.
   * 
   * @param language
   *          the requested language
   * @return the object title
   */
  public String toString(Language language) {
    return toString(language, false);
  }

  /**
   * Returns the string representation in the specified language. If no content
   * can be found in that language, then it will be looked up in the default
   * language (unless <code>force</code> is set to <code>true</code>). <br>
   * If this doesn't produce a result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language
   * @param force
   *          <code>true</code> to force the language
   * @return the object's string representation in the given language
   */
  public String toString(Language language, boolean force) {
    return super.toString();
  }

}