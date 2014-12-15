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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageContentListener;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.Localizable;
import ch.entwine.weblounge.common.security.AccessRule;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.SecurityListener;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * Mock implementation of a page that can be used to satisfy components that
 * rely on there being a page but without the requirement to containing the
 * actual content.
 */
public class MockPageImpl implements Page {

  /** The page uri */
  protected ResourceURI uri = null;

  /** The fake composer */
  protected ComposerImpl composer = null;

  /**
   * Creates a new mock page that will pretend to contain each and every
   * composer but without any pagelets in there.
   * 
   * @param site
   *          the site
   */
  public MockPageImpl(Site site) {
    this(site, (Pagelet[]) null);
  }

  /**
   * Creates a new mock page that will pretend to contain each and every
   * composer and that these composers contain the given list of pagelets as
   * their content.
   * 
   * @param site
   *          the site
   * @param pagelets
   *          the pagelets to pretend as contents
   */
  public MockPageImpl(Site site, Pagelet... pagelets) {
    uri = new PageURIImpl(site, "/");
    composer = new ComposerImpl("main");
    if (pagelets != null && pagelets.length > 0) {
      composer.setPagelets(pagelets);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getURI()
   */
  public ResourceURI getURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setType(java.lang.String)
   */
  public void setType(String type) {
    uri.setType(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getType()
   */
  public String getType() {
    return uri.getType();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    uri.setIdentifier(identifier);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getIdentifier()
   */
  public String getIdentifier() {
    return uri.getIdentifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setPath(java.lang.String)
   */
  public void setPath(String path) {
    uri.setPath(path);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getPath()
   */
  public String getPath() {
    return uri.getPath();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setVersion(long)
   */
  public void setVersion(long version) {
    uri.setVersion(version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getVersion()
   */
  public long getVersion() {
    return uri.getVersion();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setPromoted(boolean)
   */
  public void setPromoted(boolean promoted) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#isPromoted()
   */
  public boolean isPromoted() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#isIndexed()
   */
  public boolean isIndexed() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#isLocked()
   */
  public boolean isLocked() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getLockOwner()
   */
  public User getLockOwner() {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#lock(ch.entwine.weblounge.common.security.User)
   */
  public void lock(User user) throws IllegalStateException {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#unlock()
   */
  public User unlock() {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#setStationary(boolean)
   */
  public void setStationary(boolean stationary) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#isStationary()
   */
  public boolean isStationary() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addSeries(java.lang.String)
   */
  public void addSeries(String series) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeSeries(java.lang.String)
   */
  public void removeSeries(String series) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#hasSubject(java.lang.String)
   */
  public boolean hasSubject(String subject) {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#hasSeries(java.lang.String)
   */
  public boolean hasSeries(String series) {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getSubjects()
   */
  public String[] getSubjects() {
    return new String[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getSeries()
   */
  public String[] getSeries() {
    return new String[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getTitle()
   */
  public String getTitle() {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getTitle(ch.entwine.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getTitle(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getTitle(Language language, boolean force) {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setTitle(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription()
   */
  public String getDescription() {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription(ch.entwine.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getDescription(Language language, boolean force) {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setDescription(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setDescription(String description, Language language) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage()
   */
  public String getCoverage() {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage(ch.entwine.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getCoverage(Language language, boolean force) {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setCoverage(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getRights()
   */
  public String getRights() {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getRights(ch.entwine.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getRights(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getRights(Language language, boolean force) {
    return new String("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setRights(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setRights(String rights, Language language) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setCreated(User user, Date date) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setModified(User user, Date date) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setPublished(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addContent(ch.entwine.weblounge.common.content.ResourceContent)
   */
  public void addContent(ResourceContent content) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getContent(ch.entwine.weblounge.common.language.Language)
   */
  public ResourceContent getContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getOriginalContent()
   */
  public ResourceContent getOriginalContent() {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeContent(ch.entwine.weblounge.common.language.Language)
   */
  public ResourceContent removeContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contents()
   */
  public Set<ResourceContent> contents() {
    return new HashSet<ResourceContent>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#toXml()
   */
  public String toXml() {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#supportsLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    return languages().contains(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    return new HashSet<Language>(Arrays.asList(uri.getSite().getLanguages()));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contentLanguages()
   */
  public Set<Language> contentLanguages() {
    return new HashSet<Language>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#supportsContentLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsContentLanguage(Language language) {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    return language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#compareTo(ch.entwine.weblounge.common.language.Localizable,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    return uri.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    return uri.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    return new Date(0);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    return getCreationDate().after(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    return new UserImpl(uri.getSite().getAdministrator());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    return new Date(0);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.Modifiable#getLastModified()
   */
  @Override
  public Date getLastModified() {
    Date date = getModificationDate();
    if (date != null)
      return date;
    date = getPublishFrom();
    if (date != null)
      return date;
    return getCreationDate();
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    return new UserImpl(uri.getSite().getAdministrator());
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.Modifiable#getLastModifier()
   */
  @Override
  public User getLastModifier() {
    User user = getModifier();
    if (user != null)
      return user;
    user = getPublisher();
    if (user != null)
      return user;
    return getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    return new UserImpl(uri.getSite().getAdministrator());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    return new Date(0);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    return new Date(Long.MAX_VALUE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#setOwner(ch.entwine.weblounge.common.security.User)
   */
  public void setOwner(User owner) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    return new UserImpl(uri.getSite().getAdministrator());
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDefaultAccess()
   */
  @Override
  public boolean isDefaultAccess() {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#setAllowDenyOrder(ch.entwine.weblounge.common.security.Securable.Order)
   */
  @Override
  public void setAllowDenyOrder(Order order) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAllowDenyOrder()
   */
  @Override
  public Order getAllowDenyOrder() {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#addAccessRule(ch.entwine.weblounge.common.security.AccessRule)
   */
  @Override
  public void addAccessRule(AccessRule rule) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAccessRules()
   */
  @Override
  public SortedSet<AccessRule> getAccessRules() {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isAllowed(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  public boolean isAllowed(Action action, Authority authority) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDenied(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  public boolean isDenied(Action action, Authority authority) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getActions()
   */
  public Action[] getActions() {
    return Resource.actions;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#addSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#removeSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#setLayout(java.lang.String)
   */
  public void setLayout(String layout) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getLayout()
   */
  public String getLayout() {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#setTemplate(java.lang.String)
   */
  public void setTemplate(String template) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getTemplate()
   */
  public String getTemplate() {
    return uri.getSite().getDefaultTemplate().getIdentifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getComposer(java.lang.String)
   */
  public Composer getComposer(String composerId) {
    composer.setIdentifier(composerId);
    return composer;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getStage()
   */
  public Composer getStage() {
    composer.setIdentifier("stage");
    return composer;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getComposers()
   */
  public Composer[] getComposers() {
    return new Composer[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#addPagelet(ch.entwine.weblounge.common.content.page.Pagelet,
   *      java.lang.String)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer) {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#addPagelet(ch.entwine.weblounge.common.content.page.Pagelet,
   *      java.lang.String, int)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer, int index)
      throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#removePagelet(java.lang.String,
   *      int)
   */
  public Pagelet removePagelet(String composer, int index)
      throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Not implemented in mock page");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getPagelets()
   */
  public Pagelet[] getPagelets() {
    return composer.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getPagelets(java.lang.String)
   */
  public Pagelet[] getPagelets(String composer) {
    this.composer.setIdentifier(composer);
    return this.composer.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getPagelets(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public Pagelet[] getPagelets(String composer, String module, String id) {
    return this.composer.getPagelets(module, id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#getPreview()
   */
  public Pagelet[] getPreview() {
    composer.setIdentifier("preview");
    return this.composer.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#addPageContentListener(ch.entwine.weblounge.common.content.page.PageContentListener)
   */
  public void addPageContentListener(PageContentListener listener) {
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Page#removePageContentListener(ch.entwine.weblounge.common.content.page.PageContentListener)
   */
  public void removePageContentListener(PageContentListener listener) {
  }

}
