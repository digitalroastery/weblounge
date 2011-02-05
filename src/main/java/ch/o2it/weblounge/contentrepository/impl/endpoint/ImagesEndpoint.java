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
import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.content.repository.ContentRepository;
import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.UnknownLanguageException;
import ch.o2it.weblounge.common.site.ImageScalingMode;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
public class ImagesEndpoint extends ContentRepositoryEndpoint {

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Returns the image with the given identifier or a <code>404</code> if the
   * image could not be found.
   * 
   * @param request
   *          the request
   * @param imageId
   *          the resource identifier
   * @return the image
   */
  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("/{image}/metadata")
  public Response getImageResource(@Context HttpServletRequest request,
      @PathParam("image") String imageId) {

    // Check the parameters
    if (imageId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Get the resource
    final ImageResource resource = (ImageResource) loadResource(request, imageId, ImageResource.TYPE);
    if (resource == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.isModified(resource, request)) {
      return Response.notModified().build();
    }

    // Check the ETag
    String eTagValue = ResourceUtils.getETagValue(resource, null);
    if (!ResourceUtils.isMismatch(resource, null, request)) {
      return Response.notModified(new EntityTag(eTagValue)).build();
    }

    // Create the response
    ResponseBuilder response = Response.ok(resource.toXml());
    response.tag(new EntityTag(eTagValue));
    response.lastModified(resource.getModificationDate());
    return response.build();
  }

  /**
   * Returns the original image with the given identifier or a <code>404</code>
   * if the image resource or the image could not be found.
   * 
   * @param request
   *          the request
   * @param imageId
   *          the resource identifier
   * @return the resource
   */
  @GET
  @Path("/{image}/original")
  public Response getOriginalImage(@Context HttpServletRequest request,
      @PathParam("image") String imageId) {

    // Check the parameters
    if (imageId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Get the resource
    final Resource<?> resource = loadResource(request, imageId, ImageResource.TYPE);
    if (resource == null || resource.contents().isEmpty()) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Determine the language
    Language preferred = resource.getOriginalContent().getLanguage();

    return getResourceContent(request, resource, preferred);
  }

  /**
   * Returns the original image with the given identifier or a <code>404</code>
   * if the image resource or the image could not be found in the given
   * language.
   * 
   * @param request
   *          the request
   * @param imageId
   *          the resource identifier
   * @param languageId
   *          the language identifier
   * @return the image
   */
  @GET
  @Path("/{image}/locales/{language}/original")
  public Response getOriginalImage(@Context HttpServletRequest request,
      @PathParam("image") String imageId,
      @PathParam("language") String languageId) {

    // Check the parameters
    if (imageId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Extract the language
    Language language = null;
    try {
      language = LanguageUtils.getLanguage(languageId);
    } catch (UnknownLanguageException e) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    // Get the image
    final Resource<?> resource = loadResource(request, imageId, ImageResource.TYPE);
    if (resource == null || resource.contents().isEmpty()) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    return getResourceContent(request, resource, language);
  }

  /**
   * Returns the original image with the given identifier or a <code>404</code>
   * if the image resource or the image could not be found.
   * 
   * @param request
   *          the request
   * @param imageId
   *          the resource identifier
   * @param styleId
   *          the image style identifier
   * @return the resource
   */
  @GET
  @Path("/{image}/styles/{style}")
  public Response getStyledImage(@Context HttpServletRequest request,
      @PathParam("image") String imageId, @PathParam("style") String styleId) {

    // Check the parameters
    if (imageId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Get the resource
    final Resource<?> resource = loadResource(request, imageId, ImageResource.TYPE);
    if (resource == null || resource.contents().isEmpty()) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    ImageResource imageResource = (ImageResource)resource;

    // Determine the language
    Site site = getSite(request);
    Language preferred = LanguageUtils.getPreferredLanguage(resource, request, site);
    if (preferred == null) {
      preferred = resource.getOriginalContent().getLanguage();
    }

    // Find the image style
    ImageStyle style = null;
    for (Module m : site.getModules()) {
      style = m.getImageStyle(styleId);
      if (style != null) {
        return getScaledImage(request, imageResource, preferred, style);
      }
    }

    // The image style was not found
    throw new WebApplicationException(Status.BAD_REQUEST);
  }

  /**
   * Returns the original image with the given identifier or a <code>404</code>
   * if the image resource or the image could not be found in the given
   * language.
   * 
   * @param request
   *          the request
   * @param imageId
   *          the resource identifier
   * @param languageId
   *          the language identifier
   * @param styleId
   *          the image style identifier
   * @return the image
   */
  @GET
  @Path("/{image}/locales/{language}/styles/{style}")
  public Response getStyledImageContent(@Context HttpServletRequest request,
      @PathParam("image") String imageId,
      @PathParam("language") String languageId,
      @PathParam("style") String styleId) {

    // Check the parameters
    if (imageId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Extract the language
    Language language = null;
    try {
      language = LanguageUtils.getLanguage(languageId);
    } catch (UnknownLanguageException e) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    // Get the image
    final Resource<?> resource = loadResource(request, imageId, ImageResource.TYPE);
    if (resource == null || resource.contents().isEmpty())
      throw new WebApplicationException(Status.NOT_FOUND);
    ImageResource imageResource = (ImageResource)resource;

    // Find the image style
    Site site = getSite(request);
    ImageStyle style = null;
    for (Module m : site.getModules()) {
      style = m.getImageStyle(styleId);
      if (style != null) {
        return getScaledImage(request, imageResource, language, style);
      }
    }

    // The image style was not found
    throw new WebApplicationException(Status.BAD_REQUEST);
  }

  /**
   * Returns the list of image styles that are registered for a site.
   * 
   * @param request
   *          the request
   * @return the list of image styles
   */
  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("/styles")
  public Response getImagestyles(@Context HttpServletRequest request) {
    Site site = getSite(request);
    if (site == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    StringBuffer buf = new StringBuffer("<styles>");
    for (Module m : site.getModules()) {
      ImageStyle[] styles = m.getImageStyles();
      for (ImageStyle style : styles) {
        buf.append(style.toXml());
      }
    }
    buf.append("</styles>");

    ResponseBuilder response = Response.ok(buf.toString());
    return response.build();
  }

  /**
   * Returns the image styles or a <code>404</code>.
   * 
   * @param request
   *          the request
   * @param styleId
   *          the image style identifier
   * @return the image
   */
  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("/styles/{style}")
  public Response getImagestyle(@Context HttpServletRequest request,
      @PathParam("style") String styleId) {

    Site site = getSite(request);
    ImageStyle style = null;
    for (Module m : site.getModules()) {
      style = m.getImageStyle(styleId);
      if (style != null) {
        ResponseBuilder response = Response.ok(style.toXml());
        return response.build();
      }
    }

    // The image style was not found
    throw new WebApplicationException(Status.NOT_FOUND);
  }

  /**
   * Loads the given resource content.
   * 
   * @param request
   *          the servlet request
   * @param imageResource
   *          the resource
   * @param language
   *          the language
   * @param style
   *          the image style
   * @return the resource content
   */
  protected Response getScaledImage(HttpServletRequest request,
      final ImageResource imageResource, final Language language,
      final ImageStyle style) {

    // Check the parameters
    if (imageResource == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.isModified(imageResource, request)) {
      return Response.notModified().build();
    }

    // Load the content
    ResourceContent resourceContent = imageResource.getContent(language);
    if (resourceContent == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    } else if (!(resourceContent instanceof ImageContent)) {
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    // Check the ETag
    String eTag = ResourceUtils.getETagValue(imageResource, language, style);
    if (!ResourceUtils.isMismatch(eTag, request)) {
      return Response.notModified(new EntityTag(eTag)).build();
    }

    final String mimetype = ((ImageContent) resourceContent).getMimetype();
    final String format = mimetype.substring(mimetype.indexOf("/") + 1);
    ResourceURI imageURI = imageResource.getURI();

    Site site = getSite(request);
    final ContentRepository contentRepository = getContentRepository(site, false);

    // When there is no scaling required, just return the original
    if (ImageScalingMode.None.equals(style.getScalingMode())) {
      return getResourceContent(request, imageResource, language);
    }

    // Load the image contents from the repository
    ImageContent imageContents = imageResource.getContent(language);
    InputStream imageInputStream = null;
    String filename = null;
    long contentLength = -1;

    // Load the input stream from the scaled image
    InputStream contentRepositoryIs = null;
    FileOutputStream fos = null;
    try {
      File scaledImageFile = ImageStyleUtils.getScaledImageFile(imageResource, imageContents, site, style);
      long lastModified = imageResource.getModificationDate().getTime();
      if (!scaledImageFile.isFile() || scaledImageFile.lastModified() < lastModified) {
        contentRepositoryIs = contentRepository.getContent(imageURI, language);
        fos = new FileOutputStream(scaledImageFile);
        logger.debug("Creating scaled image '{}' at {}", imageResource, scaledImageFile);
        ImageStyleUtils.style(contentRepositoryIs, fos, format, style);
        scaledImageFile.setLastModified(lastModified);
      }

      // The scaled image should now exist
      imageInputStream = new FileInputStream(scaledImageFile);
      filename = scaledImageFile.getName();
      contentLength = scaledImageFile.length();
    } catch (ContentRepositoryException e) {
      logger.error("Error loading {} image '{}' from {}: {}", new Object[] {
          language,
          imageResource,
          contentRepository,
          e.getMessage() });
      logger.error(e.getMessage(), e);
      IOUtils.closeQuietly(imageInputStream);
      throw new WebApplicationException();
    } catch (IOException e) {
      logger.error("Error scaling image '{}': {}", imageURI, e.getMessage());
      IOUtils.closeQuietly(imageInputStream);
      throw new WebApplicationException();
    } finally {
      IOUtils.closeQuietly(contentRepositoryIs);
      IOUtils.closeQuietly(fos);
    }

     // Create the response
    final InputStream is = imageInputStream;
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException,
          WebApplicationException {
        try {
          IOUtils.copy(is, os);
          os.flush();
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
    });

    // Add mime type header
    String contentType = imageContents.getMimetype();
    if (contentType == null)
      contentType = MediaType.APPLICATION_OCTET_STREAM;
    response.type(contentType);

    // Add last modified header
    response.lastModified(imageResource.getModificationDate());

    // Add ETag header
    response.tag(new EntityTag(eTag));

    // Add filename header
    response.header("Content-Disposition", "inline; filename=" + filename);

    // Content length
    response.header("Content-Length", Long.toString(contentLength));

    // Send the response
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
  public String getDocumentation(@Context HttpServletRequest request) {
    if (docs == null) {
      String docsPath = request.getRequestURI();
      String docsPathExtension = request.getPathInfo();
      String servicePath = request.getRequestURI().substring(0, docsPath.length() - docsPathExtension.length());
      docs = ImagesEndpointDocs.createDocumentation(servicePath);
    }
    return docs;
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
