/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher.impl.handler;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.ScalingMode;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.dispatcher.impl.DispatchUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

/**
 * This request handler is used to handle requests to scaled images in the
 * repository.
 */
public final class ImageRequestHandlerImpl implements RequestHandler {

  /** Alternate uri prefix */
  protected static final String ALT_URI_PREFIX = "/images";

  /** Name of the image style parameter */
  protected static final String OPT_IMAGE_STYLE = "style";
  
  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(ImageRequestHandlerImpl.class);

  /**
   * Handles the request for an image resource that is believed to be in the
   * content repository. The handler scales the image as requested, sets the
   * response headers and the writes the image contents to the response.
   * <p>
   * This method returns <code>true</code> if the handler is decided to handle
   * the request, <code>false</code> otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {

    WebUrl url = request.getUrl();
    Site site = request.getSite();
    String path = url.getPath();

    // Check the request method. Only GET is supported right now.
    String requestMethod = request.getMethod();
    if (!"GET".equals(requestMethod)) {
      logger.debug("Image request handler does not support {} requests", requestMethod);
      return false;
    }

    // Get hold of the content repository
    ContentRepository contentRepository = ContentRepositoryFactory.getRepository(site);
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return false;
    }

    ResourceURI imageURI = null;
    ImageResource imageResource = null;
    try {
      if (path.startsWith(ALT_URI_PREFIX)) {
        String id = FilenameUtils.getBaseName(StringUtils.chomp(path, "/"));
        imageURI = new FileResourceURIImpl(site, null, id);
      } else {
        imageURI = new FileResourceURIImpl(site, path);
      }
      imageResource = (ImageResource)contentRepository.get(imageURI);
      if (imageResource == null) {
        logger.debug("No image found at {}", imageURI);
        return false;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error loading image from {}: {}", contentRepository, e);
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Agree to serve the image
    logger.debug("Image handler agrees to handle {}", path);

    // Is it published?
    if (!imageResource.isPublished()) {
      logger.debug("Access to unpublished image {}", imageURI);
      DispatchUtils.sendNotFound(request, response);
      return true;
    }

    // Can the page be accessed by the current user?
    User user = request.getUser();
    try {
      // TODO: Check permission
      // PagePermission p = new PagePermission(page, user);
      // AccessController.checkPermission(p);
    } catch (SecurityException e) {
      logger.warn("Access to image {} denied for user {}", imageURI, user);
      DispatchUtils.sendAccessDenied(request, response);
      return true;
    }

    // Check the modified headers
    if (!ResourceUtils.isModified(imageResource, request)) {
      logger.debug("Image {} was not modified", imageURI);
      return true;
    }

    // Determine the response language
    Language language = LanguageUtils.getPreferredLanguage(imageResource, request, site);
    if (language == null) {
      logger.warn("Image {} does not exist in any supported language", imageURI);
      DispatchUtils.sendNotFound(request, response);
      return true;
    } else {
      imageResource.switchTo(request.getLanguage());
    }
    
    // Extract the image style and scale the image
    ImageStyle style = null;
    String styleId = StringUtils.trimToNull(request.getParameter(OPT_IMAGE_STYLE));
    if (styleId != null) {
      style = ImageStyleUtils.findStyle(styleId, site);
    }

    // Check the ETag value
    String eTagValue = ResourceUtils.getETagValue(imageResource, language, style);
    if (!ResourceUtils.isMismatch(eTagValue, request)) {
      logger.debug("Image {} was not modified", imageURI);
      return true;
    }

    // Load the image contents from the repository
    ImageContent imageContents = imageResource.getContent(language);
    InputStream imageInputStream = null;
    
    // Add mime type header
    String contentType = imageContents.getMimetype();
    if (contentType == null)
      contentType = MediaType.APPLICATION_OCTET_STREAM;
    response.setContentType(contentType);

    // Add last modified header
    response.setDateHeader("Last-Modified", imageResource.getModificationDate().getTime());
    
    // Add ETag header
    String eTag = ResourceUtils.getETagValue(imageResource, language);
    response.setHeader("ETag", "\"" + eTag + "\"");
    
    // Add content disposition header
    StringBuffer filename = new StringBuffer(FilenameUtils.getBaseName(imageContents.getFilename()));
    filename.append("_").append(imageContents.getWidth()).append("x").append(imageContents.getHeight()).append(".").append(FilenameUtils.getExtension(imageContents.getFilename()));
    response.setHeader("Content-Disposition", "inline; filename=" + filename.toString());

    try {
      imageInputStream = contentRepository.getContent(imageURI, language);
    } catch (Throwable t) {
      logger.error("Error loading {} image '{}' from {}: {}", new Object[] { language, imageResource, contentRepository, t.getMessage() });
      logger.error(t.getMessage(), t);
      return false;
    } finally {
      IOUtils.closeQuietly(imageInputStream);
    }
    
    // Get the mime type
    final String mimetype = imageContents.getMimetype();
    final String format = mimetype.substring(mimetype.indexOf("/") + 1);

    // When there is no scaling required, just return the original
    if (style == null || ScalingMode.None.equals(style.getScalingMode())) {
      try {
        response.setHeader("Content-Length", Long.toString(imageContents.getSize()));
        IOUtils.copyLarge(imageInputStream, response.getOutputStream());
        response.getOutputStream().flush();
      } catch (IOException e) {
        logger.error("Error writing {} image '{}' back to client: {}", new Object[] { language, imageResource });
      } finally {
        IOUtils.closeQuietly(imageInputStream);
      }
      return true;
    }

    // Write the file back to the response
    try {
      // TODO: What is the scaled file size?
      // response.setHeader("Content-Length", Long.toString(imageContents.getSize()));
      InputStream is = contentRepository.getContent(imageURI, language);
      ImageStyleUtils.style(is, response.getOutputStream(), format, style);
      response.getOutputStream().flush();
      return true;
    } catch (ContentRepositoryException e) {
      logger.error("Unable to load image {}: {}", new Object[] {
          imageURI,
          e.getMessage(),
          e });
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (IOException e) {
      logger.error("Error sending image {} to the client: {}", imageURI, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } finally {
      IOUtils.closeQuietly(imageInputStream);
    }
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "image request handler";
  }

  /**
   * Returns a string representation of this request handler.
   * 
   * @return the handler name
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

}