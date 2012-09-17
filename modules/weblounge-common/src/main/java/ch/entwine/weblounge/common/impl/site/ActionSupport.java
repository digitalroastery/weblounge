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

import ch.entwine.weblounge.common.content.page.HTMLHeadElement;
import ch.entwine.weblounge.common.content.page.HTMLInclude;
import ch.entwine.weblounge.common.content.page.Link;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.page.Script;
import ch.entwine.weblounge.common.impl.content.GeneralComposeable;
import ch.entwine.weblounge.common.impl.content.page.LinkImpl;
import ch.entwine.weblounge.common.impl.content.page.ScriptImpl;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.config.OptionsHelper;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.ActionException;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.common.url.WebUrl;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This class is the default implementation for an <code>Action</code>. Its main
 * two methods
 * {@link #configure(WebloungeRequest, WebloungeResponse, RequestFlavor)} and
 * {@link #startResponse(WebloungeRequest, WebloungeResponse)}
 * <p>
 * <b>Note:</b> Be aware of the fact that actions are pooled, so make sure to
 * implement the <code>activate()</code> and <code>passivate()</code> method
 * accordingly and of course to include the respective super implementations.
 */
public abstract class ActionSupport extends GeneralComposeable implements Action {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ActionSupport.class);

  /** The action mountpoint */
  protected String mountpoint = null;

  /** The list of flavors */
  protected Set<RequestFlavor> flavors = new HashSet<RequestFlavor>();

  /** Options support */
  protected OptionsHelper options = new OptionsHelper();

  /** The requested output flavor */
  protected RequestFlavor flavor = null;

  /** The parent site */
  protected Site site = null;

  /** The parent module */
  protected Module module = null;

  /** List of supported methods */
  private Set<String> verbs = null;

  /** Map containing uploaded files */
  protected List<FileItem> files = null;

  /** The number of includes */
  protected int includeCount = 0;

  /** The current request object */
  protected WebloungeRequest request = null;

  /** The current response object */
  protected WebloungeResponse response = null;

  /** The site's bundle context */
  protected BundleContext bundleContext = null;

  /**
   * Default constructor.
   */
  public ActionSupport() {
    verbs = new HashSet<String>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#startResponse(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public abstract int startResponse(WebloungeRequest request,
      WebloungeResponse response) throws ActionException;

  /**
   * Enables support for the given HTTP verb.
   * 
   * @param method
   *          the method
   * @throws IllegalArgumentException
   *           if <code>method</code> is <code>null</code> or empty
   * @see Action#supportsMethod(String)
   */
  protected void enableMethod(String method) {
    if (StringUtils.isBlank(method))
      throw new IllegalArgumentException("Method must not be blank");
    if (verbs == null)
      verbs = new HashSet<String>();
    verbs.add(method.toUpperCase());
  }

  /**
   * Enables support for the given HTTP verbs.
   * 
   * @param methods
   *          the HTTP verbs to support
   * @throws IllegalArgumentException
   *           if <code>methods</code> is <code>null</code>
   * @see Action#supportsMethod(String)
   */
  protected void enableMethods(String... methods) {
    if (methods == null)
      throw new IllegalArgumentException("Methods must not be blank");
    for (String method : methods) {
      enableMethod(method);
    }
  }

  /**
   * Disables support for the given HTTP verb.
   * 
   * @param method
   *          the method
   * @throws IllegalArgumentException
   *           if <code>method</code> is <code>null</code> or empty
   * @see Action#supportsMethod(String)
   */
  protected void disableMethod(String method) {
    if (StringUtils.isBlank(method))
      throw new IllegalArgumentException("Method must not be blank");
    if (verbs != null)
      verbs.remove(method.toUpperCase());
  }

  /**
   * Disables support for the given HTTP verbs.
   * 
   * @param methods
   *          the methods
   * @throws IllegalArgumentException
   *           if <code>methods</code> is <code>null</code>
   * @see Action#supportsMethod(String)
   */
  protected void disableMethods(String... methods) {
    if (methods == null)
      throw new IllegalArgumentException("Methods must not be blank");
    for (String method : methods) {
      disableMethod(method);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that this default implementation enables support for <code>GET</code>
   * requests only.
   * 
   * @throws IllegalArgumentException
   *           if <code>method</code> is <code>null</code>
   * @see ch.entwine.weblounge.common.site.Action#supportsMethod(java.lang.String)
   */
  public boolean supportsMethod(String method) {
    if (StringUtils.isBlank(method))
      throw new IllegalArgumentException("Method must not be blank");
    method = method.toUpperCase();
    // TODO Remove after all actions provide their own verbs
    if (verbs == null || verbs.size() == 0)
      return "GET".equals(method) || "POST".equals(method) || "PUT".equals(method);
    return verbs.contains(method.toUpperCase());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#getMethods()
   */
  @Override
  public String[] getMethods() {
    if (verbs == null || verbs.size() == 0) {
      return new String[] { "GET", "POST", "PUT" };
    } else {
      return verbs.toArray(new String[verbs.size()]);
    }
  }

  /**
   * Returns the site's OSGi bundle context if available, <code>null</code>
   * otherwise.
   * 
   * @return the bundle context
   */
  protected BundleContext getBundleContext() {
    return bundleContext;
  }

  /**
   * Sets the parent module.
   * 
   * @param module
   *          the parent module
   */
  public void setModule(Module module) {
    this.module = module;
    for (HTMLHeadElement headElement : headers) {
      headElement.setModule(module);
    }
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
   * Sets the associated site.
   * 
   * @param site
   *          the associated site
   */
  public void setSite(Site site) {
    this.site = site;
    for (HTMLHeadElement headElement : headers) {
      headElement.setSite(site);
    }

    // Store the site's bundle context
    if (site != null && site instanceof SiteImpl) {
      bundleContext = ((SiteImpl) site).getBundleContext();
    }
  }

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.GeneralComposeable#setEnvironment(ch.entwine.weblounge.common.site.Environment)
   */
  @Override
  public void setEnvironment(Environment environment) {
    if (environment == null)
      throw new IllegalArgumentException("Environment must not be null");

    if (!this.environment.equals(environment) && module != null) {
      processURLTemplates(environment);
      options.setEnvironment(environment);
    }

    super.setEnvironment(environment);
  }

  /**
   * Processes both renderer and editor url by replacing templates in their
   * paths with real values from the actual module.
   * 
   * @param environment
   *          the environment
   * 
   * @return <code>false</code> if the paths don't end up being real urls,
   *         <code>true</code> otherwise
   */
  private boolean processURLTemplates(Environment environment) {
    if (site == null)
      throw new IllegalStateException("Site cannot be null");
    if (module == null)
      throw new IllegalStateException("Module cannot be null");

    // Process the head elements (scripts and style sheet includes)
    for (HTMLHeadElement headElement : headers) {
      headElement.setEnvironment(environment);
    }

    return true;
  }

  /**
   * Returns the absolute link pointing to this action.
   * 
   * @return the action's link
   */
  public WebUrl getUrl() {
    return new WebUrlImpl(site, mountpoint);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#setPath(java.lang.String)
   */
  public void setPath(String path) {
    if (StringUtils.isBlank(path))
      throw new IllegalArgumentException("Path cannot be blank");
    if (!path.startsWith("/"))
      throw new IllegalArgumentException("Action mountpoint '" + path + "' must be absolute");
    this.mountpoint = UrlUtils.trim(path);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#getPath()
   */
  public String getPath() {
    return mountpoint;
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
    PageTemplate template = (PageTemplate) request.getAttribute(WebloungeRequest.TEMPLATE);
    if (template != null)
      stage = template.getStage();
    return composer.equalsIgnoreCase(stage);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#addFlavor(ch.entwine.weblounge.common.request.RequestFlavor)
   */
  public void addFlavor(RequestFlavor flavor) {
    flavors.add(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#removeFlavor(ch.entwine.weblounge.common.request.RequestFlavor)
   */
  public void removeFlavor(RequestFlavor flavor) {
    flavors.remove(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#getFlavors()
   */
  public RequestFlavor[] getFlavors() {
    return flavors.toArray(new RequestFlavor[flavors.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#supportsFlavor(java.lang.String)
   */
  public boolean supportsFlavor(RequestFlavor flavor) {
    return flavors.contains(flavor);
  }

  /**
   * Removes all flavors.
   */
  protected void clearFlavors() {
    flavors.clear();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.Action#setOption(java.lang.String,
   *      java.lang.String)
   */
  public void setOption(String key, String value) {
    options.setOption(key, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#setOption(java.lang.String,
   *      java.lang.String, ch.entwine.weblounge.common.site.Environment)
   */
  public void setOption(String name, String value, Environment environment) {
    options.setOption(name, value, environment);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValue(java.lang.String)
   */
  public String getOptionValue(String name) {
    return options.getOptionValue(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValue(java.lang.String,
   *      java.lang.String)
   */
  public String getOptionValue(String name, String defaultValue) {
    return options.getOptionValue(name, defaultValue);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionValues(java.lang.String)
   */
  public String[] getOptionValues(String name) {
    return options.getOptionValues(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptions()
   */
  public Map<String, Map<Environment, List<String>>> getOptions() {
    return options.getOptions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#hasOption(java.lang.String)
   */
  public boolean hasOption(String name) {
    return options.hasOption(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#getOptionNames()
   */
  public String[] getOptionNames() {
    return options.getOptionNames();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.Customizable#removeOption(java.lang.String)
   */
  public void removeOption(String name) {
    options.removeOption(name);
  }

  /**
   * @see ch.entwine.weblounge.common.site.Action.module.ActionHandler#configure(ch.entwine.weblounge.api.request.WebloungeRequest,
   *      ch.entwine.weblounge.api.request.WebloungeResponse, java.lang.String)
   */
  public void configure(WebloungeRequest request, WebloungeResponse response,
      RequestFlavor flavor) throws ActionException {

    this.includeCount = 0;
    this.request = request;
    this.response = response;
    this.flavor = flavor;

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
        logger.error("Error parsing uploads: {}", e.getMessage(), e);
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
   * includes the given renderer with the request.
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
      PageletRenderer renderer, Pagelet data) throws ActionException {
    if (renderer == null) {
      String msg = "The renderer passed to include in action '" + this + "' was <null>!";
      throw new ActionException(new IllegalArgumentException(msg));
    }

    if (data != null)
      request.setAttribute(WebloungeRequest.PAGELET, data);

    // Include renderer in response
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

    request.removeAttribute(WebloungeRequest.PAGELET);
    includeCount++;
  }

  /**
   * Requests the renderer with the given id from the current module and
   * includes it in the request.
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
   * includes it in the request.
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
      String renderer, Pagelet data) throws ActionException {
    include(request, response, getModule(), renderer, data);
  }
  
  /**
   * Requests the renderer with the given id from the current module and
   * includes it in the request.
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
      PageletRenderer renderer) throws ActionException {
    include(request, response, renderer, null);
  }

  /**
   * Requests the renderer with the given id from module <code>module</code> and
   * includes it in the request.
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
      String module, String renderer, Pagelet data) throws ActionException {
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
   * includes it in the request.
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
      Module module, String renderer, Pagelet data) throws ActionException {
    if (module == null)
      throw new ActionException(new IllegalArgumentException("Module is null!"));
    if (renderer == null)
      throw new ActionException(new IllegalArgumentException("Renderer is null!"));
    PageletRenderer r = module.getRenderer(renderer);
    if (r == null) {
      String msg = "Trying to include unknown renderer '" + renderer + "'";
      throw new ActionException(new IllegalArgumentException(msg));
    }
    logger.debug("Including renderer {}", renderer);

    // Add the pagelet's header elements to the response
    for (HTMLHeadElement header : r.getHTMLHeaders()) {
      if (!HTMLInclude.Use.Editor.equals(header.getUse()))
        response.addHTMLHeader(header);
    }

    include(request, response, r, data);
  }

  /**
   * Finds the first service in the service registry and returns it. If not such
   * service is available, <code>null</code> is returned.
   * 
   * @param c
   *          the class of the service to look for
   * @return the service
   */
  protected <S extends Object> S getService(Class<S> c) {
    String className = c.getName();
    BundleContext ctx = getBundleContext();
    if (ctx == null)
      return null;
    try {
      ServiceReference serviceRef = ctx.getServiceReference(className);
      if (serviceRef == null) {
        logger.debug("No service for class {} found", className);
        return null;
      }
      S smtpService = (S) ctx.getService(serviceRef);
      return smtpService;
    } catch (IllegalStateException e) {
      logger.debug("Service of type {} cannot be received through deactivating bundle {}", className, ctx);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * When overwriting this method, please make sure to call
   * <code>super.activate()</code> as well.
   * 
   * @see ch.entwine.weblounge.common.site.Action#activate()
   */
  public void activate() {
    logger.trace("Activating action {}", this);
  }

  /**
   * {@inheritDoc}
   * <p>
   * When overwriting this method, please make sure to call
   * <code>super.passivate()</code> as well.
   * 
   * @see ch.entwine.weblounge.common.site.Action#passivate()
   */
  public void passivate() {
    logger.trace("Passivating action {}", this);
    files = null;
    includeCount = 0;
    request = null;
    response = null;
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
    if (o != null && o instanceof ActionSupport) {
      ActionSupport h = (ActionSupport) o;
      if (module == null && h.getModule() != null)
        return false;
      if (module != null && !module.equals(h.getModule()))
        return false;
      return identifier.equals(h.identifier);
    }
    return false;
  }

  /**
   * Initializes this action from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param config
   *          the action node
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static Action fromXml(Node config) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(config, xpath);
  }

  /**
   * Initializes this action from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param config
   *          the action node
   * @param xpath
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #toXml()
   */
  public static Action fromXml(Node config, XPath xpath)
      throws IllegalStateException {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    // identifier
    String identifier = XPathHelper.valueOf(config, "@id", xpath);
    if (identifier == null)
      throw new IllegalStateException("Unable to create actions without identifier");

    // class
    Action action = null;
    String className = XPathHelper.valueOf(config, "m:class", xpath);
    if (className != null) {
      try {
        Class<? extends Action> c = (Class<? extends Action>) classLoader.loadClass(className);
        action = c.newInstance();
        action.setIdentifier(identifier);
      } catch (Throwable e) {
        throw new IllegalStateException("Unable to instantiate class " + className + " for action '" + identifier + ": " + e.getMessage(), e);
      }
    } else {
      action = new HTMLActionSupport();
      action.setIdentifier(identifier);
    }

    // mountpoint
    String mountpoint = XPathHelper.valueOf(config, "m:mountpoint", xpath);
    if (mountpoint == null)
      throw new IllegalStateException("Action '" + identifier + " has no mountpoint");
    action.setPath(mountpoint);
    // TODO: handle /, /*

    // content url
    String targetUrl = XPathHelper.valueOf(config, "m:page", xpath);
    if (StringUtils.isNotBlank(targetUrl)) {
      if (!(action instanceof HTMLActionSupport))
        throw new IllegalStateException("Target page configuration for '" + action.getIdentifier() + "' requires subclassing HTMLActionSupport");
      ((HTMLActionSupport) action).setPageURI(targetUrl);
    }

    // template
    String targetTemplate = XPathHelper.valueOf(config, "m:template", xpath);
    if (StringUtils.isNotBlank(targetTemplate)) {
      if (!(action instanceof HTMLActionSupport))
        throw new IllegalStateException("Target template configuration for '" + action.getIdentifier() + "' requires subclassing HTMLActionSupport");
      ((HTMLActionSupport) action).setDefaultTemplate(targetTemplate);
    }

    // client revalidation time
    String recheck = XPathHelper.valueOf(config, "m:recheck", xpath);
    if (recheck != null) {
      try {
        action.setClientRevalidationTime(ConfigurationUtils.parseDuration(recheck));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The action revalidation time is malformed: '" + recheck + "'");
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The action revalidation time is malformed: '" + recheck + "'");
      }
    }

    // cache expiration time
    String valid = XPathHelper.valueOf(config, "m:valid", xpath);
    if (valid != null) {
      try {
        action.setCacheExpirationTime(ConfigurationUtils.parseDuration(valid));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The action valid time is malformed: '" + valid + "'", e);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The action valid time is malformed: '" + valid + "'", e);
      }
    }

    // scripts
    NodeList scripts = XPathHelper.selectList(config, "m:includes/m:script", xpath);
    for (int i = 0; i < scripts.getLength(); i++) {
      action.addHTMLHeader(ScriptImpl.fromXml(scripts.item(i)));
    }

    // links
    NodeList includes = XPathHelper.selectList(config, "m:includes/m:link", xpath);
    for (int i = 0; i < includes.getLength(); i++) {
      action.addHTMLHeader(LinkImpl.fromXml(includes.item(i)));
    }

    // name
    String name = XPathHelper.valueOf(config, "m:name", xpath);
    action.setName(name);

    // options
    Node optionsNode = XPathHelper.select(config, "m:options", xpath);
    OptionsHelper.fromXml(optionsNode, action, xpath);

    return action;
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

    // class
    b.append("<class>").append(getClass().getName()).append("</class>");

    // mountpoint
    b.append("<mountpoint>").append(mountpoint).append("</mountpoint>");

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

    // Name
    if (StringUtils.isNotBlank(name)) {
      b.append("<name><![CDATA[");
      b.append(name);
      b.append("]]></name>");
    }

    // Includes
    if (headers.size() > 0) {
      b.append("<includes>");
      for (HTMLHeadElement header : getHTMLHeaders()) {
        if (header instanceof Link)
          b.append(header.toXml());
      }
      for (HTMLHeadElement header : getHTMLHeaders()) {
        if (header instanceof Script)
          b.append(header.toXml());
      }
      b.append("</includes>");
    }

    // Options
    b.append(options.toXml());

    b.append("</action>");
    return b.toString();
  }

  /**
   * Returns a string representation of this action, which consists of the
   * action identifier and the configured method.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    if (module != null)
      buf.append(module.getIdentifier()).append("/");
    buf.append(identifier);
    return buf.toString();
  }

}