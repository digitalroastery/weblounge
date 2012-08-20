/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.dispatcher.impl.handler;

import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.impl.DispatchUtils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * This request handler deals with requests to <code>/robots.txt</code> if the
 * target site does not ship its own.
 */
public class RobotsRequestHandlerImpl implements RequestHandler {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(RobotsRequestHandlerImpl.class);

  /** Request uri */
  public static final String URI_PREFIX = "/robots.txt";

  /** Path to restrictive robots txt */
  public static final String ROBOTS_DISALLOW_TXT = "/robots/robots-disallow.txt";

  /** Path to default robots txt */
  public static final String ROBOTS_ALLOW_TXT = "/robots/robots-allow.txt";

  /** Content of the default robots.txt */
  private String allowRobotsTxt = null;

  /** Content of the restrictive robots.txt */
  private String disallowRobotsTxt = null;

  /**
   * Creates a new robots.txt request handler.
   * 
   * @throws IOException
   *           if the robots.txt files could not be loaded from the bundle
   *           resource
   */
  public RobotsRequestHandlerImpl() throws IOException {
    InputStream is = null;

    // Read permissive robots.txt
    try {
      is = RobotsRequestHandlerImpl.class.getResourceAsStream(ROBOTS_ALLOW_TXT);
      allowRobotsTxt = IOUtils.toString(is);
    } catch (IOException e) {
      logger.error("Error loading {} from bundle", ROBOTS_ALLOW_TXT);
    } finally {
      IOUtils.closeQuietly(is);
    }

    // Read restrictive robots.txt
    try {
      is = RobotsRequestHandlerImpl.class.getResourceAsStream(ROBOTS_DISALLOW_TXT);
      disallowRobotsTxt = IOUtils.toString(is);
    } catch (IOException e) {
      logger.error("Error loading {} from bundle", ROBOTS_DISALLOW_TXT);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#service(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {
    WebUrl url = request.getUrl();
    String path = url.getPath();

    // Is the request intended for this handler?
    if (!path.equals(URI_PREFIX)) {
      logger.debug("Skipping request for {}, request path does not start with {}", URI_PREFIX);
      return false;
    }

    // Check the request method. Only GET is supported right now.
    String requestMethod = request.getMethod().toUpperCase();
    if ("OPTIONS".equals(requestMethod)) {
      String verbs = "OPTIONS,GET";
      logger.trace("Answering options request to {} with {}", url, verbs);
      response.setHeader("Allow", verbs);
      response.setContentLength(0);
      return true;
    } else if (!"GET".equals(requestMethod)) {
      logger.debug("Robots request handler does not support {} requests", requestMethod);
      DispatchUtils.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, request, response);
      return true;
    }

    // Decide on which directives to send
    String robotsDirective = null;
    if (Environment.Production.equals(request.getEnvironment())) {
      // TODO: Get hold of the site bundle and check for an existing robots.txt
      robotsDirective = allowRobotsTxt;
    } else {
      robotsDirective = disallowRobotsTxt;
    }

    // Send the response
    try {
      response.setContentType("text/plain");
      response.setContentLength(robotsDirective.length());
      IOUtils.write(robotsDirective, response.getOutputStream(), "UTF-8");
    } catch (IOException e) {
      logger.warn("Error sending robots.txt to client: {}", e.getMessage());
    }

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getPriority()
   */
  public int getPriority() {
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getName()
   */
  public String getName() {
    return "robots.txt request handler";
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

}
