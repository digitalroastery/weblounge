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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.content.DeclarativeHTMLHeadElement;
import ch.o2it.weblounge.common.content.Link;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This class encapsulates the information to include a link element within the
 * &lt;head&gt; section of an <code>HTML</code> page.
 */
public class LinkImpl implements Link, DeclarativeHTMLHeadElement {

  /** Source */
  protected String href = null;

  /** The kind of device that this link is displayed on */
  protected String media = null;
  
  /** Relationship between this document and the linked one */
  protected String relation = null;

  /** Relationship between the linked document and the current one */
  protected String reverseRelation = null;
  
  /** The mime type */
  protected String type = null;
  
  /** The character set */
  protected String charset = null;

  /**
   * Creates a new link of type <code>text/css</code>, media <code>all</code>
   * and rel <code>stylesheet</code>.
   * 
   * @param href
   *          the relative link source
   */
  public LinkImpl(String href) {
    this(href, TYPE_CSS, MEDIA_ALL, REL_CSS, null, null);
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
  public LinkImpl(String href, String type) {
    this(href, type, MEDIA_ALL, REL_CSS, null, null);
  }

  /**
   * Creates a new link with rel <code>stylesheet</code>.
   * 
   * @param href
   *          the relative link source
   * @param type
   *          the type
   */
  public LinkImpl(String href, String type, String media) {
    this(href, type, media, REL_CSS, null, null);
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
   *          the relation between this document and the linked one
   * @param rev
   *          the relation between the linked document and the this one
   * @param charset
   *          the character set
   */
  public LinkImpl(String href, String type, String media, String rel,
      String rev, String charset) {
    this.media = media;
    this.relation = rel;
    this.reverseRelation = rev;
    this.type = type;
    this.charset = charset;
    this.href = href;
    if (href == null)
      throw new ConfigurationException("source path of link definition is mandatory!");
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.DeclarativeHTMLHeadElement#configure(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.site.Site, ch.o2it.weblounge.common.site.Module)
   */
  public void configure(WebloungeRequest request, Site site, Module module)
      throws IllegalStateException {
    if (href != null && href.matches(".*\\$\\{.*\\}.*")) {
      href = ConfigurationUtils.processTemplate(href, request, module);
    }
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.HTMLHeadElement#getHref()
   */
  public String getHref() {
    return href;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#setCharset(java.lang.String)
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#getCharset()
   */
  public String getCharset() {
    return charset;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#setMedia(java.lang.String)
   */
  public void setMedia(String media) {
    this.media = media;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#getMedia()
   */
  public String getMedia() {
    return media;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#setRelation(java.lang.String)
   */
  public void setRelation(String relation) {
    this.relation = relation;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#getRelation()
   */
  public String getRelation() {
    return relation;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#setReverseRelation(java.lang.String)
   */
  public void setReverseRelation(String relation) {
    this.reverseRelation = relation;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#getReverseRelation()
   */
  public String getReverseRelation() {
    return reverseRelation;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#setType(java.lang.String)
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Link#getType()
   */
  public String getType() {
    return type;
  }

  /**
   * Initializes this include from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param config
   *          the include node
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static LinkImpl fromXml(Node config) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(config, xpath);
  }

  /**
   * Initializes this include from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param config
   *          the include node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the configuration cannot be parsed
   * @see #toXml()
   */
  public static LinkImpl fromXml(Node config, XPath xpathProcessor)
      throws IllegalStateException {

    String href = XPathHelper.valueOf(config, "@href", xpathProcessor);
    if (href == null)
      throw new ConfigurationException("Source path of link definition is mandatory!");

    String charset = XPathHelper.valueOf(config, "@charset", xpathProcessor);
    String media = XPathHelper.valueOf(config, "@media", xpathProcessor);
    String rel = XPathHelper.valueOf(config, "@rel", xpathProcessor);
    String rev = XPathHelper.valueOf(config, "@rev", xpathProcessor);
    String type = XPathHelper.valueOf(config, "@type", xpathProcessor);

    LinkImpl include = new LinkImpl(href, type, media, rel, rev, charset);

    return include;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.HTMLHeadElement#toXml()
   */
  public String toXml() {
    StringBuilder sb = new StringBuilder("<link ");

    // The source
    sb.append("href=\"");
    sb.append(href);
    sb.append("\"");

    // type
    if (type != null) {
      sb.append(" type=\"");
      sb.append(type);
      sb.append("\"");
    }

    // charset
    if (charset != null) {
      sb.append(" charset=\"");
      sb.append(charset);
      sb.append("\"");
    }

    // media
    if (media != null) {
      sb.append(" media=\"");
      sb.append(media);
      sb.append("\"");
    }

    // rel
    if (relation != null) {
      sb.append(" rel=\"");
      sb.append(relation);
      sb.append("\"");
    }

    // rev
    if (reverseRelation != null) {
      sb.append(" rev=\"");
      sb.append(reverseRelation);
      sb.append("\"");
    }

    sb.append("/>");
    return sb.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return href.hashCode();
  }

  /**
   * Returns <code>true</code> if <code>o</code> is a <code>Link</code> as well
   * pointing to the same resource.
   */
  public boolean equals(Object o) {
    if (o instanceof LinkImpl) {
      LinkImpl l = (LinkImpl) o;
      return href.equals(l.href);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuilder sb = new StringBuilder("link [");

    // The source
    sb.append("src=");
    sb.append(href);

    // type
    if (type != null) {
      sb.append(";type=");
      sb.append(type);
    }

    // charset
    if (charset != null) {
      sb.append(";charset=");
      sb.append(charset);
    }

    // media
    if (media != null) {
      sb.append(";media=");
      sb.append(media);
    }

    // rel
    if (relation != null) {
      sb.append(";rel=");
      sb.append(relation);
    }

    // rev
    if (reverseRelation != null) {
      sb.append(";rev=");
      sb.append(reverseRelation);
    }

    sb.append("]");
    return sb.toString();
  }

}