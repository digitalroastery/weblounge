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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents a transaction in the response cache. A transaction keeps track of
 * the state of a response that is to be written to both the response cache and
 * the client.
 */
final class CacheTransaction {

  /** The main cache handle for this transaction */
  CacheHandle hnd = null;

  /** The response that accepts the result of this transaction */
  HttpServletResponse resp = null;

  /** The associated cache output stream */
  CacheOutputStream os = new CacheOutputStream();

  /** The cached response meta info */
  CachedHttpResponseHeaders headers = new CachedHttpResponseHeaders();

  /** The output filter */
  StreamFilter filter = null;

  /** True if the transaction has been invalidated */
  boolean invalidated = false;

  /** The cache identifier */
  String cache = null;

  /** Key into the cache */
  String cacheKey = null;

  /**
   * Creates a new transaction for the given handle, request and response. Any
   * output that is written to the response will be processed by the filter
   * prior to being sent to cache and client.
   * 
   * @param hnd
   *          the cache handle
   * @param req
   *          the servlet request
   * @param resp
   *          the servlet response
   * @param filter
   *          the filter
   */
  CacheTransaction(CacheHandle hnd, HttpServletRequest req,
      HttpServletResponse resp, StreamFilter filter) {
    this.hnd = hnd;
    this.resp = resp;
    this.filter = filter;
  }

}