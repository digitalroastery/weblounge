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

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.site.impl.handler.PageRequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * This dispatcher handles all request for a given site. In addition to this, it
 * is able to queue request whenever the site is not available for a short
 * period of time, e. g. during a site restart.
 */
public final class SiteDispatcher {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(SiteDispatcher.class);

  /** Wait this much time in the queue before sending a 503 */
  private static final int QUEUING_TIME = 30000;

  /** The associated site */
  private Site site = null;

  /** The request queue */
  private int queueSize = 0;

  /** Flag to determine whether to dispatch or to queue */
  private boolean dispatching = false;

  /** List of request handler */
  private List<RequestHandler> requestHandler = new ArrayList<RequestHandler>();

  /**
   * Creates a new site dispatcher for the site with the given identifier. The
   * dispatcher will initially be disabled.
   * 
   * @param site
   *          the site
   * @param enable
   *          <code>true</code> to enable this dispatcher
   */
  public SiteDispatcher(Site site, boolean enable) {
    this.site = site;
  }

  /**
   * The dispatching method, which takes a <code>HttpServletRequest</code> and
   * dispatches it to the site (which will in turn pass it on to a jsp or any
   * other suitable renderer).
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @throws IOException
   *           if the response cannot be written to the client
   */
  public void dispatch(WebloungeRequest request, WebloungeResponse response)
      throws IOException {
    if (!dispatching) {
      synchronized (site) {
        queueSize++;
        log_.trace("Queue size for site {} is {}", site, queueSize);
        try {
          while (!dispatching) {
            log_.info("Request {} waiting for site dispatcher", request.getRequestURI());
            site.wait(QUEUING_TIME);
          }
          log_.info("Request {} released from '{}' site dispatcher queue", request, site);
        } catch (InterruptedException e) {
          log_.info("Request for {} timed out while waiting for {} site dispatcher", request.getRequestURI(), site);
          response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } finally {
          queueSize--;
          log_.debug("Queue size for site '{}' is {}", site, queueSize);
        }
      }
    }

    // Notify site of starting request
    site.requestStarted(request, response);

    // Ask the registered request handler if they are willing to handle
    // the request. Otherwise, the site dispatcher will be called.

    for (RequestHandler handler : requestHandler) {
      try {
        if (handler.service(request, response)) {
          site.requestDelivered(request, response);
          return;
        }
      } catch (Exception e) {
        site.requestFailed(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    try {
      PageRequestHandler.getInstance().service(request, response);
      site.requestDelivered(request, response);
    } catch (Exception e) {
      log_.error("Error dispatching reqest for {}: {}", new Object[] { request.getRequestURI(), e.getMessage(), e });
      site.requestFailed(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
  }

  /**
   * Starts this dispatcher and prevents it from enqueuing requests. When this
   * method is called, the dispatcher will first empty its queue and then handle
   * the new requests.
   */
  public void startDispatching() {
    dispatching = true;
    if (queueSize > 0) {
      synchronized (site) {
        site.notifyAll();
      }
    }
  }

  /**
   * Tells the dispatcher to stop dispatching and to enqueue the requests until
   * either <code>startDispatching()</code> is called.
   */
  public void stopDispatching() {
    dispatching = false;
  }

  /**
   * Returns <code>true</code> if the dispatcher is currently dispatching
   * requests.
   * 
   * @return <code>true</code> if requests are dispatched
   */
  public boolean isDispatching() {
    return dispatching;
  }

  /**
   * Adds <code>handler</code> to the list of request handler if it has not
   * already been registered. The handler is called everytime a request enters
   * the system.
   * 
   * @param handler
   *          the request handler
   */
  public void addRequestHandler(RequestHandler handler) {
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
  public void removeRequestHandler(RequestHandler handler) {
    requestHandler.remove(handler);
  }

}