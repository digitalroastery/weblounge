/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.security.User;

import java.util.Date;

/**
 * A <code>Modifiable</code> describes objects that have a modifier and a
 * modification date.
 */
public interface Modifiable {

  /**
   * Returns the object's modification date.
   * <p>
   * Note that this method may return <code>null</code> if the resource has been
   * created but has never seen an update since then.
   * 
   * @return the modification date
   */
  Date getModificationDate();

  /**
   * Returns the user that last modified the object.
   * <p>
   * Note that this method may return <code>null</code> if the resource has been
   * created but has never seen an update since then.
   * 
   * @return the modifier
   */
  User getModifier();

  /**
   * Returns the date where the resource has last seen a change in state. This
   * method is different from {@link #getModificationDate()} in that it will
   * return the resource's creation date or whatever is reasonable if the
   * resource has not seen an update yet.
   * <p>
   * In short, this method will <b>always</b> return a date and never be null.
   * 
   * @return the date of this resource's last modification
   */
  Date getLastModified();

  /**
   * Returns the user that last modified the object. This method is different
   * from {@link #getModifier()} in that it will return the resource's creator
   * or whatever is reasonable if the resource has not seen an update yet.
   * <p>
   * In short, this method will <b>always</b> return a user and never be null.
   * 
   * @return the user that last modified the resource
   */
  User getLastModifier();

}