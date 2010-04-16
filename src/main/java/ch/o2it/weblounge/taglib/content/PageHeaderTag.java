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

package ch.o2it.weblounge.taglib.content;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;

/**
 * This tag loads the page header defined by the given url from the database. If
 * it is found, it defines the header in the page context, otherwise, the tag
 * body is skipped.
 */
public class PageHeaderTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = 2047795554694030193L;

  /** Logging facility provided by log4j */
  private final static Logger log_ = LoggerFactory.getLogger(PageHeaderTag.class);

  /** The page partition */
  private String partition = null;

  /** The page path */
  private String path = null;

  /** The page header */
  private Pagelet header = null;

  /** The requested version */
  private long version = Page.LIVE;

  /**
   * Creates a new page header list tag.
   */
  public PageHeaderTag() {
    reset();
  }

  /**
   * Sets the partition to the requested page header.
   * 
   * @param partition
   *          The partition to set.
   */
  public void setPartition(String partition) {
    this.partition = partition;
  }

  /**
   * Sets the path within the specified partition.
   * 
   * @param path
   *          The path to set.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    header = null;
    Site site = request.getSite();
    User user = request.getUser();
    Permission p = SystemPermission.READ;
    WebUrl url = site.getNavigation().getUrl(partition, path);
    if (url == null) {
      log_.warn("Page " + request.getUrl() + " requested non existing page header [partition=" + partition + "; path=" + path + "]");
      return SKIP_BODY;
    }
    header = PageHeaderManager.getHeader(url, site, user, p, version);
    if (header != null) {
      pageContext.setAttribute(PageHeaderTagVariables.HEADER, header);
      return EVAL_BODY_INCLUDE;
    } else
      return SKIP_BODY;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(PageHeaderTagVariables.HEADER);
    reset();
    return super.doEndTag();
  }

  /**
   * Initializes and resets this tag instance.
   */
  protected void reset() {
    super.reset();
    version = Page.LIVE;
    partition = null;
    path = null;
  }

}
