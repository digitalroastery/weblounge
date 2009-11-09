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

import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * This interface is used to describe objects that know about a creator, a
 * creation time, a modifier and a modification time.
 */
public interface Modifiable {

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
   * Returns the time in milliseconds when the object was last modified.
   * 
   * @return the modification time
   */
  Date getModificationDate();

  /**
   * Returns the user that last modified the object.
   * 
   * @return the modifier
   */
  User getModifier();

}