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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.ResourceUtils;
import ch.entwine.weblounge.common.impl.content.file.FileResourceImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.url.UrlUtils;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.UnknownLanguageException;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class implements the <code>REST</code> endpoint for file data.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class FilesEndpoint extends ContentRepositoryEndpoint {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FilesEndpoint.class);

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Returns the resource with the given path or a <code>404</code> if the
   * resource could not be found.
   * 
   * @param request
   *          the request
   * @param path
   *          the resource path
   * @return the resource
   */
  @GET
  @Produces("text/xml")
  @Path("/")
  public Response getFileByPath(@Context HttpServletRequest request,
      @QueryParam("path") String path) {

    // Check the parameters
    if (path == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Get the resource
    Resource<?> resource = loadResourceByPath(request, path, null);
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
   * Returns the resource with the given identifier or a <code>404</code> if the
   * resource could not be found.
   * 
   * @param request
   *          the request
   * @param resourceId
   *          the resource identifier
   * @return the resource
   */
  @GET
  @Produces("text/xml")
  @Path("/{resource}")
  public Response getFileByURI(@Context HttpServletRequest request,
      @PathParam("resource") String resourceId) {

    // Check the parameters
    if (resourceId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Get the resource
    Resource<?> resource = loadResource(request, resourceId, null);
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
   * Returns the resource content with the given identifier or a
   * <code>404</code> if the resource or the resource content could not be
   * found.
   * 
   * @param request
   *          the request
   * @param resourceId
   *          the resource identifier
   * @return the resource
   */
  @GET
  @Path("/{resource}/content")
  public Response getFileContent(@Context HttpServletRequest request,
      @PathParam("resource") String resourceId) {

    // Check the parameters
    if (resourceId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Get the resource
    final Resource<?> resource = loadResource(request, resourceId, null);
    if (resource == null || resource.contents().isEmpty()) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Determine the language
    Site site = getSite(request);
    Language preferred = LanguageUtils.getPreferredLanguage(resource, request, site);
    if (preferred == null) {
      preferred = resource.getOriginalContent().getLanguage();
    }

    return getResourceContent(request, resource, preferred);
  }

  /**
   * Returns the resource content with the given identifier or a
   * <code>404</code> if the resource or the resource content could not be
   * found.
   * 
   * @param request
   *          the request
   * @param resourceId
   *          the resource identifier
   * @param languageId
   *          the language identifier
   * @return the resource
   */
  @GET
  @Path("/{resource}/content/{languageid}")
  public Response getFileContent(@Context HttpServletRequest request,
      @PathParam("resource") String resourceId,
      @PathParam("languageid") String languageId) {

    // Check the parameters
    if (resourceId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Extract the language
    Language language = LanguageUtils.getLanguage(languageId);
    if (language == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    // Get the resource
    Resource<?> resource = loadResource(request, resourceId, null);
    if (resource == null || resource.contents().isEmpty()) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    return getResourceContent(request, resource, language);
  }

  /**
   * Adds the resource content with language <code>language</code> to the
   * specified resource.
   * 
   * @param request
   *          the request
   * @param resourceId
   *          the resource identifier
   * @param languageId
   *          the language identifier
   * @param is
   *          the input stream
   * @return the resource
   */
  @PUT
  @Path("/{resource}/content/{languageid}")
  public Response addFileContent(@Context HttpServletRequest request,
      @PathParam("resource") String resourceId,
      @PathParam("languageid") String languageId, InputStream is) {

    // Check the parameters
    if (resourceId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);
    if (is == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Extract the language
    Language language = LanguageUtils.getLanguage(languageId);
    if (language == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Get the resource
    Resource<?> resource = loadResource(request, resourceId, null);
    if (resource == null || resource.contents().isEmpty()) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Check the ETag
    if (ResourceUtils.isMismatch(resource, language, request)) {
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    // Try to create the resource
    ResourceURI uri = resource.getURI();
    ResourceContent content = null;
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resource.getURI().getType());
    ResourceReader<?, ?> reader;
    try {
      reader = (ResourceReader<?, ?>) serializer.getContentReader();
      // TODO: Get input stream for resource
      content = (ResourceContent) reader.read(null, resource.getURI().getSite());
    } catch (IOException e) {
      logger.warn("Error reading resource content {} from request", uri);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ParserConfigurationException e) {
      logger.warn("Error configuring parser to read resource content {}: {}", uri, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SAXException e) {
      logger.warn("Error parsing udpated resource {}: {}", uri, e.getMessage());
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    // TODO: Replace with current user
    User admin = site.getAdministrator();
    User user = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
    content.setCreator(user);

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    try {
      resource = contentRepository.putContent(resource.getURI(), content, is);
    } catch (IOException e) {
      logger.warn("Error writing content to resource {}: {}", uri, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while adding content to resource {}: {}", uri, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error adding content to resource {}: {}", uri, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.ok();
    response.tag(new EntityTag(ResourceUtils.getETagValue(resource, language)));
    response.lastModified(resource.getModificationDate());
    return response.build();
  }

  /**
   * Returns the resource content with the given identifier or a
   * <code>404</code> if the resource or the resource content could not be
   * found.
   * 
   * @param request
   *          the request
   * @param resourceId
   *          the resource identifier
   * @param languageId
   *          the language identifier
   * @return the resource
   */
  @DELETE
  @Path("/{resource}/content/{languageid}")
  public Response deleteFileContent(@Context HttpServletRequest request,
      @PathParam("resource") String resourceId,
      @PathParam("languageid") String languageId) {

    // Check the parameters
    if (resourceId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Extract the language
    Language language = LanguageUtils.getLanguage(languageId);
    if (language == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Get the resource
    Resource<?> resource = loadResource(request, resourceId, null);
    if (resource == null || resource.contents().isEmpty()) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Get the resource content
    ResourceContent content = resource.getContent(language);
    if (content == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    ResourceURI uri = resource.getURI();
    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    // TODO: Replace with real user

    // Delete the resource
    try {
      resource = contentRepository.deleteContent(uri, content);
    } catch (IllegalStateException e) {
      logger.warn("Tried to remove content from missing resource " + uri);
      throw new WebApplicationException(Status.NOT_FOUND);
    } catch (ContentRepositoryException e) {
      logger.warn("Error while accessing resource " + uri);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IOException e) {
      logger.warn("Error while deleting content from resource " + uri);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.ok(resource.toXml());
    response.tag(new EntityTag(ResourceUtils.getETagValue(resource, null)));
    response.lastModified(resource.getModificationDate());
    return response.build();
  }

  /**
   * Updates the indicated resource.
   * 
   * @param request
   *          the http request
   * @param resourceId
   *          the resource identifier
   * @param ifMatchHeader
   *          the resource's <code>etag</code> value
   * @param resourceContent
   *          the resource content
   * @return response an empty response
   * @throws WebApplicationException
   *           if the update fails
   */
  @PUT
  @Path("/{resource}")
  public Response updateFile(@Context HttpServletRequest request,
      @PathParam("resource") String resourceId,
      @FormParam("content") String resourceXml) {

    // Check the parameters
    if (resourceId == null)
      return Response.status(Status.BAD_REQUEST).build();
    if (resourceXml == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Extract the site
    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    User user = null; // TODO: Extract user
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    ResourceURI resourceURI = new ResourceURIImpl(null, site, null, resourceId);

    // Does the resource exist?
    try {
      if (!contentRepository.exists(resourceURI)) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Error lookup up resource {} from repository: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Check the value of the If-Match header against the etag
    try {
      Resource<?> currentResource = contentRepository.get(resourceURI);
      if (ResourceUtils.isMismatch(currentResource, null, request)) {
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Error reading current resource {} from repository: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Parse the resource and update it in the repository
    Resource<?> resource = null;
    // TOOD: Extract resource type
    String resourceType = null;
    try {
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
      ResourceReader<?, ?> resourceReader = (ResourceReader<?, ?>) serializer.getContentReader();
      resource = resourceReader.read(IOUtils.toInputStream(resourceXml, "utf-8"), site);
      // TODO: Replace this with current user
      User admin = site.getAdministrator();
      user = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
      resource.setModified(user, new Date());
      contentRepository.put(resource);
    } catch (IOException e) {
      logger.warn("Error reading udpated resource {} from request", resourceURI);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ParserConfigurationException e) {
      logger.warn("Error configuring parser to read udpated resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SAXException e) {
      logger.warn("Error parsing udpated resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while udpating resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error udpating resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.ok();
    response.tag(new EntityTag(ResourceUtils.getETagValue(resource, null)));
    return response.build();
  }

  /**
   * Creates a file resource at the site's content repository and returns the
   * location to post updates to.
   * 
   * @param request
   *          the http request
   * @param resourceXml
   *          the new resource
   * @param path
   *          the path to store the resource at
   * @return response the resource location
   */
  @POST
  @Path("/")
  public Response createFile(@Context HttpServletRequest request,
      @FormParam("resource") String resourceXml, @FormParam("path") String path) {

    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);

    // Create the resource uri
    ResourceURIImpl resourceURI = null;
    String uuid = UUID.randomUUID().toString();
    if (!StringUtils.isBlank(path)) {
      try {
        if (!path.startsWith("/"))
          path = "/" + path;
        WebUrl url = new WebUrlImpl(site, path);
        resourceURI = new ResourceURIImpl(null, site, url.getPath(), uuid);

        // Make sure the resource doesn't exist
        if (contentRepository.exists(new ResourceURIImpl(null, site, url.getPath()))) {
          logger.warn("Tried to create already existing resource {} in site '{}'", resourceURI, site);
          throw new WebApplicationException(Status.CONFLICT);
        }
      } catch (IllegalArgumentException e) {
        logger.warn("Tried to create a resource with an invalid path '{}': {}", path, e.getMessage());
        throw new WebApplicationException(Status.BAD_REQUEST);
      } catch (ContentRepositoryException e) {
        logger.warn("Resource lookup {} failed for site '{}'", resourceURI, site);
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    } else {
      resourceURI = new ResourceURIImpl(null, site, "/" + uuid.replaceAll("-", ""), uuid);
    }

    // Parse the resource and store it
    Resource<?> resource = null;
    String resourceType = getResourceType(resourceXml);
    if (resourceType == null) {
      logger.warn("Tried to create a resource without a type");
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    URI uri = null;
    if (!StringUtils.isBlank(resourceXml)) {
      logger.debug("Adding resource to {}", resourceURI);
      try {
        ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
        ResourceReader<?, ?> resourceReader = serializer.getReader();
        resource = resourceReader.read(IOUtils.toInputStream(resourceXml, "utf-8"), site);
      } catch (IOException e) {
        logger.warn("Error reading resource {} from request", resourceURI);
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (ParserConfigurationException e) {
        logger.warn("Error configuring parser to read udpated resource {}: {}", resourceURI, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (SAXException e) {
        logger.warn("Error parsing udpated resource {}: {}", resourceURI, e.getMessage());
        throw new WebApplicationException(Status.BAD_REQUEST);
      }
    } else {
      logger.debug("Creating new resource at {}", resourceURI);
      resource = new FileResourceImpl(resourceURI);
      User admin = site.getAdministrator();
      User creator = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
      resource.setCreated(creator, new Date());
    }

    // Store the new resource
    try {
      contentRepository.put(resource);
      uri = new URI(UrlUtils.concat(request.getRequestURL().toString(), resourceURI.getIdentifier()));
    } catch (URISyntaxException e) {
      logger.warn("Error creating a uri for resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IOException e) {
      logger.warn("Error writing new resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Illegal state while adding new resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error adding new resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.created(uri);
    response.tag(new EntityTag(ResourceUtils.getETagValue(resource, null)));
    return response.build();
  }

  /**
   * Removes the indicated resource from the site.
   * 
   * @param request
   *          the http request
   * @param resourceId
   *          the resource identifier
   * @return response an empty response
   */
  @DELETE
  @Path("/{resource}")
  public Response deleteFile(@Context HttpServletRequest request,
      @PathParam("resource") String resourceId) {

    // Check the parameters
    if (resourceId == null)
      return Response.status(Status.BAD_REQUEST).build();

    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    ResourceURI resourceURI = new ResourceURIImpl(null, site, null, resourceId);
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);

    // Make sure the resource exists
    try {
      if (!contentRepository.exists(resourceURI)) {
        logger.warn("Tried to delete non existing resource {} in site '{}'", resourceURI, site);
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("File lookup {} failed for site '{}'", resourceURI, site);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Delete the resource
    try {
      // TODO: Versions?
      contentRepository.delete(resourceURI);
    } catch (IOException e) {
      logger.warn("Error deleting resource {} from site '{}': {}", new Object[] {
          resourceURI,
          site,
          e.getMessage() });
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ContentRepositoryException e) {
      logger.warn("Error removing resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return Response.ok().build();
  }

  /**
   * Creates a file resource at the site's content repository by uploading
   * initial file content and returns the location to post updates to.
   * 
   * @param request
   *          the http request
   * @param resourceXml
   *          the new resource
   * @param path
   *          the path to store the resource at
   * @param mimeType
   *          the content mime type
   * @return response the resource location
   */
  @POST
  @Path("/uploads")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadFile(@Context HttpServletRequest request) {

    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }
    
    String fileName = null;
    Language language = null;
    String path = null;
    String mimeType = null;
    File uploadedFile = null;
    
    if (!ServletFileUpload.isMultipartContent(request))
      throw new WebApplicationException(Status.BAD_REQUEST);
      
    try {
      
      try {
        ServletFileUpload payload = new ServletFileUpload();
        for (FileItemIterator iter = payload.getItemIterator(request); iter.hasNext();) {
          FileItemStream item = iter.next();
          String fieldName = item.getFieldName();
          if (item.isFormField()) {
            String fieldValue = Streams.asString(item.openStream());
            if (StringUtils.isBlank(fieldValue))
              continue;
            if ("path".equals(fieldName)) {
              path = Streams.asString(item.openStream());
            } else if ("language".equals(fieldName)) {
              try {
                language = LanguageUtils.getLanguage(fieldValue);
              } catch (UnknownLanguageException e) {
                throw new WebApplicationException(Status.BAD_REQUEST);
              }
            } else if ("mimetype".equals(fieldName)) {
              mimeType = Streams.asString(item.openStream());
            }
          } else {
            // once the body gets read iter.hasNext must not be invoked
            // or the stream can not be read
            fileName = item.getName();
            mimeType = item.getContentType();
            uploadedFile = File.createTempFile("upload-", null);
            FileOutputStream fos = new FileOutputStream(uploadedFile);
            try {
              IOUtils.copy(item.openStream(), fos);
            } catch (IOException e) {
              IOUtils.closeQuietly(fos);
              throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
            } finally {
              IOUtils.closeQuietly(fos);
            }
          }
        }
      } catch (FileUploadException e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (IOException e) {
        FileUtils.deleteQuietly(uploadedFile);
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      
      // Make sure there is a language
      if (language == null) {
        language = LanguageUtils.getPreferredLanguage(request, site);
      }
  
      WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
  
      // Create the resource uri
      ResourceURIImpl resourceURI = null;
      String uuid = UUID.randomUUID().toString();
      if (!StringUtils.isBlank(path)) {
        try {
          if (!path.startsWith("/"))
            path = "/" + path;
          WebUrl url = new WebUrlImpl(site, path);
          resourceURI = new ResourceURIImpl(null, site, url.getPath(), uuid);
  
          // Make sure the resource doesn't exist
          if (contentRepository.exists(new ResourceURIImpl(null, site, url.getPath()))) {
            logger.warn("Tried to create already existing resource {} in site '{}'", resourceURI, site);
            throw new WebApplicationException(Status.CONFLICT);
          }
        } catch (IllegalArgumentException e) {
          logger.warn("Tried to create a resource with an invalid path '{}': {}", path, e.getMessage());
          throw new WebApplicationException(Status.BAD_REQUEST);
        } catch (ContentRepositoryException e) {
          logger.warn("Resource lookup {} failed for site '{}'", resourceURI, site);
          throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
      } else {
        resourceURI = new ResourceURIImpl(null, site, "/" + uuid.replaceAll("-", ""), uuid);
      }
  
      Resource<?> resource = null;
      URI uri = null;
      logger.debug("Adding resource to {}", resourceURI);
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByMimeType(mimeType);
      if (serializer == null) {
        logger.debug("No specialized resource serializer found, using regular file serializer");
        serializer = ResourceSerializerFactory.getSerializerByType(FileResource.TYPE);
      }
      resource = serializer.createNewResource(site);
      User admin = site.getAdministrator();
      User creator = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
      resource.setCreated(creator, new Date());
  
      // Store the new resource
      try {
        contentRepository.put(resource);
        String endpointURI = StringUtils.chomp(request.getRequestURI(), request.getPathInfo());
        uri = new URI(UrlUtils.concat(endpointURI, resourceURI.getIdentifier()));
      } catch (URISyntaxException e) {
        logger.warn("Error creating a uri for resource {}: {}", resourceURI, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (IOException e) {
        logger.warn("Error writing new resource {}: {}", resourceURI, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (IllegalStateException e) {
        logger.warn("Illegal state while adding new resource {}: {}", resourceURI, e.getMessage());
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      } catch (ContentRepositoryException e) {
        logger.warn("Error adding new resource {}: {}", resourceURI, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
  
      ResourceContent content = null;
      ResourceContentReader<?> reader = null;
      InputStream is = null;
      try {
        reader = serializer.getContentReader();
        is = new FileInputStream(uploadedFile);
        content = reader.createFromContent(is, language, uploadedFile.length(), fileName, mimeType);
      } catch (IOException e) {
        logger.warn("Error reading resource content {} from request", uri);
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (ParserConfigurationException e) {
        logger.warn("Error configuring parser to read resource content {}: {}", uri, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (SAXException e) {
        logger.warn("Error parsing udpated resource {}: {}", uri, e.getMessage());
        throw new WebApplicationException(Status.BAD_REQUEST);
      } finally {
        IOUtils.closeQuietly(is);
      }
  
      // TODO: Replace with current user
      User user = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
      content.setCreator(user);
      content.setFilename(fileName);
  
      try {
        is = new FileInputStream(uploadedFile);
        resource = contentRepository.putContent(resource.getURI(), content, is);
      } catch (IOException e) {
        logger.warn("Error writing content to resource {}: {}", uri, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (IllegalStateException e) {
        logger.warn("Illegal state while adding content to resource {}: {}", uri, e.getMessage());
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      } catch (ContentRepositoryException e) {
        logger.warn("Error adding content to resource {}: {}", uri, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } finally {
        IOUtils.closeQuietly(is);
      }

      // Create the response
      ResponseBuilder response = Response.created(uri);
      response.tag(new EntityTag(ResourceUtils.getETagValue(resource, null)));
      return response.build();

    } finally {
      FileUtils.deleteQuietly(uploadedFile);
    }
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
      docs = FilesEndpointDocs.createDocumentation(servicePath);
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
    return "file rest endpoint";
  }

}
