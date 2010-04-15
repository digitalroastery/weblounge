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
import ch.o2it.weblounge.dispatcher.DispatchListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for error reporting using <code>HttpServletResponse</code>.
 */
public class DispatchUtils {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(DispatchUtils.class);

  /** List of dispatcher listeners */
  private final static List<DispatchListener> dispatcher = new ArrayList<DispatchListener>();

  /**
   * Asks the client to do <code>HTTP</code> authentication by sending back the
   * <code>WWW-Authenticate</code> header together with the given realm, that
   * will label the authentication dialog.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param realm
   *          name of the authentication realm
   */
  public static void requireAuthentication(WebloungeRequest request,
      WebloungeResponse response, String realm) {
    StringBuffer authHeader = new StringBuffer("Basic realm=\"");
    authHeader.append(realm);
    authHeader.append("\"");
    response.setHeader("WWW-Authenticate", authHeader.toString());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return;
  }

  /**
   * Forward servlet request and servlet response objects, using either a
   * registered dispatcher or the request dispatcher contained in the context.
   * 
   * @param request
   *          Servlet request
   * @param response
   *          Servlet response
   * @param url
   *          target url
   */
  public static void forward(WebloungeRequest request,
      WebloungeResponse response, String url) throws ServletException,
      IOException {

    // Remove the url attribute to prevent loops. By removing, The Request
    // object
    // is forced to evaluate the url again.
    request.removeAttribute(WebloungeRequest.SESSION_URL);

    // Ask registered listeners whether they want to redirect the url.
    // TODO: Are DispatchListeners still used?
    for (int i = 0; i < dispatcher.size(); i++) {
      DispatchListener d = dispatcher.get(i);
      url = d.redirect(url, request, response);
    }

    // Ask registered listeners whether they are willing to do the work for us
    for (int i = 0; i < dispatcher.size(); i++) {
      DispatchListener d = dispatcher.get(i);
      if (d.forward(request, response, url))
        return;
    }

    // If we get here, then no dispatcher seems to be responsible for handling
    // the request.
    log_.debug("No dispatch listener took the request.");
    sendError(HttpServletResponse.SC_NOT_FOUND, null, request, response);
  }

  /**
   * Include servlet request and servlet response objects, using the request
   * dispatcher contained in the context. Please be careful: use only context
   * relative path.
   * 
   * @param request
   *          Servlet request.
   * @param response
   *          Servlet response.
   * @param url
   *          the url to include
   */
  public static void include(WebloungeRequest request,
      WebloungeResponse response, String url) throws ServletException,
      IOException {

    // Remove the url attribute to prevent loops. By removing, The Request
    // object is forced to evaluate the url again.
    request.removeAttribute(WebloungeRequest.SESSION_URL);

    // Ask registered listeners whether they want to redirect the url.
    // TODO: Are DispatchListeners still used?
    for (int i = 0; i < dispatcher.size(); i++) {
      DispatchListener d = dispatcher.get(i);
      url = d.redirect(url, request, response);
    }

    // Ask registered listeners whether they are willing to do the work for us
    for (int i = 0; i < dispatcher.size(); i++) {
      DispatchListener d = dispatcher.get(i);
      if (d.include(request, response, url))
        return;
    }

    // If we get here, then no dispatcher seems to be responsible for handling
    // the request.
    log_.debug("No dispatch listener took the request.");
    sendError(HttpServletResponse.SC_NOT_FOUND, null, request, response);
  }

  /**
   * Sends a <code>500 - Internal server error</code> message back to the
   * client.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the response object
   */
  public static void sendInternalError(WebloungeRequest request,
      WebloungeResponse response) {
    sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, request, response);
  }

  /**
   * Sends a <code>500 - Internal server error</code> message back to the
   * client.
   * 
   * @param msg
   *          the error message
   * @param request
   *          the servlet request
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
   * @param request
   *          the servlet request
   * @param response
   *          the response object
   */
  public static void sendServiceUnavailable(WebloungeRequest request,
      WebloungeResponse response) {
    sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, null, request, response);
  }

  /**
   * Sends a <code>503 - Service Unavailable error</code> message back to the
   * client.
   * 
   * @param msg
   *          the error message
   * @param request
   *          the servlet request
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
   * @param request
   *          the servlet request
   * @param response
   *          the response object
   */
  public static void sendNotFound(WebloungeRequest request,
      WebloungeResponse response) {
    sendError(HttpServletResponse.SC_NOT_FOUND, null, request, response);
  }

  /**
   * Sends a <code>404 - Url not found</code> message back to the client.
   * 
   * @param msg
   *          the error message
   * @param request
   *          the servlet request
   * @param response
   *          the response object
   */
  public static void sendNotFound(String msg, WebloungeRequest request,
      WebloungeResponse response) {
    sendError(HttpServletResponse.SC_NOT_FOUND, msg, request, response);
  }

  /**
   * Sends a <code>403 - Forbidden</code> message back to the client.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the response object
   */
  public static void sendAccessDenied(WebloungeRequest request,
      WebloungeResponse response) {
    sendError(HttpServletResponse.SC_FORBIDDEN, null, request, response);
  }

  /**
   * Sends a <code>403 - Forbidden</code> message back to the client.
   * 
   * @param msg
   *          the error message
   * @param request
   *          the servlet request
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
   * @param request
   *          the servlet request
   * @param response
   *          the response object
   */
  public static void sendError(int status, WebloungeRequest request,
      WebloungeResponse response) {
    status = status > 0 ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    sendError(status, null, request, response);
  }

  /**
   * Sends an error message with code <code>status</code> and the according
   * error message as the http response.
   * 
   * @param status
   *          the http status code
   * @param msg
   *          the error message
   * @param request
   *          the servlet request
   * @param response
   *          the response object
   */
  public static void sendError(int status, String msg,
      WebloungeRequest request, WebloungeResponse response) {
    try {
      response.invalidate();
      response.sendError(status, msg);
    } catch (Exception e2) {
      log_.error("I/O Error when sending back error message {}: {}", status, e2.getMessage());
    }
  }

  /**
   * Adds <code>listener</code> to the list of dispatch listeners if it has not
   * already been registered. The listener is called every time the current
   * request is internally being included or forwarded using the
   * <code>include</code> or <code>forward</code> method of this class.
   * 
   * @param listener
   *          the lister
   */
  public static void addDispatchListener(DispatchListener listener) {
    if (!dispatcher.contains(listener)) {
      dispatcher.add(listener);
    }
  }

  /**
   * Removes the listener from the list of request listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  public static void removeDispatchListener(DispatchListener listener) {
    dispatcher.remove(listener);
  }

}