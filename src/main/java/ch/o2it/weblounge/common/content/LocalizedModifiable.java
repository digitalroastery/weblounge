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

import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.User;

import java.util.Date;

/**
 * This interface is used to describe objects that know about a creator, a
 * creation time, a modifier and a modification time.
 */
public interface LocalizedModifiable extends Modifiable, Localizable {

  /**
   * Returns <code>true</code> if this context contains information about a
   * modification to the version identified by <code>language</code>.
   * 
   * @return <code>true</code> is this context' language version was modified
   */
  boolean isModifiedAtAll();

  /**
   * Returns <code>true</code> if this context contains information about a
   * modification.
   * 
   * @param date
   *          the date to compare to
   * @return <code>true</code> is this context was modified
   */
  boolean isModifiedAtAllAfter(Date date);

  /**
   * Returns the time in milliseconds when the object was last modified,
   * regardless of the selected language.
   * 
   * @return the modification time
   */
  Date getLastModificationDate();

  /**
   * Returns the user that last modified the object, regardless of the selected
   * language.
   * 
   * @return the modifier
   */
  User getLatModifier();

}