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

package ch.o2it.weblounge.common.content;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.User;

import java.util.Date;

/**
 * A localized modifiable describes an object that knows about a modifier and a
 * modification time in multiple languages.
 * <p>
 * For example, given that there is a page that has been created in German and
 * now been translated to French, there will most likely be modification data
 * for both the German and the French version of that page.
 * 
 * @see Modifiable
 */
public interface LocalizedModifiable extends Localizable {

  /**
   * Returns the date when the object was last modified, regardless of the
   * selected language.
   * 
   * @return the last modification date
   */
  Date getLastModificationDate();

  /**
   * Returns the user that last modified the object, regardless of the selected
   * language.
   * 
   * @return the last modifier
   */
  User getLastModifier();

  /**
   * Returns the date when the object was last modified in the given language.
   * When there is no modification information available in that language, the
   * method returns <code>null</code>.
   * 
   * @param language
   *          the language
   * @return the modification date
   */
  Date getModificationDate(Language language);

  /**
   * Returns the user that last modified the object. When there is no
   * modification information available in that language, the method returns
   * <code>null</code>.
   * 
   * @param language
   *          the language
   * @return the modifier
   */
  User getModifier(Language language);

  /**
   * Returns the date when the object was last modified in the current language.
   * <p>
   * Should the current language be undefined, then this method will return the
   * last modification date as returned by {@link #getLastModificationDate()}.
   * If no modification information is available at all, then this method
   * returns <code>null</code>.
   * 
   * @return the modification time
   * @see #switchTo(Language)
   */
  Date getModificationDate();

  /**
   * Returns the user that last modified the object in the current language.
   * <p>
   * Should the current language be undefined, then this method will return the
   * last modification date as returned by {@link #getLastModificationDate()}.
   * If no modification information is available at all, then this method
   * returns <code>null</code>.
   * 
   * @return the modifier
   * @see #switchTo(Language)
   */
  User getModifier();

}