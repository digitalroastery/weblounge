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

import ch.o2it.weblounge.common.content.HTMLInclude;
import ch.o2it.weblounge.common.content.Link;
import ch.o2it.weblounge.common.content.PageTemplate;
import ch.o2it.weblounge.common.content.RenderException;
import ch.o2it.weblounge.common.content.Script;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This renderer implements a page template that is backed by a Java Server
 * Page.
 */
public class PageTemplateImpl extends AbstractRenderer implements PageTemplate {

  /** Default composer for action output */
  protected String stage = DEFAULT_STAGE;

  /** Default page layout */
  protected String layout = null;
  
  /** Is this the default template? */
  protected boolean isDefault = false;

  /**
   * Creates a new page template.
   */
  public PageTemplateImpl() {
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
  public PageTemplateImpl(String identifier, URL url) {
    super(identifier, url);
    addFlavor(RequestFlavor.HTML);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageTemplate#setStage(java.lang.String)
   */
  public void setStage(String stage) {
    this.stage = stage;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageTemplate#getStage()
   */
  public String getStage() {
    return stage;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageTemplate#getDefaultLayout()
   */
  public String getDefaultLayout() {
    return layout;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageTemplate#setDefaultLayout(java.lang.String)
   */
  public void setDefaultLayout(String layout) {
    this.layout = layout;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.PageTemplate#setDefault(boolean)
   */
  public void setDefault(boolean v) {
    isDefault = v;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.PageTemplate#isDefault()
   */
  public boolean isDefault() {
    return isDefault;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageTemplate#render(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void render(WebloungeRequest request, WebloungeResponse response)
      throws RenderException {
    includeJSP(request, response, renderer);
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
   * Initializes this page template from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param node
   *          the page template node
   * @throws IllegalStateException
   *           if the page template cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static PageTemplate fromXml(Node node) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(node, xpath);
  }

  /**
   * Initializes this page template from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param node
   *          the page template node
   * @param xpath
   *          the xpath processor
   * @throws IllegalStateException
   *           if the page template cannot be parsed
   * @see #fromXml(Node)
   * @see #toXml()
   */
  @SuppressWarnings("unchecked")
  public static PageTemplate fromXml(Node node, XPath xpath)
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

    // Create the page template
    PageTemplate template = null;
    if (className != null) {
      Class<? extends PageTemplate> c = null;
      try {
        c = (Class<? extends PageTemplate>) classLoader.loadClass(className);
        template = c.newInstance();
        template.setIdentifier(id);
        template.setRenderer(rendererUrl);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Pagelet renderer implementation " + className + " not found", e);
      } catch (InstantiationException e) {
        throw new IllegalStateException("Error instantiating pagelet renderer " + className, e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Access violation instantiating pagelet renderer " + className, e);
      }
    } else {
      template = new PageTemplateImpl(id, rendererUrl);
    }

    // Composeable
    template.setComposeable(ConfigurationUtils.isTrue(XPathHelper.valueOf(node, "@composeable", xpath)));

    // Default
    template.setDefault(ConfigurationUtils.isTrue(XPathHelper.valueOf(node, "@default", xpath)));

    // Stage
    String stage = XPathHelper.valueOf(node, "stage", xpath);
    if (stage != null)
      template.setStage(stage);

    // Layout
    String layout = XPathHelper.valueOf(node, "layout", xpath);
    if (layout != null)
      template.setDefaultLayout(layout);

    // recheck time
    String recheck = XPathHelper.valueOf(node, "recheck", xpath);
    if (recheck != null) {
      try {
        template.setRecheckTime(ConfigurationUtils.parseDuration(recheck));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The page template valid time '" + recheck + "' is malformed", e);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The page template valid time '" + recheck + "' is malformed", e);
      }
    }

    // valid time
    String valid = XPathHelper.valueOf(node, "valid", xpath);
    if (valid != null) {
      try {
        template.setValidTime(ConfigurationUtils.parseDuration(valid));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("The page template valid time '" + valid + "' is malformed", e);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("The page template valid time '" + valid + "' is malformed", e);
      }
    }

    // name
    NodeList names = XPathHelper.selectList(node, "name", xpath);
    for (int i = 0; i < names.getLength(); i++) {
      Node localiziation = names.item(i);
      String language = XPathHelper.valueOf(localiziation, "@language", xpath);
      if (language == null)
        throw new IllegalStateException("Found page template name without language");
      String name = XPathHelper.valueOf(localiziation, "text()", xpath);
      if (name == null)
        throw new IllegalStateException("Found empty page template name");
      template.setName(name, LanguageSupport.getLanguage(language));
    }

    // scripts
    NodeList scripts = XPathHelper.selectList(node, "includes/script", xpath);
    for (int i = 0; i < scripts.getLength(); i++) {
      template.addInclude(ScriptImpl.fromXml(scripts.item(i)));
    }

    // links
    NodeList includes = XPathHelper.selectList(node, "includes/link", xpath);
    for (int i = 0; i < includes.getLength(); i++) {
      template.addInclude(LinkImpl.fromXml(includes.item(i)));
    }

    return template;
  }

  /**
   * Returns an XML representation of this renderer.
   * 
   * @return the xml representation
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<template");
    buf.append(" id=\"").append(identifier).append("\"");
    buf.append(" composeable=\"").append(composeable).append("\"");
    if (isDefault)
      buf.append(" default=\"true\"");
    buf.append(">");

    // Renderer class
    if (!this.getClass().equals(PageTemplateImpl.class))
      buf.append("<class>").append(getClass().getName()).append("</class>");

    // Renderer url
    // TODO: Handle relative paths
    buf.append("<renderer>").append(renderer.toExternalForm()).append("</renderer>");

    // State name
    if (stage != null && !DEFAULT_STAGE.equals(stage))
      buf.append("<stage>").append(stage).append("</stage>");

    // Default page layout
    if (layout != null)
      buf.append("<layout>").append(layout).append("</layout>");

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
    if (getIncludes().length > 0) {
      buf.append("<includes>");
      for (HTMLInclude include : getIncludes()) {
        if (include instanceof Link)
          buf.append(include.toXml());
      }
      for (HTMLInclude include : getIncludes()) {
        if (include instanceof Script)
          buf.append(include.toXml());
      }
      buf.append("</includes>");
    }

    buf.append("</template>");
    return buf.toString();
  }

}
