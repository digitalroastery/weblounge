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
import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils;
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
 * This tag loads an image that is defined by an identifier or a path from the
 * content repository.
 * <p>
 * If it is found, the image is defined in the jsp context variable
 * <code>image</code>, otherwise, the tag body is skipped altogether.
 */
public class ImageResourceTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = 2047795554694030193L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ImageResourceTag.class.getName());

  /** The image identifier */
  private String imageId = null;

  /** The image path */
  private String imagePath = null;

  /** The image style */
  private String imageStyle = null;

  /**
   * Sets the image identifier.
   * 
   * @param id
   *          image identifier
   */
  public void setUuid(String id) {
    imageId = id;
  }

  /**
   * Sets the image path. If both path and uuid have been defined, the uuid
   * takes precedence.
   * 
   * @param path
   *          image path
   */
  public void setPath(String path) {
    imagePath = path;
  }

  /**
   * Sets the image style.
   * 
   * @param style
   *          image style
   */
  public void setImagestyle(String style) {
    imageStyle = style;
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

    // Create the image uri, either from the id or the path. If none is
    // specified, and we are not in jsp compilation mode, issue a warning
    ResourceURI uri = null;    
    if (StringUtils.isNotBlank(imageId)) {
      uri = new ImageResourceURIImpl(site, null, imageId);
    } else if (StringUtils.isNotBlank(imagePath)) {
      uri = new ImageResourceURIImpl(site, imagePath, null);
    } else if (!request.getRequestURI().endsWith(".jsp")) {
      logger.warn("Neither uuid nor path were specified for image");
      return SKIP_BODY;
    }
      
    // Try to load the image from the content repository
    try {
      if (!repository.exists(uri)) {
        logger.warn("Non existing image {} requested on {}", uri, request.getUrl());
        return SKIP_BODY;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to look up image {} from {}", imageId, repository);
      return SKIP_BODY;
    }

    ImageResource image = null;
    ImageContent imageContent = null;
    ImageStyle style = null;

    // Load the content
    try {
      image = (ImageResource) repository.get(uri);
      language = LanguageUtils.getPreferredLanguage(image, request, site);
      image.switchTo(language);
      imageContent = image.getContent(language);
    } catch (ContentRepositoryException e) {
      logger.warn("Error trying to load image " + uri + ": " + e.getMessage(), e);
      return SKIP_BODY;
    }
    
    // Find the image style
    if (StringUtils.isNotBlank(imageStyle)) {
      style = ImageStyleUtils.findStyle(imageStyle, site);
      pageContext.setAttribute(ImageResourceTagExtraInfo.STYLE, style);
    }

    // TODO: Check the permissions

    // Store the image and the image content in the request
    pageContext.setAttribute(ImageResourceTagExtraInfo.IMAGE, image);
    pageContext.setAttribute(ImageResourceTagExtraInfo.IMAGE_CONTENT, imageContent);

    return EVAL_BODY_INCLUDE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(ImageResourceTagExtraInfo.IMAGE);
    pageContext.removeAttribute(ImageResourceTagExtraInfo.IMAGE_CONTENT);
    pageContext.removeAttribute(ImageResourceTagExtraInfo.STYLE);
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.taglib.WebloungeTag#reset()
   */
  protected void reset() {
    super.reset();
    imageId = null;
    imagePath = null;
    imageStyle = null;
  }

}