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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.WebloungeDateFormat;
import ch.o2it.weblounge.common.content.ModificationContext;
import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.content.ModificationContextImpl;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.security.PermissionSecurityContext;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Pagelet;
import ch.o2it.weblounge.common.page.PageletLocation;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityContext;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A page element is a piece of content, placed somewhere on a page. Depending
 * on the composer that created it, it consists of multiple elements and
 * properties.
 * <p>
 * Such an element ist stored in a page in the following form:
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
  private String moduleId_ = null;

  /** Pagelet identifier */
  private String id_ = null;

  /** The module */
  private Module module_ = null;

  /** The security context */
  PermissionSecurityContext securityCtx = null;

  /** The publishing context */
  PublishingContext publishingCtx = null;

  /** The modification context */
  ModificationContext modificationCtx = null;

  /** The pagelet properties */
  Map<String, Object> properties = null;

  /** Pagelet owner */
  User owner = null;

  /** The pagelet location */
  PageletLocation location_ = null;
  
  /** The content */
  LocalizableContent<Map> content_ = null;

  /**
   * Creates a new pagelet data holder.
   * 
   * @param module
   *          the defining module
   * @param id
   *          the pagelet identifier
   */
  public PageletImpl(Site site, String module, String id) {
    moduleId_ = module;
    id_ = id;
    module_ = site.getModule(moduleId_);
    if (module_ == null) {
      if (module_ == null) {
        String msg = "Configuration error: Module '" + module + "' containing renderer '" + id + "' could not be found!";
        log_.error(msg);
        throw new IllegalStateException(msg);
      } else if (module_.getRenderer(id) == null) {
        String msg = "Configuration error: Renderer '" + id + "' could not be found in module '" + id + "'";
        log_.error(msg);
        throw new IllegalStateException(msg);
      }
    }
    properties = new HashMap<String, Object>();
    securityCtx = new PageletSecurityContextImpl(module, id);
    publishingCtx = new PublishingContextImpl();
    modificationCtx = new ModificationContextImpl();
    content_ = new LocalizableContent<Map>(this);
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
  public PageletImpl(PageletLocation location, String module, String id) {
    this(location.getSite(), module, id);
    location_ = location;
  }

  /**
   * Returns the defining module identifier.
   * 
   * @return the module identifier
   */
  public String getModule() {
    return moduleId_;
  }

  /**
   * Returns the pagelet identifier.
   * 
   * @return the identifier
   */
  public String getIdentifier() {
    return id_;
  }

  /**
   * Sets the date of the last modification of this pagelet.
   * 
   * @param date
   *          the modification date
   * @param language
   *          the language version
   */
  public void setModifiedSince(Date date, Language language) {
    modificationCtx.setModificationDate(date, language);
  }

  /**
   * Sets the user that last modified the pagelet.
   * 
   * @param editor
   *          the modifying user
   * @param language
   *          the language
   */
  public void setModifiedBy(User editor, Language language) {
    modificationCtx.setModifier(editor, language);
  }

  /**
   * Returns the property with name <code>key</code> or the empty string if no
   * such property is found. If there is more than one value for the given key,
   * e. g. if this is a multivalue property, then this method returns the first
   * value of the value collection.
   * <p>
   * If no value is found at all, then the empty string is returned.
   * 
   * @param key
   *          the property name
   * @return the property value
   */
  public String getProperty(String key) {
    Object value = properties.get(key);
    if (value != null) {
      if (value instanceof String) {
        return (String) value;
      } else if (value instanceof ArrayList) {
        return (String) ((ArrayList) value).get(0);
      }
    }
    return "";
  }

  /**
   * Returns <code>true</code> if this is a multivalue property.
   * 
   * @param key
   *          the key
   * @return <code>true</code> if this key holds more than one value
   */
  public boolean isMultiValueProperty(String key) {
    Object value = properties.get(key);
    return (value instanceof ArrayList);
  }

  /**
   * Returns the array of values for the multivalue property <code>key</code>.
   * This method returns <code>null</code> if no value has been stored at all
   * for the given key, a single element string array if there is exactly one
   * string and an array of strings of all values in all other cases.
   * 
   * @param key
   *          the value's name
   * @return the value collection
   */
  public String[] getMultiValueProperty(String key) {
    Object value = properties.get(key);
    if (value != null) {
      if (value instanceof String) {
        return new String[] { (String) value };
      } else if (value instanceof List) {
        List<String> valueList = (List<String>) value;
        return valueList.toArray(new String[valueList.size()]);
      }
    }
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
    Object existing = properties.remove(key);
    if (existing != null) {
      if (existing instanceof List) {
        ((List) existing).add(value);
      } else {
        List values = new ArrayList();
        values.add(existing);
        values.add(value);
        properties.put(key, value);
      }
    } else {
      properties.put(key, value);
    }
  }

  /**
   * Sets the pagelet's owner.
   * 
   * @param owner
   *          the owner of this pagelet
   */
  void setOwner(User owner) {
    this.owner = owner;
  }

  /**
   * Returns this pagelet's owner.
   * 
   * @return the owner
   */
  public User getOwner() {
    return owner;
  }

  /**
   * Returns the publishing context that is associated with this pagelet. The
   * context tells whether the pagelet may be published on a certain point in
   * time or not.
   * 
   * @return the pagelet's publishing context
   */
  public PublishingContext getPublishingContext() {
    return publishingCtx;
  }

  /**
   * Returns <code>true</code> if the pagelet may be published. The output of
   * this method depends on the <code>check</code> method of the
   * <code>PublishingContext</code>.
   * 
   * @return <code>true</code> if the page may be published
   * @see #getPublishingContext()
   */
  public boolean isPublished() {
    return publishingCtx.isPublished();
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
   * Returns the renderer used to render this pagelet.
   * 
   * @param method
   *          the rendering method
   * @return the pagelet's renderer
   */
  public Renderer getRenderer(String method) {
    return module_.getRenderer(id_, method);
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
   * Returns the pagelet location, containing information about url, composer
   * and composer position.
   * 
   * @return the pagelet location
   */
  public PageletLocation getLocation() {
    return location_;
  }

  /**
   * Returns a string representation of this pagelet, consisting of the module
   * identifier and the pagelet id.
   * 
   * @return the pagelet string representation
   */
  public String toString() {
    return moduleId_ + "/" + id_;
  }

  /**
   * Returns an XML representation of this pagelet.
   * 
   * @return an XML representation of this pagelet
   */
  public Node toXml() {
    StringBuffer b = new StringBuffer();

    // Add root node
    b.append("<pagelet module=\"" + moduleId_ + "\" id=\"" + id_ + "\">");

    // export publishing context
    b.append(publishingCtx.toXml());

    // export security context
    b.append(securityCtx.toXml());

    // export content
    for (Language l : languages) {
      b.append("<content language=\"");
      b.append(l.getIdentifier());
      b.append("\"");
      if (l.equals(getOriginalLanguage()))
        b.append(" original=\"true\">");
      else
        b.append(">");

      b.append("<modified>");
      b.append("<date>");
      Date d = modificationCtx.getModificationDate(l);
      if (d == null)
        d = new Date();
      b.append(WebloungeDateFormat.formatStatic(d));
      b.append("</date>");
      b.append("<user>");
      User u = modificationCtx.getModifier(l);
      if (u == null)
        u = location_.getSite().getAdministrator();
      b.append(u.getLogin());
      b.append("</user>");
      b.append("</modified>");

      Map content = (HashMap) content_.get(l);
      if (content == null) {
        b.append("</content>");
        continue;
      }
      for (Iterator j = content.entrySet().iterator(); j.hasNext();) {
        Map.Entry e = (Map.Entry) j.next();
        String[] values = null;
        if (e.getValue() instanceof List) {
          List valueList = (List) e.getValue();
          values = (String[]) valueList.toArray(new String[valueList.size()]);
        } else {
          values = new String[] { e.getValue().toString() };
        }
        for (int k = 0; k < values.length; k++) {
          b.append("<text id=\"");
          b.append(e.getKey());
          b.append("\"><![CDATA[");
          b.append(values[k]);
          b.append("]]></text>");
        }
      }
      b.append("</content>");
    }
    // export properties
    if (properties.size() == 0) {
      b.append("<properties/>");
    } else {
      b.append("<properties>");
      for (Iterator i = properties.entrySet().iterator(); i.hasNext();) {
        Map.Entry e = (Map.Entry) i.next();
        String[] properties = null;
        if (e.getValue() instanceof List) {
          List<String> propertyList = (List) e.getValue();
          properties = propertyList.toArray(new String[propertyList.size()]);
        } else {
          Object value = e.getValue();
          properties = (value != null) ? new String[] { value.toString() } : new String[] {};
        }
        for (int k = 0; k < properties.length; k++) {
          b.append("<property id=\"");
          b.append(e.getKey());
          b.append("\"><![CDATA[");
          b.append(properties[k]);
          b.append("]]></property>");
        }
      }
      b.append("</properties>");
    }

    b.append("</pagelet>");

    try {
      InputSource is = new InputSource(new StringReader(b.toString()));
      DocumentBuilder docBuilder = XMLUtilities.getDocumentBuilder();
      Document doc = docBuilder.parse(is);
      return doc.getFirstChild();
    } catch (SAXException e) {
      log_.error("Error building dom tree for pagelet", e);
    } catch (IOException e) {
      log_.error("Error reading pagelet xml", e);
    } catch (ParserConfigurationException e) {
      log_.error("Error parsing pagelet xml", e);
    }
    return null;
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
    Object value = getContent(name);
    return (value instanceof ArrayList);
  }

  /**
   * Returns the content in the specified language. If the language is forced
   * using the <code>force</code> parameter, then this method will return
   * <code>null</code> if there is no entry in this language. Otherwise, the
   * content is returned in the default language.
   * <p>
   * If this is multivalue content, then this method returns the first entry
   * only. Use {@link #getMultiValueContent(String, Language, boolean)} to get
   * all values.
   * 
   * @see ch.o2it.weblounge.api.content.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.api.language.Language, boolean)
   * @see ch.o2it.weblounge.core.language.MultilingualObject#getDefaultLanguage()
   */
  public String getContent(String name, Language language, boolean force) {
    Map<String, ?> languageContent = content_.get(language, force);
    if (languageContent == null)
      return null;
    Object value = languageContent.get(name);
    if (value != null && value instanceof ArrayList) {
      return ((ArrayList<String>) value).get(0);
    }
    return (String)value;
  }

  /**
   * Returns the content in the specified language. This method will return the
   * content in the default language if there is no entry in the specified
   * language.
   * <p>
   * If this is multivalue content, then this method returns the first entry
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
   * If this is multivalue content, then this method returns the first entry
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
   * Returns the multivalue content in the specified language. If the language
   * is forced using the <code>force</code> parameter, then this method will
   * return <code>null</code> if there is no entry in this language. Otherwise,
   * the content is returned in the default language.
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
    Map languageContent = content_.get(language, force);
    if (languageContent == null)
      return null;
    Object value = languageContent.get(name);
    if (value != null) {
      if (value instanceof ArrayList) {
        List<String> valueList = (ArrayList<String>) value;
        return valueList.toArray(new String[valueList.size()]);
      } else {
        return new String[] { (String)value };
      }
    }
    return null;
  }

  /**
   * Returns the multivalue content in the specified language. If there is no
   * entry in this language, the content is returned in the default language.
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
   * Returns the multivalue content in the specified language. If there is no
   * entry in this language, the content is returned in the active language.
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
   * being converted into a multivalue content element.
   * 
   * @see ch.o2it.weblounge.core.language.MultilingualObject#setContent(java.lang.String,
   *      java.lang.Object, ch.o2it.weblounge.api.language.Language)
   * @see #isMultiValueContent(String)
   */
  public void setContent(String name, Object content, Language language) {
    Map languageContent = content_.get(language, true);
    Object value = null;
    if (languageContent == null) {
      languageContent = new HashMap<String, Object>(10);
      content_.put(languageContent, language);
    } else {
      value = languageContent.get(name);
    }
    if (value != null) {
      if (value instanceof ArrayList) {
        ((ArrayList) value).add(content);
      } else {
        List valueList = new ArrayList();
        valueList.add(value);
        valueList.add(content);
        languageContent.put(name, valueList);
      }
    } else {
      languageContent.put(name, content);
    }
  }

  /**
   * Returns the pagelet's hash code.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    if (location_ != null)
      return location_.hashCode();
    else
      return moduleId_.hashCode() | (id_.hashCode() >> 16);
  }

  /**
   * Returns <code>true</code> if <code>o</code> represent the same pagelet.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o instanceof Pagelet) {
      Pagelet p = (Pagelet) o;
      if (p.getModule().equals(moduleId_) && p.getIdentifier().equals(id_)) {
        if (p.getLocation() != null)
          return p.getLocation().equals(location_);
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getCreationDate()
   */
  public Date getCreationDate() {
    return modificationCtx.getCreationDate();
    }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getCreator()
   */
  public User getCreator() {
    return modificationCtx.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModificationContext()
   */
  public ModificationContext getModificationContext() {
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
   * @see ch.o2it.weblounge.common.content.Modifiable#getModificationDate(ch.o2it.weblounge.common.language.Language)
   */
  public Date getModificationDate(Language language) {
    return modificationCtx.getModificationDate(language);
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
   * @see ch.o2it.weblounge.common.content.Modifiable#getModifier(ch.o2it.weblounge.common.language.Language)
   */
  public User getModifier(Language language) {
    return modificationCtx.getModifier(language);
    }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#isModifiedAfter(java.util.Date)
   */
  public boolean isModifiedAfter(Date date) {
    return modificationCtx.isModifiedAfter(date);
   }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#isModifiedAfter(java.util.Date,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public boolean isModifiedAfter(Date date, Language language) {
    return modificationCtx.isModifiedAfter(date, language);
  }

}