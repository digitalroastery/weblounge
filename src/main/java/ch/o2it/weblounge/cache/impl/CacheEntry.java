/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTag;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * This class implements an entry into the cache.
 */
public final class CacheEntry implements Serializable {

  /** Serial version uid */
  private static final long serialVersionUID = 5694887351734158681L;

  /** The cache handle */
  private CacheHandle handle = null;

  /** The content buffer */
  private byte[] content;

  /** The response metadata */
  private CacheableHttpServletResponseHeaders headers = null;

  /** Date where the entry was added to the cache */
  private long entryCreationDate = 0L;

  /** The etag */
  private String eTag = null;

  /**
   * Creates a new cache entry for the given handle, content and metadata.
   * 
   * @param content
   *          the content
   * @param headers
   *          the metadata
   * @throws IllegalArgumentException
   *           if the content or the headers collection is <code>null</code>
   */
  protected CacheEntry(CacheHandle handle, byte[] content,
      CacheableHttpServletResponseHeaders headers) {
    if (handle == null)
      throw new IllegalArgumentException("Handle cannot be null");
    if (content == null)
      throw new IllegalArgumentException("Content cannot be null");
    if (headers == null)
      throw new IllegalArgumentException("Headers cannot be null");
    this.handle = handle;
    this.content = content;
    this.headers = headers;
    this.entryCreationDate = handle.getCreationDate();
    this.eTag = createETag(entryCreationDate);
  }

  /**
   * Returns the key for this entry.
   * 
   * @return the key
   */
  public String getKey() {
    return handle.getKey();
  }

  /**
   * Returns the tags for this entry.
   * 
   * @return the tags
   */
  public CacheTag[] getTags() {
    return handle.getTags();
  }

  /**
   * Returns this entry's etag value.
   * 
   * @return the etag
   */
  public String getETag() {
    if (headers.containsHeader("ETag"))
      return (String)headers.getHeaders().get("ETag");
    return eTag;
  }

  /**
   * Returns the date where this entry was created.
   * 
   * @return the creation date
   */
  public long getCreationDate() {
    return entryCreationDate;
  }

  /**
   * Returns <code>true</code> if the entry is tagged with <code>tag</code>.
   * 
   * @param tag
   *          the tag
   * @return <code>true</code> if the entry is tagged
   */
  public boolean containsTag(CacheTag tag) {
    if (handle.getTags() == null)
      return false;
    for (CacheTag t : handle.getTags()) {
      if (t.equals(tag))
        return true;
    }
    return false;
  }

  /**
   * Returns the entry's handle.
   * 
   * @return the handle
   */
  public CacheHandle getHandle() {
    return handle;
  }

  /**
   * Returns the cached response headers.
   * 
   * @return the headers
   */
  public CacheableHttpServletResponseHeaders getHeaders() {
    return headers;
  }

  /**
   * Returns the cached content.
   * 
   * @return the content
   */
  public byte[] getContent() {
    return content;
  }

  /**
   * Returns the content type.
   * 
   * @return the content type
   */
  public String getContentType() {
    Object contentType = headers.getHeaders().get("Content-Type");
    if (contentType == null)
      return null;
    if (contentType instanceof String)
      return (String) contentType;
    throw new IllegalStateException("Response contained more than one 'Content-type' header");
  }

  /**
   * Returns <code>true</code> if date is not older than the creation date of
   * this cache entry.
   * 
   * @param date
   *          the date
   * @return <code>true</code> if this entry is older or equally old
   */
  public boolean notModified(long date) {
    return date >= entryCreationDate;
  }

  /**
   * Returns <code>true</code> if <code>eTag</code> is either blank (not
   * specified) or if it matches this entry's etag.
   * 
   * @param eTag
   *          the etag
   * @return <code>true</code> if the etag is either empty or matches
   */
  public boolean matches(String eTag) {
    return StringUtils.isBlank(eTag) || this.eTag.equals(eTag);
  }

  /**
   * Returns the etag (including the extra pair of quotes) for the given value,
   * which will usually be the creation time of the cached entry.
   * 
   * @param value
   *          the value
   * @return the etag
   */
  public static String createETag(long value) {
    return "\"WL-" + Long.toHexString(value) + "\"";
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return handle.getKey();
  }

}
