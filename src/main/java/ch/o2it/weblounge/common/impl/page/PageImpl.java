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

import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.Layout;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageContentListener;
import ch.o2it.weblounge.common.page.PageHeader;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.page.Pagelet;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityContext;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A <code>Page</code> encapsulates all data that is attached with a site url.
 * For performance reasons, this object keeps parts of the page data in memory
 * and maintains indexes to speed up building different language versions.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class PageImpl implements Page {

  /** The page header */
  PageHeaderImpl header;

  /** The user that holds the editing lock for this page */
  User editor;

  /** The pagelet container */
  Map<String, List<Pagelet>> composers_;

  /** The page content listeners */
  private List<PageContentListener> listeners;

  /**
   * Creates a new page for the given page uri.
   * 
   * @param uri
   *          the page uri
   */
  public PageImpl(PageURI uri) {
    this(new PageHeaderImpl(uri));
  }

  /**
   * Creates a new page with default properties and the given page header.
   * 
   * @param header
   *          the page header
   */
  PageImpl(PageHeaderImpl header) {
    composers_ = new HashMap<String, List<Pagelet>>();
    this.header = header;
  }

  /**
   * Returns the page header.
   * 
   * @return the page header
   */
  public PageHeader getHeader() {
    return header;
  }

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  public Site getSite() {
    return header.site;
  }

  /**
   * Returns the page version, which is one of
   * <ul>
   * <li>{@link #LIVE}</li>
   * <li>{@link #ORIGINAL}</li>
   * <li>{@link #WORK}</li>
   * </ul>
   * 
   * @return the page version
   */
  public long getVersion() {
    return header.version;
  }

  /**
   * Returns the page type, which is used to include this page into news lists
   * etc.
   * 
   * @return the page type
   */
  public String getType() {
    return header.type;
  }

  /**
   * Returns the keywords that are defined for this page.
   * 
   * @return the keywords
   */
  public String[] getKeywords() {
    return header.getKeywords();
  }

  /**
   * Returns the user holding the editing lock for this page.
   * 
   * @return the user holding the editing lock for this page
   */
  public User getEditor() {
    return editor;
  }

  /**
   * Locks this page for editing and returns <code>true</code> if the page was
   * previously unlocked or the user is the same that already had a lock on this
   * page.
   * 
   * @param user
   *          the locking user
   * @return <code>true</code> if the page was locked successfuly
   */
  boolean lock(User user) {
    if (editor == null || editor.equals(user)) {
      editor = user;
      return true;
    }
    return false;
  }

  /**
   * Removes the editing lock from this page and returns <code>true</code> if
   * the page was previously unlocke, the user was the one holding the lock or
   * the user is a publisher.
   * 
   * @param user
   *          the unlocking user
   * @return <code>true</code> if the page was unlocked successfuly
   */
  boolean unlock(User user) {
    if (editor == null || editor.equals(user) || user.hasRole(SystemRole.PUBLISHER)) {
      editor = null;
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
    return editor != null;
  }

  /**
   * Returns <code>true</code> if the page is locked by <code>user</code>.
   * 
   * @return <code>true</code> if this page is locked by <code>user</code>
   */
  public boolean isLocked(User user) {
    return isLocked() && user.equals(editor);
  }

  /**
   * Adds the keyword to this page.
   * 
   * @param keyword
   *          the keyword to add
   */
  public void addKeyword(String keyword) {
    header.keywords_.add(keyword);
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
    List<Pagelet> c = composers_.get(composer);
    if (c == null) {
      c = new ArrayList<Pagelet>();
      composers_.put(composer, c);
    }
    c.add(pagelet);
  }

  /**
   * Adds the pagelet in the given composer at the specified position.
   * 
   * @param pagelet
   *          the pagelet to add
   * @param composer
   *          the composer identifier
   * @param position
   *          the position
   */
  void addPagelet(Pagelet pagelet, String composer, int position) {
    List<Pagelet> c = composers_.get(composer);
    if (c == null) {
      c = new ArrayList<Pagelet>();
      composers_.put(composer, c);
    }
    if (position < c.size()) {
      c.add(position, pagelet);
      for (int i = position; i < c.size(); i++) {
        ((PageletLocationImpl) ((PageletImpl) c.get(i)).getLocation()).setPosition(i);
      }
    } else {
      c.add(pagelet);
    }
  }

  /**
   * Returns the pagelet for the given composer at position <code>i</code> with
   * respect to the rights of the requesting user. This method looks up the
   * default version, which is <code>live</code>.
   * 
   * @param composer
   *          the composer identifier
   * @param index
   *          the index within the composer
   * @param u
   *          the user
   * @param p
   *          the permission to ask for
   * @return the composer content
   */
  public Pagelet getPagelet(String composer, int index, User u, Permission p) {
    List pagelets = composers_.get(composer);
    if (pagelets == null || pagelets.size() - 1 < index) {
      return null;
    }
    Pagelet pagelet = (Pagelet) pagelets.get(index);
    if (!pagelet.checkOne(p, u.getRoleClosure()) && !pagelet.check(p, u)) {
      return null;
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
    List<Pagelet> c = composers_.get(composer);
    if (c == null || c.size() - 1 < position) {
      return;
    }
    c.add(position - 1, c.remove(position));
    ((PageletLocationImpl) ((PageletImpl) c.get(position - 1)).getLocation()).setPosition(position - 1);
    ((PageletLocationImpl) ((PageletImpl) c.get(position)).getLocation()).setPosition(position);
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
   * Returns the page uri.
   * 
   * @return the page uri
   */
  public PageURI getURI() {
    return header.getURI();
  }

  /**
   * Returns the publishing context of this page in the current version. The
   * context tells whether the pagelet may be published on a certain point in
   * time or not.
   * 
   * @return the publishing context
   */
  public PublishingContext getPublishingContext() {
    return header.publishingCtx;
  }

  /**
   * Returns the publishing start date.
   * 
   * @return the start date
   */
  public Date getPublishFrom() {
    return header.publishingCtx.getFrom();
  }

  /**
   * Returns the publishing end date.
   * 
   * @return the end date
   */
  public Date getPublishTo() {
    return header.publishingCtx.getTo();
  }

  /**
   * Returns <code>true</code> if the page may be published. The output of this
   * method depends on the <code>check</code> method of the
   * <code>PublishingContext</code>.
   * 
   * @return <code>true</code> if the page may be published
   */
  public boolean isPublished() {
    return header.publishingCtx.isPublished();
  }

  /**
   * Returns the page title in the active language.
   * 
   * @return the page title
   */
  public String getTitle() {
    return header.getTitle();
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
    return header.getTitle(language);
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
    return header.getTitle(language, force);
  }

  /**
   * Sets the data when this page has been modified.
   * 
   * @param date
   *          the modification date
   */
  void setModifiedSince(Date date) {
    header.modifiedSince = date;
  }

  /**
   * Returns the modification date of the page.
   * 
   * @return the modification date
   */
  public Date getModifiedSince() {
    return header.modifiedSince;
  }

  /**
   * Sets the user that last modified the page.
   * 
   * @param user
   *          the modifying user
   */
  void setModifiedBy(User user) {
    header.modifyingUser = user;
  }

  /**
   * Returns the modification user of the page.
   * 
   * @return the modification date
   */
  public User getModifiedBy() {
    return header.modifyingUser;
  }

  /**
   * Sets the page's version.
   * 
   * @param version
   *          the version
   */
  void setVersion(long version) {
    header.version = version;
  }

  /**
   * Returns the composers for the given version.
   * 
   * @param version
   *          the page version
   * @return the composers
   */
  public String[] getComposers(long version) {
    Set<String> ids = composers_.keySet();
    return ids.toArray(new String[ids.size()]);
  }

  /**
   * Returns the pagelets for the given version.
   * 
   * @param version
   *          the page version
   * @return the pagelets
   */
  public Pagelet[] getPagelets(long version) {
    List<Pagelet> allpagelets = new ArrayList<Pagelet>();
    for (List<Pagelet> pagelets : composers_.values()) {
      allpagelets.addAll(pagelets);
    }
    return allpagelets.toArray(new Pagelet[allpagelets.size()]);
  }

  /**
   * Returns the pagelets that are contained in the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @return the pagelets
   */
  public Pagelet[] getPagelets(String composer) {
    List<Pagelet> c = composers_.get(composer);
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
    List<Pagelet> c = composers_.get(composer);
    if (c != null) {
      l.addAll(c);
      int i = 0;
      while (i < l.size()) {
        Pagelet p = l.get(i);
        if (!p.getModule().equals(module) || !p.getRenderer("html").getIdentifier().equals(id)) {
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
   * Removes the pagelet in the given composer at the specified position.
   * 
   * @param composer
   *          the composer identifier
   * @param position
   *          the position
   */
  void removePagelet(String composer, int position) {
    List c = composers_.get(composer);
    if (c == null) {
      return;
    }
    c.remove(position);
    for (int i = position; i < c.size(); i++) {
      ((PageletLocationImpl) ((PageletImpl) c.get(i)).getLocation()).setPosition(i);
    }
  }

  /**
   * Returns the layout associated with this page.
   * 
   * @return the associated layout
   */
  public Layout getLayout() {
    return header.getLayout();
  }

  /**
   * Returns the renderer that is used to render this page.
   * 
   * @param method
   *          the rendering method
   * @return the renderer
   */
  public Renderer getRenderer(String method) {
    return header.getRenderer(method);
  }

  /**
   * Adds a <code>PageContentListener</code> to this page, who will be notified
   * (amongst others) about new, moved, deleted or altered pagelets.
   * 
   * @param listener
   *          the new page content listener
   */
  public void addPageContentListener(PageContentListener listener) {
    if (listeners == null)
      listeners = new ArrayList<PageContentListener>();
    listeners.add(listener);
  }

  /**
   * Removes a <code>PageContentListener</code> from this page.
   * 
   * @param listener
   *          the page content listener
   */
  public void removePageContentListener(PageContentListener listener) {
    if (listeners == null)
      return;
    listeners.remove(listener);
  }

  /**
   * Returns the security context that is associated with this pagelet. The
   * context tells whether the pagelet may be accessed in a certain way by a
   * user or not.
   * 
   * @return the pagelet's security context
   */
  public SecurityContext getSecurityContext() {
    return header.securityCtx;
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
    return header.securityCtx.check(p, a);
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
    return header.securityCtx.check(p, a);
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
  public boolean checkOne(Permission permission, Set authorities) {
    return header.securityCtx.checkOne(permission, authorities);
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
  public boolean checkAll(Permission permission, Set authorities) {
    return header.securityCtx.checkAll(permission, authorities);
  }

  /**
   * Returns the pages permissions, which are
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
    header.securityCtx.addSecurityListener(listener);
  }

  /**
   * Removes the security listener from the pagelets security context.
   * 
   * @param listener
   *          the security listener
   * @see ch.o2it.weblounge.api.security.Secured#removeSecurityListener(ch.o2it.weblounge.api.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    header.securityCtx.removeSecurityListener(listener);
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
   * Returns the versions that are available for this page. The strings are
   * converted longs denoting the time of creation. Additionally, the versions
   * <code>index</code>, <code>work</code> and <code>original</code> may exist.
   * 
   * @return the available versions
   */
  public String[] getVersions() {
    // TODO: Load versions
    return null;
  }

  /**
   * Returns the page's current language.
   * 
   * @see ch.o2it.weblounge.Localizable.language.Multilingual#getLanguage()
   */
  public Language getLanguage() {
    return header.getLanguage();
  }

  /**
   * Sets the page's language.
   * 
   * @see ch.o2it.weblounge.Localizable.language.Multilingual#setLanguage(ch.o2it.weblounge.api.language.Language)
   */
  public void setLanguage(Language language) {
    header.setLanguage(language);
  }

  /**
   * Returns <code>true</code> if the page supports <code>language</code>.
   * 
   * @see ch.o2it.weblounge.Localizable.language.Multilingual#supportsLanguage(ch.o2it.weblounge.api.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    return header.supportsLanguage(language);
  }

  /**
   * Returns the page's string representation.
   * 
   * @see ch.o2it.weblounge.Localizable.language.Multilingual#toString(ch.o2it.weblounge.api.language.Language)
   */
  public String toString(Language language) {
    return header.toString(language);
  }

  /**
   * Returns the page's string representation or <code>null</code> if
   * <code>force</code> is set to <code>true</code> and no representation in the
   * given language could be found.
   * 
   * @see ch.o2it.weblounge.Localizable.language.Multilingual#toString(ch.o2it.weblounge.api.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    return header.toString(language, force);
  }

  /**
   * @see ch.o2it.weblounge.Localizable.language.Multilingual#compareTo(ch.o2it.weblounge.Localizable.language.Multilingual,
   *      ch.o2it.weblounge.api.language.Language)
   */
  public int compareTo(Localizable o, Language language) {
    return header.compareTo(o, language);
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    return header.compareTo(o);
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return header.dbPath;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return header.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof PageImpl) {
      PageImpl p = (PageImpl) obj;
      return header.equals(p.header);
    }
    return false;
  }

  /**
   * Returns a XML representation of this page header.
   * 
   * @return an XML representation of this page header
   */
  public Node toExportXml() {
    StringBuffer b = new StringBuffer();

    b.append("<page>");

    b.append(header.toExportXml());

    // TODO: Append body

    b.append("</page>");

    try {
      InputSource is = new InputSource(new StringReader(b.toString()));
      DocumentBuilder docBuilder = XMLUtilities.getDocumentBuilder();
      Document doc = docBuilder.parse(is);
      return doc.getFirstChild();
    } catch (SAXException e) {
      header.site.getLogger().error("Error building dom tree for pagelet", e);
    } catch (IOException e) {
      header.site.getLogger().error("Error reading pagelet xml", e);
    } catch (ParserConfigurationException e) {
      header.site.getLogger().error("Error parsing pagelet xml", e);
    }
    return null;
  }

}