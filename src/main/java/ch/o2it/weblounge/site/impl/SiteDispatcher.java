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

import ch.o2it.weblounge.common.impl.request.RequestSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.UrlNotFoundException;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.site.impl.handler.PageRequestHandler;

import org.apache.jasper.JasperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.rmi.server.Dispatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * This dispatcher handles all request for a given site. In addition to this, it
 * is able to queue request whenever the site is not available for a short
 * period of time, e. g. during a site restart.
 */
public class SiteDispatcher implements Dispatcher {

  /** the id */
  public final static String ID = "dispatcher";

  /** Wait this much time in the queue before sending a 503 */
  private static final int QUEUING_TIME = 30000;

  /** The associated site */
  private Site site_;

  /** The request queue */
  private int queueSize_;

  /** Flag to determine whether to dispatch or to queue */
  private boolean dispatching_;

  /** <code>True</code> if the dispatcher is enabled */
  private boolean enabled_;

  /** List of request handler */
  private List requestHandler_;

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = SiteDispatcher.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

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
    site_ = site;
    queueSize_ = 0;
    enabled_ = enable;
    dispatching_ = false;
    requestHandler_ = new ArrayList();
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
    if (!requestHandler_.contains(handler)) {
      requestHandler_.add(handler);
    }
  }

  /**
   * Removes the request handler from the list of handlers.
   * 
   * @param handler
   *          the request handler to remove
   */
  public void removeRequestHandler(RequestHandler handler) {
    requestHandler_.remove(handler);
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
   */
  public void dispatch(WebloungeRequest request, WebloungeResponse response) {
    if (!enabled_) {
      String msg = request + " - Site dispatcher for site '" + request.getServerName() + "' is disabled.";
      DispatchUtil.sendServiceUnavailable(msg, request, response);
      return;
    }

    /**
     * Check whether we are currently dispatching or enqueueing.
     */
    if (!dispatching_) {
      synchronized (site_) {
        queueSize_++;
        log_.debug("Queue size for site '" + site_ + "' is " + queueSize_);
        try {
          while (!dispatching_) {
            log_.info("Request " + request + " locked in '" + site_ + "' site dispatcher queue");
            site_.wait(QUEUING_TIME);
          }
        } catch (InterruptedException e) {
          String msg = "Request " + request + " timed out while waiting for '" + site_ + "' site dispatcher";
          log_.info(msg);
          DispatchUtil.sendServiceUnavailable(msg, request, response);
          return;
        } finally {
          queueSize_--;
          log_.debug("Queue size for site '" + site_ + "' is " + queueSize_);
        }
        log_.info("Request " + request + " released from '" + site_ + "' site dispatcher queue");
      }
    }

    // Notify site of starting request
    site_.requestStarted(request, response);

    // Ask the registered request handler if they are willing to handle
    // the request. Otherwise, the site dispatcher will be called.

    Iterator i = requestHandler_.iterator();
    while (i.hasNext()) {
      RequestHandler handler = (RequestHandler) i.next();
      try {
        if (handler.service(request, response)) {
          site_.requestDelivered(request, response);
          return;
        }
      } catch (Exception e) {
        site_.requestFailed(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    /**
     * Extract the site information. Since the site has already been evaluated
     * by the main dispatcher, we do not expect to have problems.
     */
    Site site = request.getSite();

    /**
     * Extract the language information. If no language can be assigned, send an
     * internal error message, since this is due to misconfiguration.
     */
    Language language = SessionSupport.getLanguage(request);
    log_.debug("Session language is " + language);
    if (language == null) {
      String msg = request + " - Unable to assign default language in site '" + site + "'";
      log_.info(msg);
      DispatchUtil.sendInternalError(msg, request, response);
      return;
    }

    /**
     * Start by extracting the site that will receive the request. Since
     * WebLounge can handle multiple sites, it is important to first find the
     * correct site to have a reasonable context for request processing.
     */
    WebUrl url = null;
    try {
      url = request.getUrl();
      log_.debug(request + " - Request for " + url + " received.");
      log_.debug(request + " - Url mountpoint is " + url.getMountpoint());
    } catch (UrlNotFoundException e) {
      String msg = request + " - Url '" + request.getRequestURL() + "' not found.";
      log_.info(e.getMessage());
      log_.warn(request + " - Sending 404 - " + msg);
      DispatchUtil.sendUrlNotFound(msg, request, response);
      return;
    }

    try {
      PageRequestHandler.getHandler().service(request, response);
      site_.requestDelivered(request, response);
    } catch (Exception e) {
      String params = RequestSupport.getParameters(request);
      String msg = "Error while renderering page '" + url + "' " + params;
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
      site_.requestFailed(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      DispatchUtil.sendInternalError(msg, request, response);
      return;
    }
  }

  /**
   * Enables or disables the dispatcher, depending on the <code>enable</code>
   * parameter.
   * 
   * @param enable
   *          the dispatcher state
   */
  public void setEnabled(boolean enable) {
    enabled_ = enable;
  }

  /**
   * Returns <code>true</code> if the dispatcher is enabled.
   * 
   * @return <code>true</code> if the dispatcher is enabled
   */
  public boolean isEnabled() {
    return enabled_;
  }

  /**
   * Starts this dispatcher and prevents it from enqueueing requests. When this
   * method is called, the dispatcher will first empty its queue and then handle
   * the new requests.
   */
  public void startDispatching() {
    dispatching_ = true;
    if (queueSize_ > 0) {
      synchronized (site_) {
        site_.notifyAll();
      }
    }
  }

  /**
   * Tells the dispatcher to stop dispatching and to enqueue the requests until
   * either <code>startDispatching()</code> is called.
   */
  public void stopDispatching() {
    dispatching_ = false;
  }

  /**
   * Returns <code>true</code> if the dispatcher is currently dispatching
   * requests.
   * 
   * @return <code>true</code> if requests are dispatched
   */
  public boolean isDispatching() {
    return dispatching_;
  }

}