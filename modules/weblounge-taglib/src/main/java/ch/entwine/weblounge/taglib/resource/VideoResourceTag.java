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
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceURIImpl;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.security.SystemAction;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;

/**
 * This tag loads a video resource that is defined by an identifier or a path
 * from the content repository.
 * <p>
 * If it is found, the video resource is defined in the jsp context variable
 * <code>video</code>, otherwise, the tag body is skipped altogether.
 */
public class VideoResourceTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = 2047795554694030193L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(VideoResourceTag.class);

  /** The video resource identifier */
  private String videoId = null;

  /** The video resource path */
  private String videoResourcePath = null;

  /**
   * Sets the video resource identifier.
   * 
   * @param id
   *          the video resource identifier
   */
  public void setUuid(String id) {
    videoId = id;
  }

  /**
   * Sets the video resource path. If both path and uuid have been defined, the
   * uuid takes precedence.
   * 
   * @param path
   *          the video resource path
   */
  public void setPath(String path) {
    videoResourcePath = path;
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
    Language language = request.getLanguage();

    ContentRepository repository = site.getContentRepository();
    if (repository == null) {
      logger.debug("Unable to load content repository for site '{}'", site);
      response.invalidate();
      return SKIP_BODY;
    }

    // Create the resource uri, either from the id or the path. If none is
    // specified, and we are not in jsp compilation mode, issue a warning
    ResourceURI uri = null;
    if (StringUtils.isNotBlank(videoId)) {
      uri = new MovieResourceURIImpl(site, null, videoId);
    } else if (StringUtils.isNotBlank(videoResourcePath)) {
      uri = new MovieResourceURIImpl(site, videoResourcePath);
    } else {
      throw new JspException("Neither video id nor video path were specified");
    }

    // Try to load the video resource from the content repository
    try {
      if (!repository.exists(uri)) {
        logger.warn("Non existing video resource {} requested on {}", uri, request.getUrl());
        return SKIP_BODY;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to look up video resource {} from {}", videoId, repository);
      return SKIP_BODY;
    }

    MovieResource video = null;
    MovieContent videoContent = null;

    // Store the result in the jsp page context
    try {
      video = (MovieResource) repository.get(uri);
      video.switchTo(language);
      videoContent = video.getContent(language);
      if (videoContent == null)
        videoContent = video.getOriginalContent();
    } catch (ContentRepositoryException e) {
      logger.warn("Error trying to load video resource " + uri + ": " + e.getMessage(), e);
      return SKIP_BODY;
    }

    if (!SecurityUtils.userHasPermission(request.getUser(), video, SystemAction.READ)) {
      logger.debug("User {} has no read permission on video {}", SecurityUtils.getUser(), video);
      return SKIP_BODY;
    }

    // Store the resource and the resource content in the request
    stashAndSetAttribute(VideoResourceTagExtraInfo.VIDEO, video);
    stashAndSetAttribute(VideoResourceTagExtraInfo.VIDEO_CONTENT, videoContent);

    // Add cache tags to the response
    response.addTag(CacheTag.Resource, video.getURI().getIdentifier());
    response.addTag(CacheTag.Url, video.getURI().getPath());

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
    videoId = null;
    videoResourcePath = null;
  }

}