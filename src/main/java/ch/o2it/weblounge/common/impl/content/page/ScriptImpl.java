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

package ch.o2it.weblounge.common.impl.content.page;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.content.page.DeclarativeHTMLHeadElement;
import ch.o2it.weblounge.common.content.page.Script;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This class encapsulates the information to include a script within the
 * &lt;head&gt; section of an <code>HTML</code> page. Relative paths are
 * interpreted to be relative to the weblounge folder <code>shared</code>.
 */
public class ScriptImpl implements Script, DeclarativeHTMLHeadElement {

  /** Default character set */
  public static final String DEFAULT_CHARSET = "iso-8859-1";

  /** Source */
  protected String href = null;

  /** Script type */
  protected String type = null;

  /** The character set */
  protected String charset = null;

  /** True to wait with the execution of the script until the page was loaded */
  protected boolean defer = false;

  /**
   * Creates a new <code>Script</code> object with the script's path and
   * character set given and a default character set of <code>iso-8859-1</code>.
   * 
   * @param href
   *          the path to the script
   */
  public ScriptImpl(String href) {
    this(href, null, DEFAULT_CHARSET, false);
  }

  /**
   * Creates a new <code>Script</code> object with the given path the script and
   * a default character set of <code>iso-8859-1</code>.
   * 
   * @param href
   *          the path to the script
   * @param type
   *          the type, e. g. <code>text/javascript</code>
   */
  public ScriptImpl(String href, String type) {
    this(href, type, DEFAULT_CHARSET, false);
  }

  /**
   * Creates a new <code>Script</code> object.
   * 
   * @param href
   *          the path to the script
   * @param type
   *          the type, e. g. <code>text/javascript</code>
   * @param charset
   *          the character set, e. g. <code>utf-8</code>
   */
  public ScriptImpl(String href, String type, String charset) {
    this(href, type, charset, false);
  }

  /**
   * Creates a new <code>Script</code> object.
   * 
   * @param href
   *          the path to the script
   * @param type
   *          the type, e. g. <code>text/javascript</code>
   * @param charset
   *          the character set, e. g. <code>utf-8</code>
   * @param defer
   *          defer, e. g. <code>true</code>
   */
  public ScriptImpl(String href, String type, String charset, boolean defer) {
    this.href = href;
    this.defer = defer;
    this.type = type;
    this.charset = charset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.DeclarativeHTMLHeadElement#configure(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.site.Site,
   *      ch.o2it.weblounge.common.site.Module)
   */
  public void configure(WebloungeRequest request, Site site, Module module)
      throws IllegalStateException {
    if (href != null && href.matches(".*\\$\\{.*\\}.*")) {
      href = ConfigurationUtils.processTemplate(href, module);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.HTMLHeadElement#getHref()
   */
  public String getHref() {
    return href;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Script#setType(java.lang.String)
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Script#getType()
   */
  public String getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Script#setCharset(java.lang.String)
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Script#getCharset()
   */
  public String getCharset() {
    return charset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Script#setDeferred(boolean)
   */
  public void setDeferred(boolean deferred) {
    this.defer = deferred;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Script#isDeferred()
   */
  public boolean isDeferred() {
    return defer;
  }

  /**
   * Initializes this script include from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param config
   *          the script node
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static ScriptImpl fromXml(Node config) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(config, xpath);
  }

  /**
   * Initializes this script include from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param config
   *          the script node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #toXml()
   */
  public static ScriptImpl fromXml(Node config, XPath xpathProcessor)
      throws IllegalStateException {

    String href = XPathHelper.valueOf(config, "@src", xpathProcessor);
    if (href == null)
      throw new ConfigurationException("Source path of script definition is mandatory!");

    ScriptImpl script = new ScriptImpl(href);
    script.setType(XPathHelper.valueOf(config, "@type", xpathProcessor));
    script.setCharset(XPathHelper.valueOf(config, "@charset", xpathProcessor));
    script.setDeferred("true".equalsIgnoreCase(XPathHelper.valueOf(config, "@defer", xpathProcessor)));

    return script;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Script#toXml()
   */
  public String toXml() {
    StringBuilder sb = new StringBuilder("<script ");

    // The source
    sb.append("src=\"");
    sb.append(href);
    sb.append("\"");

    // type
    if (type != null) {
      sb.append(" type=\"");
      sb.append(type);
      sb.append("\"");
    }

    // character set
    if (charset != null) {
      sb.append(" charset=\"");
      sb.append(charset);
      sb.append("\"");
    }

    // defer
    if (defer) {
      sb.append(" defer=\"true\"");
    }

    sb.append("></script>"); // self closing script tags not allowed in HTML
    return sb.toString();
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
    if (o instanceof Script) {
      Script s = (Script) o;
      return href.equals(s.getHref());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuilder sb = new StringBuilder("script [");

    // The source
    sb.append("src=");
    sb.append(href);

    // type
    if (type != null) {
      sb.append(";type=");
      sb.append(type);
    }

    // character set
    if (charset != null) {
      sb.append(";charset=");
      sb.append(charset);
    }

    // defer
    if (defer) {
      sb.append(";defer=true");
    }

    sb.append("]");
    return sb.toString();
  }

}