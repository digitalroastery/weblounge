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
import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.content.CreationContextImpl;
import ch.o2it.weblounge.common.impl.content.LocalizedModificationContextImpl;
import ch.o2it.weblounge.common.impl.content.PublishingContextImpl;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.security.PermissionSecurityContext;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageContentListener;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.page.Pagelet;
import ch.o2it.weblounge.common.page.PageletURI;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityContext;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.AuthenticatedUser;
import ch.o2it.weblounge.common.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A <code>Page</code> encapsulates all data that is attached with a site URL.
 */
public class PageImpl extends LocalizableObject implements Page {

  /** The uri */
  protected PageURIImpl uri = null;

  /** PageHeader type */
  protected String type = null;

  /** PageHeader keywords */
  protected Set<String> subjects = null;

  /** Renderer identifier */
  protected String template = null;

  /** Layout identifier */
  protected String layout = null;

  /** True if this page should show up on the sitemap */
  protected boolean isAnchorpage = false;

  /** True if the page contents should be indexed */
  protected boolean index = true;

  /** The title pagelets */
  protected List<Pagelet> headlines = null;

  /** Current page editor (and owner) */
  protected User lockOwner = null;

  /** Creation context */
  protected CreationContext creationCtx = null;

  /** Modification context */
  protected LocalizedModificationContext modificationCtx = null;

  /** The publishing context */
  protected PublishingContext publishingCtx = null;

  /** The security context */
  protected PermissionSecurityContext securityCtx = null;

  /** The title */
  protected LocalizableContent<String> title = null;

  /** The description */
  protected LocalizableContent<String> description = null;

  /** The coverage */
  protected LocalizableContent<String> coverage = null;

  /** The rights declaration */
  protected LocalizableContent<String> rights = null;

  /** The pagelet container */
  protected Map<String, List<Pagelet>> composers = null;

  /** The page content listeners */
  private List<PageContentListener> contentListeners = null;

  /**
   * Creates a new page for the given page uri.
   * 
   * @param uri
   *          the page uri
   */
  PageImpl(PageURIImpl uri) {
    super(uri.getSite().getDefaultLanguage());
    this.uri = uri;
    this.creationCtx = new CreationContextImpl();
    this.modificationCtx = new LocalizedModificationContextImpl();
    this.publishingCtx = new PublishingContextImpl();
    this.securityCtx = new PageSecurityContext();
    this.subjects = new HashSet<String>();
    this.headlines = new ArrayList<Pagelet>();
    this.title = new LocalizableContent<String>(this);
    this.description = new LocalizableContent<String>(this);
    this.coverage = new LocalizableContent<String>(this);
    this.rights = new LocalizableContent<String>(this);
    this.composers = new HashMap<String, List<Pagelet>>();
  }

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  public Site getSite() {
    return uri.getSite();
  }

  /**
   * Returns the page version.
   * 
   * @return the page version
   */
  public long getVersion() {
    return uri.getVersion();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#setAnchorpage(boolean)
   */
  public void setAnchorpage(boolean anchor) {
    this.isAnchorpage = anchor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    this.index = index;
  }

  /**
   * Returns the page type, which is used to include this page into news lists
   * etc.
   * 
   * @return the page type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the page type: news, feature, ...
   * 
   * @param type
   *          the page type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    subjects.add(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    subjects.remove(subject);
  }

  /**
   * Returns the page uri.
   * 
   * @return the page uri
   */
  public PageURI getURI() {
    return uri;
  }

  /**
   * Returns the publishing context of this page in the current version. The
   * context tells whether the pagelet may be published on a certain point in
   * time or not.
   * 
   * @return the publishing context
   */
  public PublishingContext getPublishingContext() {
    return publishingCtx;
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
   * @see ch.o2it.weblounge.common.content.Publishable#setPublisher(ch.o2it.weblounge.common.user.User)
   */
  public void setPublisher(User user) {
    publishingCtx.setPublisher(user);
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
   * @see ch.o2it.weblounge.common.content.Publishable#setPublishFrom(java.util.Date)
   */
  public void setPublishFrom(Date from) {
    publishingCtx.setPublishFrom(from);
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#setPublishTo(java.util.Date)
   */
  public void setPublishTo(Date to) {
    publishingCtx.setPublishFrom(to);
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
   * @see ch.o2it.weblounge.common.page.Page#isAnchorpage()
   */
  public boolean isAnchorpage() {
    return isAnchorpage;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#isIndexed()
   */
  public boolean isIndexed() {
    return isIndexed();
  }

  /**
   * Returns the headline for the given user regarding the read permission that
   * have been defined on the title pagelets. If no suitable headline is found,
   * <code>null</code> is returned.
   * 
   * @param moduleId
   *          the pagelet's module identifier
   * @param pageletId
   *          the pagelet identifier
   * @param user
   *          the user that wants access to the header
   * @return the first suitable headline pagelet
   */
  public Pagelet getHeadline(String moduleId, String pageletId,
      AuthenticatedUser user) {
    return getHeadline(moduleId, pageletId, user, SystemPermission.READ);
  }

  /**
   * Returns the headline for the given user regarding the permissions that have
   * been defined on the title pagelets. If no suitable headline is found,
   * <code>null</code> is returned.
   * 
   * @param moduleId
   *          the pagelet's module identifier
   * @param pageletId
   *          the pagelet identifier
   * @param user
   *          the user that wants access to the header
   * @param permission
   *          the permission requirements
   * @return the first suitable headline pagelet
   */
  public Pagelet getHeadline(String moduleId, String pageletId,
      AuthenticatedUser user, Permission permission) {
    for (int i = 0; i < headlines.size(); i++) {
      Pagelet headline = headlines.get(i);
      if (headline.getModule().equals(moduleId) && headline.getIdentifier().equals(pageletId))
        if (headline.checkOne(permission, user.getRoleClosure()) || headline.check(permission, user))
          return headline;
    }
    return null;
  }

  /**
   * Returns the headline pagelets
   * 
   * @return the headline pagelets
   */
  public Pagelet[] getHeadlines() {
    Pagelet[] h = new Pagelet[headlines.size()];
    return headlines.toArray(h);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#setCoverage(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    this.coverage.put(coverage, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getCoverage()
   */
  public String getCoverage() {
    return coverage.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getCoverage(ch.o2it.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    return coverage.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getCoverage(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getCoverage(Language language, boolean force) {
    return coverage.get(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#setDescription(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setDescription(String description, Language language) {
    this.description.put(description, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getDescription()
   */
  public String getDescription() {
    return description.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getDescription(ch.o2it.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    return description.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getDescription(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getDescription(Language language, boolean force) {
    return description.get(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#setRights(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setRights(String rights, Language language) {
    this.rights.put(rights, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getRights()
   */
  public String getRights() {
    return rights.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getRights(ch.o2it.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    return rights.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#getRights(ch.o2it.weblounge.common.language.Language,
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
   * Returns the layout associated with this page.
   * 
   * @return the associated layout
   */
  public String getLayout() {
    return layout;
  }

  /**
   * Sets the layout that is used to determine default content and initial
   * layout.
   * 
   * @param layout
   *          the layout identifier
   */
  public void setLayout(String layout) {
    this.layout = layout;
  }

  /**
   * Returns the template that is used to render this page.
   * 
   * @return the renderer
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets the renderer that is used to render this page.
   * 
   * @param template
   *          the template identifier
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * Returns the keywords that are defined for this page header.
   * 
   * @return the keywords
   */
  public String[] getSubjects() {
    String kw[] = new String[subjects.size()];
    return subjects.toArray(kw);
  }

  /**
   * Sets the keywords that are defined on this page.
   * 
   * @param keywords
   */
  public void setKeywords(String[] keywords) {
    this.subjects.clear();
    for (int i = 0; i < keywords.length; i++)
      this.subjects.add(keywords[i]);
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
   * @see ch.o2it.weblounge.common.security.Securable#setOwner(ch.o2it.weblounge.common.user.User)
   */
  public void setOwner(User owner) {
    securityCtx.setOwner(owner);
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
   * the secured object in a way that satisfies the given {@link PermissionSet}
   * <code>p</code>.
   * 
   * @param p
   *          the required set of permissions
   * @param a
   *          the authorization used to access to the secured object
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#getOwner()
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
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return uri.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Page) {
      return uri.equals(((Page) obj).getURI());
    }
    return false;
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
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished()
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
   * Returns the page's {@link CreationContext}.
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
   * Returns the page's {@link ModificationContext}.
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
   * @see ch.o2it.weblounge.common.content.Modifiable#isModifiedAfter(java.util.Date)
   */
  public boolean isModifiedAfter(Date date) {
    return modificationCtx.isModifiedAfter(date);
  }

  /**
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#isModifiedAfter(java.util.Date,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public boolean isModifiedAfter(Date date, Language language) {
    return modificationCtx.isModifiedAfter(date, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#isModifiedBefore(java.util.Date)
   */
  public boolean isModifiedBefore(Date date) {
    return modificationCtx.isModifiedBefore(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.LocalizedModifiable#isModifiedBefore(java.util.Date,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public boolean isModifiedBefore(Date date, Language language) {
    return modificationCtx.isModifiedBefore(date, language);
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
   * @see ch.o2it.weblounge.common.content.Modifiable#isModified()
   */
  public boolean isModified() {
    return modificationCtx.isModified();
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
   * Returns the user holding the editing lock for this page.
   * 
   * @return the user holding the editing lock for this page
   */
  public User getLockOwner() {
    return lockOwner;
  }

  /**
   * Locks this page for editing and returns <code>true</code> if the page was
   * previously unlocked or the user is the same that already had a lock on this
   * page.
   * 
   * @param user
   *          the locking user
   * @return <code>true</code> if the page was locked successfully
   */
  boolean lock(AuthenticatedUser user) {
    if (lockOwner == null || lockOwner.equals(user)) {
      lockOwner = user;
      return true;
    }
    return false;
  }

  /**
   * Removes the editing lock from this page and returns <code>true</code> if
   * the page was previously unlocked, the user was the one holding the lock or
   * the user is a publisher.
   * 
   * @param user
   *          the unlocking user
   * @return <code>true</code> if the page was unlocked successfully
   */
  boolean unlock(AuthenticatedUser user) {
    if (lockOwner == null || lockOwner.equals(user) || user.hasRole(SystemRole.PUBLISHER)) {
      lockOwner = null;
      return true;
    }
    return false;
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
   * Returns <code>true</code> if the page is locked by <code>user</code>.
   * 
   * @return <code>true</code> if this page is locked by <code>user</code>
   */
  public boolean isLocked(User user) {
    return isLocked() && user.equals(lockOwner);
  }

  /**
   * Adds the pagelet to the composer located in this page.
   * 
   * @param pagelet
   *          the pagelet to add
   * @param composer
   *          the composer identifier
   */
  void appendPagelet(Pagelet pagelet, String composer) {
    List<Pagelet> c = composers.get(composer);
    if (c == null) {
      c = new ArrayList<Pagelet>();
      composers.put(composer, c);
    }
    c.add(pagelet);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#addPagelet(ch.o2it.weblounge.common.page.Pagelet,
   *      java.lang.String)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer) {
    List<Pagelet> c = composers.get(composer);
    int position = (c == null) ? 0 : c.size();
    return addPagelet(pagelet, composer, position);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#addPagelet(ch.o2it.weblounge.common.page.Pagelet,
   *      java.lang.String, int)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer, int position) {
    List<Pagelet> c = composers.get(composer);
    if (c == null) {
      c = new ArrayList<Pagelet>();
      composers.put(composer, c);
    }

    // Test position
    if (position < 0 || position > c.size())
      throw new IndexOutOfBoundsException("There are only " + c.size() + " pagelets in the composer");

    // Insert
    if (position < c.size()) {
      c.add(position, pagelet);
      for (int i = position; i < c.size(); i++) {
        ((PageletURIImpl) ((PageletImpl) c.get(i)).getURI()).setPosition(i);
      }
    }

    // Append
    else {
      c.add(pagelet);
    }

    // Adjust pagelet location
    PageletURI location = pagelet.getURI();
    if (location == null) {
      location = new PageletURIImpl(uri, composer, position);
      pagelet.setURI(location);
    } else {
      location.setURI(uri);
      location.setComposer(composer);
      location.setPosition(position);
    }
    return pagelet;
  }

  /**
   * Moves the pagelet up by one step.
   * 
   * @param composer
   *          the composer identifier
   * @param position
   *          the pagelet's original position
   */
  void movePageletUp(String composer, int position) {
    List<Pagelet> c = composers.get(composer);
    if (c == null || c.size() - 1 < position) {
      return;
    }
    c.add(position - 1, c.remove(position));
    ((PageletURIImpl) ((PageletImpl) c.get(position - 1)).getURI()).setPosition(position - 1);
    ((PageletURIImpl) ((PageletImpl) c.get(position)).getURI()).setPosition(position);
  }

  /**
   * Moves the pagelet down by one step.
   * 
   * @param composer
   *          the composer identifier
   * @param position
   *          the pagelet's original position
   */
  void movePageletDown(String composer, int position) {
    movePageletUp(composer, position + 1);
  }

  /**
   * Returns the pagelets that are contained in the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @return the pagelets
   */
  public Pagelet[] getPagelets(String composer) {
    List<Pagelet> c = composers.get(composer);
    if (c == null) {
      c = new ArrayList<Pagelet>();
    }
    Pagelet[] pagelets = new Pagelet[c.size()];
    return c.toArray(pagelets);
  }

  /**
   * Returns a copy of the pagelets of the given module and renderer that are
   * contained in the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @param module
   *          the module identifier
   * @param id
   *          the renderer id
   * @return the pagelets
   */
  public Pagelet[] getPagelets(String composer, String module, String id) {
    List<Pagelet> l = new ArrayList<Pagelet>();
    List<Pagelet> c = composers.get(composer);
    if (c != null) {
      l.addAll(c);
      int i = 0;
      while (i < l.size()) {
        Pagelet p = l.get(i);
        if (!p.getModule().equals(module) || !p.getIdentifier().equals(id)) {
          l.remove(i);
        } else {
          i++;
        }
      }
    }
    Pagelet[] pagelets = new Pagelet[l.size()];
    return l.toArray(pagelets);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#removePagelet(java.lang.String,
   *      int)
   */
  public Pagelet removePagelet(String composer, int position) {
    List<Pagelet> pagelets = composers.get(composer);

    // Test index
    if (pagelets == null || pagelets.size() < position)
      throw new IndexOutOfBoundsException("No pagelet at position " + position + " found");

    // Remove the pagelet and update uris of following pagelets
    Pagelet pagelet = pagelets.remove(position);
    for (int i = position; i < pagelets.size(); i++) {
      pagelets.get(i).getURI().setPosition(i);
    }
    return pagelet;
  }

  /**
   * Adds a <code>PageContentListener</code> to this page, who will be notified
   * (amongst others) about new, moved, deleted or altered pagelets.
   * 
   * @param listener
   *          the new page content listener
   */
  public void addPageContentListener(PageContentListener listener) {
    if (contentListeners == null)
      contentListeners = new ArrayList<PageContentListener>();
    contentListeners.add(listener);
  }

  /**
   * Removes a <code>PageContentListener</code> from this page.
   * 
   * @param listener
   *          the page content listener
   */
  public void removePageContentListener(PageContentListener listener) {
    if (contentListeners == null)
      return;
    contentListeners.remove(listener);
  }

  /**
   * Returns the document name for the given version. For the live version, this
   * method will return <code>index.xml</code>. Available versions are:
   * <ul>
   * <li>{@link #LIVE}</li>
   * <li>{@link #WORK}</li>
   * <li>{@link #ORIGINAL}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static String getDocument(long version) {
    if (version == LIVE)
      return "index.xml";
    else if (version == ORIGINAL)
      return "original.xml";
    else if (version == WORK)
      return "work.xml";
    else
      return Long.toString(version) + ".xml";
  }

  /**
   * Returns the version identifier for the given version. Available versions
   * are:
   * <ul>
   * <li>{@link #LIVE}</li>
   * <li>{@link #WORK}</li>
   * <li>{@link #ORIGINAL}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static String getVersion(long version) {
    if (version == LIVE)
      return "live";
    else if (version == ORIGINAL)
      return "original";
    else if (version == WORK)
      return "work";
    else
      return Long.toString(version);
  }

  /**
   * Returns the version for the given version identifier. Available versions
   * are:
   * <ul>
   * <li>{@link #LIVE}</li>
   * <li>{@link #WORK}</li>
   * <li>{@link #ORIGINAL}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static long getVersion(String version) {
    if (version.equals("index")) {
      return Page.LIVE;
    } else if (version.equals("work")) {
      return Page.WORK;
    } else if (version.equals("original")) {
      return Page.ORIGINAL;
    } else {
      try {
        return Long.parseLong(version);
      } catch (NumberFormatException e) {
        return -1;
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    return title.compareTo(o, l);
  }

  /**
   * Returns a XML representation of this page header.
   * 
   * @return an XML representation of this page header
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();

    b.append("<page>");

    // Add header
    b.append("<header>");

    b.append("<renderer>");
    b.append(template);
    b.append("</renderer>\n");

    b.append("<layout>");
    b.append(layout);
    b.append("</layout>\n");

    b.append("<type>");
    b.append(type);
    b.append("</type>\n");

    b.append(publishingCtx.toXml());
    b.append(securityCtx.toXml());

    if (subjects.size() != 0) {
      b.append("<keywords>\n");
      for (String k : subjects) {
        b.append("<keyword><![CDATA[");
        b.append(k);
        b.append("]]></keyword>\n");
      }
      b.append("</keywords>\n");
    } else {
      b.append("<keywords/>\n");
    }

    for (Language l : languages) {
      b.append("<title language=\"");
      b.append(l.getIdentifier());
      b.append("\"><![CDATA[");
      b.append(getTitle(l));
      b.append("]]></title>\n");
    }

    for (Language l : modificationCtx.languages()) {
      b.append("<modified language=\"");
      b.append(l.getIdentifier());
      b.append("\">");
      b.append("<date>");
      Date d = modificationCtx.getModificationDate(l);
      if (d == null)
        d = new Date();
      b.append(WebloungeDateFormat.formatStatic(d));
      b.append("</date>\n");
      b.append("<user>");
      User u = modificationCtx.getModifier(l);
      if (u == null)
        u = uri.getSite().getAdministrator();
      b.append(u.getLogin());
      b.append("</user>\n");
      b.append("</modified>\n");
    }

    b.append("</header>");

    b.append("<body>");

    for (Map.Entry<String, List<Pagelet>> entry : composers.entrySet()) {
      b.append("<composer name=\"");
      b.append(entry.getKey());
      b.append("\">");

      for (Pagelet pagelet : entry.getValue()) {
        b.append(pagelet.toXml());
      }

      b.append("</composer>");
    }

    b.append("</body>");

    b.append("</page>");

    /**
     * try { InputSource is = new InputSource(new StringReader(b.toString()));
     * DocumentBuilder docBuilder = XMLUtilities.getDocumentBuilder(); Document
     * doc = docBuilder.parse(is); return doc.getFirstChild(); } catch
     * (SAXException e) { log_.error("Error building dom tree for pagelet", e);
     * } catch (IOException e) { log_.error("Error reading pagelet xml", e); }
     * catch (ParserConfigurationException e) {
     * log_.error("Error parsing pagelet xml", e); }
     */

    return b.toString();
  }

}