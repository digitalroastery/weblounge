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

/**
 * Tag used to identify entries in the caching service.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public final class CacheTag implements Tag {

  /** Special object representing the "any" value */
  public final static Object ANY = new Object();

	/** Tag key */
	String key = null;
	
	/** Tag value */
	Object value = null;
	
	/**
	 * Creates a cache tag with an empty value. This kind of tag can be used to identify
	 * cache elements who must not have a tag with the given key defined.
	 * 
	 * @param key the tag key
	 */
	public CacheTag(String key) {
		this(key, ANY);
	}

	/**
	 * Creates a cache tag with the given name and value.
	 * <p>
	 * Note that a tag value of <code>null</code> is replaced by the final static field
	 * {@link #ANY}.
	 * 
	 * @param key the tag key
	 * @param value the tag value
	 */
	public CacheTag(String key, Object value) {
		if (key == null)
			throw new IllegalArgumentException("Tag key must not be null!");
		if (value == null)
			value = ANY;
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Returns the tag's key name.
	 * 
	 * @return the key name
	 */
	public String getName() {
		return key;
	}

	/**
	 * Returns the tag value. Although values may be arbitrary objects, a few special
	 * values exist:
	 * <ul>
	 * 		<li><i>*</i> - Matches any value for the given key</li>
	 * 		<li><i>Null</i> - Means that a tag with this key must not be defined</li>
	 * </ul>
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CacheTag) {
			CacheTag tag = (CacheTag)obj;
			return key.equals(tag.key) && value.equals(tag.value);
		}
		return super.equals(obj);
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return key.hashCode() + value.hashCode();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return key + " = " + value;
	}
	
}