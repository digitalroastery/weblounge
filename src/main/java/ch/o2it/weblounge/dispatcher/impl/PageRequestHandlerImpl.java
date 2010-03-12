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

import static ch.o2it.weblounge.common.request.RequestFlavor.HTML;

import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.impl.request.Http11Constants;
import ch.o2it.weblounge.common.impl.request.Http11Utils;
import ch.o2it.weblounge.common.impl.request.RequestUtils;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.PageTemplate;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.dispatcher.PageRequestHandler;
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
public final class PageRequestHandlerImpl implements PageRequestHandler {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(PageRequestHandlerImpl.class);

  /** The singleton handler instance */
  private final static PageRequestHandlerImpl handler = new PageRequestHandlerImpl();

  /**
   * Handles the request for a simple url available somewhere in the system. The
   * handler sets the response type, does the url history and then forwards
   * request to the corresponding JSP page or XSLT stylesheet.
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
    // TODO: Criteria would be loading the page from the repository
    // TODO: Think about performance, page lookup is expensive
    // TODO: Add support for XML and JSON
    if (!HTML.equals(request.getFlavor())) {
      log_.debug("Skipping request for {}, flavor {} is not supported", path, request.getFlavor());
      return false;
    }

    // Check the request method
    String requestMethod = request.getMethod();
    if (!Http11Utils.checkDefaultMethods(requestMethod, response)) {
      log_.debug("Page handler answered head request for {}", request.getUrl());
      return true;
    }

    // Check if the request is controlled by an action.
    Action action = (Action) request.getAttribute(WebloungeRequest.REQUEST_ACTION);

    // Check if the page is already part of the cache. If so, our task is
    // already done!
    if (request.getVersion() == Page.LIVE && action == null) {
      long validTime = Renderer.DEFAULT_VALID_TIME;
      long recheckTime = Renderer.DEFAULT_RECHECK_TIME;

      // Create the set of tags that identify the page
      CacheTagSet cacheTags = createCacheTags(request);

      // Check if the page is already part of the cache
      if (response.startResponse(cacheTags, validTime, recheckTime)) {
        log_.debug("Page handler answered request for {} from cache", request.getUrl());
        return true;
      }

      processingMode = Mode.Cached;
    } else if (Http11Constants.METHOD_HEAD.equals(requestMethod)) {
      // handle HEAD requests
      Http11Utils.startHeadResponse(response);
      processingMode = Mode.Head;
    } else if (request.getVersion() == Page.WORK) {
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
    }

    // Get the renderer id that has been registered with the url. For this, we
    // first have to load the page data, then get the associated renderer
    // bundle.
    try {
      Page page = null;
      PageURI pageURI = new PageURIImpl(request);
      Site site = request.getSite();
      RequestFlavor contentFlavor = request.getFlavor();

      // Load the page
      try {
        page = site.getPage(pageURI);
      } catch (IOException e) {
        log_.error("Unable to load page {}: {}", new Object[] {
            pageURI,
            e.getMessage(),
            e });
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
        // PagePermission p = new PagePermission(page, user);
        // AccessController.checkPermission(p);
      } catch (SecurityException e) {
        log_.warn("Accessed to page {} denied for user {}", pageURI, user);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return true;
      }

      // Store the page in the request
      request.setAttribute(WebloungeRequest.REQUEST_PAGE, page);

      // Get hold of the page template
      PageTemplate template = null;
      try {
        template = getPageTemplate(page, request);
      } catch (IllegalStateException e) {
        log_.warn(e.getMessage());
        try {
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IOException e1) { /* never mind */ }
        return true;
      }

      // Does the template support the requested flavor?
      if (!template.supportsFlavor(contentFlavor)) {
        log_.warn("Template {} does not support requested flavor {}", template, contentFlavor);
        try {
          response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } catch (IOException e) { /* never mind */ }
        return true;
      }

      // Select the actual renderer by method and have it render the
      // request. Since renderers are being pooled by the bundle, we
      // have to return it after the request has finished.
      try {

        // Add additional cache tags
        response.addTag("webl:template", template.getIdentifier());
        response.addTag("webl:pagetype", page.getType());
        for (String keyword : page.getSubjects()) {
          response.addTag("webl:keyword", keyword);
        }

        // Configure valid and recheck time according to the template
        response.setRecheckTime(template.getRecheckTime());
        response.setValidTime(template.getValidTime());

        log_.info("Rendering {} through {}", path, template);
        template.render(request, response);
      } catch (Exception e) {
        String params = RequestUtils.getParameters(request);
        String msg = "Error rendering template '" + template + "' on '" + path + "' " + params;
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
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      return true;
    } catch (IOException e) {
      log_.error("I/O exception while sending error status: {}", e.getMessage(), e);
      return true;
    } finally {
      if (action == null) {
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
  }

  /**
   * Returns the template that will be used to handle this request. If the
   * template cannot be found or used for some reason, an
   * {@link IllegalStateException} is thrown.
   * 
   * @param page
   *          the page
   * @param request
   *          the request
   * @return the template
   * @throws IllegalStateException
   *           if the template cannot be found
   */
  protected PageTemplate getPageTemplate(Page page, WebloungeRequest request) throws IllegalStateException {
    Site site = request.getSite();
    String templateId = (String) request.getAttribute(WebloungeRequest.REQUEST_TEMPLATE);
    PageTemplate template = null;
    if (templateId != null) {
      template = site.getTemplate(templateId);
      if (template == null) {
        throw new IllegalStateException("Page template " + templateId + " specified by request was not found");
      }
    } else {
      template = site.getTemplate(page.getTemplate());
      if (template == null) {
        throw new IllegalStateException("Page template " + templateId + " specified by page " + page + " was not found");
      }
    }
    return template;
  }

  /**
   * Returns the primary set of cache tags for the given request.
   * 
   * @param request
   *          the request
   * @return the cache tags
   */
  protected CacheTagSet createCacheTags(WebloungeRequest request) {
    CacheTagSet cacheTags = new CacheTagSet();
    cacheTags.add(CacheTag.Url, request.getUrl().getPath());
    cacheTags.add(CacheTag.Url, request.getRequestedUrl().getPath());
    cacheTags.add(CacheTag.Language, request.getLanguage().getIdentifier());
    cacheTags.add(CacheTag.User, request.getUser().getLogin());
    cacheTags.add(CacheTag.Site, request.getSite().getIdentifier());
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
    return cacheTags;
  }

  /**
   * Returns the singleton instance of this class.
   * 
   * @return the request handler instance
   */
  public static RequestHandler getInstance() {
    return handler;
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
    return "page request handler";
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