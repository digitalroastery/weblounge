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

import ch.o2it.weblounge.common.impl.request.RequestUtils;
import ch.o2it.weblounge.common.impl.util.I18n;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.Renderer;

import org.apache.jasper.JasperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      RequestFlavor flavor) throws ActionException {
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
  public int startHTMLResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    return EVAL_REQUEST;
  }

  /**
   * This default implementation writes nothing to the output.
   *
   * @see ch.o2it.weblounge.common.site.Action#startXMLResponse(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void startXMLResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
  }

  /**
   * This default implementation writes nothing to the output.
   *
   * @see ch.o2it.weblounge.common.site.Action#startJSONResponse(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void startJSONResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
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
  public int startHTMLIncludes(WebloungeRequest request, WebloungeResponse response)
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
      String params = RequestUtils.getParameters(request);
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
  public List<String> getUrlParameters() {
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
  public boolean isUrlParameterPresent(int i) {
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
  public String getUrlParameter(int i) {
    if (i < getUrlParameters().size())
      return this.getUrlParameters().get(i);
    else
      return null;
  }

}