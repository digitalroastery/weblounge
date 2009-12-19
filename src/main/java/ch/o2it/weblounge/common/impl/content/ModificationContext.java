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

import ch.o2it.weblounge.common.content.Modifiable;
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
 * The modification context contains information about when an object was
 * modified and who the modifier was. It can be used by <code>Modifiable</code>
 * objects as the backing implementation.
 * <p>
 * The context adds additional means of specifying and querying modifier and
 * modification date. It also allows for easy serialization and deserialization
 * of <code>Modifiable</code> data.
 * <p>
 * Following is an example of the data structure that the modification context
 * is able to handle:
 * 
 * <pre>
 * &lt;modified&gt;
 *   &lt;user id="john" realm="testland"&gt;John Doe&lt;/user&gt;
 *   &lt;date&gt;2009/11/06 08:52:52 GMT&lt;/date&gt;
 * &lt;/modified&gt;
 * </pre>
 * 
 * @see Modifiable
 */
public class ModificationContext implements Cloneable {

  /** Modification date */
  protected Date modificationDate = null;

  /** Editor */
  protected User modifier = null;

  /**
   * Creates a modification context with today's date and no modifier.
   */
  public ModificationContext() {
    this(new Date(), null);
  }

  /**
   * Creates a modification context with the given modifier and the modification
   * time reflecting the current date.
   * 
   * @param modifier
   *          the modifying user
   */
  public ModificationContext(User modifier) {
    this(new Date(), modifier);
  }

  /**
   * Creates a modification context reflecting a modification by the specified
   * user and at the given date.
   * 
   * @param date
   *          the modification date
   * @param modifier
   *          the modifying user
   */
  public ModificationContext(Date date, User modifier) {
    this.modificationDate = date;
    this.modifier = modifier;
  }

  /**
   * Returns the modification date.
   * 
   * @return the modification date
   */
  public Date getModificationDate() {
    return modificationDate;
  }

  /**
   * Returns the modifier.
   * 
   * @return the modifier
   */
  public User getModifier() {
    return modifier;
  }

  /**
   * Returns <code>true</code> if the object was modified with respect to the
   * current date (as in <code>new Date()<code>).
   * 
   * @return <code>true</code> if the object was modified
   */
  public boolean isModified() {
    return isModifiedBefore(new Date());
  }

  /**
   * Returns <code>true</code> if the object was modified prior to the given
   * date.
   * 
   * @return <code>true</code> if the object was modified prior to
   *         <code>date</code>
   */
  public boolean isModifiedAfter(Date date) {
    return modificationDate != null && modificationDate.after(date);
  }

  /**
   * Returns <code>true</code> if the object was modified after the given date.
   * 
   * @return <code>true</code> if the object was modified after to
   *         <code>date</code>
   */
  public boolean isModifiedBefore(Date date) {
    return modificationDate != null && modificationDate.before(date);
  }

  /**
   * Sets the modifier along with the modification date.
   * 
   * @param user
   *          the modifier
   * @param date
   *          the modification date
   */
  public void setModified(User user, Date date) {
    this.modifier = user;
    this.modificationDate = date;
  }

  /**
   * Sets the modification date.
   * 
   * @param date
   *          the modification date
   */
  public void setModificationDate(Date date) {
    this.modificationDate = date;
  }

  /**
   * Sets the user that modified the object.
   * 
   * @param modifier
   *          the modifier
   */
  public void setModifier(User modifier) {
    this.modifier = modifier;
  }

  /**
   * Returns a copy of this modification context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() throws CloneNotSupportedException {
    ModificationContext ctxt = (ModificationContext) super.clone();
    if (modifier != null)
      ctxt.modifier = (User) modifier.clone();
    if (modificationDate != null)
      ctxt.modificationDate = (Date) modificationDate.clone();
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
   * @throws IllegalArgumentException
   *           if the modification date found in this context cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static ModificationContext fromXml(Node context) {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this context from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param context
   *          the publish context node
   * @param xpath
   *          the xpath processor
   * @throws IllegalArgumentException
   *           if the modification date found in this context cannot be parsed
   * @see #toXml()
   */
  public static ModificationContext fromXml(Node context, XPath xpath)
      throws IllegalArgumentException {

    Node contextRoot = XPathHelper.select(context, "//modified", xpath);
    if (contextRoot == null)
      return null;

    // Modifying user
    ModificationContext ctx = new ModificationContext();
    Node creator = XPathHelper.select(contextRoot, "/modified/user", xpath);
    if (creator == null)
      throw new IllegalStateException("Modifier cannot be null");
    ctx.setModifier(UserImpl.fromXml(creator, xpath));

    // Modification date
    String date = XPathHelper.valueOf(contextRoot, "/modified/date", xpath);
    if (date == null)
      throw new IllegalStateException("Date cannot be null");
    try {
      ctx.setModificationDate(WebloungeDateFormat.parseStatic(date));
    } catch (ParseException e) {
      throw new IllegalArgumentException("The modification date '" + date + "' cannot be parsed", e);
    }

    return ctx;
  }

  /**
   * Returns an <code>XML</code> representation of the context, that will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;modified&gt;
   *   &lt;user id="john" realm="testland"&gt;John Doe&lt;/user&gt;
   *   &lt;date&gt;2009/11/06 08:52:52 GMT&lt;/date&gt;
   * &lt;/modified&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>ModificationContext</code> from the serialized output of this method.
   * 
   * @return the <code>XML</code> representation of the context
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
   */
  public String toXml() {
    if (modifier == null || modificationDate == null)
      return "";

    StringBuffer b = new StringBuffer();
    b.append("<modified>");
    if (modifier != null) {
      b.append(modifier.toXml());
    }
    if (modificationDate != null) {
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(modificationDate));
      b.append("</date>");
    }
    b.append("</modified>");
    return b.toString();
  }

}