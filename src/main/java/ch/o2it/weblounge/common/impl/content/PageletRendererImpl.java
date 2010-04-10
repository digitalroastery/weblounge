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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.HTMLHeadElement;
import ch.o2it.weblounge.common.content.Link;
import ch.o2it.weblounge.common.content.PageletRenderer;
import ch.o2it.weblounge.common.content.RenderException;
import ch.o2it.weblounge.common.content.Script;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Module;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This renderer implements a pagelet renderer that is backed by a Java Server
 * Page.
 */
public class PageletRendererImpl extends AbstractRenderer implements PageletRenderer {

  /** The editor url */
  protected URL editor = null;

  /** The defining module */
  protected Module module = null;

  /**
   * Creates a new page template that is backed by a Java Server Page located at
   * <code>url</code>.
   */
  public PageletRendererImpl() {
    addFlavor(RequestFlavor.HTML);
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
   * @see ch.o2it.weblounge.common.content.PageletRenderer#setModule(ch.o2it.weblounge.common.site.Module)
   */
  public void setModule(Module module) {
    this.module = module;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletRenderer#getModule()
   */
  public Module getModule() {
    return module;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletRenderer#setEditor(java.net.URL)
   */
  public void setEditor(URL editor) {
    this.editor = editor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletRenderer#getEditor()
   */
  public URL getEditor() {
    return editor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Renderer#render(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void render(WebloungeRequest request, WebloungeResponse response)
      throws RenderException {
    includeJSP(request, response, renderer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletRenderer#renderAsEditor(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void renderAsEditor(WebloungeRequest request,
      WebloungeResponse response) throws RenderException {
    includeJSP(request, response, editor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.GeneralComposeable#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.GeneralComposeable#equals(java.lang.Object)
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
  public static PageletRenderer fromXml(Node node)
      throws IllegalStateException {
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
  @SuppressWarnings("unchecked")
  public static PageletRenderer fromXml(Node node, XPath xpath)
      throws IllegalStateException {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    // Identifier
    String id = XPathHelper.valueOf(node, "@id", xpath);
    if (id == null)
      throw new IllegalStateException("Missing id in page template definition");

    // Class
    String className = XPathHelper.valueOf(node, "class", xpath);

    // Renderer url
    URL rendererUrl = null;
    String rendererUrlNode = XPathHelper.valueOf(node, "renderer", xpath);
    if (rendererUrlNode == null)
      throw new IllegalStateException("Missing renderer in page template definition");
    try {
      rendererUrl = new URL(rendererUrlNode);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Malformed renderer url in page template definition: " + rendererUrlNode);
    }

    // Create the pagelet renderer
    PageletRenderer renderer = null;
    if (className != null) {
      Class<? extends PageletRenderer> c = null;
      try {
        c = (Class<? extends PageletRenderer>)classLoader.loadClass(className);
        renderer = c.newInstance();
        renderer.setIdentifier(id);
        renderer.setRenderer(rendererUrl);
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
      renderer.setRenderer(rendererUrl);
    }
    
    // Composeable
    renderer.setComposeable("true".equals(XPathHelper.valueOf(node, "@composeable", xpath)));

    // Editor url
    String editorUrlNode = XPathHelper.valueOf(node, "editor", xpath);
    try {
      if (editorUrlNode != null) {
        URL editorUrl = new URL(editorUrlNode);
        renderer.setEditor(editorUrl);
      }
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Malformed editor url in page template definition: " + editorUrlNode);
    }

    // recheck time
    String recheck = XPathHelper.valueOf(node, "recheck", xpath);
    if (recheck != null) {
      try {
        renderer.setRecheckTime(ConfigurationUtils.parseDuration(recheck));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The pagelet renderer valid time '" + recheck + "' is malformed", e);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The pagelet renderer valid time '" + recheck + "' is malformed", e);
      }
    }

    // valid time
    String valid = XPathHelper.valueOf(node, "valid", xpath);
    if (valid != null) {
      try {
        renderer.setValidTime(ConfigurationUtils.parseDuration(valid));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The pagelet renderer valid time '" + valid + "' is malformed", e);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The pagelet renderer valid time '" + valid + "' is malformed", e);
      }
    }

    // name
    NodeList names = XPathHelper.selectList(node, "name", xpath);
    for (int i = 0; i < names.getLength(); i++) {
      Node localiziation = names.item(i);
      String language = XPathHelper.valueOf(localiziation, "@language", xpath);
      if (language == null)
        throw new IllegalStateException("Found pagelet renderer name without language");
      String name = XPathHelper.valueOf(localiziation, "text()", xpath);
      if (name == null)
        throw new IllegalStateException("Found empty pagelet name");
      renderer.setName(name, LanguageSupport.getLanguage(language));
    }

    // scripts
    NodeList scripts = XPathHelper.selectList(node, "includes/script", xpath);
    for (int i = 0; i < scripts.getLength(); i++) {
      renderer.addHTMLHeader(ScriptImpl.fromXml(scripts.item(i)));
    }

    // links
    NodeList includes = XPathHelper.selectList(node, "includes/link", xpath);
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

    // Renderer class
    if (!this.getClass().equals(PageletRendererImpl.class))
      buf.append("<class>").append(getClass().getName()).append("</class>");

    // Renderer url
    // TODO: Handle relative paths
    buf.append("<renderer>").append(renderer.toExternalForm()).append("</renderer>");

    // Editor url
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

    // Names
    for (Language l : name.languages()) {
      buf.append("<name language=\"").append(l.getIdentifier()).append("\">");
      buf.append(name.get(l));
      buf.append("</name>");
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
