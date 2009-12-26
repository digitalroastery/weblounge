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

package ch.o2it.weblounge.common.request;

import ch.o2it.weblounge.common.content.Taggable;

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
 * @see ch.o2it.weblounge.common.Times
 */
public interface CacheHandle extends Taggable<CacheTag> {

  /**
   * Returns the time the cached object expires in milliseconds. When that time
   * is reached, the object will be invalidated and removed from the cache.
   * 
   * @return the expiration time
   */
  long getExpires();

  /**
   * Sets the time the cached object expires in milliseconds.
   * 
   * @param expires
   *          the expiration time to set
   * @see ch.o2it.weblounge.common.Times
   */
  void setExpires(long expires);

  /**
   * Returns the recheck time of the cached objects in milliseconds. When the
   * recheck time is reached, clients should check if their locally cached
   * version of the object is still valid.
   * 
   * @return the recheck time
   */
  long getRecheck();

  /**
   * Sets the recheck time of the cached object.
   * 
   * @param recheck
   *          the new recheck time
   * @see ch.o2it.weblounge.common.Times
   */
  void setRecheck(long recheck);

}