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
import ch.o2it.weblounge.common.security.User;

import java.util.Date;

/**
 * This interface defines an object that encapsulates access to creation and
 * modification data.
 */
public interface ModificationContext extends Cloneable {

  /**
   * Sets the creation date of this object.
   * 
   * @param date
   *          the creation date
   */
  void setCreationDate(Date date);

  /**
   * Sets the user that created the object.
   * 
   * @param user
   *          the creator
   */
  void setCreator(User user);

  /**
   * Returns the date when the object was created.
   * 
   * @return the creation time
   */
  Date getCreationDate();

  /**
   * Returns the user that created the object.
   * 
   * @return the creator
   */
  User getCreator();

  /**
   * Sets the date of the last modification of this object.
   * 
   * @param date
   *          the modfication date
   * @param language
   *          the locale
   */
  void setModificationDate(Date date, Language language);

  /**
   * Sets the user that last modified the object.
   * 
   * @param editor
   *          the modifying user
   */
  void setModifier(User editor, Language language);

  /**
   * Returns <code>true</code> if this context contains information about a
   * modification.
   * 
   * @param language
   *          the locale
   * @return <code>true</code> is this context was modified
   */
  boolean isModified(Language language);

  /**
   * Returns the date when the object was last modified.
   * 
   * @param language
   *          the locale
   * @return the modification time
   */
  Date getModificationDate(Language language);

  /**
   * Returns the user that last modified the object.
   * 
   * @param language
   *          the locale
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