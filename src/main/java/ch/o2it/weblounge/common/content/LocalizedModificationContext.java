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
 * This interface defines an object that encapsulates access to modification
 * data.
 */
public interface LocalizedModificationContext extends LocalizedModifiable, Cloneable {

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
   * Returns <code>true</code> if this context contains information about a
   * modification.
   * 
   * @return <code>true</code> is this context was modified
   */
  boolean isModified();

  /**
   * Returns <code>true</code> if this context was modified after the given
   * date.
   * 
   * @param date
   *          the date to compare to
   * @return <code>true</code> is this context was modified after the given date
   */
  boolean isModifiedAfter(Date date);

  /**
   * Returns <code>true</code> if this context was modified before the given
   * date.
   * 
   * @param date
   *          the date to compare to
   * @return <code>true</code> is this context was modified before the given
   *         date
   */
  boolean isModifiedBefore(Date date);

  /**
   * Returns an XML representation of this context for the specified language.
   * If there is no modification for the given language, then <code>null</code>
   * returned.
   * 
   * @param language
   *          the language to consider
   * @return an XML representation of this context or <code>null</code>
   */
  String toXml(Language language);

}