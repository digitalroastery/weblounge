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

import ch.o2it.weblounge.common.content.CreationContext;
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
 * Default implementation of the creation context.
 */
public class CreationContextImpl implements CreationContext {

  /** Creation date */
  protected Date creationDate = null;

  /** Creator */
  protected User creator = null;

  /**
   * Creates a new and empty modification context.
   */
  public CreationContextImpl() {
    this.creationDate = new Date();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.CreationContext#getCreationDate()
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.CreationContext#getCreator()
   */
  public User getCreator() {
    return creator;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.CreationContext#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    this.creationDate = date;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.CreationContext#setCreator(ch.o2it.weblounge.common.user.User)
   */
  public void setCreator(User user) {
    this.creator = user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.CreationContext#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    return creationDate != null && creationDate.after(date);
  }

  /**
   * Returns a copy of this modification context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    CreationContextImpl ctxt = new CreationContextImpl();
    ctxt.creationDate = creationDate;
    ctxt.creator = creator;
    return ctxt;
  }

  /**
   * Initializes this context from an XML node.
   * 
   * @param context
   *          the creation context node
   * @throws IllegalStateException
   *           if the date found in this context cannot be parsed
   */
  public static CreationContext fromXml(Node context)
      throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this context from an XML node.
   * 
   * @param context
   *          the creation context node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the date found in this context cannot be parsed
   */
  public static CreationContext fromXml(Node context, XPath xpathProcessor)
      throws IllegalStateException {
    Node contextRoot = XPathHelper.select(context, "//created", xpathProcessor);
    if (contextRoot == null)
      return null;

    CreationContextImpl c = new CreationContextImpl();
    Node creator = XPathHelper.select(contextRoot, "//created/user", xpathProcessor);
    if (creator != null)
      c.setCreator(UserImpl.fromXml(creator, xpathProcessor));
    String date = XPathHelper.valueOf(contextRoot, "//created/date", xpathProcessor);
    if (date != null)
      try {
        c.setCreationDate(WebloungeDateFormat.parseStatic(date));
      } catch (ParseException e) {
        throw new IllegalArgumentException("The creation date " + date + " cannot be parsed", e);
      }
    return c;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.CreationContext#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<created>");
    if (creator != null) {
      b.append(creator.toXml());
    }
    if (creationDate != null) {
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(creationDate));
      b.append("</date>");
    }
    b.append("</created>");
    return b.toString();
  }

}