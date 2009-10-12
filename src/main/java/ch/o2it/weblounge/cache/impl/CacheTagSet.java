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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A <code>CacheTagSet</code> holds a set of cache tags and specifies either a unique
 * key used in a cache handle or a matcher when it comes to selecting elements from
 * the cache.
 * <p>
 * The set allows multiple tags with the same key, but only one tag with a key-value pair.
 */
public final class CacheTagSet implements Set<Tag>, Iterable<Tag> {

	/** The tags */
	private List<Tag> tags_ = null;

	/**
	 * Creates a new set of cache tags.
	 */
	public CacheTagSet() {
		tags_ = new ArrayList<Tag>();
	}
	
	/**
	 * Adds a new cache tag with the given key and value and inserts it into the
	 * set, provided an identical tag is not already contained.
	 * 
	 * @param key the tag key
	 * @param value the tag value
	 * @return <code>true</code> if the tag could be inserted
	 */
	public boolean add(String key, Object value) {
		return add(new CacheTag(key, value));
	}
	
	/**
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean add(Tag tag) {
		if (!tags_.contains(tag))
			return tags_.add(tag);
		return false;
	}

	/**
	 * Prevents this key from showing up in matching elements or, to put it another
	 * way, no cache elements will be selected that contain a tag with the specified key.
	 * 
	 * @param key the tag key
	 * @return <code>true</code> if the tag could be inserted
	 */
	public boolean prevent(String key) {
		if (key == null)
			throw new IllegalArgumentException("Argument 'key' must not be null!");
		return add(new CacheTag(key, CacheTag.ANY));
	}

	/**
	 * Prevents these keys from showing up in matching elements or, to put it another
	 * way, no cache elements will be selected that contain a tag with the specified keys.
	 * 
	 * @param keys the tag keys
	 * @return <code>true</code> if the tags could be inserted
	 */
	public boolean preventAll(Collection<String> keys) {
		if (keys == null)
			throw new IllegalArgumentException("Argument 'keys' must not be null!");
		boolean changed = false;
		for (String key : keys) {
			changed |= add(new CacheTag(key, CacheTag.ANY));
		}
		return changed;
	}

	/**
	 * @see java.util.Set#addAll(java.util.Collection)
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
	 * @see java.util.Set#clear()
	 */
	public void clear() {
		tags_.clear();
	}

	/**
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return tags_.contains(o);
	}

	/**
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return tags_.containsAll(c);
	}

	/**
	 * @see java.util.Set#isEmpty()
	 */
	public boolean isEmpty() {
		return tags_.size() == 0;
	}

	/**
	 * @see java.util.Set#iterator()
	 */
	public Iterator<Tag> iterator() {
		return tags_.iterator();
	}

	/**
	 * Works like specified in the documentation of {@link Set}, with the exception that
	 * tags contained in the primary key (the <code>CacheSet</code> handed over in
	 * the constructor of this class) will not be removed.
	 * 
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return tags_.remove(o);
	}

	/**
	 * Works like specified in the documentation of {@link Set}, with the exception that
	 * tags contained in the primary key (the <code>CacheSet</code> handed over in
	 * the constructor of this class) will not be removed, even if they are contained in
	 * set <code>c</code>.
	 * 
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return tags_.removeAll(c);
	}

	/**
	 * Works like specified in the documentation of {@link Set}, with the exception that
	 * tags contained in the primary key (the <code>CacheSet</code> handed over in
	 * the constructor of this class) will also be retained, no matter if its contained in
	 * set <code>c</code> or not.
	 * 
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return tags_.retainAll(c);
	}

	/**
	 * @see java.util.Set#size()
	 */
	public int size() {
		return tags_.size();
	}

	/**
	 * @see java.util.Set#toArray()
	 */
	public Object[] toArray() {
		return tags_.toArray();
	}

	/**
	 * @see java.util.Set#toArray(T[])
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