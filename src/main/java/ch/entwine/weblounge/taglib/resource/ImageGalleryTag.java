/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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
package ch.entwine.weblounge.taglib.resource;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;

/**
 * This tag prints out <code>HTML</code> code tailored to the image gallery
 * found at TODO: which one?
 * 
 * TODO: This tag implementation may not be used anymore and needs cleanpu
 */
public class ImageGalleryTag extends WebloungeTag {

  /** serial version id */
  private static final long serialVersionUID = -3661095423286843878L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ImageGalleryTag.class.getName());

  /** The subjects (tags) to use for image selection */
  private List<String> imageSubjects = null;

  /** The regular image style */
  private String styleNormal = null;

  /** The image style used for the large image version */
  private String styleBig = null;

  /** Image style used for thumbnail previews */
  private String styleThumb = null;

  /**
   * Sets the subjects (tags) that will be used to select the images.
   * 
   * @param subjects
   *          the subjects
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
   * Sets the image style used to display images at their regular size.
   * 
   * @param style
   *          the style
   */
  public void setNormalstyle(String style) {
    this.styleNormal = style;
  }

  /**
   * Sets the image style used to display enlarged versions of the images.
   * 
   * @param style
   *          the image style
   */
  public void setBigstyle(String style) {
    this.styleBig = style;
  }

  /**
   * Sets the image style used to display thumbnail previews of the images.
   * 
   * @param style
   *          the image style
   */
  public void setThumbstyle(String style) {
    this.styleThumb = style;
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

    SearchQuery query = new SearchQueryImpl(site);
    query.withVersion(Resource.LIVE);
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

    ResourceURI uri = null;
    ImageResource image = null;
    ImageContent imageContent = null;
    String linkToImage = null;
    PrintWriter writer = null;

    try {
      writer = response.getWriter();

      for (int i = 0; i < result.getItems().length; i++) {
        uri = new ImageResourceURIImpl(site, null, result.getItems()[i].getId());
        if (repository.exists(uri)) {
          image = (ImageResource) repository.get(uri);
          language = LanguageUtils.getPreferredLanguage(image, request, site);
          image.switchTo(language);
          imageContent = image.getContent(language);

          linkToImage = UrlUtils.concat("/weblounge-images", image.getIdentifier(), imageContent.getFilename());

          // Find the image style

          writer.write("<a href=\"");
          writer.write(linkToImage + "?style=" + this.styleNormal); // normal
                                                                    // size
          writer.write("\" rel=\"");
          writer.write(linkToImage + "?style=" + this.styleBig); // big size
          writer.write("\"><img src=\"");
          writer.write(linkToImage + "?style=" + this.styleThumb); // thumb size
          writer.write("\"></a>");
          writer.flush();

        }
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error loading image for gallery: " + e.getMessage());
      throw new JspException(e);
    } catch (IOException e) {
      logger.error("Error writing image gallery: " + e.getMessage());
      throw new JspException(e);
    }

    return SKIP_BODY;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    imageSubjects = null;
    styleNormal = null;
    styleBig = null;
    styleThumb = null;
  }

}
