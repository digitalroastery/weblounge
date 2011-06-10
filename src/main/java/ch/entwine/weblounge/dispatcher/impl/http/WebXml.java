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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.Servlet;

/**
 * This class represents an in-memory representation of the classic
 * <code>web.xml</code> found in web applications slated for deployment in a
 * servlet container.
 */
public class WebXml {

  /** The servlets */
  private TreeMap<String, WebXmlServlet> webXmlServlets = new TreeMap<String, WebXmlServlet>();

  /** The servlet filters */
  private TreeMap<String, WebXmlFilter> webXmlFilters = new TreeMap<String, WebXmlFilter>();

  /** The context parameters */
  private Map<String, String> contextParams = new HashMap<String, String>();

  /** The list of welcome files */
  private ArrayList<String> webXmlWelcomeFiles = new ArrayList<String>();

  /**
   * Returns the filters.
   * 
   * @return the filters
   */
  public Map<String, WebXmlFilter> getFilters() {
    return webXmlFilters;
  }

  /**
   * Returns the servlets.
   * 
   * @return the servlets
   */
  public Map<String, WebXmlServlet> getServlets() {
    return webXmlServlets;
  }

  /**
   * Returns the context parameters.
   * 
   * @return the context parameters
   */
  public Map<String, String> getContextParams() {
    return contextParams;
  }

  /**
   * Returns the welcome files.
   * 
   * @return the welcome files
   */
  public List<String> getWelcomeFiles() {
    return webXmlWelcomeFiles;
  }

  /**
   * Returns <code>true</code> if welcome files are defined.
   * 
   * @return <code>true</code> if welcome files are defined
   */
  public boolean containsWelcomeFiles() {
    return !webXmlWelcomeFiles.isEmpty();
  }

  /**
   * Adds a welcome file.
   * 
   * @param file
   *          the file name
   * @return the web xml
   */
  public WebXml addWelcomeFile(String file) {
    webXmlWelcomeFiles.add(file);
    return this;
  }

  /**
   * Adds a servlet with the given name and implementation.
   * 
   * @param name
   *          the servlet name
   * @param servlet
   *          the servlet class
   * @return the new servlet
   */
  public WebXmlServlet addServlet(String name, Class<? extends Servlet> servlet) {
    if (servlet == null)
      return null;
    if (name == null)
      name = servlet.getName();
    WebXmlServlet webXmlServlet = new WebXmlServlet(name, servlet);
    webXmlServlets.put(name, webXmlServlet);
    return webXmlServlet;
  }

  /**
   * Adds a servlet with the servlet class name as the servlet name.
   * 
   * @param servlet
   *          the servlet class
   * @return the new servlet
   */
  public WebXmlServlet addServlet(Class<? extends Servlet> servlet) {
    if (servlet == null)
      return null;
    String name = servlet.getName();
    WebXmlServlet webXmlServlet = new WebXmlServlet(name, servlet);
    webXmlServlets.put(name, webXmlServlet);
    return webXmlServlet;
  }

  /**
   * Adds a servlet filter with the given name and implementation.
   * 
   * @param name
   *          the filter name
   * @param filter
   *          the filter class
   * @return the new filter
   */
  public WebXmlFilter addFilter(String name, Class<? extends Filter> filter) {
    if (filter == null)
      return null;
    if (name == null)
      name = filter.getName();
    WebXmlFilter webXmlFilter = new WebXmlFilter(name, filter);
    webXmlFilters.put(name, webXmlFilter);
    return webXmlFilter;
  }

  /**
   * Adds a servlet filter with the filter class name as the filter name.
   * 
   * @param filter
   *          the filter class
   * @return the new filter
   */
  public WebXmlFilter addFilter(Class<?> filter) {
    if (filter == null)
      return null;
    String name = filter.getName();
    WebXmlFilter webXmlFilter = new WebXmlFilter(name, filter);
    webXmlFilters.put(name, webXmlFilter);
    return webXmlFilter;
  }

  /**
   * Adds a context parameter.
   * 
   * @param paramName
   *          name of the context parameter
   * @param paramValue
   *          parameter value
   * @return the web xml
   */
  public WebXml addContextParam(String paramName, String paramValue) {
    if (paramName == null || paramValue == null)
      return this;
    contextParams.put(paramName, paramValue);
    return this;
  }

  /**
   * Returns the value of the given context parameter or <code>null</code> if no
   * such parameter was specified.
   * 
   * @param paramName
   *          the parameter name
   * @return the parameter value
   */
  public String getContextParam(String paramName) {
    return contextParams.get(paramName);
  }

  /**
   * Returns the value of the given context parameter or
   * <code>defaultValue</code> if no such parameter was specified.
   * 
   * @param paramName
   *          the parameter name
   * @param defaultValue
   *          the default value
   * @return the parameter value
   */
  public String getContextParam(String paramName, String defaultValue) {
    String value = contextParams.get(paramName);
    return (value != null) ? value : defaultValue;
  }

  /**
   * Returns <code>true</code> if a context parameter with the given name
   * exists.
   * 
   * @param paramName
   *          the parameter name
   * @return <code>true</code> if the parameter is defined
   */
  public boolean containsContextParam(String paramName) {
    return contextParams.containsKey(paramName);
  }

  /**
   * Returns <code>true</code> if a filter with the given name exists.
   * 
   * @param filterName
   *          the filter name
   * @return <code>true</code> if the filter is defined
   */
  public boolean containsFilter(String filterName) {
    return webXmlFilters.containsKey(filterName);
  }

  /**
   * Returns <code>true</code> if a servlet with the given name exists.
   * 
   * @param servletName
   *          the servlet name
   * @return <code>true</code> if the servlet is defined
   */
  public boolean containsServlet(String servletName) {
    return webXmlServlets.containsKey(servletName);
  }

}
