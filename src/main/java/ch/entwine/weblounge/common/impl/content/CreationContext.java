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

package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.security.User;

import org.w3c.dom.Node;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * The creation context contains information about when an object was created
 * and who the creator was. It can be used by <code>Creatable</code> objects as
 * the backing implementation.
 * <p>
 * The context adds additional means of specifying and querying creator and
 * creation date. It also allows for easy serialization and deserialization of
 * <code>Creatable</code> data.
 * <p>
 * Following is an example of the data structure that the modification context
 * is able to handle:
 * 
 * <pre>
 * &lt;created&gt;
 *   &lt;user id="john" realm="testland"&gt;John Doe&lt;/user&gt;
 *   &lt;date&gt;2009/11/06 08:52:52 GMT&lt;/date&gt;
 * &lt;/created&gt;
 * </pre>
 * 
 * @see ch.entwine.weblounge.common.content.Creatable
 */
public class CreationContext implements Cloneable {

  /** Creation date */
  protected Date creationDate = null;

  /** Creator */
  protected User creator = null;

  /**
   * Creates a new and empty modification context.
   */
  public CreationContext() {
    this.creationDate = cutOffMillis(new Date());
  }

  /**
   * Returns the creation date.
   * 
   * @return the creation date
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Returns the user that created the object.
   * 
   * @return the creator
   */
  public User getCreator() {
    return creator;
  }

  /**
   * Sets the creation date and the user who created the object.
   * <p>
   * Note that this method will cut off the millisecond portion of the date.
   * </p>
   * 
   * @param creator
   *          the user creating the object
   * @param creationDate
   *          the date of creation
   */
  public void setCreated(User user, Date date) {
    this.creator = user;
    this.creationDate = cutOffMillis(date);
  }

  /**
   * Sets the creation date.
   * <p>
   * Note that this method will cut off the millisecond portion of the date.
   * </p>
   * 
   * @param date
   *          the creation date
   */
  public void setCreationDate(Date date) {
    this.creationDate = cutOffMillis(date);
  }

  /**
   * Sets the user that created the object.
   * 
   * @param user
   *          the creator
   */
  public void setCreator(User user) {
    this.creator = user;
  }

  /**
   * Returns <code>true</code> if this context was created after the given date.
   * 
   * @param date
   *          the date to compare to
   * @return <code>true</code> is this context was created after the given date
   */
  public boolean isCreatedAfter(Date date) {
    return creationDate != null && creationDate.after(cutOffMillis(date));
  }

  /**
   * Creates a clone of this <code>CreationContext</code>.
   * 
   * @return the cloned creation context
   */
  public Object clone() throws CloneNotSupportedException {
    CreationContext ctxt = (CreationContext) super.clone();
    if (creator != null)
      ctxt.creator = (User) creator.clone();
    if (creationDate != null)
      ctxt.creationDate = (Date) creationDate.clone();
    return ctxt;
  }

  /**
   * Cut off the milliseconds from the date.
   * 
   * @param date
   *          with milliseconds
   * @return date without milliseconds
   */
  private Date cutOffMillis(Date date) {
    if (date == null)
      return null;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Initializes this context from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param context
   *          the creation context node
   * @throws IllegalStateException
   *           if the context cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static CreationContext fromXml(Node context)
      throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this context from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param context
   *          the creation context node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the context cannot be parsed
   * @see #toXml()
   */
  public static CreationContext fromXml(Node context, XPath xpathProcessor)
      throws IllegalStateException {

    Node contextRoot = XPathHelper.select(context, "/created", xpathProcessor);
    if (contextRoot == null)
      return null;

    CreationContext ctx = new CreationContext();

    // Creator
    Node creator = XPathHelper.select(contextRoot, "/created/user", xpathProcessor);
    if (creator != null)
      ctx.setCreator(UserImpl.fromXml(creator, xpathProcessor));

    // Creation date
    String date = XPathHelper.valueOf(contextRoot, "/created/date", xpathProcessor);
    if (date != null)
      try {
        ctx.setCreationDate(WebloungeDateFormat.parseStatic(date));
      } catch (ParseException e) {
        throw new IllegalStateException("The creation date '" + date + "' cannot be parsed", e);
      }
    return ctx;
  }

  /**
   * Returns an <code>XML</code> representation of the context, that will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;created&gt;
   *   &lt;user id="john" realm="testland"&gt;John Doe&lt;/user&gt;
   *   &lt;date&gt;2009/11/06 08:52:52 GMT&lt;/date&gt;
   * &lt;/created&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>CreationContext</code> from the serialized output of this method.
   * 
   * @return the <code>XML</code> representation of the context
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<created>");

    // User
    if (creator != null) {
      b.append("<user");

      // id
      b.append(" id=\"");
      b.append(creator.getLogin());
      b.append("\"");

      // realm
      if (creator.getRealm() != null) {
        b.append(" realm=\"");
        b.append(creator.getRealm());
        b.append("\"");
      }

      b.append(">");

      // name
      if (creator.getName() != null) {
        b.append("<![CDATA[").append(creator.getName()).append("]]>");
      }

      b.append("</user>");
    }

    // Date
    if (creationDate != null) {
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(creationDate));
      b.append("</date>");
    }

    b.append("</created>");
    return b.toString();
  }

}