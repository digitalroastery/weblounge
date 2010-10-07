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

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;

/**
 * This tag loads the page header defined by the given url from the database. If
 * it is found, the header is defined in the page context, otherwise, the tag
 * body is skipped.
 */
public class PageHeaderTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = 2047795554694030193L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PageHeaderTag.class.getName());

  /** The page identifier */
  private String pageId = null;

  /**
   * Sets the page identifier.
   * 
   * @param id
   *          page identifier
   */
  public void setPageid(String id) {
    pageId = id;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() {
    Site site = request.getSite();
    ContentRepository repository = ContentRepositoryFactory.getRepository(site);
    if (repository == null) {
      logger.warn("Unable to load content repository for site '{}'", site);
      return SKIP_BODY;
    }

    // Try to load the page
    ResourceURI uri = new PageURIImpl(site, null, pageId);
    Page page = null;
    try {
      page = (Page)repository.get(uri);
    } catch (ContentRepositoryException e) {
      logger.warn("Error trying to load page preview " + uri + ": " + e.getMessage(), e);
      return SKIP_BODY;
    } finally {
      if (page == null) {
        logger.warn("Page {} requested non existing page header {}", request.getUrl(), uri);
        return SKIP_BODY;
      }
    }

    // TODO: Check the permissions

    // Store the preview in the request
    Pagelet[] preview = page.getPreview();
    if (preview != null && preview.length > 0) {
      pageContext.setAttribute(PageHeaderTagVariables.PREVIEW, preview);
      return EVAL_BODY_INCLUDE;
    } else
      return SKIP_BODY;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(PageHeaderTagVariables.PREVIEW);
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.taglib.WebloungeTag#reset()
   */
  protected void reset() {
    super.reset();
    pageId = null;
  }

}