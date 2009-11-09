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

package ch.o2it.weblounge.common.impl.util.datatype;

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * A set implementation backed by an <code>IdentityHashMap</code>. 
 */
public final class IdentityHashSet<E> extends AbstractSet<E> implements Set<E> {
	
	/** the values in the backing map */
	private static final Object VALUE = new Object();
	
	/** the backing map */
	private IdentityHashMap<E, Object> map;
	
	/** create a new identity hash set */
	public IdentityHashSet() { 
		map = new IdentityHashMap<E, Object>();
	}
	
	/**
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	@Override
	public boolean add(E e) {
		return map.put(e, VALUE) == null;
	}
	
	/**
	 * @see java.util.AbstractCollection#clear()
	 */
	@Override
	public void clear() {
		map.clear();
	}
	
	/**
	 * @see java.util.AbstractCollection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}
	
	/**
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	/**
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}
	
	/**
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		return map.remove(o) == VALUE;
	}
	
	/**
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return map.size();
	}
}