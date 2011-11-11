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

package ch.entwine.weblounge.kernel.http;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.Servlet;

/**
 * Defines a servlet as part of the <code>WebXml</code>.
 */
public class WebXmlServlet {

  /** The servlet name */
  private String servletName = null;
  
  /** Servlet implementation */
  private Class<? extends Servlet> servletClass = null;

  /** Load this servlet on startup? */
  private String loadOnStartup = null;
  
  /** The init parameters */
  private Dictionary<String, String> initParams = new Hashtable<String, String>();
  
  /** The servlet mappings */
  private ArrayList<String> servletMappings = new ArrayList<String>();
  
  /** The filter names as defined for this servlet */
  private ArrayList<String> filterNames = new ArrayList<String>();

  /**
   * Creates a new servlet with the given name and implementation.
   * 
   * @param servletName
   *          the servlet name
   * @param servlet
   *          the servlet implementation
   */
  public WebXmlServlet(String servletName, Class<? extends Servlet> servlet) {
    this.servletName = servletName;
    this.servletClass = servlet;
  }

  /**
   * Returns the servlet name.
   * 
   * @return the name
   */
  public String getName() {
    return servletName;
  }

  /**
   * Returns the filter names
   * 
   * @return the filter names
   */
  public List<String> getFilterNames() {
    return filterNames;
  }

  /**
   * Returns the init parameters.
   * 
   * @return the init parameters
   */
  public Dictionary<String, String> getInitParams() {
    return initParams;
  }

  /**
   * Returns <code>true</code> if the servlet should be loaded at startup.
   * 
   * @return <code>true</code> to load the servlet at startup
   */
  public String getLoadOnStartup() {
    return loadOnStartup;
  }

  /**
   * Specifies whether this servlet should be loaded at startup time.
   * 
   * @param loadOnStartup
   *          <code>true</code> to load at startup
   * @return the servlet
   */
  public WebXmlServlet setLoadOnStartup(String loadOnStartup) {
    if (loadOnStartup != null)
      initParams.put("load-on-startup", loadOnStartup);
    this.loadOnStartup = loadOnStartup;
    return this;
  }

  /**
   * Adds an init parameter to the servlet.
   * 
   * @param paramName
   *          the parameter name
   * @param paramValue
   *          the parameter value
   * @return the servlet
   */
  public WebXmlServlet addInitParam(String paramName, String paramValue) {
    if (paramName != null && paramValue != null)
      initParams.put(paramName, paramValue);
    return this;
  }

  /**
   * Returns the servlet mappings.
   * 
   * @return the servlet mappings
   */
  public List<String> getServletMappings() {
    return servletMappings;
  }

  /**
   * Returns the servlet implementation.
   * 
   * @return the servlet implementation
   */
  public Class<? extends Servlet> getServletClass() {
    return servletClass;
  }

  /**
   * Adds a servlet mapping.
   * 
   * @param mapping
   *          the servlet mapping
   * @return the servlet
   */
  public WebXmlServlet addMapping(String mapping) {
    if (mapping == null)
      return null;
    if (!mapping.startsWith("/"))
      mapping = "/" + mapping;
    servletMappings.add(mapping);
    return this;
  }

  /**
   * Adds a filter to this servlet.
   * 
   * @param filterName
   *          name of the filter
   * @return the servlet
   */
  public WebXmlServlet addFilter(String filterName) {
    filterNames.add(filterName);
    return this;
  }

}
