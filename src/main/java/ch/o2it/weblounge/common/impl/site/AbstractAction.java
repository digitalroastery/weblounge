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

import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.page.GeneralComposeable;
import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.util.config.OptionsHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.PageTemplate;
import ch.o2it.weblounge.common.site.PageletRenderer;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is the default implementation for an <code>Action</code>.
 * <p>
 * <b>Note:</b> Be aware of the fact that actions are pooled, so make sure to
 * implement the <code>activate()</code> and <code>passivate()</code> method
 * accordingly and of course to include the respective super implementations.
 */
public abstract class AbstractAction extends GeneralComposeable implements Action {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(AbstractAction.class);

  /** The action mountpoint */
  protected String mountpoint = null;

  /** The list of flavors */
  protected Set<RequestFlavor> flavors = new HashSet<RequestFlavor>();

  /** The action name */
  protected LocalizableContent<String> name = new LocalizableContent<String>();

  /** Options support */
  protected OptionsHelper options = new OptionsHelper();

  /** The requested output flavor */
  protected RequestFlavor flavor = null;

  /** The parent site */
  protected Site site = null;

  /** The parent module */
  protected Module module = null;

  /** Map containing uploaded files */
  protected List<FileItem> files = null;
  
  /** Parameter collection extracted from the url extension */
  private List<String> urlparams = new ArrayList<String>();

  /** The number of includes */
  protected int includeCount = 0;

  /** The current request object */
  protected WebloungeRequest request = null;

  /** The current response object */
  protected WebloungeResponse response = null;

  /**
   * Default constructor.
   */
  public AbstractAction() {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#startResponse(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public int startResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    return EVAL_REQUEST;
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
   * Returns the absolute link pointing to this action.
   * 
   * @return the action's link
   */
  public WebUrl getUrl() {
    return new WebUrlImpl(site, UrlSupport.concat(new String[] {
        site.getUrl().getPath(),
        mountpoint }));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#setPath(java.lang.String)
   */
  public void setPath(String path) {
    this.mountpoint = UrlSupport.trim(path);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#getPath()
   */
  public String getPath() {
    return mountpoint;
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
  
  /**
   * Returns the extension part of the requested url. For example, if an action
   * is mounted to <code>/test</code> and the url is <code>/test/a</code> then
   * this method will return <code>/a</code>. For the mount point itself, the
   * method will return <code>/</code>.
   * 
   * @return the path extension relative to the action's mount point
   */
  protected String getRequestedUrlExtension() {
    if (request == null)
      throw new IllegalStateException("Request has not started");
    return request.getRequestedUrl().getPath().substring(mountpoint.length());
  }

  /**
   * Returns the extension part of the target url. For example, if an action is
   * mounted to <code>/test</code> and the url is <code>/test/a</code> then this
   * method will return <code>/a</code>. For the mount point itself, the method
   * will return <code>/</code>.
   * 
   * @return the path extension relative to the action's mount point
   */
  protected String getUrlExtension() {
    if (request == null)
      throw new IllegalStateException("Request has not started");
    return request.getUrl().getPath().substring(mountpoint.length());
  }

  /**
   * Returns the requested output flavor.
   * 
   * @return the output flavor
   */
  protected RequestFlavor getFlavor() {
    return flavor;
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#addFlavor(ch.o2it.weblounge.common.request.RequestFlavor)
   */
  public void addFlavor(RequestFlavor flavor) {
    flavors.add(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#removeFlavor(ch.o2it.weblounge.common.request.RequestFlavor)
   */
  public void removeFlavor(RequestFlavor flavor) {
    flavors.remove(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#getFlavors()
   */
  public RequestFlavor[] getFlavors() {
    return flavors.toArray(new RequestFlavor[flavors.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#supportsFlavor(java.lang.String)
   */
  public boolean supportsFlavor(RequestFlavor flavor) {
    return flavors.contains(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String key, String value) {
    options.setOption(key, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name) {
    return options.getOptionValue(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValue(java.lang.String,
   *      java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    return options.getOptionValue(name, defaultValue);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptionValues(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    return options.getOptionValues(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#getOptions()
   */
  public Map<String, List<String>> getOptions() {
    return options.getOptions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return options.hasOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    options.removeOption(name);
  }

  /**
   * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#configure(ch.o2it.weblounge.api.request.WebloungeRequest,
   *      ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public void configure(WebloungeRequest request, WebloungeResponse response,
      RequestFlavor flavor) throws ActionException {

    this.includeCount = 0;
    this.request = request;
    this.response = response;
    this.flavor = flavor;
    
    loadUrlExtensionValues(request);

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
      PageletRenderer renderer, Object data) throws ActionException {
    if (renderer == null) {
      String msg = "The renderer passed to include in action '" + this + "' was <null>!";
      throw new ActionException(new IllegalArgumentException(msg));
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

    // Add additional cache tags
    if (renderer.getModule() != null)
      response.addTag(CacheTag.Module, renderer.getModule());

    // Include renderer in response
    try {
      response.addTag(CacheTag.Renderer, renderer.getIdentifier());
      renderer.render(request, response);
    } finally {
      response.endResponsePart();
    }
    includeCount++;
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
      String renderer) throws ActionException {
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
      String renderer, Object data) throws ActionException {
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
      String module, String renderer, Object data) throws ActionException {
    if (module == null)
      throw new ActionException(new IllegalArgumentException("Module is null!"));
    if (renderer == null)
      throw new ActionException(new IllegalArgumentException("Renderer is null!"));
    Module m = getSite().getModule(module);
    if (m == null) {
      String msg = "Trying to include renderer from unknown module '" + module + "'";
      throw new ActionException(new IllegalArgumentException(msg));
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
      Module module, String renderer, Object data) throws ActionException {
    if (module == null)
      throw new ActionException(new IllegalArgumentException("Module is null!"));
    if (renderer == null)
      throw new ActionException(new IllegalArgumentException("Renderer is null!"));
    PageletRenderer r = module.getRenderer(renderer);
    if (r == null) {
      String msg = "Trying to include unknown renderer '" + renderer + "'";
      throw new ActionException(new IllegalArgumentException(msg));
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
    return identifier.hashCode();
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
      return module.equals(h.getModule()) && identifier.equals(h.identifier);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * When overwriting this method, please make sure to call
   * <code>super.activate()</code> as well.
   * 
   * @see ch.o2it.weblounge.common.site.Action#activate()
   */
  public void activate() {
    log_.trace("Activating action {}", this);
  }

  /**
   * {@inheritDoc}
   * <p>
   * When overwriting this method, please make sure to call
   * <code>super.passivate()</code> as well.
   * 
   * @see ch.o2it.weblounge.common.site.Action#passivate()
   */
  public void passivate() {
    log_.trace("Passivating action {}", this);
    flavor = null;
    files = null;
    includeCount = 0;
    request = null;
    response = null;
  }

  /**
   * Returns a string representation of this action, which consists of the
   * action identifier and the configured method.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getModule().getIdentifier() + "/" + identifier;
  }

}