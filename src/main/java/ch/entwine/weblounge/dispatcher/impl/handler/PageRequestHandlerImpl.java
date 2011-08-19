/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.dispatcher.impl.handler;

import static ch.entwine.weblounge.common.request.RequestFlavor.ANY;
import static ch.entwine.weblounge.common.request.RequestFlavor.HTML;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.content.Renderer;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.request.CacheTagSet;
import ch.entwine.weblounge.common.impl.request.Http11Constants;
import ch.entwine.weblounge.common.impl.request.Http11Utils;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.dispatcher.PageRequestHandler;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.impl.DispatchUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServletResponse;

/**
 * The <code>PageRequestHandler</code> is used to handle requests to urls simply
 * mapped to a certain template. The request handler will verify access rights
 * and then simply forward the request to the template handler.
 */
public final class PageRequestHandlerImpl implements PageRequestHandler {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(PageRequestHandlerImpl.class);

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-pages/";

  /** The singleton handler instance */
  private static final PageRequestHandlerImpl handler = new PageRequestHandlerImpl();

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

    logger.debug("Page handler agrees to handle {}", request.getUrl());

    Mode processingMode = Mode.Default;
    WebUrl url = request.getUrl();
    String path = url.getPath();
    RequestFlavor contentFlavor = request.getFlavor();

    if (contentFlavor == null || contentFlavor.equals(ANY))
      contentFlavor = RequestFlavor.HTML;

    // Check the request flavor
    // TODO: Criteria would be loading the page from the repository
    // TODO: Think about performance, page lookup is expensive
    if (!HTML.equals(contentFlavor)) {
      logger.debug("Skipping request for {}, flavor {} is not supported", path, request.getFlavor());
      return false;
    }

    // Check the request method
    String requestMethod = request.getMethod();
    if (!Http11Utils.checkDefaultMethods(requestMethod, response)) {
      logger.debug("Page handler answered head request for {}", request.getUrl());
      return true;
    }

    // Create the set of tags that identify the page
    CacheTagSet cacheTags = createCacheTags(request);

    // Check if the request is controlled by an action.
    Action action = (Action) request.getAttribute(WebloungeRequest.ACTION);

    // Check if the page is already part of the cache. If so, our task is
    // already done!
    if (request.getVersion() == Resource.LIVE && action == null) {
      long validTime = Renderer.DEFAULT_VALID_TIME;
      long recheckTime = Renderer.DEFAULT_RECHECK_TIME;

      // Check if the page is already part of the cache
      if (response.startResponse(cacheTags.getTags(), validTime, recheckTime)) {
        logger.debug("Page handler answered request for {} from cache", request.getUrl());
        return true;
      }

      processingMode = Mode.Cached;
    } else if (Http11Constants.METHOD_HEAD.equals(requestMethod)) {
      // handle HEAD requests
      Http11Utils.startHeadResponse(response);
      processingMode = Mode.Head;
    } else if (request.getVersion() == Resource.WORK) {
      response.setMaximumValidTime(0);
    }

    // Add the cache tags (in addition to what the action handler might have
    // set already)
    response.addTags(cacheTags);

    // Set the default maximum render and valid times for pages
    response.setMaximumRecheckTime(Renderer.DEFAULT_RECHECK_TIME);
    response.setMaximumValidTime(Renderer.DEFAULT_VALID_TIME);

    // Get the renderer id that has been registered with the url. For this, we
    // first have to load the page data, then get the associated renderer
    // bundle.
    try {
      Page page = null;
      ResourceURI pageURI = null;
      Site site = request.getSite();

      // Check if a page was passed as an attribute
      if (request.getAttribute(WebloungeRequest.PAGE) != null) {
        page = (Page) request.getAttribute(WebloungeRequest.PAGE);
        pageURI = page.getURI();
      }

      // Load the page from the content repository
      else {
        ContentRepository contentRepository = site.getContentRepository();
        if (contentRepository == null) {
          logger.debug("No content repository found for site '{}'", site);
          return false;
        }

        // Load the page
        try {
          if (action != null) {
            pageURI = getPageURIForAction(action, request);
          } else if (path.startsWith(URI_PREFIX)) {
            String uriSuffix = StringUtils.chomp(path.substring(URI_PREFIX.length()), "/");
            uriSuffix = URLDecoder.decode(uriSuffix, "utf-8");
            pageURI = new PageURIImpl(site, null, uriSuffix);
          } else {
            pageURI = new PageURIImpl(request);
          }

          if (contentRepository.exists(pageURI)) {
            page = (Page) contentRepository.get(pageURI);
          }
        } catch (ContentRepositoryException e) {
          logger.error("Unable to load page {} from {}: {}", new Object[] {
              pageURI,
              contentRepository,
              e.getMessage(),
              e });
          DispatchUtils.sendInternalError(request, response);
          return true;
        }

        // Does it exist at all?
        if (page == null) {
          logger.debug("No page found for {}", pageURI);
          return false;
        }
      }

      // Is it published?
      if (!page.isPublished()) {
        logger.debug("Access to unpublished page {}", pageURI);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return true;
      }

      // Does the client already have up-to-date content?
      if (action == null && !ResourceUtils.hasChanged(request, page)) {
        logger.debug("Page {} was not modified", pageURI);
        DispatchUtils.sendNotModified(request, response);
        return true;
      }

      // Can the page be accessed by the current user?
      User user = request.getUser();
      try {
        // TODO: Check permission
        // PagePermission p = new PagePermission(page, user);
        // AccessController.checkPermission(p);
      } catch (SecurityException e) {
        logger.warn("Accessed to page {} denied for user {}", pageURI, user);
        DispatchUtils.sendAccessDenied(request, response);
        return true;
      }

      // Store the page in the request
      request.setAttribute(WebloungeRequest.PAGE, page);

      // Get hold of the page template
      PageTemplate template = null;
      try {
        template = getPageTemplate(page, request);
      } catch (IllegalStateException e) {
        DispatchUtils.sendInternalError(request, response);
        return true;
      }

      // Does the template support the requested flavor?
      if (!template.supportsFlavor(contentFlavor)) {
        logger.warn("Template '{}' does not support requested flavor {}", template, contentFlavor);
        DispatchUtils.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, request, response);
        return true;
      }
      
      // Add last modified header
      response.setDateHeader("Last-Modified", ResourceUtils.getModificationDate(page).getTime());
      // Add ETag header
      String eTag = ResourceUtils.getETagValue(page);
      response.setHeader("ETag", eTag);
      // Set the content type
      response.setContentType("text/html");

      // Add additional cache tags
      response.addTag(CacheTag.Renderer, template.getIdentifier());

      // Configure valid and recheck time according to the template
      response.setMaximumRecheckTime(template.getRecheckTime());
      response.setMaximumValidTime(template.getValidTime());

      // Set the Expires header
      long expires = response.getMaxiumValidTime();
      if (expires > 0) {
        expires = System.currentTimeMillis() + expires;
      } else {
        expires = System.currentTimeMillis() + Times.MS_PER_MIN;
      }
      response.setDateHeader("Expires", expires);

      // Select the actual renderer by method and have it render the
      // request. Since renderers are being pooled by the bundle, we
      // have to return it after the request has finished.
      try {
        logger.debug("Rendering {} using page template '{}'", path, template);
        template.render(request, response);
      } catch (Throwable t) {
        String params = RequestUtils.dumpParameters(request);
        String msg = "Error rendering template '" + template + "' on '" + path + "' " + params;
        Throwable o = t.getCause();
        if (o != null) {
          msg += ": " + o.getMessage();
          logger.error(msg, o);
        } else {
          logger.error(msg, t);
        }
        DispatchUtils.sendInternalError(request, response);
      }

      return true;
    } catch (EOFException e) {
      logger.debug("Error writing page '{}' back to client: connection closed by client", url);
      return true;
    } catch (IOException e) {
      logger.error("I/O exception while sending error status: {}", e.getMessage(), e);
      return true;
    } finally {
      if (action == null) {
        switch (processingMode) {
          case Cached:
            response.endResponse();
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
   * Tries to determine the target page for the action result. The
   * <code>{@link HTMLAction.TARGET}</code> request attribute and parameter will
   * be considered. In any case, the site's homepage will be the fallback.
   * 
   * @param action
   *          the action handler
   * @param request
   *          the weblounge request
   * @return the target page
   */
  protected ResourceURI getPageURIForAction(Action action,
      WebloungeRequest request) {
    ResourceURI target = null;
    Site site = request.getSite();

    // Check if a target-page parameter was passed
    if (request.getParameter(HTMLAction.TARGET) != null) {
      String targetUrl = request.getParameter(HTMLAction.TARGET);
      try {
        String decocedTargetUrl = null;
        String encoding = request.getCharacterEncoding();
        if (encoding == null)
          encoding = "utf-8";
        decocedTargetUrl = URLDecoder.decode(targetUrl, encoding);
        target = new PageURIImpl(site, decocedTargetUrl);
      } catch (UnsupportedEncodingException e) {
        logger.warn("Error while decoding target url {}: {}", targetUrl, e.getMessage());
        target = new PageURIImpl(site, "/");
      }
    }

    // Nothing found, let's choose the site's homepage
    if (target == null) {
      target = new PageURIImpl(site, "/");
    }

    return target;
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
  protected PageTemplate getPageTemplate(Page page, WebloungeRequest request)
      throws IllegalStateException {
    Site site = request.getSite();
    String templateId = (String) request.getAttribute(WebloungeRequest.TEMPLATE);
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getName()
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