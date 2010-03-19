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

import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.request.RequestUtils;
import ch.o2it.weblounge.common.impl.util.I18n;
import ch.o2it.weblounge.common.page.HTMLInclude;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.HTMLAction;
import ch.o2it.weblounge.common.site.PageTemplate;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.Site;

import org.apache.jasper.JasperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the default implementation for an <code>HTMLAction</code>. The
 * implementations of the various <code>startXYZ</code> methods are implemented
 * such that they leave it to the target page to render the stuff.
 * <p>
 * <b>Note:</b> Be aware of the fact that actions are pooled, so make sure to
 * implement the <code>activate()</code> and <code>passivate()</code> method
 * accordingly and include the respective super implementations.
 */
public class HTMLActionSupport extends AbstractActionSupport implements HTMLAction {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(HTMLActionSupport.class);

  /** The default path to render on */
  protected String targetPath = null;

  /** The page uri, deducted from targetPath */
  protected PageURI pageURI = null;

  /** The page template id */
  protected String templateId = null;

  /** The page template */
  protected PageTemplate template = null;

  /** The underlying page */
  protected Page page = null;

  /** The renderer used for this request */
  protected Renderer renderer = null;

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

  /**
   * Creates a new action implementation that directly supports the generation
   * of <code>HTML</code> pages.
   */
  public HTMLActionSupport() {
    flavors.add(RequestFlavor.HTML);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#passivate()
   */
  public void passivate() {
    super.passivate();
    page = null;
    renderer = null;
    infoMessages = null;
    warningMessages = null;
    errorMessages = null;
  }

  /**
   * Sets the associated site if this is a site related renderer configuration.
   * 
   * @param site
   *          the associated site
   */
  public void setSite(Site site) {
    super.setSite(site);
    if (targetPath != null)
      this.pageURI = new PageURIImpl(site, targetPath);
    if (templateId != null)
      this.template = site.getTemplate(templateId);
  }

  /**
   * Sets the page that is used to do the action's rendering.
   * 
   * @param page
   *          the page
   */
  public void setPage(Page page) {
    this.page = page;
  }

  /**
   * Returns the page that is used to do the action's rendering.
   * 
   * @return the target page
   */
  public Page getPage() {
    return page;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#setPageURI(ch.o2it.weblounge.common.page.PageURI)
   */
  public void setPageURI(PageURI uri) {
    this.pageURI = uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#getPageURI()
   */
  public PageURI getPageURI() {
    return pageURI;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#setTemplate(ch.o2it.weblounge.common.site.PageTemplate)
   */
  public void setTemplate(PageTemplate template) {
    this.template = template;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#getTemplate()
   */
  public PageTemplate getTemplate() {
    return template;
  }

  /**
   * Returns <code>true</code> if <code>composer</code> equals the stage of the
   * current renderer.
   * 
   * @param composer
   *          the composer to test
   * @param request
   *          the request
   * @return <code>true</code> if <code>composer</code> is the main stage
   */
  protected boolean isStage(String composer, WebloungeRequest request) {
    if (composer == null)
      throw new IllegalArgumentException("Composer may not be null!");

    String stage = PageTemplate.DEFAULT_STAGE;
    PageTemplate template = (PageTemplate) request.getAttribute(WebloungeRequest.REQUEST_TEMPLATE);
    if (template != null)
      stage = template.getStage();
    return composer.equalsIgnoreCase(stage);
  }

  /**
   * This implementation always returns {@link Action#EVAL_REQUEST} and simply
   * sets the content type on the response to
   * <code>text/html;charset=utf-8</code>. This means that subclasses should
   * either overwrite this method to specify a different encoding or make sure
   * that everything that is written to the response is encoded to
   * <code>utf-8</code>.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return {@link Action#EVAL_REQUEST}
   */
  public int startResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    response.setContentType("text/html;charset=utf-8");
    return EVAL_REQUEST;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This implementation asks the action to return the include headers
   * <code>&lt;script&gt;</code> and <code>&lt;link&gt;</code> by calling
   * {@link #getIncludes()} and writes them to the response.
   * 
   * @see ch.o2it.weblounge.common.site.HTMLAction#startHeader(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void startHeader(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException {
    for (HTMLInclude include : getIncludes()) {
      response.getOutputStream().println(include.toXml());
    }
  }

  /**
   * This method always returns {@link HTMLAction#EVAL_COMPOSER} and therefore
   * leaves rendering to the actual content of the composer. This means that if
   * this action is rendered on an existing page, a call to
   * {@link #startPagelet(WebloungeRequest, WebloungeResponse, String, int)} for
   * each of them will be issued.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the composer content do the rendering.
   * @see ch.o2it.weblounge.api.module.ActionHandler#startComposer(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  public int startStage(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException {
    return EVAL_COMPOSER;
  }

  /**
   * This method always returns {@link HTMLAction#EVAL_COMPOSER} and therefore
   * leaves rendering to the actual content of the composer. This means that if
   * this action is rendered on an existing page, a call to
   * {@link #startPagelet(WebloungeRequest, WebloungeResponse, String, int)} for
   * each of them will be issued.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @return either <code>EVAL_COMPOSER</code> or <code>SKIP_COMPOSER</code>
   *         depending on whether the action wants to render the composer on its
   *         own or have the composer content do the rendering.
   * @see ch.o2it.weblounge.api.module.ActionHandler#startComposer(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  public int startComposer(WebloungeRequest request,
      WebloungeResponse response, String composer) throws IOException,
      ActionException {
    return EVAL_COMPOSER;
  }

  /**
   * This method always returns {@link HTMLAction#EVAL_PAGELET} and therefore
   * leaves rendering to the pagelet.
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
   *         depending on whether the action wants to render the pagelet itself
   *         or have the pagelet do the rendering.
   * @see ch.o2it.weblounge.api.module.ActionHandler#startPagelet(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String,
   *      int)
   */
  public int startPagelet(WebloungeRequest request, WebloungeResponse response,
      String composer, int position) throws IOException, ActionException {
    return EVAL_PAGELET;
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

}