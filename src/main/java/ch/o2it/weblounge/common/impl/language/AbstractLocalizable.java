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

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class represents an abstract implementation of a {@link Localizable}
 * object.
 */
public abstract class AbstractLocalizable implements Localizable {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(AbstractLocalizable.class);

  /**
   * Behaviour to return the language that was originally used to create the
   * object
   */
  public final static int LANG_ORIGINAL = 0;

  /** Behaviour to return the language that has explicitely been set */
  public final static int LANG_CUSTOM = 1;

  /** list of added languages */
  protected Set<Language> languages;

  /** the language that has been provided first */
  protected Language originalLanguage;

  /** the default language */
  protected Language defaultLanguage;

  /** the language that is currently set for this object */
  protected Language selectedLanguage;

  /** the language that is currently used to display this object */
  private Language activeLanguage_;

  /** the selector for the default language selection */
  private int languageBehaviour_;

  /**
   * Constructor for class AbstractMultilingual with a default behaviour of
   * <code>LANG_ORIGINAL</code>.
   */
  public AbstractLocalizable() {
    this(null);
  }

  /**
   * Constructor for class AbstractMultilingual with the given language as the
   * default language.
   * 
   * @param language
   *          the default language
   */
  public AbstractLocalizable(Language language) {
    languages = new HashSet<Language>();
    languageBehaviour_ = (language != null) ? LANG_CUSTOM : LANG_ORIGINAL;
    defaultLanguage = language;
    activeLanguage_ = language;
  }

  /**
   * Resets all language settings, including original, default, selected and
   * active language.
   */
  protected void reset() {
    originalLanguage = null;
    defaultLanguage = null;
    selectedLanguage = null;
    activeLanguage_ = null;
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
    }
  }

  /**
   * Removes the given language from this object.
   * 
   * @param language
   *          the language to be removed
   */
  public void remove(Language language) {
    switch (languageBehaviour_) {
    case LANG_ORIGINAL:
      if (language.equals(originalLanguage))
        throw new IllegalArgumentException("Cannot disable original language!");
      break;
    case LANG_CUSTOM:
      if (language.equals(defaultLanguage))
        throw new IllegalArgumentException("Cannot disable default language!");
      break;
    }
    if (language.equals(activeLanguage_)) {
      activeLanguage_ = getDefaultLanguage();
    }
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
   */
  public void setLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");

    // If the language is available, then select it and initialize
    // the object with this language

    if (supportsLanguage(language)) {
      activeLanguage_ = language;
    }

    // The selected language is not available. It now depends on the
    // language behaviour which language to choose

    else {
      switch (languageBehaviour_) {
      case LANG_ORIGINAL:
        activeLanguage_ = getOriginalLanguage();
        if (activeLanguage_ == null)
          throw new IllegalStateException("Original language must not be null");
        break;
      case LANG_CUSTOM:
        activeLanguage_ = getDefaultLanguage();
        if (activeLanguage_ == null)
          throw new IllegalStateException("Default language must not be null");
        break;
      }
    }

    // Last but not least install language dependend data for the
    // active language

    selectedLanguage = language;
    log_.debug("Active language for object " + toString() + " is " + activeLanguage_);
  }

  /**
   * Makes <code>language</code> the given language, even if the language hasn't
   * been enabled before. This is useful to force empty output on objects that
   * haven't been tranlated so far.<br>
   * Note that calling this method will also call
   * {@link #initLanguage(Language)}.
   * 
   * @param language
   *          the language to force
   */
  public void forceLanguage(Language language) {
    selectedLanguage = language;
    activeLanguage_ = language;
    log_.debug("Active language has been forced to " + selectedLanguage);
  }

  /**
   * Returns the language that is currently used to display the object.
   * 
   * @return the currently active language
   */
  public Language getLanguage() {
    return activeLanguage_;
  }

  /**
   * Returns the selected language. Note that the selected language may differ
   * from the displayed language provided by calling {@link #getLanguage},
   * depending on whether the selected language is supported by the object or
   * not.
   * 
   * @return the selected language
   * @see #setLanguage(Language)
   */
  public Language getSelectedLanguage() {
    return selectedLanguage;
  }

  /**
   * Returns the currently active language.
   * 
   * @return the currently active language
   */
  public Language getActiveLanguage() {
    return activeLanguage_;
  }

  /**
   * Sets the behaviour of this multilingual object in the case that the object
   * description is requested in a language that has not been provided.<br>
   * There are two known behaviours:
   * <ul>
   * <li>{@link #LANG_ORIGINAL}: If the requested language is not in the list of
   * supported languages, then the language is chosen that was used to first
   * create this object.</li>
   * <li>{@link #LANG_CUSTOM}: If the requested language is not in the list of
   * supported languages, then the language is chosen that was set using
   * {@link #setDefaultLanguage(Language)}.
   * </ul>
   * The default for this setting is {@link #LANG_ORIGINAL}.
   * 
   * @param behaviour
   *          the object behaviour
   */
  public void setLanguageBehaviour(int behaviour) {
    switch (behaviour) {
    case LANG_ORIGINAL:
    case LANG_CUSTOM:
      languageBehaviour_ = behaviour;
      break;
    default:
      throw new IllegalArgumentException("Unknown Language behaviour: " + behaviour);
    }
  }

  /**
   * Returns the behaviour of this multilingual object regarding it's default
   * language.
   * 
   * @return the default language behaviour
   * @see #setLanguageBehaviour(int)
   */
  public int getLanguageBehaviour() {
    return languageBehaviour_;
  }

  /**
   * Explicitely sets the default language for this object and switches the
   * language behaviour to {@link #LANG_CUSTOM}.
   * 
   * @param language
   *          the default language
   * @throws IllegalArgumentException
   *           if the argument <code>language</code> was null
   */
  public void setDefaultLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Argument language may not be null!");
    defaultLanguage = language;
    languageBehaviour_ = LANG_CUSTOM;
    if (activeLanguage_ == null)
      activeLanguage_ = defaultLanguage;
  }

  /**
   * Returns the default language. Depending on the behaviour specified by
   * {@link #setDefaultLanguage(Language)}, this method either returns the
   * language that was first used to describe this oject or any custom language.
   * 
   * @return the default language for this object
   */
  public Language getDefaultLanguage() {
    switch (languageBehaviour_) {
    case LANG_ORIGINAL:
      log_.debug("Returning default language. Mode is 'original language'");
      return originalLanguage;
    default:
      log_.debug("Returning default language. Mode is 'selected language'");
      return defaultLanguage;
    }
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
   * (the native language). <br>
   * If the language has not yet been enabled, this method will enable it.
   * 
   * @param language
   *          the original language for this object
   * @throws IllegalArgumentException
   *           if the argument <code>language</code> was null
   */
  public void setOriginalLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Argument language may not be null!");
    enableLanguage(language);
    originalLanguage = language;
    if (activeLanguage_ == null) {
      activeLanguage_ = originalLanguage;
    }
  }

  /**
   * Returns the number of supported languages.
   * 
   * @return the number of supported languages
   */
  public int getLanguageCount() {
    return languages.size();
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
    return languages.contains(language) || language.equals(originalLanguage) || language.equals(defaultLanguage);
  }

  /**
   * Returns true if the selected language equals the original language.
   * 
   * @return true if the current language is the original one
   */
  public boolean isOriginalLanguageSelected() {
    if ((originalLanguage == null) || (selectedLanguage == null))
      return false;
    return originalLanguage.equals(selectedLanguage);
  }

  /**
   * Returns true if the selected language equals the original language.
   * 
   * @return true if the current language is the original one
   */
  public boolean isOriginalLanguageActive() {
    if ((originalLanguage == null) || (activeLanguage_ == null))
      return false;
    return originalLanguage.equals(activeLanguage_);
  }

  /**
   * Returns the languages currently supported by this object.
   * 
   * @return a supported language iteration.
   */
  public Iterator<Language> languages() {
    return languages.iterator();
  }

  /**
   * Returns the component title in the active language. The title is identified
   * by the name "name".
   * 
   * @return the component title.
   */
  public String toString() {
    return toString(getDefaultLanguage());
  }

  /**
   * Returns the title in the requested language or <code>null</code> if the
   * title doesn't exist in that language.
   * 
   * @param language
   *          the requested language
   * @return the object title
   */
  public abstract String toString(Language language);

  /**
   * Returns the object using <code>language</code> as the output language. If
   * no content can be found in that language, then it will be looked up in the
   * default language (unless <code>force</code> is set to <code>true</code>). <br>
   * If this doesn't produce a result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language
   * @param force
   *          <code>true</code> to force the language
   * @return the object's string representation in the given language
   */
  public abstract String toString(Language language, boolean force);

  /**
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language language) {
    if (o != null && language != null) {
      return toString(language).compareTo(o.toString(language));
    } else {
      return 0;
    }
  }

}