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

package ch.entwine.weblounge.dispatcher.impl.http;

import ch.entwine.weblounge.dispatcher.DispatcherConfiguration;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of a servlet that forwards requests to a welcome file.
 */
public class WelcomeFileFilterServlet extends HttpServlet {

  /** Serial version uid */
  private static final long serialVersionUID = 4015041977387112025L;

  /** List of welcome files */
  private List<String> welcomeFileList = new ArrayList<String>();

  /** Application root path */
  private String appRoot = null;
  
  /** Bundle root uri */
  private String bundleUriNamespace = null;

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    if (req.getPathInfo() != null && "/".equals(req.getPathInfo())) {
      if (!welcomeFileList.isEmpty()) {
        // use the first one for now:
        for (String welcomeFile : welcomeFileList) {
          String file = welcomeFile;
          if (welcomeFile.startsWith("/"))
            file = welcomeFile.substring(1);
          RequestDispatcher dispatcher = req.getRequestDispatcher(file);
          // TODO: check if resource exists before forwarding
          dispatcher.forward(req, res);
          return;
        }
      } else {
        req.getRequestDispatcher(req.getServletPath() + "/resources/index.html").forward(req, res); 
        // Tomcat also defaults to index.jsp
        return;
      }
    } else {
      // no welcome file, trying to forward to remapped resource:
      // /resources"+req.getPathInfo()
      req.getRequestDispatcher(req.getServletPath() + "/resources" + req.getPathInfo()).forward(req, res);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    String welcomeFilesDelimited = config.getInitParameter(DispatcherConfiguration.WELCOME_FILES);
    if (welcomeFilesDelimited != null)
      welcomeFileList = Arrays.asList(StringUtils.split(welcomeFilesDelimited, ";"));
    appRoot = config.getInitParameter(DispatcherConfiguration.WEBAPP_CONTEXT_ROOT);
    if (appRoot == null || "/".equals(appRoot))
      appRoot = "";
    bundleUriNamespace = config.getInitParameter(DispatcherConfiguration.BUNDLE_CONTEXT_ROOT_URI);
    if (bundleUriNamespace == null || "/".equals(bundleUriNamespace))
      bundleUriNamespace = "";
  }

}
