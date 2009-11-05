/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.site.Include;
import ch.o2it.weblounge.common.site.Module;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;

/**
 * This class encapsulates the information to include a link element within the
 * &lt;head&gt; section of an html page.
 * 
 * @author Tobias Wunden
 */
public class IncludeImpl implements Include {

  /** The default link type */
  protected final static String TYPE_CSS = "text/css";

  /** The default link media */
  protected final static String MEDIA_ALL = "all";

  /** The default link rel */
  protected final static String REL_CSS = "stylesheet";

  /** Source */
  protected String href = null;

  /** The attributes collection */
  private Map<String, String> attributes = new HashMap<String, String>();

  /**
   * Creates a new link of type <code>text/css</code>, media <code>all</code>
   * and rel <code>stylesheet</code>.
   * 
   * @param href
   *          the relative link source
   */
  public IncludeImpl(String href) {
    this(href, TYPE_CSS, MEDIA_ALL, REL_CSS, null, null, null);
  }

  /**
   * Creates a new link with media <code>all</code> and rel
   * <code>stylesheet</code>.
   * 
   * @param href
   *          the relative link source
   * @param type
   *          the type
   */
  public IncludeImpl(String href, String type) {
    this(href, type, MEDIA_ALL, REL_CSS, null, null, null);
  }

  /**
   * Creates a new link with rel <code>stylesheet</code>.
   * 
   * @param href
   *          the relative link source
   * @param type
   *          the type
   */
  public IncludeImpl(String href, String type, String media) {
    this(href, type, media, REL_CSS, null, null, null);
  }

  /**
   * Creates a new link.
   * 
   * @param href
   *          the relative link source
   * @param type
   *          the type
   * @param media
   *          the target media
   * @param rel
   *          // TODO
   * @param title
   *          the title
   * @param rev
   *          // TODO
   * @param charset
   *          the character set
   */
  public IncludeImpl(String href, String type, String media, String rel,
      String title, String rev, String charset) {
    addAttribute("media", media);
    addAttribute("rel", rel);
    addAttribute("rev", rev);
    addAttribute("type", type);
    addAttribute("title", title);
    addAttribute("charset", charset);
    this.href = href;
    if (href == null)
      throw new ConfigurationException("source path of link definition is mandatory!");
  }

  /**
   * Creates a new link.
   * 
   * @param node
   *          the configuration node
   * @param xpath
   *          the xpath engine
   */
  public IncludeImpl(Node node, XPath xpath) {
    addAttribute("media", XPathHelper.valueOf(node, "@media", "all", xpath));
    addAttribute("rel", XPathHelper.valueOf(node, "@rel", "stylesheet", xpath));
    addAttribute("rev", XPathHelper.valueOf(node, "@rev", xpath));
    addAttribute("type", XPathHelper.valueOf(node, "@type", "text/css", xpath));
    addAttribute("title", XPathHelper.valueOf(node, "@title", xpath));
    addAttribute("charset", XPathHelper.valueOf(node, "@charset", xpath));
    href = XPathHelper.valueOf(node, "text()", xpath);
    if (href == null)
      throw new ConfigurationException("source path of link definition is mandatory!");
  }

  /**
   * @see ch.o2it.weblounge.api.module.ModuleInclude#setModule(ch.o2it.weblounge.common.site.Module)
   */
  public void setModule(Module m) {
    if (href == null)
      href = m.getVirtualPath(href, true);
  }

  /**
   * Adds this attribute to the list of attributes.
   * 
   * @param key
   *          the attribute name
   * @param value
   *          the value
   */
  private void addAttribute(String key, String value) {
    if (value != null)
      attributes.put(key, value);
  }

  /**
   * Returns <code>true</code> if <code>o</code> is a <code>Link</code> as well
   * pointing to the same resource.
   */
  public boolean equals(Object o) {
    if (o instanceof IncludeImpl) {
      IncludeImpl l = (IncludeImpl) o;
      return href.equals(l.href) && attributes.equals(l.attributes);
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ch.o2it.weblounge.core.common.url.Include#toString()
   */
  public String toString() {
    StringBuilder sb = new StringBuilder("<link ");

    // The source
    sb.append("href=\"");
    sb.append(href);
    sb.append("\" ");

    // Other attributes
    for (Map.Entry<String, String> e : attributes.entrySet()) {
      if (e.getValue() != null) {
        sb.append(e.getKey());
        sb.append("=\"");
        sb.append(e.getValue());
        sb.append("\" ");
      }
    }

    sb.append("></link>");
    return sb.toString();
  }

}