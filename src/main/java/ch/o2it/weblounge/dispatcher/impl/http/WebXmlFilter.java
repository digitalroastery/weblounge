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

public class WebXmlFilter {
  String filterName;

  public String getFilterName() {
    return filterName;
  }

  Class<?> filterClass;
  String loadOnStartup;
  Dictionary<String, String> initParams = new Hashtable<String, String>();
  ArrayList<String> filterMappings = new ArrayList<String>();

  public Dictionary<String, String> getInitParams() {
    return initParams;
  }

  public WebXmlFilter addInitParam(String paramName, String paramValue) {
    if (paramName != null && paramValue != null)
      initParams.put(paramName, paramValue);
    return this;
  }

  public ArrayList<String> getFilterMappings() {
    return filterMappings;
  }

  public Class<?> getFilterClass() {
    return filterClass;
  }

  @SuppressWarnings("unused")
  private WebXmlFilter() {
  }

  public WebXmlFilter(String filterName, Class<?> filterClass) {
    this.filterName = filterName;
    this.filterClass = filterClass;
  }

  public WebXmlFilter addMapping(String mapping) {

    if (mapping == null)
      return null;

    if (mapping != null && !mapping.startsWith("/"))
      mapping = "/" + mapping;

    filterMappings.add(mapping);
    return this;
  }
}
