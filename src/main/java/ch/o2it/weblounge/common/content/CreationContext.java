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
 * TODO: Comment CreationContext
 */
public interface CreationContext extends Cloneable {

  /**
   * @see ch.o2it.weblounge.common.content.CreationContext#getCreationDate()
   */
  Date getCreationDate();

  /**
   * @see ch.o2it.weblounge.common.content.CreationContext#getCreator()
   */
  User getCreator();

  /**
   * @see ch.o2it.weblounge.common.content.CreationContext#getCreationDate(java.util.Date)
   */
  void setCreationDate(Date date);

  /**
   * @see ch.o2it.weblounge.common.content.CreationContext#setCreator(ch.o2it.weblounge.common.user.User)
   */
  void setCreator(User user);

  /**
   * @see ch.o2it.weblounge.common.content.CreationContext#isCreatedAfter(java.util.Date)
   */
  boolean isCreatedAfter(Date date);

  /**
   * Creates a clone of this <code>CreationContext</code>.
   * 
   * @return the cloned creation context
   */
  Object clone();

  /**
   * @see ch.o2it.weblounge.common.content.CreationContext#toXml()
   */
  String toXml();

}