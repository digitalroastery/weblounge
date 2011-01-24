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

package ch.o2it.weblounge.taglib.resource;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;

/**
 * This tag loads an page that is defined by an identifier or a path from the
 * content repository.
 * <p>
 * If it is found, the page is defined in the jsp context variable
 * <code>page</code>, otherwise, the tag body is skipped altogether.
 */
public class PageTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = 2047795554694030193L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PageTag.class.getName());

  /** The page identifier */
  private String pageId = null;

  /** The page path */
  private String pagePath = null;

  /**
   * Sets the page identifier.
   * 
   * @param id
   *          page identifier
   */
  public void setUuid(String id) {
    pageId = id;
  }

  /**
   * Sets the page path. If both path and uuid have been defined, the uuid takes
   * precedence.
   * 
   * @param path
   *          page path
   */
  public void setPath(String path) {
    pagePath = path;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {
    Site site = request.getSite();
    Language language = request.getLanguage();

    ContentRepository repository = ContentRepositoryFactory.getRepository(site);
    if (repository == null) {
      logger.warn("Unable to load content repository for site '{}'", site);
      return SKIP_BODY;
    }

    // Create the page uri, either from the id or the path. If none is
    // specified, and we are not in jsp compilation mode, issue a warning
    ResourceURI uri = null;
    if (StringUtils.isNotBlank(pageId)) {
      uri = new PageURIImpl(site, null, pageId);
    } else if (StringUtils.isNotBlank(pagePath)) {
      uri = new PageURIImpl(site, pagePath, null);
    } else if (!request.getRequestURI().endsWith(".jsp")) {
      logger.warn("Neither uuid nor path were specified for image");
      return SKIP_BODY;
    }

    // Try to load the page from the content repository
    try {
      if (!repository.exists(uri)) {
        logger.warn("Non existing page {} requested on {}", uri, request.getUrl());
        return SKIP_BODY;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to look up page {} from {}", pageId, repository);
      return SKIP_BODY;
    }

    Page page = null;

    // Store the result in the jsp page context
    try {
      page = (Page) repository.get(uri);
      language = LanguageUtils.getPreferredLanguage(page, request, site);
      page.switchTo(language);
    } catch (ContentRepositoryException e) {
      logger.warn("Error trying to load page " + uri + ": " + e.getMessage(), e);
      return SKIP_BODY;
    }

    // TODO: Check the permissions

    // Store the page and the page content in the request
    pageContext.setAttribute(PageTagExtraInfo.PAGE, page);

    return EVAL_BODY_INCLUDE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(PageTagExtraInfo.PAGE);
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    pageId = null;
    pagePath = null;
  }

}