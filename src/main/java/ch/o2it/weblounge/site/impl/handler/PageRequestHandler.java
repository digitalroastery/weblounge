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

package ch.o2it.weblounge.site.impl.handler;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.http.Http11Constants;
import ch.o2it.weblounge.common.impl.request.Http11Utils;
import ch.o2it.weblounge.common.impl.request.RequestSupport;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTagSet;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.PageManager;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.dispatcher.RequestHandlerConfiguration;

import org.apache.jasper.JasperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * The <code>PageRequestHandler</code> is used to handle requests to urls simply
 * mapped to a certain template. The request handler will verify access rights
 * and then simply forward the request to the template handler.
 */
public class PageRequestHandler implements RequestHandler, Http11Constants {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(PageRequestHandler.class);

  /** The handler identifier */
  public static final String ID = "pagerequesthandler";

  /** The singleton handler instance */
  private static PageRequestHandler handler_ = new PageRequestHandler();

  /** Valid extensions for page urls */
  private static List<String> extensions = new ArrayList<String>();

  // Add the valid extensions
  static {
    extensions.add(".xml");
    extensions.add(".html");
    extensions.add(".htm");
  }

  /**
   * Handles the request for a simple url available somewhere in the system. The
   * handler sets the response type, does the url history and then forwards
   * request to the corresponding jsp page or xslt stylesheet.
   * <p>
   * This method returns <code>true</code> if the handler is decided to handle
   * the request, <code>false</code> otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the webloungeresponse
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {

    log_.debug("Page handler agrees to handle " + request.getUrl());

    WebUrl url = request.getUrl();
    String path = url.getPath();
    Site site = null;
    int pathLength = path.length();

    // Check if there is a path extension like ".xml". Since this is the default
    // handler,
    // requests to .gif might also appear, which will lead to a database
    // exception
    // because the corresponding page will not be found.
    // Because of this, we do some simple filtering here before bothering the
    // database.

    if (pathLength > 3 && path.lastIndexOf(".") == pathLength - 4) {
      String extension = path.substring(pathLength - 4);
      if (!extensions.contains(extension)) {
        log_.debug("Skipping request for " + path);
        return false;
      }
    }

    // check the request method
    String requestMethod = request.getMethod();
    if (!Http11Utils.checkDefaultMethods(requestMethod, response))
      return true;

    // Check if the request is controlled by an action.
    Action action = (Action) request.getAttribute(Action.ID);

    // Check if browser must be told not to cache the page
    Object controlCenter = request.getSession().getAttribute("Weblounge-Control-Center");
    if (request.getVersion() == Page.WORK || controlCenter != null) {
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
    }

    // Check if the page is already part of the cache. If so, our task is
    // already done!
    Cache cache = (Cache) ServiceManager.getEnabledSystemService(Cache.ID);
    CacheHandle cacheHdl = null;
    boolean cachePages = cache != null && !"false".equals(cache.getConfiguration().getOption("pages"));
    if (request.getVersion() == Page.LIVE && action == null) {
      if (cachePages && controlCenter == null) {

        CacheTagSet cacheTags = new CacheTagSet();
        long validTime = Times.MS_PER_DAY;
        long recheckTime = Times.MS_PER_HOUR;

        // Create tagset
        cacheTags.add("webl:url", url.getPath());
        cacheTags.add("webl:url", request.getRequestedUrl().getPath());
        cacheTags.add("webl:language", request.getLanguage().getIdentifier());
        cacheTags.add("webl:user", request.getUser().getLogin());
        cacheTags.add("webl:site", url.getSite().getIdentifier());
        Enumeration<?> pe = request.getParameterNames();
        int parameterCount = 0;
        while (pe.hasMoreElements()) {
          parameterCount++;
          String key = pe.nextElement().toString();
          String[] values = request.getParameterValues(key);
          for (String value : values) {
            cacheTags.add(key, value);
          }
        }
        cacheTags.add("webl:parameters", Integer.toString(parameterCount));

        // Check if the page is already part of the cache
        cacheHdl = cache.startResponse(cacheTags, request, response, validTime, recheckTime);
        if (cacheHdl == null) {
          request.getPreviousUrl().addEntry(url);
          return true;
        }

      } else if (METHOD_HEAD.equals(requestMethod)) {
        // handle HEAD requests
        Http11Utils.startHeadResponse(response);
      }
    }

    // Get the renderer id that has been registered with the url. For this, we
    // first
    // have to load the page data, then get the associated renderer bundle.

    try {
      Page page = null;
      site = request.getSite();
      User user = request.getUser();
      long version = request.getVersion();

      try {
        page = PageManager.getPage(path, site, user, SystemPermission.READ, version);
        request.setAttribute(Page.ID, page);
      } catch (SecurityException e) {
        String msg = "User '" + user + "' at " + request.getRemoteAddr() + " was denied acces to page " + url;
        site.getLogger().warn(msg);
        DispatchUtil.sendAccessDenied("Access denied!", request, response);
        return true;
      }
      if (page == null) {
        String msg = "No data found for url '" + url + "'";
        site.getLogger().debug("Url not found: '" + url + "'");
        DispatchUtil.sendUrlNotFound(msg, request, response);
        return true;
      }

      Permission p = SystemPermission.TRANSLATE;
      if (!page.isPublished() && !page.checkOne(p, user.getRoleClosure()) && !page.check(p, user)) {
        String msg = "No data found for url '" + url + "'";
        site.getLogger().debug("Access denied to unpublished url '" + url + "'");
        DispatchUtil.sendUrlNotFound(msg, request, response);
        return true;
      }

      // See if the request contains instructions concerning the template
      // to be used

      String rendererId = (String) request.getAttribute(Renderer.TEMPLATE);
      Renderer renderer = null;
      if (rendererId != null) {
        renderer = site.getRenderer(rendererId, request.getFlavor());
      } else {
        renderer = page.getRenderer(request.getFlavor());
      }
      if (renderer == null) {
        String params = RequestSupport.getParameters(request);
        String msg = (rendererId != null) ? "Template '" + rendererId + "' was not found " : "No template was found ";
        msg += "to render url '" + url + "' " + params;
        site.getLogger().warn(msg);
        DispatchUtil.sendInternalError(msg, request, response);
        return true;
      }

      // Select the actual renderer by method and have it render the
      // request. Since renderers are being pooled by the bundle, we
      // have to return it after the request has finished.

      String method = request.getFlavor();

      try {
        if (renderer.provides(method)) {
          try {

            // Add additional cache tags
            response.addTag("webl:template", rendererId);
            response.addTag("webl:pagetype", page.getType());
            for (String keyword : page.getKeywords()) {
              response.addTag("webl:keyword", keyword);
            }

            log_.info("Rendering '" + path + "' through '" + renderer + "'");
            renderer.configure(method, null);
            renderer.render(request, response);
            if (action == null) {
              request.getPreviousUrl().addEntry(url);
              if (cache != null) {
                request.getPreviousUrl().addEntry(url);
              } else if (METHOD_HEAD.equals(requestMethod)) {
                Http11Utils.endHeadResponse(response);
              }
            }
          } catch (Exception e) {
            String params = RequestSupport.getParameters(request);
            String msg = "Error rendering template '" + renderer + "' on '" + path + "' " + params;
            Throwable o = e.getCause();
            if (o instanceof JasperException && ((JasperException) o).getRootCause() != null) {
              Throwable rootCause = ((JasperException) o).getRootCause();
              msg += ": " + rootCause.getMessage();
              site.getLogger().error(msg, rootCause);
            } else if (o != null) {
              msg += ": " + o.getMessage();
              site.getLogger().error(msg, o);
            } else {
              site.getLogger().error(msg, e);
            }
            DispatchUtil.sendInternalError(msg, request, response);
          } finally {
            renderer.cleanup();
          }
        } else {
          String params = RequestSupport.getParameters(request);
          String msg = "Method '" + method + "' not supported by template '" + renderer + "' on '" + path + "' " + params;
          site.getLogger().warn(msg);
          DispatchUtil.sendInternalError(msg, request, response);
          return true;
        }
      } finally {
        site.getRenderers().returnRenderer(renderer);
      }
      return true;

    } finally {
      if (cachePages && action == null) {
        if (cacheHdl != null && !cache.endResponse(response)) {
          String params = RequestSupport.getParameters(request);
          site.getLogger().warn("Error caching response for page " + request.getUrl() + " " + params);
        }
      }
    }
  }

  /**
   * Returns an instance of class <code>SimpleRequestHandler</code>.
   * 
   * @return the request handler instance
   */
  public static RequestHandler getHandler() {
    return handler_;
  }

  /**
   * Returns a string representation of this request handler.
   * 
   * @return the handler name
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "default request handler";
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#configure(ch.o2it.weblounge.dispatcher.api.request.RequestHandlerConfiguration)
   */
  public void configure(RequestHandlerConfiguration config)
      throws ConfigurationException {
    /* this handler ist configured explicitly */
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getIdentifier()
   */
  public String getIdentifier() {
    return "default";
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "default request handler";
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getDescription()
   */
  public String getDescription() {
    return "this handler handles all requets that are not handled by any other handler";
  }

}