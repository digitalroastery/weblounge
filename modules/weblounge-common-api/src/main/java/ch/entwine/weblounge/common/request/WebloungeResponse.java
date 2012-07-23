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

package ch.entwine.weblounge.common.request;

import ch.entwine.weblounge.common.content.Taggable;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * The <code>WebloungeResponse</code> defines a few extensions to the default
 * <code>HttpServletResponse</code>. In particular, they deal with support for
 * server side caching of the response stream.
 */
public interface WebloungeResponse extends HttpServletResponse, Taggable<CacheTag> {

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
   * @return <code>true</code> if the item was found in the cache, false
   *         otherwise
   * @throws IllegalStateException
   *           if the response has already been started
   */
  boolean startResponse(CacheTag[] uniqueTags, long validTime, long recheckTime)
      throws IllegalStateException;

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
   * Sets the recheck time on the current response or response part.
   * <p>
   * The implementation will consider the minimum of any existing recheck time
   * and <code>recheckTime</code> to be the new maximum recheck time.
   * 
   * @param recheckTime
   *          the recheck time in milliseconds
   */
  void setClientRevalidationTime(long recheckTime);

  /**
   * Returns the maximum recheck time.
   * 
   * @return the maximum recheck time
   */
  long getClientRevalidationTime();

  /**
   * Sets the maximum valid time on the current response or response part.
   * <p>
   * The implementation will consider the minimum of any existing valid time and
   * <code>validTime</code> to be the new maximum valid time.
   * 
   * @param validTime
   *          the valid time in milliseconds
   */
  void setCacheExpirationTime(long validTime);

  /**
   * Returns <code>true</code> if this response is being cached. Note that this
   * method returns <code>true</code> even after a call to {@link #invalidate()}
   * .
   * 
   * @return <code>true</code> if
   */
  boolean isCached();

  /**
   * Returns the maximum valid time.
   * 
   * @return the maximum valid time
   */
  long getCacheExpirationTime();

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
   * Returns the response status.
   * 
   * @return the status
   */
  int getStatus();

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