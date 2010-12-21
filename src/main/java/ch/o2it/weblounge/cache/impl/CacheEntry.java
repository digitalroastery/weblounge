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

import java.io.Serializable;

/**
 * This class implements an entry into the cache.
 */
public final class CacheEntry implements Serializable {

  /** Serial version uid */
  private static final long serialVersionUID = 5694887351734158681L;

  /** The content buffer */
  private byte[] content;

  /** The response metadata */
  private CachedHttpResponseHeaders headers = null;

  /**
   * Creates a new cache handle with the given expiration and recheck time, the
   * content and the metadata.
   * 
   * @param content
   *          the content
   * @param headers
   *          the metadata
   */
  protected CacheEntry(byte[] content, CachedHttpResponseHeaders headers) {
    if (content == null)
      throw new IllegalArgumentException("Content cannot be null");
    if (headers == null)
      throw new IllegalArgumentException("Headers cannot be null");
    this.content = content;
    this.headers = headers;
  }

  /**
   * Returns the cached response headers.
   * 
   * @return the headers
   */
  public CachedHttpResponseHeaders getHeaders() {
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

}
