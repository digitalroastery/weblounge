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
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceReader;
import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.page.PageImpl;
import ch.o2it.weblounge.common.impl.content.page.PageReader;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.contentrepository.WritableContentRepository;
import ch.o2it.weblounge.kernel.SiteManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class implements the <code>REST</code> endpoint for file data.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class FileEndpoint {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FileEndpoint.class);

  /** The sites that are online */
  private transient SiteManager sites = null;

  /** The endpoint documentation */
  private String docs = null;

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
  @Path("/{resourceid}")
  public Response getImage(@Context HttpServletRequest request,
      @PathParam("resourceid") String resourceId) {
    String language = getLanguage(request).getIdentifier();
    return getImage(request, resourceId, language);
  }

  /**
   * Returns the resource with the given identifier or a <code>404</code> if the
   * resource could not be found.
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
  @Path("/{resourceid}/{language}")
  public Response getImage(@Context HttpServletRequest request,
      @PathParam("resourceid") String resourceId,
      @PathParam("languageid") String languageId) {

    // Check the parameters
    if (resourceId == null)
      return Response.status(Status.BAD_REQUEST).build();
    if (languageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Get the resource
    final FileResource resource = loadResource(request, resourceId, languageId);
    if (resource == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!isModidifed(resource, request)) {
      return Response.notModified().build();
    }

    // Create the response
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException,
          WebApplicationException {
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
    response.tag(new EntityTag(Long.toString(resource.getModificationDate().getTime())));
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
  @Path("/{resourceid}")
  public Response updatePage(@Context HttpServletRequest request,
      @PathParam("resourceid") String resourceId,
      @FormParam("page") String resourceXml,
      @HeaderParam("If-Match") String ifMatchHeader) {

    // Check the parameters
    if (resourceId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Extract the site
    Site site = getSite(request);
    User user = null; // TODO: Extract user
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    ResourceURI resourceURI = new FileResourceURIImpl(site, null, resourceId);

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
    if (ifMatchHeader != null) {
      try {
        FileResource currentPage = (FileResource)contentRepository.get(resourceURI);
        String etag = Long.toString(currentPage.getModificationDate().getTime());
        if (!etag.equals(ifMatchHeader)) {
          throw new WebApplicationException(Status.PRECONDITION_FAILED);
        }
      } catch (ContentRepositoryException e) {
        logger.warn("Error reading current resource {} from repository: {}", resourceURI, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    }

    // Parse the resource and update it in the repository
    FileResource resource = null;
    try {
      FileResourceReader resourceReader = new FileResourceReader();
      resource = resourceReader.read(resourceURI, IOUtils.toInputStream(resourceXml));
      // TODO: Replace this with current user
      User admin = site.getAdministrator();
      User modifier = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
      resource.setModified(modifier, new Date());
      // TODO: Store the resource
      //contentRepository.update(resourceURI, resource, user);
    } catch (SecurityException e) {
      logger.warn("Tried to update resource {} of site '{}' without permission", resourceURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error reading udpated resource {} from request", resourceURI);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ParserConfigurationException e) {
      logger.warn("Error configuring parser to read udpated resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SAXException e) {
      logger.warn("Error parsing udpated resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    // Create the response
    ResponseBuilder response = Response.ok();
    response.tag(new EntityTag(Long.toString(resource.getModificationDate().getTime())));
    return response.build();
  }

  /**
   * Creates a resource at the site's content repository and returns the location to
   * post updates to.
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
  public Response addPage(@Context HttpServletRequest request,
      @FormParam("resource") String resourceXml, @FormParam("path") String path) {

    Site site = getSite(request);
    User user = null; // TODO: Extract user
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);

    // Create the resource uri
    ResourceURIImpl resourceURI = null;
    String uuid = UUID.randomUUID().toString();
    if (!StringUtils.isBlank(path)) {
      try {
        if (!path.startsWith("/"))
          path = "/" + path;
        WebUrl url = new WebUrlImpl(site, path);
        resourceURI = new FileResourceURIImpl(site, url.getPath(), uuid);

        // Make sure the resource doesn't exist
        if (contentRepository.exists(new FileResourceURIImpl(site, url.getPath()))) {
          logger.warn("Tried to create already existing resource {} in site '{}'", resourceURI, site);
          throw new WebApplicationException(Status.CONFLICT);
        }
      } catch (IllegalArgumentException e) {
        logger.warn("Tried to create a resource with an invalid path '{}': {}", path, e.getMessage());
        throw new WebApplicationException(Status.BAD_REQUEST);
      } catch (ContentRepositoryException e) {
        logger.warn("Page lookup {} failed for site '{}'", resourceURI, site);
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    } else {
      resourceURI = new FileResourceURIImpl(site, "/" + uuid.replaceAll("-", ""), uuid);
    }

    // Parse the resource and store it
    PageImpl resource = null;
    URI uri = null;
    if (!StringUtils.isBlank(resourceXml)) {
      logger.debug("Adding resource to {}", resourceURI);
      try {
        PageReader resourceReader = new PageReader();
        resource = resourceReader.read(resourceURI, IOUtils.toInputStream(resourceXml));
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
      resource = new PageImpl(resourceURI);
      resource.setTemplate(site.getDefaultTemplate().getIdentifier());
      User admin = site.getAdministrator();
      User creator = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
      resource.setCreated(creator, new Date());
    }

    // Store the new resource
    try {
      contentRepository.put(resource, user);
      uri = new URI(UrlSupport.concat(request.getRequestURL().toString(), resourceURI.getId()));
    } catch (URISyntaxException e) {
      logger.warn("Error creating a uri for resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SecurityException e) {
      logger.warn("Tried to update resource {} of site '{}' without permission", resourceURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error writing new resource {}: {}", resourceURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.created(uri);
    response.tag(new EntityTag(Long.toString(resource.getModificationDate().getTime())));
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
  @Path("/{resourceid}")
  public Response deletePage(@Context HttpServletRequest request,
      @PathParam("resourceid") String resourceId) {

    // Check the parameters
    if (resourceId == null)
      return Response.status(Status.BAD_REQUEST).build();

    Site site = getSite(request);
    User user = null; // TODO: Extract user
    ResourceURI resourceURI = new FileResourceURIImpl(site, null, resourceId);
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);

    // Make sure the resource doesn't exist
    try {
      if (!contentRepository.exists(resourceURI)) {
        logger.warn("Tried to delete non existing resource {} in site '{}'", resourceURI, site);
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Page lookup {} failed for site '{}'", resourceURI, site);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Delete the resource
    try {
      contentRepository.delete(resourceURI, user);
    } catch (SecurityException e) {
      logger.warn("Tried to delete resource {} of site '{}' without permission", resourceURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error deleting resource {} from site '{}': {}", new Object[] {
          resourceURI,
          site,
          e.getMessage() });
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return Response.ok().build();
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
      String endpointUrl = "/system/files";
      // TODO: determine endpoint url
      docs = FileEndpointDocs.createDocumentation(endpointUrl);
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
   * Returns the language from the request that matches a site language.
   * 
   * @param request
   *          the request language
   * @return the language
   */
  private Language getLanguage(HttpServletRequest request) {
    Site site = getSite(request);
    // TODO: extract language
    return site.getDefaultLanguage();
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
   * Returns the resource identified by the given request and resource
   * identifier or <code>null</code> if either one of the site, the site's
   * content repository or the resource itself is not available.
   * 
   * @param request
   *          the servlet request
   * @param resourceId
   *          the resource identifier
   * @param languageId
   *          the language
   * @return the resource
   */
  protected FileResource loadResource(HttpServletRequest request, String resourceId,
      String languageId) {
    if (sites == null) {
      logger.debug("Unable to load page '{}': no sites registered", resourceId);
      return null;
    }

    // Extract the site
    Site site = getSite(request);

    // Extract the language
    Language language = site.getLanguage(languageId);
    if (language == null) {
      logger.warn("Language '{}' is not supported by site '{}'", site);
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    // Look for the content repository
    ContentRepository contentRepository = ContentRepositoryFactory.getRepository(site);
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }

    // Load the resource and return it
    // try {
    ResourceURI resourceURI = new FileResourceURIImpl(site, null, resourceId);
    // Resource resource = contentRepository.getPage(resourceURI);
    URL fileUrl = getClass().getResource("/image/placeholder.jpg");
    FileResource resource = new FileResourceImpl(resourceURI, fileUrl);
    return resource;
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
    return "file rest endpoint";
  }

}
