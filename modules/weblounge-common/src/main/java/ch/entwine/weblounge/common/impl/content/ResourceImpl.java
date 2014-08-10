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

package ch.entwine.weblounge.common.impl.content;

import static ch.entwine.weblounge.common.language.Localizable.LanguageResolution.Original;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.impl.content.page.PageSecurityContext;
import ch.entwine.weblounge.common.impl.language.LocalizableContent;
import ch.entwine.weblounge.common.impl.language.LocalizableObject;
import ch.entwine.weblounge.common.impl.security.SecurityContextImpl;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.Localizable;
import ch.entwine.weblounge.common.security.AccessRule;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.SecurityListener;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * A <code>Resource</code> encapsulates all data that is attached with a
 * resource URL.
 */
public abstract class ResourceImpl<T extends ResourceContent> extends LocalizableObject implements Resource<T> {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourceImpl.class);

  /** The uri */
  protected ResourceURI uri = null;

  /** Resource type */
  protected String type = null;

  /** Keywords */
  protected List<String> subjects = null;

  /** Series */
  protected List<String> series = null;

  /** True if this page should show up on the sitemap */
  protected boolean isPromoted = false;

  /** True if the page contents should be indexed */
  protected boolean isIndexed = true;

  /** Current page editor (and owner) */
  protected User lockOwner = null;

  /** Creation context */
  protected CreationContext creationCtx = null;

  /** Modification context */
  protected ModificationContext modificationCtx = null;

  /** The publishing context */
  protected PublishingContext publishingCtx = null;

  /** The security context */
  protected SecurityContextImpl securityCtx = null;

  /** The title */
  protected LocalizableContent<String> title = null;

  /** The description */
  protected LocalizableContent<String> description = null;

  /** The coverage */
  protected LocalizableContent<String> coverage = null;

  /** The rights declaration */
  protected LocalizableContent<String> rights = null;

  /** The resource content */
  protected Map<Language, T> content = null;

  /**
   * Creates a new page for the given page uri.
   * 
   * @param uri
   *          the page uri
   */
  public ResourceImpl(ResourceURI uri) {
    super(null);
    setDefaultLanguage(uri.getSite().getDefaultLanguage());
    this.uri = uri;
    this.content = new HashMap<Language, T>();
    this.creationCtx = new CreationContext();
    this.modificationCtx = new ModificationContext();
    this.publishingCtx = new PublishingContext();
    this.securityCtx = new PageSecurityContext();
    this.subjects = new ArrayList<String>();
    this.series = new ArrayList<String>();
    this.title = new LocalizableContent<String>(this);
    this.description = new LocalizableContent<String>(this);
    this.coverage = new LocalizableContent<String>(this);
    this.rights = new LocalizableContent<String>(this);

    this.creationCtx.setCreated(uri.getSite().getAdministrator(), new Date());
    this.securityCtx.setOwner(uri.getSite().getAdministrator());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setPromoted(boolean)
   */
  public void setPromoted(boolean anchor) {
    this.isPromoted = anchor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    this.isIndexed = index;
  }

  /**
   * Returns the resource type.
   * 
   * @return the resource type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the page type: news, feature, ...
   * 
   * @param type
   *          the resource type
   */
  public void setType(String type) {
    this.type = type;
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
   * @see ch.entwine.weblounge.common.content.Resource#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    uri.setIdentifier(identifier);
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
   * @see ch.entwine.weblounge.common.content.Resource#setPath(java.lang.String)
   */
  public void setPath(String path) {
    uri.setPath(path);
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
   * @see ch.entwine.weblounge.common.content.Resource#setVersion(long)
   */
  public void setVersion(long version) {
    uri.setVersion(version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    subjects.add(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addSeries(java.lang.String)
   */
  public void addSeries(String series) {
    this.series.add(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    subjects.remove(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeSeries(java.lang.String)
   */
  public void removeSeries(String series) {
    this.series.remove(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#hasSubject(java.lang.String)
   */
  public boolean hasSubject(String subject) {
    return subjects.contains(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#hasSeries(java.lang.String)
   */
  public boolean hasSeries(String series) {
    return this.series.contains(series);
  }

  /**
   * Returns the page uri.
   * 
   * @return the page uri
   */
  public ResourceURI getURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setPublished(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    publishingCtx.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    return publishingCtx.getPublisher();
  }

  /**
   * Returns the publishing start date.
   * 
   * @return the start date
   */
  public Date getPublishFrom() {
    return publishingCtx.getPublishFrom();
  }

  /**
   * Returns the publishing end date.
   * 
   * @return the end date
   */
  public Date getPublishTo() {
    return publishingCtx.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#isPromoted()
   */
  public boolean isPromoted() {
    return isPromoted;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#isIndexed()
   */
  public boolean isIndexed() {
    return isIndexed;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setCoverage(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    this.coverage.put(coverage, language);
    enableLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage()
   */
  public String getCoverage() {
    return coverage.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage(ch.entwine.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    return coverage.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getCoverage(Language language, boolean force) {
    return coverage.get(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setDescription(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setDescription(String description, Language language) {
    this.description.put(description, language);
    enableLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription()
   */
  public String getDescription() {
    return description.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription(ch.entwine.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    return description.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getDescription(Language language, boolean force) {
    return description.get(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setRights(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setRights(String rights, Language language) {
    this.rights.put(rights, language);
    enableLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getRights()
   */
  public String getRights() {
    return rights.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getRights(ch.entwine.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    return rights.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getRights(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getRights(Language language, boolean force) {
    return rights.get(language, force);
  }

  /**
   * Sets the page title in the specified language.
   * 
   * @param title
   *          the page title
   * @param language
   *          the language
   */
  public void setTitle(String title, Language language) {
    this.title.put(title, language);
    enableLanguage(language);
  }

  /**
   * Returns the page title in the active language.
   * 
   * @return the page title
   */
  public String getTitle() {
    return title.get();
  }

  /**
   * Returns the page title in the specified language or <code>null</code> if
   * this language version is not available.
   * 
   * @param language
   *          the language identifier
   * @return the page title
   */
  public String getTitle(Language language) {
    return title.get(language);
  }

  /**
   * Returns the page title in the required language. If no title can be found
   * in that language, then it will be looked up in the default language (unless
   * <code>force</code> is set to <code>true</code>). If that doesn't produce a
   * result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the title language
   * @param force
   *          <code>true</code> to force the language
   * @return the content
   */
  public String getTitle(Language language, boolean force) {
    return title.get(language, force);
  }

  /**
   * Returns the keywords that are defined for this page header.
   * 
   * @return the keywords
   */
  public String[] getSubjects() {
    String[] kw = new String[subjects.size()];
    return subjects.toArray(kw);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getSeries()
   */
  public String[] getSeries() {
    return series.toArray(new String[series.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#setOwner(ch.entwine.weblounge.common.security.User)
   */
  public void setOwner(User owner) {
    if (owner == null) {
      logger.warn("Someone tried to set the owner of '{}' to null", this);
      return;
    }
    securityCtx.setOwner(owner);
  }

  /**
   * Sets the order of evaluation for access control lists.
   * 
   * @param order
   *          the order in which to evaluate the access control entries
   */
  public void setAllowDenyOrder(Order order) {
    securityCtx.setAllowDenyOrder(order);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAllowDenyOrder()
   */
  public Order getAllowDenyOrder() {
    return securityCtx.getAllowDenyOrder();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#addAccessRule(ch.entwine.weblounge.common.security.AccessRule)
   */
  @Override
  public void addAccessRule(AccessRule rule) {
    securityCtx.addAccessRule(rule);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAccessRules()
   */
  @Override
  public SortedSet<AccessRule> getAccessRules() {
    return securityCtx.getAccessRules();
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isAllowed(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isAllowed(Action action, Authority authority) {
    return securityCtx.isAllowed(action, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDenied(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isDenied(Action action, Authority authority) {
    return securityCtx.isDenied(action, authority);
  }

  /**
   * Returns the pagelets actions, which are
   * <ul>
   * <li>READ</li>
   * <li>WRITE</li>
   * <li>TRANSLATE</li>
   * <li>MANAGE</li>
   * </ul>
   * 
   * @return the actions that can be set on the pagelet
   * @see ch.entwine.weblounge.api.security.Secured#getActions()
   */
  public Action[] getActions() {
    return actions;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (securityCtx == null)
      return null;
    return securityCtx.getOwner();
  }

  /**
   * Adds the security listener to the pagelets security context.
   * 
   * @param listener
   *          the security listener
   * @see ch.entwine.weblounge.api.security.Secured#addSecurityListener(ch.entwine.weblounge.api.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    securityCtx.addSecurityListener(listener);
  }

  /**
   * Removes the security listener from the pagelets security context.
   * 
   * @param listener
   *          the security listener
   * @see ch.entwine.weblounge.api.security.Secured#removeSecurityListener(ch.entwine.weblounge.api.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    securityCtx.removeSecurityListener(listener);
  }

  /**
   * Returns the string representation of this page header.
   * 
   * @return the string representation
   */
  public String toString() {
    return uri.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    return publishingCtx.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    return publishingCtx.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setCreated(User creator, Date creationDate) {
    if (creator == null) {
      logger.warn("Someone tried to set the creator of '{}' to null", this);
      return;
    } else if (creationDate == null) {
      logger.warn("Someone tried to set the creation date of '{}' to null", this);
      return;
    }
    creationCtx.setCreated(creator, creationDate);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    if (date == null) {
      logger.warn("Someone tried to set creation date of '{}' to null", this);
      return;
    }
    creationCtx.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getCreationDate()
   */
  public Date getCreationDate() {
    return creationCtx.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    if (user == null) {
      logger.warn("Someone tried to set creator of '{}' to null", this);
      return;
    }
    creationCtx.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getCreator()
   */
  public User getCreator() {
    return creationCtx.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    return creationCtx.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    return modificationCtx.getModificationDate();
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
    return modificationCtx.getModifier();
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
   * @see ch.entwine.weblounge.common.content.Modifiable#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setModified(User user, Date date) {
    modificationCtx.setModified(user, date);
  }

  /**
   * Returns the user holding the editing lock for this page.
   * 
   * @return the user holding the editing lock for this page
   */
  public User getLockOwner() {
    return lockOwner;
  }

  /**
   * Locks this page for editing.
   * 
   * @param user
   *          the locking user
   */
  public void lock(User user) throws IllegalStateException {
    if (lockOwner != null && !lockOwner.equals(user) && !SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      throw new IllegalStateException("The page is already locked by " + lockOwner);
    lockOwner = user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#unlock()
   */
  public User unlock() {
    User previousLockOwner = lockOwner;
    lockOwner = null;
    return previousLockOwner;
  }

  /**
   * Returns <code>true</code> if the page is locked.
   * 
   * @return <code>true</code> if this page is locked
   */
  public boolean isLocked() {
    return lockOwner != null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addContent(ch.entwine.weblounge.common.content.ResourceContent)
   */
  public void addContent(T content) {
    if (content == null)
      throw new IllegalArgumentException("Resource content cannot be null");
    this.content.put(content.getLanguage(), content);
    this.enableLanguage(content.getLanguage());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getContent(ch.entwine.weblounge.common.language.Language)
   */
  public T getContent(Language language) {
    return content.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getOriginalContent()
   */
  public T getOriginalContent() {
    T original = null;
    for (T c : content.values()) {
      if (original == null || original.isCreatedAfter(c.getCreationDate()))
        original = c;
    }
    return original;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeContent(ch.entwine.weblounge.common.language.Language)
   */
  public T removeContent(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Content language must not be null");

    boolean useOriginalLanguage = Original.equals(behavior);
    disableLanguage(language);

    // Fix handling of potential loss of original language. If the original
    // language has been deleted, the underlying implementation will switch the
    // resource to a random default language.
    if (useOriginalLanguage && !Original.equals(behavior)) {
      Site site = uri.getSite();
      if (supportsLanguage(site.getDefaultLanguage())) {
        logger.trace("Switching default language of localizable object {} to site default language {}", this, defaultLanguage);
        setOriginalLanguage(site.getDefaultLanguage());
      }
    }

    return content.remove(language);

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contents()
   */
  public Set<T> contents() {
    Set<T> contents = new HashSet<T>();
    contents.addAll(content.values());
    return contents;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contentLanguages()
   */
  public Set<Language> contentLanguages() {
    return content.keySet();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#supportsContentLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsContentLanguage(Language language) {
    return content.containsKey(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.language.LocalizableObject#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  @Override
  public Language switchTo(Language language) {
    Language l = super.switchTo(language);
    title.switchTo(language);
    description.switchTo(language);
    coverage.switchTo(language);
    rights.switchTo(language);
    return l;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#compareTo(ch.entwine.weblounge.common.language.Localizable,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (o instanceof Resource)
      return title.get(l).compareTo(((Resource<?>) o).getTitle(l));
    return title.compareTo(o, l);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return uri.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Resource) {
      return uri.equals(((Resource<?>) obj).getURI());
    }
    return false;
  }

  /**
   * Returns a XML representation of this page header.
   * 
   * @return an XML representation of this page header
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    String rootTag = toXmlRootTag();

    b.append("<").append(rootTag);
    b.append(" id=\"");
    b.append(uri.getIdentifier());
    if (StringUtils.isNotBlank(uri.getPath())) {
      b.append("\" path=\"");
      b.append(uri.getPath());
    }
    b.append("\" version=\"");
    b.append(ResourceUtils.getVersionString(uri.getVersion()));
    b.append("\">");

    // Add header
    b.append("<head>");

    // Add custom header data
    toXmlHead(b);

    b.append("<promote>");
    b.append(Boolean.toString(isPromoted));
    b.append("</promote>");

    b.append("<index>");
    b.append(Boolean.toString(isIndexed));
    b.append("</index>");

    // Metadata
    b.append("<metadata>");

    // Title
    for (Language language : title.languages()) {
      b.append("<title language=\"");
      b.append(language.getIdentifier());
      b.append("\"><![CDATA[");
      b.append(title.get(language));
      b.append("]]></title>");
    }

    // Description
    for (Language language : description.languages()) {
      b.append("<description language=\"");
      b.append(language.getIdentifier());
      b.append("\"><![CDATA[");
      b.append(description.get(language));
      b.append("]]></description>");
    }

    // Subject
    Collections.sort(subjects);
    for (String s : subjects) {
      b.append("<subject><![CDATA[");
      b.append(s);
      b.append("]]></subject>");
    }

    // Series
    Collections.sort(series);
    for (String s : series) {
      b.append("<series><![CDATA[");
      b.append(s);
      b.append("]]></series>");
    }

    // Type
    if (type != null) {
      b.append("<type><![CDATA[");
      b.append(type);
      b.append("]]></type>");
    }

    // Coverage
    for (Language language : coverage.languages()) {
      b.append("<coverage language=\"");
      b.append(language.getIdentifier());
      b.append("\"><![CDATA[");
      b.append(coverage.get(language));
      b.append("]]></coverage>");
    }

    // Rights
    for (Language language : rights.languages()) {
      b.append("<rights language=\"");
      b.append(language.getIdentifier());
      b.append("\"><![CDATA[");
      b.append(rights.get(language));
      b.append("]]></rights>");
    }

    b.append("</metadata>");

    // Created
    b.append(creationCtx.toXml());

    // Modified
    b.append(modificationCtx.toXml());

    // Published
    b.append(publishingCtx.toXml());

    // Security
    b.append(securityCtx.toXml());

    // Lock (wrap in UserImpl to prevent weblounge user details)
    if (lockOwner != null) {
      b.append("<locked>");
      b.append(lockOwner.toXml());
      b.append("</locked>");
    }

    b.append("</head>");

    // Add custom body
    StringBuffer body = new StringBuffer();
    body = toXmlBody(body);
    if (body.length() > 0) {
      b.append("<body>");
      b.append(body);
      b.append("</body>");
    } else {
      b.append("<body/>");
    }

    b.append("</").append(rootTag).append(">");

    return b.toString();
  }

  /**
   * Returns the name of the root tag as used in {@link #toXml()}.
   * 
   * @return the root tag
   */
  protected String toXmlRootTag() {
    return "resource";
  }

  /**
   * Optionally adds additional pieces to the <tt>&lt;head&gt;</tt> section of
   * the resource's xml serialization.
   * <p>
   * Subclasses that need to store additional content in that section should
   * overwrite this method and add that information to the buffer.
   * 
   * @param buffer
   *          the string buffer
   */
  protected void toXmlHead(StringBuffer buffer) {
    return;
  }

  /**
   * Optionally adds additional pieces to the <tt>&lt;body&gt;</tt> section of
   * the resource's xml serialization.
   * <p>
   * Subclasses that need to store additional content in that section should
   * overwrite this method and add that information to the buffer. However, make
   * sure to call <code>super.toXmlBody()</code> since this implementation adds
   * the resource contents to the body.
   * 
   * @param buffer
   *          the string buffer
   */
  protected StringBuffer toXmlBody(StringBuffer buffer) {
    List<T> orderedContentList = new ArrayList<T>(this.content.size());
    orderedContentList.addAll(this.content.values());
    Collections.sort(orderedContentList);
    for (ResourceContent content : orderedContentList) {
      buffer.append(content.toXml());
    }
    return buffer;
  }

}