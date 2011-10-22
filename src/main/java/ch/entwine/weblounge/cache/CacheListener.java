/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.cache;

import ch.entwine.weblounge.common.request.CacheHandle;
import ch.entwine.weblounge.common.request.CacheTag;

/**
 * A cache listener will, after registration with a cache instance, receive
 * notifications about new and invalidated cache entries.
 */
public interface CacheListener {

  /**
   * Notifies the listener about the addition of a cache entry identified by
   * <code>handle</code>.
   * 
   * @param handle
   *          the handle
   */
  void cacheEntryAdded(CacheHandle handle);

  /**
   * Notifies the listener about the removal of the cache entry identified by
   * <code>handle</code>.
   * 
   * @param handle
   *          the handle
   */
  void cacheEntryRemoved(CacheHandle handle);

  /**
   * Tells the listener that all cache entries that match the given set have
   * been invalidated.
   * 
   * @param tags
   *          the tag set
   */
  void cacheSetInvalidated(CacheTag[] tags);

  /**
   * Callback indicating that the cache has been cleared and all content was
   * removed from it. Note that cache listeners will not receive notifications
   * for each individual entry.
   */
  void cacheCleared();

}
