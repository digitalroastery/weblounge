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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class WelcomeFileFilter implements javax.servlet.Filter {

  public static final String WELCOME_FILES = "weblounge.http.WELCOME_FILES";

  List<String> welcomeFileList = new ArrayList<String>();
  String appRoot = null;
  String bundleUriNamespace = null;

  public void destroy() {
  }

  public void doFilter(ServletRequest req, ServletResponse res,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest hreq = (HttpServletRequest) req;
    String uri = hreq.getRequestURI();
    String contextPath = hreq.getContextPath();
    String servletPath = hreq.getServletPath();
    System.out.println("WelcomeFileFilter.URI" + uri);
    System.out.println("WelcomeFileFilter.contextPath" + contextPath);
    System.out.println("WelcomeFileFilter.servletPath" + servletPath);

    if ((appRoot + bundleUriNamespace).equals(uri)) {
      System.out.println("is welcomeURL");
    }

    chain.doFilter(req, res);

  }

  public void init(FilterConfig config) throws ServletException {

    String welcomeFilesDelimited = config.getInitParameter(WELCOME_FILES);
    if (welcomeFilesDelimited != null)
      welcomeFileList = Arrays.asList(StringUtils.split(welcomeFilesDelimited, ";"));
    appRoot = config.getInitParameter(HttpActivator.WEBAPP_CONTEXTROOT);
    if (appRoot == null || appRoot.equals("/"))
      appRoot = "";

    bundleUriNamespace = config.getInitParameter(HttpActivator.BUNDLE_URI_NAMESPACE);
    if (bundleUriNamespace == null || bundleUriNamespace.equals("/"))
      bundleUriNamespace = "";

    System.out.println("WelcomeFileFilter.init" + welcomeFilesDelimited);

  }

}
