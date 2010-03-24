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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;

public class WebXmlServlet {
  String servletName;
  Class<? extends Servlet> servletClass;
  String loadOnStartup;
  Dictionary<String, String> initParams = new Hashtable<String, String>();
  ArrayList<String> servletMappings = new ArrayList<String>();
  ArrayList<String> filterNames = new ArrayList<String>();

  public ArrayList<String> getFilterNames() {
    return filterNames;
  }

  public Dictionary<String, String> getInitParams() {
    return initParams;
  }

  public String getLoadOnStartup() {
    return initParams.get("load-on-startup");
  }

  public WebXmlServlet setLoadOnStartup(String loadOnStartup) {
    if (loadOnStartup != null)
      initParams.put("load-on-startup", loadOnStartup);
    return this;
  }

  public WebXmlServlet addInitParam(String paramName, String paramValue) {
    if (paramName != null && paramValue != null)
      initParams.put(paramName, paramValue);
    return this;
  }

  public ArrayList<String> getServletMappings() {
    return servletMappings;
  }

  public Class<? extends Servlet> getServletClass() {
    return servletClass;
  }

  @SuppressWarnings("unused")
  private WebXmlServlet() {
  }

  public WebXmlServlet(String servletName, Class<? extends Servlet> servlet) {
    this.servletName = servletName;
    this.servletClass = servlet;
  }

  public WebXmlServlet addMapping(String mapping) {

    if (mapping == null)
      return null;

    if (mapping != null && !mapping.startsWith("/"))
      mapping = "/" + mapping;

    servletMappings.add(mapping);
    return this;
  }

  public WebXmlServlet addFilter(String filterName) {
    filterNames.add(filterName);
    return this;

  }
}
