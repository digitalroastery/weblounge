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

import ch.o2it.weblounge.common.request.WebloungeRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility implementation to deal with <code>HttpRequest</code> objects.
 */
public final class RequestSupport {

  /**
   * RequestSupport is a static class and therefore has no constructor.
   */
  private RequestSupport() {
  }

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
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string.
   * 
   * @return <code>true</code> if the parameter is available
   */
  public static boolean parameterExists(WebloungeRequest request,
      String parameter) {
    String p = request.getParameter(parameter);
    return (p != null && !p.equals(""));
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, <code>null</code> otherwise.
   * <p>
   * Note that this method includes the check for <tt>hidden</tt> parameters.
   * 
   * @return the parameter value or <code>null</code> if the parameter is not
   *         available
   */
  public static String getParameter(WebloungeRequest request, String parameter) {
    String p = request.getParameter(parameter);
    if (p != null) {
      try {
        p = URLDecoder.decode(p.trim(), "UTF-8");
      } catch (UnsupportedEncodingException e) {
      }
    }
    return (p != null && !p.trim().equals("")) ? p : null;
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, <code>defaultValue</code> otherwise.
   * <p>
   * Note that this method includes the check for <tt>hidden</tt> parameters.
   * 
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   */
  public static String getParameter(WebloungeRequest request, String parameter,
      String defaultValue) {
    String p = getParameter(request, parameter);
    return (p != null) ? p : defaultValue;
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, <code>null</code> otherwise.
   * <p>
   * Note that this method includes the check for <tt>hidden</tt> parameters.
   * 
   * @return <code>null</code> if the parameter is not available
   * @throws IllegalStateException
   *           if the required parameter was not found
   */
  public static String getRequiredParameter(WebloungeRequest request,
      String parameter) throws IllegalStateException {
    String p = getParameter(request, parameter);
    if (p == null) {
      throw new IllegalStateException(parameter);
    }
    return p;
  }

  /**
   * This method returns without noise if one of the specified parameters can be
   * found in the request and is not equal to the empty string. Otherwise, a
   * <code>IllegalArgumentException</code> is thrown.
   * 
   * @param request
   *          the request
   * @param parameters
   *          the parameter list
   * @throws IllegalStateException
   *           if none of the given argument can be found in the request
   */
  public static void requireAny(WebloungeRequest request, String[] parameters)
      throws IllegalStateException {
    if (parameters == null)
      return;
    for (int i = 0; i < parameters.length; i++) {
      if (getParameter(request, parameters[i]) != null)
        return;
    }
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    for (int i = 0; i < parameters.length; i++) {
      buf.append(parameters[i]);
      if (i < parameters.length - 1)
        buf.append(" | ");
    }
    buf.append("]");
    throw new IllegalStateException(buf.toString());
  }

}