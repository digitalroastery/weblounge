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

package ch.entwine.weblounge.kernel.shared;

import ch.entwine.weblounge.common.impl.request.Http11ProtocolHandler;
import ch.entwine.weblounge.common.impl.request.Http11ResponseType;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.activation.FileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that serves both resources from an arbitrary directory or a bundle's
 * classpath.
 * <p>
 * Note that in cases where both a directory and a bundle have been configured,
 * and a resource exists in both places, the directory takes precedence. This
 * allows to overwrite
 */
public class WebloungeResourcesServlet extends HttpServlet {

  /** Serial version uid */
  private static final long serialVersionUID = 4007081684493732350L;

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeSharedResources.class);

  /** Directory with external resources */
  protected File resourcesDir = null;

  /** Bundle context */
  private Bundle bundle = null;

  /** The path into the bundle's classpath */
  private String bundlePath = null;

  /** The servlet configuration */
  private ServletConfig servletConfig = null;

  /** Mime type detector */
  private Tika mimeTypeDetector = new Tika();

  /**
   * Creates a new servlet that serves resources from the bundle's classpath.
   * The resources are exposed in such a way that <code>bundlePath</code> will
   * be mounted as the root.
   * 
   * @param bundle
   *          the bundle
   * @param bundlePath
   *          path into the bundle's classpath
   */
  public WebloungeResourcesServlet(Bundle bundle, String bundlePath) {
    this(null, bundle, bundlePath);
    if (bundle == null)
      throw new IllegalArgumentException("Bundle must not be null");
    if (StringUtils.isBlank(bundlePath))
      throw new IllegalArgumentException("Bundle path must not be blank");
  }

  /**
   * Creates a new servlet that serves resources from an arbitrary directory on
   * the filesystem.
   * 
   * @param resourcesDir
   *          directory for resources
   */
  public WebloungeResourcesServlet(File resourcesDir) {
    this(resourcesDir, null, null);
    if (resourcesDir == null)
      throw new IllegalArgumentException("Resources directory must not be null");
  }

  /**
   * Creates a new servlet that serves both resources from an arbitrary
   * directory on the filesystem and the bundle's classpath.
   * 
   * @param resourcesDir
   *          directory for external resources
   * @param bundle
   *          the bundle
   * @param bundlePath
   *          path into the bundle's classpath
   * @throws IllegalArgumentException
   *           if both <code>resourcesDir</code> and <code>bundle</code> are
   *           <code>null</code>
   */
  public WebloungeResourcesServlet(File resourcesDir, Bundle bundle,
      String bundlePath) {
    this.resourcesDir = resourcesDir;
    this.bundle = bundle;
    this.bundlePath = bundlePath;
    if (resourcesDir == null && (bundle == null || StringUtils.isBlank(bundlePath)))
      throw new IllegalArgumentException("Either one of resources directory or bundle and bundle path must be provided");
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    this.servletConfig = config;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse)
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Http11ResponseType responseType = null;
    String requestPath = request.getPathInfo();
    URL url = null;
    long lastModified = 0L;
    long contentLength = 0L;

    // Are we looking at the top-level directory?
    if (requestPath == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // Serve from directory

    if (resourcesDir != null) {
      File resource = new File(PathUtils.concat(resourcesDir.getAbsolutePath(), requestPath));

      // Don't serve directory listings
      if (resource.isDirectory() || StringUtils.isBlank((FilenameUtils.getExtension(requestPath)))) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }

      // Check if the file exists
      if (resource.exists() && resource.canRead()) {
        url = resource.toURI().toURL();
      }
    }

    // Serve from bundle if the same resource wasn't loaded from the directory
    // already

    if (bundle != null && url == null) {
      String resourcePath = UrlUtils.concat(bundlePath, requestPath);
      url = bundle.getEntry(resourcePath);
    }

    // Check if the resource exists
    if (url == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    URLConnection conn = url.openConnection();
    contentLength = conn.getContentLength();
    lastModified = conn.getLastModified();

    // A mime type would be nice as well
    String mimeType = servletConfig.getServletContext().getMimeType(requestPath);
    InputStream is = null;
    if (StringUtils.isBlank(mimeType)) {
      mimeType = mimeTypeDetector.detect(requestPath);
      if (mimeType == null) {
        try {
          is = url.openStream();
          mimeType = mimeTypeDetector.detect(is);
        } catch (IOException e) {
          logger.warn("Error detecting mime type: {}", e.getMessage());
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
    }

    String encoding = null;

    // Try to get mime type and content encoding from resource
    if (mimeType == null)
      mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(requestPath);
    if (mimeType == null)
      mimeType = conn.getContentType();
    encoding = conn.getContentEncoding();

    if (mimeType != null) {
      if (encoding != null)
        mimeType += ";" + encoding;
      response.setContentType(mimeType);
    }

    // Send the response back to the client
    is = url.openStream();
    try {
      logger.debug("Serving {}", url);
      responseType = Http11ProtocolHandler.analyzeRequest(request, lastModified, 0, contentLength);
      if (!Http11ProtocolHandler.generateResponse(response, responseType, is)) {
        logger.warn("I/O error while generating content from {}", url);
      }
    } finally {
      IOUtils.closeQuietly(is);
    }

  }
}
