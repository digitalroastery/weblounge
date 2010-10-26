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

package ch.o2it.weblounge.common.impl.request;

import ch.o2it.weblounge.common.request.CacheTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A <code>CacheTagSet</code> holds a set of cache tags and specifies either a
 * unique key used in a cache handle or a matcher when it comes to selecting
 * elements from the cache.
 * <p>
 * The set allows multiple tags with the same key, but only one tag with a
 * key-value pair.
 */
public final class CacheTagSet implements Set<CacheTag> {

  /** The tags */
  private List<CacheTag> tags = null;

  /**
   * Creates a new set of cache tags.
   */
  public CacheTagSet() {
    tags = new ArrayList<CacheTag>();
  }

  /**
   * Adds a new cache tag with the given key and value and inserts it into the
   * set, provided an identical tag is not already contained.
   * 
   * @param key
   *          the tag key
   * @param value
   *          the tag value
   * @return <code>true</code> if the tag could be inserted
   */
  public boolean add(String key, Object value) {
    return add(new CacheTagImpl(key, value));
  }

  /**
   * @see java.util.Set#add(java.lang.Object)
   */
  public boolean add(CacheTag tag) {
    if (!tags.contains(tag))
      return tags.add(tag);
    return false;
  }

  /**
   * Prevents this key from showing up in matching elements or, to put it
   * another way, no cache elements will be selected that contain a tag with the
   * specified key.
   * 
   * @param key
   *          the tag key
   * @return <code>true</code> if the tag could be inserted
   */
  public boolean excludeTagsWith(String key) {
    if (key == null)
      throw new IllegalArgumentException("Key must not be null!");
    return add(new CacheTagImpl(key, CacheTag.ANY));
  }

  /**
   * Prevents these keys from showing up in matching elements or, to put it
   * another way, no cache elements will be selected that contain a tag with the
   * specified keys.
   * 
   * @param keys
   *          the tag keys
   * @return <code>true</code> if the tags could be inserted
   */
  public boolean excludeTagsWith(Collection<String> keys) {
    if (keys == null)
      throw new IllegalArgumentException("Keys must not be null!");
    boolean changed = false;
    for (String key : keys) {
      changed |= add(new CacheTagImpl(key, CacheTag.ANY));
    }
    return changed;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#addAll(java.util.Collection)
   */
  public boolean addAll(Collection<? extends CacheTag> c) {
    boolean inserted = false;
    for (CacheTag tag : c) {
      if (!tags.contains(tag)) {
        tags.add(tag);
        inserted = true;
      }
    }
    return inserted;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#clear()
   */
  public void clear() {
    tags.clear();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#contains(java.lang.Object)
   */
  public boolean contains(Object o) {
    return tags.contains(o);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection<?> c) {
    return tags.containsAll(c);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#isEmpty()
   */
  public boolean isEmpty() {
    return tags.size() == 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#iterator()
   */
  public Iterator<CacheTag> iterator() {
    return tags.iterator();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.util.Set#remove(java.lang.Object)
   */
  public boolean remove(Object o) {
    return tags.remove(o);
  }

  /**
   * Removes the tag with the given name and value.
   * 
   * @param name
   *          the tag name
   * @param value
   *          the tag value
   * @return <code>true</code> if the tag has been removed
   */
  public boolean remove(String name, Object value) {
    List<CacheTag> candidates = new ArrayList<CacheTag>();
    for (CacheTag t : tags) {
      if (t.getName().equals(name) && t.getValue().equals(value))
        candidates.add(t);
    }
    return tags.removeAll(candidates);
  }

  /**
   * Works like specified in the documentation of {@link Set}, with the
   * exception that tags contained in the primary key (the <code>CacheSet</code>
   * handed over in the constructor of this class) will not be removed, even if
   * they are contained in set <code>c</code>.
   * 
   * @see java.util.Set#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection<?> c) {
    return tags.removeAll(c);
  }

  /**
   * Removes all tags with the given tag name.
   * 
   * @param name
   *          the tag name
   * @return <code>true</code> if at least one tag has been removed
   */
  public boolean removeAllByTagName(String name) {
    List<CacheTag> candidates = new ArrayList<CacheTag>();
    for (CacheTag t : tags) {
      if (t.getName().equals(name))
        candidates.add(t);
    }
    return tags.removeAll(candidates);
  }

  /**
   * {@inheritDoc}
   *
   * @see java.util.Set#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection<?> c) {
    return tags.retainAll(c);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#size()
   */
  public int size() {
    return tags.size();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#toArray()
   */
  public Object[] toArray() {
    return tags.toArray();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Set#toArray(T[])
   */
  public <T> T[] toArray(T[] a) {
    return tags.toArray(a);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return tags.toString();
  }

}