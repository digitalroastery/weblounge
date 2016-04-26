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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.file.FileContent;
import ch.entwine.weblounge.common.impl.content.GeneralResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.security.SecurablePermission;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.impl.DispatchUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * This request handler is used to handle requests to files in the repository.
 */
public final class FileRequestHandlerImpl implements RequestHandler {

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-files/";

  /** Length of a UUID */
  protected static final int UUID_LENGTH = 36;

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(FileRequestHandlerImpl.class);

  /**
   * Handles the request for a file resource that is believed to be in the
   * content repository. The handler sets the response headers and the writes
   * the file contents to the response.
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

    // Check if the request uri matches the special uri for files. If so, try
    // to extract the id from the last part of the path. If not, check if there
    // is a file with the current path.
    ResourceURI fileURI = null;
    Resource<? extends FileContent> fileResource = null;
    try {
      String id = null;
      String filePath = null;

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
            filePath = uriSuffix;
            fileName = FilenameUtils.getName(filePath);
          }
        } else {
          filePath = "/" + uriSuffix;
          fileName = FilenameUtils.getName(filePath);
        }
        fileURI = new GeneralResourceURIImpl(site, filePath, id);
      } else {
        filePath = path;
        fileName = FilenameUtils.getName(filePath);
        fileURI = new FileResourceURIImpl(site, filePath, null);
      }

      // Try to load the resource
      try {
        fileResource = (Resource<? extends FileContent>) contentRepository.get(fileURI);
        if (fileResource == null) {
          logger.debug("No file found at {}", fileURI);
          return false;
        }
      } catch (ClassCastException e) {
        logger.debug("Resource {} does not extend file resource {}", fileURI);
        return false;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error loading file from {}: {}", contentRepository, e);
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (UnsupportedEncodingException e) {
      logger.error("Error decoding file url {} using utf-8: {}", path, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Try to serve the file
    logger.debug("File handler agrees to handle {}", path);

    // Check the request method. Only GET is supported right now.
    String requestMethod = request.getMethod().toUpperCase();
    if ("OPTIONS".equals(requestMethod)) {
      String verbs = "OPTIONS,GET";
      logger.trace("Answering options request to {} with {}", url, verbs);
      response.setHeader("Allow", verbs);
      response.setContentLength(0);
      return true;
    } else if (!"GET".equals(requestMethod)) {
      logger.debug("File request handler does not support {} requests", requestMethod);
      DispatchUtils.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, request, response);
      return true;
    }

    // Is it published?
    // TODO: Fix this. fileResource.isPublished() currently returns false,
    // as both from and to dates are null (see PublishingCtx)
    // if (!fileResource.isPublished()) {
    // logger.debug("Access to unpublished file {}", fileURI);
    // DispatchUtils.sendNotFound(request, response);
    // return true;
    // }

    // Can the page be accessed by the current user?
    User user = request.getUser();
    try {
      SecurablePermission readPermission = new SecurablePermission(fileResource, READ);
      if (System.getSecurityManager() != null)
        System.getSecurityManager().checkPermission(readPermission);
    } catch (SecurityException e) {
      logger.warn("Access to file {} denied for user {}", fileURI, user);
      DispatchUtils.sendAccessDenied(request, response);
      return true;
    }

    // Determine the response language by filename
    Language language = null;
    if (StringUtils.isNotBlank(fileName)) {
      for (FileContent c : fileResource.contents()) {
        if (c.getFilename().equalsIgnoreCase(fileName)) {
          if (language != null) {
            logger.debug("Unable to determine language from ambiguous filename");
            language = LanguageUtils.getPreferredContentLanguage(fileResource, request, site);
            break;
          }
          language = c.getLanguage();
        }
      }
      if (language == null)
        language = LanguageUtils.getPreferredContentLanguage(fileResource, request, site);
    } else {
      language = LanguageUtils.getPreferredContentLanguage(fileResource, request, site);
    }

    // If the filename did not lead to a language, apply language resolution
    if (language == null) {
      logger.warn("File {} does not exist in any supported language", fileURI);
      DispatchUtils.sendNotFound(request, response);
      return true;
    }

    // Check the modified headers
    long revalidationTime = MS_PER_DAY;
    long expirationDate = System.currentTimeMillis() + revalidationTime;
    if (!ResourceUtils.hasChanged(request, fileResource, language)) {
      logger.debug("File {} was not modified", fileURI);
      response.setDateHeader("Expires", expirationDate);
      DispatchUtils.sendNotModified(request, response);
      return true;
    }

    // Add mime type header
    FileContent content = fileResource.getContent(language);

    // If the content is hosted externally, send a redirect and be done with it
    if (content.getExternalLocation() != null) {
      try {
        response.sendRedirect(content.getExternalLocation().toExternalForm());
      } catch (IOException e) {
        logger.debug("Client ignore redirect to {}", content.getExternalLocation());
      }
      return true;
    }

    String contentType = content.getMimetype();
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

    // Add last modified header
    response.setDateHeader("Last-Modified", ResourceUtils.getModificationDate(fileResource, language).getTime());

    // Add ETag header
    String eTag = ResourceUtils.getETagValue(fileResource);
    response.setHeader("ETag", eTag);

    // Set the Expires header
    response.setDateHeader("Expires", expirationDate);

    // Add content disposition header
    response.setHeader("Content-Disposition", "inline; filename=" + content.getFilename());

    // Add content size
    response.setHeader("Content-Length", Long.toString(content.getSize()));

    // Write the file back to the response
    InputStream fileContents = null;
    try {
      fileContents = contentRepository.getContent(fileURI, language);
      IOUtils.copy(fileContents, response.getOutputStream());
      response.getOutputStream().flush();
      return true;
    } catch (ContentRepositoryException e) {
      logger.error("Unable to load file {}: {}", new Object[] {
          fileURI,
          e.getMessage(),
          e });
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (EOFException e) {
      logger.debug("Error writing file '{}' back to client: connection closed by client", fileURI);
      return true;
    } catch (IOException e) {
      if (RequestUtils.isCausedByClient(e))
        return true;
      logger.error("Error sending file {} to the client: {}", fileURI, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } finally {
      IOUtils.closeQuietly(fileContents);
    }
  }

  /**
   * @see ch.entwine.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "file request handler";
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