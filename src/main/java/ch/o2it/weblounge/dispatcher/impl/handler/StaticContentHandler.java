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

package ch.o2it.weblounge.dispatcher.impl.handler;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.request.Http11ProtocolHandler;
import ch.o2it.weblounge.common.impl.request.Http11ResponseType;
import ch.o2it.weblounge.common.impl.request.Http11Utils;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.dispatcher.impl.DispatchUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Pattern;

import javax.activation.FileTypeMap;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>StaticContentHandler</code> maps the request uri to local file
 * paths and returns its content to the client.
 * <p>
 * This handler is used as a last resort in the the chain of request handlers,
 * meaning that if no other handler is taking responsibility for a request, this
 * handler is explicitly called to make sure static resources are being served
 * instead.
 */
public class StaticContentHandler implements RequestHandler, Times {

  /** The logger */
  private static final Logger log = LoggerFactory.getLogger(StaticContentHandler.class);

  /** The singleton instance */
  private static StaticContentHandler instance = null;

  /** The mime type definitions */
  private static final FileTypeMap mimeTypes = FileTypeMap.getDefaultFileTypeMap();

  /** Path rules */
  // TODO: Configure
  private static final PathRule url_rules[] = {
      new PathRule("^/(?:sites/[^/]+|shared)/module/[^/]+/[^/]+/", new String[] { "^/(?:sites/[^/]+|shared)/module/[^/]+/(?:lib|classes|conf|doc)/" }),
      new PathRule("^/(?:sites/[^/]+|shared)/[^/]+/", new String[] { "^/(?:sites/[^/]+|shared)/(?:lib|classes|module)/" }) };

  /**
   * Returns the singleton instance of this content handler.
   * 
   * @return the static content handler
   */
  public static StaticContentHandler getInstance() {
    if (instance == null) {
      instance = new StaticContentHandler();
    }
    return instance;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.RequestHandler#getIdentifier()
   */
  public String getIdentifier() {
    return "static";
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.RequestHandler#getName()
   */
  public String getName() {
    return "Static content handler";
  }

  /**
   * Serve the static content located at url (relative to the webapp root).
   * 
   * @param req
   *          the http servlet request
   * @param resp
   *          the http servlet response
   * @param url
   *          the url to include
   */
  public void service(ServletRequest req, ServletResponse resp, String url) {
    WebloungeRequest request = null;
    WebloungeResponse response = null;
    try {
      request = (WebloungeRequest) req;
      response = (WebloungeResponse) resp;
    } catch (ClassCastException e) {
      log.error("Error casting request and response to Http objects", e);
      return;
    }

    // check the request method
    String method = request.getMethod();
    if (!Http11Utils.checkDefaultMethods(method, response))
      return;

    try {
      log.trace("Static request for {}", url);
      // check the url
      if (url == null) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      URL contentRootURL = request.getSite().getStaticContentRoot();
      String protocol = contentRootURL.getProtocol();
      Http11ResponseType responseType = null;
      InputStream is = null;

      // Are we looking at a filesystem-based content root?
      if ("file".equals(protocol)) {
        File file = new File(contentRootURL.toExternalForm(), url);
        if (!file.exists() || !file.canRead()) {
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }

        // we do not provide directory listings!
        if (!file.isFile()) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
        }

        /* analyze the HTTP request and generate headers */
        responseType = Http11ProtocolHandler.analyzeRequest(request, file.lastModified(), MS_PER_DAY + System.currentTimeMillis(), file.length());
        Http11ProtocolHandler.generateHeaders(response, responseType);

        /* write the response */
        is = new FileInputStream(file);
      }

      // Let's just try to open the stream from a web server
      else if ("http".equals(protocol) || "https".equals(protocol)) {
        try {
          is = contentRootURL.openStream();
        } catch (IOException e) {
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
      }

      // We can't handle this type of urls.
      else {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      // Set the content type
      String contentType = mimeTypes.getContentType(contentRootURL.getFile());
      if (contentType != null)
        response.setContentType(contentType);

      // Finally, start sending the response
      try {
        log.debug("Serving {}", contentRootURL);
        Http11ProtocolHandler.generateResponse(response, responseType, is);
      } finally {
        is.close();
      }
    } catch (IOException e) {
      log.error("Exception while handling static request for {}", url, e);
    }
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#service(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse)
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null)
      return false;

    for (int i = 0; i < url_rules.length; i++) {
      PathRule rule = url_rules[i];
      if (rule.includes(pathInfo)) {
        if (rule.excludes(pathInfo))
          return false;
        try {
          log.trace("Handling request {}", request.getUrl());
          DispatchUtils.forward(request, response, pathInfo);
        } catch (Exception e) {
          Throwable o = e.getCause();
          if (o != null) {
            String msg = "Error while dispatching static request to " + pathInfo + ": ";
            msg += e.getMessage();
            log.error(msg, o);
          } else {
            String msg = "Error while dispatching static request " + pathInfo + ": ";
            log.error(msg, e);
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * A <code>PathRule</code> is used to make sure content is only loaded from
   * directories on the file system that should be accessible.
   */
  private static final class PathRule {

    /** the include pattern */
    private Pattern include;

    /** the exclude pattern */
    private Pattern[] excludes;

    /**
     * Creates a new <code>PathRule</code>.
     * 
     * @param include
     *          the include pattern
     * @param excludes
     *          the exclude pattern
     */
    public PathRule(String include, String excludes[]) {
      this.include = Pattern.compile(include);
      this.excludes = new Pattern[excludes.length];
      for (int i = 0; i < excludes.length; i++) {
        this.excludes[i] = Pattern.compile(excludes[i]);
      }
    }

    /**
     * Checks whether this <code>PathRule</code> includes the given path.
     * 
     * @param path
     *          the path to check
     * @return <code>true</code> if the path is included
     */
    public boolean includes(String path) {
      return include.matcher(path).lookingAt();
    }

    /**
     * Checks whether this <code>PathRule</code> excludes the given path.
     * 
     * @param path
     *          the path to check
     * @return <code>true</code> if the path is excluded
     */
    public boolean excludes(String path) {
      for (int i = 0; i < excludes.length; i++)
        if (excludes[i].matcher(path).lookingAt())
          return true;
      return false;
    }

  }

}