/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.kernel;

import ch.o2it.weblounge.common.impl.request.Http11ProtocolHandler;
import ch.o2it.weblounge.common.impl.request.Http11ResponseType;
import ch.o2it.weblounge.common.impl.url.PathUtils;

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
 * Servlet that serves both resources from an external directory and the kernel
 * bundle's <code>html</code> resource directory.
 * <p>
 * Note that in cases where a resource exists in both places, the external
 * directory takes precedence.
 */
public class WebloungeSharedResourcesServlet extends HttpServlet {

  /** Serial version uid */
  private static final long serialVersionUID = 4007081684493732350L;

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeSharedResources.class);

  /** Directory with external resources */
  protected File externalResourcesDir = null;

  /** Bundle context */
  private Bundle bundle = null;
  
  /** The servlet config */
  private ServletConfig servletConfig = null;

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    this.servletConfig = config;
  }

  /**
   * Creates a new servlet that serves both resources from an external directory
   * and the kernel bundle's <code>html</code> resource directory.
   * 
   * @param externalResourcesDir
   *          directory for external resources
   * @param bundle
   *          the kernel bundle
   */
  WebloungeSharedResourcesServlet(File externalResourcesDir, Bundle bundle) {
    this.externalResourcesDir = externalResourcesDir;
    this.bundle = bundle;
    MimetypesFileTypeMap fileTypes = (MimetypesFileTypeMap)FileTypeMap.getDefaultFileTypeMap();
    fileTypes.addMimeTypes("text/javascript js");
    fileTypes.addMimeTypes("text/css css");
    FileTypeMap.setDefaultFileTypeMap(fileTypes);
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
    long contentLength = 0L;

    // Are we looking at the top-level directory?
    if (requestPath == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    File resource = new File(PathUtils.concat(externalResourcesDir.getAbsolutePath(), requestPath));

    // Don't serve directory listings
    if (resource.isDirectory() || StringUtils.isBlank((FilenameUtils.getExtension(requestPath)))) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // If the resource doesn't exist, try to serve it from the bundle
    if (resource.exists() && resource.canRead()) {
      url = resource.toURI().toURL();
    } else {
      String resourcePath = PathUtils.concat(WebloungeSharedResources.RESOURCES_BUNDLE_DIR, requestPath);
      url = bundle.getEntry(resourcePath);
      if (url == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
    }

    URLConnection conn = url.openConnection();
    contentLength = conn.getContentLength();
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
      responseType = Http11ProtocolHandler.analyzeRequest(request, resource.lastModified(), 0, contentLength);
      if (!Http11ProtocolHandler.generateResponse(response, responseType, is)) {
        logger.warn("I/O error while generating content from {}", url);
      }
    } finally {
      IOUtils.closeQuietly(is);
    }

  }

}
