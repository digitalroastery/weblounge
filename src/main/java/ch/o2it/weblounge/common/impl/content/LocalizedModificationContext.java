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

import ch.o2it.weblounge.common.impl.language.LanguageUtils;
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
 * The localized modification context contains information about when an object
 * was modified and who the modifier was in a given language. It can be used by
 * <code>LocalizedModifiable</code> objects as the backing implementation.
 * <p>
 * The context adds additional means of specifying and querying modifier and
 * modification date. It also allows for easy serialization and deserialization
 * of <code>LocalizedModifiable</code> data.
 * <p>
 * Following is an example of the data structure that the modification context
 * is able to handle:
 * 
 * <pre>
 * &lt;content&gt;
 *   &lt;locale language="de" original="true"&gt;
 *     &lt;modified&gt;
 *       &lt;user id="hans" realm="testland"&gt;Hans Muster&lt;/user&gt;
 *       &lt;date&gt;2009/01/07 19:05:41 GMT&lt;/date&gt;
 *     &lt;/modified&gt;
 *   &lt;/locale&gt;
 *   &lt;locale language="fr"&gt;
 *     &lt;modified&gt;
 *       &lt;user id="amelie" realm="testland"&gt;Amélie Poulard&lt;/user&gt;
 *       &lt;date&gt;2009/02/18 21:06:40 GMT&lt;/date&gt;
 *     &lt;/modified&gt;
 *   &lt;/locale&gt;
 * &lt;/content&gt;
 * </pre>
 * 
 * @see ch.o2it.weblounge.common.content.LocalizedModifiable
 */
public class LocalizedModificationContext extends LocalizableObject implements Cloneable {

  /** The last modifier */
  protected transient User lastModifier = null;

  /** The last modification date */
  protected transient Date lastModification = null;

  /** The earliest modification date */
  protected transient Date earliestModification = null;

  /** Localized publishing information */
  protected Map<Language, Modification> modifications = null;

  /**
   * Creates a new and empty localized modification context.
   */
  public LocalizedModificationContext() {
    this.modifications = new HashMap<Language, Modification>();
  }

  /**
   * Returns the modification date in the current language. If the current
   * language is undefined, then the last modification date is returned.
   * <p>
   * If no modification information is present for any language, this method
   * returns <code>null</code>.
   * 
   * @return the modification date in the current language
   * @see #switchTo(Language)
   * @see #switchTo(Language, boolean)
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
   * Returns the modification date in the specified language or
   * <code>null</code> if no modification information is present in that
   * language.
   * 
   * @param language
   *          the language
   * @return the modification time
   */
  public Date getModificationDate(Language language) {
    Modification modification = modifications.get(language);
    return (modification != null) ? modification.getDate() : null;
  }

  /**
   * Returns the user that modified the object in the current language. If the
   * current language is undefined, then the user is returned that last modified
   * the object in any language.
   * <p>
   * If no modification information is present for any language, this method
   * returns <code>null</code>.
   * 
   * @return the modifier in the current language
   * @see #switchTo(Language)
   * @see #switchTo(Language, boolean)
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
   * Returns the user that modified the object in the specified language or
   * <code>null</code> if no modification in that language has occurred.
   * 
   * @param language
   *          the language
   * @return the modifier
   */
  public User getModifier(Language language) {
    Modification modification = modifications.get(language);
    return (modification != null) ? modification.getUser() : null;
  }

  /**
   * Returns <code>true</code> if the object is modified in the current language
   * prior to <code>now</code> (or <code>new Date()</code>, for that matter). If
   * the current language is undefined, then <code>true</code> is returned if
   * there was any modification prior to <code>now</code>.
   * <p>
   * If no modification information is present for any language, this method
   * returns <code>false</code>.
   * 
   * @return <code>true</code> if the object was modified
   * @see #switchTo(Language)
   * @see #switchTo(Language, boolean)
   */
  public boolean isModified() {
    Language currentLanguage = getLanguage();
    Date d = null;
    if (currentLanguage != null) {
      Modification c = modifications.get(currentLanguage);
      if (c != null)
        d = c.getDate();
    }
    if (d == null)
      d = getLastModificationDate();
    return d != null && new Date().after(d);
  }

  /**
   * Returns <code>true</code> if the object is modified in the current language
   * after <code>date</code>. If the current language is undefined, then
   * <code>true</code> is returned if there was any modification after
   * <code>date</code>.
   * <p>
   * If no modification information is present for any language, this method
   * returns <code>false</code>.
   * 
   * @return <code>true</code> if the object was modified after
   *         <code>date</code>
   * @see #switchTo(Language)
   * @see #switchTo(Language, boolean)
   */
  public boolean isModifiedAfter(Date date) {
    Language currentLanguage = getLanguage();
    Date d = null;
    if (currentLanguage != null) {
      Modification c = modifications.get(currentLanguage);
      if (c != null)
        d = c.getDate();
    }
    if (d == null)
      d = getLastModificationDate();
    return d != null && d.after(date);
  }

  /**
   * Returns <code>true</code> if the object is modified in the specified
   * language after <code>date</code>.
   * <p>
   * If no modification information is present for that language, this method
   * returns <code>false</code>.
   * 
   * @return <code>true</code> if the object was modified after
   *         <code>date</code>
   */
  public boolean isModifiedAfter(Date date, Language language) {
    Modification modification = modifications.get(language);
    return modification != null && modification.getDate().after(date);
  }

  /**
   * Returns <code>true</code> if the object is modified in the current language
   * before <code>date</code>. If the current language is undefined, then
   * <code>true</code> is returned if there was any modification before
   * <code>date</code>.
   * <p>
   * If no modification information is present for any language, this method
   * returns <code>false</code>.
   * 
   * @return <code>true</code> if the object was modified before
   *         <code>date</code>
   * @see #switchTo(Language)
   * @see #switchTo(Language, boolean)
   */
  public boolean isModifiedBefore(Date date) {
    Language currentLanguage = getLanguage();
    Date d = null;
    if (currentLanguage != null) {
      Modification c = modifications.get(currentLanguage);
      if (c != null)
        d = c.getDate();
    }
    if (d == null)
      d = getLastModificationDate();
    return d != null && d.before(date);
  }

  /**
   * Returns <code>true</code> if the object is modified in the specified
   * language before <code>date</code>.
   * <p>
   * If no modification information is present for that language, this method
   * returns <code>false</code>.
   * 
   * @return <code>true</code> if the object was modified before
   *         <code>date</code>
   */
  public boolean isModifiedBefore(Date date, Language language) {
    Modification modification = modifications.get(language);
    return modification != null && modification.getDate().before(date);
  }

  /**
   * Sets the user that last modified the object in the given language as well
   * as the modification date.
   * 
   * @param user
   *          the user that modified the object
   * @param date
   *          the date of modification
   * @param language
   *          the language version that was modified
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
   * Returns the date when this object was last modified in any language.
   * 
   * @return the last modification date, regardless of the language
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
   * Returns the date when this object was first modified in any language.
   * 
   * @return the first modification date, regardless of the language
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
   * Returns the user that last modified the object, regardless of the language.
   * 
   * @return the last modifier
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.language.LocalizableObject#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (o instanceof LocalizedModificationContext) {
      return getLastModificationDate().compareTo(getLastModificationDate());
    } else if (o instanceof ModificationContext) {
      return getModificationDate().compareTo(((ModificationContext) o).getModificationDate());
    }
    return 0;
  }

  /**
   * Returns a copy of this context.
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() throws CloneNotSupportedException {
    LocalizedModificationContext ctxt = (LocalizedModificationContext) super.clone();
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
   * Initializes this context from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param context
   *          the localized modification context node
   * @throws IllegalStateException
   *           if the context cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static LocalizedModificationContext fromXml(Node context)
      throws IllegalArgumentException {
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
   * @throws IllegalStateException
   *           if the context cannot be parsed
   * @see #toXml()
   */
  public static LocalizedModificationContext fromXml(Node context, XPath xpath)
      throws IllegalStateException {
    NodeList locales = XPathHelper.selectList(context, "locale", xpath);
    if (locales == null)
      return null;

    LocalizedModificationContext ctx = new LocalizedModificationContext();
    for (int i = 0; i < locales.getLength(); i++) {
      xpath.reset();
      Node locale = locales.item(i);
      Node languageNode = locale.getAttributes().getNamedItem("language");
      Node originalNode = locale.getAttributes().getNamedItem("original");
      boolean original = originalNode != null && ConfigurationUtils.isTrue(originalNode.getNodeValue());
      if (languageNode == null)
        throw new IllegalStateException("Found locale without language");
      Language language = LanguageUtils.getLanguage(languageNode.getNodeValue());

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
        throw new IllegalStateException("The modification date '" + date + "' cannot be parsed", e);
      }

      ctx.setModified(modifier, modificationDate, language);
      if (original)
        ctx.setOriginalLanguage(language);
    }

    return ctx;
  }

  /**
   * Returns an <code>XML</code> representation of the context, that will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;content&gt;
   *   &lt;locale language="de" original="true"&gt;
   *     &lt;modified&gt;
   *       &lt;user id="hans" realm="testland"&gt;Hans Muster&lt;/user&gt;
   *       &lt;date&gt;2009/01/07 19:05:41 GMT&lt;/date&gt;
   *     &lt;/modified&gt;
   *   &lt;/locale&gt;
   *   &lt;locale language="fr"&gt;
   *     &lt;modified&gt;
   *       &lt;user id="amelie" realm="testland"&gt;Amélie Poulard&lt;/user&gt;
   *       &lt;date&gt;2009/02/18 21:06:40 GMT&lt;/date&gt;
   *     &lt;/modified&gt;
   *   &lt;/locale&gt;
   * &lt;/content&gt;
   * </pre>
   * 
   * Use {@link #fromXml(Node))} or {@link #fromXml(Node, XPath)} to create a
   * <code>LocalizedModificationContext</code> from the serialized output of
   * this method.
   * 
   * @return the <code>XML</code> representation of the context
   * @see #fromXml(Node)
   * @see #fromXml(Node, XPath)
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
  private static class Modification {

    /** Modifier */
    private User modifier = null;

    /** Date of modification */
    private Date modificationDate = null;

    /**
     * Creates a new modification.
     */
    Modification() {
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