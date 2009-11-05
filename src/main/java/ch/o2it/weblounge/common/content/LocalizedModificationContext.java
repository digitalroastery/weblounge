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
import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * This interface defines an object that encapsulates access to creation and
 * modification data.
 */
public interface LocalizedModificationContext extends LocalizedModifiable, Cloneable {

  /**
   * Sets the user that created the object.
   * 
   * @param user
   *          the creator
   */
  void setCreator(User user);

  /**
   * Sets the creation date of this object.
   * 
   * @param date
   *          the creation date
   */
  void setCreationDate(Date date);

  /**
   * Sets creator and creation date.
   * 
   * @param user
   *          the user that created the object
   * @param date
   *          the date of creation
   */
  void setCreated(User user, Date date);

  /**
   * Sets the date of the last modification of this object in the current
   * language.
   * 
   * @param date
   *          the modification date
   */
  void setModificationDate(Date date);

  /**
   * Sets the date of the last modification of this object.
   * 
   * @param date
   *          the modification date
   * @param language
   *          the language
   */
  void setModificationDate(Date date, Language language);

  /**
   * Sets the user that last modified the object.
   * 
   * @param editor
   *          the modifying user
   */
  void setModifier(User editor);

  /**
   * Sets the user that last modified the object.
   * 
   * @param editor
   *          the modifying user
   * @param language
   *          the affected language version
   */
  void setModifier(User editor, Language language);

  /**
   * Sets creator and creation date.
   * 
   * @param user
   *          the user that created the object
   * @param date
   *          the date of creation
   * @param language
   *          the language version that was modified
   */
  void setModified(User user, Date date, Language language);

  /**
   * Returns the time in milliseconds when the object was last modified.
   * 
   * @param language
   *          the language
   * @return the modification time
   */
  Date getModificationDate(Language language);

  /**
   * Returns the user that last modified the object.
   * 
   * @param language
   *          the language
   * @return the modifier
   */
  User getModifier(Language language);

  /**
   * Returns an XML representation of this context.
   * 
   * @return an XML representation of this context
   */
  String toXml();

}