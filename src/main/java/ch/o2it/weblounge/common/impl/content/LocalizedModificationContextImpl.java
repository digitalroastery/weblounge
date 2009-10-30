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

import ch.o2it.weblounge.common.content.LocalizedModificationContext;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.LocalizationListener;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.Date;
import java.util.HashSet;

import javax.xml.xpath.XPath;

/**
 * Default implementation fo the modification context.
 */
public class LocalizedModificationContextImpl extends LocalizableObject implements LocalizedModificationContext, LocalizationListener {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(LocalizedModificationContextImpl.class);

  /** Creation date */
  protected Date creationDate = null;

  /** Creator */
  protected User creator = null;
  
  /** The last modifier */
  protected transient User lastModifier = null;
  
  /** The last modification date */
  protected transient Date lastModification = null;

  /** Localized publishing information */
  protected LocalizableContent<Modification> modifications = null;

  /**
   * Creates a new and empty modification context with a creation date of
   * <code>now</code>.
   */
  public LocalizedModificationContextImpl() {
    this.creationDate = new Date();
    this.modifications = new LocalizableContent<Modification>(this);
  }

  /**
   * Creates a new and empty modification context with a creation date of
   * <code>now</code>.
   */
  public LocalizedModificationContextImpl(LocalizableObject localized) {
    this.creationDate = new Date();
    this.modifications = new LocalizableContent<Modification>(this);
    localized.addLocalizationListener(this);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.Modifiable#getCreationDate()
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.Modifiable#getCreator()
   */
  public User getCreator() {
    return creator;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    this.creationDate = date;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#setCreator(ch.o2it.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    this.creator = user;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    Modification modification = modifications.get();
    return (modification != null) ? modification.getDate() : null;
  }
  
  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#getModificationDate(ch.o2it.weblounge.common.language.Language)
   */
  public Date getModificationDate(Language language) {
    Modification modification = modifications.get(language);
    return (modification != null) ? modification.getDate() : null;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    Modification modification = modifications.get();
    return (modification != null) ? modification.getUser() : null;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#getModifier(ch.o2it.weblounge.common.language.Language)
   */
  public User getModifier(Language language) {
    Modification modification = modifications.get(language);
    return (modification != null) ? modification.getUser() : null;
  }
  
  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.Modifiable#isModified()
   */
  public boolean isModified() {
    return modifications.get() != null;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.Modifiable#isModifiedAfter(java.util.Date)
   */
  public boolean isModifiedAfter(Date date) {
    Modification modification = modifications.get();
    return (modification != null) ? modification.getDate().after(date) : false;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#isModifiedAtAllAfter(java.util.Date)
   */
  public boolean isModifiedAtAllAfter(Date date) {
    for (Modification modification : modifications.values()) {
      if (modification.getDate().after(date))
        return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#setModificationDate(java.util.Date)
   */
  public void setModificationDate(Date date) {
    setModified(null, date, getLanguage());
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModifier(ch.o2it.weblounge.common.security.User)
   */
  public void setModifier(User editor) {
    setModified(editor, null, getLanguage());
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#setModificationDate(java.util.Date, ch.o2it.weblounge.common.language.Language)
   */
  public void setModificationDate(Date date, Language language) {
    setModified(null, date, language);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#setModifier(ch.o2it.weblounge.common.security.User, ch.o2it.weblounge.common.language.Language)
   */
  public void setModifier(User modifier, Language language) {
    setModified(modifier, null, language);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#setModified(ch.o2it.weblounge.common.security.User, java.util.Date, ch.o2it.weblounge.common.language.Language)
   */
  public void setModified(User user, Date date, Language language) {
    Modification modification = modifications.get();
    if (modification == null) {
      modification = new Modification();
      modifications.put(modification, language);
    }
    modification.setUser(user);
    modification.setDate(date);
    
    // These might no longer be valid
    lastModifier = null;
    lastModification = null;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#setCreated(ch.o2it.weblounge.common.security.User, java.util.Date)
   */
  public void setCreated(User user, Date date) {
    this.creator = user;
    this.creationDate = date;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getLastModificationDate()
   */
  public Date getLastModificationDate() {
    if (lastModification != null)
      return lastModification;

    // First time calculation
    Date date = creationDate;
    for (Modification m : modifications.values()) {
      if (date == null || m.getDate().after(date))
        date = m.getDate();
    }
   
    // Store for later reference
    lastModification = date;

    return date;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getLastModifier()
   */
  public User getLastModifier() {
    if (lastModifier != null)
      return lastModifier;
    
    // First time calculation
    Date date = creationDate;
    User user = null;
    for (Modification m : modifications.values()) {
      if (date == null || m.getDate().after(date)) {
        date = m.getDate();
        user = m.getUser();
      }
    }
    
    // Store for later reference
    lastModification = date;
    lastModifier = user;
    
    return user;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#isModifiedAtAll()
   */
  public boolean isModifiedAtAll() {
    return creationDate.before(getLastModificationDate());
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.language.LocalizationListener#switchedTo(ch.o2it.weblounge.common.language.Language)
   */
  public void switchedTo(Language language) {
    modifications.switchTo(language);
  }

  /**
   * Returns a copy of this modification context.
   * 
   * @see java.lang.Object#clone()
   */
  public LocalizedModificationContextImpl clone() {
    LocalizedModificationContextImpl ctxt = new LocalizedModificationContextImpl();
    
    ctxt.behavior = behavior;
    ctxt.currentLanguage = currentLanguage;
    ctxt.defaultLanguage = defaultLanguage;
    ctxt.originalLanguage = originalLanguage;
    ctxt.languages = new HashSet<Language>();
    ctxt.languages.addAll(languages);

    ctxt.creationDate = creationDate;
    ctxt.creator = creator;
    ctxt.modifications = modifications.clone();
    ctxt.addLocalizationListener(modifications);
    
    return ctxt;
  }

  /**
   * Initializes this context from an xml node.
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
      if (creator == null)
        creator = site.getAdministrator();
      Node createdNode = XPathHelper.select(path, context, "//created/date");
      creationDate = (createdNode != null) ? WebloungeDateFormat.parseStatic(createdNode.getNodeValue()) : null;
    } catch (Exception e) {
      log_.warn("Error reading creation data", e);
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
            modifications.put(new Modification(modifier, modified), language);
          }
        }
      }
    } catch (Exception e) {
      log_.warn("Error reading modification data", e);
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

    // Modifications
    for (Language language : modifications.languages()) {
      Modification modification = modifications.get(language);
      b.append("<modified language=\"");
      b.append(language.getIdentifier());
      b.append("\">");
      b.append("<user>");
      b.append(modification.getUser().getLogin());
      b.append("</user>");
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(modification.getDate()));
      b.append("</date>");
      b.append("/modified");
    }
    return b.toString();
  }

  /**
   * Helper class used to hold modification information.
   */
  private class Modification {

    /** Modifier */
    private User modifier = null;

    /** Date of modification */
    private Date modificationDate = null;

    /**
     * Creates a new and empty modification.
     */
    Modification() { }

    /**
     * Creates a new modification including the user and the date of modification.
     * 
     * @param modifier
     *          the modifier
     * @param modificationDate
     *          the date of modification
     */
    Modification(User modifier, Date modificationDate) {
      this.modifier = modifier;
      this.modificationDate = modificationDate;
    }

    /**
     * Sets the modifier.
     * 
     * @param modifier
     *          the modifying user
     */
    void setUser(User modifier) {
      this.modifier = modifier;
    }

    /**
     * Returns the user that modified the item.
     * 
     * @return the modifier
     */
    User getUser() {
      return modifier;
    }

    /**
     * Sets the modification date.
     * 
     * @param date
     *          the modification date
     */
    void setDate(Date date) {
      modificationDate = date;
    }

    /**
     * Returns the modification date.
     * 
     * @return the date
     */
    Date getDate() {
      return modificationDate;
    }

  }

}