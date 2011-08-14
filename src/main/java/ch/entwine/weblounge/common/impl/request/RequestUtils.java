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

package ch.entwine.weblounge.common.impl.request;

import ch.entwine.weblounge.common.impl.site.ActionSupport;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility implementation to deal with <code>HttpRequest</code> objects.
 */
public final class RequestUtils {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ActionSupport.class);

  /**
   * RequestSupport is a static class and therefore has no constructor.
   */
  private RequestUtils() {
  }

  /**
   * Returns a string representation of the request headers.
   * 
   * @param request
   *          the request
   * @return the headers
   */
  public static String dumpHeaders(WebloungeRequest request) {
    Enumeration<?> hn = request.getHeaderNames();
    String headers = "Request headers:";
    while (hn.hasMoreElements()) {
      String header = (String) hn.nextElement();
      String value = request.getHeader(header);
      headers = headers.concat(header).concat(": ").concat(value);
    }
    return headers;
  }

  /**
   * Returns a string representation of the request parameters.
   * 
   * @param request
   *          the request
   * @return the request parameters
   */
  public static String dumpParameters(WebloungeRequest request) {
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
    return (params.length() > 0) ? params.toString() : "-";
  }

  /**
   * Returns the extension part of the requested url. For example, if an action
   * is mounted to <code>/test</code> and the url is <code>/test/a</code> then
   * this method will return <code>/a</code>. For the mount point itself, the
   * method will return <code>/</code>.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action
   * @return the path extension relative to the action's mount point
   */
  public static String getRequestedUrlExtension(WebloungeRequest request,
      Action action) {
    if (request == null)
      throw new IllegalStateException("Request has not started");
    return request.getRequestedUrl().getPath().substring(action.getPath().length());
  }

  /**
   * Returns a list of parameters found in the path extension requested url
   * (split by '/'). For example, if an action is mounted to <code>/test</code>
   * and the url is <code>/test/a/b/c</code> then this method will return a list
   * with the values 'a', 'b' and 'c'. For the mount point itself, the method
   * will return an empty list.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action
   * @return a list with parameters found in the path extension of the action's
   *         mount point
   */
  public static List<String> getRequestedUrlParams(WebloungeRequest request,
      Action action) {
    // load parameter values from url extension
    List<String> urlparams = new ArrayList<String>();
    String path = request.getRequestedUrl().getPath();
    String actionMountpoint = action.getPath();
    String[] params = path.substring(actionMountpoint.length()).split("/");

    for (int i = 0; i < params.length; i++) {
      urlparams.add(params[i]);
    }

    return urlparams;
  }

  /**
   * Returns the extension part of the target url. For example, if an action is
   * mounted to <code>/test</code> and the url is <code>/test/a</code> then this
   * method will return <code>/a</code>. For the mount point itself, the method
   * will return <code>/</code>.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action
   * @return the path extension relative to the action's mount point
   */
  public static String getUrlExtension(WebloungeRequest request, Action action) {
    if (request == null)
      throw new IllegalStateException("Request has not started");
    return request.getUrl().getPath().substring(action.getPath().length());
  }

  /**
   * Returns a list of parameters found in the extension part of the targeted
   * url (split by '/'). For example, if an action is mounted to
   * <code>/test</code> and the url is <code>/test/a/b/c</code> then this method
   * will return a list with the values 'a', 'b' and 'c'. For the mount point
   * itself, the method will return an empty list.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action
   * @return a list with parameters found in the path extension of the action's
   *         mount point
   */
  public static List<String> getUrlParams(WebloungeRequest request,
      Action action) {
    // load parameter values from url extension
    List<String> urlparams = new ArrayList<String>();
    String path = request.getUrl().getPath();
    String actionMountpoint = action.getPath();
    String[] params = path.substring(actionMountpoint.length()).split("/");

    for (int i = 0; i < params.length; i++) {
      urlparams.add(params[i]);
    }

    return urlparams;
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, <code>null</code> otherwise.
   * <p>
   * Note that this method includes the check for <tt>hidden</tt> parameters.
   * 
   * @param request
   *          the weblounge request
   * @param parameter
   *          the parameter name
   * @return the parameter value or <code>null</code> if the parameter is not
   *         available
   */
  public static String getParameter(WebloungeRequest request, String parameter) {
    String p = request.getParameter(parameter);
    if (p != null) {
      try {
        p = URLDecoder.decode(p.trim(), "utf-8");
      } catch (UnsupportedEncodingException e) {
      }
    }
    return StringUtils.trimToNull(p);
  }

  /**
   * Checks a parameter <code>parameter</code> is present in the url of the
   * request at the indicated position, starting with the action's mountpoint.
   * In this case, the parameter itself is returned, <code>null</code>
   * otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param index
   *          the parameter index
   * @return the parameter value or <code>null</code> if the parameter is not
   *         available
   */
  public static String getParameter(WebloungeRequest request, Action action,
      int index) {
    List<String> urlparams = getRequestedUrlParams(request, action);

    // Did we extract as many parameters as we should?
    if (index >= urlparams.size())
      return null;

    String p = urlparams.get(index);
    try {
      p = URLDecoder.decode(p.trim(), "utf-8");
    } catch (UnsupportedEncodingException e) {
      logger.error("Encoding 'utf-8' is not supported on this platform");
    }
    return StringUtils.trimToNull(p);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, <code>defaultValue</code> otherwise.
   * <p>
   * Note that this method includes the check for <tt>hidden</tt> parameters.
   * 
   * @param request
   *          the weblounge request
   * @param parameter
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   */
  public static String getParameterWithDefault(WebloungeRequest request,
      String parameter, String defaultValue) {
    String p = getParameter(request, parameter);
    return (p != null) ? p : defaultValue;
  }

  /**
   * Checks a parameter <code>parameter</code> is present in the url of the
   * request at the indicated position, starting with the action's mountpoint.
   * In this case, the parameter itself is returned, <code>defaultValue</code>
   * otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param index
   *          the parameter index
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   */
  public static String getParameterWithDefault(WebloungeRequest request,
      Action action, int index, String defaultValue) {
    String p = getParameter(request, action, index);
    return (p != null) ? p : defaultValue;
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, while a <code>IllegalStateException</code> is thrown otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param parameter
   *          the parameter name
   * @return the parameter
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static String getRequiredParameter(WebloungeRequest request,
      String parameter) throws IllegalStateException {
    String p = getParameter(request, parameter);
    if (p == null)
      throw new IllegalStateException("Request parameter '" + parameter + "' is mandatory");
    return p;
  }

  /**
   * Checks a parameter <code>parameter</code> is present in the url of the
   * request at the indicated position, starting with the action's mountpoint.
   * In this case, the parameter itself is returned, while a
   * <code>IllegalStateException</code> is thrown otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param parameter
   *          the parameter index
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static String getRequiredParameter(WebloungeRequest request,
      Action action, int parameter) {
    String p = getParameter(request, action, parameter);
    if (p == null)
      throw new IllegalStateException("Url parameter at " + parameter + " is mandatory");
    return p;
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, <code>null</code> otherwise.
   * <p>
   * The parameter is decoded using the specified <code>encoding</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameter
   *          the parameter name
   * @param decoding
   *          the encoding parameter, e. g. <code>utf-8</code>
   * @return the decoded parameter value or <code>null</code> if the parameter
   *         is not available
   */
  public static String getParameterWithDecoding(WebloungeRequest request,
      String parameter, String decoding) {
    String p = request.getParameter(parameter);
    if (p != null) {
      p = p.trim();
      if ("application/x-www-form-urlencoded".equalsIgnoreCase(request.getContentType())) {
        try {
          p = URLDecoder.decode(p, decoding);
        } catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException("Encoding " + decoding + " is unsupported");
        } catch (IllegalArgumentException e) {
          // Tried decoding a string with a % inside, so obviously the
          // parameter
          // was decoded already
        }
      }
    }
    return StringUtils.trimToNull(p);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string.
   * 
   * @param request
   *          the weblounge request
   * @param parameter
   *          the parameter name
   * @return <code>true</code> if the parameter is available
   */
  public static boolean parameterExists(WebloungeRequest request,
      String parameter) {
    String p = getParameter(request, parameter);
    return StringUtils.trimToNull(p) != null;
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string.
   * 
   * @param request
   *          the weblounge request
   * @param parameter
   *          the parameter index
   * @return <code>true</code> if the parameter is available
   */
  public static boolean parameterExists(WebloungeRequest request,
      Action action, int parameter) {
    String p = getParameter(request, action, parameter);
    return StringUtils.trimToNull(p) != null;
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>short</code>. In that case, the
   * parameter is returned as a <code>short</code>, otherwise <code>0</code> is
   * returned.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>short</code>
   */
  public static short getShortParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getShortParameter(request, parameterName, false);
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>short</code>. In that
   * case, the parameter is returned as a <code>short</code>, otherwise
   * <code>0</code> is returned.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>short</code>
   */
  public static short getShortParameter(WebloungeRequest request,
      Action action, int parameter) throws IllegalArgumentException {
    return getShortParameter(request, action, parameter, false);
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>short</code>. Should the parameter be
   * part of the request, it's value is returned as a <code>short</code> whereas
   * otherwise the <code>defaultValue</code> is returned instead.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>short</code>
   */
  public static short getShortParameterWithDefault(WebloungeRequest request,
      String parameterName, short defaultValue) throws IllegalArgumentException {
    try {
      return getShortParameter(request, parameterName, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if a parameter is present in the url at position <code>index</code>
   * and represents a valid <code>short</code>. Should the parameter be part of
   * the request, it's value is returned as a <code>short</code> whereas
   * otherwise the <code>defaultValue</code> is returned instead.
   * 
   * @param request
   *          the weblounge request
   * @param index
   *          the parameter index
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>short</code>
   */
  public static short getShortParameterWithDefault(WebloungeRequest request,
      Action action, int index, short defaultValue)
      throws IllegalArgumentException {
    try {
      return getShortParameter(request, action, index, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>short</code>. Should the parameter not
   * be part of the request, <code>defaultValue</code> is returned, otherwise
   * the parameter value is returned as a <code>short</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>short</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static short getRequiredShortParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getShortParameter(request, parameterName, true);
  }

  /**
   * Checks if a parameter is present at position <code>index</code> in the url
   * whether it represents a valid <code>short</code>. In this case, that
   * parameter is returned as a <code>short</code> value, whereas otherwise a
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the weblounge action
   * @param index
   *          the parameter index
   * @return the parameter value
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>short</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static short getRequiredShortParameter(WebloungeRequest request,
      Action action, int index) throws IllegalArgumentException,
      IllegalStateException {
    return getShortParameter(request, action, index, true);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is a valid <code>short</code>. In that case, the parameter is returned
   * as a <code>short</code>, otherwise <code>0</code> is returned.
   * <p>
   * If the parameter is required, then an {@link IllegalStateException} is
   * thrown.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>short</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static short getShortParameter(WebloungeRequest request,
      String parameterName, boolean required) throws IllegalArgumentException,
      IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, parameterName);
    else
      p = getParameter(request, parameterName);
    if (p == null)
      return 0;
    try {
      return Short.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Request parameter '" + parameterName + "' must be a short");
    }
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>short</code>. In that
   * case, the parameter is returned as a <code>short</code>, otherwise
   * <code>0</code> is returned.
   * <p>
   * If the parameter is required, and it is not part of the url, then an
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action that determines the mountpoint
   * @param index
   *          the index of the parameter in the url
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>short</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static short getShortParameter(WebloungeRequest request,
      Action action, int index, boolean required)
      throws IllegalArgumentException, IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, action, index);
    else
      p = getParameter(request, action, index);
    if (p == null)
      return 0;
    try {
      return Short.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Url parameter at index " + index + " must be a short");
    }
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>int</code>. In that case, the parameter is
   * returned as a <code>int</code>, otherwise <code>0</code> is returned.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>int</code>
   */
  public static int getIntegerParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getIntegerParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>int</code>. Should the parameter not be part
   * of the request, <code>defaultValue</code> is returned, otherwise the
   * parameter value is returned as an <code>int</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>int</code>
   */
  public static int getIntegerParameterWithDefault(WebloungeRequest request,
      String parameterName, int defaultValue) throws IllegalArgumentException {
    try {
      return getIntegerParameter(request, parameterName, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>int</code>. Should the
   * parameter be part of the request, it's value is returned as a
   * <code>int</code> whereas otherwise the <code>defaultValue</code> is
   * returned instead.
   * 
   * @param request
   *          the weblounge request
   * @param index
   *          the parameter index
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>int</code>
   */
  public static int getIntegerParameterWithDefault(WebloungeRequest request,
      Action action, int index, int defaultValue)
      throws IllegalArgumentException {
    try {
      return getIntegerParameter(request, action, index, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is a valid <code>int</code>. Should the parameter not be part of the
   * request, <code>0</code> is returned, otherwise the parameter value is
   * returned as an <code>int</code>.
   * <p>
   * If the parameter is required, then an {@link IllegalStateException} is
   * thrown.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>int</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static int getIntegerParameter(WebloungeRequest request,
      String parameterName, boolean required) throws IllegalArgumentException,
      IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, parameterName);
    else
      p = getParameter(request, parameterName);
    if (p == null)
      return 0;
    try {
      return Integer.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Request parameter '" + parameterName + "' must be an int");
    }
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>int</code>. In that
   * case, the parameter is returned as a <code>int</code>, otherwise
   * <code>0</code> is returned.
   * <p>
   * If the parameter is required, and it is not part of the url, then an
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action that determines the mountpoint
   * @param index
   *          the index of the parameter in the url
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>int</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static int getIntegerParameter(WebloungeRequest request,
      Action action, int index, boolean required)
      throws IllegalArgumentException, IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, action, index);
    else
      p = getParameter(request, action, index);
    if (p == null)
      return 0;
    try {
      return Integer.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Url parameter at index " + index + " must be a int");
    }
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>int</code>. Should the parameter not
   * be part of the request, <code>defaultValue</code> is returned, otherwise
   * the parameter value is returned as an <code>int</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>int</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static int getRequiredIntegerParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getIntegerParameter(request, parameterName, true);
  }

  /**
   * Checks if a parameter is present at position <code>index</code> in the url
   * whether it represents a valid <code>int</code>. In this case, that
   * parameter is returned as a <code>int</code> value, whereas otherwise a
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the weblounge action
   * @param index
   *          the parameter index
   * @return the parameter value
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>int</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static int getRequiredIntegerParameter(WebloungeRequest request,
      Action action, int index) throws IllegalArgumentException,
      IllegalStateException {
    return getIntegerParameter(request, action, index, true);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>long</code>. In that case, the parameter is
   * returned as a <code>long</code>, otherwise <code>0</code> is returned.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>long</code>
   */
  public static long getLongParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getLongParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>long</code>. Should the parameter not
   * be part of the request, <code>defaultValue</code> is returned, otherwise
   * the parameter value is returned as an <code>long</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>long</code>
   */
  public static long getLongParameterWithDefault(WebloungeRequest request,
      String parameterName, long defaultValue) throws IllegalArgumentException {
    try {
      return getLongParameter(request, parameterName, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if a parameter is present in the url at position <code>index</code>
   * and represents a valid <code>long</code>. Should the parameter be part of
   * the request, it's value is returned as a <code>long</code> whereas
   * otherwise the <code>defaultValue</code> is returned instead.
   * 
   * @param request
   *          the weblounge request
   * @param index
   *          the parameter index
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>long</code>
   */
  public static long getLongParameterWithDefault(WebloungeRequest request,
      Action action, int index, long defaultValue)
      throws IllegalArgumentException {
    try {
      return getLongParameter(request, action, index, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is a valid <code>long</code>. Should the parameter not be part of the
   * request, <code>0</code> is returned, otherwise the parameter value is
   * returned as an <code>long</code>.
   * <p>
   * If the parameter is required, then an {@link IllegalStateException} is
   * thrown.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>long</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static long getLongParameter(WebloungeRequest request,
      String parameterName, boolean required) throws IllegalArgumentException,
      IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, parameterName);
    else
      p = getParameter(request, parameterName);
    if (p == null)
      return 0;
    try {
      return Long.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Request parameter '" + parameterName + "' must be a long");
    }
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>long</code>. In that
   * case, the parameter is returned as a <code>long</code>, otherwise
   * <code>0L</code> is returned.
   * <p>
   * If the parameter is required, and it is not part of the url, then an
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action that determines the mountpoint
   * @param index
   *          the index of the parameter in the url
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0L</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>long</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static long getLongParameter(WebloungeRequest request, Action action,
      int index, boolean required) throws IllegalArgumentException,
      IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, action, index);
    else
      p = getParameter(request, action, index);
    if (p == null)
      return 0L;
    try {
      return Long.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Url parameter at index " + index + " must be a long");
    }
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>long</code>. Should the parameter not
   * be part of the request, <code>defaultValue</code> is returned, otherwise
   * the parameter value is returned as an <code>long</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>long</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static long getRequiredLongParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getLongParameter(request, parameterName, true);
  }

  /**
   * Checks if a parameter is present at position <code>index</code> in the url
   * whether it represents a valid <code>long</code>. In this case, that
   * parameter is returned as a <code>long</code> value, whereas otherwise a
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the weblounge action
   * @param index
   *          the parameter index
   * @return the parameter value
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>long</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static long getRequiredLongParameter(WebloungeRequest request,
      Action action, int index) throws IllegalArgumentException,
      IllegalStateException {
    return getLongParameter(request, action, index, true);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>float</code>. In that case, the parameter is
   * returned as a <code>float</code>, otherwise <code>0.0f</code> is returned.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>float</code>
   */
  public static float getFloatParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getFloatParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>float</code>. Should the parameter not
   * be part of the request, <code>defaultValue</code> is returned, otherwise
   * the parameter value is returned as an <code>float</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>float</code>
   */
  public static float getFloatParameterWithDefault(WebloungeRequest request,
      String parameterName, float defaultValue) throws IllegalArgumentException {
    try {
      return getFloatParameter(request, parameterName, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>float</code>. Should
   * the parameter be part of the request, it's value is returned as a
   * <code>float</code> whereas otherwise the <code>defaultValue</code> is
   * returned instead.
   * 
   * @param request
   *          the weblounge request
   * @param index
   *          the parameter index
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>float</code>
   */
  public static float getFloatParameterWithDefault(WebloungeRequest request,
      Action action, int index, float defaultValue)
      throws IllegalArgumentException {
    try {
      return getFloatParameter(request, action, index, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is a valid <code>float</code>. Should the parameter not be part of the
   * request, <code>0</code> is returned, otherwise the parameter value is
   * returned as an <code>float</code>.
   * <p>
   * If the parameter is required, then an {@link IllegalStateException} is
   * thrown.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>float</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static float getFloatParameter(WebloungeRequest request,
      String parameterName, boolean required) throws IllegalArgumentException,
      IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, parameterName);
    else
      p = getParameter(request, parameterName);
    if (p == null)
      return 0;
    try {
      return Float.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Request parameter '" + parameterName + "' must be a float");
    }
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>float</code>. In that
   * case, the parameter is returned as a <code>float</code>, otherwise
   * <code>0.0f</code> is returned.
   * <p>
   * If the parameter is required, and it is not part of the url, then an
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action that determines the mountpoint
   * @param index
   *          the index of the parameter in the url
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0.0f</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>float</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static float getFloatParameter(WebloungeRequest request,
      Action action, int index, boolean required)
      throws IllegalArgumentException, IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, action, index);
    else
      p = getParameter(request, action, index);
    if (p == null)
      return 0.0f;
    try {
      return Float.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Url parameter at index " + index + " must be a float");
    }
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>float</code>. Should the parameter not
   * be part of the request, <code>defaultValue</code> is returned, otherwise
   * the parameter value is returned as an <code>float</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>float</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static float getRequiredFloatParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getFloatParameter(request, parameterName, true);
  }

  /**
   * Checks if a parameter is present at position <code>index</code> in the url
   * whether it represents a valid <code>float</code>. In this case, that
   * parameter is returned as a <code>float</code> value, whereas otherwise a
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the weblounge action
   * @param index
   *          the parameter index
   * @return the parameter value
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>float</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static float getRequiredFloatParameter(WebloungeRequest request,
      Action action, int index) throws IllegalArgumentException,
      IllegalStateException {
    return getFloatParameter(request, action, index, true);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>boolean</code>. Should the parameter not be
   * part of the request, <code>0</code> is returned, otherwise the parameter
   * value is returned as an <code>boolean</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   */
  public static boolean getBooleanParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getBooleanParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>boolean</code>. Should the parameter
   * not be part of the request, <code>defaultValue</code> is returned,
   * otherwise the parameter value is returned as an <code>boolean</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   */
  public static boolean getBooleanParameterWithDefault(
      WebloungeRequest request, String parameterName, boolean defaultValue)
      throws IllegalArgumentException {
    String p = getParameter(request, parameterName);
    if (p == null)
      return defaultValue;
    else
      return getBooleanParameter(request, parameterName);
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>boolean</code>. Should
   * the parameter be part of the request, it's value is returned as a
   * <code>boolean</code> whereas otherwise the <code>defaultValue</code> is
   * returned instead.
   * 
   * @param request
   *          the weblounge request
   * @param index
   *          the parameter index
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>boolean</code>
   */
  public static boolean getBooleanParameterWithDefault(
      WebloungeRequest request, Action action, int index, boolean defaultValue)
      throws IllegalArgumentException {
    try {
      return getBooleanParameter(request, action, index, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>boolean</code>. Should the parameter
   * not be part of the request, <code>defaultValue</code> is returned,
   * otherwise the parameter value is returned as an <code>boolean</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static boolean getRequiredBooleanParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getBooleanParameter(request, parameterName, true);
  }

  /**
   * Checks if a parameter is present at position <code>index</code> in the url
   * whether it represents a valid <code>boolean</code>. In this case, that
   * parameter is returned as a <code>boolean</code> value, whereas otherwise a
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the weblounge action
   * @param index
   *          the parameter index
   * @return the parameter value
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>boolean</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static boolean getRequiredBooleanParameter(WebloungeRequest request,
      Action action, int index) throws IllegalArgumentException,
      IllegalStateException {
    return getBooleanParameter(request, action, index, true);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is a valid <code>boolean</code>. Should the parameter not be part of
   * the request, <code>0</code> is returned, otherwise the parameter value is
   * returned as an <code>boolean</code>.
   * <p>
   * If the parameter is required, then an {@link IllegalStateException} is
   * thrown.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static boolean getBooleanParameter(WebloungeRequest request,
      String parameterName, boolean required) throws IllegalArgumentException,
      IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, parameterName);
    else
      p = getParameter(request, parameterName);
    if (p == null)
      return false;
    if (!"true".equalsIgnoreCase(p) && !"false".equalsIgnoreCase(p))
      throw new IllegalArgumentException("Request parameter '" + parameterName + "' must be a boolean");
    return Boolean.valueOf(p);
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>boolean</code>. In that
   * case, the parameter is returned as a <code>boolean</code>, otherwise
   * <code>false</code> is returned.
   * <p>
   * If the parameter is required, and it is not part of the url, then an
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action that determines the mountpoint
   * @param index
   *          the index of the parameter in the url
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>false</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static boolean getBooleanParameter(WebloungeRequest request,
      Action action, int index, boolean required)
      throws IllegalArgumentException, IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, action, index);
    else
      p = getParameter(request, action, index);
    if (p == null)
      return false;
    try {
      return Boolean.valueOf(p);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Url parameter at index " + index + " must be a boolean");
    }
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>java.util.Date</code>. Should the parameter
   * not be part of the request, <code>0</code> is returned, otherwise the
   * parameter value is returned as an <code>java.util.Date</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   */
  public static Date getDateParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getDateParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>java.util.Date</code>. Should the
   * parameter not be part of the request, <code>defaultValue</code> is
   * returned, otherwise the parameter value is returned as an
   * <code>java.util.Date</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   */
  public static Date getDateParameterWithDefault(WebloungeRequest request,
      String parameterName, Date defaultValue) throws IllegalArgumentException {
    String p = getParameter(request, parameterName);
    if (p == null)
      return defaultValue;
    else
      return getDateParameter(request, parameterName);
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>java.util.Date</code> .
   * Should the parameter be part of the request, it's value is returned as a
   * <code>java.util.Date</code> whereas otherwise the <code>defaultValue</code>
   * is returned instead.
   * 
   * @param request
   *          the weblounge request
   * @param index
   *          the parameter index
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>boolean</code>
   */
  public static Date getDateParameterWithDefault(WebloungeRequest request,
      Action action, int index, Date defaultValue)
      throws IllegalArgumentException {
    try {
      return getDateParameter(request, action, index, true);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>java.util.Date</code>. Should the
   * parameter not be part of the request, <code>defaultValue</code> is
   * returned, otherwise the parameter value is returned as an
   * <code>java.util.Date</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param defaultValue
   *          the default parameter value
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static Date getRequiredDateParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getDateParameter(request, parameterName, true);
  }

  /**
   * Checks if a parameter is present at position <code>index</code> in the url
   * whether it represents a valid <code>java.util.Date</code>. In this case,
   * that parameter is returned as a <code>java.util.Date</code> value, whereas
   * otherwise a {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the weblounge action
   * @param index
   *          the parameter index
   * @return the parameter value
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to a <code>boolean</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  public static Date getRequiredDateParameter(WebloungeRequest request,
      Action action, int index) throws IllegalArgumentException,
      IllegalStateException {
    return getDateParameter(request, action, index, true);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is a valid <code>java.util.Date</code>. Should the parameter not be
   * part of the request, <code>0</code> is returned, otherwise the parameter
   * value is returned as an <code>java.util.Date</code>.
   * <p>
   * If the parameter is required, then an {@link IllegalStateException} is
   * thrown.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static Date getDateParameter(WebloungeRequest request,
      String parameterName, boolean required) throws IllegalArgumentException,
      IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, parameterName);
    else
      p = getParameter(request, parameterName);
    if (p == null)
      return null;
    try {
      return parseDate(p);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Request parameter must be a date");
    }
  }

  /**
   * Checks if a parameter is present in the url at position
   * <code>parameter</code> and represents a valid <code>java.util.Date</code> .
   * In that case, the parameter is returned as a <code>java.util.Date</code>,
   * otherwise <code>false</code> is returned.
   * <p>
   * If the parameter is required, and it is not part of the url, then an
   * {@link IllegalStateException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param action
   *          the action that determines the mountpoint
   * @param index
   *          the index of the parameter in the url
   * @param required
   *          <code>true</code> if this parameter is mandatory
   * @return the parameter value or <code>false</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>boolean</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private static Date getDateParameter(WebloungeRequest request, Action action,
      int index, boolean required) throws IllegalArgumentException,
      IllegalStateException {
    String p = null;
    if (required)
      p = getRequiredParameter(request, action, index);
    else
      p = getParameter(request, action, index);
    if (p == null)
      return null;
    try {
      return parseDate(p);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Url parameter at index " + index + " must be a date");
    }
  }

  /**
   * Parses a string into a <code>java.util.Date</code> object. Valid date
   * string patterns are
   * 
   * <ul>
   * <li>dd.MM.yyyy (ex. 18.12.2010)</li>
   * <li>dd.MM.yyyy kk:mm (ex. 18.12.2010 15:35)</li>
   * <li>dd.MM.yyyy kk:mm:ss (ex. 18.12.2010 15:35:22)</li>
   * <li>yyyy-MM-dd (ex. 2010-12-18)</li>
   * <li>yyyyMMdd (ex. 20101218)</li>
   * <li>yyyy-MM-ddTkk:mmZ (ex. 2010-12-18T15:35+01:00)</li>
   * <li>yyyy-MM-ddTkk:mm:ssZ (ex. 2010-12-18T15:35:22+01:00)</li>
   * </ul>
   * 
   * @param datestring
   *          string to parse
   * @return date
   * @throws ParseException
   *           if the given string could not be converted to a valid date object
   */
  private static Date parseDate(String datestring) throws ParseException {
    String[] parsePatterns = {
    // german/swiss date format
        "dd.MM.yyyy",
        "dd.MM.yyyy kk:mm",
        "dd.MM.yyyy kk:mm:ss",
        // ISO8601
        "yyyy-MM-dd",
        "yyyyMMdd",
        "yyyy-MM-ddTkk:mmZ",
        "yyyy-MM-ddTkk:mm:ssZ",
        // us date format
        "MM/dd/yyyy" };
    return DateUtils.parseDate(datestring, parsePatterns);
  }

  /**
   * This method returns without noise if one of the specified parameters can be
   * found in the request and is not equal to the empty string. Otherwise, an
   * {@link IllegalArgumentException} is thrown.
   * 
   * @param request
   *          the weblounge request
   * @param parameters
   *          the parameter list
   * @throws IllegalStateException
   *           if none of the parameters were found in the request
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
    throw new IllegalArgumentException(buf.toString());
  }

}