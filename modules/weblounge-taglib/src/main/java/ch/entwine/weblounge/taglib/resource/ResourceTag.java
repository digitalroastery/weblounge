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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.impl.content.GeneralResourceURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;

/**
 * This tag loads an resource that is defined by an identifier or a path from
 * the content repository.
 * <p>
 * If it is found, the resource is defined in the jsp context variable
 * <code>resource</code>, otherwise, the tag body is skipped altogether.
 */
public class ResourceTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = 2047795554694030193L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourceTag.class);

  /** The resource identifier */
  private String resourceId = null;

  /** The resource path */
  private String resourcePath = null;

  /**
   * Sets the resource identifier.
   * 
   * @param id
   *          resource identifier
   */
  public void setUuid(String id) {
    resourceId = id;
  }

  /**
   * Sets the resource path. If both path and uuid have been defined, the uuid
   * takes precedence.
   * 
   * @param path
   *          resource path
   */
  public void setPath(String path) {
    resourcePath = path;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {

    // Don't do work if not needed (which is the case during precompilation)
    if (RequestUtils.isPrecompileRequest(request))
      return SKIP_BODY;

    Site site = request.getSite();

    ContentRepository repository = site.getContentRepository();
    if (repository == null) {
      logger.debug("Unable to load content repository for site '{}'", site);
      response.invalidate();
      return SKIP_BODY;
    }

    // Create the resource uri, either from the id or the path. If none is
    // specified, and we are not in jsp compilation mode, issue a warning
    ResourceURI uri = null;
    if (StringUtils.isNotBlank(resourceId)) {
      uri = new GeneralResourceURIImpl(site, null, resourceId);
    } else if (StringUtils.isNotBlank(resourcePath)) {
      uri = new GeneralResourceURIImpl(site, resourcePath);
    } else {
      throw new JspException("Neither uuid nor path were specified for resource");
    }

    // Try to load the resource from the content repository
    try {
      if (!repository.exists(uri)) {
        logger.warn("Non existing resource {} requested on {}", uri, request.getUrl());
        return SKIP_BODY;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to look up resource {} from {}", resourceId, repository);
      return SKIP_BODY;
    }

    Resource<?> resource = null;
    ResourceContent resourceContent = null;

    // Try to determine the language
    Language language = request.getLanguage();

    // Store the result in the jsp page context
    try {
      resource = repository.get(uri);
      resource.switchTo(language);

      Language contentLanguage = null;
      contentLanguage = LanguageUtils.getPreferredContentLanguage(resource, request, site);
      if (contentLanguage == null) {
        logger.warn("Resource {} does not have suitable content", resource);
        return SKIP_BODY;
      }

      resourceContent = resource.getContent(contentLanguage);
      if (resourceContent == null)
        resourceContent = resource.getOriginalContent();
    } catch (ContentRepositoryException e) {
      logger.warn("Error trying to load resource " + uri + ": " + e.getMessage(), e);
      return SKIP_BODY;
    }

    // TODO: Check the permissions

    // Store the resource and the resource content in the request
    stashAndSetAttribute(ResourceTagExtraInfo.RESOURCE, resource);
    stashAndSetAttribute(ResourceTagExtraInfo.RESOURCE_CONTENT, resourceContent);

    // Add cache tags to the response
    response.addTag(CacheTag.Resource, resource.getURI().getIdentifier());
    response.addTag(CacheTag.Url, resource.getURI().getPath());

    return EVAL_BODY_INCLUDE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
   */
  @Override
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
    resourceId = null;
    resourcePath = null;
  }

}