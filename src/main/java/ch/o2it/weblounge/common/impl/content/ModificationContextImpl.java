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
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.Date;

import javax.xml.xpath.XPath;

/**
 * TODO: Comment ModificationContextImpl
 */
public class ModificationContextImpl implements ModificationContext {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ModificationContextImpl.class);

  /** Creation date */
  protected Date creationDate = null;

  /** Creator */
  protected User creator = null;

  /** Modification date */
  protected Date modificationDate = null;

  /** Editor */
  protected User editor = null;

  /**
   * Creates a new and empty modification context.
   */
  public ModificationContextImpl() {
    this.creationDate = new Date();
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getCreationDate()
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getCreator()
   */
  public User getCreator() {
    return creator;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    this.creationDate = date;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#setCreator(ch.o2it.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    this.creator = user;
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
    return editor;
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
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModifier(ch.o2it.weblounge.common.security.User)
   */
  public void setModifier(User editor) {
    this.editor = editor;
  }

  /**
   * Returns a copy of this modification context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    ModificationContextImpl ctxt = new ModificationContextImpl();
    ctxt.creationDate = creationDate;
    ctxt.creator = creator;
    ctxt.editor = editor;
    ctxt.modificationDate = modificationDate;
    return ctxt;
  }

  /**
   * Initializes this context from an XML node.
   * 
   * @param context
   *          the publish context node
   * @param site
   *          the associated site
   */
  public void init(XPath path, Node context, Site site) {

    // created
    try {
      Node creatorNode = XPathHelper.select(path, context, "//created/user");
      if (creatorNode != null) {
        creator = site.getUsers().getUser(creatorNode.getNodeValue());
      }
      Node createdNode = XPathHelper.select(path, context, "//created/date");
      creationDate = (createdNode != null) ? WebloungeDateFormat.parseStatic(createdNode.getNodeValue()) : null;
    } catch (Exception e) {
      log_.error("Error reading creation data for modification context", e);
    }
    
    // modification
    try {
      Node modifierNode = XPathHelper.select(path, context, "//modified/user");
      Node modifiedNode = XPathHelper.select(path, context, "//modified/date");
      if (modifierNode != null && modifiedNode != null) {
        User modifier = site.getUsers().getUser(modifierNode.getNodeValue());
        Date modified = (modifiedNode != null) ? WebloungeDateFormat.parseStatic(modifiedNode.getNodeValue()) : null;
        if (modifier != null) {
          this.editor = modifier;
          this.modificationDate = modified;
        }
      }
    } catch (Exception e) {
      log_.error("Error reading modification data", e);
    }

  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();

    // Creation
    if (creator == null)
      b.append("<created/>");
    else {
      b.append("<created>");
      b.append("<user>");
      b.append(creator.getLogin());
      b.append("</user>");
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(creationDate));
      b.append("</date>");
      b.append("/modified");
    }

    // Modification
    if (editor != null) {
      b.append("<modified>");
      b.append("<user>");
      b.append(editor.getLogin());
      b.append("</user>");
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(modificationDate));
      b.append("</date>");
      b.append("/modified");
    }
    return b.toString();
  }

}