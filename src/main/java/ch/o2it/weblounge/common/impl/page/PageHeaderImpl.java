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
import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.impl.security.PageSecurityContext;
import ch.o2it.weblounge.common.impl.security.PermissionSecurityContext;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Layout;
import ch.o2it.weblounge.common.page.PageHeader;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.page.Pagelet;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityContext;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.security.User;
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
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A <code>PageHeader</code> encapsulates all page header data that is attached
 * to a site uri.
 */
public class PageHeaderImpl extends LocalizableObject<PageHeader> implements PageHeader {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(PageHeader.class);

  /** The uri */
  PageURI uri = null;

  /** PageHeader type */
  String type = null;

  /** PageHeader keywords */
  List<String> keywords_ = null;

  /** Renderer identifier */
  String renderer = null;

  /** Layout identifier */
  String layout = null;

  /** The title pagelets */
  List<Pagelet> headlines = null;

  /** The publishing context */
  PublishingContext publishingCtx = null;

  /** The security context */
  PermissionSecurityContext securityCtx = null;

  /** The user that last modified the page */
  User modifyingUser = null;

  /** Date when the page was last modified */
  Date modifiedSince = null;

  /**
   * Creates a new page with default properties.
   */
  PageHeaderImpl(PageURI uri) {
    this.uri = uri;
    publishingCtx = new PublishingContextImpl();
    securityCtx = new PageSecurityContext();
    keywords_ = new ArrayList<String>();
    headlines = new ArrayList<Pagelet>();
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
    // TODO Invalidate link lists
    this.type = type;
  }

  /**
   * Adds the keyword to this page.
   * 
   * @param keyword
   *          the keyword to add
   */
  void addKeyword(String keyword) {
    keywords_.add(keyword);
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
   * Returns the publishing start date.
   * 
   * @return the start date
   */
  public Date getPublishFrom() {
    return publishingCtx.getFrom();
  }

  /**
   * Returns the publishing end date.
   * 
   * @return the end date
   */
  public Date getPublishTo() {
    return publishingCtx.getTo();
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
  public Pagelet getHeadline(String moduleId, String pageletId, User user) {
    return getHeadline(moduleId, pageletId, user, SystemPermission.READ);
  }

  /**
   * Returns the headline for the given user regarding the permissions that have
   * been defined on the title pagelets. If no suitable hedline is found,
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
  public Pagelet getHeadline(String moduleId, String pageletId, User user,
      Permission permission) {
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
   * Returns the page title in the active language.
   * 
   * @return the page title
   */
  public String getTitle() {
    return (String) getContent("title");
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
    return (String) getContent("title", language);
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
    return (String) getContent("title", language, force);
  }

  /**
   * Sets the data when this page has been modified.
   * 
   * @param date
   *          the modification date
   */
  void setModifiedSince(Date date) {
    modifiedSince = date;
  }

  /**
   * Returns the modification date of the page.
   * 
   * @return the modification date
   */
  public Date getModifiedSince() {
    return modifiedSince;
  }

  /**
   * Sets the user that last modified the page.
   * 
   * @param user
   *          the modifying user
   */
  void setModifiedBy(User user) {
    modifyingUser = user;
  }

  /**
   * Returns the modification user of the page.
   * 
   * @return the modification date
   */
  public User getModifiedBy() {
    return modifyingUser;
  }

  /**
   * Returns the layout associated with this page.
   * 
   * @return the associated layout
   */
  public Layout getLayout() {
    return uri.getSite().getLayout(layout);
  }

  /**
   * Returns the renderer that is used to render this page.
   * 
   * @param method
   *          the rendering method
   * @return the renderer
   */
  public Renderer getRenderer(String method) {
    return (renderer != null) ? uri.getSite().getRenderer(renderer, method) : null;
  }

  /**
   * Sets the renderer that is used to render this page.
   * 
   * @param renderer
   *          the renderer identifier
   */
  public void setRenderer(String renderer) {
    this.renderer = renderer;
  }

  /**
   * Returns the keywords that are defined for this page header.
   * 
   * @return the keywords
   */
  public String[] getKeywords() {
    String kw[] = new String[keywords_.size()];
    return keywords_.toArray(kw);
  }

  /**
   * Sets the keywords that are defined on this page.
   * 
   * @param keywords
   */
  public void setKeywords(String[] keywords) {
    // TODO Invalidate link lists
    keywords_.clear();
    for (int i = 0; i < keywords.length; i++)
      keywords_.add(keywords[i]);
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
   * Returns a XML representation of this page header.
   * 
   * @return an XML representation of this page header
   */
  public Node toXml() {
    StringBuffer b = new StringBuffer();

    // Add root node
    b.append("<header>");

    b.append("<renderer>");
    b.append(renderer);
    b.append("</renderer>\n");

    b.append("<layout>");
    b.append(layout);
    b.append("</layout>\n");

    b.append("<type>");
    b.append(type);
    b.append("</type>\n");

    b.append(publishingCtx.toXml());
    b.append(securityCtx.toXml());

    if (keywords_.size() != 0) {
      b.append("<keywords>\n");
      for (Iterator i = keywords_.iterator(); i.hasNext();) {
        b.append("<keyword><![CDATA[");
        b.append(i.next());
        b.append("]]></keyword>\n");
      }
      b.append("</keywords>\n");
    } else {
      b.append("<keywords/>\n");
    }

    for (Iterator i = languages(); i.hasNext();) {
      Language l = (Language) i.next();
      b.append("<title language=\"");
      b.append(l.getIdentifier());
      b.append("\"><![CDATA[");
      b.append(getTitle(l));
      b.append("]]></title>\n");
    }

    b.append("<modified>\n");
    b.append("<date>");
    Date d = modifiedSince;
    if (d == null)
      d = new Date();
    b.append(WebloungeDateFormat.formatStatic(d));
    b.append("</date>\n");
    b.append("<user>");
    User u = modifyingUser;
    if (u == null)
      u = uri.getSite().getAdministrator();
    b.append(u.getLogin());
    b.append("</user>\n");
    b.append("</modified>\n");

    b.append("</header>");

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
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return uri.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof PageHeaderImpl) {
      PageHeaderImpl h = (PageHeaderImpl) obj;
      return uri.equals(h.uri);
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

}