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

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.Env;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ActionConfiguration;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.Include;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.PageRendererConfiguation;
import ch.o2it.weblounge.common.site.PageTemplate;
import ch.o2it.weblounge.common.site.PageletRenderer;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.ScriptInclude;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * This class is the default implementation for a <code>ActionHandler</code>.
 * The implementations of the various <code>startXYZ</code> methods are
 * implemented such that they leave it to the target page to render the stuff.
 * <p>
 * Be aware of the fact that action handlers are pooled, so make sure to
 * implement the <code>cleanup</code> method to clear any state information from
 * this handler instance.
 * 
 * TODO: Integrate with pooling api
 */
public abstract class AbstractAction implements Action {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(AbstractAction.class);

  /** The action configuration */
  protected ActionConfiguration config = null;

  /** The requested rendering method */
  protected String method = null;

  /** The parent site */
  protected Site site = null;

  /** The parent module */
  protected Module module = null;

  /** Map containing uploaded files */
  protected List<FileItem> files = null;

  /** The number of includes */
  protected int includes = 0;

  /** The underlying page */
  protected Page page = null;

  /** The renderer used for this request */
  protected Renderer renderer = null;

  /** The current request object */
  protected WebloungeRequest request = null;

  /** The current response object */
  protected WebloungeResponse response = null;

  /**
   * Returns the action configuration.
   * 
   * @return the configuration
   */
  public ActionConfiguration getConfiguration() {
    return config;
  }

  /**
   * Sets the action handler configuration, which will be available to
   * subclasses via the instance member <code>config</code>.
   * 
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#init(ch.o2it.weblounge.common.site.api.module.ActionConfiguration)
   */
  public void init(ActionConfiguration config) {
    this.config = config;
    for (Include l : config.getLinks())
      if (l instanceof IncludeImpl)
        ((IncludeImpl) l).setModule(module);
    for (ScriptInclude s : config.getScripts())
      if (s instanceof ScriptIncludeImpl)
        ((ScriptIncludeImpl) s).setModule(module);
  }

  /**
   * Returns the action identifier.
   * 
   * @return the action identifier
   */
  public String getIdentifier() {
    return config.getIdentifier();
  }

  /**
   * Sets the parent module.
   * 
   * @param module
   *          the parent module
   */
  public void setModule(Module module) {
    this.module = module;
  }

  /**
   * Returns the parent module.
   * 
   * @return the module
   */
  public Module getModule() {
    return module;
  }

  /**
   * Returns the parent module with the given identifier.
   * 
   * @return the module
   */
  public Module getModule(String module) {
    return this.module.getSite().getModule(module);
  }

  /**
   * Sets the associated site if this is a site related renderer configuration.
   * 
   * @param site
   *          the associated site
   */
  public void setSite(Site site) {
    this.site = site;
  }

  /**
   * Returns the parent site.
   * 
   * @return the site
   */
  public Site getSite() {
    return site;
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
   * Returns the absolute link pointing to this action.
   * 
   * @return the action's link
   */
  public String getLink() {
    return UrlSupport.concat(new String[] {
        module.getSite().getLink(),
        config.getMountpoint() });
  }

  /**
   * Returns the path relative to the server root, including the weblounge mount
   * point.
   * 
   * @return the action's path
   */
  public String getPath() {
    return UrlSupport.concat(new String[] {
        Env.getMountpoint(),
        config.getMountpoint() });
  }

  /**
   * Returns the extension part of the requested url. For example, if an action
   * is mounted to <code>/test</code> and the url is <code>/test/a</code> then
   * this method will return <code>/a</code>. For the mount point itself, the
   * method will return <code>/</code>.
   * 
   * @param request
   *          the request
   * @return the path extension relative to the action's mount point
   */
  public String getRequestedUrlExtension(WebloungeRequest request) {
    return request.getRequestedUrl().getPath().substring(config.getMountpoint().length());
  }

  /**
   * Returns the extension part of the target url. For example, if an action is
   * mounted to <code>/test</code> and the url is <code>/test/a</code> then this
   * method will return <code>/a</code>. For the mount point itself, the method
   * will return <code>/</code>.
   * 
   * @param request
   *          the request
   * @return the path extension relative to the action's mount point
   */
  public String getUrlExtension(WebloungeRequest request) {
    return request.getUrl().getPath().substring(config.getMountpoint().length());
  }

  /**
   * Sets the method that is requested upon the next run.
   * 
   * @param method
   *          the rendering method
   */
  protected void setMethod(String method) {
    this.method = method;
  }

  /**
   * Returns the requested rendering method.
   * 
   * @return the rendering method
   */
  protected String getMethod() {
    return method;
  }

  /**
   * Configures the request to use the given renderer.
   * 
   * @param renderer
   *          the renderer identifier
   * @param request
   *          the request
   */
  protected void setRenderer(String renderer, WebloungeRequest request) {
    request.setAttribute(Renderer.TEMPLATE, renderer);
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
    
    String stage = PageRendererConfiguation.DEFAULT_STAGE;

    // Is a template defined in the request?
    String templateId = (String) request.getAttribute(WebloungeRequest.REQUEST_TEMPLATE);
    PageTemplate template = null;
    if (templateId != null) {
      template = site.getTemplate(templateId);
      if (template != null)
        stage = template.getStage();
      else
        log_.warn("Action {} was told to use non-existing template {}", this, templateId);
    }
    return composer.equalsIgnoreCase(stage);
  }

  /**
   * Returns <code>true</code> if the given method is supported by the action.
   * The method is used to lookup a rendering method for a given action id.
   * 
   * @param method
   *          the method name
   * @return <code>true</code> if the action supports the rendering method
   */
  public boolean provides(String method) {
    return config.provides(method);
  }

  /**
   * Returns the supported flavors of this action. Common flavors are defined in
   * {@link RequestFlavor}, but you are free to define your own.
   * 
   * @return the supported flavors
   */
  public String[] getFlavors() {
    return config.methods();
  }

  /**
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#configure(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public void configure(WebloungeRequest request, WebloungeResponse response,
      String method) {
    includes = 0;
    this.request = request;
    this.response = response;
    this.method = method;

    // Check if we have a file upload request
    if (ServletFileUpload.isMultipartContent(request)) {

      // Create a factory for disk-based file items
      DiskFileItemFactory factory = new DiskFileItemFactory();
      // TODO: Configure factory
      // factory.setSizeThreshold(yourMaxMemorySize);
      // factory.setRepository(yourTempDirectory);

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Set overall request size constraint
      // TODO: Configure uploader
      // upload.setSizeMax(yourMaxRequestSize);

      // Parse the request
      try {
        files = upload.parseRequest(request);
      } catch (FileUploadException e) {
        log_.error("Error parsing uploads: {}", e.getMessage(), e);
      }
    }

  }

  /**
   * Returns an iteration of the files that have been uploaded in the current
   * step. Note that this iterator may be empty if no files are present, since
   * the files collection is cleared if the wizard moves on. <br>
   * The iterator returns elements of type <code>UploadedFile</code>.
   * 
   * @return an iteration of uploaded files
   */
  protected Iterator<FileItem> files() {
    if (files != null)
      return files.iterator();
    return (new ArrayList<FileItem>()).iterator();
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
   * @param data
   *          is passed to the renderer
   * @throws ActionException
   *           if the passed renderer is <code>null</code>
   */
  protected void include(WebloungeRequest request, WebloungeResponse response,
      PageletRenderer renderer, Object data) {
    if (renderer == null) {
      String msg = "The renderer passed to include in action '" + this + "' was <null>!";
      throw new ActionException(this, "html", new IllegalArgumentException(msg));
    }

    // Prepare caching
    Language language = request.getLanguage();
    User user = request.getUser();
    long validTime = renderer.getValidTime();
    long recheckTime = renderer.getRecheckTime();
    CacheTagSet rendererTagSet = new CacheTagSet();

    rendererTagSet.add(CacheTag.Site, request.getSite().getIdentifier());
    rendererTagSet.add(CacheTag.Url, request.getUrl().getPath());
    rendererTagSet.add(CacheTag.Url, request.getRequestedUrl().getPath());
    rendererTagSet.add(CacheTag.Language, language.getIdentifier());
    rendererTagSet.add(CacheTag.User, user.getLogin());
    rendererTagSet.add(CacheTag.Module, getModule().getIdentifier());
    rendererTagSet.add(CacheTag.Action, getIdentifier());
    rendererTagSet.add(CacheTag.Position, includes);
    Enumeration<?> pe = request.getParameterNames();
    int parameterCount = 0;
    while (pe.hasMoreElements()) {
      parameterCount++;
      String key = pe.nextElement().toString();
      String[] values = request.getParameterValues(key);
      for (String value : values) {
        rendererTagSet.add(key, value);
      }
    }
    rendererTagSet.add(CacheTag.Parameters, Integer.toString(parameterCount));
    if (response.startResponsePart(rendererTagSet, validTime, recheckTime)) {
      log_.debug("Action handler {} answered request for {} from cache", this, renderer);
      return;
    }

    // Include renderer
    try {

      // Add additional cache tags
      if (renderer.getModule() != null)
        response.addTag(CacheTag.Module, renderer.getModule());
      response.addTag(CacheTag.Renderer, renderer.getIdentifier());

      // Render
      renderer.configure(request.getFlavor(), data);
      renderer.render(request, response);
      renderer.cleanup();

    } finally {
      Module m = renderer.getModule();
      if (m != null)
        m.returnRenderer(renderer);
      response.endResponsePart();
    }
    includes++;
  }

  /**
   * Requests the renderer with the given id from the current module and
   * Includes it in the request.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param renderer
   *          the renderer to include
   * @throws ActionException
   *           if the passed renderer cannot be found.
   */
  protected void include(WebloungeRequest request, WebloungeResponse response,
      String renderer) {
    include(request, response, getModule(), renderer, null);
  }

  /**
   * Requests the renderer with the given id from the current module and
   * Includes it in the request.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param renderer
   *          the renderer to include
   * @param data
   *          is passed to the renderer
   * @throws ActionException
   *           if the passed renderer cannot be found.
   */
  protected void include(WebloungeRequest request, WebloungeResponse response,
      String renderer, Object data) {
    include(request, response, getModule(), renderer, data);
  }

  /**
   * Requests the renderer with the given id from module <code>module</code> and
   * Includes it in the request.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param module
   *          the module identifier
   * @param renderer
   *          the renderer to include
   * @param data
   *          is passed to the renderer
   * @throws ActionException
   *           if the passed renderer cannot be found.
   */
  protected void include(WebloungeRequest request, WebloungeResponse response,
      String module, String renderer, Object data) {
    if (module == null)
      throw new ActionException(this, "html", new IllegalArgumentException("Module is null!"));
    if (renderer == null)
      throw new ActionException(this, "html", new IllegalArgumentException("Renderer is null!"));
    Module m = getSite().getModule(module);
    if (m == null) {
      String msg = "Trying to include renderer from unknown module '" + module + "'";
      throw new ActionException(this, "html", new IllegalArgumentException(msg));
    }
    include(request, response, m, renderer, data);
  }

  /**
   * Requests the renderer with the given id from module <code>module</code> and
   * Includes it in the request.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param module
   *          the module
   * @param renderer
   *          the renderer to include
   * @param data
   *          is passed to the renderer
   * @throws ActionException
   *           if the passed renderer cannot be found.
   */
  protected void include(WebloungeRequest request, WebloungeResponse response,
      Module module, String renderer, Object data) {
    if (module == null)
      throw new ActionException(this, "html", new IllegalArgumentException("Module is null!"));
    if (renderer == null)
      throw new ActionException(this, "html", new IllegalArgumentException("Renderer is null!"));
    PageletRenderer r = module.getRenderer(renderer);
    if (r == null) {
      String msg = "Trying to include unknown renderer '" + renderer + "'";
      throw new ActionException(this, "html", new IllegalArgumentException(msg));
    }
    log_.debug("Including renderer {}", renderer);
    include(request, response, r, data);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return config.getIdentifier().hashCode();
  }

  /**
   * Returns <code>true</code> if <code>o</code> equals this action handler.
   * 
   * @param o
   *          the object to test for equality
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o != null && o instanceof AbstractAction) {
      AbstractAction h = (AbstractAction) o;
      return this.config == h.config;
    }
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.core.util.pool.Lease#leased()
   */
  public void leased() {
  }

  /**
   * This method clears all member variables except for the action
   * configuration, site and module which are only set once.
   * 
   * @see ch.o2it.weblounge.common.core.util.pool.Lease#passivate()
   */
  public void returned() {
    cleanup();
    site = null;
    module = null;
    method = null;
    files = null;
    includes = 0;
    page = null;
    renderer = null;
    request = null;
    response = null;
  }

  /**
   * @see ch.o2it.weblounge.common.core.util.pool.Lease#dispose()
   */
  public boolean dispose() {
    return false;
  }

  /**
   * @see ch.o2it.weblounge.common.core.util.pool.Lease#retired()
   */
  public void retired() {
  }

  /**
   * Returns <code>s</code> if <code>s</code> is not <code>null</code>.
   * otherwise, the replacement is returned.
   * 
   * @param s
   *          the original text
   * @param replacement
   *          the replacement
   * @return <code>s</code> or <code>replacement</code>
   */
  protected static String replaceNull(String s, String replacement) {
    return (s == null || s.trim().equals("")) ? replacement : s;
  }

  /**
   * Returns <code>s</code> if <code>s</code> is not the empty string.
   * otherwise, <code>null</code> is returned.
   * 
   * @param s
   *          the original text
   * @return <code>s</code> or <code>null</code>
   */
  protected static String emptyToNull(String s) {
    return (s != null && s.trim().equals("")) ? null : s;
  }

  /**
   * Returns a string representation of this action, which consists of the
   * action identifier and the configured method.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getModule().getIdentifier() + "/" + config.getIdentifier() + ((method != null) ? " (" + method + ")" : "");
  }

}