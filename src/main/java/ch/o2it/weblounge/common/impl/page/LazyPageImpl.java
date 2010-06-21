/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

import ch.o2it.weblounge.common.content.Composer;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageContentListener;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Set;

/**
 * Implementation of a lazy loading page.
 */
public class LazyPageImpl implements Page {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LazyPageImpl.class);
  
  /** The page content as a byte array containing the page's xml */
  protected byte[] pageXml = null;

  /** The backing page */
  protected PageImpl page = null;

  /** The page uri */
  protected PageURI uri = null;

  /**
   * Creates a new lazy page.
   * 
   * @param uri
   *          the page uri
   * @param data
   *          the page's data
   */
  public LazyPageImpl(PageURI uri, byte[] data) {
    this.uri = uri;
    this.pageXml = data;
  }
  
  /**
   * Loads the page.
   */
  protected void loadPage() {
    PageReader reader = new PageReader();
    try {
      page = reader.read(new ByteArrayInputStream(pageXml), uri);
    } catch (Exception e) {
      logger.error("Failed to lazy-load {}", uri);
      throw new IllegalStateException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#addPageContentListener(ch.o2it.weblounge.common.content.PageContentListener)
   */
  public void addPageContentListener(PageContentListener listener) {
    if (page == null)
      loadPage();
    page.addPageContentListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#addPagelet(ch.o2it.weblounge.common.content.Pagelet,
   *      java.lang.String)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer) {
    if (page == null)
      loadPage();
    return page.addPagelet(pagelet, composer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#addPagelet(ch.o2it.weblounge.common.content.Pagelet,
   *      java.lang.String, int)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer, int index)
      throws IndexOutOfBoundsException {
    if (page == null)
      loadPage();
    return page.addPagelet(pagelet, composer, index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    if (page == null)
      loadPage();
    page.addSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getComposers()
   */
  public Composer[] getComposers() {
    if (page == null)
      loadPage();
    return page.getComposers();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getCoverage()
   */
  public String getCoverage() {
    if (page == null)
      loadPage();
    return getCoverage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getCoverage(ch.o2it.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    if (page == null)
      loadPage();
    return page.getCoverage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getCoverage(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getCoverage(Language language, boolean force) {
    if (page == null)
      loadPage();
    return page.getCoverage(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getDescription()
   */
  public String getDescription() {
    if (page == null)
      loadPage();
    return page.getDescription();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getDescription(ch.o2it.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    if (page == null)
      loadPage();
    return page.getDescription(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getDescription(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getDescription(Language language, boolean force) {
    if (page == null)
      loadPage();
    return getDescription(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getLayout()
   */
  public String getLayout() {
    if (page == null)
      loadPage();
    return page.getLayout();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getLockOwner()
   */
  public User getLockOwner() {
    if (page == null)
      loadPage();
    return page.getLockOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getPagelets()
   */
  public Pagelet[] getPagelets() {
    if (page == null)
      loadPage();
    return page.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getPagelets(java.lang.String)
   */
  public Pagelet[] getPagelets(String composer) {
    if (page == null)
      loadPage();
    return page.getPagelets(composer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getPagelets(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public Pagelet[] getPagelets(String composer, String module, String id) {
    if (page == null)
      loadPage();
    return page.getPagelets(composer, module, id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getPreview()
   */
  public Pagelet[] getPreview() {
    if (page == null)
      loadPage();
    return page.getPreview();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getRights()
   */
  public String getRights() {
    if (page == null)
      loadPage();
    return page.getRights();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getRights(ch.o2it.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    if (page == null)
      loadPage();
    return page.getRights(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getRights(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getRights(Language language, boolean force) {
    if (page == null)
      loadPage();
    return page.getRights(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getSubjects()
   */
  public String[] getSubjects() {
    if (page == null)
      loadPage();
    return page.getSubjects();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getTemplate()
   */
  public String getTemplate() {
    if (page == null)
      loadPage();
    return page.getTemplate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getTitle()
   */
  public String getTitle() {
    if (page == null)
      loadPage();
    return page.getTitle();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getTitle(ch.o2it.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    if (page == null)
      loadPage();
    return page.getTitle(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getTitle(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getTitle(Language language, boolean force) {
    if (page == null)
      loadPage();
    return page.getTitle(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getType()
   */
  public String getType() {
    if (page == null)
      loadPage();
    return page.getType();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#getURI()
   */
  public PageURI getURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#hasSubject(java.lang.String)
   */
  public boolean hasSubject(String subject) {
    if (page == null)
      loadPage();
    return page.hasSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#isIndexed()
   */
  public boolean isIndexed() {
    if (page == null)
      loadPage();
    return page.isIndexed();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#isLocked()
   */
  public boolean isLocked() {
    if (page == null)
      loadPage();
    return page.isLocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#isPromoted()
   */
  public boolean isPromoted() {
    if (page == null)
      loadPage();
    return page.isPromoted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#removePageContentListener(ch.o2it.weblounge.common.content.PageContentListener)
   */
  public void removePageContentListener(PageContentListener listener) {
    if (page == null)
      loadPage();
    page.removePageContentListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#removePagelet(java.lang.String,
   *      int)
   */
  public Pagelet removePagelet(String composer, int index)
      throws IndexOutOfBoundsException {
    if (page == null)
      loadPage();
    return page.removePagelet(composer, index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    if (page == null)
      loadPage();
    page.removeSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setCoverage(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    if (page == null)
      loadPage();
    page.setCoverage(coverage, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setDescription(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setDescription(String description, Language language) {
    if (page == null)
      loadPage();
    page.setDescription(description, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    if (page == null)
      loadPage();
    page.setIndexed(index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setLayout(java.lang.String)
   */
  public void setLayout(String layout) {
    if (page == null)
      loadPage();
    page.setLayout(layout);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setLocked(ch.o2it.weblounge.common.user.User)
   */
  public void setLocked(User user) throws IllegalStateException {
    if (page == null)
      loadPage();
    page.setLocked(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  public void setModified(User user, Date date) {
    if (page == null)
      loadPage();
    page.setModified(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setPromoted(boolean)
   */
  public void setPromoted(boolean promoted) {
    if (page == null)
      loadPage();
    page.setPromoted(promoted);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setPublished(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    if (page == null)
      loadPage();
    page.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setRights(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setRights(String rights, Language language) {
    if (page == null)
      loadPage();
    page.setRights(rights, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setTemplate(java.lang.String)
   */
  public void setTemplate(String template) {
    if (page == null)
      loadPage();
    page.setTemplate(template);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setTitle(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    if (page == null)
      loadPage();
    page.setTitle(title, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setType(java.lang.String)
   */
  public void setType(String type) {
    if (page == null)
      loadPage();
    page.setType(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#setUnlocked()
   */
  public User setUnlocked() {
    if (page == null)
      loadPage();
    return page.setUnlocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Page#toXml()
   */
  public String toXml() {
    if (page == null)
      loadPage();
    return page.toXml();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (page == null)
      loadPage();
    return page.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    if (page == null)
      loadPage();
    return page.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#supportsLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    if (page == null)
      loadPage();
    return page.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#switchTo(ch.o2it.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    if (page == null)
      loadPage();
    return page.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    if (page == null)
      loadPage();
    return page.toString(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    if (page == null)
      loadPage();
    return page.toString(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    if (page == null)
      loadPage();
    return page.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    if (page == null)
      loadPage();
    return page.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    if (page == null)
      loadPage();
    return page.getModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    if (page == null)
      loadPage();
    return page.getModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    if (page == null)
      loadPage();
    return page.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    if (page == null)
      loadPage();
    return page.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    if (page == null)
      loadPage();
    return page.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    if (page == null)
      loadPage();
    return page.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#addSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    if (page == null)
      loadPage();
    page.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public void allow(Permission permission, Authority authority) {
    if (page == null)
      loadPage();
    page.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(Permission permission, Authority authority) {
    if (page == null)
      loadPage();
    return page.check(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.PermissionSet,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(PermissionSet permissions, Authority authority) {
    if (page == null)
      loadPage();
    return page.check(permissions, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#checkAll(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    if (page == null)
      loadPage();
    return page.checkAll(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#checkOne(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    if (page == null)
      loadPage();
    return page.checkOne(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#deny(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public void deny(Permission permission, Authority authority) {
    if (page == null)
      loadPage();
    page.deny(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (page == null)
      loadPage();
    return page.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#permissions()
   */
  public Permission[] permissions() {
    if (page == null)
      loadPage();
    return page.permissions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#removeSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (page == null)
      loadPage();
    page.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#setOwner(ch.o2it.weblounge.common.user.User)
   */
  public void setOwner(User owner) {
    if (page == null)
      loadPage();
    page.setOwner(owner);
  }

}
