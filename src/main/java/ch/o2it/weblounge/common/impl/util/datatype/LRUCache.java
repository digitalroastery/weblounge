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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The <code>LRUCache</code> keeps the most recently used items for fast access.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public final class LRUCache<K, V> {

  /** The serial version id */
  private static final long serialVersionUID = 6642425005899107195L;

  /** The cache head element */
  private CacheHandle<V> head;

  /** The cache tail element */
  private CacheHandle<V> tail;

  /** The cache */
  private Map<K, CacheHandle<V>> cache;

  /** The cache listeners */
  private List<LRUCacheListener<K, V>> listeners;

  /** Default cache size */
  public static final int DEFAULT_CACHESIZE = 100;

  /** Actual cache size */
  private int cacheSize;

  /** Maximum cache size */
  private int maxCacheSize_ = DEFAULT_CACHESIZE;

  // Logging

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(LRUCache.class);

  /**
   * Creates a new cache.
   */
  public LRUCache() {
    this(DEFAULT_CACHESIZE);
  }

  /**
   * Creates a new cache with the maximum capacity of <code>size</code>.
   * 
   * @param size
   *          the maximum size
   */
  public LRUCache(int size) {
    cache = new HashMap<K, CacheHandle<V>>();
    listeners = new ArrayList<LRUCacheListener<K, V>>();
    head = null;
    tail = null;
    cacheSize = 0;
    maxCacheSize_ = size;
  }

  /**
   * Adds an item to the registry.
   * 
   * @param key
   *          the key
   * @param item
   *          the item to store
   * @see ch.o2it.weblounge.common.impl.util.registry.Registry#put(java.lang.Object,
   *      java.lang.Object)
   */
  public void put(K key, V item) {
    if (key == null)
      throw new IllegalArgumentException("Key must not be null!");
    if (item == null)
      throw new IllegalArgumentException("Item must not be null!");

    // If cache size is too big, drop one
    if (cacheSize >= maxCacheSize_) {
      log_.debug("Cache is full. Dropping last recently used element");
      CacheHandle<V> dropped = dropLRUEntry();
      if (dropped != null) {
        fireCacheElementDropped(dropped.item);
      }
    }

    CacheHandle<V> cacheHandle = new CacheHandle<V>(item);

    // Rechain the cache
    synchronized (this) {
      cacheSize++;
      cacheHandle.setNext(head);
      head = cacheHandle;
      if (tail == null)
        tail = cacheHandle;
    }

    // Add the new element
    CacheHandle<V> added = cache.put(key, cacheHandle);
    if (added != null) {
      fireCacheElementDropped(added.item);
    }
  }

  /**
   * Returns the item for a given key or <code>null</code> if the item has not
   * yet been added to the cache.
   * 
   * @param key
   *          the key
   * @return the associated item
   * @see ch.o2it.weblounge.common.impl.util.registry.Registry#get(java.lang.Object)
   */
  public V get(K key) {
    CacheHandle<V> cacheHandle = cache.get(key);
    V item = null;
    if (cacheHandle != null) {
      item = cacheHandle.item;
      log_.debug("Hit: Item '" + key + "' found in cache");
      if (cacheHandle != head) {
        synchronized (this) {
          if (cacheHandle == tail)
            tail = cacheHandle.prev;
          cacheHandle.prev.setNext(cacheHandle.next);
          cacheHandle.setNext(head);
          cacheHandle.setPrevious(null);
          head = cacheHandle;
        }
      }
    } else {
      log_.debug("Miss: Item '" + key + "' not found in cache");
    }
    return item;
  }

  /**
   * Returns the removed item or <code>null</code> if the item was not found.
   * 
   * @param key
   *          the key
   * @return the removed item
   */
  public V remove(K key) {
    CacheHandle<V> cacheHandle = cache.remove(key);
    V item = null;
    if (cacheHandle != null) {
      item = cacheHandle.item;
      synchronized (this) {
        cacheSize--;
        if (cacheHandle != head) {
          if (cacheHandle == tail)
            tail = cacheHandle.prev;
          cacheHandle.prev.setNext(cacheHandle.next);
        } else {
          head = null;
          tail = null;
        }
      }
      log_.debug(cacheHandle + " invalidated");
    }
    if (item != null)
      fireCacheElementRemoved(item);
    return item;
  }

  /**
   * Returns an iteration of the cached items.
   * 
   * @return an iteration of items
   */
  public Iterator pages() {
    return head;
  }

  /**
   * Sets the maximum size for the cache.
   * 
   * @param size
   *          the maximum cache size
   */
  public void setMaximumCacheSize(int size) {
    maxCacheSize_ = size;
    log_.debug("Maximum cache size is " + size);
    while (cacheSize > maxCacheSize_) {
      CacheHandle<V> dropped = dropLRUEntry();
      if (dropped != null) {
        fireCacheElementDropped(dropped.item);
      }
    }
  }

  /**
   * Returns the maximum size for the cache.
   * 
   * @return the maximum cache size
   */
  public int getMaximumCacheSize() {
    return maxCacheSize_;
  }

  /**
   * Clears the cache.
   */
  public void clear() {
    head = null;
    tail = null;
    for (CacheHandle<V> handle : cache.values()) {
      fireCacheElementDropped(handle.item);
    }
    cache.clear();
    cacheSize = 0;
  }

  /**
   * Returns the current size of the cache.
   * 
   * @return the current cache size
   */
  public long getCacheSize() {
    return cacheSize;
  }

  /**
   * Adds the listener to the list of cache listeners.
   * 
   * @param listener
   *          the listener to add
   */
  public void addCacheListener(LRUCacheListener<K, V> listener) {
    listeners.add(listener);
  }

  /**
   * Removes the listener from the list of cache listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  public void removeCacheListener(LRUCacheListener<K, V> listener) {
    listeners.add(listener);
  }

  /**
   * Method to drop the entry that is marked as the tail.
   */
  protected CacheHandle<V> dropLRUEntry() {
    log_.debug("Dropping last recently used element");
    if (tail != null) {
      synchronized (this) {
        CacheHandle<V> handle = tail;
        if (handle.prev != null) {
          tail = handle.prev;
          tail.next = null;
        }
        handle.prev = null;
        cache.remove(handle);
        cacheSize--;
        return handle;
      }
    }
    return null;
  }

  /**
   * This method is used to inform listeners about a newly added element.
   * 
   * @param element
   *          the new element
   */
  protected void fireCacheElementAdded(V element) {
    for (LRUCacheListener<K, V> listener : listeners) {
      listener.cacheElementAdded(this, element);
    }
  }

  /**
   * This method is used to inform listeners about a newly added element.
   * 
   * @param element
   *          the new element
   */
  protected void fireCacheElementRemoved(V element) {
    for (LRUCacheListener<K, V> listener : listeners) {
      listener.cacheElementRemoved(this, element);
    }
  }

  /**
   * This method is used to inform listeners about a newly added element.
   * 
   * @param element
   *          the new element
   */
  protected void fireCacheElementDropped(V element) {
    for (LRUCacheListener<K, V> listener : listeners) {
      listener.cacheElementDropped(this, element);
    }
  }

  /**
   * Dumps the cache contents.
   */
  String dump() {
    String dump = "\n\tCachesize: " + cacheSize + "\n";
    dump += "\tMaximum Cachesize: " + maxCacheSize_ + "\n";
    CacheHandle handle = head;
    int i = 2;
    while (handle != null) {
      if (handle == head)
        dump += "\t[head] ";
      else if (handle == tail)
        dump += "\t[tail] ";
      else
        dump += "\t[" + i++ + "] ";
      dump += handle.item.toString() + "\n";
      handle = handle.next;
    }
    return dump;
  }

  /**
   * Inner class used to build a double linked chain of cached items. The chain
   * start is identified by <code>getPrevious() == null</code>, the last element
   * has a next element of <code>null</code>.
   * 
   * @author Tobias Wunden
   * @version 1.0
   * @since Weblounge 2.0
   */
  private class CacheHandle<H> implements Iterator {

    /** References used to maintain the chain */
    CacheHandle<H> prev, next;

    /** Cached element */
    H item;

    /**
     * Creates a new cache handle to the given <code>item</code>.
     * 
     * @param item
     *          the cached element
     */
    CacheHandle(H item) {
      this.item = item;
    }

    /**
     * Links this element to <code>hdl</code> as its next element.
     * 
     * @param hdl
     *          the next element
     */
    void setNext(CacheHandle<H> hdl) {
      if (hdl != null) {
        hdl.prev = this;
      }
      next = hdl;
    }

    /**
     * Links this element to <code>hdl</code> as its previous element.
     * 
     * @param hdl
     *          the previous element
     */
    void setPrevious(CacheHandle<H> hdl) {
      if (hdl != null) {
        hdl.next = this;
      }
      prev = hdl;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return (next != null);
    }

    /**
     * @see java.util.Iterator#next()
     */
    public CacheHandle<H> next() {
      return next;
    }

    /**
     * This method is not allowed for this iterator and will therefore throw a
     * <code>{@link UnsupportedOperationException}</code>
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
      return item.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
      if (o != null && o instanceof CacheHandle) {
        CacheHandle pch = (CacheHandle) o;
        return item.equals(pch.item);
      }
      return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return item.toString();
    }
  }

}