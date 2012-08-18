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

package ch.entwine.weblounge.common.impl.content.page;

import static ch.entwine.weblounge.common.site.Environment.Any;

import ch.entwine.weblounge.common.content.RenderException;
import ch.entwine.weblounge.common.content.page.HTMLHeadElement;
import ch.entwine.weblounge.common.content.page.Link;
import ch.entwine.weblounge.common.content.page.PagePreviewMode;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.page.Script;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This renderer implements a pagelet renderer that is backed by a Java Server
 * Page.
 */
public class PageletRendererImpl extends AbstractRenderer implements PageletRenderer {

  /** The logging facility */
  private final Logger logger = LoggerFactory.getLogger(PageletRendererImpl.class);

  /** The editor url */
  protected URL editor = null;

  /** The defining module */
  protected Module module = null;

  /** The site */
  protected Site site = null;

  /** The preview mode */
  protected PagePreviewMode previewMode = PagePreviewMode.None;

  /**
   * Creates a new page template.
   */
  public PageletRendererImpl() {
    addFlavor(RequestFlavor.HTML);
  }

  /**
   * Creates a new page template with the given identifier.
   * 
   * @param identifier
   *          the template identifier
   */
  public PageletRendererImpl(String identifier) {
    this(identifier, null);
  }

  /**
   * Creates a new page template that is backed by a Java Server Page located at
   * <code>url</code>.
   * 
   * @param identifier
   *          the template identifier
   * @param url
   *          the renderer url
   */
  public PageletRendererImpl(String identifier, URL url) {
    super(identifier, url);
    addFlavor(RequestFlavor.HTML);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.PageletRenderer#setModule(ch.entwine.weblounge.common.site.Module)
   */
  public void setModule(Module module) {
    if (module == null)
      throw new IllegalArgumentException("Module must not be null");
    this.module = module;
    this.site = module.getSite();
    for (HTMLHeadElement htmlHead : headers) {
      htmlHead.setSite(site);
      htmlHead.setModule(module);
    }
    if (!Any.equals(environment)) {
      processURLTemplates(environment);
    }
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

    // Is there anything we need to be doing?
    if (!environment.equals(this.environment) && module != null) {
      logger.debug("Processing url templates of {} with environment {}", this, environment);
      processURLTemplates(environment);
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
    if (module == null)
      throw new IllegalStateException("Module is null");
    if (module.getSite() == null)
      throw new IllegalArgumentException("Site is null");

    // Process the renderer URL
    for (Map.Entry<String, URL> entry : renderers.entrySet()) {
      URL renderer = entry.getValue();
      String rendererURL = ConfigurationUtils.processTemplate(renderer.toExternalForm(), module, environment);
      try {
        renderer = new URL(rendererURL);
        renderers.put(entry.getKey(), renderer);
      } catch (MalformedURLException e) {
        logger.warn("Renderer url {} of pagelet {} is malformed", rendererURL, this);
        return false;
      }
    }

    // Process the editor URL
    if (editor != null) {
      String editorURL = ConfigurationUtils.processTemplate(editor.toExternalForm(), module, environment);
      try {
        editor = new URL(editorURL);
      } catch (MalformedURLException e) {
        logger.warn("Editor url {} of pagelet {} is malformed", editorURL, this);
        return false;
      }
    }

    // Process the head elements (scripts and stylesheet includes)
    for (HTMLHeadElement headElement : headers) {
      headElement.setEnvironment(environment);
    }

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.GeneralComposeable#addHTMLHeader(ch.entwine.weblounge.common.content.page.HTMLHeadElement)
   */
  @Override
  public void addHTMLHeader(HTMLHeadElement header) {
    if (module != null) {
      if (module != null)
        header.setModule(module);
      if (site != null)
        header.setSite(site);
      if (!Any.equals(environment))
        header.setEnvironment(environment);
    }
    super.addHTMLHeader(header);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.PageletRenderer#getModule()
   */
  public Module getModule() {
    return module;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.PageletRenderer#setPreviewMode(ch.entwine.weblounge.common.content.Pagelet.PagePreviewMode)
   */
  public void setPreviewMode(PagePreviewMode mode) {
    this.previewMode = mode;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.PageletRenderer#getPreviewMode()
   */
  public PagePreviewMode getPreviewMode() {
    return previewMode;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.page.AbstractRenderer#setRenderer(java.net.URL)
   */
  @Override
  public void setRenderer(URL renderer) {
    super.setRenderer(renderer);
    if (!Any.equals(environment) && module != null)
      processURLTemplates(environment);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.page.AbstractRenderer#getRenderer()
   */
  @Override
  public URL getRenderer() {
    return getRenderer(RendererType.Page.toString());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.page.AbstractRenderer#getRenderer(java.lang.String)
   */
  @Override
  public URL getRenderer(String type) {
    return super.getRenderer(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.PageletRenderer#setEditor(java.net.URL)
   */
  public void setEditor(URL editor) {
    this.editor = editor;
    if (!Any.equals(environment) && module != null)
      processURLTemplates(environment);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.PageletRenderer#getEditor()
   */
  public URL getEditor() {
    return editor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Renderer#render(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public void render(WebloungeRequest request, WebloungeResponse response)
      throws RenderException {
    URL renderer = renderers.get(RendererType.Page.toString().toLowerCase());
    includeJSP(request, response, renderer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.PageletRenderer#renderAsEditor(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public void renderAsEditor(WebloungeRequest request,
      WebloungeResponse response) throws RenderException {
    includeJSP(request, response, editor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.GeneralComposeable#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.GeneralComposeable#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    // This is to indicate that using the super implementation is sufficient
    return super.equals(o);
  }

  /**
   * Initializes this pagelet renderer from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param node
   *          the pagelet renderer node
   * @throws IllegalStateException
   *           if the pagelet renderer cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static PageletRenderer fromXml(Node node) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(node, xpath);
  }

  /**
   * Initializes this pagelet renderer from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param node
   *          the pagelet renderer node
   * @param xpath
   *          the xpath processor
   * @throws IllegalStateException
   *           if the pagelet renderer cannot be parsed
   * @see #fromXml(Node)
   * @see #toXml()
   */
  public static PageletRenderer fromXml(Node node, XPath xpath)
      throws IllegalStateException {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    // Identifier
    String id = XPathHelper.valueOf(node, "@id", xpath);
    if (id == null)
      throw new IllegalStateException("Missing id in page template definition");

    // Class
    String className = XPathHelper.valueOf(node, "m:class", xpath);

    // Create the pagelet renderer
    PageletRenderer renderer = null;
    if (className != null) {
      Class<? extends PageletRenderer> c = null;
      try {
        c = (Class<? extends PageletRenderer>) classLoader.loadClass(className);
        renderer = c.newInstance();
        renderer.setIdentifier(id);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Pagelet renderer implementation " + className + " not found", e);
      } catch (InstantiationException e) {
        throw new IllegalStateException("Error instantiating pagelet renderer " + className, e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Access violation instantiating pagelet renderer " + className, e);
      }
    } else {
      renderer = new PageletRendererImpl();
      renderer.setIdentifier(id);
    }

    // Renderer url
    NodeList rendererUrlNodes = XPathHelper.selectList(node, "m:renderer", xpath);
    if (rendererUrlNodes.getLength() == 0)
      throw new IllegalStateException("Missing renderer in page template definition");
    for (int i = 0; i < rendererUrlNodes.getLength(); i++) {
      Node rendererUrlNode = rendererUrlNodes.item(i);
      URL rendererUrl = null;
      Node typeNode = rendererUrlNode.getAttributes().getNamedItem("type");
      String type = (typeNode != null) ? typeNode.getNodeValue() : RendererType.Page.toString();
      try {
        rendererUrl = new URL(rendererUrlNode.getFirstChild().getNodeValue());
        renderer.addRenderer(rendererUrl, type);
      } catch (MalformedURLException e) {
        throw new IllegalStateException("Malformed renderer url in page template definition: " + rendererUrlNode);
      }
    }

    // Composeable
    renderer.setComposeable("true".equals(XPathHelper.valueOf(node, "@composeable", xpath)));

    // Preview mode
    String previewMode = XPathHelper.valueOf(node, "m:preview", xpath);
    if (previewMode != null)
      renderer.setPreviewMode(PagePreviewMode.parse(previewMode));

    // Editor url
    String editorUrlNode = XPathHelper.valueOf(node, "m:editor", xpath);
    try {
      if (editorUrlNode != null) {
        URL editorUrl = new URL(editorUrlNode);
        renderer.setEditor(editorUrl);
      }
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Malformed editor url in page template definition: " + editorUrlNode);
    }

    // client revalidation time
    String recheck = XPathHelper.valueOf(node, "m:recheck", xpath);
    if (recheck != null) {
      try {
        renderer.setClientRevalidationTime(ConfigurationUtils.parseDuration(recheck));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The pagelet renderer revalidation time is malformed: '" + recheck + "'");
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The pagelet renderer revalidation time is malformed: '" + recheck + "'");
      }
    }

    // cache expiration time
    String valid = XPathHelper.valueOf(node, "m:valid", xpath);
    if (valid != null) {
      try {
        renderer.setCacheExpirationTime(ConfigurationUtils.parseDuration(valid));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The pagelet renderer valid time is malformed: '" + valid + "'", e);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The pagelet renderer valid time is malformed: '" + valid + "'", e);
      }
    }

    // name
    String name = XPathHelper.valueOf(node, "m:name", xpath);
    renderer.setName(name);

    // scripts
    NodeList scripts = XPathHelper.selectList(node, "m:includes/m:script", xpath);
    for (int i = 0; i < scripts.getLength(); i++) {
      renderer.addHTMLHeader(ScriptImpl.fromXml(scripts.item(i)));
    }

    // links
    NodeList includes = XPathHelper.selectList(node, "m:includes/m:link", xpath);
    for (int i = 0; i < includes.getLength(); i++) {
      renderer.addHTMLHeader(LinkImpl.fromXml(includes.item(i)));
    }

    return renderer;
  }

  /**
   * Returns an XML representation of this renderer.
   * 
   * @return the xml representation
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<pagelet");
    buf.append(" id=\"").append(identifier).append("\"");
    buf.append(" composeable=\"").append(composeable).append("\"");
    buf.append(">");

    // Names
    if (StringUtils.isNotBlank(name)) {
      buf.append("<name><![CDATA[");
      buf.append(name);
      buf.append("]]></name>");
    }

    // Renderer class
    if (!this.getClass().equals(PageletRendererImpl.class))
      buf.append("<class>").append(getClass().getName()).append("</class>");

    // Renderer url
    for (Map.Entry<String, URL> entry : renderers.entrySet()) {
      if (renderers.size() > 1)
        buf.append("<renderer type=\"").append(entry.getKey()).append("\">");
      else
        buf.append("<renderer>");
      buf.append(entry.getValue().toExternalForm()).append("</renderer>");
    }

    // Editor url
    if (editor != null)
      buf.append("<editor>").append(editor.toExternalForm()).append("</editor>");

    // Recheck time
    if (recheckTime >= 0) {
      buf.append("<recheck>");
      buf.append(ConfigurationUtils.toDuration(recheckTime));
      buf.append("</recheck>");
    }

    // Valid time
    if (validTime >= 0) {
      buf.append("<valid>");
      buf.append(ConfigurationUtils.toDuration(validTime));
      buf.append("</valid>");
    }

    // Preview
    if (!previewMode.equals(PagePreviewMode.None)) {
      buf.append("<preview>");
      buf.append(previewMode.toString().toLowerCase());
      buf.append("</preview>");
    }

    // Includes
    if (getHTMLHeaders().length > 0) {
      buf.append("<includes>");
      for (HTMLHeadElement header : getHTMLHeaders()) {
        if (header instanceof Link)
          buf.append(header.toXml());
      }
      for (HTMLHeadElement header : getHTMLHeaders()) {
        if (header instanceof Script)
          buf.append(header.toXml());
      }
      buf.append("</includes>");
    }

    buf.append("</pagelet>");
    return buf.toString();
  }

}
