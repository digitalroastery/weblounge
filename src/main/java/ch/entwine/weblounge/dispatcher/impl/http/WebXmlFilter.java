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
import java.util.Map;

/**
 * This class models filter information that usually goes into a web
 * application's <code>web.xml</code>.
 */
public class WebXmlFilter {

  private String filterName;
  private Class<?> filterClass;
  private Map<String, String> initParams = new HashMap<String, String>();
  private ArrayList<String> filterMappings = new ArrayList<String>();

  /**
   * Creates a new filter.
   * 
   * @param filterName
   *          the filter name
   * @param filterClass
   *          the filter implementation
   */
  public WebXmlFilter(String filterName, Class<?> filterClass) {
    this.filterName = filterName;
    this.filterClass = filterClass;
  }

  /**
   * Returns the filter name
   * 
   * @return the filter name
   */
  public String getFilterName() {
    return filterName;
  }

  /**
   * Returns the init parameters.
   * 
   * @return the parameters
   */
  public Map<String, String> getInitParams() {
    return initParams;
  }

  /**
   * Adds an init parameter.
   * 
   * @param paramName
   *          the parameter name
   * @param paramValue
   *          the parameter value
   * @return the filter
   */
  public WebXmlFilter addInitParam(String paramName, String paramValue) {
    if (paramName != null && paramValue != null)
      initParams.put(paramName, paramValue);
    return this;
  }

  /**
   * Returns the list of filter mappings.
   * 
   * @return the mappings
   */
  public ArrayList<String> getFilterMappings() {
    return filterMappings;
  }

  /**
   * Returns the filter class.
   * 
   * @return the class
   */
  public Class<?> getFilterClass() {
    return filterClass;
  }

  /**
   * Adds a filter mapping.
   * 
   * @param mapping
   *          the mapping
   * @return the filter
   */
  public WebXmlFilter addMapping(String mapping) {
    if (mapping == null)
      return null;
    if (!mapping.startsWith("/"))
      mapping = "/" + mapping;
    filterMappings.add(mapping);
    return this;
  }

}
