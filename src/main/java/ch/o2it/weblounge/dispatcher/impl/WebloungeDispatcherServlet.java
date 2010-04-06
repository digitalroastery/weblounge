/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.dispatcher.impl;

import ch.o2it.weblounge.common.impl.request.RequestUtils;
import ch.o2it.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.o2it.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.o2it.weblounge.common.request.RequestListener;
import ch.o2it.weblounge.common.request.ResponseCache;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.dispatcher.DispatchListener;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.dispatcher.SiteRegistrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the main dispatcher for weblounge, every request starts and ends
 * here. Using the <code>HttpServiceTracker</code>, the servlet is registered
 * with an instance of the OSGi web service.
 * <p>
 * The servlet is also where you enable and disable response caching by calling
 * <code>setResponseCache()</code> with the appropriate implementation
 * reference.
 */
public final class WebloungeDispatcherServlet extends HttpServlet {

  /** Serial version uid */
  private static final long serialVersionUID = 8939686825567275614L;

  /** Logger */
  private static final Logger log_ = LoggerFactory.getLogger(WebloungeDispatcherServlet.class);

  /** Value of the X-Powered-By header */
  private static final String poweredBy = "Weblounge Content Mangement System";

  /** The response cache */
  private transient ResponseCache cache = null;

  /** The sites that are online */
  private transient SiteRegistrationService sites = null;

  /** List of request listeners */
  private List<RequestListener> requestListeners = null;

  /** List of dispatcher listeners */
  private List<DispatchListener> dispatcher = null;

  /** List of dispatcher listeners */
  private List<RequestHandler> requestHandler = null;

  /**
   * Creates a new instance of the weblounge dispatcher servlet.
   */
  WebloungeDispatcherServlet() {
    requestListeners = new ArrayList<RequestListener>();
    dispatcher = new ArrayList<DispatchListener>();
    requestHandler = new ArrayList<RequestHandler>();
  }
  
  /**
   * Sets the site locator.
   * 
   * @param siteLocator
   *          the site locator
   */
  public void setSiteLocator(SiteRegistrationService siteLocator) {
    this.sites = siteLocator;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    super.doGet(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doDelete(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    super.doDelete(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    super.doHead(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doOptions(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doOptions(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    super.doOptions(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    super.doPost(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    super.doPut(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#doTrace(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doTrace(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    super.doTrace(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#getLastModified(javax.servlet.http.HttpServletRequest)
   */
  @Override
  protected long getLastModified(HttpServletRequest req) {
    return super.getLastModified(req);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void service(HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) throws ServletException, IOException {

    log_.debug("Serving {}", httpRequest.getRequestURI());

    // Wrap request and response
    WebloungeRequestImpl request = new WebloungeRequestImpl(httpRequest);
    WebloungeResponseImpl response = new WebloungeResponseImpl(httpResponse);

    // Get the site dispatcher
    Site site = getSiteByRequest(request);

    // See if a site dispatcher was found, and if so, if it's enabled
    if (site == null) {
      log_.warn("No dispatcher found for {}", request);
      DispatchUtils.sendNotFound("Not found", request, response);
      return;
    } else if (!site.isRunning()) {
      log_.warn("Dispatcher for site {} is temporarily not available", site);
      DispatchUtils.sendServiceUnavailable("Site is temporarily unavailable", request, response);
      return;
    }

    // Configure request and response objects
    request.init(site);
    response.setRequest(request);
    response.setResponseCache(cache);
    response.setHeader("X-Powered-By", poweredBy);

    // Notify listeners about starting request
    fireRequestStarted(request, response, site);

    // Ask the registered request handler if they are willing to handle
    // the request.
    // TODO: Synchronize
    for (RequestHandler handler : requestHandler) {
      try {
        log_.trace("Asking {} to serve {}", handler, request);
        if (handler.service(request, response)) {
          log_.debug("{} served request {}", handler, request);
          
          // Notify listeners about finished request
          if (response.hasError()) {
            log_.info("Request processing failed on {}", request);
            fireRequestFailed(request, response, site);
          } else {
            fireRequestDelivered(request, response, site);
          }
          return;
        }
      } catch (Throwable t) {
        response.invalidate();
        String params = RequestUtils.dumpParameters(request);
        if (t.getCause() != null) {
          t = t.getCause();
        }
        log_.error("Request handler {} failed to handle {} {}: {}", new Object[] {
            handler,
            request,
            params,
            t.getMessage(),
            t });
        DispatchUtils.sendInternalError(t.getMessage(), request, response);
      } finally {
        log_.debug("Finished processing of {}", httpRequest.getRequestURI());
      }
    }
    
    // Apparently, nobody felt responsible for this request
    log_.debug("No handler found to handle {}", request);
    DispatchUtils.sendNotFound(request, response);
    fireRequestFailed(request, response, site);
  }

  /**
   * Enables and disables caching by telling the dispatcher to use
   * <code>cache</code> for response caching. Pass <code>null</code> to disable
   * response caching.
   * 
   * @param cache
   *          the response cache implementation
   */
  public void setResponseCache(ResponseCache cache) {
    this.cache = cache;
  }

  /**
   * Returns the site that is being targeted by <code>request</code> or
   * <code>null</code> if either no site was found or the site is disabled right
   * now.
   * 
   * @param request
   *          the http request
   * @return the target site or <code>null</code>
   */
  private Site getSiteByRequest(HttpServletRequest request) {
    if (sites == null)
      return null;
    Site site = sites.findSiteByRequest(request);
    if (site != null && !site.isRunning()) {
      log_.debug("Ignoring request for disabled site {}", site);
      return null;
    }
    return site;
  }

  /**
   * Adds <code>listener</code> to the list of request listeners if it has not
   * already been registered. The listener is called a few times during the
   * lifecycle of a request.
   * 
   * @param listener
   *          the lister
   */
  public void addRequestListener(RequestListener listener) {
    // TODO: Synchronize
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
  public void removeRequestListener(RequestListener listener) {
    // TODO: Synchronize
    requestListeners.remove(listener);
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
  public void addDispatchListener(DispatchListener listener) {
    // TODO: Synchronize
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
  public void removeDispatchListener(DispatchListener listener) {
    // TODO: Synchronize
    dispatcher.remove(listener);
  }

  /**
   * Adds <code>handler</code> to the list of request handler if it has not
   * already been registered. The handler is called every time a request enters
   * the system.
   * 
   * @param handler
   *          the request handler
   */
  public void addRequestHandler(RequestHandler handler) {
    // TODO: Synchronize
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
    // TODO: Synchronize
    requestHandler.remove(handler);
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
  protected void fireRequestStarted(WebloungeRequest request,
      WebloungeResponse response, Site site) {
    // TODO: Synchronize
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
  protected void fireRequestDelivered(WebloungeRequest request,
      WebloungeResponse response, Site site) {
    // TODO: Synchronize
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
  protected void fireRequestFailed(WebloungeRequest request,
      WebloungeResponse response, Site site) {
    WebloungeResponseImpl resp = ((WebloungeResponseImpl) response);
    // TODO: Synchronize
    for (int i = 0; i < requestListeners.size(); i++) {
      RequestListener listener = requestListeners.get(i);
      listener.requestFailed(request, response, resp.getResponseStatus());
    }
    // TODO: Move to where the event is fired
    if (site != null)
      site.requestFailed(request, response, resp.getResponseStatus());
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Weblounge Dispatcher Servlet";
  }

}