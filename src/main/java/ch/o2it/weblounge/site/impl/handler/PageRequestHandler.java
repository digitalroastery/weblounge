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

import static ch.o2it.weblounge.common.request.RequestFlavor.html;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.impl.request.Http11Constants;
import ch.o2it.weblounge.common.impl.request.Http11Utils;
import ch.o2it.weblounge.common.impl.request.RequestSupport;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.dispatcher.RequestHandler;

import org.apache.jasper.JasperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletResponse;

/**
 * The <code>PageRequestHandler</code> is used to handle requests to urls simply
 * mapped to a certain template. The request handler will verify access rights
 * and then simply forward the request to the template handler.
 */
public class PageRequestHandler implements RequestHandler {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(PageRequestHandler.class);

  /** The singleton handler instance */
  private final static PageRequestHandler handler_ = new PageRequestHandler();

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
   *          the weblounge response
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {

    log_.debug("Page handler agrees to handle {}", request.getUrl());

    Mode processingMode = Mode.Default;
    WebUrl url = request.getUrl();
    String path = url.getPath();

    // Check the request flavor
    // TODO: Add support for XML and JSON
    if (!html.equals(request.getFlavor())) {
      log_.debug("Skipping request for {}, flavor {} is not supported", path, request.getFlavor());
      return false;
    }

    // Check the request method
    String requestMethod = request.getMethod();
    if (!Http11Utils.checkDefaultMethods(requestMethod, response))
      return true;

    // Check if the request is controlled by an action.
    Action action = (Action) request.getAttribute(WebloungeRequest.REQUEST_ACTION);

    // Check if browser must be told not to cache the page
    if (request.getVersion() == Page.WORK) {
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
    }

    // Check if the page is already part of the cache. If so, our task is
    // already done!
    if (request.getVersion() == Page.LIVE && action == null) {
      CacheTagSet cacheTags = new CacheTagSet();
      long validTime = Times.MS_PER_DAY;
      long recheckTime = Times.MS_PER_HOUR;

      // Create the set of tags that identify the page
      cacheTags.add(CacheTag.Url, url.getPath());
      cacheTags.add(CacheTag.Url, request.getRequestedUrl().getPath());
      cacheTags.add(CacheTag.Language, request.getLanguage().getIdentifier());
      cacheTags.add(CacheTag.User, request.getUser().getLogin());
      cacheTags.add(CacheTag.Site, url.getSite().getIdentifier());
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
      cacheTags.add(CacheTag.Parameters, Integer.toString(parameterCount));

      // Check if the page is already part of the cache
      if (response.startResponse(cacheTags, validTime, recheckTime))
        return true;
      
      processingMode = Mode.Cached;
    } else if (Http11Constants.METHOD_HEAD.equals(requestMethod)) {
      // handle HEAD requests
      Http11Utils.startHeadResponse(response);
      processingMode = Mode.Head;
    }

    // Get the renderer id that has been registered with the url. For this, we
    // first have to load the page data, then get the associated renderer
    // bundle.
    try {
      Page page = null;
      PageURI pageURI = new PageURIImpl(request);
      Site site = request.getSite();
      
      // Load the page
      try {
        page = site.getPage(pageURI);
      } catch (IOException e) {
        log_.error("Unable to load page {}: {}", new Object[] {pageURI, e.getMessage(), e});
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return true;
      }
      
      // Does it exist at all?
      if (page == null) {
        log_.debug("No page found for {}", pageURI);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return true;
      }

      // Is it published?
      if (!page.isPublished()) {
        log_.debug("Access to unpublished page {}", pageURI);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return true;
      }
      
      // Can the page be accessed by the current user?
      User user = request.getUser();
      try {
        // TODO: Check permission
        //PagePermission p = new PagePermission(page, user);
        //AccessController.checkPermission(p);
      } catch (SecurityException e) {
        log_.warn("Accessed to page {} denied for user {}", pageURI, user);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return true;
      }
      
      // Store the page in the request
      request.setAttribute(WebloungeRequest.REQUEST_PAGE, page);
      
      // See if the request contains instructions regarding the template
      // to be used
      String rendererId = (String) request.getAttribute(WebloungeRequest.REQUEST_TEMPLATE);
      Renderer renderer = null;
      if (rendererId != null) {
        renderer = site.getTemplate(rendererId);
      } else {
        renderer = site.getTemplate(page.getTemplate());
      }

      if (renderer == null) {
        String params = RequestSupport.getParameters(request);
        String msg = (rendererId != null) ? "Template '" + rendererId + "' was not found " : "No template was found ";
        msg += "to render url '" + url + "' " + params;
        site.getLogger().warn(msg);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return true;
      }

      // Select the actual renderer by method and have it render the
      // request. Since renderers are being pooled by the bundle, we
      // have to return it after the request has finished.

      String method = request.getFlavor();

      if (renderer.provides(method)) {
        try {

          // Add additional cache tags
          response.addTag("webl:template", rendererId);
          response.addTag("webl:pagetype", page.getType());
          for (String keyword : page.getSubjects()) {
            response.addTag("webl:keyword", keyword);
          }

          log_.info("Rendering {} through {}", path, renderer);
          renderer.configure(method, null);
          renderer.render(request, response);
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
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
          renderer.cleanup();
        }
      } else {
        String params = RequestSupport.getParameters(request);
        String msg = "Method '" + method + "' not supported by template '" + renderer + "' on '" + path + "' " + params;
        site.getLogger().warn(msg);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return true;
      }
      return true;
    } catch (IOException e) {
      log_.error("I/O exception while sending error status: {}", e.getMessage(), e);
      return true;
    } finally {
      switch (processingMode) {
        case Cached:
          response.endResponsePart();
          break;
        case Head:
          Http11Utils.endHeadResponse(response);
          break;
        default:
          break;
      }
    }
  }

  /**
   * Returns the singleton instance of this class.
   * 
   * @return the request handler instance
   */
  public static RequestHandler getInstance() {
    return handler_;
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getIdentifier()
   */
  public String getIdentifier() {
    return "page";
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "Page request handler";
  }

  /**
   * Returns a string representation of this request handler.
   * 
   * @return the handler name
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

}