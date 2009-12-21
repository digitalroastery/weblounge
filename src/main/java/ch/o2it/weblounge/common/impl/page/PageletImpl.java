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

import ch.o2it.weblounge.common.impl.content.CreationContext;
import ch.o2it.weblounge.common.impl.content.LocalizedModificationContext;
import ch.o2it.weblounge.common.impl.content.PublishingContext;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.security.SecurityContextImpl;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.Pagelet;
import ch.o2it.weblounge.common.page.PageletURI;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
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
  SecurityContextImpl securityCtx = null;

  /** The creation context */
  CreationContext creationCtx = null;

  /** The publishing context */
  PublishingContext publishingCtx = null;

  /** The modification context */
  LocalizedModificationContext modificationCtx = null;

  /** The pagelet properties */
  Map<String, String[]> properties = null;

  /** The content */
  LocalizableContent<Map<String, String[]>> content = null;

  /**
   * Creates an empty pagelet. This constructor is for use by the
   * {@link PageletReader} only.
   */
  PageletImpl() {
    this(null, null);
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
    moduleId = module;
    pageletId = id;
    setLanguageResolution(LanguageResolution.Original);
    properties = new HashMap<String, String[]>();
    creationCtx = new CreationContext();
    publishingCtx = new PublishingContext();
    modificationCtx = new LocalizedModificationContext();
    securityCtx = new PageletSecurityContext();
    content = new LocalizableContent<Map<String, String[]>>(this);
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
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getModule()
   */
  public String getModule() {
    return moduleId;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getIdentifier()
   */
  public String getIdentifier() {
    return pageletId;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getProperty(java.lang.String)
   */
  public String getProperty(String key) {
    String[] values = properties.get(key);
    if (values != null && values.length > 0)
      return values[0];
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#isMultiValueProperty(java.lang.String)
   */
  public boolean isMultiValueProperty(String key) {
    String[] values = properties.get(key);
    return values != null && values.length > 1;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getMultiValueProperty(java.lang.String)
   */
  public String[] getMultiValueProperty(String key) {
    String[] values = properties.get(key);
    if (values != null)
      return values;
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#setProperty(java.lang.String, java.lang.String)
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
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#setOwner(ch.o2it.weblounge.common.user.User)
   */
  public void setOwner(User owner) {
    securityCtx.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    return securityCtx.getOwner();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#allow(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)
   */
  public void allow(Permission permission, Authority authority) {
    securityCtx.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#deny(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)
   */
  public void deny(Permission permission, Authority authority) {
    securityCtx.deny(permission, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(Permission p, Authority a) {
    return securityCtx.check(p, a);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.PermissionSet, ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(PermissionSet p, Authority a) {
    return securityCtx.check(p, a);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#checkOne(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    return securityCtx.checkOne(permission, authorities);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#checkAll(ch.o2it.weblounge.common.security.Permission, ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    return securityCtx.checkAll(permission, authorities);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#permissions()
   */
  public Permission[] permissions() {
    return permissions;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#addSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    securityCtx.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.security.Securable#removeSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    securityCtx.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#setURI(ch.o2it.weblounge.common.page.PageletURI)
   */
  public void setURI(PageletURI uri) {
    this.uri = uri;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getURI()
   */
  public PageletURI getURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#setCreated(ch.o2it.weblounge.common.user.User, java.util.Date)
   */
  public void setCreated(User creator, Date creationDate) {
    creationCtx.setCreated(creator, creationDate);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    return creationCtx.getCreationDate();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    return creationCtx.getCreator();
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
   * @see ch.o2it.weblounge.common.page.Pagelet#setPublished(ch.o2it.weblounge.common.user.User, java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    publishingCtx.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getModificationDate()
   */
  public Date getModificationDate() {
    return modificationCtx.getModificationDate();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getModifier()
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
   * @see ch.o2it.weblounge.common.page.Pagelet#setModified(ch.o2it.weblounge.common.user.User, java.util.Date, ch.o2it.weblounge.common.language.Language)
   */
  public void setModified(User user, Date date, Language language) {
    modificationCtx.setModified(user, date, language);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#isMultiValueContent(java.lang.String)
   */
  public boolean isMultiValueContent(String name) {
    Map<String, String[]> languageContent = content.get();
    if (languageContent == null)
      return false;
    String[] values = languageContent.get(name);
    return values != null && values.length > 1;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getContent(java.lang.String, ch.o2it.weblounge.common.language.Language, boolean)
   */
  public String getContent(String name, Language language, boolean force) {
    Map<String, String[]> languageContent = content.get(language, force);
    if (languageContent == null)
      return null;
    String[] values = languageContent.get(name);
    return (values != null && values.length > 0) ? values[0] : null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getContent(java.lang.String, ch.o2it.weblounge.common.language.Language)
   */
  public String getContent(String name, Language language) {
    return getContent(name, language, false);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getContent(java.lang.String)
   */
  public String getContent(String name) {
    return getContent(name, getLanguage(), false);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getMultiValueContent(java.lang.String, ch.o2it.weblounge.common.language.Language, boolean)
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
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getMultiValueContent(java.lang.String, ch.o2it.weblounge.common.language.Language)
   */
  public String[] getMultiValueContent(String name, Language language) {
    return getMultiValueContent(name, language, false);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#getMultiValueContent(java.lang.String)
   */
  public String[] getMultiValueContent(String name) {
    return getMultiValueContent(name, getLanguage(), false);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#setContent(java.lang.String, java.lang.String, ch.o2it.weblounge.common.language.Language)
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
   * {@inheritDoc}
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
   * {@inheritDoc}
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
   * pagelet as well, then the {@link PageletURI} is used for the comparison.
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
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.language.LocalizableObject#toString()
   */
  public String toString() {
    return moduleId + "/" + pageletId;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Pagelet#toXml()
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
   * Utility class used to compare content and property map entries.
   */
  protected static class MapEntryComparator implements Comparator<Map.Entry<String, String[]>>, Serializable {

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