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

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.impl.DispatchUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * This request handler tries to handle requests which could not be served by
 * any other request handler.
 */
public class NotFoundRequestHandlerImpl implements RequestHandler {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(NotFoundRequestHandlerImpl.class);

  @Override
  public boolean service(WebloungeRequest request, WebloungeResponse response) {

    logger.debug("Not Found Request Handler starts handling request");

    Site site = request.getSite();
    WebUrl url = request.getRequestedUrl();
    String path = request.getRequestURI();

    ResourceURI pageUri = site.getErrorPage(path);

    if (pageUri == null) {
      logger.debug("Site {} has no 404 error page for path '{}' configured", site, path);
      return false;
    }

    // Check the request method. This handler only supports GET
    String requestMethod = request.getMethod().toUpperCase();
    if ("OPTIONS".equals(requestMethod)) {
      String verbs = "OPTIONS,GET";
      logger.trace("Answering options request to {} with {}", url, verbs);
      response.setHeader("Allow", verbs);
      response.setContentLength(0);
      return true;
    } else if (!"GET".equals(requestMethod)) {
      logger.debug("Feed request handler does not support {} requests", url, requestMethod);
      DispatchUtils.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, request, response);
      return true;
    }

    // Check if we have a working content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("Content repository not available to read page '{}'", pageUri);
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Read the page from the content repository
    Page page;
    try {
      page = contentRepository.get(pageUri);
    } catch (ContentRepositoryException e) {
      logger.error("Error getting page '{}' from content repository", pageUri);
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    if (page == null) {
      logger.warn("The 404 error page '{}' could not be found in the content repository");
      DispatchUtils.sendError(HttpServletResponse.SC_NOT_FOUND, request, response);
      return true;
    }

    logger.debug("Found 404 error page '{}' for requested path '{}", page, path);

    // Let the page request handler do the remaining work
    request.setAttribute(WebloungeRequest.PAGE, page);
    PageRequestHandlerImpl.getInstance().service(request, response);
    request.removeAttribute(WebloungeRequest.PAGE);

    return true;
  }

  @Override
  public int getPriority() {
    // Make sure, this request handler is last in the chain
    return -100;
  }

  @Override
  public String getName() {
    return toString();
  }

  @Override
  public String toString() {
    return "404 Not Found Request Handler";
  }

}
