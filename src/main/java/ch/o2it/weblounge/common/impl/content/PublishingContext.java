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

import ch.o2it.weblounge.common.content.Publishable;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.user.User;

import org.w3c.dom.Node;

import java.text.ParseException;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * The publishing context contains information about when an object will be
 * published and thus be openly accessible. It can be used by
 * <code>Publishable</code> objects as the backing implementation.
 * <p>
 * The publishing context adds additional means of specifying and querying
 * publisher and publishing start and end date. It also allows for easy
 * serialization and deserialization of <code>Publishable</code> data.
 * <p>
 * Following is an example of the data structure that the publishing context is
 * able to handle:
 * 
 * <pre>
 * &lt;published&gt;
 *   &lt;user id="john" realm="testland"&gt;John Doe&lt;/user&gt;
 *   &lt;from&gt;2009/11/06 08:52:52 GMT&lt;/from&gt;
 *   &lt;to&gt;2010/11/06 23:59:59 GMT&lt;/to&gt;
 * &lt;/published&gt;
 * </pre>
 * 
 * @see Publishable
 */
public class PublishingContext implements Cloneable {

  /** Publisher */
  protected User publisher = null;

  /** Start date */
  protected Date startDate = null;

  /** End date */
  protected Date endDate = null;

  /**
   * Creates a default publishing context with a publishing start date of
   * <code>now</code> (or <code>new Date()</code>, for that matter).
   */
  public PublishingContext() {
    startDate = new Date();
  }

  /**
   * Creates a default publishing context with a publishing start date of
   * <code>now</code> (or <code>new Date()</code>, for that matter) and
   * <code>publisher</code> as the publishing user.
   * 
   * @param publisher
   *          the user publishing the content
   */
  public PublishingContext(User publisher) {
    this.publisher = publisher;
    startDate = new Date();
  }

  /**
   * Creates a default publishing context with the given publishing start and
   * end date and <code>publisher</code> as the publishing user.
   * 
   * @param publisher
   *          the user publishing the content
   * @param startDate
   *          publication start date
   * @param endDate
   *          publication end date
   */
  public PublishingContext(User publisher, Date startDate, Date endDate) {
    this.publisher = publisher;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * Sets the publisher, start and end date of this context. Throws an
   * {@link IllegalArgumentException} if <code>startDate</code> is the same as
   * or after <code>endDate</code>.
   * 
   * @param publisher
   *          the publishing user
   * @param startDate
   *          the publishing start date
   * @param endDate
   *          the publishing end date
   * @throws IllegalArgumentException
   *           if the start date is after the end date
   */
  public void setPublished(User publisher, Date startDate, Date endDate)
      throws IllegalStateException {
    if (startDate.after(endDate))
      throw new IllegalArgumentException("Start date is after end date");
    this.publisher = publisher;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * Sets the user that published the object.
   * 
   * @param user
   *          the publisher
   */
  public void setPublisher(User user) {
    publisher = user;
  }

  /**
   * Returns the user that published the object.
   * 
   * @return the publisher
   */
  public User getPublisher() {
    return publisher;
  }

  /**
   * Sets the publishing start date.
   * <p>
   * Pass <code>null</code> to set no start date at all, meaning that the object
   * will be published until the end date is reached. If <code>startDate</code>
   * is after the current end date, an {@link IllegalArgumentException} is
   * thrown.
   * 
   * @param startDate
   *          the start date
   * @throw IllegalArgumentException if <code>startDate</code> is after the
   *        current end date
   */
  public void setPublishFrom(Date startDate) {
    if (startDate != null && endDate != null && startDate.after(endDate))
      throw new IllegalArgumentException("Start date is after end date");
    this.startDate = startDate;
  }

  /**
   * Returns the publishing start date. A value of <code>null</code> means that
   * this object will be published until the publishing end date is reached.
   * 
   * @return the publishing end date
   * @see #getPublishTo()
   */
  public Date getPublishFrom() {
    return startDate;
  }

  /**
   * Sets the publishing end date.
   * <p>
   * Pass <code>null</code> to set no end date at all, meaning that the object
   * will be published forever starting on the current start date. If
   * <code>endDate</code> is before the current start date, an
   * {@link IllegalArgumentException} is thrown.
   * 
   * @param endDate
   *          the end date
   * @throw IllegalArgumentException if <code>endDate</code> is before the
   *        current start date
   */
  public void setPublishTo(Date endDate) {
    if (startDate != null && endDate != null && endDate.before(startDate))
      throw new IllegalArgumentException("End date is before start date");
    this.endDate = endDate;
  }

  /**
   * Returns the publishing end date. A value of <code>null</code> means that
   * this object will be published forever.
   * 
   * @return the publishing end date
   * @see #getPublishFrom()
   */
  public Date getPublishTo() {
    return endDate;
  }

  /**
   * Returns <code>true</code> if the object is published with respect to the
   * current date, that is <code>new Date()</code> is after the current start
   * date and before the current end date.
   * 
   * @return <code>true</code> if the object is currently published
   */
  public boolean isPublished() {
    return isPublished(new Date());
  }

  /**
   * Returns <code>true</code> if the object is published with respect to
   * <code>date</code>, that is <code>date</code> is after the current start
   * date and before the current end date.
   * 
   * @return <code>true</code> if the object is published on the indicated date
   */
  public boolean isPublished(Date date) {
    if (date == null)
      throw new IllegalArgumentException("Date cannot be null");
    boolean checkFrom = startDate == null || startDate.before(date);
    boolean checkTo = endDate == null || endDate.after(date);
    return (checkFrom && checkTo);
  }

  /**
   * Returns a copy of this publishing context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() throws CloneNotSupportedException {
    PublishingContext ctxt = (PublishingContext) super.clone();
    if (publisher != null)
      ctxt.publisher = (User) publisher.clone();
    if (startDate != null)
      ctxt.startDate = (Date) startDate.clone();
    if (endDate != null)
      ctxt.endDate = (Date) endDate.clone();
    return ctxt;
  }

  /**
   * Initializes this context from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param context
   *          the publish context node
   * @throws IllegalStateException
   *           if the context cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static PublishingContext fromXml(Node context)
      throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this context from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param context
   *          the publish context node
   * @param xpathProcessor
   *          the xpath processor
   * @throws IllegalStateException
   *           if the context cannot be parsed
   * @see #toXml()
   */
  public static PublishingContext fromXml(Node context, XPath xpathProcessor)
      throws IllegalStateException {

    // Look up the root node
    Node contextRoot = XPathHelper.select(context, "/published", xpathProcessor);
    if (contextRoot == null)
      return null;

    PublishingContext ctx = new PublishingContext();

    // Publisher
    Node publisher = XPathHelper.select(contextRoot, "/published/user", xpathProcessor);
    if (publisher != null)
      ctx.setPublisher(UserImpl.fromXml(publisher, xpathProcessor));

    // Start date
    String startDate = XPathHelper.valueOf(contextRoot, "/published/from", xpathProcessor);
    if (startDate != null) {
      try {
        ctx.setPublishFrom(WebloungeDateFormat.parseStatic(startDate));
      } catch (ParseException e) {
        throw new IllegalStateException("The publishing start date '" + startDate + "' cannot be parsed", e);
      }
    }

    // End date
    String endDate = XPathHelper.valueOf(contextRoot, "/published/to", xpathProcessor);
    if (endDate != null) {
      try {
        ctx.setPublishTo(WebloungeDateFormat.parseStatic(endDate));
      } catch (ParseException e) {
        throw new IllegalStateException("The publishing end date '" + endDate + "' cannot be parsed", e);
      }
    }

    return ctx;
  }

  /**
   * Returns an <code>XML</code> representation of the context, that will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;published&gt;
   *   &lt;user id="john" realm="testland"&gt;John Doe&lt;/user&gt;
   *   &lt;from&gt;2009/11/06 08:52:52 GMT&lt;/from&gt;
   *   &lt;to&gt;2010/11/06 23:59:59 GMT&lt;/to&gt;
   * &lt;/published&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>PublishingContext</code> from the serialized output of this method.
   * 
   * @return the <code>XML</code> representation of the context
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
   */
  public String toXml() {
    if (publisher == null || startDate == null)
      return "";

    StringBuffer b = new StringBuffer();
    b.append("<published>");

    // Publisher
    if (publisher != null)
      b.append(publisher.toXml());

    // Start date
    if (startDate != null) {
      b.append("<from>");
      b.append(WebloungeDateFormat.formatStatic(startDate));
      b.append("</from>");
    }

    // End date
    if (endDate != null) {
      b.append("<to>");
      b.append(WebloungeDateFormat.formatStatic(endDate));
      b.append("</to>");
    }

    b.append("</published>");
    return b.toString();
  }

}