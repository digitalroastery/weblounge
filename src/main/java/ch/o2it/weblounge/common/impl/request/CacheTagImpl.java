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

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.request.CacheTag;

/**
 * Tag used to identify entries in the caching service.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public final class CacheTagImpl implements Tag, CacheTag {

  /** Special object representing the "any" value */
  public final static Object ANY = new Object();

  /** Tag key */
  String key = null;

  /** Tag value */
  Object value = null;

  /**
   * Creates a cache tag with an empty value. This kind of tag can be used to
   * identify cache elements who must not have a tag with the given key defined.
   * 
   * @param key
   *          the tag key
   */
  public CacheTagImpl(String key) {
    this(key, ANY);
  }

  /**
   * Creates a cache tag with the given name and value.
   * <p>
   * Note that a tag value of <code>null</code> is replaced by the final static
   * field {@link #ANY}.
   * 
   * @param key
   *          the tag key
   * @param value
   *          the tag value
   */
  public CacheTagImpl(String key, Object value) {
    if (key == null)
      throw new IllegalArgumentException("Tag key must not be null!");
    if (value == null)
      value = ANY;
    this.key = key;
    this.value = value;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.CacheTag#getName()
   */
  public String getName() {
    return key;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.CacheTag#getValue()
   */
  public Object getValue() {
    return value;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CacheTagImpl) {
      CacheTagImpl tag = (CacheTagImpl) obj;
      return key.equals(tag.key) && value.equals(tag.value);
    }
    return super.equals(obj);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return key.hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return key + " = " + value;
  }

  /**
   * Sets this tag to match any value with the given name, which makes it a
   * wildcard with respect to the tag value.
   */
  public void setMatchAny() {
    this.value = ANY;
  }

  /**
   * Returns <code>true</code> if this tag matches any other tag with the same
   * name, regardless of its value.
   * 
   * @return <code>true</code> if this tag matches all values
   */
  public boolean matchesAny() {
    return value == ANY;
  }

}