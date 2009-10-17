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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.WebloungeDateFormat;
import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;

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
 * 	&lt;publish&lt;
 * 		&lt;from&lt;&lt;/from&lt;
 * 		&lt;to&lt;&lt;/to&lt;
 * 	&lt;/publish&lt;
 * </pre>
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class PublishingContextImpl implements PublishingContext, Cloneable {

  /** Context identifier */
  private String id_;

  /** Start date */
  private Date from_;

  /** End date */
  private Date to_;

  /**
   * Creates a default publishing context with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   */
  public PublishingContextImpl() {
    this("<default>");
  }

  /**
   * Creates a default publishing context with the given name and initially no
   * restrictions.
   * 
   * @param identifier
   *          the context identifier
   */
  public PublishingContextImpl(String identifier) {
    id_ = (identifier != null) ? identifier : "<default>";
    from_ = new Date();
    to_ = null;
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
   * Returns the publishing start date.
   * 
   * @return the publishing start date
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
   * Returns the publishing end date.
   * 
   * @return the publishing end date
   */
  public Date getPublishTo() {
    return to_;
  }

  /**
   * Checks whether the item attached to this context may be published on the
   * given date or not.
   * 
   * @param date
   *          the current date
   * @return <code>true</code> if the item may be published
   */
  public boolean check(Date date) {
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
  public void init(XPath path, Node context) {
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
    PublishingContextImpl ctxt = new PublishingContextImpl(id_);
    ctxt.from_ = from_;
    ctxt.to_ = to_;
    return ctxt;
  }

  /**
   * Returns the string representation of this context which is equal to the
   * context identifier.
   * 
   * @return the context identifier
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return id_;
  }

  /**
   * Returns an XML representation of this context.
   * 
   * @return an XML representation of this context
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<publish>");
    if (from_ == null)
      b.append("<from/>");
    else {
      b.append("<from>");
      b.append(WebloungeDateFormat.formatStatic(from_));
      b.append("</from>");
    }
    if (to_ == null)
      b.append("<to/>");
    else {
      b.append("<to>");
      b.append(WebloungeDateFormat.formatStatic(to_));
      b.append("</to>");
    }
    b.append("</publish>");
    return b.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PublishingContext#isPublished()
   */
  public boolean isPublished() {
    return check(new Date());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PublishingContext#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    return check(date);
  }

}