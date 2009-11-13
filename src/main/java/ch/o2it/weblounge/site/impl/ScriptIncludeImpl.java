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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.ScriptInclude;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;

/**
 * This class encapsulates the information to include a script within the
 * &lt;head&gt; section of an html page. Relative paths are interpreted to be
 * relative to the weblounge folder <code>shared</code>.
 */
public class ScriptIncludeImpl implements ScriptInclude {

  /** The default script type */
  protected final static String TYPE_JAVASCRIPT = "text/javascript";

  /** Source */
  protected String href = null;

  /** The attributes collection */
  private Map<String, String> attributes = new HashMap<String, String>();

  /**
   * Creates a new <code>ScriptInclude</code> object with a default type of
   * <code>text/javascript</code>.
   * 
   * @param href
   *          the path to the script
   */
  public ScriptIncludeImpl(String href) {
    this(href, TYPE_JAVASCRIPT, null, null, null);
  }

  /**
   * Creates a new <code>ScriptInclude</code> object.
   * 
   * @param href
   *          the path to the script
   * @param type
   *          the type, e. g. <code>text/javascript</code>
   */
  public ScriptIncludeImpl(String href, String type) {
    this(href, type, null, null, null);
  }

  /**
   * Creates a new <code>ScriptInclude</code> object.
   * 
   * @param href
   *          the path to the script
   * @param type
   *          the type, e. g. <code>text/javascript</code>
   * @param defer
   *          defer
   */
  public ScriptIncludeImpl(String href, String type, String defer) {
    this(href, type, defer, null, null);
  }

  /**
   * Creates a new <code>ScriptInclude</code> object.
   * 
   * @param href
   *          the path to the script
   * @param type
   *          the type, e. g. <code>text/javascript</code>
   * @param defer
   *          defer
   * @param language
   *          the language
   * @param charset
   *          the charset
   */
  public ScriptIncludeImpl(String href, String type, String defer,
      String language, String charset) {
    addAttribute("defer", defer);
    addAttribute("language", language);
    addAttribute("type", type);
    addAttribute("charset", charset);
  }

  /**
   * Creates a new link.
   * 
   * @param node
   *          the configuration node
   * @param xpath
   *          the xpath engine
   */
  public ScriptIncludeImpl(Node node, XPath xpath) {
    addAttribute("defer", XPathHelper.valueOf(node, "@defer", xpath));
    addAttribute("language", XPathHelper.valueOf(node, "@language", xpath));
    addAttribute("type", XPathHelper.valueOf(node, "@type", "text/css", xpath));
    addAttribute("charset", XPathHelper.valueOf(node, "@charset", xpath));
    href = XPathHelper.valueOf(node, "text()", xpath);
    if (href == null)
      throw new ConfigurationException("Source path of script definition is mandatory!");
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
   * @see ch.o2it.weblounge.api.module.ModuleScript#setModule(ch.o2it.weblounge.common.site.Module)
   */
  public void setModule(Module m) {
    if (href == null)
      href = m.getVirtualPath(href, true);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return (href != null) ? href.hashCode() : super.hashCode();
  }

  /**
   * Returns <code>true</code> if <code>o</code> is a <code>ScriptInclude</code>
   * as well pointing to the same script.
   */
  public boolean equals(Object o) {
    if (o instanceof ScriptIncludeImpl) {
      ScriptIncludeImpl s = (ScriptIncludeImpl) o;
      return href.equals(s.href) && attributes.equals(s.attributes);
    }
    return false;
  }

  /**
   * Returns the string representation of this element.
   */
  public String toString() {
    StringBuilder sb = new StringBuilder("<script ");

    // The source
    sb.append("src=\"");
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

    sb.append("></script>");
    return sb.toString();
  }

}