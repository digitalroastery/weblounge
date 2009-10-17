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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.content.LocalizedModifiable;
import ch.o2it.weblounge.common.content.Publishable;
import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.security.User;

import org.w3c.dom.Node;

import java.util.Date;

/**
 * A <code>Page</code> encapsulates all data that is attached with a site url.
 * For performance reasons, this object keeps parts of the page data in memory
 * and maintains indexes to speed up building different language versions.
 */
public interface Page extends Localizable, LocalizedModifiable, Publishable, Securable {

  /** Request page identifier */
  String ID = "page";

  /** Page headlines in request */
  public static final String HEADLINES = "headlines";

  /** Live version of a page */
  long LIVE = 0;

  /** Original version of a page */
  long ORIGINAL = 1;

  /** Work version of a page */
  long WORK = 2;

  /** The page's permissions */
  static final Permission[] permissions = new Permission[] {
      SystemPermission.READ,
      SystemPermission.WRITE,
      SystemPermission.TRANSLATE,
      SystemPermission.PUBLISH,
      SystemPermission.MANAGE };

  /**
   * Returns the page uri.
   * 
   * @return the page url
   */
  public PageURI getURI();

  /**
   * Returns the page type, which is used to include this page into news lists
   * etc.
   * 
   * @return the page type
   */
  public String getType();

  /**
   * True to include this page in the sitemap.
   * 
   * @return <code>true</code> to include in sitemap
   */
  boolean inSitemap();

  /**
   * Returns <code>true</code> if the page is locked.
   * 
   * @return <code>true</code> if this page is locked
   */
  boolean isLocked();

  /**
   * Returns <code>true</code> if the page is locked by <code>user</code>.
   * 
   * @return <code>true</code> if this page is locked by <code>user</code>
   */
  boolean isLocked(User user);

  /**
   * Returns the user holding the editing lock for this page.
   * 
   * @return the user holding the editing lock for this page
   */
  User getEditor();

  /**
   * Returns the publishing context of this page in the current version. The
   * context tells whether the pagelet may be published on a certain point in
   * time or not.
   * 
   * @return the publishing context
   */
  public PublishingContext getPublishingContext();

  /**
   * Returns the publishing start date.
   * 
   * @return the start date
   */
  public Date getPublishFrom();

  /**
   * Returns the publishing end date.
   * 
   * @return the end date
   */
  public Date getPublishTo();

  /**
   * Returns the keywords that are defined for this page header.
   * 
   * @return the keywords
   */
  public String[] getKeywords();

  /**
   * Returns the page title in the active language.
   * 
   * @return the content
   */
  String getTitle();

  /**
   * Returns the page title in the specified language or <code>null</code> if
   * this language version is not available.
   * 
   * @param l
   *          the language identifier
   * @return the page title
   */
  public String getTitle(Language l);

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
  String getTitle(Language language, boolean force);

  /**
   * Returns the headline pagelets
   * 
   * @return the headline pagelets
   */
  public Pagelet[] getHeadlines();

  /**
   * Returns the modification date of the page.
   * 
   * @return the modification date
   */
  public Date getModifiedSince();

  /**
   * Returns the modification user of the page.
   * 
   * @return the modification date
   */
  public User getModifiedBy();

  /**
   * Returns the layout associated with this page.
   * 
   * @return the associated layout
   */
  public Layout getLayout();

  /**
   * Returns the renderer that is used to render this page.
   * 
   * @param method
   *          the rendering method
   * @return the renderer
   */
  public Renderer getRenderer(String method);

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
  Pagelet getPagelet(String composer, int index, User u, Permission p);

  /**
   * Returns the composers for the given version.
   * 
   * @param version
   *          the page version
   * @return the composers
   */
  String[] getComposers(long version);

  /**
   * Returns the pagelets for the given version.
   * 
   * @param version
   *          the page version
   * @return the pagelets
   */
  Pagelet[] getPagelets(long version);

  /**
   * Returns the pagelets that are contained in the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @return the pagelets
   */
  Pagelet[] getPagelets(String composer);

  /**
   * Returns the pagelets of the given module and renderer that are contained in
   * the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @param module
   *          the module identifier
   * @param id
   *          the renderer id
   * @return the pagelets
   */
  Pagelet[] getPagelets(String composer, String module, String id);

  /**
   * Adds a <code>PageContentListener</code> to this page, who will be notified
   * (amongst others) about new, moved, deleted or altered pagelets.
   * 
   * @param listener
   *          the new page content listener
   */
  void addPageContentListener(PageContentListener listener);

  /**
   * Removes a <code>PageContentListener</code> from this page.
   * 
   * @param listener
   *          the page content listener
   */
  void removePageContentListener(PageContentListener listener);

  /**
   * Returns an XML representation of this page header.
   * 
   * @return an XML representation of this page header
   */
  public Node toXml();

}