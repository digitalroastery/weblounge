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

package ch.entwine.weblounge.contentrepository.impl.endpoint;

import ch.entwine.weblounge.common.content.MalformedResourceURIException;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.file.FileContent;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.SiteManager;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

/**
 * Utility implementation for content repository endpoints, providing easy
 * access to the content repository and its functionality.
 */
public class ContentRepositoryEndpoint {

  /** Logging facility */
  static final Logger logger = LoggerFactory.getLogger(ContentRepositoryEndpoint.class);

  /** Regular expression to match the resource type */
  protected static final Pattern resourceTypeRegex = Pattern.compile(".*<\\s*([\\w]*) .*");

  /** The sites that are online */
  protected transient SiteManager sites = null;

  /**
   * Loads the given resource content.
   * 
   * @param request
   *          the servlet request
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @return the resource content
   */
  protected Response getResourceContent(HttpServletRequest request,
      final Resource<?> resource, final Language language) {

    // Check the parameters
    if (resource == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.hasChanged(request, resource)) {
      return Response.notModified().build();
    }

    // Load the content
    ResourceContent resourceContent = resource.getContent(language);
    if (resourceContent == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    Site site = getSite(request);
    final ContentRepository contentRepository = getContentRepository(site, false);

    // Create the response
    final InputStream is;
    try {
      is = contentRepository.getContent(resource.getURI(), language);
    } catch (IOException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ContentRepositoryException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Is the content locally available
    if (is == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    // Write the file contents back
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException,
          WebApplicationException {
        try {
          IOUtils.copy(is, os);
        } catch (IOException e) {
          logger.warn("Error writing file contents to response", e);
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
    });

    // Set file-related response information
    if (resourceContent instanceof FileContent) {
      FileContent fileContent = (FileContent) resourceContent;
      if (fileContent.getMimetype() != null)
        response.type(fileContent.getMimetype());
      else
        response.type(MediaType.APPLICATION_OCTET_STREAM);
       if (fileContent.getSize() > 0)
       response.header("Content-Length", fileContent.getSize());
    }

    // Add an e-tag and send the response
    response.header("Content-Disposition", "inline; filename=" + resource.getContent(language).getFilename());
    response.tag(ResourceUtils.getETagValue(resource));
    response.lastModified(ResourceUtils.getModificationDate(resource));
    return response.build();
  }

  /**
   * Returns the resource identified by the given request and resource
   * identifier or <code>null</code> if either one of the site, the site's
   * content repository or the resource itself is not available.
   * <p>
   * If <code>resourceType</code> is not <code>null</code>, the loaded resource
   * is only returned if the resource type matches.
   * 
   * @param request
   *          the servlet request
   * @param resourceId
   *          the resource identifier
   * @param resourceType
   *          the resource type
   * @return the resource
   */
  protected Resource<?> loadResource(HttpServletRequest request,
      String resourceId, String resourceType) {
    return loadResource(request, resourceId, resourceType, Resource.LIVE);
  }
  
  
  /**
   * Returns the resource identified by the given request and resource
   * identifier or <code>null</code> if either one of the site, the site's
   * content repository or the resource itself is not available.
   * <p>
   * If <code>resourceType</code> is not <code>null</code>, the loaded resource
   * is only returned if the resource type matches.
   * 
   * @param request
   *          the servlet request
   * @param resourceId
   *          the resource identifier
   * @param resourceType
   *          the resource type
   * @param version
   *          the version
   * @return the resource
   */
  protected Resource<?> loadResource(HttpServletRequest request,
      String resourceId, String resourceType, long version) {
    if (sites == null) {
      logger.debug("Unable to load resource '{}': no sites registered", resourceId);
      return null;
    }
    
    // Extract the site
    Site site = getSite(request);
    
    // Look for the content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }
    
    // Load the resource and return it
    try {
      ResourceURI resourceURI = new ResourceURIImpl(resourceType, site, null, resourceId, version);
      Resource<?> resource = contentRepository.get(resourceURI);
      if (resource == null)
        return null;
      if (resourceType != null && !resourceType.equals(resource.getURI().getType())) {
        return null;
      }
      return resource;
    } catch (ContentRepositoryException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Returns the resource identified by the given request and resource path or
   * <code>null</code> if either one of the site, the site's content repository
   * or the resource itself is not available.
   * <p>
   * If <code>resourceType</code> is not <code>null</code>, the loaded resource
   * is only returned if the resource type matches.
   * 
   * @param request
   *          the servlet request
   * @param resourcePath
   *          the resource path
   * @param resourceType
   *          the resource type
   * @return the resource
   */
  protected Resource<?> loadResourceByPath(HttpServletRequest request,
      String resourcePath, String resourceType) {
    if (sites == null) {
      logger.debug("Unable to load resource '{}': no sites registered", resourcePath);
      return null;
    }

    // Extract the site
    Site site = getSite(request);

    // Look for the content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }

    // Load the resource and return it
    try {
      ResourceURI resourceURI = new ResourceURIImpl(resourceType, site, resourcePath);
      Resource<?> resource = contentRepository.get(resourceURI);
      if (resource == null)
        return null;
      if (resourceType != null && !resourceType.equals(resource.getURI().getType())) {
        return null;
      }
      return resource;
    } catch (MalformedResourceURIException e) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (ContentRepositoryException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Tries to locate the content repository for the given site. If
   * <code>writable</code> is <code>true</code>, the method tries to cast the
   * repository to a <code>WritableContentRepository</code>.
   * <p>
   * This method throws a corresponding <code>WebApplicationException</code> in
   * case of failure.
   * 
   * @param site
   *          the site
   * @param writable
   *          <code>true</code> to request a writable repository
   * @return the repository
   * @throws WebApplicationException
   *           if the repository can't be located or if it's not writable
   */
  protected ContentRepository getContentRepository(Site site, boolean writable) {
    try {
      ContentRepository contentRepository = site.getContentRepository();
      if (contentRepository == null) {
        logger.warn("No content repository found for site '{}'", site);
        throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
      }
      if (writable) {
        WritableContentRepository wcr = (WritableContentRepository) contentRepository;
        return wcr;
      } else {
        return contentRepository;
      }
    } catch (ClassCastException e) {
      logger.warn("Content repository '{}' is not writable", site);
      throw new WebApplicationException(Status.NOT_ACCEPTABLE);
    }
  }

  /**
   * Extracts the site from the request and returns it. If the site is not found
   * or it's not running, a corresponding <code>WebApplicationException</code>
   * is thrown.
   * 
   * @param request
   *          the http request
   * @return the site
   * @throws WebApplicationException
   *           if the site is not found or is not running
   */
  protected Site getSite(HttpServletRequest request)
      throws WebApplicationException {
    URL url = UrlUtils.toURL(request, false, false);
    Site site = sites.findSiteByURL(url);
    if (site == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    } else if (!site.isOnline()) {
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }
    return site;
  }

  /**
   * Returns the resource type or <code>null</code> if the type could not be
   * extracted from the specified document.
   * 
   * @param input
   *          the input text
   * @return the resource type
   */
  protected String getResourceType(String input) {
    Matcher m = resourceTypeRegex.matcher(input);
    if (m.matches()) {
      return m.group(1);
    }
    return null;
  }

  /**
   * Callback for OSGi to set the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void setSiteManager(SiteManager siteManager) {
    this.sites = siteManager;
  }

  /**
   * Callback for OSGi to remove the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void removeSiteManager(SiteManager siteManager) {
    this.sites = null;
  }

}
