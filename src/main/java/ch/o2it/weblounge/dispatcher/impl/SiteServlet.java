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

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.request.Http11ProtocolHandler;
import ch.o2it.weblounge.common.impl.request.Http11ResponseType;
import ch.o2it.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.o2it.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.o2it.weblounge.common.impl.util.classloader.ContextClassLoaderUtils;
import ch.o2it.weblounge.common.impl.util.classloader.JasperClassLoader;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.util.resource.Resource;
import org.ops4j.pax.web.jsp.JspServletWrapper;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
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

  /** The site */
  private final Site site;

  /** The http context */
  private final BundleHttpContext siteHttpContext;

  /** The Jasper servlet */
  protected final Servlet jasperServlet;

  /** Jasper specific class loader */
  private final JasperClassLoader jasperClassLoader;

  /** Path rules */
  private List<ResourceSet> resourceSets = null;

  /**
   * Creates a new site servlet for the given bundle and context.
   * 
   * @param site
   *          the site
   * @param bundle
   *          the site bundle
   * @param httpContext
   *          the http context
   */
  public SiteServlet(final Site site, final BundleHttpContext httpContext) {
    this.site = site;
    this.siteHttpContext = httpContext;
    this.jasperServlet = new JspServletWrapper(httpContext.getBundle());
    this.jasperClassLoader = new JasperClassLoader(httpContext.getBundle(), JasperClassLoader.class.getClassLoader());
    this.resourceSets = new ArrayList<ResourceSet>();
    this.resourceSets.add(new SiteResourceSet());
    this.resourceSets.add(new ModuleResourceSet());
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
    } catch (Throwable ignore) {
      logger.error("Ignored exception", ignore);
    }
  }

  /**
   * Returns the site that is serving content through this servlet.
   * 
   * @return the site
   */
  public Site getSite() {
    return site;
  }

  /**
   * Returns the site's bundle.
   * 
   * @return the bundle
   */
  public Bundle getBundle() {
    return siteHttpContext.getBundle();
  }

  /**
   * Returns the bundle context that is used by this servlet to load the actual
   * content for delivery.
   * 
   * @return the bundle context
   */
  public BundleHttpContext getBundleContext() {
    return siteHttpContext;
  }

  /**
   * Delegates to the jasper servlet.
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
  public void serviceJavaServerPage(final HttpServletRequest httpRequest,
      final HttpServletResponse httpResponse) throws ServletException,
      IOException {

    final HttpServletRequest request;
    final HttpServletResponse response;

    // Wrap request and response if necessary
    if (httpRequest instanceof WebloungeRequest) {
      request = httpRequest;
      response = httpResponse;
    } else {
      request = new WebloungeRequestImpl(httpRequest);
      response = new WebloungeResponseImpl(httpResponse);
      ((WebloungeRequestImpl) request).init(site);
      ((WebloungeResponseImpl) response).setRequest((WebloungeRequestImpl) request);
    }

    // Configure request and response objects

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
    } catch (Throwable t) {
      // re-thrown
      logger.error("Wow, certainly didn't expect this to happen!", t);
      throw new ServletException(t);
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

    Http11ResponseType responseType = null;
    String requestPath = request.getPathInfo();

    // There is also a special set of resources that we don't want to expose
    if (isProtected(requestPath)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // Does the resource exist?
    final URL url = siteHttpContext.getResource(requestPath);
    if (url == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // Load the resource from the bundle
    final Resource resource = Resource.newResource(url);
    if (!resource.exists()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // We don't allow directory listings
    if (resource.isDirectory()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    URLConnection conn = url.openConnection();
    String mimeType = siteHttpContext.getMimeType(requestPath);
    String encoding = null;

    // Try to get mime type and content encoding from resource
    if (mimeType == null)
      mimeType = conn.getContentType();
    encoding = conn.getContentEncoding();

    if (mimeType != null) {
      if (encoding != null)
        mimeType += ";" + encoding;
      response.setContentType(mimeType);
    }

    // Send the response back to the client
    InputStream is = resource.getInputStream();
    try {
      logger.debug("Serving {}", url);
      responseType = Http11ProtocolHandler.analyzeRequest(request, resource.lastModified(), Times.MS_PER_DAY + System.currentTimeMillis(), resource.length());
      if (!Http11ProtocolHandler.generateResponse(response, responseType, is)) {
        logger.warn("I/O error while generating content from {}", url);
      }
    } finally {
      is.close();
    }

  }

  /**
   * Returns <code>true</code> if the resource is protected. Examples of
   * protected resources are <code>web.xml</code> inside of the
   * <code>WEB-INF</code> directory etc.
   * 
   * @param path
   *          the path to the resource that is about to be served
   * @return <code>true</code> if the resource needs to be protected
   */
  public boolean isProtected(String path) {
    for (ResourceSet resourceSet : resourceSets) {
      if (resourceSet.includes(path) && resourceSet.excludes(path))
        return true;
    }
    return false;
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
    } catch (Throwable t) {
      logger.error("Wow, certainly didn't expect this to happen!", t);
    }
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "site " + site;
  }

}
