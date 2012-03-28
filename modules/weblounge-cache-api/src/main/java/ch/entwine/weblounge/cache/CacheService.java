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

package ch.entwine.weblounge.cache;

import ch.entwine.weblounge.common.request.ResponseCache;

/**
 * The <code>ResponseCacheService</code> is a service providing an
 * implementation of the {@link ResponseCache} interface along with some methods
 * to manage the cache and get cache statistics.
 */
public interface CacheService extends ResponseCache {

  /**
   * Resets the cache statistics.
   */
  void resetStatistics();

  /**
   * Clears the cache.
   */
  void clear();

  /**
   * Returns the cache identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Tells the cache implementation to stop caching and do cleanup work.
   */
  void shutdown();

  /**
   * Adds <code>listener</code> to the list of cache listeners.
   * 
   * @param listener
   *          the cache listener to add
   */
  void addCacheListener(CacheListener listener);

  /**
   * Removes <code>listener</code> from the list of cache listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  void removeCacheListener(CacheListener listener);

}
