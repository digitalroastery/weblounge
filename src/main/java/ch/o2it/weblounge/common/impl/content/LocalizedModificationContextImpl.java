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

import ch.o2it.weblounge.common.content.LocalizedModificationContext;
import ch.o2it.weblounge.common.content.ModificationContext;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.user.User;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Default implementation of the localized modification context.
 */
public class LocalizedModificationContextImpl extends LocalizableObject implements LocalizedModificationContext {

  /** The last modifier */
  protected transient User lastModifier = null;

  /** The last modification date */
  protected transient Date lastModification = null;

  /** The earliest modification date */
  protected transient Date earliestModification = null;

  /** Localized publishing information */
  protected Map<Language, Modification> modifications = null;

  /**
   * Creates a new and empty modification context with a creation date of
   * <code>now</code>.
   */
  public LocalizedModificationContextImpl() {
    this.modifications = new HashMap<Language, Modification>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    Language currentLanguage = getLanguage();
    Date date = null;
    if (currentLanguage != null) {
      Modification c = modifications.get(currentLanguage);
      if (c != null)
        date = c.getDate();
    }
    return date != null ? date : getLastModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#getModificationDate(ch.o2it.weblounge.common.language.Language)
   */
  public Date getModificationDate(Language language) {
    Modification modification = modifications.get(language);
    return (modification != null) ? modification.getDate() : null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    Language currentLanguage = getLanguage();
    User modifier = null;
    if (currentLanguage != null) {
      Modification c = modifications.get(currentLanguage);
      if (c != null)
        modifier = c.getUser();
    }
    return modifier != null ? modifier : getLastModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#getModifier(ch.o2it.weblounge.common.language.Language)
   */
  public User getModifier(Language language) {
    Modification modification = modifications.get(language);
    return (modification != null) ? modification.getUser() : null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#isModified()
   */
  public boolean isModified() {
    Date d = getLastModificationDate();
    return d != null && new Date().after(getLastModificationDate());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#isModifiedAfter(java.util.Date)
   */
  public boolean isModifiedAfter(Date date) {
    Date d = getLastModificationDate();
    return (d != null) ? d.after(date) : false;
  }

  /**
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#isModifiedAfter(java.util.Date,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public boolean isModifiedAfter(Date date, Language language) {
    Modification modification = modifications.get(language);
    return modification != null && modification.getDate().after(date);
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#isModifiedBefore(java.util.Date)
   */
  public boolean isModifiedBefore(Date date) {
    Date d = getEarliestModificationDate();
    return d != null && d.before(date);
  }

  /**
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#isModifiedBefore(java.util.Date,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public boolean isModifiedBefore(Date date, Language language) {
    Modification modification = modifications.get(language);
    return modification != null && modification.getDate().before(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModificationContext#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, ch.o2it.weblounge.common.language.Language)
   */
  public void setModified(User user, Date date, Language language) {
    Modification modification = modifications.get(language);
    if (modification == null) {
      modification = new Modification();
      modifications.put(language, modification);
    }
    modification.setUser(user);
    modification.setDate(date);

    // These might no longer be valid
    lastModifier = null;
    lastModification = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getLastModificationDate()
   */
  public Date getLastModificationDate() {
    if (lastModification != null)
      return lastModification;

    // First time calculation
    Date date = null;
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
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getEarliestModificationDate()
   */
  public Date getEarliestModificationDate() {
    if (earliestModification != null)
      return earliestModification;

    // First time calculation
    Date date = null;
    for (Modification m : modifications.values()) {
      if (date == null || m.getDate().before(date))
        date = m.getDate();
    }

    // Store for later reference
    earliestModification = date;

    return date;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getLastModifier()
   */
  public User getLastModifier() {
    if (lastModifier != null)
      return lastModifier;

    // First time calculation
    Date date = null;
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
   * Returns a copy of this modification context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() throws CloneNotSupportedException {
    LocalizedModificationContextImpl ctxt = (LocalizedModificationContextImpl)super.clone();
    ctxt.behavior = behavior;
    ctxt.currentLanguage = currentLanguage;
    ctxt.defaultLanguage = defaultLanguage;
    ctxt.originalLanguage = originalLanguage;
    ctxt.languages = new HashSet<Language>();
    ctxt.languages.addAll(languages);
    ctxt.modifications = new HashMap<Language, Modification>();
    for (Map.Entry<Language, Modification> entry : modifications.entrySet()) {
      ctxt.modifications.put(entry.getKey(), entry.getValue());
    }

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
  public static LocalizedModificationContextImpl fromXml(Node context) {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this context from an xml node.
   * 
   * @param context
   *          the publish context node
   * @param xpath
   *          the xpath processor
   * @throws IllegalArgumentException
   *           if the modification date found in this context cannot be parsed
   */
  public static LocalizedModificationContextImpl fromXml(Node context,
      XPath xpath) {
    NodeList locales = XPathHelper.selectList(context, "//locale", xpath);
    if (locales == null)
      return null;

    LocalizedModificationContextImpl ctx = new LocalizedModificationContextImpl();
    for (int i = 0; i < locales.getLength(); i++) {
      xpath.reset();
      Node locale = locales.item(i);
      Node languageNode = locale.getAttributes().getNamedItem("language");
      Node originalNode = locale.getAttributes().getNamedItem("original");
      boolean original = originalNode != null && ConfigurationUtils.isTrue(originalNode.getNodeValue());
      if (languageNode == null)
        throw new IllegalStateException("Found locale without language");
      Language language = LanguageSupport.getLanguage(languageNode.getNodeValue());

      // Modifying user
      Node modifierNode = XPathHelper.select(locale, "modified/user", xpath);
      if (modifierNode == null)
        throw new IllegalStateException("Modifier cannot be null");
      User modifier = UserImpl.fromXml(modifierNode, xpath);

      // Modification date
      String date = XPathHelper.valueOf(locale, "modified/date", xpath);
      if (date == null)
        throw new IllegalStateException("Date cannot be null");
      Date modificationDate = null;
      try {
        modificationDate = WebloungeDateFormat.parseStatic(date);
      } catch (ParseException e) {
        throw new IllegalArgumentException("The modification date '" + date + "' cannot be parsed", e);
      }

      ctx.setModified(modifier, modificationDate, language);
      if (original)
        ctx.setOriginalLanguage(language);
    }

    return ctx;
  }

  /**
   * @see ch.o2it.weblounge.common.content.ModificationContext#toXml()
   */
  public String toXml(Language language) {
    Modification modification = modifications.get(language);
    StringBuffer b = new StringBuffer();
    if (modification != null) {
      b.append("<modified>");
      b.append(modification.getUser().toXml());
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(modification.getDate()));
      b.append("</date>");
      b.append("</modified>");
    }
    return b.toString();
  }

  /**
   * Helper class used to hold modification information.
   */
  static class Modification {

    /** Modifier */
    private User modifier = null;

    /** Date of modification */
    private Date modificationDate = null;

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

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (o instanceof LocalizedModificationContextImpl) {
      return getLastModificationDate().compareTo(getLastModificationDate());
    } else if (o instanceof ModificationContext) {
      return getModificationDate().compareTo(((ModificationContext) o).getModificationDate());
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModificationDate(java.util.Date)
   */
  public void setModificationDate(Date date) {
    Modification modification = modifications.get(getLanguage());
    if (modification == null) {
      modification = new Modification();
      modifications.put(getLanguage(), modification);
    }
    modification.setDate(date);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.ModificationContext#setModifier(ch.o2it.weblounge.common.user.User)
   */
  public void setModifier(User user) {
    Modification modification = modifications.get(getLanguage());
    if (modification == null) {
      modification = new Modification();
      modifications.put(getLanguage(), modification);
    }
    modification.setUser(user);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.content.ModificationContext#toXml()
   */
  public String toXml() {
    throw new UnsupportedOperationException("XML can only be generated per language");
  }

}