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
 * The creation context contains information about when an object was created
 * and who the creator was and can be used by <code>Creatable</code> objects as
 * the backing implementation.
 * <p>
 * The creation context adds additional means of specifying and querying creator
 * and creation date. It also allows for easy serialization and deserialization
 * of <code>Creatable</code> data.
 * 
 * @see Creatable
 */
public interface CreationContext extends Cloneable {

  /**
   * Sets the user that created the object.
   * 
   * @param user
   *          the creator
   */
  void setCreator(User user);

  /**
   * Returns the user that created the object.
   * 
   * @return the creator
   */
  User getCreator();

  /**
   * Sets the creation date.
   * 
   * @param date
   *          the creation date
   */
  void setCreationDate(Date date);

  /**
   * Returns the creation date.
   * 
   * @return the creation date
   */
  Date getCreationDate();

  /**
   * Returns <code>true</code> if this context was created after the given date.
   * 
   * @param date
   *          the date to compare to
   * @return <code>true</code> is this context was created after the given date
   */
  boolean isCreatedAfter(Date date);

  /**
   * Sets the creation date and the user who created the object.
   * 
   * @param creator
   *          the user creating the object
   * @param creationDate
   *          the date of creation
   */
  void setCreated(User creator, Date creationDate);

  /**
   * Creates a clone of this <code>CreationContext</code>.
   * 
   * @return the cloned creation context
   */
  Object clone() throws CloneNotSupportedException;

  /**
   * @see ch.o2it.weblounge.common.content.CreationContext#toXml()
   */
  String toXml();

}