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

import ch.o2it.weblounge.cache.StreamFilter;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTag;

/**
 * Represents a transaction in the response cache. A transaction keeps track of
 * the state of a response that is to be written to both the response cache and
 * the client.
 */
final class CacheTransaction {

  /** The main cache handle for this transaction */
  private CacheHandle hnd = null;

  /** The associated cache output stream */
  private CacheOutputStream os = new CacheOutputStream();

  /** The cached response meta info */
  private CachedHttpResponseHeaders headers = new CachedHttpResponseHeaders();

  /** The output filter */
  private StreamFilter filter = null;

  /** True if the transaction has been invalidated */
  private boolean valid = true;

  /** The cache identifier */
  private String cache = null;

  /**
   * Creates a new transaction for the given handle, request and response. Any
   * output that is written to the response will be processed by the filter
   * prior to being sent to cache and client.
   * 
   * @param hnd
   *          the cache handle
   * @param filter
   *          the filter
   */
  CacheTransaction(CacheHandle hnd, String cache, StreamFilter filter) {
    this.hnd = hnd;
    this.cache = cache;
    this.filter = filter;
  }

  /**
   * Returns the cache handle.
   * 
   * @return the handle
   */
  CacheHandle getHandle() {
    return hnd;
  }

  /**
   * Returns the cache identifier.
   * 
   * @return the cache
   */
  String getCache() {
    return cache;
  }

  /**
   * Returns the transaction's output stream.
   * 
   * @return the output stream
   */
  CacheOutputStream getOutputStream() {
    return os;
  }

  /**
   * Returns the content that was written to the output stream.
   * 
   * @return the content
   */
  byte[] getContent() {
    return os.getBuffer();
  }

  /**
   * Returns the headers that were written to the response.
   * 
   * @return the response headers
   */
  CachedHttpResponseHeaders getHeaders() {
    return headers;
  }

  /**
   * Returns the transaction's tags.
   * 
   * @return the tags
   */
  CacheTag[] getTags() {
    return hnd.getTags();
  }

  /**
   * Returns the installed stream filter.
   * 
   * @return the filter
   */
  StreamFilter getFilter() {
    return filter;
  }

  /**
   * Marks this transaction as invalidated, which means that it's content will
   * not be stored in the cache.
   */
  void invalidate() {
    valid = false;
  }

  /**
   * Returns <code>true</code> if this transaction is valid and it's content
   * should be added to the cache.
   * 
   * @return <code>true</code> if this transaction is valid
   */
  boolean isValid() {
    return valid;
  }

}