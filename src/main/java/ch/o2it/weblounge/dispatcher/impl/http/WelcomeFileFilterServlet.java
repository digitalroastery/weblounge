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

package ch.o2it.weblounge.dispatcher.impl.http;

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

public class WelcomeFileFilterServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public static final String WELCOME_FILES = "weblounge.http.WELCOME_FILES";

  List<String> welcomeFileList = new ArrayList<String>();
  String appRoot = null;
  String bundleUriNamespace = null;

  public void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    if (req.getPathInfo() != null && req.getPathInfo().equals("/")) {
      if (welcomeFileList != null && !welcomeFileList.isEmpty()) {
        // use the first one for now:
        for (String welcomeFile : welcomeFileList) {
          try {
            if (welcomeFile.startsWith("/"))
              welcomeFile = welcomeFile.substring(1);

            RequestDispatcher dispatcher = req.getRequestDispatcher(welcomeFile);
            // TODO: check if resource exists before forwarding
            dispatcher.forward(req, res);
            return;
          } catch (Exception e) {
            e.printStackTrace();
          }
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

  public void init(ServletConfig config) throws ServletException {

    String welcomeFilesDelimited = config.getInitParameter(WELCOME_FILES);
    if (welcomeFilesDelimited != null)
      welcomeFileList = Arrays.asList(StringUtils.split(welcomeFilesDelimited, ";"));

    appRoot = config.getInitParameter(HttpActivator.WEBAPP_CONTEXTROOT);
    if (appRoot == null || appRoot.equals("/"))
      appRoot = "";

    bundleUriNamespace = config.getInitParameter(HttpActivator.BUNDLE_URI_NAMESPACE);
    if (bundleUriNamespace == null || bundleUriNamespace.equals("/"))
      bundleUriNamespace = "";
  }

}
