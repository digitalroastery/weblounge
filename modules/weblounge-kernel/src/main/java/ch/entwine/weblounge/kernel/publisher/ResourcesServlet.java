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

package ch.entwine.weblounge.kernel.publisher;

import ch.entwine.weblounge.common.impl.request.Http11ProtocolHandler;
import ch.entwine.weblounge.common.impl.request.Http11ResponseType;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
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
public class ResourcesServlet extends HttpServlet {

  /** Serial version uid */
  private static final long serialVersionUID = 4007081684493732350L;

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(ResourcesServlet.class);

  /** Directory with external resources */
  protected File resourcesDir = null;

  /** Bundle context */
  private Bundle bundle = null;

  /** The path into the bundle's classpath */
  private String bundlePath = null;

  /** The file to serve when the root directory is hit */
  private String welcomeFile = null;

  /** The servlet configuration */
  private ServletConfig servletConfig = null;

  /**
   * Creates a new servlet that serves resources from the bundle's classpath.
   * The resources are exposed in such a way that <code>bundlePath</code> will
   * be mounted as the root.
   * 
   * @param bundle
   *          the bundle
   * @param bundlePath
   *          path into the bundle's classpath
   * @param welcomeFile
   *          the file to serve if the resource mountpoint is hit
   * @throws IllegalArgumentException
   *           if <code>bundle</code> or <code>bundlePath</code> are
   *           <code>null</code>
   */
  public ResourcesServlet(Bundle bundle, String bundlePath, String welcomeFile) {
    this(null, bundle, bundlePath, welcomeFile);
  }

  /**
   * Creates a new servlet that serves resources from the bundle's classpath.
   * The resources are exposed in such a way that <code>bundlePath</code> will
   * be mounted as the root.
   * 
   * @param bundle
   *          the bundle
   * @param bundlePath
   *          path into the bundle's classpath
   * @throws IllegalArgumentException
   *           if <code>bundle</code> or <code>bundlePath</code> are
   *           <code>null</code>
   */
  public ResourcesServlet(Bundle bundle, String bundlePath) {
    this(null, bundle, bundlePath, null);
    if (bundle == null)
      throw new IllegalArgumentException("Bundle must not be null");
    if (StringUtils.isBlank(bundlePath))
      throw new IllegalArgumentException("Bundle path must not be blank");
  }

  /**
   * Creates a new servlet that serves resources from an arbitrary directory on
   * the filesystem.
   * 
   * @param resourceDir
   *          directory for resources
   * @throws IllegalArgumentException
   *           if <code>resourcesDir</code> is <code>null</code>
   */
  public ResourcesServlet(File resourceDir) {
    this(resourceDir, null, null, null);
    if (resourceDir == null)
      throw new IllegalArgumentException("Resource directory must not be null");
  }

  /**
   * Creates a new servlet that serves both resources from an arbitrary
   * directory on the filesystem and the bundle's classpath.
   * 
   * @param resourceDir
   *          directory for external resources
   * @param bundle
   *          the bundle
   * @param bundlePath
   *          path into the bundle's classpath
   * @param welcomeFile
   *          the file to serve if the resource mountpoint is hit
   * @throws IllegalArgumentException
   *           if both <code>resourcesDir</code> and <code>bundle</code> are
   *           <code>null</code>
   */
  public ResourcesServlet(File resourceDir, Bundle bundle, String bundlePath,
      String welcomeFile) {
    this.resourcesDir = resourceDir;
    this.bundle = bundle;
    this.bundlePath = bundlePath;
    this.welcomeFile = welcomeFile;
    if (resourceDir == null && (bundle == null || StringUtils.isBlank(bundlePath)))
      throw new IllegalArgumentException("Either one of resource directory or bundle and bundle path must be provided");
    MimetypesFileTypeMap fileTypes = (MimetypesFileTypeMap) FileTypeMap.getDefaultFileTypeMap();
    fileTypes.addMimeTypes("text/javascript js");
    fileTypes.addMimeTypes("text/css css");
    FileTypeMap.setDefaultFileTypeMap(fileTypes);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  @Override
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
      if (StringUtils.isNotBlank(welcomeFile)) {
        requestPath = PathUtils.concat("/", welcomeFile);
      } else {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
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
      if (welcomeFile != null && "/".equals(requestPath))
        requestPath = welcomeFile;
      String resourcePath = UrlUtils.concat(bundlePath, requestPath);
      url = bundle.getResource(resourcePath);
    }

    // Check if the resource exists
    if (url == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // Try to load the file contents. In case of an IOException, it is highly
    // likely that we've hit a directory, since the bundle had returned a valid
    // entry before.
    URLConnection conn = null;
    try {
      conn = url.openConnection();
    } catch (IOException e) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    contentLength = conn.getContentLength();
    lastModified = conn.getLastModified();
    String mimeType = servletConfig.getServletContext().getMimeType(requestPath);
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
    InputStream is = url.openStream();
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

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Weblounge resources servlet";
  }

}
