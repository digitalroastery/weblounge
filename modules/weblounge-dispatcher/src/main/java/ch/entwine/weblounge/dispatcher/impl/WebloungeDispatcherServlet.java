/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.dispatcher.impl;

import ch.entwine.weblounge.cache.CacheService;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.entwine.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.entwine.weblounge.common.request.RequestListener;
import ch.entwine.weblounge.common.request.ResponseCache;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.dispatcher.DispatchListener;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.SiteDispatcherService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.Servlet;
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
  private static final Logger logger = LoggerFactory.getLogger(WebloungeDispatcherServlet.class);

  /** Value of the X-Powered-By header */
  private static final String POWERED_BY = "Weblounge Content Management System";

  /** Response default encoding */
  private static final String DEFAULT_RESPONSE_ENCODING = "UTF-8";

  /** The environment */
  private Environment environment = Environment.Production;

  /** The sites that are online */
  private transient SiteDispatcherService sites = null;

  /** The security service */
  private SecurityService securityService = null;

  /** List of request listeners */
  private List<RequestListener> requestListeners = null;

  /** List of dispatcher listeners */
  private List<DispatchListener> dispatcher = null;

  /** List of dispatcher listeners */
  private Set<RequestHandler> requestHandler = null;

  /** List with well known urls and files */
  private static List<String> wellknownFiles = new ArrayList<String>();

  /** List of sites that have already issued a warning once */
  private List<Site> missingRepositoryWarnings = new ArrayList<Site>();

  /** The response caches */
  private Map<String, ResponseCache> caches = null;

  static {
    wellknownFiles.add("/favicon.ico");
    wellknownFiles.add("/robots.txt");
  }

  /**
   * Creates a new instance of the weblounge dispatcher servlet.
   * 
   * @param environment
   *          the environment
   */
  WebloungeDispatcherServlet(Environment environment) {
    this.environment = environment;
    requestListeners = new CopyOnWriteArrayList<RequestListener>();
    dispatcher = new CopyOnWriteArrayList<DispatchListener>();
    requestHandler = new TreeSet<RequestHandler>(new Comparator<RequestHandler>() {
      public int compare(RequestHandler handler1, RequestHandler handler2) {
        int compare = Integer.valueOf(handler2.getPriority()).compareTo(Integer.valueOf(handler1.getPriority()));
        // FIXME if 0 is returned the request handler will not be added?!
        if (compare == 0)
          return 1;
        return compare;
      }
    });
    caches = new HashMap<String, ResponseCache>();
  }

  /**
   * Sets the site locator.
   * 
   * @param siteDispatcher
   *          the site locator
   */
  void setSiteDispatcher(SiteDispatcherService siteDispatcher) {
    this.sites = siteDispatcher;
  }

  /**
   * Removes the site locator.
   * 
   * @param siteDispatcher
   *          the site locator
   */
  void removeSiteDispatcher(SiteDispatcherService siteDispatcher) {
    this.sites = null;
  }

  /**
   * Sets the security service.
   * 
   * @param securityService
   *          the security service
   */
  void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
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

    if (sites == null) {
      httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return;
    }

    logger.debug("Serving {}", httpRequest.getRequestURI());

    // Get the site dispatcher
    Site site = securityService.getSite();
    if (site == null) {
      site = getSiteByRequest(httpRequest);
      securityService.setSite(site);
    }

    boolean isSpecial = StringUtils.isNotBlank(httpRequest.getHeader("X-Weblounge-Special"));
    if (site == null) {
      if (!wellknownFiles.contains(httpRequest.getRequestURI()))
        logger.warn("No site found to handle {}", httpRequest.getRequestURL());
      httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    } else if (!site.isOnline() && !isSpecial) {
      if (site.getContentRepository() == null) {
        if (!missingRepositoryWarnings.contains(site)) {
          logger.warn("No content repository connected to site '{}'", site);
          missingRepositoryWarnings.add(site);
        } else {
          logger.debug("No content repository connected to site '{}'", site);
        }
      } else {
        logger.debug("Ignoring request for disabled site '{}'", site);
      }
      httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    } else if (missingRepositoryWarnings.size() > 0) {
      missingRepositoryWarnings.remove(site);
    }

    // Make sure the response is buffered
    httpResponse = new BufferedHttpServletResponse(httpResponse);

    // Get the servlet that is responsible for the site's content
    Servlet siteServlet = sites.getSiteServlet(site);

    // Get the response cache, if available
    ResponseCache cache = caches.get(site.getIdentifier());

    // Wrap for caching
    if (cache != null) {
      httpResponse = cache.createCacheableResponse(httpRequest, httpResponse);
    }

    // Wrap request and response
    WebloungeRequestImpl request = new WebloungeRequestImpl(httpRequest, siteServlet, environment);
    WebloungeResponseImpl response = new WebloungeResponseImpl(httpResponse);

    // See if a site dispatcher was found, and if so, if it's enabled
    if (!site.isOnline() && !isSpecial) {
      logger.warn("Dispatcher for site {} is temporarily not available", site);
      DispatchUtils.sendServiceUnavailable("Site is temporarily unavailable", request, response);
      return;
    }

    // Configure request and response objects
    request.init(site);
    response.setRequest(request);
    response.setResponseCache(cache);
    response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
    response.setHeader("X-Powered-By", POWERED_BY);
    response.setHeader("Cache-Control", "private");
    response.setDateHeader("Date", Calendar.getInstance().getTimeInMillis());

    // Notify listeners about starting request
    fireRequestStarted(request, response, site);

    boolean requestServed = false;

    // Ask the registered request handler if they are willing to handle
    // the request.
    try {
      securityService.setSite(site);
      request.setUser(securityService.getUser());
      for (RequestHandler handler : requestHandler) {
        try {
          logger.trace("Asking {} to serve {}", handler, request);
          if (handler.service(request, response)) {
            requestServed = true;
            logger.debug("{} served request {}", handler, request);
            if (response.hasError()) {
              logger.debug("Request processing failed on {}", request);
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
          logger.error("Request handler '{}' failed to handle {} {}", new Object[] {
              handler,
              request,
              params });
          logger.error(t.getMessage(), t);
          DispatchUtils.sendInternalError(t.getMessage(), request, response);
          break;
        }
      }
    } finally {
      securityService.setSite(null);
      if (requestServed) {
        response.endResponse();
        logger.debug("Finished processing of {}", httpRequest.getRequestURI());
      } else {
        logger.debug("No handler found for {}", request);
        DispatchUtils.sendNotFound(request, response);
        if (cache != null)
          cache.invalidate(response);
        fireRequestFailed(request, response, site);
      }
    }
  }

  /**
   * Returns the site that is being targeted by <code>request</code>.
   * 
   * @param request
   *          the http request
   * @return the target site or <code>null</code>
   */
  private Site getSiteByRequest(HttpServletRequest request) {
    if (sites == null)
      return null;
    Site site = sites.findSiteByRequest(request);
    return site;
  }

  /**
   * Adds <code>listener</code> to the list of request listeners if it has not
   * already been registered. The listener is called a few times during the life
   * cycle of a request.
   * 
   * @param listener
   *          the lister
   */
  void addRequestListener(RequestListener listener) {
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
  void removeRequestListener(RequestListener listener) {
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
  void addDispatchListener(DispatchListener listener) {
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
  void removeDispatchListener(DispatchListener listener) {
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
  void addRequestHandler(RequestHandler handler) {
    requestHandler.add(handler);
    requestHandler.size();
  }

  /**
   * Removes the request handler from the list of handlers.
   * 
   * @param handler
   *          the request handler to remove
   */
  void removeRequestHandler(RequestHandler handler) {
    requestHandler.remove(handler);
  }

  /**
   * Registers the response cache with the main dispatcher servlet.
   * 
   * @param cache
   *          the response cache
   */
  void addResponseCache(CacheService cache) {
    caches.put(cache.getIdentifier(), cache);
    logger.info("Response caching activated for site '{}'", cache.getIdentifier());
  }

  /**
   * Removes the response cache from the main dispatcher servlet.
   * 
   * @param cache
   *          the response cache
   */
  void removeResponseCache(CacheService cache) {
    caches.remove(cache.getIdentifier());
    logger.info("Response caching deactivated for site '{}'", cache.getIdentifier());
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
    for (int i = 0; i < requestListeners.size(); i++) {
      RequestListener listener = requestListeners.get(i);
      listener.requestFailed(request, response, resp.getStatus());
    }
    if (site != null)
      site.requestFailed(request, response, resp.getStatus());
  }

  /**
   * Sets the environment that is used to determine the correct context for
   * requests.
   * 
   * @param environment
   *          the environment
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
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