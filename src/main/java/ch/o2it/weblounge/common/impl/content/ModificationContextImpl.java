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

import ch.o2it.weblounge.common.content.ModificationContext;
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
 * Default implementation of the {@link ModificationContext}.
 */
public class ModificationContextImpl implements ModificationContext {

  /** Modification date */
  protected Date modificationDate = null;

  /** Editor */
  protected User modifier = null;

  /**
   * Creates a modification context with today's date.
   */
  public ModificationContextImpl() {
    this(new Date(), null);
  }

  /**
   * Creates a modification context with the given modifier, with the
   * modification time reflecting the current date.
   * 
   * @param modifier
   *          the modifying user
   */
  public ModificationContextImpl(User modifier) {
    this(new Date(), modifier);
  }

  /**
   * Creates a modification context reflecting modification at the given date,
   * made by the referenced user.
   * 
   * @param date
   *          the modification date
   * @param modifier
   *          the modifying user
   */
  public ModificationContextImpl(Date date, User modifier) {
    this.modificationDate = date;
    this.modifier = modifier;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getModificationDate()
   */
  public Date getModificationDate() {
    return modificationDate;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getModifier()
   */
  public User getModifier() {
    return modifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#isModified()
   */
  public boolean isModified() {
    return isModifiedBefore(new Date());
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#isModifiedAfter(java.util.Date)
   */
  public boolean isModifiedAfter(Date date) {
    return modificationDate != null && modificationDate.after(date);
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#isModifiedBefore(java.util.Date)
   */
  public boolean isModifiedBefore(Date date) {
    return modificationDate != null && modificationDate.before(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  public void setModified(User user, Date date) {
    this.modifier = user;
    this.modificationDate = date;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModificationDate(java.util.Date)
   */
  public void setModificationDate(Date date) {
    this.modificationDate = date;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModifier(ch.o2it.weblounge.common.user.User)
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
    ModificationContextImpl ctxt = (ModificationContextImpl)super.clone();
    if (modifier != null)
      ctxt.modifier = (User) modifier.clone();
    if (modificationDate != null)
      ctxt.modificationDate = (Date) modificationDate.clone();
    return ctxt;
  }

  /**
   * Initializes this context from an <code>XML</code> node.
   * 
   * @param context
   *          the publish context node
   * @throws IllegalArgumentException
   *           if the modification date found in this context cannot be parsed
   */
  public static ModificationContextImpl fromXml(Node context) {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this context from an <code>XML</code> node.
   * 
   * @param context
   *          the publish context node
   * @param xpath
   *          the xpath processor
   * @throws IllegalArgumentException
   *           if the modification date found in this context cannot be parsed
   */
  public static ModificationContextImpl fromXml(Node context, XPath xpath)
      throws IllegalArgumentException {

    Node contextRoot = XPathHelper.select(context, "//modified", xpath);
    if (contextRoot == null)
      return null;

    // Modifying user
    ModificationContextImpl ctx = new ModificationContextImpl();
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
   * @see ch.o2it.weblounge.common.content.ModificationContext#toXml()
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