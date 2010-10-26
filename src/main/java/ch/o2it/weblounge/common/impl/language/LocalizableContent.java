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

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.LocalizationListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This class represents a general object container that is capable of
 * presenting the same content in various languages.
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
 */
public class LocalizableContent<T> extends LocalizableObject implements LocalizationListener, Cloneable {

  /** the content in various languages */
  protected Map<Language, T> content = null;

  /**
   * Constructor for a localizable object with a default behavior of
   * {@link LanguageResolution#Original}.
   */
  public LocalizableContent() {
    content = new HashMap<Language, T>();
  }

  /**
   * Creates localizable content with a default language <code>language</code>
   * and the behavior set to {@link LanguageResolution#Default} as long as
   * <code>language</code> is not set to <code>null</code>, in which case the
   * behavior will be set to {@link LanguageResolution#Original}.
   * 
   * @param language
   *          the default language
   */
  public LocalizableContent(Language language) {
    super(language);
    content = new HashMap<Language, T>();
  }

  /**
   * Creates localizable content that registers with the given
   * <code>localizable</code> to get notified in case of language switches. The
   * <code>localizable</code> will then be called using
   * {@link LocalizationListener#switchedTo(Language)}.
   * 
   * @param localizable
   *          the parent localizable
   */
  public LocalizableContent(LocalizableObject localizable) {
    this(localizable.getDefaultLanguage());
    if (localizable.getOriginalLanguage() != null)
      this.setOriginalLanguage(localizable.getOriginalLanguage());
    if (localizable.getDefaultLanguage() != null)
      this.setDefaultLanguage(localizable.getDefaultLanguage());
    this.setLanguageResolution(localizable.getLanguageResolution());
    localizable.addLocalizationListener(this);
  }

  /**
   * Removes all content from the object, and only the language resolution is
   * kept as is.
   */
  public void clear() {
    content.clear();
    languages.clear();
  }

  /**
   * Returns <code>true</code> if there is no content at all stored in the
   * object.
   * 
   * @return <code>true</code> if the object is empty
   */
  public boolean isEmpty() {
    return content.isEmpty();
  }

  /**
   * Returns the number of localized versions of the content by looking at the
   * number of supported languages.
   * 
   * @return the number of localized versions
   */
  public int size() {
    return content.size();
  }

  /**
   * Returns the content in all languages.
   * 
   * @return the content
   */
  public Collection<T> values() {
    return content.values();
  }

  /**
   * Removes the content as well as support for this language from this object.
   * 
   * @see ch.o2it.weblounge.common.impl.language.LocalizableObject#disableLanguage(ch.o2it.weblounge.common.language.Language)
   */
  @Override
  public void disableLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");
    super.disableLanguage(language);
    content.remove(language);
  }

  /**
   * Adds the content in the given language. Any content that might already be
   * present for the given language will be dropped and returned by this method.
   * 
   * @param content
   *          the content
   * @param language
   *          the language
   */
  public T put(T content, Language language) {
    if (content == null)
      throw new IllegalArgumentException("Content must not be null");
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");

    if (this.content.size() == 0)
      this.originalLanguage = language;
    enableLanguage(language);
    return this.content.put(language, content);
  }

  /**
   * Returns the content in the current language or fallback content as defined
   * by the language resolution strategy.
   * 
   * @return the content
   * @see #getLanguage()
   * @see #getLanguageResolution()
   */
  public T get() {
    return get(getLanguage(), false);
  }

  /**
   * Returns the content in the specified language or fallback content as
   * defined by the language resolution strategy.
   * 
   * @param language
   *          the content language
   * @return the content
   */
  public T get(Language language) {
    return get(language, false);
  }

  /**
   * Returns the content in the specified language or fallback content as
   * defined by the language resolution strategy as long as <code>force</code>
   * is set to <code>true</code>.
   * 
   * @param language
   *          the content language
   * @param force
   *          <code>true</code> to force the language
   * @return the content
   */
  public T get(Language language, boolean force) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");

    T c = content.get(language);
    if (c == null && !force) {
      Language l = null;
      switch (behavior) {
        case Default:
          l = getDefaultLanguage();
          break;
        case Original:
          l = getOriginalLanguage();
          break;
        default:
          throw new IllegalStateException(this + " is neither using default nor original language");
      }
      c = content.get(l);
    }
    return c;
  }

  /**
   * This implementation of the {@link LocalizationListener} switches
   * <code>this</code> to the same language, i. e.
   * <code>this.switchTo(language)</code> is issued as a reaction to this
   * callback.
   * 
   * @see ch.o2it.weblounge.common.language.LocalizationListener#switchedTo(ch.o2it.weblounge.common.language.Language,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void switchedTo(Language language, Language requestedLanguage) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");
    switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#clone()
   */
  @SuppressWarnings("unchecked")
  public Object clone() throws CloneNotSupportedException {
    LocalizableContent<T> c = (LocalizableContent<T>) super.clone();
    c.behavior = behavior;
    c.currentLanguage = currentLanguage;
    c.defaultLanguage = defaultLanguage;
    c.originalLanguage = originalLanguage;

    // languages
    c.languages = new HashSet<Language>();
    c.languages.addAll(languages);

    // content
    c.content = new HashMap<Language, T>();
    c.content.putAll(content);

    return c;
  }

  /**
   * Returns a <code>String</code> representation of the content in the
   * specified language or fallback content as defined by the language
   * resolution strategy as long as <code>force</code> is set to
   * <code>true</code>.
   * 
   * @param language
   *          the language
   * @param force
   *          <code>true</code> to force the language
   * @return the object's string representation in the given language
   */
  public String toString(Language language, boolean force) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");

    T c = content.get(language);

    // Not found? Try the fall back language
    if (c == null && !force)
      c = content.get(resolveLanguage());

    return (c != null) ? c.toString() : null;
  }

}