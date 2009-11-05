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

import ch.o2it.weblounge.common.content.ModificationContext;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.user.User;

import org.w3c.dom.Node;

import java.text.ParseException;
import java.util.Date;

import javax.xml.xpath.XPath;

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
    this.modificationDate = new Date();
  }

  /**
   * Creates a modification context reflecting modification at the given date.
   * 
   * @param date
   *          the modification date
   */
  public ModificationContextImpl(Date date) {
    this.modificationDate = date;
  }

  /**
   * Creates a modification context reflecting modification at the given date,
   * made by the referenced user.
   * 
   * @param date
   *          the modification date
   * @param editor
   *          the modifying user
   */
  public ModificationContextImpl(Date date, User editor) {
    this.modificationDate = date;
    this.modifier = editor;
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
    return isModifiedAfter(new Date());
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#isModified()
   */
  public boolean isModifiedAfter(Date date) {
    return modificationDate != null && modificationDate.after(date);
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
  public Object clone() {
    ModificationContextImpl ctxt = new ModificationContextImpl();
    ctxt.modifier = modifier;
    ctxt.modificationDate = modificationDate;
    return ctxt;
  }

  /**
   * Initializes this context from an XML node.
   * 
   * @param context
   *          the publish context node
   */
  public static ModificationContextImpl fromXml(XPath xpath, Node context) {
    Node contextRoot = XPathHelper.select(context, "//modified", xpath);
    if (contextRoot == null)
      return null;

    ModificationContextImpl c = new ModificationContextImpl();
    Node creator = XPathHelper.select(contextRoot, "/user", xpath);
    if (creator != null)
      c.setModifier(UserImpl.fromXml(creator, xpath));
    String date = XPathHelper.valueOf(contextRoot, "/date", xpath);
    if (date != null)
      try {
        c.setModificationDate(WebloungeDateFormat.parseStatic(date));
      } catch (ParseException e) {
        throw new IllegalArgumentException("The modification date " + date + " cannot be parsed", e);
      }
    return c;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<modified>");
    if (modifier != null) {
      b.append("<user>");
      b.append(modifier.getLogin());
      b.append("</user>");
    }
    if (modificationDate != null) {
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(modificationDate));
      b.append("</date>");
    }
    b.append("/modified");
    return b.toString();
  }

}