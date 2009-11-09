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

import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.user.User;

import org.w3c.dom.Node;

import java.text.ParseException;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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
  protected User publisher = null;

  /** Start date */
  protected Date startDate = null;

  /** End date */
  protected Date endDate = null;

  /**
   * Creates a default publishing context with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   */
  public PublishingContextImpl() {
    startDate = new Date();
  }

  /**
   * Creates a default publishing context with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   * 
   * @param publisher
   *          the user publishing the content
   */
  public PublishingContextImpl(User publisher) {
    this.publisher = publisher;
    startDate = new Date();
  }

  /**
   * Creates a default publishing context with no restrictions and a context
   * identifier of <tt>&lt;default&gt;</tt>.
   * 
   * @param publisher
   *          the user publishing the content
   * @param startDate
   *          publication start date
   * @param endDate
   *          publication end date
   */
  public PublishingContextImpl(User publisher, Date startDate, Date endDate) {
    this.publisher = publisher;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * Sets the user that published or unpublished the page.
   * 
   * @param user
   *          the publisher
   */
  public void setPublisher(User user) {
    publisher = user;
  }

  /**
   * Returns the publisher.
   * 
   * @return the publisher
   */
  public User getPublisher() {
    return publisher;
  }

  /**
   * Sets the publishing start date. Pass <code>null</code> to set no start date
   * at all.
   * 
   * @param from
   *          the start date
   */
  public void setPublishFrom(Date from) {
    this.startDate = from;
  }

  /**
   * @see ch.o2it.weblounge.common.content.PublishingContext#getPublishFrom()
   */
  public Date getPublishFrom() {
    return startDate;
  }

  /**
   * Sets the publishing end date. Pass <code>null</code> to set no end date at
   * all.
   * 
   * @param to
   *          the end date
   */
  public void setPublishTo(Date to) {
    this.endDate = to;
  }

  /**
   * @see ch.o2it.weblounge.common.content.PublishingContext#getPublishTo()
   */
  public Date getPublishTo() {
    return endDate;
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
    boolean checkFrom = startDate == null || startDate.before(date);
    boolean checkTo = endDate == null || endDate.after(date);
    return (checkFrom && checkTo);
  }

  /**
   * Creates a publishing context from an <code>XML</code> node.
   * 
   * @param context
   *          the publish context node
   * @throws IllegalArgumentException
   *           if either the publishing start date or the publishing end date
   *           found in this context cannot be parsed
   */
  public static PublishingContextImpl fromXml(Node context)
      throws IllegalArgumentException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Creates a publishing context from an <code>XML</code> node.
   * 
   * @param context
   *          the publish context node
   * @param xpathProcessor
   *          the xpath processor
   * @throws IllegalArgumentException
   *           if either the publishing start date or the publishing end date
   *           found in this context cannot be parsed
   */
  public static PublishingContextImpl fromXml(Node context, XPath xpathProcessor)
      throws IllegalArgumentException {

    // Look up the root node
    Node contextRoot = XPathHelper.select(context, "//published", xpathProcessor);
    if (contextRoot == null)
      return null;

    PublishingContextImpl ctx = new PublishingContextImpl();

    // Publisher
    Node publisher = XPathHelper.select(contextRoot, "//published/user", xpathProcessor);
    if (publisher == null)
      throw new IllegalStateException("Publisher cannot be null");
    ctx.setPublisher(UserImpl.fromXml(publisher, xpathProcessor));

    // Start date
    String startDate = XPathHelper.valueOf(contextRoot, "//published/from", xpathProcessor);
    if (startDate == null)
      throw new IllegalStateException("Publishing start date cannot be null");
    try {
      ctx.setPublishFrom(WebloungeDateFormat.parseStatic(startDate));
    } catch (ParseException e) {
      throw new IllegalArgumentException("The publishing start date '" + startDate + "' cannot be parsed", e);
    }

    // End date
    String endDate = XPathHelper.valueOf(contextRoot, "//published/to", xpathProcessor);
    if (endDate != null) {
      try {
        ctx.setPublishTo(WebloungeDateFormat.parseStatic(endDate));
      } catch (ParseException e) {
        throw new IllegalArgumentException("The publishing end date '" + endDate + "' cannot be parsed", e);
      }
    }

    return ctx;
  }

  /**
   * Returns a copy of this publishing context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    PublishingContextImpl ctxt = new PublishingContextImpl();
    if (publisher != null)
      ctxt.publisher = (User) publisher.clone();
    if (startDate != null)
      ctxt.startDate = (Date) startDate.clone();
    if (endDate != null)
      ctxt.endDate = (Date) endDate.clone();
    return ctxt;
  }

  /**
   * Returns an XML representation of this context.
   * 
   * @return an XML representation of this context
   */
  public String toXml() {
    if (publisher == null || startDate == null)
      return "";

    StringBuffer b = new StringBuffer();
    b.append("<published>");
    b.append(publisher.toXml());
    
    b.append("<from>");
    b.append(WebloungeDateFormat.formatStatic(startDate));
    b.append("</from>");

    if (endDate != null) {
      b.append("<to>");
      b.append(WebloungeDateFormat.formatStatic(endDate));
      b.append("</to>");
    }

    b.append("</published>");
    return b.toString();
  }

}