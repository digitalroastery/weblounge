/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.cache.impl.handle;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.request.CacheTagImpl;
import ch.entwine.weblounge.common.impl.request.CacheTagSet;
import ch.entwine.weblounge.common.request.CacheHandle;
import ch.entwine.weblounge.common.request.CacheTag;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

  /** Date where the handle was created */
  private long creationDate = System.currentTimeMillis();

  /** Date where the content associated with this handle was last modified */
  private Date modificationDate = new Date(0);

  /** The expiration time of the cached object. */
  protected long expirationTime = Times.MS_PER_SECOND;

  /** The recheck time of the cached object */
  protected long revalidationTime = Times.MS_PER_SECOND;

  /** the set of cache tags */
  protected CacheTagSet tags = new CacheTagSet();

  /**
   * Creates a new CacheHandle that expires at the given time.
   * 
   * @param key
   *          the key into the cache
   * @param expirationTime
   *          the relative expiration time of the cached object
   * @param revalidationTime
   *          the time the cached element has to be checked for modifications.
   */
  public CacheHandleImpl(String key, long expirationTime, long revalidationTime) {
    this(expirationTime, revalidationTime);
    if (StringUtils.isBlank(key))
      throw new IllegalArgumentException("Key cannot be null");
    this.key = key;
  }

  /**
   * Creates a new CacheHandle that expires at the given time. Make sure to set
   * the key as soon as possible.
   * 
   * @param expirationTime
   *          the relative expiration time of the cached object
   * @param revalidationTime
   *          the time the cached element has to be checked for modifications.
   */
  protected CacheHandleImpl(long expirationTime, long revalidationTime) {
    setCacheExpirationTime(expirationTime);
    setClientRevalidationTime(revalidationTime);
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
   * @see ch.entwine.weblounge.common.request.CacheHandle#getKey()
   */
  public String getKey() {
    return key;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.CacheHandle#getCreationDate()
   */
  public long getCreationDate() {
    return creationDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.CacheHandle#setModificationDate(Date)
   */
  @Override
  public Date setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate.after(this.modificationDate) ? modificationDate : this.modificationDate;
    return this.modificationDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.CacheHandle#getModificationDate()
   */
  @Override
  public Date getModificationDate() {
    return modificationDate.getTime() > 0 ? modificationDate : new Date();
  }

  /**
   * Returns the time the cached object expires
   * 
   * @return the expiration time
   */
  public final long getCacheExpirationTime() {
    return expirationTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.CacheHandle#setCacheExpirationTime(long)
   */
  public final void setCacheExpirationTime(long expirationTime) {
    this.expirationTime = Math.max(expirationTime, Times.MS_PER_SECOND);
  }

  /**
   * Returns the recheck time of the cached objects.
   * 
   * @return the recheck time
   */
  public final long getClientRevalidationTime() {
    return revalidationTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.CacheHandle#setClientRevalidationTime(long)
   */
  public final void setClientRevalidationTime(long revalidationTime) {
    this.revalidationTime = Math.max(revalidationTime, Times.MS_PER_SECOND);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.CacheHandle#getTagSet()
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
      return key.equals(((CacheHandle) o).getKey());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#addTag(java.lang.String,
   *      java.lang.String)
   */
  public boolean addTag(String key, String value) {
    return tags.add(new CacheTagImpl(key, value));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#addTag(ch.entwine.weblounge.common.content.Tag)
   */
  public boolean addTag(CacheTag tag) {
    return tags.add(tag);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#addTags(java.util.Collection)
   */
  public boolean addTags(Collection<CacheTag> t) {
    return tags.addAll(t);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#removeTags(java.lang.String)
   */
  public boolean removeTags(String name) {
    return removeTag(name, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#removeTag(java.lang.String,
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#clearTags()
   */
  public void clearTags() {
    tags.clear();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#containsTag(ch.entwine.weblounge.common.content.Tag)
   */
  public boolean containsTag(CacheTag tag) {
    return tags.contains(tag);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#containsTag(java.lang.String)
   */
  public boolean containsTag(String name) {
    return containsTag(name, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#containsTag(java.lang.String,
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#isTagged()
   */
  public boolean isTagged() {
    return !tags.isEmpty();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#removeTag(ch.entwine.weblounge.common.content.Tag)
   */
  public boolean removeTag(CacheTag tag) {
    return tags.remove(tag);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#getTags()
   */
  public CacheTag[] getTags() {
    return tags.toArray(new CacheTag[tags.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Taggable#tags()
   */
  public Iterator<CacheTag> tags() {
    return Collections.unmodifiableSet(tags).iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getKey();
  }

}