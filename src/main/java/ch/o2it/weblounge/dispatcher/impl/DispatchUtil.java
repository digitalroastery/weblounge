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

package ch.o2it.weblounge.dispatcher.impl;

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for error reporting using <code>HttpServletResponse</code>.
 */
public class DispatchUtil {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(DispatchUtil.class);

  /**
   * Sends a <code>500 - Internal server error</code> message back to the
   * client.
   * 
   * @param msg
   *          the error message
   * @param response
   *          the response object
   */
  public static void sendInternalError(String msg, WebloungeRequest request,
      WebloungeResponse response) {
    sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, request, response);
  }

  /**
   * Sends a <code>503 - Service Unavailable error</code> message back to the
   * client.
   * 
   * @param msg
   *          the error message
   * @param response
   *          the response object
   */
  public static void sendServiceUnavailable(String msg,
      WebloungeRequest request, WebloungeResponse response) {
    sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, msg, request, response);
  }

  /**
   * Sends a <code>404 - Url not found</code> message back to the client.
   * 
   * @param msg
   *          the error message
   * @param response
   *          the response object
   */
  public static void sendUrlNotFound(String msg, WebloungeRequest request,
      WebloungeResponse response) {
    sendError(HttpServletResponse.SC_NOT_FOUND, msg, request, response);
  }

  /**
   * Sends a <code>403 - Forbidden</code> message back to the client.
   * 
   * @param msg
   *          the error message
   * @param response
   *          the response object
   */
  public static void sendAccessDenied(String msg, WebloungeRequest request,
      WebloungeResponse response) {
    sendError(HttpServletResponse.SC_FORBIDDEN, msg, request, response);
  }

  /**
   * Sends an error message with code <code>status</code> and the according
   * error message as the http response.
   * 
   * @param status
   *          the http status code
   * @param msg
   *          the error message
   * @param response
   *          the response object
   */
  public static void sendError(int status, String msg,
      WebloungeRequest request, WebloungeResponse response) {
    try {
      response.invalidate();
      response.sendError(status, msg);
    } catch (Exception e2) {
      log_.error("I/O Error when sending back error message " + status + ": " + e2.getMessage());
    }
  }

}