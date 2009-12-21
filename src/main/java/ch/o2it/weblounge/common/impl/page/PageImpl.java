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
import ch.o2it.weblounge.common.impl.content.ModificationContext;
import ch.o2it.weblounge.common.impl.content.PublishingContext;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.security.PermissionSecurityContext;
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
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A <code>Page</code> encapsulates all data that is attached with a site URL.
 */
public class PageImpl extends LocalizableObject implements Page {

  /** The uri */
  protected PageURIImpl uri = null;

  /** PageHeader type */
  protected String type = null;

  /** PageHeader keywords */
  protected List<String> subjects = null;

  /** Renderer identifier */
  protected String template = null;

  /** Layout identifier */
  protected String layout = null;

  /** True if this page should show up on the sitemap */
  protected boolean isPromoted = false;

  /** True if the page contents should be indexed */
  protected boolean isIndexed = true;

  /** The title pagelets */
  protected List<Pagelet> headlines = null;

  /** Current page editor (and owner) */
  protected User lockOwner = null;

  /** Creation context */
  protected CreationContext creationCtx = null;

  /** Modification context */
  protected ModificationContext modificationCtx = null;

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
  public PageImpl(PageURIImpl uri) {
    super(uri.getSite().getDefaultLanguage());
    this.uri = uri;
    this.creationCtx = new CreationContext();
    this.modificationCtx = new ModificationContext();
    this.publishingCtx = new PublishingContext();
    this.securityCtx = new PageSecurityContext();
    this.subjects = new ArrayList<String>();
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
   * @see ch.o2it.weblounge.common.page.Page#setPromoted(boolean)
   */
  public void setPromoted(boolean anchor) {
    this.isPromoted = anchor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    this.isIndexed = index;
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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#hasSubject(java.lang.String)
   */
  public boolean hasSubject(String subject) {
    return subjects.contains(subject);
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
   * @see ch.o2it.weblounge.common.content.Publishable#getPublisher()
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
   * @see ch.o2it.weblounge.common.page.Page#isPromoted()
   */
  public boolean isPromoted() {
    return isPromoted;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.page.Page#isIndexed()
   */
  public boolean isIndexed() {
    return isIndexed;
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
   * @see ch.o2it.weblounge.common.content.Modifiable#setModified(ch.o2it.weblounge.common.user.User,
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
  public void setLocked(User user) throws IllegalStateException {
    if (lockOwner != null && !lockOwner.equals(user))
      throw new IllegalStateException("The page is already locked by " + lockOwner);
    lockOwner = user;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.page.Page#setUnlocked()
   */
  public User setUnlocked() {
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
      for (int i = position + 1; i < c.size(); i++) {
        c.get(i).getURI().setPosition(i);
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
      throw new IndexOutOfBoundsException("No pagelet found at position " + position);

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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (o instanceof Page)
      return title.get(l).compareTo(((Page) o).getTitle(l));
    return title.compareTo(o, l);
  }

  /**
   * Returns a XML representation of this page header.
   * 
   * @return an XML representation of this page header
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();

    b.append("<page id=\"");
    b.append(uri.id);
    b.append("\" version=\"");
    b.append(PageUtils.getVersionString(uri.version));
    b.append("\">");

    // Add header
    b.append("<head>");

    b.append("<template>");
    b.append(template);
    b.append("</template>");

    b.append("<layout>");
    b.append(layout);
    b.append("</layout>");

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
    
    // Type
    b.append("<type><![CDATA[");
    b.append(type);
    b.append("]]></type>");

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
    
    // Pagelock
    if (lockOwner != null) {
      b.append("<locked>");
      b.append(lockOwner.toXml());
      b.append("</locked>");
    }

    b.append("</head>");

    b.append("<body>");

    for (Map.Entry<String, List<Pagelet>> entry : composers.entrySet()) {
      b.append("<composer id=\"");
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