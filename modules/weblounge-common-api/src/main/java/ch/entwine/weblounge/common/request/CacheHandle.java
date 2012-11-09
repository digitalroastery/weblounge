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

package ch.entwine.weblounge.common.request;

import ch.entwine.weblounge.common.content.Taggable;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Identifies a cached object with a recheck and expiration time. Clients may
 * locally cache objects and use them without checking with the object's
 * original source until <code>recheck</code> time is reached. At that point,
 * the client should check if the object has been modified. Once the expiration
 * time has been reached, the object must be thrown away and reloaded from the
 * original source.
 * <p>
 * Implementing classes must provide meaningful <code>hashCode()</code> and
 * </code>equals()</code> methods.
 * 
 * @see ch.entwine.weblounge.common.Times
 */
public interface CacheHandle extends Serializable, Taggable<CacheTag> {

  /**
   * Returns the key that is used to reference the entry in the cache.
   * 
   * @return the key
   */
  String getKey();

  /**
   * Returns the date where this handle was created.
   * 
   * @return the creation date
   */
  long getCreationDate();

  /**
   * Sets the handle's modification date to be maximum of what has been set as
   * the modification date already and <code>modificationDate</code>.
   * <p>
   * Using this method over the course of a request allows everyone to add their
   * 2 cents to what the modification date should really be and at the end,
   * {@link #getModificationDate()} will return the most recent date as the
   * result.
   * 
   * @param modificationDate
   *          the modification date
   * @return the current modification date
   */
  Date setModificationDate(Date modifcationDate);

  /**
   * Returns the date when the content identified by this handle was last
   * modified.
   * 
   * @return the content's modification date
   */
  Date getModificationDate();

  /**
   * Returns the time the cached object expires in milliseconds. When that time
   * is reached, the object will be invalidated and removed from the cache.
   * 
   * @return the expiration time
   */
  long getCacheExpirationTime();

  /**
   * Sets the time the cached object expires in milliseconds.
   * 
   * @param expires
   *          the expiration time to set
   * @see ch.entwine.weblounge.common.Times
   */
  void setCacheExpirationTime(long expires);

  /**
   * Returns the recheck time of the cached objects in milliseconds. When the
   * recheck time is reached, clients should check if their locally cached
   * version of the object is still valid.
   * 
   * @return the recheck time
   */
  long getClientRevalidationTime();

  /**
   * Sets the recheck time of the cached object.
   * 
   * @param recheck
   *          the new recheck time
   * @see ch.entwine.weblounge.common.Times
   */
  void setClientRevalidationTime(long recheck);

  /**
   * Returns the tags in a set.
   * 
   * @return the set
   */
  Set<CacheTag> getTagSet();

}