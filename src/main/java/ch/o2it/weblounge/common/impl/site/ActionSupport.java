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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.impl.request.RequestSupport;
import ch.o2it.weblounge.common.impl.util.I18n;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.Renderer;

import org.apache.jasper.JasperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a default implementation for an
 * <code>ActionHandler</code>. The implementations of the various
 * <code>startXYZ</code> methods are implemented such that they leave the
 * rendering completely to the target page.
 * <p>
 * Be aware of the fact that action handlers are pooled, so make sure to
 * implement the <code>cleanup</code> method to clear any state information from
 * this handler instance and as usual, don't forget to call the super
 * implementation when overwriting methods.
 */
public class ActionSupport extends AbstractAction {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(ActionSupport.class);

  /** Request parameter name for information messages */
  public final static String INFOS = "webl:infos";

  /** Request parameter name for information messages */
  public final static String WARNINGS = "webl:warnings";

  /** Request parameter name for information messages */
  public final static String ERRORS = "webl:errors";

  /** Request field containing the required field names */
  public static final String FLD_REQUIRED = "required";

  /** The information messages */
  protected List<String> infoMessages = null;

  /** The warning messages */
  protected List<String> warningMessages = null;

  /** The error messages */
  protected List<String> errorMessages = null;

  /** Parameter collection extracted from the url extension */
  private List<String> urlparams = new ArrayList<String>();

  /**
   * This method prepares the action handler for the next upcoming request by
   * passing it the request and response for first analysis as well as the
   * desired output method.
   * <p>
   * It is not recommended that subclasses use this method to write anything to
   * the response. The call serves the single purpose acquire resources and set
   * up for the call to <code>startPage</code>.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   * @param flavor
   *          the output method
   * @see ch.o2it.weblounge.common.site.Action#configure(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse,
   *      ch.o2it.weblounge.common.page.Page,
   *      ch.o2it.weblounge.common.site.Renderer, java.lang.String)
   */
  public void configure(WebloungeRequest request, WebloungeResponse response,
      RequestFlavor flavor) {
    super.configure(request, response, flavor);
    loadUrlExtensionValues(request);
  }

  /**
   * This method always returns {@link Action#EVAL_REQUEST} and therefore leaves
   * rendering to the page.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return either <code>EVAL_PAGE</code> or <code>SKIP_PAGE</code> depending
   *         on whether the action wants to render the page on its own or have
   *         the template do the rendering.
   * @see ch.o2it.weblounge.api.module.ActionHandler#startPage(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse)
   */
  public int startResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    return EVAL_REQUEST;
  }

  /**
   * This method is called by the target page and gives the action the
   * possibility to either replace the includes in the page header, add more
   * includes to the existing ones or have the page handle the includes.
   * 
   * Implementing classes may return one out of two values:
   * <ul>
   * <li><code>EVAL_INCLUDES</code> to have the page handle the includes</li>
   * <li><code>SKIP_INCLUDES</code> to skip any includes by this page</li>
   * </ul>
   * If <code>SKIP_INCLUDES</code> is returned, then the action is responsible
   * for writing any output to the response object, since control of rendering
   * is transferred completely. <br>
   * If <code>EVAL_INCLUDES</code> is returned, the page will control the adding
   * of includes, while the action may still add some itself.
   * 
   * <b>Note</b> This callback will only be performed if the page contains a
   * &lt;webl:inlcudes/&gt; tag in the header section.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return either <code>EVAL_INCLUDES</code> or <code>SKIP_INCLUDES</code>
   */
  public int startIncludes(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    return EVAL_INCLUDES;
  }

  /**
   * This method always returns <code>true</code> and therefore leaves rendering
   * to the composer.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the template do the rendering.
   * @see ch.o2it.weblounge.api.module.ActionHandler#startComposer(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  public int startStage(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    return EVAL_COMPOSER;
  }

  /**
   * This method always returns <code>true</code> and therefore leaves rendering
   * to the composer.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @param composer
   *          the composer identifier
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the template do the rendering.
   * @see ch.o2it.weblounge.api.module.ActionHandler#startComposer(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  public int startComposer(WebloungeRequest request,
      WebloungeResponse response, String composer) throws ActionException {
    return EVAL_COMPOSER;
  }

  /**
   * This method always returns <code>true</code> and therefore leaves rendering
   * to the pagelet.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @param composer
   *          the composer identifier
   * @param position
   *          the pagelet position
   * @return either <code>EVAL_PAGELET</code> or <code>SKIP_PAGELET</code>
   *         depending on whether the action wants to render the pagelet on its
   *         own or have the template do the rendering.
   * @see ch.o2it.weblounge.api.module.ActionHandler#startPagelet(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String,
   *      int)
   */
  public int startPagelet(WebloungeRequest request, WebloungeResponse response,
      String composer, int position) throws ActionException {
    return EVAL_PAGELET;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.site.AbstractAction#passivate()
   */
  @Override
  public void passivate() {
    super.passivate();
    infoMessages = null;
    warningMessages = null;
    errorMessages = null;
  }

  /**
   * Includes the given renderer with the request.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param renderer
   *          the renderer to include
   */
  protected void include(WebloungeRequest request, WebloungeResponse response,
      Renderer renderer) {
    if (renderer == null) {
      String msg = "Renderer to be included in action '" + this + "' on " + request.getUrl() + " was not found!";
      log_.error(msg);
      response.invalidate();
      return;
    }
    try {
      renderer.render(request, response);
    } catch (Exception e) {
      String params = RequestSupport.getParameters(request);
      String msg = "Error including '" + renderer + "' in action '" + this + "' on " + request.getUrl() + " " + params;
      Throwable o = e.getCause();
      if (o instanceof JasperException && ((JasperException) o).getRootCause() != null) {
        Throwable rootCause = ((JasperException) o).getRootCause();
        msg += ": " + rootCause.getMessage();
        log_.error(msg, rootCause);
      } else if (o != null) {
        msg += ": " + o.getMessage();
        log_.error(msg, o);
      } else {
        log_.error(msg, e);
      }
      response.invalidate();
    }
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, <code>null</code> otherwise.
   * <p>
   * The parameter is decoded using the default encoding <code>utf-8</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameter
   *          the parameter name
   * @param encoding
   *          the encoding parameter, e. g. <code>utf-8</code>
   * @return the decoded parameter value or <code>null</code> if the parameter
   *         is not available
   */
  protected String getParameter(WebloungeRequest request, String parameter) {
    return getParameterWithDecoding(request, parameter, "UTF-8");
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
  protected String getParameterWithDecoding(WebloungeRequest request,
      String parameter, String decoding) {
    String p = request.getParameter(parameter);
    if (p != null) {
      p = p.trim();
      if ("application/x-www-form-urlencoded".equalsIgnoreCase(request.getContentType())) {
        try {
          p = URLDecoder.decode(p, decoding);
        } catch (UnsupportedEncodingException e) {
          // The specified encoding is unknown
          log_.error("Unknown encoding: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
          // Tried decoding a string with a % inside, so obviously the parameter
          // was decoded already
        }
      }
    }
    return (p != null && !"".equals(p)) ? p : null;
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
  protected boolean parameterExists(WebloungeRequest request, String parameter) {
    String p = request.getParameter(parameter);
    return (p != null && !p.equals(""));
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
   *          parameter value, should the parameter not be included in the
   *          request
   * @return the parameter value or <code>defaultValue</code> if the parameter
   *         is not available
   */
  protected String getParameterWithDefault(WebloungeRequest request,
      String parameter, String defaultValue) {
    String p = getParameter(request, parameter);
    return (p != null) ? p : defaultValue;
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is not equal to the empty string. In this case, the parameter itself is
   * returned, <code>null</code> otherwise.
   * 
   * @return <code>null</code> if the parameter is not available
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  protected String getRequiredParameter(WebloungeRequest request,
      String parameterName) throws IllegalStateException {
    String p = getParameter(request, parameterName);
    if (p == null)
      throw new IllegalStateException("Request parameter '" + parameterName + "' is mandatory");
    return p;
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and is a valid <code>short</code>. Should the parameter not be part of the
   * request, <code>0</code> is returned, otherwise the parameter value is
   * returned as an <code>short</code>.
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
   *           if the parameter value cannot be cast to an <code>short</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  private short getShortParameter(WebloungeRequest request,
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
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>short</code>. Should the parameter not be part
   * of the request, <code>0</code> is returned, otherwise the parameter value
   * is returned as an <code>short</code>.
   * 
   * @param request
   *          the weblounge request
   * @param parameterName
   *          the parameter name
   * @return the parameter value or <code>0</code> if the parameter is not
   *         available
   * @throws IllegalArgumentException
   *           if the parameter value cannot be cast to an <code>short</code>
   */
  protected short getShortParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getShortParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>short</code>. Should the parameter not be part
   * of the request, <code>defaultValue</code> is returned, otherwise the
   * parameter value is returned as an <code>short</code>.
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
   *           if the parameter value cannot be cast to an <code>short</code>
   */
  protected short getShortParameterWithDefault(WebloungeRequest request,
      String parameterName, short defaultValue) throws IllegalArgumentException {
    short p = getShortParameter(request, parameterName, false);
    return (p != 0) ? p : defaultValue;
  }

  /**
   * Checks if the parameter <code>parameterName</code> is present in the
   * request and represents a valid <code>short</code>. Should the parameter not
   * be part of the request, <code>defaultValue</code> is returned, otherwise
   * the parameter value is returned as an <code>short</code>.
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
   *           if the parameter value cannot be cast to an <code>short</code>
   * @throws IllegalStateException
   *           if the parameter was not found in the request
   */
  protected short getRequiredShortParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getShortParameter(request, parameterName, true);
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
  private int getIntegerParameter(WebloungeRequest request,
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
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>int</code>. Should the parameter not be part
   * of the request, <code>0</code> is returned, otherwise the parameter value
   * is returned as an <code>int</code>.
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
  protected int getIntegerParameter(WebloungeRequest request,
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
  protected int getIntegerParameterWithDefault(WebloungeRequest request,
      String parameterName, int defaultValue) throws IllegalArgumentException {
    int p = getIntegerParameter(request, parameterName, false);
    return (p != 0) ? p : defaultValue;
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
  protected int getRequiredIntegerParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getIntegerParameter(request, parameterName, true);
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
  private long getLongParameter(WebloungeRequest request, String parameterName,
      boolean required) throws IllegalArgumentException, IllegalStateException {
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
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>long</code>. Should the parameter not be part
   * of the request, <code>0</code> is returned, otherwise the parameter value
   * is returned as an <code>long</code>.
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
  protected long getLongParameter(WebloungeRequest request, String parameterName)
      throws IllegalArgumentException {
    return getLongParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>long</code>. Should the parameter not be part
   * of the request, <code>defaultValue</code> is returned, otherwise the
   * parameter value is returned as an <code>long</code>.
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
  protected long getLongParameterWithDefault(WebloungeRequest request,
      String parameterName, long defaultValue) throws IllegalArgumentException {
    long p = getLongParameter(request, parameterName, false);
    return (p != 0) ? p : defaultValue;
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
  protected long getRequiredLongParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getLongParameter(request, parameterName, true);
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
  private float getFloatParameter(WebloungeRequest request,
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
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>float</code>. Should the parameter not be part
   * of the request, <code>0</code> is returned, otherwise the parameter value
   * is returned as an <code>float</code>.
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
  protected float getFloatParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getFloatParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>float</code>. Should the parameter not be part
   * of the request, <code>defaultValue</code> is returned, otherwise the
   * parameter value is returned as an <code>float</code>.
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
  protected float getFloatParameterWithDefault(WebloungeRequest request,
      String parameterName, float defaultValue) throws IllegalArgumentException {
    float p = getFloatParameter(request, parameterName, false);
    return (p != 0) ? p : defaultValue;
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
  protected float getRequiredFloatParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getFloatParameter(request, parameterName, true);
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
  private boolean getBooleanParameter(WebloungeRequest request,
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
  protected boolean getBooleanParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException {
    return getBooleanParameter(request, parameterName, false);
  }

  /**
   * Checks if the parameter <code>parameter</code> is present in the request
   * and represents a valid <code>boolean</code>. Should the parameter not be
   * part of the request, <code>defaultValue</code> is returned, otherwise the
   * parameter value is returned as an <code>boolean</code>.
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
  protected boolean getBooleanParameterWithDefault(WebloungeRequest request,
      String parameterName, boolean defaultValue)
      throws IllegalArgumentException {
    String p = getParameter(request, parameterName);
    if (p == null)
      return defaultValue;
    else
      return getBooleanParameter(request, parameterName);
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
  protected boolean getRequiredBooleanParameter(WebloungeRequest request,
      String parameterName) throws IllegalArgumentException,
      IllegalStateException {
    return getBooleanParameter(request, parameterName, true);
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
  protected void requireAny(WebloungeRequest request, String[] parameters)
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

  /**
   * Loads parameters provided via the url extension (e. g.
   * action/param1/param2)
   * 
   * @param request
   *          request to gather values from.
   */
  private void loadUrlExtensionValues(WebloungeRequest request) {
    // load parameter values from url extension
    urlparams = new ArrayList<String>();
    String[] params = getRequestedUrlExtension().split("/");
    // first param is empty (because of leading slash), therefore start with
    // index
    // 1
    for (int i = 1; i < params.length; i++) {
      urlparams.add(params[i]);
    }
  }

  /**
   * Returns a collection with all the parameters provided via the url
   * extension.
   * 
   * TODO: Hide this in general getParameter()
   * 
   * @return a <code>List<String></code> object with all the parameters
   */
  protected List<String> getUrlParameters() {
    return this.urlparams;
  }

  /**
   * Returns true, if there is an url parameter at the specified position,
   * otherwise, false is returned.
   * 
   * TODO: Hide this in general getParameter()
   * 
   * @param i
   *          position of the parameter in the url extension
   * @return true if parameter is present, otherwise false
   */
  protected boolean isUrlParameterPresent(int i) {
    String param = getUrlParameter(i);
    if (param == null || param.length() == 0)
      return false;
    else
      return true;
  }

  /**
   * Returns a parameter value which was provided via the url extension.
   * 
   * TODO: Hide this in general getParameter()
   * 
   * @param i
   *          position of the parameter in the url extension
   * @return a <code>String</code> object with the requested parameter value
   */
  protected String getUrlParameter(int i) {
    if (i < getUrlParameters().size())
      return this.getUrlParameters().get(i);
    else
      return null;
  }

  // TODO: add Javadoc
  protected boolean checkShort(String parameter, WebloungeRequest request) {
    try {
      Short.parseShort(request.getParameter(parameter));
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // TODO: add Javadoc
  protected boolean checkShort(String parameter, short min, short max,
      WebloungeRequest request) {
    try {
      short s = Short.parseShort(request.getParameter(parameter));
      return (s >= min && s <= max);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // TODO: add Javadoc
  protected boolean checkInteger(String parameter, WebloungeRequest request) {
    try {
      Integer.parseInt(request.getParameter(parameter));
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // TODO: add Javadoc
  protected boolean checkInteger(String parameter, int min, int max,
      WebloungeRequest request) {
    try {
      int s = Integer.parseInt(request.getParameter(parameter));
      return (s >= min && s <= max);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // TODO: add Javadoc
  protected boolean checkLong(String parameter, WebloungeRequest request) {
    try {
      Long.parseLong(request.getParameter(parameter));
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // TODO: add Javadoc
  protected boolean checkLong(String parameter, long min, long max,
      WebloungeRequest request) {
    try {
      long l = Long.parseLong(request.getParameter(parameter));
      return (l >= min && l <= max);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Returns a list of required fields, which are determined by evaluating the
   * request parameter <code>{@link #FLD_REQUIRED}</code> and then searching the
   * request for the fields mentioned there.
   * 
   * @param request
   *          the request
   * @return a list with missing fields
   */
  protected static List<String> checkMissingFields(WebloungeRequest request) {
    List<String> incomplete = new ArrayList<String>();

    String fldRequired = request.getParameter(FLD_REQUIRED);
    if (fldRequired == null || fldRequired.trim().length() == 0) {
      return null;
    }

    // Check existence of required fields

    String[] required = ConfigurationUtils.getMultiOptionValues(fldRequired);
    for (int i = 0; i < required.length; i++) {
      String field = required[i];
      String value = request.getParameter(field);
      if (value == null || value.trim().length() == 0) {
        incomplete.add(field);
      }
    }

    if (incomplete.size() > 0) {
      request.setAttribute(FLD_REQUIRED, incomplete);
      return incomplete;
    }
    return null;
  }

  /**
   * Adds the message to the list of information messages.
   * 
   * @param request
   *          the weblounge request
   * @param msg
   *          the message
   */
  protected void reportInfo(WebloungeRequest request, String msg) {
    reportInfo(request, msg, new Object[] {});
  }

  /**
   * Adds the message to the list of information messages.
   * 
   * @param request
   *          the weblounge request
   * @param msg
   *          the message
   * @param arguments
   *          the list of arguments to incorporate into the message
   */
  protected void reportInfo(WebloungeRequest request, String msg,
      Object... arguments) {
    if (msg == null)
      throw new IllegalArgumentException("Message cannot be null");
    if (infoMessages == null) {
      infoMessages = new ArrayList<String>();
      request.setAttribute(INFOS, infoMessages);
    }
    infoMessages.add(createMessage(request, msg, arguments));
  }

  /**
   * Returns <code>true</code> if info messages have been reported.
   * 
   * @return <code>true</code> if there are info messages
   */
  protected boolean hasMessages() {
    return infoMessages == null || infoMessages.size() == 0;
  }

  /**
   * Adds the message to the list of warning messages.
   * 
   * @param request
   *          the weblounge request
   * @param msg
   *          the warning message
   */
  protected void reportWarning(WebloungeRequest request, String msg) {
    reportWarning(request, msg, null, new Object[] {});
  }

  /**
   * Adds the message to the list of warning messages.
   * 
   * @param request
   *          the weblounge request
   * @param msg
   *          the warning message
   * @param arguments
   *          the list of arguments to incorporate into the message
   */
  protected void reportWarning(WebloungeRequest request, String msg,
      Object... arguments) {
    if (msg == null)
      throw new IllegalArgumentException("Warning message cannot be null");
    if (warningMessages == null) {
      warningMessages = new ArrayList<String>();
      request.setAttribute(WARNINGS, warningMessages);
    }
    warningMessages.add(createMessage(request, msg, arguments));
  }

  /**
   * Returns <code>true</code> if warning messages have been reported.
   * 
   * @return <code>true</code> if there are warning messages
   */
  protected boolean hasWarnings() {
    return warningMessages == null || warningMessages.size() == 0;
  }

  /**
   * Adds the message to the list of error messages.
   * 
   * @param msg
   *          the error message
   * @param request
   *          the weblounge request
   */
  protected void reportError(WebloungeRequest request, String msg) {
    reportError(request, msg, null, new Object[] {});
  }

  /**
   * Adds the message to the list of error messages.
   * 
   * @param msg
   *          the error message
   * @param request
   *          the weblounge request
   * @param arguments
   *          the list of arguments to incorporate into the message
   */
  protected void reportError(WebloungeRequest request, String msg,
      Object... arguments) {
    if (msg == null)
      throw new IllegalArgumentException("Error message cannot be null");
    if (errorMessages == null) {
      errorMessages = new ArrayList<String>();
      request.setAttribute(ERRORS, errorMessages);
    }
    errorMessages.add(createMessage(request, msg, arguments));
  }

  /**
   * Processes the message by first looking up its I18n translation and then
   * applying optional arguments using a {@link MessageFormat}.
   * 
   * @param request
   *          the request
   * @param msg
   *          the message
   * @param args
   *          optional message arguments
   * @return the message
   */
  private String createMessage(WebloungeRequest request, String msg,
      Object... args) {
    msg = I18n.get(msg, request.getLanguage(), request.getSite());
    if (args == null || args.length == 0)
      return msg;
    return MessageFormat.format(msg, args);
  }

  /**
   * Returns <code>true</code> if error messages have been reported.
   * 
   * @return <code>true</code> if there are error messages
   */
  protected boolean hasErrors() {
    return errorMessages == null || errorMessages.size() == 0;
  }

}