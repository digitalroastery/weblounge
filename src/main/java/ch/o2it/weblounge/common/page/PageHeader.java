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

import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Node;

import java.util.Date;

/**
 * A <code>PageHeader</code> encapsulates all page header data like title of a
 * page, type, layout, access restrictions etc.
 * 
 * TODO Integrate into Page with lazy loading?
 */
public interface PageHeader extends Localizable, Securable {

  /** Request page header identifier */
  public static final String ID = "pageheader";

  /** Page headlines in request */
  static final String HEADLINES = "headlines";

  /** The page's permissions */
  static final Permission[] permissions = new Permission[] { SystemPermission.READ, SystemPermission.WRITE, SystemPermission.TRANSLATE, SystemPermission.PUBLISH, SystemPermission.MANAGE };

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  public Site getSite();

  /**
   * Returns the page version.
   * 
   * @return the page version
   */
  public long getVersion();

  /**
   * Returns the page type, which is used to include this page into news lists
   * etc.
   * 
   * @return the page type
   */
  public String getType();

  /**
   * Returns the page uri.
   * 
   * @return the page url
   */
  public PageURI getURI();

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
  public Pagelet getHeadline(String moduleId, String pageletId, User user);

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
  public Pagelet getHeadline(String moduleId, String pageletId, User user,
      Permission permission);

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
   * Returns an XML representation of this page header.
   * 
   * @return an XML representation of this page header
   */
  public Node toXml();

}