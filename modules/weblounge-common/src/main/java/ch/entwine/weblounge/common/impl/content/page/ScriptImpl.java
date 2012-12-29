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

import ch.entwine.weblounge.common.ConfigurationException;
import ch.entwine.weblounge.common.content.page.Script;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This class encapsulates the information to include a script within the
 * &lt;head&gt; section of an <code>HTML</code> page. Relative paths are
 * interpreted to be relative to the weblounge folder <code>shared</code>.
 */
public class ScriptImpl implements Script {

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

  /** Element usage scenario */
  protected Use use = null;

  /** The jquery version to use in for the script */
  protected String jquery = null;

  /** The site */
  protected Site site = null;

  /** The module */
  protected Module module = null;

  /**
   * Creates a new <code>Script</code> object with the script's path and
   * character set given and a default character set of <code>iso-8859-1</code>.
   * 
   * @param href
   *          the path to the script
   */
  public ScriptImpl(String href) {
    this(href, null, DEFAULT_CHARSET, null, false);
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
    this(href, type, DEFAULT_CHARSET, null, false);
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
  public ScriptImpl(String href, String type, String jquery, String charset) {
    this(href, type, charset, jquery, false);
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
  public ScriptImpl(String href, String type, String charset, String jquery,
      boolean defer) {
    this.href = href;
    this.defer = defer;
    this.type = type;
    this.charset = charset;
    this.jquery = jquery;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.DeclarativeHTMLHeadElement#setSite(ch.entwine.weblounge.common.site.Site)
   */
  public void setSite(Site site) {
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    this.site = site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.DeclarativeHTMLHeadElement#setModule(ch.entwine.weblounge.common.site.Module)
   */
  public void setModule(Module module) {
    if (module == null)
      throw new IllegalArgumentException("Module must not be null");
    this.module = module;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.HTMLHeadElement#setEnvironment(ch.entwine.weblounge.common.site.Environment)
   */
  public void setEnvironment(Environment environment) {
    if (href == null || !href.matches(".*\\$\\{.*\\}.*"))
      return;

    // The module may be null, but the site must not
    if (site == null)
      throw new IllegalStateException("Site must not be null");

    if (module != null) {
      href = ConfigurationUtils.processTemplate(href, module, environment);
    } else {
      href = ConfigurationUtils.processTemplate(href, site, environment);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.HTMLHeadElement#getUse()
   */
  public Use getUse() {
    return use != null ? use : Use.All;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.HTMLHeadElement#setUse(ch.entwine.weblounge.common.content.page.HTMLInclude.Use)
   */
  public void setUse(Use use) {
    this.use = use;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.HTMLHeadElement#getHref()
   */
  public String getHref() {
    return href;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Script#setType(java.lang.String)
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Script#getType()
   */
  public String getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Script#setCharset(java.lang.String)
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Script#getCharset()
   */
  public String getCharset() {
    return charset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Script#setJQuery(java.lang.String)
   */
  public void setJQuery(String jquery) {
    this.jquery = jquery;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Script#getJQuery()
   */
  public String getJQuery() {
    return jquery;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Script#setDeferred(boolean)
   */
  public void setDeferred(boolean deferred) {
    this.defer = deferred;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Script#isDeferred()
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
    script.setJQuery(XPathHelper.valueOf(config, "@jquery", xpathProcessor));
    script.setDeferred("true".equalsIgnoreCase(XPathHelper.valueOf(config, "@defer", xpathProcessor)));

    String use = XPathHelper.valueOf(config, "@use", xpathProcessor);
    if (StringUtils.isNotBlank(use)) {
      script.setUse(Use.valueOf(StringUtils.capitalize(use)));
    }

    return script;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.HTMLHeadElement#toHtml()
   */
  @Override
  public String toHtml() {
    return toXml(false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.HTMLHeadElement#toXml()
   */
  public String toXml() {
    return toXml(true);
  }

  /**
   * Returns the <code>XML</code> representation of this script, optionally
   * skipping attributes that are not part of the <code>HTML</code> standard.
   * 
   * @param includeNonHtmlAttributes
   *          <code>true</code> to include non <code>HTML</code> attributes
   * @return the xml representation
   */
  private String toXml(boolean includeNonHtmlAttributes) {
    StringBuilder sb = new StringBuilder("<script");

    // jquery
    if (jquery != null) {
      sb.append(" jquery=\"");
      sb.append(jquery);
      sb.append("\"");
    }

    // use
    if (includeNonHtmlAttributes && use != null) {
      sb.append(" use=\"");
      sb.append(use.toString().toLowerCase());
      sb.append("\"");
    }

    // The source
    sb.append(" src=\"");
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

    // jquery
    if (jquery != null) {
      sb.append(";jquery=");
      sb.append(jquery);
    }

    // use
    if (use != null) {
      sb.append(";use=");
      sb.append(use.toString().toLowerCase());
    }

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