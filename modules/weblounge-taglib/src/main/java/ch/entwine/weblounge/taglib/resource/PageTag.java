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

package ch.entwine.weblounge.taglib.resource;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

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

    ContentRepository repository = site.getContentRepository();
    if (repository == null) {
      logger.debug("Unable to load content repository for site '{}'", site);
      response.invalidate();
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
    } else {
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
    stashAndSetAttribute(PageTagExtraInfo.PAGE, page);
    
    // Add the cache tags to the response
    response.addTag(CacheTag.Resource, page.getURI().getIdentifier());
    response.addTag(CacheTag.Url, page.getURI().getPath());

    return EVAL_BODY_INCLUDE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
   */
  public int doEndTag() throws JspException {
    removeAndUnstashAttributes();
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    pageId = null;
    pagePath = null;
  }

}