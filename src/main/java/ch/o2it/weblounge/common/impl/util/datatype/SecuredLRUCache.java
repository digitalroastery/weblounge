/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.datatype;

import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.user.AuthenticatedUser;

/**
 * LRU cache for secured objects.
 * 
 * @author Tobias Wunden
 * 
 */
public class SecuredLRUCache<K, V extends Securable> {

  /** The cache */
  private LRUCache<K,V> cache_ = null;

  /**
   * Creates a new secured cache.
   */
  public SecuredLRUCache() {
    cache_ = new LRUCache<K,V>();
  }

  /**
   * Creates a new secured cache with a maximum capacity of <code>size</code>.
   * 
   * @param size
   *          the maximum capacity
   */
  public SecuredLRUCache(int size) {
    cache_ = new LRUCache<K, V>(size);
  }

  /**
   * Returns the secured item if it was found in the cache and passed the
   * security check.
   * 
   * @param key
   *          the lookup key
   * @param permission
   *          the permission to gain
   * @param user
   *          the user that wants access
   * @return the secured object
   */
  public V get(K key, Permission permission, AuthenticatedUser user) {
    V item = cache_.get(key);
    if (item == null)
      return null;
    return item.check(permission, user) ? item : null;
  }

  /**
   * Puts an object into the cache
   * 
   * @param key
   *          the key
   * @param item
   *          the item to be cached
   */
  public void put(K key, V item) {
    cache_.put(key, item);
  }

  /**
   * Removes the object from the cache.
   * 
   * @param key
   *          the key
   */
  public V remove(K key) {
    return cache_.remove(key);
  }

}