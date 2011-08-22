/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.cache.impl;

import ch.entwine.weblounge.common.request.CacheHandle;
import ch.entwine.weblounge.common.request.CacheTag;

import java.io.Serializable;

/**
 * This class represents a key in the weblounge site cache. It offers access to
 * the primary key as well as the full tag set.
 */
public class CacheEntryKey implements Serializable {

  /** Serial version uid */
  private static final long serialVersionUID = -2864438259828883116L;

  /** The primary key */
  protected String primaryKey = null;

  /** The full tag set */
  protected String tags = null;

  /**
   * Creates a new key element for the cache.
   * 
   * @param hdl
   *          the cache handle
   */
  public CacheEntryKey(CacheHandle hdl) {
    primaryKey = hdl.getKey();
    tags = createKey(hdl.getTags());
  }

  /**
   * Returns the primary key.
   * 
   * @return the primary key
   */
  String getKey() {
    return primaryKey;
  }

  /**
   * Returns the full tag set (in a serialized form).
   * 
   * @return the tags
   */
  String getTags() {
    return tags;
  }


  /**
   * Creates the key out of the set of tags. Note that the <code>site</code> tag
   * is skipped since this cache implementation uses a separate cache per site
   * anyway.
   * 
   * @param tags
   *          the tags
   * @return the key
   */
  protected String createKey(CacheTag[] tags) {
    if (tags == null || tags.length == 0)
      throw new IllegalArgumentException("Tags must not be null or empty");

    StringBuffer key = new StringBuffer();

    // Build the key
    for (CacheTag tag : tags) {
      if (CacheTag.Site.equals(tag.getName()))
        continue;
      if (key.length() > 0)
        key.append("; ");
      key.append(tag.getName()).append("=").append(tag.getValue());
    }

    return key.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return primaryKey.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CacheEntryKey))
      return false;
    return primaryKey.equals(o.toString());
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return primaryKey;
  }
  
}
