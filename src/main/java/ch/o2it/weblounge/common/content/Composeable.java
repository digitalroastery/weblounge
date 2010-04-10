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

import javax.servlet.http.HttpServletRequest;

/**
 * This interface defines common methods and fields for composeable objects like
 * templates, pagelets, actions and images.
 * <p>
 * A <code>Composeable</code> is an object that is being rendered as a response
 * or a part of a response to a request. When the Composeable represents a
 * response part only, it might want to place include instructions into the
 * <code>&lt;head&gt;</code> section of the enclosing <code>HTML</code> page. To
 * do this, it just needs to return these includes in {@link #getHTMLHeaders()} and
 * {@link #getScripts()}.
 * <p>
 * Composeables are usually used to display content to the user. Often, this
 * content might become invalid and needs to be recalculated in some way. The
 * composeable can indicate this to the system by returning proper values in
 * {@link #getValidTime()} and {@link #getRecheckTime()}.
 */
public interface Composeable extends Localizable {

  /**
   * Sets the composeable identifier.
   * 
   * @param identifier
   *          the identifier
   */
  void setIdentifier(String identifier);

  /**
   * Returns the composeable identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Set to <code>true</code> if this composeable should available to the user.
   * 
   * @param composeable
   *          <code>true</code> if the composeable might be selected by the user
   */
  void setComposeable(boolean composeable);

  /**
   * Returns <code>true</code> if the object is composeable, which means that it
   * may be selected by the user rather than only being used through code.
   * 
   * @return <code>true</code> if the composeable is composeable
   */
  boolean isComposeable();

  /**
   * Sets the name in the specified language.
   * 
   * @param name
   *          the name
   * @param language
   *          the language
   */
  void setName(String name, Language language);

  /**
   * Returns the composeable name.
   * 
   * @return the composeable name
   */
  String getName();

  /**
   * Returns the composeable name in the required language. If not available,
   * the name will be returned in the default language.
   * 
   * @param language
   *          the required language
   * @return the composeable name
   */
  String getName(Language language);

  /**
   * Returns the composeable name in the required language. If not available,
   * either <code>null</code> or the name in a fallback language will be
   * returned, depending on the value of <code>force</code>.
   * 
   * @param language
   *          the required language
   * @param force
   *          <code>true</code> to force a <code>null</code> value rather then a
   *          fallback language
   * @return the composeable name
   */
  String getName(Language language, boolean force);

  /**
   * Sets the number of milliseconds that content represented by this
   * composeable will remain valid.
   * 
   * @param time
   *          the valid time
   */
  void setValidTime(long time);

  /**
   * Returns the amount of time in milliseconds that output using this
   * composeable will be valid. When this time has been exceeded, content
   * generated using this composeable should be removed from any cache systems
   * and be regenerated.
   * 
   * @return the valid time
   */
  long getValidTime();

  /**
   * Sets the number of milliseconds that clients may assume that content
   * represented by this composeable will be valid. After that time, clients
   * need to check back and make sure it is still unmodified and valid.
   * 
   * @param time
   *          the recheck time
   */
  void setRecheckTime(long time);

  /**
   * Returns the amount of time in milliseconds that output using this
   * composeable is likely to still be valid. However, clients should check to
   * make sure that this actually is the case.
   * 
   * @return the recheck time
   */
  long getRecheckTime();

  /**
   * Adds a link or script to the list of includes.
   * 
   * @param header
   *          the include
   */
  void addHTMLHeader(HTMLHeadElement header);

  /**
   * Removes a link or a script from the list of includes.
   * 
   * @param header
   *          the include
   */
  void removeHTMLHeader(HTMLHeadElement header);

  /**
   * Returns the &lt;link&gt; or &lt;script&gt; elements that have been defined
   * for this action. They will be set as attributes in the
   * {@link HttpServletRequest}, where they are available to the page renderer
   * so that they can be included in the page's <code>&lt;head&gt;</code>
   * section.
   * 
   * @return the includes
   */
  HTMLHeadElement[] getHTMLHeaders();

}
