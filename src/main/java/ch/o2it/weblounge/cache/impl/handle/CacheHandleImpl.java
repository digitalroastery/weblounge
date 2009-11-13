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

package ch.o2it.weblounge.cache.impl.handle;

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.impl.request.CacheTagImpl;
import ch.o2it.weblounge.common.request.CacheHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Identifies a cached object. Implementing classes must provide meaningful
 * <code>hashCode()</code> and </code>equals()</code> methods.
 */
public abstract class CacheHandleImpl implements CacheHandle {

  /** The expiration time of the cached object. */
  private long expires;

  /** The recheck time of the cached object */
  private long recheck;

  /** the set of cache tags */
  private Set<Tag> tags = new HashSet<Tag>();

  /**
   * Creates a new CacheHandle that expires at the given time.
   * 
   * @param expires
   *          the relative expiration time of the cached object
   * @param recheck
   *          the time the cached element has to be checked for modifications.
   */
  public CacheHandleImpl(long expires, long recheck) {
    setRecheck(recheck);
    setExpires(expires);
  }

  /**
   * Returns the time the cached object expires
   * 
   * @return the expiration time
   */
  public final long getExpires() {
    return expires;
  }

  /**
   * Sets the time the cached object expires.
   * 
   * @param expires
   *          the expiration time to set
   */
  public final void setExpires(long expires) {
    this.expires = (expires > MS_PER_SECOND) ? expires : MS_PER_SECOND;
  }

  /**
   * Returns the recheck time of the cached objects.
   * 
   * @return the recheck time
   */
  public final long getRecheck() {
    return recheck;
  }

  /**
   * Sets the recheck time of the cached object.
   * 
   * @param recheck
   *          the new recheck time
   */
  public final void setRecheck(long recheck) {
    this.recheck = (recheck > MS_PER_SECOND) ? recheck : MS_PER_SECOND;

  }

  /**
   * Returns the short name of this cache handle.
   * 
   * @return the short name of this handle
   */
  public abstract String getShortName();

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public abstract int hashCode();

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public abstract boolean equals(Object o);

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTag(java.lang.String,
   *      java.lang.Object)
   */
  public boolean addTag(String key, Object value) {
    return tags.add(new CacheTagImpl(key, value));
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTag(ch.o2it.weblounge.api.util.Tag)
   */
  public boolean addTag(Tag tag) {
    return tags.add(tag);
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTags(java.util.Collection)
   */
  public boolean addTags(Collection<Tag> t) {
    return tags.addAll(t);
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#removeTag(java.lang.String)
   */
  public boolean removeTag(String name) {
    return removeTag(name, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#removeTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean removeTag(String name, String value) {
    if (name == null)
      throw new IllegalArgumentException("Cannot remove tag without a name");
    Object tag = null;
    List<Tag> tags = new ArrayList<Tag>();
    for (Tag t : tags) {
      if (t.getName().equals(name) && t.getValue().equals(value)) {
        tag = null;
        break;
      }
    }
    return (tag != null) ? tags.remove(tag) : false;
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#clearTags()
   */
  public void clearTags() {
    tags.clear();
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#containsTag(ch.o2it.weblounge.api.util.Tag)
   */
  public boolean containsTag(Tag tag) {
    return tags.contains(tag);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#containsTag(java.lang.String)
   */
  public boolean containsTag(String name) {
    return containsTag(name, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Taggable#containsTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean containsTag(String name, String value) {
    if (name == null)
      throw new IllegalArgumentException("Argument 'name' cannot be null");
    for (Tag t : tags) {
      if (t.getName().equals(name) && (value == null || t.getValue().equals(value)))
        return true;
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#isTagged()
   */
  public boolean isTagged() {
    return !tags.isEmpty();
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#removeTag(ch.o2it.weblounge.api.util.Tag)
   */
  public boolean removeTag(Tag tag) {
    return tags.remove(tag);
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#getTags()
   */
  public Tag[] getTags() {
    return (Tag[]) tags.toArray(new Tag[tags.size()]);
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#tags()
   */
  public Iterator<Tag> tags() {
    return Collections.unmodifiableSet(tags).iterator();
  }

}