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

import ch.o2it.weblounge.common.impl.request.RequestSupport;
import ch.o2it.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.o2it.weblounge.common.request.RequestHandler;
import ch.o2it.weblounge.common.request.RequestListener;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteNotFoundException;
import ch.o2it.weblounge.common.url.Url;
import ch.o2it.weblounge.dispatcher.DispatchListener;
import ch.o2it.weblounge.dispatcher.impl.handler.StaticContentHandler;

import org.apache.jasper.JasperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>Dispatcher</code> is used to dispatch the urls requested by the site
 * visitors. The handler tries to map the urls either to the community system
 * itself or to one of the modules.
 * 
 * @author Tobias Wunden
 * @version 2.0
 * @since WebLounge 1.0
 */

public final class WebloungeDispatcher {

  /** List of request listeners */
  private static List<RequestListener> requestListeners;

  /** List of dispatcher listeners */
  private static List<DispatchListener> dispatcher;

  /** List of dispatcher listeners */
  private static List<RequestHandler> requestHandler;

  /** Reference to dispatcher singleton */
  private static WebloungeDispatcher instance_;

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = WebloungeDispatcher.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  static {
    requestListeners = new ArrayList<RequestListener>();
    dispatcher = new ArrayList<DispatchListener>();
    requestHandler = new ArrayList<RequestHandler>();
  }

  /**
   * Constructor for class Dispatcher. This class is not intended to be
   * instantiated, therefore, the only constructor is private. To use this
   * class, call it's various static methods.
   */
  private WebloungeDispatcher() {
  }

  /**
   * Returns the singleton instance of the weblounge dispatcher.
   * 
   * @return the dispatcher instance
   */
  public static WebloungeDispatcher getInstance() {
    if (instance_ == null) {
      instance_ = new WebloungeDispatcher();
    }
    return instance_;
  }

  /**
   * Dispatches the request to the content management system. This is the
   * central dispatching method and is called directly by the main servlet upon
   * request receipt.<br>
   * Therefore, this is a good point to perform any kind of logging, global
   * request or response modification etc.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   */
  public void dispatch(WebloungeRequest request, WebloungeResponse response) {
    try {

      /**
       * Extract the site information. If no suitable site can be found and all
       * sites have been loaded, send an internal error message, since this is
       * due to malconfiguration.
       */
      Site site = null;
      try {
        // TODO: Get from service
        site = request.getSite();
        log_.debug("Request is being delegated to site " + site);
      } catch (SiteNotFoundException e) {
        String msg = "No site not found to handle requests for host '" + request.getServerName() + "'";
        log_.info(e.getMessage());
        DispatchUtil.sendInternalError(msg, request, response);
        return;
      }

      /**
       * See if we were able to extract a user. If not, send a login request.
       */
      if (request.getUser() == null) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"Weblounge Authentication\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      // Ask the registered request handler if they are willing to handle
      // the request. Otherwise, the site dispatcher will be called.

      // TODO: Synchronize
      for (RequestHandler handler : requestHandler) {
        try {
          if (handler.service(request, response)) {
            log_.debug("Request handler " + handler + " took request " + request);
            return;
          }
        } catch (Throwable t) {
          log_.error("Request handler " + handler + " produced error: " + t.getMessage(), t);
        }
      }

      // Get the site dispatcher and get rid of the request

      String url = request.getRequestURI();
      if (site.isEnabled()) {
        try {

          // Inform listeners about delivered request, but check first if we
          // encountered
          // an error while processing it.
          ((WebloungeResponseImpl) response).setRequest(request);
          if (response.preconditionFailed()) {
            log_.info("Precondition failed on request " + request);
            return;
          }

          fireRequestStarted(request, response, site);

          // Inform listeners about delivered request, but check first if we
          // encountered
          // an error while processing it.
          if (response.processingFailed()) {
            log_.info("Processing failed on request " + request);
            return;
          }

          response.setHeader("X-Powered-By", "Weblounge Content Mangement System");
          site.dispatch(request, response);
          if (response.processingFailed()) {
            log_.info("Processing failed on request " + request);
            return;
          }

          fireRequestDelivered(request, response, site);

        } catch (Exception e) {
          String params = RequestSupport.getParameters(request);
          String msg = "Error while renderering '" + url + "' " + params;
          Throwable o = e.getCause();
          if (o instanceof JasperException && ((JasperException) o).getRootCause() != null) {
            Throwable rootCause = ((JasperException) o).getRootCause();
            msg += ": " + rootCause.getMessage();
            log_.error(msg, rootCause);
          } else if (o != null) {
            msg += ": " + o.getMessage();
            log_.error(msg, o);
          } else {
            log_.error(msg, e);
          }
          DispatchUtil.sendInternalError(msg, request, response);
          return;
        }
      } else {
        String msg = request + " - Site dispatcher for " + site + " is temporarily not available. Please try again later.";
        log_.warn(msg);
        DispatchUtil.sendServiceUnavailable(msg, request, response);
        return;
      }
    } finally {
      log_.debug("Request " + request + " processed");
    }
  }

  /**
   * Adds <code>listener</code> to the list of request listeners if it has not
   * already been registered. The listener is called a few times during the
   * lifecycle of a request.
   * 
   * @param listener
   *          the lister
   */
  public static void addRequestListener(RequestListener listener) {
    if (!requestListeners.contains(listener)) {
      requestListeners.add(listener);
    }
  }

  /**
   * Removes the listener from the list of request listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  public static void removeRequestListener(RequestListener listener) {
    requestListeners.remove(listener);
  }

  /**
   * Adds <code>listener</code> to the list of dispatch listeners if it has not
   * already been registered. The listener is called everytime the current
   * request is internally being included or forwarded using the
   * <code>include</code> or <code>forward</code> method of this class.
   * 
   * @param listener
   *          the lister
   */
  public static void addDispatchListener(DispatchListener listener) {
    if (!requestListeners.contains(listener)) {
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

  /**
   * Adds <code>handler</code> to the list of request handler if it has not
   * already been registered. The handler is called everytime a request enters
   * the system.
   * 
   * @param handler
   *          the request handler
   */
  public static void addRequestHandler(RequestHandler handler) {
    if (!requestHandler.contains(handler)) {
      requestHandler.add(handler);
    }
  }

  /**
   * Removes the request handler from the list of handlers.
   * 
   * @param handler
   *          the request handler to remove
   */
  public static void removeRequestHandler(RequestHandler handler) {
    requestHandler.remove(handler);
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
  public static void forward(WebloungeRequest request, WebloungeResponse response, String url) throws ServletException, IOException {

    // Remove the url attribute to prevent loops. By removing, The Request
    // object
    // is forced to evaluate the url again.
    request.removeAttribute(Url.ID);

    // Ask registered listeners whether they want to redirect the url.
    for (int i = 0; i < dispatcher.size(); i++) {
      DispatchListener d = dispatcher.get(i);
      url = d.redirect(url, request, response);
    }

    // Ask registerd listeners whether they are willing to do the work for us
    for (int i = 0; i < dispatcher.size(); i++) {
      DispatchListener d = dispatcher.get(i);
      if (d.forward(request, response, url))
        return;
    }

    // If we get here, then no dispatcher seems to be responsible for handling
    // the request.
    // In this case, we use the default mechanism for request forwarding:
    StaticContentHandler.service(request, response, url);
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
  public static void include(WebloungeRequest request, WebloungeResponse response, String url) throws ServletException, IOException {

    // Remove the url attribute to prevent loops. By removing, The Request
    // object
    // is forced to evaluate the url again.
    request.removeAttribute(Url.ID);

    // Ask registered listeners whether they want to redirect the url.
    for (int i = 0; i < dispatcher.size(); i++) {
      DispatchListener d = dispatcher.get(i);
      url = d.redirect(url, request, response);
    }

    // Ask registerd listeners whether they are willing to do the work for us
    for (int i = 0; i < dispatcher.size(); i++) {
      DispatchListener d = dispatcher.get(i);
      if (d.include(request, response, url))
        return;
    }
    log_.debug("No dispatch listener took the request.");
    log_.debug("Handling " + url + " as static content");

    // If we get here, then no dispatcher seems to be responsible for handling
    // the request.
    // In this case, we use the default mechanism for request forwarding:
    StaticContentHandler.service(request, response, url);
  }

  /**
   * Evaluates the query part of the url. This part may include information to
   * switch the language or do other things, while the same page should be
   * redisplayed.
   * <p>
   * Overwrite this method if you have defined your own queries, but be sure to
   * call <code>super.evaluateQuery()</code> to maintain functionality
   * originally provided by the system.
   * 
   * @param request
   *          the <code>ServletRequest</code>
   */
  static void evaluateQuery(String query, Site site, HttpServletRequest request) {
    // Nothing to do here
  }

  /**
   * Method to fire a <code>startRequest()</code> message to all registered
   * <code>RequestListeners</code>.
   * 
   * @param request
   *          the starting request
   * @param response
   *          the servlet response
   * @param site
   *          the target site
   */
  static void fireRequestStarted(WebloungeRequest request, WebloungeResponse response, Site site) {
    WebloungeResponseImpl resp = ((WebloungeResponseImpl) response);
    resp.setState(WebloungeResponse.STATE_SYSTEM_PROCESSING);
    for (int i = 0; i < requestListeners.size(); i++) {
      RequestListener listener = requestListeners.get(i);
      listener.requestStarted(request, response);
    }
  }

  /**
   * Method to fire a <code>requestDelivered()</code> message to all registered
   * <code>RequestListeners</code>.
   * 
   * @param request
   *          the delivered request
   * @param response
   *          the servlet response
   * @param site
   *          the target site
   */
  static void fireRequestDelivered(WebloungeRequest request, WebloungeResponse response, Site site) {
    for (int i = 0; i < requestListeners.size(); i++) {
      RequestListener listener = requestListeners.get(i);
      listener.requestDelivered(request, response);
    }
  }

  /**
   * Method to fire a <code>requestFailed()</code> message to all registered
   * <code>RequestListeners</code>.
   * 
   * @param request
   *          the failing request
   * @param response
   *          the servlet response
   * @param site
   *          the target site
   */
  static void fireRequestFailed(WebloungeRequest request, WebloungeResponse response, Site site) {
    WebloungeResponseImpl resp = ((WebloungeResponseImpl) response);
    for (int i = 0; i < requestListeners.size(); i++) {
      RequestListener listener = requestListeners.get(i);
      listener.requestFailed(request, response, resp.getError());
    }
    // TODO: Move to where the event is fired
    if (site != null)
      site.requestFailed(request, response, resp.getError());
  }

}