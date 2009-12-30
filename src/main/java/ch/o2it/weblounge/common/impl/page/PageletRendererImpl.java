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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Include;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.PageletRenderer;
import ch.o2it.weblounge.common.site.RenderException;
import ch.o2it.weblounge.common.site.ScriptInclude;

import org.w3c.dom.Node;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This renderer implements a pagelet renderer that is backed by a Java Server
 * Page.
 */
public class PageletRendererImpl extends AbstractRenderer implements PageletRenderer {

  /** The editor url */
  protected URL editor = null;

  /** Links that need to be included in the header */
  protected Set<Include> links = null;

  /** Scripts that need to be included in the header */
  protected Set<ScriptInclude> scripts = null;

  /** The defining module */
  protected Module module = null;

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
    addFlavor(RequestFlavor.HTML.toString());
    links = new HashSet<Include>();
    scripts = new HashSet<ScriptInclude>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#setModule(ch.o2it.weblounge.common.site.Module)
   */
  public void setModule(Module module) {
    this.module = module;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#getModule()
   */
  public Module getModule() {
    return module;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#addLink(ch.o2it.weblounge.common.site.Include)
   */
  public void addLink(Include link) {
    links.add(link);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#removeLink(ch.o2it.weblounge.common.site.Include)
   */
  public void removeLink(Include link) {
    links.remove(link);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#getLinks()
   */
  public Include[] getLinks() {
    return links.toArray(new Include[links.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#addScript(ch.o2it.weblounge.common.site.ScriptInclude)
   */
  public void addScript(ScriptInclude script) {
    scripts.add(script);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#removeScript(ch.o2it.weblounge.common.site.ScriptInclude)
   */
  public void removeScript(ScriptInclude script) {
    scripts.remove(script);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#getScripts()
   */
  public ScriptInclude[] getScripts() {
    return scripts.toArray(new ScriptInclude[scripts.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#setEditor(java.net.URL)
   */
  public void setEditor(URL editor) {
    this.editor = editor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#getEditor()
   */
  public URL getEditor() {
    return editor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Renderer#render(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void render(WebloungeRequest request, WebloungeResponse response)
      throws RenderException {
    includeJSP(request, response, renderer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.PageletRenderer#renderAsEditor(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void renderAsEditor(WebloungeRequest request,
      WebloungeResponse response) throws RenderException {
    includeJSP(request, response, editor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.page.GeneralComposeable#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.page.GeneralComposeable#equals(java.lang.Object)
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
  public static PageletRendererImpl fromXml(Node node)
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
  public static PageletRendererImpl fromXml(Node node, XPath xpath)
      throws IllegalStateException {

    // Identifier
    String id = XPathHelper.valueOf(node, "@id", xpath);
    if (id == null)
      throw new IllegalStateException("Missing id in page template definition");

    // Url
    URL rendererUrl = null;
    if (XPathHelper.valueOf(node, "renderer", xpath) == null)
      throw new IllegalStateException("Missing renderer in page template definition");
    try {
      rendererUrl = new URL(XPathHelper.valueOf(node, "renderer", xpath));
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Malformed renderer url in page template definition");
    }

    // Create the pagelet renderer
    PageletRendererImpl template = new PageletRendererImpl(id, rendererUrl);

    // Composeable
    template.setComposeable("true".equals(XPathHelper.valueOf(node, "@composeable", xpath)));

    // Recheck time
    try {
      if (XPathHelper.valueOf(node, "recheck", xpath) != null) {
        long recheckTime = Long.parseLong(XPathHelper.valueOf(node, "recheck", xpath));
        template.setRecheckTime(recheckTime);
      }
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid recheck time in page template definition");
    }

    // Valid time
    try {
      if (XPathHelper.valueOf(node, "valid", xpath) != null) {
        long validTime = Long.parseLong(XPathHelper.valueOf(node, "recheck", xpath));
        template.setRecheckTime(validTime);
      }
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid valid time in page template definition");
    }

    // TODO: Links, Scripts

    // Names
    LanguageSupport.addDescriptions(node, "name", null, template.name, false);

    return template;
  }

  /**
   * Returns an XML representation of this renderer.
   * 
   * @return the xml representation
   */
  String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<renderer id=\"");
    buf.append(" id=\"").append(identifier).append("\"");
    buf.append(" composeable=\"").append(composeable).append("\"");
    buf.append(">");

    buf.append("</renderer>");
    return buf.toString();
  }

}
