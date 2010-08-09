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

package ch.o2it.weblounge.common.content.page;

import ch.o2it.weblounge.common.content.Creatable;
import ch.o2it.weblounge.common.content.LocalizedModifiable;
import ch.o2it.weblounge.common.content.Publishable;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * A pagelet is a piece of content, placed inside a composer on a page. It
 * consists of multilingual content and properties that are valid for all
 * languages.
 * <p>
 * During lifetime, the pagelet keeps track of creation, modification and
 * publishing processes.
 */
public interface Pagelet extends Creatable, Publishable, LocalizedModifiable, Securable {

  /** The pagelet's permissions */
  static final Permission[] permissions = new Permission[] {
      SystemPermission.READ,
      SystemPermission.WRITE,
      SystemPermission.TRANSLATE,
      SystemPermission.MANAGE };

  /**
   * Returns identifier of the module that contains the pagelet.
   * 
   * @return the module identifier
   */
  String getModule();

  /**
   * Returns the pagelet identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Returns the names of the properties stored inside this pagelet.
   * 
   * @return the property names
   */
  String[] getPropertyNames();

  /**
   * Adds a property to this pagelet. Properties are language independent, so
   * there is no need to pass the language.
   * 
   * @param key
   *          the property name
   * @param value
   *          the property value
   */
  void setProperty(String key, String value);

  /**
   * Returns the property with name <code>key</code> or <code>null</code> if no
   * such property is found.
   * 
   * @param key
   *          the property name
   * @return the property value
   */
  String getProperty(String key);

  /**
   * Returns <code>true</code> if this is a multiple value property.
   * 
   * @param key
   *          the key
   * @return <code>true</code> if this key holds more than one value
   */
  boolean isMultiValueProperty(String key);

  /**
   * Returns the values for the multiple value property <code>key</code>. This
   * method returns <code>null</code> if no value has been stored at all for the
   * given key, a single element string array if there is exactly one string and
   * an array of strings containing the values in all other cases.
   * 
   * @param key
   *          the property name
   * @return the property values
   */
  String[] getMultiValueProperty(String key);

  /**
   * Returns the names of the content elements stored inside this pagelet for
   * the given language.
   * 
   * @param language
   *          the language
   * @return the content names
   */
  String[] getContentNames(Language language);

  /**
   * Sets the pagelet's content in the given language. If the content identified
   * by <code>name</code> has already been assigned, then the content element is
   * converted into a multiple value content element.
   * 
   * @see ch.o2it.weblounge.core.language.MultilingualObject#setContent(java.lang.String,
   *      java.lang.Object, ch.o2it.weblounge.api.language.Language)
   * @see #isMultiValueContent(String)
   */
  void setContent(String name, String value, Language language);

  /**
   * Returns <code>true</code> if this content element holds more than one
   * entry. This is the case if {@link #setContent(String, String, Language)}
   * has been called multiple times with the same element name.
   * 
   * @param name
   *          the element name
   * @return <code>true</code> if this content element holds more than one value
   */
  boolean isMultiValueContent(String name);

  /**
   * Returns the content in the specified language. If that language version is
   * not available, the original version is looked up, unless <code>force</code>
   * is set to <code>true</code>, which will lead to <code>null</code> being
   * returned instead.
   * <p>
   * If there is no content with the given name, this method will return
   * <code>null</code>.
   * 
   * @see #getContent(String, Language, boolean)
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language, boolean)
   */
  String[] getMultiValueContent(String name, Language language, boolean force);

  /**
   * Returns the content in the specified language. If that language version is
   * not available, the original version is looked up.
   * <p>
   * If there is no content with the given name, this method will return
   * <code>null</code>.
   * 
   * @see #getContent(String, Language)
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  String[] getMultiValueContent(String name, Language language);

  /**
   * Returns the content in the pagelet's current language. If that language
   * version is not available, the original version is looked up.
   * <p>
   * If there is no content with the given name, this method will return
   * <code>null</code>.
   * 
   * @see #getContent(String)
   * @see #switchTo(Language)
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getContent(java.lang.String)
   */
  String[] getMultiValueContent(String name);

  /**
   * Returns the content in the specified language. If that language version is
   * not available, the original version is looked up.
   * <p>
   * If there is no content with the given name, this method will return
   * <code>null</code>.
   * 
   * @param name
   *          the content name
   * @param language
   *          the content language
   * @return the content
   */
  String getContent(String name, Language language);

  /**
   * Returns the content in the specified language. If that language version is
   * not available, the original version is looked up, unless <code>force</code>
   * is set to <code>true</code>, which will lead to <code>null</code> being
   * returned instead.
   * <p>
   * If there is no content with the given name, this method will return
   * <code>null</code>.
   * 
   * @param name
   *          the content name
   * @param language
   *          the content language
   * @param force
   *          <code>true</code> to force the language
   * @return the content
   */
  String getContent(String name, Language language, boolean force);

  /**
   * Returns the content in the pagelet's current language. If that language
   * version is not available, the original version is looked up.
   * <p>
   * If there is no content with the given name, this method will return
   * <code>null</code>.
   * 
   * @param name
   *          the content name
   * @return the content
   */
  String getContent(String name);

  /**
   * Sets the pagelet's location, containing the page that it's on, the composer
   * that it is in and the position within that composer.
   * 
   * @param uri
   *          the pagelet uri
   */
  void setURI(PageletURI uri);

  /**
   * Returns the pagelet location, containing information about url, composer
   * and composer position.
   * 
   * @return the pagelet location
   */
  PageletURI getURI();

  /**
   * Sets the user that created the pagelet along with the creation date.
   * 
   * @param creator
   *          the user creating the pagelet
   * @param creationDate
   *          the creation date
   */
  void setCreated(User creator, Date creationDate);

  /**
   * Sets the user that last modified the object in the given language as well
   * as the modification date.
   * 
   * @param user
   *          the user that modified the object
   * @param date
   *          the date of modification
   * @param language
   *          the language version that was modified
   */
  void setModified(User user, Date date, Language language);

  /**
   * Sets the publisher and the publishing start and end date.
   * 
   * @param publisher
   *          the publisher
   * @param from
   *          publishing start date
   * @param to
   *          publishing end date
   */
  void setPublished(User publisher, Date from, Date to);

  /**
   * Returns an XML representation of this pagelet.
   * 
   * @return an XML representation of this pagelet
   */
  String toXml();

}