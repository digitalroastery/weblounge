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

package ch.entwine.weblounge.dispatcher.impl.handler;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.ImageScalingMode;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.impl.DispatchUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.core.MediaType;

/**
 * This request handler is used to handle requests to scaled images in the
 * repository.
 */
public final class ImageRequestHandlerImpl implements RequestHandler {

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-images/";

  /** Name of the image style parameter */
  protected static final String OPT_IMAGE_STYLE = "style";
  
  /** Length of a UUID */
  protected static final int UUID_LENGTH = 36;

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
    String fileName = null;

    // Check the request method. Only GET is supported right now.
    String requestMethod = request.getMethod();
    if (!"GET".equals(requestMethod)) {
      logger.debug("Image request handler does not support {} requests", requestMethod);
      return false;
    }

    // Get hold of the content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return false;
    }

    // Check if the request uri matches the special uri for images. If so, try
    // to extract the id from the last part of the path. If not, check if there
    // is an image with the current path.
    ResourceURI imageURI = null;
    ImageResource imageResource = null;
    try {
      String id = null;
      String imagePath = null;

      if (path.startsWith(URI_PREFIX)) {
        String uriSuffix = StringUtils.chomp(path.substring(URI_PREFIX.length()), "/");
        uriSuffix = URLDecoder.decode(uriSuffix, "utf-8");
  
        // Check whether we are looking at a uuid or a url path
        if (uriSuffix.length() == UUID_LENGTH) {
          id = uriSuffix;
        } else if (uriSuffix.length() >= UUID_LENGTH) {
          int lastSeparator = uriSuffix.indexOf('/');
          if (lastSeparator == UUID_LENGTH && uriSuffix.indexOf('/', lastSeparator + 1) < 0) {
            id = uriSuffix.substring(0, lastSeparator);
            fileName = uriSuffix.substring(lastSeparator + 1);
          } else {
            imagePath = uriSuffix;
            fileName = FilenameUtils.getName(imagePath);
          }
        } else {
          imagePath = "/" + uriSuffix; 
          fileName = FilenameUtils.getName(imagePath);
        }
      } else {
        imagePath = path;
        fileName = FilenameUtils.getName(imagePath);
      }

      // Try to load the resource
      imageURI = new ImageResourceURIImpl(site, imagePath, id);
      imageResource = (ImageResource) contentRepository.get(imageURI);
      if (imageResource == null) {
        logger.debug("No image found at {}", imageURI);
        return false;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error loading image from {}: {}", contentRepository, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (UnsupportedEncodingException e) {
      logger.error("Error decoding image url {} using utf-8: {}", path, e.getMessage());
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

    // Can the image be accessed by the current user?
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

    // Determine the response language
    Language language = null;
    if (StringUtils.isNotBlank(fileName)) {
      for (ImageContent c : imageResource.contents()) {
        if (c.getFilename().equalsIgnoreCase(fileName)) {
          if (language != null) {
            logger.debug("Unable to determine language from ambiguous filename");
            language = LanguageUtils.getPreferredLanguage(imageResource, request, site);
            break;
          }
          language = c.getLanguage();
        }
      }
    } else {
      language = LanguageUtils.getPreferredLanguage(imageResource, request, site);
    }
    
    if (language == null) {
      logger.warn("Image {} does not exist in any supported language", imageURI);
      DispatchUtils.sendNotFound(request, response);
      return true;
    }
    
    // Extract the image style and scale the image
    ImageStyle style = null;
    String styleId = StringUtils.trimToNull(request.getParameter(OPT_IMAGE_STYLE));
    if (styleId != null) {
      style = ImageStyleUtils.findStyle(styleId, site);
      if (style == null) {
        DispatchUtils.sendBadRequest("Image style '" + styleId + "' not found", request, response);
        return true;
      }
    }
    
    // Check the modified headers
    if (!ResourceUtils.hasChanged(request, imageResource, style)) {
      logger.debug("Image {} was not modified", imageURI);
      DispatchUtils.sendNotModified(request, response);
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
    response.setDateHeader("Last-Modified", ResourceUtils.getModificationDate(imageResource).getTime());
    
    // Set Expires header
    response.setDateHeader("Expires", System.currentTimeMillis() + Times.MS_PER_HOUR);

    // Add ETag header
    response.setHeader("ETag", ResourceUtils.getETagValue(imageResource, style));

    // Get the mime type
    final String mimetype = imageContents.getMimetype();
    final String format = mimetype.substring(mimetype.indexOf("/") + 1);

    // When there is no scaling required, just return the original
    if (style == null || ImageScalingMode.None.equals(style.getScalingMode())) {
      
      // Load the input stream from the repository
      try {
        imageInputStream = contentRepository.getContent(imageURI, language);
      } catch (Throwable t) {
        logger.error("Error loading {} image '{}' from {}: {}", new Object[] {
            language,
            imageResource,
            contentRepository,
            t.getMessage() });
        logger.error(t.getMessage(), t);
        IOUtils.closeQuietly(imageInputStream);
        return false;
      }

      // Write the image back to the client
      try {
        response.setHeader("Content-Length", Long.toString(imageContents.getSize()));
        response.setHeader("Content-Disposition", "inline; filename=" + imageContents.getFilename());
        IOUtils.copy(imageInputStream, response.getOutputStream());
        response.getOutputStream().flush();
      } catch (EOFException e) {
        logger.debug("Error writing image '{}' back to client: connection closed by client", imageResource);
        return true;
      } catch (IOException e) {
        logger.error("Error writing {} image '{}' back to client: {}", new Object[] {
            language,
            imageResource,
            e.getMessage() });
      } finally {
        IOUtils.closeQuietly(imageInputStream);
      }
      return true;
    }

    // Write the scaled file back to the response
    File scaledImageFile = null;
    try {

      // If the scaled version is not there yet, create it
      String filename = imageContents.getFilename();
      scaledImageFile = ImageStyleUtils.createScaledFile(imageURI, filename, language, style);
      long lastModified = imageResource.getModificationDate().getTime();
      if (!scaledImageFile.isFile() || scaledImageFile.lastModified() < lastModified) {
        InputStream is = contentRepository.getContent(imageURI, language);
        FileOutputStream fos = new FileOutputStream(scaledImageFile);
        logger.debug("Creating scaled image '{}' at {}", imageResource, scaledImageFile);
        ImageStyleUtils.style(is, fos, format, style);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(fos);
        scaledImageFile.setLastModified(lastModified);
      }

      response.setHeader("Content-Disposition", "inline; filename=" + scaledImageFile.getName());
      response.setHeader("Content-Length", Long.toString(scaledImageFile.length()));
      imageInputStream = new FileInputStream(scaledImageFile);
      IOUtils.copy(imageInputStream, response.getOutputStream());
      response.getOutputStream().flush();
      return true;
    } catch (ContentRepositoryException e) {
      logger.error("Unable to load image {}: {}", new Object[] {
          imageURI,
          e.getMessage(),
          e });
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (EOFException e) {
      logger.debug("Error writing image '{}' back to client: connection closed by client", imageResource);
      return true;
    } catch (IOException e) {
      logger.error("Error sending image '{}' to the client: {}", imageURI, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (Throwable t) {
      FileUtils.deleteQuietly(scaledImageFile);
      logger.error("Error creating scaled image '{}': {}", imageURI, t.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } finally {
      IOUtils.closeQuietly(imageInputStream);
    }
  }

  /**
   * @see ch.entwine.weblounge.dispatcher.api.request.RequestHandler#getName()
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