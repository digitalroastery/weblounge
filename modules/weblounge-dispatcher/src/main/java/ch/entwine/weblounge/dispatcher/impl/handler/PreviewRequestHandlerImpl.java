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

import static ch.entwine.weblounge.common.Times.MS_PER_DAY;
import static ch.entwine.weblounge.common.security.SystemAction.READ;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.security.SecurablePermission;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * This request handler is used to handle requests to scaled images in the
 * repository.
 */
public final class PreviewRequestHandlerImpl implements RequestHandler {

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-previews/";

  /** Name of the image style parameter */
  protected static final String OPT_IMAGE_STYLE = "style";

  /** Length of a UUID */
  protected static final int UUID_LENGTH = 36;

  /** The server environment */
  protected Environment environment = Environment.Production;

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(PreviewRequestHandlerImpl.class);

  /** The preview generators */
  private final List<PreviewGenerator> previewGenerators = new ArrayList<PreviewGenerator>();
  
  /** Lock object for access to the 'previewGenerators' list */
  private final Object previewGeneratorsLock = new Object();

  /** The list of previews that are being created at the moment */
  private final List<String> previews = new ArrayList<String>();
  
  /** Lock object for access to the 'previews' list */
  private final Object previewsLock = new Object();

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

    // This request handler can only be used with the prefix
    if (!path.startsWith(URI_PREFIX))
      return false;

    // Get hold of the content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return false;
    } else if (contentRepository.isIndexing()) {
      logger.debug("Content repository of site '{}' is currently being indexed", site);
      DispatchUtils.sendServiceUnavailable(request, response);
      return true;
    }

    // Check if the request uri matches the special uri for previews. If so, try
    // to extract the id from the last part of the path. If not, check if there
    // is an image with the current path.
    ResourceURI resourceURI = null;
    Resource<?> resource = null;
    try {
      String id = null;
      String imagePath = null;

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

      // Try to load the resource
      resourceURI = new ResourceURIImpl(null, site, imagePath, id);
      resource = contentRepository.get(resourceURI);
      if (resource == null) {
        logger.debug("No resource found at {}", resourceURI);
        return false;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error loading resource from {}: {}", contentRepository, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (UnsupportedEncodingException e) {
      logger.error("Error decoding resource url {} using utf-8: {}", path, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Agree to serve the preview
    logger.debug("Preview handler agrees to handle {}", path);

    // Check the request method. Only GET is supported right now.
    String requestMethod = request.getMethod().toUpperCase();
    if ("OPTIONS".equals(requestMethod)) {
      String verbs = "OPTIONS,GET";
      logger.trace("Answering options request to {} with {}", url, verbs);
      response.setHeader("Allow", verbs);
      response.setContentLength(0);
      return true;
    } else if (!"GET".equals(requestMethod)) {
      logger.debug("Image request handler does not support {} requests", requestMethod);
      DispatchUtils.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, request, response);
      return true;
    }

    // Is it published?
    // TODO: Fix this. imageResource.isPublished() currently returns false,
    // as both from and to dates are null (see PublishingCtx)
    // if (!imageResource.isPublished()) {
    // logger.debug("Access to unpublished image {}", imageURI);
    // DispatchUtils.sendNotFound(request, response);
    // return true;
    // }

    // Can the resource be accessed by the current user?
    User user = request.getUser();
    try {
      SecurablePermission readPermission = new SecurablePermission(resource, READ);
      if (System.getSecurityManager() != null)
        System.getSecurityManager().checkPermission(readPermission);
    } catch (SecurityException e) {
      logger.warn("Access to resource {} denied for user {}", resourceURI, user);
      DispatchUtils.sendAccessDenied(request, response);
      return true;
    }

    // Determine the response language by filename
    Language language = null;
    if (StringUtils.isNotBlank(fileName)) {
      for (ResourceContent c : resource.contents()) {
        if (c.getFilename().equalsIgnoreCase(fileName)) {
          if (language != null) {
            logger.debug("Unable to determine language from ambiguous filename");
            language = LanguageUtils.getPreferredContentLanguage(resource, request, site);
            break;
          }
          language = c.getLanguage();
        }
      }
      if (language == null)
        language = LanguageUtils.getPreferredContentLanguage(resource, request, site);
    } else {
      language = LanguageUtils.getPreferredContentLanguage(resource, request, site);
    }

    // If the filename did not lead to a language, apply language resolution
    if (language == null) {
      logger.warn("Resource {} does not exist in any supported language", resourceURI);
      DispatchUtils.sendNotFound(request, response);
      return true;
    }

    // Find a resource preview generator
    PreviewGenerator previewGenerator = null;
    synchronized (previewGeneratorsLock) {
      for (PreviewGenerator generator : previewGenerators) {
        if (generator.supports(resource, language)) {
          previewGenerator = generator;
          break;
        }
      }
    }

    // If we did not find a preview generator, we need to let go
    if (previewGenerator == null) {
      logger.debug("Unable to generate preview for {} since no suitable preview generator is available", resource);
      DispatchUtils.sendNotFound(request, response);
      return true;
    }

    // Extract the image style
    ImageStyle style = null;
    String styleId = StringUtils.trimToNull(request.getParameter(OPT_IMAGE_STYLE));
    if (styleId != null) {
      style = ImageStyleUtils.findStyle(styleId, site);
      if (style == null) {
        DispatchUtils.sendBadRequest("Image style '" + styleId + "' not found", request, response);
        return true;
      }
    }

    // Get the path to the preview image
    File previewFile = ImageStyleUtils.getScaledFile(resource, language, style);

    // Check the modified headers
    long revalidationTime = MS_PER_DAY;
    long expirationDate = System.currentTimeMillis() + revalidationTime;
    if (!ResourceUtils.hasChanged(request, previewFile)) {
      logger.debug("Scaled preview {} was not modified", resourceURI);
      response.setDateHeader("Expires", expirationDate);
      DispatchUtils.sendNotModified(request, response);
      return true;
    }

    // Load the image contents from the repository
    ResourceContent resourceContents = resource.getContent(language);

    // Add mime type header
    String contentType = resourceContents.getMimetype();
    if (contentType == null)
      contentType = MediaType.APPLICATION_OCTET_STREAM;

    // Set the content type
    String characterEncoding = response.getCharacterEncoding();
    if (StringUtils.isNotBlank(characterEncoding))
      response.setContentType(contentType + "; charset=" + characterEncoding.toLowerCase());
    else
      response.setContentType(contentType);

    // Browser caches and proxies are allowed to keep a copy
    response.setHeader("Cache-Control", "public, max-age=" + revalidationTime);

    // Set Expires header
    response.setDateHeader("Expires", expirationDate);

    // Write the image back to the client
    InputStream previewInputStream = null;
    try {
      if (previewFile.isFile() && previewFile.lastModified() >= resourceContents.getCreationDate().getTime()) {
        previewInputStream = new FileInputStream(previewFile);
      } else {
        previewInputStream = createPreview(request, response, resource, language, style, previewGenerator, previewFile, contentRepository);
      }

      if (previewInputStream == null) {
        // Assuming that createPreview() is setting the response header in the
        // case of failure
        return true;
      }

      // Add last modified header
      response.setDateHeader("Last-Modified", previewFile.lastModified());
      response.setHeader("ETag", ResourceUtils.getETagValue(previewFile.lastModified()));
      response.setHeader("Content-Disposition", "inline; filename=" + previewFile.getName());
      response.setHeader("Content-Length", Long.toString(previewFile.length()));
      previewInputStream = new FileInputStream(previewFile);
      IOUtils.copy(previewInputStream, response.getOutputStream());
      response.getOutputStream().flush();
      return true;
    } catch (EOFException e) {
      logger.debug("Error writing image '{}' back to client: connection closed by client", resource);
      return true;
    } catch (IOException e) {
      DispatchUtils.sendInternalError(request, response);
      if (RequestUtils.isCausedByClient(e))
        return true;
      logger.error("Error sending image '{}' to the client: {}", resourceURI, e.getMessage());
      return true;
    } catch (Throwable t) {
      logger.error("Error creating scaled image '{}': {}", resourceURI, t.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } finally {
      IOUtils.closeQuietly(previewInputStream);
    }
  }

  /**
   * Creates the preview image for the given resource and returns an input
   * stream to the preview or <code>null</code> if the preview could not be
   * created.
   */
  private InputStream createPreview(WebloungeRequest request,
      WebloungeResponse response, Resource<?> resource, Language language,
      ImageStyle style, PreviewGenerator previewGenerator, File previewFile,
      ContentRepository contentRepository) {

    String pathToImageFile = previewFile.getAbsolutePath();
    boolean firstOne = true;

    // Make sure the preview is not already being generated by another thread
    synchronized (previewsLock) {
      while (previews.contains(pathToImageFile)) {
        logger.debug("Preview at {} is being created, waiting for it to be generated", pathToImageFile);
        firstOne = false;
        try {
          previewsLock.wait(500);
          if (previews.contains(pathToImageFile)) {
            logger.trace("After waiting 500ms, preview at {} is still being worked on", pathToImageFile);
            DispatchUtils.sendServiceUnavailable(request, response);
            return null;
          }
        } catch (InterruptedException e) {
          DispatchUtils.sendServiceUnavailable(request, response);
          return null;
        }
      }

      // Make sure others are waiting until we are done
      if (firstOne) {
        previews.add(pathToImageFile);
      }
    }

    // Determine the resource's modification date
    long resourceLastModified = ResourceUtils.getModificationDate(resource, language).getTime();

    // Create the preview if this is the first request
    if (firstOne) {

      ResourceURI resourceURI = resource.getURI();

      if (style != null)
        logger.info("Creating preview of {} with style '{}' at {}", new String[] {
            resource.getIdentifier(),
            style.getIdentifier(),
            pathToImageFile });
      else
        logger.info("Creating original preview of {} at {}", new String[] {
            resource.getIdentifier(),
            pathToImageFile });

      // Get hold of the content
      ResourceContent resourceContents = resource.getContent(language);

      // Get the mime type
      final String mimetype = resourceContents.getMimetype();
      final String format = mimetype.substring(mimetype.indexOf("/") + 1);

      boolean scalingFailed = false;

      InputStream is = null;
      FileOutputStream fos = null;
      try {
        is = contentRepository.getContent(resourceURI, language);

        // Remove the original image
        FileUtils.deleteQuietly(previewFile);

        // Create a work file
        File imageDirectory = previewFile.getParentFile();
        String workFileName = "." + UUID.randomUUID() + "-" + previewFile.getName();
        FileUtils.forceMkdir(imageDirectory);
        File workImageFile = new File(imageDirectory, workFileName);

        // Create the scaled image
        fos = new FileOutputStream(workImageFile);
        logger.debug("Creating scaled image '{}' at {}", resource, previewFile);
        previewGenerator.createPreview(resource, environment, language, style, format, is, fos);

        // Move the work image in place
        try {
          FileUtils.moveFile(workImageFile, previewFile);
        } catch (IOException e) {
          logger.warn("Concurrent creation of preview {} resolved by copy instead of rename", previewFile.getAbsolutePath());
          FileUtils.copyFile(workImageFile, previewFile);
          FileUtils.deleteQuietly(workImageFile);
        } finally {
          previewFile.setLastModified(Math.max(new Date().getTime(), resourceLastModified));
        }

        // Make sure preview generation was successful
        if (!previewFile.isFile()) {
          logger.warn("The file at {} is not a regular file", pathToImageFile);
          scalingFailed = true;
        } else if (previewFile.length() == 0) {
          logger.warn("The scaled file at {} has zero length", pathToImageFile);
          scalingFailed = true;
        }

      } catch (ContentRepositoryException e) {
        logger.error("Unable to load image {}: {}", new Object[] {
            resourceURI,
            e.getMessage(),
            e });
        scalingFailed = true;
        DispatchUtils.sendInternalError(request, response);
      } catch (IOException e) {
        logger.error("Error sending image '{}' to the client: {}", resourceURI, e.getMessage());
        scalingFailed = true;
        DispatchUtils.sendInternalError(request, response);
      } catch (Throwable t) {
        logger.error("Error creating scaled image '{}': {}", resourceURI, t.getMessage());
        scalingFailed = true;
        DispatchUtils.sendInternalError(request, response);
      } finally {
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(fos);

        try {
          if (scalingFailed && previewFile != null) {
            logger.info("Cleaning up after failed scaling of {}", pathToImageFile);
            File f = previewFile;
            FileUtils.deleteQuietly(previewFile);
            f = previewFile.getParentFile();
            while (f != null && f.isDirectory() && (f.listFiles() == null || f.listFiles().length == 0)) {
              FileUtils.deleteQuietly(f);
              f = f.getParentFile();
            }
          }
        } catch (Throwable t) {
          logger.warn("Error cleaning up after failed scaling of {}", pathToImageFile);
        }

        synchronized (previews) {
          previews.remove(pathToImageFile);
          previews.notifyAll();
        }

      }

    }

    // Make sure whoever was in charge of creating the preview, was
    // successful
    boolean scaledImageExists = previewFile.isFile();
    boolean scaledImageIsOutdated = previewFile.lastModified() < resourceLastModified;
    if (!scaledImageExists || scaledImageIsOutdated) {
      logger.debug("Apparently, preview rendering for {} failed", previewFile.getAbsolutePath());
      DispatchUtils.sendServiceUnavailable(request, response);
      return null;
    } else {
      try {
        return new FileInputStream(previewFile);
      } catch (Throwable t) {
        logger.error("Error reading content from preview at {}: {}", previewFile.getAbsolutePath(), t.getMessage());
        DispatchUtils.sendServiceUnavailable(request, response);
        return null;
      }
    }
  }

  /**
   * Sets the server environment.
   * 
   * @param environment
   *          the server environment
   */
  void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * Adds the preview generator to the list of registered preview generators.
   * 
   * @param generator
   *          the generator
   */
  void addPreviewGenerator(PreviewGenerator generator) {
    synchronized (previewGeneratorsLock) {
      previewGenerators.add(generator);
      Collections.sort(previewGenerators, new Comparator<PreviewGenerator>() {
        public int compare(PreviewGenerator a, PreviewGenerator b) {
          return Integer.valueOf(b.getPriority()).compareTo(a.getPriority());
        }
      });
    }
  }

  /**
   * Removes the preview generator from the list of registered preview
   * generators.
   * 
   * @param generator
   *          the generator
   */
  void removePreviewGenerator(PreviewGenerator generator) {
    synchronized (previewGeneratorsLock) {
      previewGenerators.remove(generator);
    }
  }

  /**
   * @see ch.entwine.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "preview request handler";
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

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getPriority()
   */
  public int getPriority() {
    return 0;
  }

}