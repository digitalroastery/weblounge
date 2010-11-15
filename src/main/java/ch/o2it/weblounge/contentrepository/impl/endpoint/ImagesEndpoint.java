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
import ch.o2it.weblounge.common.content.file.FileContent;
import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.UnknownLanguageException;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.ScalingMode;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

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
    Site site = getSite(request);
    Set<Language> resourceLanguages = resource.languages();
    Language defaultLanguage = site.getDefaultLanguage();
    Language preferred = LanguageUtils.getPreferredLanguage(resourceLanguages, request, defaultLanguage);
    if (preferred == null) {
      preferred = resource.getOriginalContent().getLanguage();
    }

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

    // Determine the language
    Site site = getSite(request);
    Set<Language> resourceLanguages = resource.languages();
    Language defaultLanguage = site.getDefaultLanguage();
    Language preferred = LanguageUtils.getPreferredLanguage(resourceLanguages, request, defaultLanguage);
    if (preferred == null) {
      preferred = resource.getOriginalContent().getLanguage();
    }

    // Find the image style
    ImageStyle style = null;
    for (Module m : site.getModules()) {
      style = m.getImageStyle(styleId);
      if (style != null) {
        return getScaledImage(request, resource, preferred, style);
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

    // Find the image style
    Site site = getSite(request);
    ImageStyle style = null;
    for (Module m : site.getModules()) {
      style = m.getImageStyle(styleId);
      if (style != null) {
        return getScaledImage(request, resource, language, style);
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
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @param style
   *          the image style
   * @return the resource content
   */
  protected Response getScaledImage(HttpServletRequest request,
      final Resource<?> resource, final Language language,
      final ImageStyle style) {

    // Check the parameters
    if (resource == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.isModified(resource, request)) {
      return Response.notModified().build();
    }

    // Load the content
    ResourceContent resourceContent = resource.getContent(language);
    if (resourceContent == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    } else if (!(resourceContent instanceof ImageContent)) {
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    // Check the ETag
    String eTagValue = ResourceUtils.getETagValue(resource, language, style);
    if (!ResourceUtils.isMismatch(resource, language, request)) {
      return Response.notModified(new EntityTag(eTagValue)).build();
    }

    final String mimetype = ((ImageContent) resourceContent).getMimetype();
    final String format = mimetype.substring(mimetype.indexOf("/") + 1);

    Site site = getSite(request);
    final ContentRepository contentRepository = getContentRepository(site, false);
    final Language selectedLanguage = language;

    // When there is no scaling required, just return the original
    if (ScalingMode.None.equals(style.getScalingMode())) {
      return getResourceContent(request, resource, selectedLanguage);
    }

    // Create the response
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException,
          WebApplicationException {
        InputStream is = null;
        try {
          try {
            is = contentRepository.getContent(resource.getURI(), selectedLanguage);
            if (is == null)
              throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
            ImageStyleUtils.style(is, os, format, style);
          } catch (ContentRepositoryException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
          }
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
    }

    // Add an e-tag and send the response
    response.header("Content-Disposition", "inline; filename=" + resource.getContent(selectedLanguage).getFilename());
    response.tag(new EntityTag(eTagValue));
    response.lastModified(resource.getModificationDate());
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
      String endpointUrl = "/system/weblounge/images";
      // TODO: determine endpoint url
      docs = ImagesEndpointDocs.createDocumentation(endpointUrl);
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
