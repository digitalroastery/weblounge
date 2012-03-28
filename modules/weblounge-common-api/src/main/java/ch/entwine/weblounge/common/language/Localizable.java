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

import java.util.Set;

/**
 * This interface defines methods for multilingual objects. Such an object is
 * capable of representing it's content in various languages and usually also
 * implements fallback strategies if content is missing in a given language but
 * available in others. This is where <i>language resolution</i> comes into the
 * game.
 * <p>
 * Usually, you will have an initial version of the content, say in French.
 * Imagine you added a translation of that content in English (your default
 * language), and now a user wants to see the content in German. Since that
 * localization of the content is not available, what are you going to present
 * to the user?
 * <p>
 * Depending on the {@link LanguageResolution} specified for this object, the
 * user will either see the original content (French) or the content in the
 * default language (English).
 * <p>
 * The same applies to the {@link #switchTo(Language)} method, that is intended
 * to switch the object's current language to <code>language</code>. Depending
 * on whether there actually is content in that language, the object might
 * either be switched accordingly or to another language that is chosen using
 * the selected language resolution strategy. However, using
 * {@link #switchTo(Language)} allows to handle the <code>Localizable</code> in
 * a way as if there were no localized versions available.
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
   * Returns <code>true</code> if the given language is supported, i. e. if
   * there is content available in that language. Note that the object might be
   * able to show content based on the language resolution strategy, but it is
   * not required to do so.
   * 
   * @param language
   *          a language
   * @return <code>true</code> if the language is supported
   */
  boolean supportsLanguage(Language language);

  /**
   * Returns an iteration of all languages that are supported by the
   * localizable.
   * 
   * @return the supported languages
   */
  Set<Language> languages();

  /**
   * Makes <code>language</code> the current language for this localizable.
   * <p>
   * Depending on whether the object supports the language, a fallback might be
   * chosen and the object is switched to the next best language with respect to
   * the language resolution strategy. This can be determined by comparing the
   * method's return value to <code>language</code>.
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

  /**
   * Compares this Localizable with the specified Localizable for order. Returns
   * a negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   * 
   * <p>
   * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
   * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>. (This implies
   * that <tt>x.compareTo(y)</tt> must throw an exception iff
   * <tt>y.compareTo(x)</tt> throws an exception.)
   * 
   * <p>
   * The implementor must also ensure that the relation is transitive:
   * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
   * <tt>x.compareTo(z)&gt;0</tt>.
   * 
   * <p>
   * Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
   * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for all
   * <tt>z</tt>.
   * 
   * <p>
   * It is strongly recommended, but <i>not</i> strictly required that
   * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>. Generally speaking, any
   * class that implements the <tt>Comparable</tt> interface and violates this
   * condition should clearly indicate this fact. The recommended language is
   * "Note: this class has a natural ordering that is inconsistent with equals."
   * 
   * <p>
   * In the foregoing description, the notation <tt>sgn(</tt><i>expression</i>
   * <tt>)</tt> designates the mathematical <i>signum</i> function, which is
   * defined to return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according
   * to whether the value of <i>expression</i> is negative, zero or positive.
   * 
   * @param o
   *          the object to be compared.
   * @param l
   *          the language to be used for the comparison
   * @return a negative integer, zero, or a positive integer as this object is
   *         less than, equal to, or greater than the specified object.
   * 
   * @throws ClassCastException
   *           if the specified object's type prevents it from being compared to
   *           this object.
   */
  int compareTo(Localizable o, Language l);

  /**
   * Returns the <code>String</code> representation in the requested language or
   * <code>null</code> if the title doesn't exist in that language.
   * <p>
   * This implementation forwards the request to
   * {@link #toString(Language, boolean)}.
   * 
   * @param language
   *          the requested language
   * @return the object title
   */
  String toString(Language language);

  /**
   * Returns the <code>String</code> representation in the specified language.
   * If no content can be found in that language, then it will be looked up in
   * the default language (unless <code>force</code> is set to <code>true</code>
   * ). <br>
   * If this doesn't produce a result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language
   * @param force
   *          <code>true</code> to force the language
   * @return the object's string representation in the given language
   */
  String toString(Language language, boolean force);

}