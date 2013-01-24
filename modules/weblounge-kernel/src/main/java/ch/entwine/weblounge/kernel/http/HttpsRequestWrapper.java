/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.kernel.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This wrapper is used to pretend the <code>HTTPS</code> scheme.
 */
public class HttpsRequestWrapper extends HttpServletRequestWrapper {

  /** The original URL */
  private String originalURL;

  /**
   * Wraps the request to enforce https as the request scheme.
   * 
   * @param request
   *          the original request
   */
  public HttpsRequestWrapper(HttpServletRequest request) {
    super(request);
    StringBuffer url = super.getRequestURL();
    int protocolIndex = url.indexOf("://");
    originalURL = "https" + url.substring(protocolIndex);
    originalURL = url.toString();
  }

  /**
   * Overwrites the original request's scheme to return <code>https</code>.
   * 
   * @return always returns <code>https</code>
   */
  public String getScheme() {
    return "https";
  }

  /**
   * Indicates that this is a secured request.
   * 
   * @return always returns <code>true</code>
   */
  public boolean isSecure() {
    return true;
  }

  /**
   * Returns the request url with a "fixed" scheme of <code>https</code>.
   * 
   * @return the original url featuring the <code>https</code> scheme
   */
  public StringBuffer getRequestURL() {
    return new StringBuffer(this.originalURL);
  }
}
