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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import ch.o2it.weblounge.common.content.Taggable;

/**
 * Wrapper for a request that is being processed by the weblounge content
 * management system.
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
   * Tells the cache to not cache this response. This method should be called in
   * case of a rendering error.
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
   * Returns <code>true</code> if a request listener detects that a precondition
   * failed and triggered the <code>sendError</code> method.
   * 
   * @return <code>true</code> if a precondition failed
   */
  boolean preconditionFailed();

  /**
   * Returns <code>true</code> if a request listener detects that a error
   * occurred while processing the request.
   * 
   * @return <code>true</code> if an error has occurred
   */
  boolean processingFailed();

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