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

package ch.o2it.weblounge.common.request;

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.content.Taggable;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * The <code>WebloungeResponse</code> defines a few extensions to the default
 * <code>HttpServletResponse</code>. In particular, they deal with support for
 * server side caching of the response stream.
 */
public interface WebloungeResponse extends HttpServletResponse, Taggable {

  /** Initial response state */
  int STATE_SYSTEM_INITIALIZING = 0;

  /** Response state when the request is about to be processed by the site */
  int STATE_SITE_INITIALIZING = 1;

  /** State while the request is being processed by the system */
  int STATE_SYSTEM_PROCESSING = 2;

  /** State while the request is being processed by the site */
  int STATE_SITE_PROCESSING = 3;

  /** Error while checking preconditions */
  int STATE_PRECONDITION_FAILED = -1;

  /** Error while processing the request */
  int STATE_PROCESSING_FAILED = -2;

  /**
   * Starts a cacheable response. By calling this method, the response output
   * will be written to the cache as well as to the client.
   * <p>
   * If the method returns <code>true</code>, then the response was found in the
   * cache and has been directly written to the response from the cache. In this
   * case, clients <b>must not</b> write data to the response, since it has been
   * submitted to the client already.
   * <p>
   * If the method returns <code>false</code>, the data was not found but will
   * be put into the cache when {@link #endResponse()} is called.
   * 
   * @param uniqueTags
   *          the tags identifying this response
   * @param validTime
   *          the valid time in milliseconds
   * @param recheckTime
   *          the recheck time in milliseconds
   * @return the <code>CacheHandle</code> of the response or <code>null</code>
   *         if the response was found in the cache
   * @throws IllegalStateException
   *           if the response has already been started
   */
  boolean startResponse(Iterable<Tag> uniqueTags, long validTime,
      long recheckTime) throws IllegalStateException;

  /**
   * Tell the cache service that writing the response to the client is now
   * finished and that the cache buffer containing the response may be written
   * to the cache.
   * 
   * @param response
   *          the servlet response
   * @throws IllegalStateException
   *           if the response has never been started
   */
  void endResponse() throws IllegalStateException;

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
   * In this case, clients <b>must not</b> write data to the response.
   * <p>
   * If it returns <code>false</code>, the data was not found but will be put
   * into the cache when {@link #endResponsePart()} is called.
   * 
   * @param uniqueTags
   *          the tag set identifying the response part
   * @param validTime
   *          the valid time in milliseconds
   * @param recheckTime
   *          the recheck time in milliseconds
   * @return the <code>CacheHandle</code> of the response part or
   *         <code>null</code> if the response part was found in the cache
   */
  boolean startResponsePart(Iterable<Tag> uniqueTags, long validTime,
      long recheckTime);

  /**
   * Tells the cache manager that the current response part is complete and may
   * be written to the cache.
   * 
   * @throws IllegalStateException
   *           if the response part was never started or if another response
   *           part needs to be finished first
   */
  void endResponsePart() throws IllegalStateException;

  /**
   * Tells the cache to not cache this response. This method should be called in
   * case of any rendering error.
   */
  void invalidate();

  /**
   * Returns <code>true</code> if the response that was generated is considered
   * valid and can therefore be cached.
   * 
   * @return <code>true</code> if the response is valid
   * @see #invalidate()
   */
  boolean isValid();

  /**
   * Method to be called when an error is detected while processing the request.
   * <p>
   * <b>Note:</b> Call <code>super.sendError(error, msg)<code> when
   * overwriting this method. Otherwise the system will not be able to
	 * handle the notification of request listeners.
   * 
   * @param error
   *          the HTTP error code
   * @param msg
   *          the error message
   * @see javax.servlet.http.HttpServletResponse#sendError(int,
   *      java.lang.String)
   */
  void sendError(int error, String msg) throws IOException;

  /**
   * Method to be called when an error is detected while processing the request.
   * <p>
   * <b>Note:</b> Call <code>super.sendError(error, msg)<code> when
   * overwriting this method. Otherwise the system will not be able to
	 * handle the notification of request listeners.
   * 
   * @param error
   *          the HTTP error code
   * @see javax.servlet.http.HttpServletResponse#sendError(int)
   */
  void sendError(int error) throws IOException;

}