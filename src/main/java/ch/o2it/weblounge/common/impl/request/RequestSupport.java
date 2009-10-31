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

package ch.o2it.weblounge.common.impl.request;

import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.request.WebloungeRequest;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility implementation to deal with <code>HttpRequest</code> objects.
 */
public final class RequestSupport {

  /**
   * RequestSupport is a static class and therefore has no constructor.
   */
  private RequestSupport() { }

  /**
   * Dumps the request headers to <code>System.out</code>.
   * 
   * @param request
   *          the request
   */
  public static void dumpHeaders(HttpServletRequest request) {
    Enumeration<?> hi = request.getHeaderNames();
    System.out.println("Request headers:");
    while (hi.hasMoreElements()) {
      String header = (String) hi.nextElement();
      String value = request.getHeader(header);
      System.out.println("\t" + header + ": " + value);
    }
  }

  /**
   * Returns a string representation of the request parameters.
   * 
   * @param request
   *          the request
   * @return the request parameters
   */
  public static String getParameters(WebloungeRequest request) {
    StringBuffer params = new StringBuffer();
    Enumeration<?> e = request.getParameterNames();
    while (e.hasMoreElements()) {
      String param = (String) e.nextElement();
      String value = request.getParameter(param);
      if (params.length() > 0) {
        params.append(";");
      }
      params.append(param);
      params.append("=");
      params.append(value);
    }
    return (params.length() > 0) ? "[" + params.toString() + "]" : "[-]";
  }

  /**
   * Returns the version for the given version identifier. Available versions
   * are:
   * <ul>
   * <li>{@link #LIVE}</li>
   * <li>{@link #WORK}</li>
   * <li>{@link #ORIGINAL}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static long getVersion(String version) {
    if (version.equals("index")) {
      return Page.LIVE;
    } else if (version.equals("work")) {
      return Page.WORK;
    } else if (version.equals("original")) {
      return Page.ORIGINAL;
    } else {
      try {
        return Long.parseLong(version);
      } catch (NumberFormatException e) {
        return -1;
      }
    }
  }

}