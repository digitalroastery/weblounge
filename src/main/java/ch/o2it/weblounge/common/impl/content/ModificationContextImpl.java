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

import ch.o2it.weblounge.common.WebloungeDateFormat;
import ch.o2it.weblounge.common.content.ModificationContext;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Node;

import java.util.Date;

import javax.xml.xpath.XPath;

/**
 * TODO: Comment ModificationContextImpl
 */
public class ModificationContextImpl extends LocalizableObject<ModificationContextImpl> implements ModificationContext {

  /** The context identifier */
  private String id_ = null;

  /** Site */
  protected Site site = null;

  /** Creation date */
  protected Date created = null;

  /** Creator */
  protected User creator = null;

  /**
   * Creates a new and empty modification context.
   */
  public ModificationContextImpl(Site site) {
    this(site, "<default>");
  }

  /**
   * Creates a default publishing context with the given name and initially no
   * restrictions.
   * 
   * @param identifier
   *          the context identifier
   */
  public ModificationContextImpl(Site site, String identifier) {
    id_ = (identifier != null) ? identifier : "<default>";
    created = new Date();
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getCreationDate()
   */
  public Date getCreationDate() {
    return created;
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
    this.created = date;
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
    return getModificationDate(getLanguage());
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getModificationDate(ch.o2it.weblounge.common.language.Language)
   */
  public Date getModificationDate(Language language) {
    ModificationContextImpl ctxt = get(language);
    return (ctxt != null) ? ctxt.getModificationDate() : null;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getModifier()
   */
  public User getModifier() {
    return getModifier(getLanguage());
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#getModifier(ch.o2it.weblounge.common.language.Language)
   */
  public User getModifier(Language language) {
    ModificationContextImpl ctxt = get(language);
    return (ctxt != null) ? ctxt.getModifier() : null;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#isModified()
   */
  public boolean isModified() {
    return isModified(getLanguage());
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#isModified(ch.o2it.weblounge.common.language.Language)
   */
  public boolean isModified(Language language) {
    ModificationContextImpl ctxt = get(language);
    return (ctxt != null) ? ctxt.isModified() : false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModificationDate(java.util.Date)
   */
  public void setModificationDate(Date date) {
    setModificationDate(date, getLanguage());
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModificationDate(java.util.Date,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setModificationDate(Date date, Language language) {
    ModificationContextImpl ctxt = get(language);
    if (ctxt == null) {
      ctxt = new ModificationContextImpl(site, id_);
      put(ctxt, language);
    }
    ctxt.setModificationDate(date);
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModifier(ch.o2it.weblounge.common.security.User)
   */
  public void setModifier(User editor) {
    setModifier(editor, getLanguage());
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModifier(ch.o2it.weblounge.common.security.User,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setModifier(User editor, Language language) {
    ModificationContextImpl ctxt = get(language);
    if (ctxt == null) {
      ctxt = new ModificationContextImpl(site, id_);
      put(ctxt, language);
    }
    ctxt.setModifier(editor);
  }

  /**
   * Returns a copy of this modification context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    ModificationContextImpl ctxt = new ModificationContextImpl(site, id_);
    ctxt.created = created;
    ctxt.creator = creator;
    for (Language l : content.keySet()) {
      ctxt.put((ModificationContextImpl) content.get(l).clone(), l);
    }
    return ctxt;
  }

  /**
   * Initializes this context from an xml node.
   * 
   * @param context
   *          the publish context node
   */
  public void init(XPath path, Node context) {
    // created
    try {
      Node creatorNode = XPathHelper.select(path, context, "//created/user");
      if (creatorNode != null) {
        creator = site.getUsers().getUser(creatorNode.getNodeValue());
      }
      if (creator == null)
        creator = site.getAdministrator();
      Node createdNode = XPathHelper.select(path, context, "//created/date");
      created = (createdNode != null) ? WebloungeDateFormat.parseStatic(createdNode.getNodeValue()) : null;
    } catch (Exception e) {
      site.getLogger().warn("Error reading history creation data for " + id_);
    }
    // modifications
    try {
      Node languageNode = XPathHelper.select(path, context, "//modified/@language");
      Language language = site.getLanguage(languageNode.getNodeValue());
      if (language != null) {
        Node modifierNode = XPathHelper.select(path, context, "//modified/user");
        Node modifiedNode = XPathHelper.select(path, context, "//modified/date");
        if (modifierNode != null && modifiedNode != null) {
          User modifier = site.getUsers().getUser(modifierNode.getNodeValue());
          Date modified = (modifiedNode != null) ? WebloungeDateFormat.parseStatic(modifiedNode.getNodeValue()) : null;
          if (modifier != null) {
            ModificationContextImpl ctxt = new ModificationContextImpl(site);
            ctxt.setCreator(creator);
            ctxt.setCreationDate(created);
            ctxt.setModifier(modifier);
            ctxt.setModificationDate(modified);
            put(ctxt, language);
          }
        }
      }
    } catch (Exception e) {
      site.getLogger().warn("Error reading history creation data for " + id_);
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
      b.append(WebloungeDateFormat.formatStatic(created));
      b.append("</date>");
      b.append("/modified");
    }

    // Modification
    for (Language language : content.keySet()) {
      ModificationContextImpl ctxt = content.get(language);
      b.append("<modified language=\"");
      b.append(language.getIdentifier());
      b.append("\">");
      b.append("<user>");
      b.append(ctxt.getModifier().getLogin());
      b.append("</user>");
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(ctxt.getModificationDate()));
      b.append("</date>");
      b.append("/modified");
    }
    return b.toString();
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

}