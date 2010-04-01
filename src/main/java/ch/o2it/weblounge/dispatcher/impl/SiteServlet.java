/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

import org.apache.commons.io.FilenameUtils;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.resource.Resource;
import org.ops4j.pax.web.jsp.JspServletWrapper;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that knows how to deal with resources loaded from <code>OSGi</code>
 * context.
 */
public class SiteServlet extends HttpServlet {

  /** The serial version UID */
  private static final long serialVersionUID = 6443055837961417300L;

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SiteServlet.class);

  /** If-None-Match HTTP header name */
  private static final String IFNONEMATCH_HEADER = "If-None-Match";

  /** ETag HTTP header name */
  private static final String ETAG = "ETag";
  
  /** The http context */
  private final HttpContext siteHttpContext;

  /** The Jasper servlet */
  protected final Servlet jasperServlet;

  /** Jasper specific class loader */
  private final JasperClassLoader jasperClassLoader;

  /**
   * Creates a new site servlet for the given bundle and context.
   * 
   * @param bundle
   *          the site bundle
   * @param httpContext
   *          the http context
   */
  public SiteServlet(final BundleHttpContext httpContext) {
    siteHttpContext = httpContext;
    jasperServlet = new JspServletWrapper(httpContext.getBundle());
    jasperClassLoader = new JasperClassLoader(httpContext.getBundle(), JasperClassLoader.class.getClassLoader());
  }

  /**
   * Delegates to the jasper servlet with a controlled context class loader.
   * 
   * @see JspServletWrapper#init(ServletConfig)
   */
  public void init(final ServletConfig config) throws ServletException {
    try {
      ContextClassLoaderUtils.doWithClassLoader(jasperClassLoader, new Callable<Void>() {
        public Void call() throws Exception {
          jasperServlet.init(config);
          return null;
        }
      });
    } catch (ServletException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception ignore) {
      logger.error("Ignored exception", ignore);
    }
  }

  /**
   * Delegates to jasper servlet.
   * 
   * @see JspServletWrapper#getServletConfig()
   */
  public ServletConfig getServletConfig() {
    return jasperServlet.getServletConfig();
  }

  /**
   * Depending on whether a call to a jsp is made or not, delegates to the
   * jasper servlet with a controlled context class loader or tries to load the
   * requested file from the bundle as a static resource.
   * 
   * @see HttpServlet#service(HttpServletRequest, HttpServletResponse)
   */
  public void service(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {
    String filename = FilenameUtils.getName(request.getPathInfo());
    if (filename.endsWith(".jsp")) {
      serviceJavaServerPage(request, response);
    } else {
      serviceResource(request, response);
    }
  }

  /**
   * Delegates to jasper servlet with a controlled context class loader.
   * 
   * @see JspServletWrapper#service(HttpServletRequest, HttpServletResponse)
   */
  public void serviceJavaServerPage(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {
    try {
      ContextClassLoaderUtils.doWithClassLoader(jasperClassLoader, new Callable<Void>() {
        public Void call() throws Exception {
          jasperServlet.service(request, response);
          return null;
        }
      });
    } catch (ServletException e) {
      // re-thrown
      throw e;
    } catch (IOException e) {
      // re-thrown
      throw e;
    } catch (RuntimeException e) {
      // re-thrown
      throw e;
    } catch (Exception e) {
      logger.error("Wow, certainly didn't expect this to happen!", e);
    }
  }

  /**
   * Tries to serve the request as a static resource from the bundle.
   * 
   * @param request
   *          the http servlet request
   * @param response
   *          the http servlet response
   * @throws ServletException
   *           if serving the request fails
   * @throws IOException
   *           if writing the response back to the client fails
   */
  protected void serviceResource(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {

    String requestPath = request.getPathInfo();

    // Does the resource exist?
    final URL url = siteHttpContext.getResource(requestPath);
    if (url == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // Load the resource from the bundle
    final Resource resource = Resource.newResource(url, false);
    if (!resource.exists()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // We don't allow directory listings
    if (resource.isDirectory()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // If the request contains an etag and its the same for the resource, we
    // deliver a not modified response
    String eTag = String.valueOf(resource.lastModified());
    if ((request.getHeader(IFNONEMATCH_HEADER) != null) && (eTag.equals(request.getHeader(IFNONEMATCH_HEADER)))) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }
    
    // Set the etag
    response.setHeader(ETAG, eTag);

    String mimeType = siteHttpContext.getMimeType(requestPath);
    if (mimeType == null) {
      try {
        mimeType = url.openConnection().getContentType();
      } catch (IOException ignore) {
        // we do not care about such an exception as the fact that we are using
        // also the connection for finding the mime type is just a
        // "nice to have" rather than a requirement
      }
    }
    if (mimeType != null) {
      response.setContentType(mimeType);
      // TODO shall we handle also content encoding?
    }

    // Send the response back to the client
    OutputStream out = response.getOutputStream();
    if (out instanceof HttpConnection.Output) {
      ((HttpConnection.Output) out).sendContent(resource.getInputStream());
    } else {
      resource.writeTo(out, 0, resource.length());
    }
    response.setStatus(HttpServletResponse.SC_OK);
  }

  /**
   * Delegates to jasper servlet.
   * 
   * @see JspServletWrapper#getServletInfo()
   */
  public String getServletInfo() {
    return jasperServlet.getServletInfo();
  }

  /**
   * Delegates to jasper servlet with a controlled context class loader.
   * 
   * @see JspServletWrapper#destroy()
   */
  public void destroy() {
    try {
      ContextClassLoaderUtils.doWithClassLoader(jasperClassLoader, new Callable<Void>() {
        public Void call() throws Exception {
          jasperServlet.destroy();
          return null;
        }
      });
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Wow, certainly didn't expect this to happen!", e);
    }
  }

}
