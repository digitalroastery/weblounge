/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.workbench;

import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.content.page.PageletURI;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Wrapper around a {@link ch.o2it.weblounge.common.content.page.Pagelet} that
 * will replace pagelet contents and properties with their trimpath equivalents.
 */
public class TrimpathPageletWrapper implements Pagelet {

  /** The wrapped pagelet */
  protected Pagelet pagelet = null;

  /**
   * Creates a new wrapper around <code>pagelet</code>.
   * 
   * @param pagelet
   *          the pagelet
   */
  public TrimpathPageletWrapper(Pagelet pagelet) {
    this.pagelet = pagelet;
  }

  /**
   * Returns the trimpath expression for the element with name
   * <code>element</code>.
   * 
   * @param element
   *          the element name
   * @param language
   *          the language
   * @param force
   *          whether to force the language
   * @return the trimpath expression
   */
  protected String replaceContent(String element, Language language,
      boolean force) {
    StringBuilder sb = new StringBuilder();
    sb.append("{ pagelet.locale.text['").append(element).append("'] }");
    return sb.toString();
  }

  /**
   * Returns the trimpath expression for the multivalued element with name
   * <code>element</code>.
   * 
   * @param element
   *          the element name
   * @param language
   *          the language
   * @param force
   *          whether to force the language
   * @return the trimpath expression
   */
  protected String[] replaceMultivalueContent(String element,
      Language language, boolean force) {
    List<String> replacements = new ArrayList<String>();
    for (int i = 0; i < pagelet.getMultiValueContent(element, language, force).length; i++) {
      StringBuilder sb = new StringBuilder();
      sb.append("{ pagelet.locale.text['").append(element).append("'][").append(i).append("] }");
      replacements.add(sb.toString());
    }
    return replacements.toArray(new String[replacements.size()]);
  }

  /**
   * Returns the trimpath expression for the property with name
   * <code>property</code>.
   * 
   * @param property
   *          the property name
   * @return the trimpath expression
   */
  protected String replaceProperty(String property) {
    StringBuilder sb = new StringBuilder();
    sb.append("{ pagelet.properties.property['").append(property).append("'] }");
    return sb.toString();
  }

  /**
   * Returns the trimpath expression for the property with name
   * <code>property</code>.
   * 
   * @param property
   *          the property name
   * @return the trimpath expression
   */
  protected String[] replaceMultivalueProperty(String property) {
    List<String> replacements = new ArrayList<String>();
    for (int i = 0; i < pagelet.getMultiValueProperty(property).length; i++) {
      StringBuilder sb = new StringBuilder();
      sb.append("{ pagelet.properties.property['").append(property).append("'][").append(i).append("] }");
      replacements.add(sb.toString());
    }
    return replacements.toArray(new String[replacements.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#setOwner(ch.o2it.weblounge.common.user.User)
   */
  public void setOwner(User owner) {
    pagelet.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    return pagelet.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    pagelet.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    return pagelet.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    return pagelet.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public void allow(Permission permission, Authority authority) {
    pagelet.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#setCreator(ch.o2it.weblounge.common.user.User)
   */
  public void setCreator(User user) {
    pagelet.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    return pagelet.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getLastModificationDate()
   */
  public Date getLastModificationDate() {
    return pagelet.getLastModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    return pagelet.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    return pagelet.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getLastModifier()
   */
  public User getLastModifier() {
    return pagelet.getLastModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#deny(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public void deny(Permission permission, Authority authority) {
    pagelet.deny(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    return pagelet.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    return pagelet.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getModificationDate(ch.o2it.weblounge.common.language.Language)
   */
  public Date getModificationDate(Language language) {
    return pagelet.getModificationDate(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getModule()
   */
  public String getModule() {
    return pagelet.getModule();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    return pagelet.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getIdentifier()
   */
  public String getIdentifier() {
    return pagelet.getIdentifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(Permission permission, Authority authority) {
    return pagelet.check(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getPropertyNames()
   */
  public String[] getPropertyNames() {
    return pagelet.getPropertyNames();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getModifier(ch.o2it.weblounge.common.language.Language)
   */
  public User getModifier(Language language) {
    return pagelet.getModifier(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#setProperty(java.lang.String,
   *      java.lang.String)
   */
  public void setProperty(String key, String value) {
    pagelet.setProperty(key, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getModificationDate()
   */
  public Date getModificationDate() {
    return pagelet.getModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#supportsLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    return pagelet.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.PermissionSet,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(PermissionSet permissions, Authority authority) {
    return pagelet.check(permissions, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getProperty(java.lang.String)
   */
  public String getProperty(String key) {
    return replaceProperty(key);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#isMultiValueProperty(java.lang.String)
   */
  public boolean isMultiValueProperty(String key) {
    return pagelet.isMultiValueProperty(key);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    return pagelet.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#getModifier()
   */
  public User getModifier() {
    return pagelet.getModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#checkOne(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    return pagelet.checkOne(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getMultiValueProperty(java.lang.String)
   */
  public String[] getMultiValueProperty(String key) {
    return replaceMultivalueProperty(key);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#switchTo(ch.o2it.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    return pagelet.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#checkAll(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    return pagelet.checkAll(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getContentNames(ch.o2it.weblounge.common.language.Language)
   */
  public String[] getContentNames(Language language) {
    return pagelet.getContentNames(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#setContent(java.lang.String,
   *      java.lang.String, ch.o2it.weblounge.common.language.Language)
   */
  public void setContent(String name, String value, Language language) {
    pagelet.setContent(name, value, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#permissions()
   */
  public Permission[] permissions() {
    return pagelet.permissions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    return pagelet.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#addSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    pagelet.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#removeSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    pagelet.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#isMultiValueContent(java.lang.String)
   */
  public boolean isMultiValueContent(String name) {
    return pagelet.isMultiValueContent(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getMultiValueContent(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language, boolean)
   */
  public String[] getMultiValueContent(String name, Language language,
      boolean force) {
    return replaceMultivalueContent(name, language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getMultiValueContent(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public String[] getMultiValueContent(String name, Language language) {
    return replaceMultivalueContent(name, language, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getMultiValueContent(java.lang.String)
   */
  public String[] getMultiValueContent(String name) {
    return replaceMultivalueContent(name, null, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    return pagelet.toString(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public String getContent(String name, Language language) {
    return replaceContent(name, language, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    return pagelet.toString(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getContent(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language, boolean)
   */
  public String getContent(String name, Language language, boolean force) {
    return replaceContent(name, language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getContent(java.lang.String)
   */
  public String getContent(String name) {
    return replaceContent(name, null, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#setURI(ch.o2it.weblounge.common.content.page.PageletURI)
   */
  public void setURI(PageletURI uri) {
    pagelet.setURI(uri);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#getURI()
   */
  public PageletURI getURI() {
    return pagelet.getURI();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#setCreated(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  public void setCreated(User creator, Date creationDate) {
    pagelet.setCreated(creator, creationDate);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, ch.o2it.weblounge.common.language.Language)
   */
  public void setModified(User user, Date date, Language language) {
    pagelet.setModified(user, date, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#setPublished(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    pagelet.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Pagelet#toXml()
   */
  public String toXml() {
    return pagelet.toXml();
  }

}
