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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.content.CreationContext;
import ch.o2it.weblounge.common.content.LocalizedModificationContext;
import ch.o2it.weblounge.common.content.ModificationContext;
import ch.o2it.weblounge.common.impl.content.CreationContextImpl;
import ch.o2it.weblounge.common.impl.content.LocalizedModificationContextImpl;
import ch.o2it.weblounge.common.impl.content.PublishingContextImpl;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.security.PermissionSecurityContext;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.Pagelet;
import ch.o2it.weblounge.common.page.PageletURI;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityContext;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * A page element is a piece of content, placed somewhere on a page. Depending
 * on the composer that created it, it consists of multiple elements and
 * properties.
 * <p>
 * Such an element is stored in a page in the following form:
 * 
 * <pre>
 * 	&lt;pagelet&gt;
 * 		&lt;content language=&quot;de&quot; original=&quot;true&quot;&gt;
 * 			&lt;modified&gt;
 * 				&lt;date/&gt;
 * 				&lt;user&gt;wunden&lt;/user&gt;
 * 			&lt;/modified&gt;
 * 			&lt;text id=&quot;keyword&quot;&gt;Keyword&lt;/text&gt;
 * 			&lt;text id=&quot;title&quot;&gt;My Big Title&lt;/text&gt;
 * 			&lt;text id=&quot;lead&quot;&gt;This is the leading sentence.&lt;/text&gt;
 * 		&lt;/content&gt;
 * 		&lt;property id=&quot;showauthor&quot;&gt;true&lt;/property&gt;
 * 		&lt;property id=&quot;showdate&quot;&gt;true&lt;/property&gt;
 * 	&lt;/pagelet&gt;
 * </pre>
 */
public final class PageletImpl extends LocalizableObject implements Pagelet {

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(Pagelet.class);

  /** Module that defined the pagelet */
  private String moduleId = null;

  /** Pagelet identifier */
  private String pageletId = null;

  /** The pagelet location */
  PageletURI uri = null;

  /** The security context */
  PermissionSecurityContext securityCtx = null;

  /** The creation context */
  CreationContextImpl creationCtx = null;

  /** The publishing context */
  PublishingContextImpl publishingCtx = null;

  /** The modification context */
  LocalizedModificationContextImpl modificationCtx = null;

  /** The pagelet properties */
  Map<String, String[]> properties = null;

  /** The content */
  LocalizableContent<Map<String, String[]>> content = null;

  /**
   * Creates an empty pagelet. This constructor is for use with a
   * {@link PageletReader} only.
   */
  PageletImpl() {
    properties = new HashMap<String, String[]>();
    creationCtx = new CreationContextImpl();
    publishingCtx = new PublishingContextImpl();
    modificationCtx = new LocalizedModificationContextImpl();
    securityCtx = new PageletSecurityContext();
    content = new LocalizableContent<Map<String, String[]>>(this);
  }

  /**
   * Creates a new pagelet data holder.
   * 
   * @param module
   *          the defining module
   * @param id
   *          the pagelet identifier
   */
  public PageletImpl(String module, String id) {
    this();
    moduleId = module;
    pageletId = id;
  }

  /**
   * Creates a new pagelet data holder at the specified location.
   * 
   * @param location
   *          the pagelet location
   * @param module
   *          the defining module
   * @param id
   *          the pagelet identifier
   */
  public PageletImpl(PageletURI location, String module, String id) {
    this(module, id);
    uri = location;
  }

  /**
   * Returns the defining module identifier.
   * 
   * @return the module identifier
   */
  public String getModule() {
    return moduleId;
  }

  /**
   * Returns the pagelet identifier.
   * 
   * @return the identifier
   */
  public String getIdentifier() {
    return pageletId;
  }

  /**
   * Returns the property with name <code>key</code> or the empty string if no
   * such property is found. If there is more than one value for the given key,
   * e. g. if this is a multiple value property, then this method returns the
   * first value of the value collection.
   * <p>
   * If no value is found at all, then the empty string is returned.
   * 
   * @param key
   *          the property name
   * @return the property value
   */
  public String getProperty(String key) {
    String[] values = properties.get(key);
    if (values != null && values.length > 0)
      return values[0];
    return null;
  }

  /**
   * Returns <code>true</code> if this is a multiple value property.
   * 
   * @param key
   *          the key
   * @return <code>true</code> if this key holds more than one value
   */
  public boolean isMultiValueProperty(String key) {
    String[] values = properties.get(key);
    return values != null && values.length > 1;
  }

  /**
   * Returns the array of values for the multiple value property
   * <code>key</code>. This method returns <code>null</code> if no value has
   * been stored at all for the given key, a single element string array if
   * there is exactly one string and an array of strings of all values in all
   * other cases.
   * 
   * @param key
   *          the value's name
   * @return the value collection
   */
  public String[] getMultiValueProperty(String key) {
    String[] values = properties.get(key);
    if (values != null)
      return values;
    return null;
  }

  /**
   * Adds a property to this pagelet. Properties are not language dependent, so
   * there is no need to pass the language.
   * 
   * @param key
   *          the property name
   * @param value
   *          the property value
   */
  public void setProperty(String key, String value) {
    String[] existing = properties.remove(key);
    List<String> values = new ArrayList<String>();
    if (existing != null) {
      for (String s : existing)
        values.add(s);
    }
    values.add(value);
    properties.put(key, values.toArray(new String[values.size()]));
  }

  /**
   * Sets the pagelet's owner.
   * 
   * @param owner
   *          the owner of this pagelet
   */
  public void setOwner(User owner) {
    securityCtx.setOwner(owner);
  }

  /**
   * Returns this pagelet's owner.
   * 
   * @return the owner
   */
  public User getOwner() {
    return securityCtx.getOwner();
  }

  /**
   * Returns the security context that is associated with this pagelet. The
   * context tells whether the pagelet may be accessed in a certain way by a
   * user or not.
   * 
   * @return the pagelet's security context
   */
  public SecurityContext getSecurityContext() {
    return securityCtx;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public void allow(Permission permission, Authority authority) {
    securityCtx.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#deny(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public void deny(Permission permission, Authority authority) {
    securityCtx.deny(permission, authority);
  }

  /**
   * Returns <code>true</code> if the user <code>u</code> is allowed to do
   * actions that require permission <code>p</code> on this pagelet.
   * 
   * @param p
   *          the required permission
   * @param a
   *          the authorization used to access to this pagelet
   * @return <code>true</code> if the user has the required permission
   */
  public boolean check(Permission p, Authority a) {
    return securityCtx.check(p, a);
  }

  /**
   * Returns <code>true</code> if the user <code>u</code> is allowed to act on
   * the secured object in a way that satisfies the given permissionset
   * <code>p</code>.
   * 
   * @param p
   *          the required set of permissions
   * @param a
   *          the authorization used to access to this pagelet
   * @return <code>true</code> if the user owns the required permissions
   */
  public boolean check(PermissionSet p, Authority a) {
    return securityCtx.check(p, a);
  }

  /**
   * Checks whether at least one of the given authorities pass with respect to
   * the given permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authorities
   *          the objects claiming the permission
   * @return <code>true</code> if all authorities pass
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    return securityCtx.checkOne(permission, authorities);
  }

  /**
   * Checks whether all of the given authorities pass with respect to the given
   * permission.
   * 
   * @param permission
   *          the permission to obtain
   * @param authorities
   *          the object claiming the permission
   * @return <code>true</code> if all authorities pass
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    return securityCtx.checkAll(permission, authorities);
  }

  /**
   * Returns the pagelets permissions, which are
   * <ul>
   * <li>READ</li>
   * <li>WRITE</li>
   * <li>TRANSLATE</li>
   * <li>MANAGE</li>
   * </ul>
   * 
   * @return the permissions that can be set on the pagelet
   * @see ch.o2it.weblounge.api.security.Secured#permissions()
   */
  public Permission[] permissions() {
    return permissions;
  }

  /**
   * Adds the security listener to the pagelets security context.
   * 
   * @param listener
   *          the security listener
   * @see ch.o2it.weblounge.api.security.Secured#addSecurityListener(ch.o2it.weblounge.api.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    securityCtx.addSecurityListener(listener);
  }

  /**
   * Removes the security listener from the pagelets security context.
   * 
   * @param listener
   *          the security listener
   * @see ch.o2it.weblounge.api.security.Secured#removeSecurityListener(ch.o2it.weblounge.api.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    securityCtx.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.page.Pagelet#setURI(ch.o2it.weblounge.common.page.PageletURI)
   */
  public void setURI(PageletURI uri) {
    this.uri = uri;
  }

  /**
   * Returns the pagelet location, containing information about url, composer
   * and composer position.
   * 
   * @return the pagelet location
   */
  public PageletURI getURI() {
    return uri;
  }

  /**
   * Returns the pagelet's {@link CreationContext}.
   * 
   * @return the creation context
   */
  public CreationContext getCreationContext() {
    return creationCtx;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#setCreated(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  public void setCreated(User creator, Date creationDate) {
    creationCtx.setCreated(creator, creationDate);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getCreationDate()
   */
  public Date getCreationDate() {
    return creationCtx.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getCreator()
   */
  public User getCreator() {
    return creationCtx.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    return creationCtx.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    return publishingCtx.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    return publishingCtx.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    return publishingCtx.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    return publishingCtx.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#setPublished(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    publishingCtx.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    return publishingCtx.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#setPublishFrom(java.util.Date)
   */
  public void setPublishFrom(Date from) {
    publishingCtx.setPublishFrom(from);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#setPublishTo(java.util.Date)
   */
  public void setPublishTo(Date to) {
    publishingCtx.setPublishTo(to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#setPublisher(ch.o2it.weblounge.common.user.User)
   */
  public void setPublisher(User user) {
    publishingCtx.setPublisher(user);
  }

  /**
   * Returns the pagelet's {@link ModificationContext}.
   * 
   * @return the modification context
   */
  public LocalizedModificationContext getModificationContext() {
    return modificationCtx;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    return modificationCtx.getModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    return modificationCtx.getModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getLastModificationDate()
   */
  public Date getLastModificationDate() {
    return modificationCtx.getLastModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getLastModifier()
   */
  public User getLastModifier() {
    return modificationCtx.getLastModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getModificationDate(ch.o2it.weblounge.common.language.Language)
   */
  public Date getModificationDate(Language language) {
    return modificationCtx.getModificationDate(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getModifier(ch.o2it.weblounge.common.language.Language)
   */
  public User getModifier(Language language) {
    return modificationCtx.getModifier(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, ch.o2it.weblounge.common.language.Language)
   */
  public void setModified(User user, Date date, Language language) {
    modificationCtx.setModified(user, date, language);
  }

  /**
   * Returns a string representation of this pagelet, consisting of the module
   * identifier and the pagelet id.
   * 
   * @return the pagelet string representation
   */
  public String toString() {
    return moduleId + "/" + pageletId;
  }

  /**
   * Returns an XML representation of this pagelet.
   * 
   * @return an XML representation of this pagelet
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();

    // Add root node
    b.append("<pagelet module=\"" + moduleId + "\" id=\"" + pageletId + "\">");

    // export security context
    b.append(securityCtx.toXml());

    // export creation context
    b.append(creationCtx.toXml());

    // export publishing context
    b.append(publishingCtx.toXml());

    // export content
    for (Language l : languages) {
      b.append("<locale language=\"");
      b.append(l.getIdentifier());
      b.append("\"");
      if (l.equals(getOriginalLanguage()))
        b.append(" original=\"true\">");
      else
        b.append(">");

      b.append("<modified>");
      User u = modificationCtx.getModifier(l);
      if (u == null)
        u = uri.getSite().getAdministrator();
      b.append(u.toXml());
      b.append("<date>");
      Date d = modificationCtx.getModificationDate(l);
      if (d == null)
        d = new Date();
      b.append(WebloungeDateFormat.formatStatic(d));
      b.append("</date>");
      b.append("</modified>");

      // export content
      MapEntryComparator comparator = new MapEntryComparator();
      SortedSet<Map.Entry<String, String[]>> entrySet = new TreeSet<Map.Entry<String, String[]>>(comparator);
      entrySet.addAll(content.get(l).entrySet());
      for (Map.Entry<String, String[]> e : entrySet) {
        for (String value : e.getValue()) {
          b.append("<text id=\"");
          b.append(e.getKey());
          b.append("\"><![CDATA[");
          b.append(value);
          b.append("]]></text>");
        }
      }
      b.append("</locale>");
    }

    // export properties
    if (properties.size() == 0) {
      b.append("<properties/>");
    } else {
      b.append("<properties>");
      MapEntryComparator comparator = new MapEntryComparator();
      SortedSet<Map.Entry<String, String[]>> entrySet = new TreeSet<Map.Entry<String, String[]>>(comparator);
      entrySet.addAll(properties.entrySet());
      for (Map.Entry<String, String[]> p : entrySet) {
        for (String value : p.getValue()) {
          b.append("<property id=\"");
          b.append(p.getKey());
          b.append("\"><![CDATA[");
          b.append(value);
          b.append("]]></property>");
        }
      }
      b.append("</properties>");
    }

    b.append("</pagelet>");

    return b.toString();

    /*
     * try { InputSource is = new InputSource(new StringReader(b.toString()));
     * DocumentBuilder docBuilder = XMLUtilities.getDocumentBuilder(); Document
     * doc = docBuilder.parse(is); return doc.getFirstChild(); } catch
     * (SAXException e) { log_.error("Error building dom tree for pagelet", e);
     * } catch (IOException e) { log_.error("Error reading pagelet xml", e); }
     * catch (ParserConfigurationException e) {
     * log_.error("Error parsing pagelet xml", e); } return null;
     */
  }

  /**
   * Returns <code>true</code> if this content element holds more than one
   * entry.
   * 
   * @param name
   *          the element name
   * @return <code>true</code> if this is multidimensional content
   */
  public boolean isMultiValueContent(String name) {
    Map<String, String[]> languageContent = content.get();
    if (languageContent == null)
      return false;
    String[] values = languageContent.get(name);
    return values != null && values.length > 1;
  }

  /**
   * Returns the content in the specified language. If the language is forced
   * using the <code>force</code> parameter, then this method will return
   * <code>null</code> if there is no entry in this language. Otherwise, the
   * content is returned in the default language.
   * <p>
   * If this is multiple value content, then this method returns the first entry
   * only. Use {@link #getMultiValueContent(String, Language, boolean)} to get
   * all values.
   * 
   * @see ch.o2it.weblounge.api.content.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.api.language.Language, boolean)
   * @see ch.o2it.weblounge.core.language.MultilingualObject#getDefaultLanguage()
   */
  public String getContent(String name, Language language, boolean force) {
    Map<String, String[]> languageContent = content.get(language, force);
    if (languageContent == null)
      return null;
    String[] values = languageContent.get(name);
    return (values != null && values.length > 0) ? values[0] : null;
  }

  /**
   * Returns the content in the specified language. This method will return the
   * content in the default language if there is no entry in the specified
   * language.
   * <p>
   * If this is multiple value content, then this method returns the first entry
   * only. Use {@link #getMultiValueContent(String, Language)} to get all
   * values.
   * 
   * @see ch.o2it.weblounge.api.content.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.api.language.Language, boolean)
   * @see ch.o2it.weblounge.core.language.MultilingualObject#getDefaultLanguage()
   */
  public String getContent(String name, Language language) {
    return getContent(name, language, false);
  }

  /**
   * Returns the content in the specified language. This method will return the
   * content in the currently active language if there is no entry in the
   * specified language.
   * <p>
   * If this is multiple value content, then this method returns the first entry
   * only. Use {@link #getMultiValueContent(String)} to get all values.
   * 
   * @see ch.o2it.weblounge.api.content.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.api.language.Language, boolean)
   * @see ch.o2it.weblounge.core.language.MultilingualObject#getActiveLanguage()
   */
  public String getContent(String name) {
    return getContent(name, getLanguage(), false);
  }

  /**
   * Returns the multiple value content in the specified language. If the
   * language is forced using the <code>force</code> parameter, then this method
   * will return <code>null</code> if there is no entry in this language.
   * Otherwise, the content is returned in the default language.
   * <p>
   * If this is single value content, then this method returns an array
   * containing only the single value.
   * 
   * @see #getContent(String, Language, boolean)
   * @see ch.o2it.weblounge.api.content.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.api.language.Language, boolean)
   * @see ch.o2it.weblounge.core.language.MultilingualObject#getDefaultLanguage()
   */
  public String[] getMultiValueContent(String name, Language language,
      boolean force) {
    Map<String, String[]> languageContent = content.get(language, force);
    if (languageContent == null)
      return new String[] {};
    else if (languageContent.get(name) == null)
      return new String[] {};
    return languageContent.get(name);
  }

  /**
   * Returns the multiple value content in the specified language. If there is
   * no entry in this language, the content is returned in the default language.
   * <p>
   * If this is single value content, then this method returns an array
   * containing only the single value.
   * 
   * @see #getContent(String, Language)
   * @see ch.o2it.weblounge.api.content.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.api.language.Language)
   * @see ch.o2it.weblounge.core.language.MultilingualObject#getDefaultLanguage()
   */
  public String[] getMultiValueContent(String name, Language language) {
    return getMultiValueContent(name, language, false);
  }

  /**
   * Returns the multiple value content in the specified language. If there is
   * no entry in this language, the content is returned in the active language.
   * <p>
   * If this is single value content, then this method returns an array
   * containing only the single value.
   * 
   * @see #getContent(String)
   * @see ch.o2it.weblounge.api.content.Pagelet#getContent(java.lang.String)
   * @see ch.o2it.weblounge.core.language.MultilingualObject#getActiveLanguage()
   */
  public String[] getMultiValueContent(String name) {
    return getMultiValueContent(name, getLanguage(), false);
  }

  /**
   * Sets the pagelet's content in the given language. If the content identified
   * by <code>name</code> has already been assigned, then the content element is
   * being converted into a multiple value content element.
   * 
   * @see ch.o2it.weblounge.core.language.MultilingualObject#setContent(java.lang.String,
   *      java.lang.Object, ch.o2it.weblounge.api.language.Language)
   * @see #isMultiValueContent(String)
   */
  public void setContent(String name, String value, Language language) {
    Map<String, String[]> languageContent = content.get(language, true);
    if (languageContent == null) {
      languageContent = new HashMap<String, String[]>();
      content.put(languageContent, language);
      enableLanguage(language);
    }
    List<String> values = new ArrayList<String>();
    String[] existing = languageContent.remove(name);
    if (existing != null) {
      for (String e : existing)
        values.add(e);
    }
    values.add(value);
    languageContent.put(name, values.toArray(new String[values.size()]));
  }

  /**
   * Returns the pagelet's hash code.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    if (uri != null)
      return uri.hashCode();
    else
      return moduleId.hashCode() | (pageletId.hashCode() >> 16);
  }

  /**
   * Returns <code>true</code> if <code>o</code> represent the same pagelet.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o instanceof Pagelet) {
      Pagelet p = (Pagelet) o;
      if (p.getModule().equals(moduleId) && p.getIdentifier().equals(pageletId)) {
        if (p.getURI() != null)
          return p.getURI().equals(uri);
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This implementation does nothing, since it doesn't make sense to compare
   * the pagelet due with respect to its language. If <code>o</code> is a
   * pagelet as well, then the {@link PageletURI} is used for the
   * comparison.
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   * @see ch.o2it.weblounge.common.page.PageletURI#compareTo(PageletURI)
   */
  public int compareTo(Localizable o, Language l) {
    if (uri != null && o instanceof Pagelet) {
      Pagelet p = (Pagelet) o;
      if (p.getURI() != null)
        return uri.compareTo(p.getURI());
    }
    return 0;
  }

  /**
   * Utility class used to compare content and property map entries.
   */
  static class MapEntryComparator implements Comparator<Map.Entry<String, String[]>>, Serializable {

    /** Serial version uid */
    private static final long serialVersionUID = 853284601216740051L;

    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Entry<String, String[]> o1, Entry<String, String[]> o2) {
      int keyComparison = o1.getKey().compareTo(o2.getKey());
      if (keyComparison != 0)
        return keyComparison;
      return o1.getValue()[0].compareTo(o2.getValue()[0]);
    }

  }

}