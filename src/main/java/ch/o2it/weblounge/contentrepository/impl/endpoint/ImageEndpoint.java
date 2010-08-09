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

package ch.o2it.weblounge.contentrepository.impl.endpoint;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.image.ImageResourceImpl;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.contentrepository.WritableContentRepository;
import ch.o2it.weblounge.kernel.SiteManager;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

/**
 * This class implements the <code>REST</code> endpoint for images.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class ImageEndpoint {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ImageEndpoint.class);

  /** The sites that are online */
  private transient SiteManager sites = null;

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Returns the image with the given identifier or a <code>404</code> if the
   * image could not be found.
   * 
   * @param request
   *          the request
   * @param resourceId
   *          the image identifier
   * @return the image
   */
  @GET
  @Path("/{resourceid}")
  public Response getImage(@Context HttpServletRequest request,
      @PathParam("resourceid") String resourceId) {

    // Check the parameters
    if (resourceId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Get the image resource
    final ImageResource resource = loadImage(request, resourceId);
    if (resource == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!isModidifed(resource, request)) {
      return Response.notModified().build();
    }

    // Create the response
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException, WebApplicationException {
        InputStream is = null;
        try {
          is = resource.openStream();
          IOUtils.copy(is, os);
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
    });
    if (resource.getMimeType() != null)
      response.type(resource.getMimeType());
    else
      response.type(MediaType.APPLICATION_OCTET_STREAM);
    // TODO: Add content length
    response.tag(new EntityTag(Long.toString(resource.getModificationDate().getTime())));
    response.lastModified(resource.getModificationDate());
    return response.build();
  }

  /**
   * Returns the scaled image with the given identifier or a <code>404</code> if
   * either the image or the image format could not be found.
   * 
   * @param request
   *          the request
   * @param resourceId
   *          the image identifier
   * @param formatId
   *          the format identifier
   * @return the scaled image
   */
  @GET
  @Path("/{resourceid}/formats/{formatid}")
  @Produces("image/*")
  public Response scaleImage(@Context HttpServletRequest request,
      @PathParam("resourceid") String resourceId,
      @PathParam("formatid") String formatId) {

    // Check the parameters
    if (resourceId == null)
      return Response.status(Status.BAD_REQUEST).build();
    if (formatId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Get the image resource
    ImageResource image = loadImage(request, resourceId);
    if (image == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!isModidifed(image, request)) {
      return Response.notModified().build();
    }

    // Load the image format
    ImageStyle format = null;
    Site site = getSite(request);
    for (Module m : site.getModules()) {
      format = m.getImageStyle(formatId);
      if (format != null)
        break;
    }
    if (format == null) {
      logger.warn("Image format '{}' does not exist in site '{}'");
      // TODO: Throw exception
      // return Response.status(Status.BAD_REQUEST).build();
    }

    // TODO: Scale
    final ImageResource scaledImage = image;

    // Create the response
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException,
          WebApplicationException {
        IOUtils.copy(scaledImage.openStream(), os);
      }
    });
    response.tag(new EntityTag(Long.toString(image.getModificationDate().getTime())));
    response.lastModified(image.getModificationDate());
    return response.build();
  }

  /**
   * Returns the endpoint documentation.
   * 
   * @return the endpoint documentation
   */
  @GET
  @Path("/docs")
  @Produces(MediaType.TEXT_HTML)
  public String getDocumentation() {
    if (docs == null) {
      String endpointUrl = "/system/images";
      // TODO: determine endpoint url
      docs = ImageEndpointDocs.createDocumentation(endpointUrl);
    }
    return docs;
  }

  /**
   * Returns <code>true</code> if the resource either is more recent than the
   * cached version on the client side or the request does not contain caching
   * information.
   * 
   * @param resource
   *          the resource
   * @param request
   *          the client request
   * @return <code>true</code> if the page is more recent than the version that
   *         is cached at the client.
   * @throws WebApplicationException
   *           if the <code>If-Modified-Since</code> cannot be converted to a
   *           date.
   */
  protected boolean isModidifed(Resource resource, HttpServletRequest request) {
    try {
      long cachedModificationDate = request.getDateHeader("If-Modified-Since");
      Date pageModificationDate = resource.getModificationDate();
      return cachedModificationDate < pageModificationDate.getTime();
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(Status.BAD_REQUEST);
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
    Site site = sites.findSiteByName(request.getServerName());
    if (site == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    } else if (!site.isRunning()) {
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }
    return site;
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
    ContentRepository contentRepository = null;
    try {
      contentRepository = ContentRepositoryFactory.getRepository(site);
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
   * Returns the image identified by the given request and resource identifier
   * or <code>null</code> if either one of the site, the site's content
   * repository or the image itself is not available.
   * 
   * @param request
   *          the servlet request
   * @param resourceId
   *          the image identifier
   * @return the image
   */
  protected ImageResource loadImage(HttpServletRequest request, String resourceId) {
    if (sites == null) {
      logger.debug("Unable to load page '{}': no sites registered", resourceId);
      return null;
    }

    // Extract the site
    Site site = getSite(request);

    // Look for the content repository
    ContentRepository contentRepository = ContentRepositoryFactory.getRepository(site);
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }

    // Load the image and return it
    // try {
    ResourceURI resourceURI = new FileResourceURIImpl(site, null, resourceId);
    // Resource resource = contentRepository.getPage(resourceURI);
    URL imageUrl = getClass().getResource("/image/placeholder.jpg");
    ImageResource resource = new ImageResourceImpl(resourceURI, imageUrl);
    if (!(resource instanceof ImageResource))
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    return (ImageResource) resource;
    // } catch (ContentRepositoryException e) {
    // throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    // }
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

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "images rest endpoint";
  }

}
