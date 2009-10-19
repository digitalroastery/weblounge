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

package ch.o2it.weblounge.cache;

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * TODO: Comment CacheService
 */
public interface CacheService {

  /**
   * Starts a cacheable response. By calling this method, a new response wrapper
   * is generated which will write the response output to the cache as well as
   * to the client.
   * <p>
   * If the method returns <code>true</code>, then the response part was found
   * in the cache and has been directly written to the response from the cache.
   * If it returns <code>false</code>, the data was not found but will be put
   * into the cache when {@link #endResponse(WebloungeResponse)} is called.
   * 
   * @param uniqueTags
   *          the tags identifying this response
   * @param request
   *          the request
   * @param response
   *          the response
   * @param validTime
   *          the valid time in milliseconds
   * @param recheckTime
   *          the recheck time in milliseconds
   * @return the <code>CacheHandle</code> of the response or <code>null</code>
   *         if the response was found in the cache
   */
  CacheHandle startResponse(Iterable<Tag> uniqueTags, WebloungeRequest request,
      WebloungeResponse response, long validTime, long recheckTime);

  /**
   * Starts a cacheable response. By calling this method, a new response wrapper
   * is generated which will write the response output to the cache as well as
   * to the client.
   * <p>
   * If the method returns <code>true</code>, then the response part was found
   * in the cache and has been directly written to the response from the cache.
   * If it returns <code>false</code>, the data was not found but will be put
   * into the cache when {@link #endResponse(WebloungeResponse)} is called.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @return boolean <code>true</code> if the response was found in the cache
   */
  boolean startResponse(CacheHandle handle, WebloungeRequest request,
      WebloungeResponse response);

  /**
   * Tell the cache service that writing the response to the client is now
   * finished and that the cache buffer containing the response may be written to
   * the cache.
   * 
   * @param response
   *          the servlet response
   */
  boolean endResponse(WebloungeResponse response);

  /**
   * Tell the cache service that writing the response to the client failed for
   * some reason. In this case, the cache will discard the cached data for this
   * response and directly write the data out to the client.
   * <p>
   * Once this method has been called, all subsequent calls to
   * <code>endResponse</code>, <code>startResponsePart</code> and
   * <code>endtResponsePart</code> will have no effect.
   * 
   * @param response
   *          the servlet response
   */
  void invalidateResponse(WebloungeResponse response);

  /**
   * Starts caching a sub portion of the current response, identified by a set
   * of cache tags.
   * <p>
   * Dividing the cached response into parts has the advantage, that, if for
   * example on part of a page becomes invalid, the other parts remain in the
   * cache and only the invalidated part and the page in whole have to be
   * rebuilt.
   * <p>
   * If the method returns <code>true</code>, then the response part was found
   * in the cache and has been directly written to the response from the cache.
   * If it returns <code>false</code>, the data was not found but will be put
   * into the cache.
   * 
   * @param uniqueTags
   *          the tag set identifying the response part
   * @param response
   *          the servlet response
   * @param validTime
   *          the valid time in milliseconds
   * @param recheckTime
   *          the recheck time in milliseconds
   * @return the <code>CacheHandle</code> of the responose part or
   *         <code>null</code> if the response part was found in the cache
   */
  CacheHandle startResponsePart(Iterable<Tag> uniqueTags,
      HttpServletResponse response, long validTime, long recheckTime);

  /**
   * Starts caching a sub portion of the current response, identified by
   * <code>handle</code>. Dividing the cached response into parts has the
   * advantage, that, if for example on part of a page becomes invalid, the
   * other parts remain in the cache and only the invalidated part and the page
   * in whole have to be rebuilt.
   * <p>
   * If the method returns <code>true</code>, then the response part was found
   * in the cache and has been directly written to the response from the cache.
   * If it returns <code>false</code>, the data was not found but will be put
   * into the cache.
   * 
   * @param handle
   *          the response part identifier
   * @param response
   *          the servlet response
   * @return boolean <code>true</code> if the response part was found in the
   *         cache
   */
  boolean startResponsePart(CacheHandle handle, HttpServletResponse response);

  /**
   * Tells the cache manager that the data identified by <code>handle</code> is
   * complete and may be written to the cache.
   * 
   * @param handle
   *          the response part identifier. <br>
   *          NOTE: This MUST be the same instance that was used to start the
   *          corresponding response part!
   * @param response
   *          the servlet response
   */
  void endResponsePart(CacheHandle handle, HttpServletResponse response);

  /**
   * Tells the cache manager to throw away the data identified by
   * <code>handle</code>..
   * 
   * @param tags
   *          the cache data identifier
   */
  Set<CacheHandle> invalidate(Iterable<Tag> tags);

  /**
   * Tells the cache manager to throw away the data identified by
   * <code>handle</code>..
   * 
   * @param handle
   *          the cache data identifier
   */
  Set<CacheHandle> invalidateEntry(CacheHandle handle);

  /**
   * Sets the maximum cache size to the given number of bytes.
   * 
   * @param size
   *          The maximum cache size
   */
  void setSize(long size);

  /**
   * Resets the cache statistics.
   */
  void resetStatistics();

  /**
   * Clears the cache.
   */
  void empty();

}