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

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.request.CacheTagImpl;
import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTag;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Identifies a cached object. Implementing classes must provide meaningful
 * <code>hashCode()</code> and </code>equals()</code> methods.
 */
public class CacheHandleImpl implements CacheHandle {

  /** The serial version uid */
  private static final long serialVersionUID = 924532419150524805L;

  /** The key */
  protected String key = null;

  /** The expiration time of the cached object. */
  protected long expires;

  /** The recheck time of the cached object */
  protected long recheck;

  /** the set of cache tags */
  protected CacheTagSet tags = new CacheTagSet();

  /**
   * Creates a new CacheHandle that expires at the given time.
   * 
   * @param key
   *          the key into the cache
   * @param expires
   *          the relative expiration time of the cached object
   * @param recheck
   *          the time the cached element has to be checked for modifications.
   */
  public CacheHandleImpl(String key, long expires, long recheck) {
    this(expires, recheck);
    if (StringUtils.isBlank(key))
      throw new IllegalArgumentException("Key cannot be null");
    this.key = key;
  }

  /**
   * Creates a new CacheHandle that expires at the given time. Make sure to set
   * the key as soon as possible.
   * 
   * @param expires
   *          the relative expiration time of the cached object
   * @param recheck
   *          the time the cached element has to be checked for modifications.
   */
  protected CacheHandleImpl(long expires, long recheck) {
    setRecheck(recheck);
    setExpires(expires);
  }

  /**
   * Sets the key.
   * 
   * @param key
   *          the key
   */
  protected void setKey(String key) {
    this.key = key;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.CacheHandle#getKey()
   */
  public String getKey() {
    return key;
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
    this.expires = (expires > Times.MS_PER_SECOND) ? expires : Times.MS_PER_SECOND;
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
    this.recheck = (recheck > Times.MS_PER_SECOND) ? recheck : Times.MS_PER_SECOND;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.request.CacheHandle#getTagSet()
   */
  public Set<CacheTag> getTagSet() {
    return tags;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (key == null)
      throw new IllegalStateException("Key has not been set");
    return key.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (key == null)
      throw new IllegalStateException("Key has not been set");
    if (o instanceof CacheHandle) {
      return key.equals(((CacheHandle)o).getKey());
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTag(java.lang.String,
   *      ava.lang.String)
   */
  public boolean addTag(String key, String value) {
    return tags.add(new CacheTagImpl(key, value));
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTag(ch.o2it.weblounge.api.util.Tag)
   */
  public boolean addTag(CacheTag tag) {
    return tags.add(tag);
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTags(java.util.Collection)
   */
  public boolean addTags(Collection<CacheTag> t) {
    return tags.addAll(t);
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#removeTags(java.lang.String)
   */
  public boolean removeTags(String name) {
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
    for (CacheTag t : tags) {
      if (t.getName().equals(name) && t.getValue().equals(value)) {
        tag = t;
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
  public boolean containsTag(CacheTag tag) {
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
      throw new IllegalArgumentException("Name cannot be null");
    for (CacheTag t : tags) {
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
   * @see ch.o2it.weblounge.api.util.Taggable#removeTags(ch.o2it.weblounge.api.util.Tag)
   */
  public boolean removeTag(CacheTag tag) {
    return tags.remove(tag);
  }

  /**
   * @see ch.o2it.weblounge.common.content.Taggable#getTags()
   */
  public CacheTag[] getTags() {
    return tags.toArray(new CacheTag[tags.size()]);
  }

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#tags()
   */
  public Iterator<CacheTag> tags() {
    return Collections.unmodifiableSet(tags).iterator();
  }

}