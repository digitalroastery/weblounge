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
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.url.UrlUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

  /** The image subjects */
  private List<String> imageSubjects = null;

  /** The alternative image identifier */
  private String altImageId = null;

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
   * Sets the image subjects. If neither path nor uuid have been defined, a
   * repository search with the given subjects is done.
   * 
   * @param subjects
   *          image subjects
   */
  public void setSubjects(String subjects) {
    if (imageSubjects == null)
      imageSubjects = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(subjects, ",;");
    while (st.hasMoreTokens()) {
      imageSubjects.add(st.nextToken());
    }
  }

  /**
   * Sets the image identifier of the alternative image.
   * 
   * @param altImage
   *          image identifier
   */
  public void setAltimage(String altImage) {
    altImageId = altImage;
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

    ContentRepository repository = site.getContentRepository();
    if (repository == null) {
      logger.debug("Unable to load content repository for site '{}'", site);
      response.invalidate();
      return SKIP_BODY;
    }

    // Create the image uri, either from the id or the path. If none is
    // specified, and we are not in jsp compilation mode, issue a warning
    ResourceURI uri = null;
    if (StringUtils.isNotBlank(imageId)) {
      uri = new ImageResourceURIImpl(site, null, imageId);
    } else if (StringUtils.isNotBlank(imagePath)) {
      uri = new ImageResourceURIImpl(site, imagePath, null);
    } else if (imageSubjects.size() > 0) {
      SearchQuery query = new SearchQueryImpl(site);
      query.withType(ImageResource.TYPE);
      for (int i = 0; i < imageSubjects.size(); i++)
        query.withSubject(imageSubjects.get(i));
      SearchResult result;
      try {
        result = repository.find(query);
      } catch (ContentRepositoryException e) {
        logger.warn("Error searching for image with given subjects.");
        return SKIP_BODY;
      }
      if (result.getHitCount() > 1)
        logger.info("Search returned {} images. Will take no. 1 for further processing.", result.getHitCount());
      if (result.getHitCount() > 0)
        uri = new ImageResourceURIImpl(site, null, result.getItems()[0].getId());
    }
    if (uri == null) {
      // no image found, try to load alternative image
      uri = new ImageResourceURIImpl(site, null, altImageId);
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
    String linkToImage = null;
    int imageWidth = 0;
    int imageHeight = 0;

    // Load the content
    try {
      image = (ImageResource) repository.get(uri);
      language = LanguageUtils.getPreferredLanguage(image, request, site);
      image.switchTo(language);
      imageContent = image.getContent(language);
      imageWidth = imageContent.getWidth();
      imageHeight = imageContent.getHeight();
      // TODO: Make this a reference rather than a hard coded string
      linkToImage = UrlUtils.concat("/weblounge-images", image.getIdentifier(), imageContent.getFilename());
    } catch (ContentRepositoryException e) {
      logger.warn("Error trying to load image " + uri + ": " + e.getMessage(), e);
      return SKIP_BODY;
    }

    // Find the image style
    if (StringUtils.isNotBlank(imageStyle)) {
      style = ImageStyleUtils.findStyle(imageStyle, site);
      if (style != null) {
        linkToImage += "?style=" + style.getIdentifier();
        imageWidth = ImageStyleUtils.getWidth(imageContent, style);
        imageHeight = ImageStyleUtils.getHeight(imageContent, style);
        pageContext.setAttribute(ImageResourceTagExtraInfo.STYLE, style);
      } else {
        logger.warn("Image style '{}' not found", imageStyle);
      }
    }

    // TODO: Check the permissions

    // Store the image and the image content in the request
    pageContext.setAttribute(ImageResourceTagExtraInfo.IMAGE, image);
    pageContext.setAttribute(ImageResourceTagExtraInfo.IMAGE_CONTENT, imageContent);
    pageContext.setAttribute(ImageResourceTagExtraInfo.IMAGE_WIDTH, imageWidth);
    pageContext.setAttribute(ImageResourceTagExtraInfo.IMAGE_HEIGHT, imageHeight);
    pageContext.setAttribute(ImageResourceTagExtraInfo.IMAGE_SRC, linkToImage);

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
    pageContext.removeAttribute(ImageResourceTagExtraInfo.IMAGE_WIDTH);
    pageContext.removeAttribute(ImageResourceTagExtraInfo.IMAGE_HEIGHT);
    pageContext.removeAttribute(ImageResourceTagExtraInfo.IMAGE_SRC);
    pageContext.removeAttribute(ImageResourceTagExtraInfo.STYLE);
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
    imageId = null;
    imagePath = null;
    imageStyle = null;
  }

}