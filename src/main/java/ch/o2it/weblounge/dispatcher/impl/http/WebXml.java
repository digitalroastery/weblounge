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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.Servlet;

public class WebXml {

  private TreeMap<String, WebXmlServlet> webXmlServlets = new TreeMap<String, WebXmlServlet>();
  private TreeMap<String, WebXmlFilter> webXmlFilters = new TreeMap<String, WebXmlFilter>();

  private ArrayList<WebXmlContextParam> contextParams = new ArrayList<WebXmlContextParam>();
  private ArrayList<String> webXmlWelcomeFiles = new ArrayList<String>();

  public TreeMap<String, WebXmlFilter> getWebXmlFilters() {
    return webXmlFilters;
  }

  public TreeMap<String, WebXmlServlet> getWebXmlServlets() {
    return webXmlServlets;
  }

  public ArrayList<WebXmlContextParam> getContextParams() {
    return contextParams;
  }

  public ArrayList<String> getWebXmlWelcomeFiles() {
    return webXmlWelcomeFiles;
  }

  public Dictionary<String, Object> getWebXmlWelcomeFilesAsDictionary() {

    Dictionary<String, Object> webXmlFiles = new Hashtable<String, Object>();
    if (webXmlWelcomeFiles != null && !webXmlWelcomeFiles.isEmpty())
      webXmlFiles.put(WelcomeFileFilterServlet.WELCOME_FILES, StringUtils.join(webXmlWelcomeFiles, ";"));
    return webXmlFiles;
  }

  public boolean containsWelcomeFiles() {
    return !webXmlWelcomeFiles.isEmpty();
  }

  public WebXml addWelcomeFile(String file) {
    webXmlWelcomeFiles.add(file);
    return this;
  }

  public WebXmlServlet addServlet(String name, Class<? extends Servlet> servlet) {
    if (servlet == null)
      return null;
    if (name == null)
      name = servlet.getName();

    WebXmlServlet webXmlServlet = new WebXmlServlet(name, servlet);

    webXmlServlets.put(name, webXmlServlet);
    // we want to return an object that allows us to call addMapping to add one
    // or more mappings
    return webXmlServlet;
  }

  public WebXmlServlet addServlet(Class<? extends Servlet> servlet) {
    if (servlet == null)
      return null;
    String name = servlet.getName(); // class name

    WebXmlServlet webXmlServlet = new WebXmlServlet(name, servlet);

    webXmlServlets.put(name, webXmlServlet);
    // we want to return an object that allows us to call addMapping to add one
    // or more mappings
    return webXmlServlet;
  }

  public WebXmlFilter addFilter(String name, Class<? extends Filter> filter) {
    if (filter == null)
      return null;
    if (name == null)
      name = filter.getName();

    WebXmlFilter webXmlFilter = new WebXmlFilter(name, filter);

    webXmlFilters.put(name, webXmlFilter);
    // we want to return an object that allows us to call addMapping to add one
    // or more mappings
    return webXmlFilter;
  }

  public WebXmlFilter addFilter(Class<?> filter) {
    if (filter == null)
      return null;
    String name = filter.getName(); // class name

    WebXmlFilter webXmlFilter = new WebXmlFilter(name, filter);

    webXmlFilters.put(name, webXmlFilter);
    // we want to return an object that allows us to call addMapping to add one
    // or more mappings
    return webXmlFilter;
  }

  public WebXml addContextParam(String paramName, String paramValue) {
    if (paramName == null || paramValue == null)
      return this;

    WebXmlContextParam param = new WebXmlContextParam(paramName, paramValue);
    contextParams.add(param);
    return this;
  }

  public boolean containsContextParam(String paramName) {
    for (WebXmlContextParam param : contextParams) {
      if (param.paramName.equals(paramName))
        return true;
    }
    return false;
  }

  public boolean containsFilter(String filterName) {
    return webXmlFilters.containsKey(filterName);
  }

  public boolean containsServlet(String servletName) {
    return webXmlServlets.containsKey(servletName);
  }
}
