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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.content.Creatable;
import ch.o2it.weblounge.common.content.LocalizedModifiable;
import ch.o2it.weblounge.common.content.Publishable;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SystemPermission;

import org.w3c.dom.Node;

/**
 * A page element is a piece of content, placed somewhere on a page. Depending
 * on the composer that created it, it consists of multiple elements and
 * properties.
 * <p>
 * Such an element is stored in a page in the following form:
 * 
 * <pre>
 * 	&lt;pagelet&gt;
 * 		&lt;content language=&quot;de&quot; original=&quot;true&quot;&gt;
 * 			&lt;modified&gt;
 * 				&lt;date/&gt;
 * 				&lt;user&gt;wunden&lt;/user&gt;
 * 			&lt;/modified&gt;
 * 			&lt;text id=&quot;keyword&quot;&gt;Keyword&lt;/text&gt;
 * 			&lt;text id=&quot;title&quot;&gt;My Big Title&lt;/text&gt;
 * 			&lt;text id=&quot;lead&quot;&gt;This is the leading sentence.&lt;/text&gt;
 * 		&lt;/content&gt;
 * 		&lt;property id=&quot;showauthor&quot;&gt;true&lt;/property&gt;
 * 		&lt;property id=&quot;showdate&quot;&gt;true&lt;/property&gt;
 * 	&lt;/pagelet&gt;
 * </pre>
 */
public interface Pagelet extends Localizable, Creatable, LocalizedModifiable, Publishable, Securable {

  /** Pagelet identifier in request */
  static final String ID = "pagelet";

  /** Identifier for passing additional data with the request */
  static final String ATTRIBUTES = "pagelet-attributes";

  /** The pagelet's permissions */
  static final Permission[] permissions = new Permission[] {
    SystemPermission.READ,
    SystemPermission.WRITE,
    SystemPermission.TRANSLATE,
    SystemPermission.MANAGE
  };

  /**
   * Returns the defining module identifier.
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
   * Returns the property with name <code>key</code> or the empty string if no
   * such property is found.
   * 
   * @param key
   *          the property name
   * @return the property value
   */
  String getProperty(String key);

  /**
   * Returns <code>true</code> if this is a multivalue property.
   * 
   * @param key
   *          the key
   * @return <code>true</code> if this key holds more than one value
   */
  boolean isMultiValueProperty(String key);

  /**
   * Returns the array of values for the multivalue property <code>key</code>.
   * This method returns <code>null</code> if no value has been stored at all
   * for the given key, a single element string array if there is excactly one
   * string and an array of strings of all values in all other cases.
   * 
   * @param key
   *          the value's name
   * @return the value collection
   */
  String[] getMultiValueProperty(String key);

  /**
   * Returns <code>true</code> if this content element holds more than one
   * entry.
   * 
   * @param name
   *          the element name
   * @return <code>true</code> if this is multidimensional content
   */
  boolean isMultiValueContent(String name);

  /**
   * Returns the multivalue content in the specified language. If the language
   * is forced using the <code>force</code> parameter, then this method will
   * return <code>null</code> if there is no entry in this language. Otherwise,
   * the content is returned in the default language.
   * <p>
   * If this is single value content, then this method returns an array
   * containing only the single value.
   * 
   * @see #getContent(String, Language, boolean)
   * @see ch.o2it.weblounge.common.page.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language, boolean)
   * @see ch.o2it.weblounge.common.language.MultilingualObject#getDefaultLanguage()
   */
  String[] getMultiValueContent(String name, Language language, boolean force);

  /**
   * Returns the multivalue content in the specified language. If there is no
   * entry in this language, the content is returned in the default language.
   * <p>
   * If this is single value content, then this method returns an array
   * containing only the single value.
   * 
   * @see #getContent(String, Language)
   * @see ch.o2it.weblounge.common.page.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   * @see ch.o2it.weblounge.common.language.MultilingualObject#getDefaultLanguage()
   */
  String[] getMultiValueContent(String name, Language language);

  /**
   * Returns the multivalue content in the specified language. If there is no
   * entry in this language, the content is returned in the active language.
   * <p>
   * If this is single value content, then this method returns an array
   * containing only the single value.
   * 
   * @see #getContent(String)
   * @see ch.o2it.weblounge.common.page.Pagelet#getContent(java.lang.String)
   * @see ch.o2it.weblounge.common.language.MultilingualObject#getActiveLanguage()
   */
  String[] getMultiValueContent(String name);

  /**
   * Returns the content in the required language. If no content can be found in
   * that language, then it will be looked up in the default language. If that
   * doesn't produce a result as well, <code>null</code> is returned.
   * 
   * @param name
   *          the content name
   * @param language
   *          the content language
   * @return the content
   */
  String getContent(String name, Language language);

  /**
   * Returns the content in the required language. If no content can be found in
   * that language, then it will be looked up in the default language (unless
   * <code>force</code> is set to <code>true</code>). If that doesn't produce a
   * result as well, <code>null</code> is returned.
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
   * Returns the content element with the given identifier or <code>null</code>
   * if no such content was found.
   * 
   * @param name
   *          the content identifier
   * @return the content
   */
  String getContent(String name);

  /**
   * Returns the identifier of the renderer used to render this pagelet.
   * 
   * @return the pagelet's renderer
   */
  String getRenderer();

  /**
   * Returns the pagelet location, containing information about url, composer
   * and composer position.
   * 
   * @return the pagelet location
   */
  PageletLocation getLocation();

  /**
   * Returns an XML representation of this pagelet.
   * 
   * @return an XML representation of this pagelet
   */
  Node toXml();

}