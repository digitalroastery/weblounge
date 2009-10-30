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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.request.CacheTagSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A <code>CacheTagSet</code> holds a set of cache tags and specifies either a unique
 * key used in a cache handle or a matcher when it comes to selecting elements from
 * the cache.
 * <p>
 * The set allows multiple tags with the same key, but only one tag with a key-value pair.
 */
public final class CacheTagSetImpl implements CacheTagSet {

	/** The tags */
	private List<Tag> tags_ = null;

	/**
	 * Creates a new set of cache tags.
	 */
	public CacheTagSetImpl() {
		tags_ = new ArrayList<Tag>();
	}
	
	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#add(java.lang.String, java.lang.Object)
   */
	public boolean add(String key, Object value) {
		return add(new CacheTagImpl(key, value));
	}
	
	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#add(ch.o2it.weblounge.common.content.Tag)
   */
	public boolean add(Tag tag) {
		if (!tags_.contains(tag))
			return tags_.add(tag);
		return false;
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#prevent(java.lang.String)
   */
	public boolean prevent(String key) {
		if (key == null)
			throw new IllegalArgumentException("Argument 'key' must not be null!");
		return add(new CacheTagImpl(key, CacheTagImpl.ANY));
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#preventAll(java.util.Collection)
   */
	public boolean preventAll(Collection<String> keys) {
		if (keys == null)
			throw new IllegalArgumentException("Argument 'keys' must not be null!");
		boolean changed = false;
		for (String key : keys) {
			changed |= add(new CacheTagImpl(key, CacheTagImpl.ANY));
		}
		return changed;
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#addAll(java.util.Collection)
   */
	public boolean addAll(Collection<? extends Tag> c) {
		boolean inserted = false;
		for (Tag tag : c) {
			if (!tags_.contains(tag)) {
				tags_.add(tag);
				inserted = true;
			}
		}
		return inserted;
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#clear()
   */
	public void clear() {
		tags_.clear();
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#contains(java.lang.Object)
   */
	public boolean contains(Object o) {
		return tags_.contains(o);
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#containsAll(java.util.Collection)
   */
	public boolean containsAll(Collection<?> c) {
		return tags_.containsAll(c);
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#isEmpty()
   */
	public boolean isEmpty() {
		return tags_.size() == 0;
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#iterator()
   */
	public Iterator<Tag> iterator() {
		return tags_.iterator();
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#remove(java.lang.Object)
   */
	public boolean remove(Object o) {
		return tags_.remove(o);
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#removeAll(java.util.Collection)
   */
	public boolean removeAll(Collection<?> c) {
		return tags_.removeAll(c);
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#retainAll(java.util.Collection)
   */
	public boolean retainAll(Collection<?> c) {
		return tags_.retainAll(c);
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#size()
   */
	public int size() {
		return tags_.size();
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#toArray()
   */
	public Object[] toArray() {
		return tags_.toArray();
	}

	/**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.request.CacheTagSet#toArray(T[])
   */
	public <T> T[] toArray(T[] a) {
		return tags_.toArray(a);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return tags_.toString();
	}
}