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

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.content.Renderer;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.HTMLHeadElement;
import ch.entwine.weblounge.common.content.page.Link;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.page.Script;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.ActionException;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.common.site.I18nDictionary;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
public class HTMLActionSupport extends ActionSupport implements HTMLAction {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(HTMLActionSupport.class);

  /** The default path to render on */
  protected String targetPath = null;

  /** The page uri, deducted from targetPath */
  protected ResourceURI pageURI = null;

  /** The default page template id */
  protected String defaultTemplateId = null;

  /** The stage renderer id */
  protected String stageRendererId = null;

  /** The page template */
  protected PageTemplate defaultTemplate = null;

  /** The page template */
  protected PageTemplate template = null;

  /** The underlying page */
  protected Page page = null;

  /** The renderer used for this request */
  protected PageletRenderer stageRenderer = null;

  /** The information messages */
  protected List<String> infoMessages = null;

  /** The warning messages */
  protected List<String> warningMessages = null;

  /** The error messages */
  protected List<String> errorMessages = null;

  /** The runtime head elements */
  protected List<HTMLHeadElement> runtimeHeaders = null;

  /** Flag to indicate that output has been written to the client */
  protected boolean outputStarted = false;

  /** Flag to indicate that headers have been processed */
  protected boolean headersPassed = false;

  /**
   * Creates a new action implementation that directly supports the generation
   * of <code>HTML</code> pages.
   */
  public HTMLActionSupport() {
    this(null);
  }

  /**
   * Creates a new action implementation that directly supports the generation
   * of <code>HTML</code> pages.
   * <p>
   * When passing in <code>renderer</code>, the action's default behavior will
   * be to write the renderer to the stage.
   * 
   * @param renderer
   *          the renderer identifier
   */
  public HTMLActionSupport(String renderer) {
    flavors.add(RequestFlavor.HTML);
    stageRendererId = renderer;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#passivate()
   */
  public void passivate() {
    super.passivate();
    page = null;
    template = null;
    runtimeHeaders = null;
    infoMessages = null;
    warningMessages = null;
    errorMessages = null;
    outputStarted = false;
    headersPassed = false;
  }

  /**
   * Sets the associated site if this is a site related renderer configuration.
   * 
   * @param site
   *          the associated site
   */
  public void setSite(Site site) {
    super.setSite(site);
    if (site == null)
      return;

    if (targetPath != null)
      this.pageURI = new PageURIImpl(site, targetPath);
    if (defaultTemplateId != null && defaultTemplate == null)
      setDefaultTemplate(site.getTemplate(defaultTemplateId));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.site.ActionSupport#setModule(ch.entwine.weblounge.common.site.Module)
   */
  @Override
  public void setModule(Module module) {
    super.setModule(module);
    if (StringUtils.isNotBlank(stageRendererId)) {
      this.stageRenderer = getModule().getRenderer(stageRendererId);
      if (this.stageRenderer == null) {
        logger.warn("Stage renderer '{}' for action {} not found", stageRendererId, this);
      }
    }
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
   * @see ch.entwine.weblounge.common.site.Action#setPageURI(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public void setPageURI(ResourceURI uri) {
    this.pageURI = uri;
  }

  /**
   * Convenience method used to be able to define the target URI without having
   * access to the <code>Site</code> yet that will allow to set the uri as an
   * object of type <code>PageURI</code>.
   * 
   * @param uri
   *          the target path
   * @see #setPageURI(ResourceURI)
   */
  void setPageURI(String uri) {
    this.targetPath = uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#getPageURI()
   */
  public ResourceURI getPageURI() {
    if (pageURI == null && site != null && StringUtils.isNotBlank(targetPath)) {
      pageURI = new PageURIImpl(site, targetPath);
    }
    return pageURI;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.HTMLAction#setDefaultTemplate(ch.entwine.weblounge.common.content.page.PageTemplate)
   */
  public void setDefaultTemplate(PageTemplate template) {
    this.defaultTemplate = template;
  }

  /**
   * Convenience method used to be able to define the target template without
   * having access to the <code>Site</code> yet that will allow to set the
   * template as an object of type <code>PageTemplate</code>.
   * 
   * @param template
   *          the template identifier
   * @see #setTemplate(PageTemplate)
   */
  void setDefaultTemplate(String template) {
    this.defaultTemplateId = template;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.HTMLAction#getDefaultTemplate()
   */
  public PageTemplate getDefaultTemplate() {
    if (defaultTemplate != null)
      return defaultTemplate;

    // At configuration time, only the template is known
    if (defaultTemplateId != null) {
      defaultTemplate = site.getTemplate(defaultTemplateId);
      return defaultTemplate;
    }

    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.HTMLAction#setDefaultTemplate(ch.entwine.weblounge.common.content.page.PageTemplate)
   */
  public void setTemplate(PageTemplate template) {
    this.template = template;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#getTemplate()
   */
  public PageTemplate getTemplate() {
    return template;
  }

  /**
   * Sets the renderer that is used to renderer the <code>stage</code> composer
   * when implementers don't overwrite
   * {@link #startStage(WebloungeRequest, WebloungeResponse, Composer)} and
   * {@link #startComposer(WebloungeRequest, WebloungeResponse, Composer)}.
   * 
   * @param renderer
   *          the stage renderer
   */
  public void setStageRenderer(PageletRenderer renderer) {
    this.stageRendererId = (renderer != null) ? renderer.getIdentifier() : null;
    this.stageRenderer = renderer;
  }

  /**
   * Returns the renderer that is used to renderer the <code>stage</code>
   * composer when implementers don't overwrite
   * {@link #startStage(WebloungeRequest, WebloungeResponse, Composer)} and
   * {@link #startComposer(WebloungeRequest, WebloungeResponse, Composer)}.
   * 
   * @return the renderer
   */
  public PageletRenderer getStageRenderer() {
    if (stageRenderer == null) {
      if (StringUtils.isNotBlank(stageRendererId)) {
        if (module == null) {
          throw new IllegalStateException("Module was not set");
        }
        stageRenderer = getModule().getRenderer(stageRendererId);
        if (stageRenderer == null) {
          logger.warn("Stage renderer '{}' not found in module '{}'", stageRendererId, module.getIdentifier());
        }
      }
    }
    return stageRenderer;
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
    PageTemplate template = (PageTemplate) request.getAttribute(WebloungeRequest.TEMPLATE);
    if (template != null)
      stage = template.getStage();
    return composer.equalsIgnoreCase(stage);
  }

  /**
   * This implementation always returns
   * {@link ch.entwine.weblounge.common.site.Action#EVAL_REQUEST} and simply
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
   * @return {@link ch.entwine.weblounge.common.site.Action#EVAL_REQUEST}
   */
  public int startResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    String characterEncoding = response.getCharacterEncoding();
    if (StringUtils.isNotBlank(characterEncoding))
      response.setContentType("text/html;charset=" + characterEncoding.toLowerCase());
    else
      response.setContentType("text/html");
    outputStarted = true;
    return EVAL_REQUEST;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.GeneralComposeable#getHTMLHeaders()
   */
  @Override
  public HTMLHeadElement[] getHTMLHeaders() {
    List<HTMLHeadElement> headerList = new ArrayList<HTMLHeadElement>();
    headerList.addAll(Arrays.asList(super.getHTMLHeaders()));
    if (runtimeHeaders != null)
      headerList.addAll(runtimeHeaders);
    return headerList.toArray(new HTMLHeadElement[headerList.size()]);
  }

  /**
   * {@inheritDoc}
   * <p>
   * This implementation asks the action to return the include headers
   * <code>&lt;script&gt;</code> and <code>&lt;link&gt;</code> by calling
   * {@link #getHTMLHeaders()} and writes them to the response.
   * <p>
   * Since this implementation collects all of the includes that are needed to
   * render the page by iterating over the included elements, it returns
   * {@link ch.entwine.weblounge.common.site.HTMLAction#SKIP_HEADER} to tell the
   * tag implementation that no further work is needed.
   * 
   * @see ch.entwine.weblounge.common.site.HTMLAction#startHeader(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public int startHeader(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException {

    // Take note that headers have been passed
    headersPassed = true;

    return EVAL_HEADER;
  }

  /**
   * This method always returns
   * {@link ch.entwine.weblounge.common.site.HTMLAction#EVAL_COMPOSER} and
   * therefore leaves rendering to the actual content of the composer. This
   * means that if this action is rendered on an existing page, a call to
   * {@link #startPagelet(WebloungeRequest, WebloungeResponse, String, ch.entwine.weblounge.common.content.page.Pagelet)}
   * for each of them will be issued.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @return <code>EVAL_COMPOSER</code>
   * @see ch.entwine.weblounge.common.site.HTMLAction#startStage(ch.entwine.weblounge.api.request.WebloungeRequest,
   *      ch.entwine.weblounge.api.request.WebloungeResponse,
   *      ch.entwine.weblounge.common.content.page.Composer)
   */
  public int startStage(WebloungeRequest request, WebloungeResponse response,
      Composer composer) throws IOException, ActionException {
    if (stageRenderer != null) {
      include(request, response, stageRenderer);
      return SKIP_COMPOSER;
    }
    return EVAL_COMPOSER;
  }

  /**
   * This method always returns
   * {@link ch.entwine.weblounge.common.site.HTMLAction#EVAL_COMPOSER} and
   * therefore leaves rendering to the actual content of the composer. This
   * means that if this action is rendered on an existing page, a call to
   * {@link #startPagelet(WebloungeRequest, WebloungeResponse, String, ch.entwine.weblounge.common.content.page.Pagelet)}
   * for each of them will be issued.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @return <code>EVAL_COMPOSER</code>
   * @see ch.entwine.weblounge.common.site.HTMLAction#startComposer(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse,
   *      ch.entwine.weblounge.common.content.page.Composer)
   */
  public int startComposer(WebloungeRequest request,
      WebloungeResponse response, Composer composer) throws IOException,
      ActionException {
    return EVAL_COMPOSER;
  }

  /**
   * This method always returns
   * {@link ch.entwine.weblounge.common.site.HTMLAction#EVAL_PAGELET} and
   * therefore leaves rendering to the pagelet.
   * 
   * @param request
   *          the request object
   * @param response
   *          the response object
   * @param pagelet
   *          the pagelet
   * @return <code>EVAL_PAGELET</code>
   * @see ch.entwine.weblounge.common.site.HTMLAction#startPagelet(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse,
   *      ch.entwine.weblounge.common.content.page.Pagelet)
   */
  public int startPagelet(WebloungeRequest request, WebloungeResponse response,
      Pagelet pagelet) throws IOException, ActionException {
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
      logger.error(msg);
      response.invalidate();
      return;
    }
    try {
      renderer.render(request, response);
    } catch (Throwable t) {
      String params = RequestUtils.dumpParameters(request);
      String msg = "Error including '" + renderer + "' in action '" + this + "' on " + request.getUrl() + " " + params;
      Throwable o = t.getCause();
      if (o != null) {
        msg += ": " + o.getMessage();
        logger.error(msg, o);
      } else {
        logger.error(msg, t);
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
      request.setAttribute(HTMLAction.INFOS, infoMessages);
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
      request.setAttribute(HTMLAction.WARNINGS, warningMessages);
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
   * Announces the use of the given renderer during the execution of this
   * action. This will lead to the inclusion of the pagelet renderer's scripts
   * and links in the action's head section.
   * <p>
   * <strong>Note:</strong> Elements need to be announced <i>before</i> the
   * request has started processed, which means that the perfect place to do it
   * would be in either one of
   * {@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)} or
   * {@link #startResponse(WebloungeRequest, WebloungeResponse)}.
   * 
   * @param renderer
   *          the renderer
   * @throws IllegalStateException
   *           if the head elements are being added <i>after</i> the action's
   *           head section has been processed
   */
  protected void use(PageletRenderer renderer) {
    if (renderer == null)
      throw new IllegalArgumentException("Renderer must not be null");
    if (outputStarted)
      throw new IllegalStateException("HTMLHead elements can't be added after the head section has been processed");

    // Register the renderer's head elements
    for (HTMLHeadElement headElement : renderer.getHTMLHeaders()) {
      addRuntimeHeader(headElement);
    }
  }

  /**
   * Adds an {@link HTMLHeadElement} to the list of headers that will be written
   * to the <code>&lt;head&gt;</code> section of the page. This method should be
   * called if actions intend to include pagelets that require
   * <code>&lt;script&gt;</code> or <code>&lt;link&gt;</code> tags in the
   * <code>&lt;head&gt;</code> section of the page.
   * <p>
   * <strong>Note:</strong> Header elements obviously need to be added
   * <i>before</i> the headers have been processed, which means that the perfect
   * place to do it would be in either one of
   * {@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)},
   * {@link #startResponse(WebloungeRequest, WebloungeResponse)} or
   * {@link #startHeader(WebloungeRequest, WebloungeResponse)}.
   * 
   * @param headElement
   *          the head element to add
   * @throws IllegalStateException
   *           if the head elements are being added <i>after</i> the action's
   *           head section has been processed
   */
  protected void addRuntimeHeader(HTMLHeadElement headElement) {
    if (headersPassed)
      throw new IllegalStateException("HTMLHead elements can't be added after the head section has been processed");
    if (runtimeHeaders == null)
      runtimeHeaders = new ArrayList<HTMLHeadElement>();
    runtimeHeaders.add(headElement);
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
      request.setAttribute(HTMLAction.ERRORS, errorMessages);
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
    I18nDictionary dictionary = request.getSite().getI18n();
    msg = dictionary.get(msg, request.getLanguage());
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<action id=\"");
    b.append(identifier);
    b.append("\">");

    // Names
    if (StringUtils.isNotBlank(name)) {
      b.append("<name><![CDATA[");
      b.append(name);
      b.append("]]></name>");
    }

    // class
    b.append("<class>").append(getClass().getName()).append("</class>");

    // mountpoint
    b.append("<mountpoint>").append(mountpoint).append("</mountpoint>");

    // pageuri
    if (pageURI != null)
      b.append("<page>").append(pageURI.getPath()).append("</page>");
    else if (targetPath != null)
      b.append("<page>").append(targetPath).append("</page>");

    // template
    if (template != null)
      b.append("<template>").append(template).append("</template>");
    else if (defaultTemplateId != null)
      b.append("<template>").append(defaultTemplateId).append("</template>");

    // Recheck time
    if (recheckTime >= 0) {
      b.append("<recheck>");
      b.append(ConfigurationUtils.toDuration(recheckTime));
      b.append("</recheck>");
    }

    // Valid time
    if (validTime >= 0) {
      b.append("<valid>");
      b.append(ConfigurationUtils.toDuration(validTime));
      b.append("</valid>");
    }

    // Includes
    if (headers.size() > 0) {
      b.append("<includes>");
      for (HTMLHeadElement include : getHTMLHeaders()) {
        if (include instanceof Link)
          b.append(include.toXml());
      }
      for (HTMLHeadElement include : getHTMLHeaders()) {
        if (include instanceof Script)
          b.append(include.toXml());
      }
      b.append("</includes>");
    }

    // Options
    b.append(options.toXml());

    b.append("</action>");
    return b.toString();
  }

}