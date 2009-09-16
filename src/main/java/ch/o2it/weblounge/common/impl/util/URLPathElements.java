/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util;

import java.util.Map;

/**
 * URLPathElements
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */
public class URLPathElements {
  /** the context path */
  String contextPath;

  /** the servlet path */
  String servletPath;

  /** the requets uri */
  String requestUri;

  /** the path info */
  String pathInfo;

  /** the query string */
  String queryString;

  /** the request parameters */
  Map<String, Object> params;

  /**
   * @return Returns the contextPath.
   */
  public String getContextPath() {
    return contextPath;
  }

  /**
   * @return Returns the params.
   */
  public Map<String, Object> getParams() {
    return params;
  }

  /**
   * @return Returns the pathInfo.
   */
  public String getPathInfo() {
    return pathInfo;
  }

  /**
   * @return Returns the queryString.
   */
  public String getQueryString() {
    return queryString;
  }

  /**
   * @return Returns the requestUri.
   */
  public String getRequestUri() {
    return requestUri;
  }

  /**
   * @return Returns the servletPath.
   */
  public String getServletPath() {
    return servletPath;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[context=" + contextPath + ", servlet=" + servletPath + ", path=" + pathInfo + ", query=" + queryString + "]";
  }
}
