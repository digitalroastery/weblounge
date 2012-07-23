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

package ch.entwine.weblounge.kernel.site;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.request.Http11ProtocolHandler;
import ch.entwine.weblounge.common.impl.request.Http11ResponseType;
import ch.entwine.weblounge.common.impl.request.SiteRequestWrapper;
import ch.entwine.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.entwine.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
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

  /** The supported formats */
  public enum Format {
    Processed, Raw
  };

  /** Parameter name for the output format */
  public static final String PARAM_FORMAT = "format";

  /** The site */
  private final Site site;

  /** The site bundle */
  private final Bundle bundle;

  /** The Jasper servlet */
  protected final Servlet jasperServlet;

  /** Path rules */
  private List<ResourceSet> resourceSets = null;

  /** The security service */
  private SecurityService securityService = null;

  /** Tika mime type library */
  private Tika tika = null;

  /** Flag to reflect servlet initialization */
  private boolean initialized = false;

  /** The environment */
  private Environment environment = Environment.Production;

  /**
   * Creates a new site servlet for the given bundle and context.
   * 
   * @param site
   *          the site
   * @param bundle
   *          the site bundle
   * @param bundle
   *          the site bundle
   * @param environment
   *          the environment
   */
  public SiteServlet(final Site site, final Bundle bundle,
      Environment environment) {
    this.site = site;
    this.bundle = bundle;
    this.environment = environment;
    this.jasperServlet = new JspServletWrapper(bundle);
    this.resourceSets = new ArrayList<ResourceSet>();
    this.resourceSets.add(new SiteResourceSet());
    this.resourceSets.add(new ModuleResourceSet());
    this.tika = new Tika();
  }

  /**
   * Delegates to the jasper servlet with a controlled context class loader.
   * 
   * @see JspServletWrapper#init(ServletConfig)
   */
  public void init(final ServletConfig config) throws ServletException {
    jasperServlet.init(config);
    initialized = true;
  }

  /**
   * Returns <code>true</code> if the servlet has been initialized.
   * 
   * @return <code>true</code> if the servlet has been initialized
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Sets the environment.
   * 
   * @param environment
   *          the environment
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
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
    return bundle;
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

    // Don't allow listing the root directory?
    if (StringUtils.isBlank(filename)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // Check the requested format. In case of a JSP, this can either be
    // processed (default) or raw, in which case the file contents are
    // returned rather than Jasper's output of it.
    Format format = Format.Processed;
    String f = request.getParameter(PARAM_FORMAT);
    if (StringUtils.isNotBlank(f)) {
      try {
        format = Format.valueOf(StringUtils.capitalize(f.toLowerCase()));
      } catch (IllegalArgumentException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
    }

    if (Format.Processed.equals(format) && filename.endsWith(".jsp")) {
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
    if (httpRequest instanceof SiteRequestWrapper) {
      request = httpRequest;
      response = httpResponse;
    } else if (httpRequest instanceof WebloungeRequest) {
      request = new SiteRequestWrapper((WebloungeRequest) httpRequest, httpRequest.getPathInfo(), false);
      response = httpResponse;
    } else {
      WebloungeRequestImpl webloungeRequest = new WebloungeRequestImpl(httpRequest, environment);
      webloungeRequest.init(site);
      webloungeRequest.setUser(securityService.getUser());
      String requestPath = UrlUtils.concat("/site", httpRequest.getPathInfo());
      request = new SiteRequestWrapper(webloungeRequest, requestPath, false);
      response = new WebloungeResponseImpl(httpResponse);
      ((WebloungeResponseImpl) response).setRequest(webloungeRequest);
    }

    // Configure request and response objects

    try {
      jasperServlet.service(request, response);
    } catch (ServletException e) {
      // re-thrown
      throw e;
    } catch (IOException e) {
      // re-thrown
      throw e;
    } catch (Throwable t) {
      // re-thrown
      logger.error("Error while serving jsp {}: {}", request.getRequestURI(), t.getMessage());
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

    String bundlePath = UrlUtils.concat("/site", requestPath);

    // Does the resource exist?
    final URL url = bundle.getResource(bundlePath);
    if (url == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // Load the resource from the bundle
    URLConnection connection = url.openConnection();
    String contentEncoding = connection.getContentEncoding();
    long contentLength = connection.getContentLength();
    long lastModified = connection.getLastModified();

    if (contentLength <= 0) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // final Resource resource = Resource.newResource(url);
    // if (!resource.exists()) {
    // response.sendError(HttpServletResponse.SC_NOT_FOUND);
    // return;
    // }

    // We don't allow directory listings
    // if (resource.isDirectory()) {
    // response.sendError(HttpServletResponse.SC_FORBIDDEN);
    // return;
    // }

    String mimeType = tika.detect(bundlePath);

    // Try to get mime type and content encoding from resource
    if (mimeType == null)
      mimeType = connection.getContentType();

    if (mimeType != null) {
      if (contentEncoding != null)
        mimeType += ";" + contentEncoding;
      response.setContentType(mimeType);
    }

    // Send the response back to the client
    InputStream is = connection.getInputStream();
    // InputStream is = resource.getInputStream();
    try {
      logger.debug("Serving {}", url);
      responseType = Http11ProtocolHandler.analyzeRequest(request, lastModified, Times.MS_PER_DAY + System.currentTimeMillis(), contentLength);
      if (!Http11ProtocolHandler.generateResponse(response, responseType, is)) {
        logger.warn("I/O error while generating content from {}", url);
      }
    } finally {
      IOUtils.closeQuietly(is);
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
    jasperServlet.destroy();
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
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "site " + site;
  }

}
