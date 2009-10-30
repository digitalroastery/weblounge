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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Node;

import java.util.Date;

import javax.xml.xpath.XPath;

/**
 * This class models the publishing constraints that apply to an arbitrary
 * object in the system.
 * <p>
 * A publishing context definition contains information on the start and end
 * date of the publishing time. The context usually looks like follows:
 * 
 * <pre>
 * 	&lt;published&lt;
 * 		&lt;user&gt;tobias.wunden&lt;/user&gt;
 * 		&lt;from&gt;2006/05/11 10:42:46 GMT&lt;/from&gt;
 * 		&lt;to&gt;2006/06/11 00:00:00 GMT&lt;/to&gt;
 * 	&lt;/published&gt;
 * </pre>
 */
public class PublishingContextImpl implements PublishingContext {

  /** Publisher */
  private User publisher_ = null;

  /** Start date */
  private Date from_ = null;

  /** End date */
  private Date to_ = null;

  /**
   * Creates a default publishing context with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   */
  public PublishingContextImpl() {
    publisher_ = null;
    from_ = new Date();
    to_ = null;
  }

  /**
   * Sets the user that published or unpublished the page.
   * 
   * @param user
   *          the publisher
   */
  public void setPublisher(User user) {
    publisher_ = user;
  }

  /**
   * Returns the publisher.
   * 
   * @return the publisher
   */
  public User getPublisher() {
    return publisher_;
  }

  /**
   * Sets the publishing start date. Pass <code>null</code> to set no start date
   * at all.
   * 
   * @param from
   *          the start date
   */
  public void setPublishFrom(Date from) {
    from_ = from;
  }

  /**
   * @see ch.o2it.weblounge.common.content.PublishingContext#getPublishFrom()
   */
  public Date getPublishFrom() {
    return from_;
  }

  /**
   * Sets the publishing end date. Pass <code>null</code> to set no end date at
   * all.
   * 
   * @param to
   *          the end date
   */
  public void setPublishTo(Date to) {
    to_ = to;
  }

  /**
   * @see ch.o2it.weblounge.common.content.PublishingContext#getPublishTo()
   */
  public Date getPublishTo() {
    return to_;
  }

  /**
   * @see ch.o2it.weblounge.common.content.PublishingContext#isPublished()
   */
  public boolean isPublished() {
    return isPublished(new Date());
  }

  /**
   * @see ch.o2it.weblounge.common.content.PublishingContext#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    Arguments.checkNull(date, "date");
    boolean checkFrom = from_ == null || from_.before(date);
    boolean checkTo = to_ == null || to_.after(date);
    return (checkFrom && checkTo);
  }

  /**
   * Initializes this context from an xml node.
   * 
   * @param context
   *          the publish context node
   */
  public void init(XPath path, Node context, Site site) {
    try {
      Node publisher = XPathHelper.select(path, context, "//publish/user");
      publisher_ = (publisher != null) ? site.getUsers().getUser(publisher.getNodeValue()) : null;
    } catch (Exception e) {
    }
    try {
      Node from = XPathHelper.select(path, context, "//publish/from");
      from_ = (from != null) ? WebloungeDateFormat.parseStatic(from.getNodeValue()) : null;
    } catch (Exception e) {
    }
    try {
      Node to = XPathHelper.select(path, context, "//publish/to");
      to_ = (to != null) ? WebloungeDateFormat.parseStatic(to.getNodeValue()) : null;
    } catch (Exception e) {
    }
  }

  /**
   * Returns a copy of this publishing context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    PublishingContextImpl ctxt = new PublishingContextImpl();
    ctxt.from_ = from_;
    ctxt.to_ = to_;
    return ctxt;
  }

  /**
   * Returns an XML representation of this context.
   * 
   * @return an XML representation of this context
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<published>");
    if (publisher_ != null) {
      b.append("<user>");
      b.append(publisher_.getLogin());
      b.append("</user>");
    }
    if (from_ == null) {
      b.append("<from/>");
    } else {
      b.append("<from>");
      b.append(WebloungeDateFormat.formatStatic(from_));
      b.append("</from>");
    }
    if (to_ != null) {
      b.append("<to>");
      b.append(WebloungeDateFormat.formatStatic(to_));
      b.append("</to>");
    }
    b.append("</published>");
    return b.toString();
  }

}