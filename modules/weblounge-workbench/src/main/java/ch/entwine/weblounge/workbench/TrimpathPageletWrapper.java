/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.workbench;

import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.Localizable;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.SecurityListener;
import ch.entwine.weblounge.common.security.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Wrapper around a {@link ch.entwine.weblounge.common.content.page.Pagelet}
 * that will replace pagelet contents and properties with their trimpath
 * equivalents.
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
    sb.append("${ locale.current.text.").append(element).append(" }");
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
      sb.append("${ locale.current.text.").append(element).append("[").append(i).append("] }");
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
    sb.append("${ properties.property.").append(property).append(" }");
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
      sb.append("${ properties.property.").append(property).append("[").append(i).append("] }");
      replacements.add(sb.toString());
    }
    return replacements.toArray(new String[replacements.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#setOwner(ch.entwine.weblounge.common.security.User)
   */
  public void setOwner(User owner) {
    pagelet.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    return pagelet.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    pagelet.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    return pagelet.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    return pagelet.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAllowDenyOrder()
   */
  public Order getAllowDenyOrder() {
    return pagelet.getAllowDenyOrder();
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#allow(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public void allow(Action action, Authority authority) {
    pagelet.allow(action, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isAllowed(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isAllowed(Action action, Authority authority) {
    return pagelet.isAllowed(action, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    pagelet.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    return pagelet.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.LocalizedModifiable#getLastModificationDate()
   */
  public Date getLastModificationDate() {
    return pagelet.getLastModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    return pagelet.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    return pagelet.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.LocalizedModifiable#getLastModifier()
   */
  public User getLastModifier() {
    return pagelet.getLastModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#deny(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public void deny(Action action, Authority authority) {
    pagelet.deny(action, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDenied(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isDenied(Action action, Authority authority) {
    return pagelet.isDenied(action, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    return pagelet.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    return pagelet.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.LocalizedModifiable#getModificationDate(ch.entwine.weblounge.common.language.Language)
   */
  public Date getModificationDate(Language language) {
    return pagelet.getModificationDate(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getModule()
   */
  public String getModule() {
    return pagelet.getModule();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    return pagelet.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getIdentifier()
   */
  public String getIdentifier() {
    return pagelet.getIdentifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getPropertyNames()
   */
  public String[] getPropertyNames() {
    return pagelet.getPropertyNames();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.LocalizedModifiable#getModifier(ch.entwine.weblounge.common.language.Language)
   */
  public User getModifier(Language language) {
    return pagelet.getModifier(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#addProperty(java.lang.String,
   *      java.lang.String)
   */
  public void addProperty(String key, String value) {
    pagelet.addProperty(key, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#setProperty(java.lang.String,
   *      java.lang.String)
   */
  public void setProperty(String key, String value) {
    pagelet.setProperty(key, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#removeProperty(java.lang.String)
   */
  public void removeProperty(String key) {
    pagelet.removeProperty(key);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.LocalizedModifiable#getModificationDate()
   */
  public Date getModificationDate() {
    return pagelet.getModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#supportsLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    return pagelet.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getProperty(java.lang.String)
   */
  public String getProperty(String key) {
    return replaceProperty(key);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#isMultiValueProperty(java.lang.String)
   */
  public boolean isMultiValueProperty(String key) {
    return pagelet.isMultiValueProperty(key);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    return pagelet.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.LocalizedModifiable#getModifier()
   */
  public User getModifier() {
    return pagelet.getModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#setContent(java.lang.Object)
   */
  public void setContent(Object content) {
    pagelet.setContent(content);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getContent()
   */
  public Object getContent() {
    return pagelet.getContent();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getMultiValueProperty(java.lang.String)
   */
  public String[] getMultiValueProperty(String key) {
    return replaceMultivalueProperty(key);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    return pagelet.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getContentNames(ch.entwine.weblounge.common.language.Language)
   */
  public String[] getContentNames(Language language) {
    return pagelet.getContentNames(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#setContent(java.lang.String,
   *      java.lang.String, ch.entwine.weblounge.common.language.Language)
   */
  public void setContent(String name, String value, Language language) {
    pagelet.setContent(name, value, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#actions()
   */
  public Action[] actions() {
    return pagelet.actions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#compareTo(ch.entwine.weblounge.common.language.Localizable,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    return pagelet.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#addSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    pagelet.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#removeSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    pagelet.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#isMultiValueContent(java.lang.String)
   */
  public boolean isMultiValueContent(String name) {
    return pagelet.isMultiValueContent(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getMultiValueContent(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language, boolean)
   */
  public String[] getMultiValueContent(String name, Language language,
      boolean force) {
    return replaceMultivalueContent(name, language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getMultiValueContent(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public String[] getMultiValueContent(String name, Language language) {
    return replaceMultivalueContent(name, language, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getMultiValueContent(java.lang.String)
   */
  public String[] getMultiValueContent(String name) {
    return replaceMultivalueContent(name, null, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    return pagelet.toString(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getContent(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public String getContent(String name, Language language) {
    return replaceContent(name, language, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    return pagelet.toString(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getContent(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language, boolean)
   */
  public String getContent(String name, Language language, boolean force) {
    return replaceContent(name, language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getContent(java.lang.String)
   */
  public String getContent(String name) {
    return replaceContent(name, null, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#setURI(ch.entwine.weblounge.common.content.page.PageletURI)
   */
  public void setURI(PageletURI uri) {
    pagelet.setURI(uri);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#getURI()
   */
  public PageletURI getURI() {
    return pagelet.getURI();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setCreated(User creator, Date creationDate) {
    pagelet.setCreated(creator, creationDate);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, ch.entwine.weblounge.common.language.Language)
   */
  public void setModified(User user, Date date, Language language) {
    pagelet.setModified(user, date, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#setPublished(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    pagelet.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Pagelet#toXml()
   */
  public String toXml() {
    return pagelet.toXml();
  }

}
