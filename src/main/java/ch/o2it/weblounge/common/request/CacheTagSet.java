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

import ch.o2it.weblounge.common.content.Tag;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * TODO: Comment CacheTagSet
 */
public interface CacheTagSet extends Set<Tag>, Iterable<Tag> {

  /**
   * Adds a new cache tag with the given key and value and inserts it into the
   * set, provided an identical tag is not already contained.
   * 
   * @param key the tag key
   * @param value the tag value
   * @return <code>true</code> if the tag could be inserted
   */
  boolean add(String key, Object value);

  /**
   * @see java.util.Set#add(java.lang.Object)
   */
  boolean add(Tag tag);

  /**
   * Prevents this key from showing up in matching elements or, to put it another
   * way, no cache elements will be selected that contain a tag with the specified key.
   * 
   * @param key the tag key
   * @return <code>true</code> if the tag could be inserted
   */
  boolean prevent(String key);

  /**
   * Prevents these keys from showing up in matching elements or, to put it another
   * way, no cache elements will be selected that contain a tag with the specified keys.
   * 
   * @param keys the tag keys
   * @return <code>true</code> if the tags could be inserted
   */
  boolean preventAll(Collection<String> keys);

  /**
   * @see java.util.Set#addAll(java.util.Collection)
   */
  boolean addAll(Collection<? extends Tag> c);

  /**
   * @see java.util.Set#clear()
   */
  void clear();

  /**
   * @see java.util.Set#contains(java.lang.Object)
   */
  boolean contains(Object o);

  /**
   * @see java.util.Set#containsAll(java.util.Collection)
   */
  boolean containsAll(Collection<?> c);

  /**
   * @see java.util.Set#isEmpty()
   */
  boolean isEmpty();

  /**
   * @see java.util.Set#iterator()
   */
  Iterator<Tag> iterator();

  /**
   * Works like specified in the documentation of {@link Set}, with the exception that
   * tags contained in the primary key (the <code>CacheSet</code> handed over in
   * the constructor of this class) will not be removed.
   * 
   * @see java.util.Set#remove(java.lang.Object)
   */
  boolean remove(Object o);

  /**
   * Works like specified in the documentation of {@link Set}, with the exception that
   * tags contained in the primary key (the <code>CacheSet</code> handed over in
   * the constructor of this class) will not be removed, even if they are contained in
   * set <code>c</code>.
   * 
   * @see java.util.Set#removeAll(java.util.Collection)
   */
  boolean removeAll(Collection<?> c);

  /**
   * Works like specified in the documentation of {@link Set}, with the exception that
   * tags contained in the primary key (the <code>CacheSet</code> handed over in
   * the constructor of this class) will also be retained, no matter if its contained in
   * set <code>c</code> or not.
   * 
   * @see java.util.Set#retainAll(java.util.Collection)
   */
  boolean retainAll(Collection<?> c);

  /**
   * @see java.util.Set#size()
   */
  int size();

  /**
   * @see java.util.Set#toArray()
   */
  Object[] toArray();

  /**
   * @see java.util.Set#toArray(T[])
   */
  <T> T[] toArray(T[] a);

}