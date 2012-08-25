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
import ch.entwine.weblounge.common.content.file.FileContent;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;

/**
 * This tag loads an file that is defined by an identifier or a path from the
 * content repository.
 * <p>
 * If it is found, the file is defined in the jsp context variable
 * <code>file</code>, otherwise, the tag body is skipped altogether.
 */
public class FileResourceTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = 2047795554694030193L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FileResourceTag.class.getName());

  /** The file identifier */
  private String fileId = null;

  /** The file path */
  private String filePath = null;

  /**
   * Sets the file identifier.
   * 
   * @param id
   *          file identifier
   */
  public void setUuid(String id) {
    fileId = id;
  }

  /**
   * Sets the file path. If both path and uuid have been defined, the uuid takes
   * precedence.
   * 
   * @param path
   *          file path
   */
  public void setPath(String path) {
    filePath = path;
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

    // Create the file uri, either from the id or the path. If none is
    // specified, issue a warning
    ResourceURI uri = null;
    if (StringUtils.isNotBlank(fileId)) {
      uri = new FileResourceURIImpl(site, null, fileId);
    } else if (StringUtils.isNotBlank(filePath)) {
      uri = new FileResourceURIImpl(site, filePath, null);
    } else {
      throw new JspException("Neither resource id nor resource path were specified");
    }

    // Try to load the file from the content repository
    try {
      if (!repository.exists(uri)) {
        logger.warn("Non existing file {} requested on {}", uri, request.getUrl());
        return SKIP_BODY;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to look up file {} from {}", fileId, repository);
      return SKIP_BODY;
    }

    FileResource file = null;
    FileContent fileContent = null;

    // Determine the languages
    Language language = request.getLanguage();

    // Store the result in the jsp page context
    try {
      file = (FileResource) repository.get(uri);
      file.switchTo(language);

      Language contentLanguage = null;
      contentLanguage = LanguageUtils.getPreferredContentLanguage(file, request, site);
      if (contentLanguage == null) {
        logger.warn("File {} does not have suitable content", file);
        return SKIP_BODY;
      }

      fileContent = file.getContent(contentLanguage);
    } catch (ContentRepositoryException e) {
      logger.warn("Error trying to load file " + uri + ": " + e.getMessage(), e);
      return SKIP_BODY;
    }

    // TODO: Check the permissions

    // Store the file and the file content in the request
    stashAndSetAttribute(FileResourceTagExtraInfo.FILE, file);
    stashAndSetAttribute(FileResourceTagExtraInfo.FILE_CONTENT, fileContent);

    // Add cache tags to response
    response.addTag(CacheTag.Resource, file.getURI().getIdentifier());
    response.addTag(CacheTag.Url, file.getURI().getPath());

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
    fileId = null;
    filePath = null;
  }

}