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

import ch.o2it.weblounge.common.ConfigurationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

/**
 * URLMapping
 * 
 * @version $Revision: 1090 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */
public class URLMapping {

  /** the urls that should be handled by this servlet */
  private String match;

  /** flag to indicate postfix matches against the target url */
  private boolean pathMapping;

  /** flag to indicate prefix matches against the target url */
  private boolean extensionMapping;

  /**
   * Creates a new <code>URLMapping</code> for the given mapping string.
   * 
   * @param mapping
   *          the path mapping string
   * @throws ConfigurationException
   *           if the mapping is invalid
   */
  public URLMapping(String mapping) throws ConfigurationException {
    match = mapping;
    if (match == null)
      throw new ConfigurationException("The URL mapping must be specified");
    if (match.length() == 0)
      throw new ConfigurationException("The URL mapping must not be empty");
    if (match.equals("*"))
      throw new ConfigurationException("All wildcard mappings (*) are not supported");
    extensionMapping = match.startsWith("*");
    pathMapping = match.endsWith("*");
    if (extensionMapping && pathMapping)
      throw new ConfigurationException("Combined pre- and postfix mappings (*url*) are not supported");
    if (extensionMapping)
      match = match.substring(1);
    if (pathMapping) {
      match = match.substring(0, match.length() - 1);
    }
    if (match.indexOf('*') != -1)
      throw new ConfigurationException("Internal wildcard mappings (/url*url) are not supported");
    if (!extensionMapping && !match.startsWith("/"))
      throw new ConfigurationException("The mapping must start with a /");
    if (pathMapping && !match.endsWith("/"))
      throw new ConfigurationException("The path mapping must end with a /*");
  }

  /**
   * Extracts the <code>URLPathElements</code> from the request url.
   * 
   * @param url
   *          the url for the new request
   * @param req
   *          the original request
   * @param servletRelative
   *          if true, the url is interpreted relative to the requesting servlet
   * @return the <code>URLPathElements</code> for the new request
   */
  @SuppressWarnings("deprecation")
  public URLPathElements getPaths(String url, HttpServletRequest req,
      boolean servletRelative) {
    assert url != null;

    URLPathElements paths = new URLPathElements();

    // extract the query string
    assert url.indexOf('?') != 0;
    paths.queryString = req.getQueryString();
    int index = url.indexOf('?');
    if (index != -1) {
      if (index < url.length() - 1) {
        String query = url.substring(index + 1);
        if (paths.queryString == null || paths.queryString.length() == 0)
          paths.queryString = query;
        else
          paths.queryString += "&" + query;
        // !TODO: reimplement
        paths.params = HttpUtils.parseQueryString(query);
      }
      if (index > 0)
        url = url.substring(0, index);
    }

    // extract the context path
    if (servletRelative) {
      paths.contextPath = req.getContextPath() + req.getServletPath();
      if (paths.contextPath.endsWith("/"))
        paths.contextPath = paths.contextPath.substring(0, paths.contextPath.length() - 1);
    } else
      paths.contextPath = req.getContextPath();

    // the remaining url is the request uri
    paths.requestUri = paths.contextPath + url;

    // extract the servlet path and the path info
    assert url.startsWith("/");
    if (pathMapping) {
      assert url.startsWith(match);
      paths.servletPath = match;
      if (url.length() > match.length())
        paths.pathInfo = url.substring(match.length());
      else
        paths.pathInfo = null;
    } else if (extensionMapping) {
      assert url.endsWith(match);
      paths.servletPath = url;
      paths.pathInfo = null;
    } else {
      assert url.equals(match);
      paths.servletPath = match;
      paths.pathInfo = null;
    }

    return paths;
  }

  /**
   * Checks whether a given URL should matches the mapping.
   * 
   * @param url
   *          the url to check
   * @return <code>true</code> iff this given url matches the mapping
   */
  public boolean matchURL(String url) {
    if (url == null)
      return false;
    int index = url.indexOf('?');
    if (index > 1)
      url = url.substring(0, index);
    return ((extensionMapping && url.endsWith(match)) || (pathMapping && url.startsWith(match)) || url.equals(match));
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof URLMapping))
      return false;
    URLMapping mapping = (URLMapping) obj;
    return (mapping.extensionMapping == extensionMapping) && (mapping.pathMapping == pathMapping) && mapping.match.equals(match);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (pathMapping ? "path" : extensionMapping ? "extension" : "absolute") + " mapping [" + (extensionMapping ? "*" : "") + match + (pathMapping ? "*" : "") + "]";
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hash = match.hashCode();
    return pathMapping ? hash << 1 : extensionMapping ? hash << 2 : hash;
  }
}
