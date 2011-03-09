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

import ch.o2it.weblounge.common.content.page.HTMLHeadElement;
import ch.o2it.weblounge.common.content.page.Link;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.content.page.PageletRenderer;
import ch.o2it.weblounge.common.content.page.Script;
import ch.o2it.weblounge.common.impl.content.GeneralComposeable;
import ch.o2it.weblounge.common.impl.content.page.LinkImpl;
import ch.o2it.weblounge.common.impl.content.page.ScriptImpl;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.config.OptionsHelper;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
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

  /** Map containing uploaded files */
  protected List<FileItem> files = null;

  /** The number of includes */
  protected int includeCount = 0;

  /** The current request object */
  protected WebloungeRequest request = null;

  /** The current response object */
  protected WebloungeResponse response = null;

  /**
   * Default constructor.
   */
  public ActionSupport() {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#startResponse(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public abstract int startResponse(WebloungeRequest request,
      WebloungeResponse response) throws ActionException;

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
   * Sets the associated site.
   * 
   * @param site
   *          the associated site
   */
  public void setSite(Site site) {
    this.site = site;
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
   * Returns the absolute link pointing to this action.
   * 
   * @return the action's link
   */
  public WebUrl getUrl() {
    return new WebUrlImpl(site, UrlUtils.concat(site.getURL().toExternalForm(), mountpoint));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#setPath(java.lang.String)
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
   * @see ch.o2it.weblounge.common.site.Action#getPath()
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
   * Removes all flavors.
   */
  protected void clearFlavors() {
    flavors.clear();
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

    // Adjust the maximum valid and recheck time and add cache tags
    response.setMaximumValidTime(renderer.getValidTime());
    response.setMaximumRecheckTime(renderer.getRecheckTime());
    if (renderer.getModule() != null)
      response.addTag(CacheTag.Module, renderer.getModule().getIdentifier());
    response.addTag(CacheTag.Renderer, renderer.getIdentifier());

    // Include renderer in response
    renderer.render(request, response);
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
    logger.debug("Including renderer {}", renderer);
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
   * {@inheritDoc}
   * <p>
   * When overwriting this method, please make sure to call
   * <code>super.activate()</code> as well.
   * 
   * @see ch.o2it.weblounge.common.site.Action#activate()
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
   * @see ch.o2it.weblounge.common.site.Action#passivate()
   */
  public void passivate() {
    logger.trace("Passivating action {}", this);
    headers = null;
    flavor = null;
    files = null;
    includeCount = 0;
    request = null;
    response = null;
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
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #toXml()
   */
  @SuppressWarnings("unchecked")
  public static Action fromXml(Node config, XPath xpathProcessor)
      throws IllegalStateException {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    // identifier
    String identifier = XPathHelper.valueOf(config, "@id", xpathProcessor);
    if (identifier == null)
      throw new IllegalStateException("Unable to create actions without identifier");

    // class
    Action action = null;
    String className = XPathHelper.valueOf(config, "m:class", xpathProcessor);
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
    String mountpoint = XPathHelper.valueOf(config, "m:mountpoint", xpathProcessor);
    if (mountpoint == null)
      throw new IllegalStateException("Action '" + identifier + " has no mountpoint");
    action.setPath(mountpoint);
    // TODO: handle /, /*

    if (action instanceof HTMLActionSupport) {
      // content url
      String targetUrl = XPathHelper.valueOf(config, "m:page", xpathProcessor);
      ((HTMLActionSupport) action).setPageURI(targetUrl);

      // template
      String targetTemplate = XPathHelper.valueOf(config, "m:template", xpathProcessor);
      ((HTMLActionSupport) action).setTemplate(targetTemplate);
    }

    // recheck time
    String recheck = XPathHelper.valueOf(config, "m:recheck", xpathProcessor);
    if (recheck != null) {
      try {
        action.setRecheckTime(ConfigurationUtils.parseDuration(recheck));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The action recheck time '" + recheck + "' is malformed", e);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The action recheck time '" + recheck + "' is malformed", e);
      }
    }

    // valid time
    String valid = XPathHelper.valueOf(config, "m:valid", xpathProcessor);
    if (valid != null) {
      try {
        action.setValidTime(ConfigurationUtils.parseDuration(valid));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The action valid time '" + recheck + "' is malformed", e);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The action valid time '" + recheck + "' is malformed", e);
      }
    }

    // scripts
    NodeList scripts = XPathHelper.selectList(config, "m:includes/m:script", xpathProcessor);
    for (int i = 0; i < scripts.getLength(); i++) {
      action.addHTMLHeader(ScriptImpl.fromXml(scripts.item(i)));
    }

    // links
    NodeList includes = XPathHelper.selectList(config, "m:includes/m:link", xpathProcessor);
    for (int i = 0; i < includes.getLength(); i++) {
      action.addHTMLHeader(LinkImpl.fromXml(includes.item(i)));
    }

    // name
    String name = XPathHelper.valueOf(config, "m:name", xpathProcessor);
    action.setName(name);

    // options
    Node optionsNode = XPathHelper.select(config, "m:options", xpathProcessor);
    OptionsHelper.fromXml(optionsNode, action, xpathProcessor);

    return action;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Action#toXml()
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
      b.append("<name>");
      b.append(name);
      b.append("</name>");
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