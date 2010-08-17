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

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.page.PageImpl;
import ch.o2it.weblounge.common.impl.content.page.PageReader;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.WritableContentRepository;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class implements the <code>REST</code> endpoint for page data.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class PagesEndpoint extends ContentRepositoryEndpoint {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PagesEndpoint.class);

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Returns the page with the given identifier or a <code>404</code> if the
   * page could not be found.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @return the page
   */
  @GET
  @Path("/{page}")
  public Response getPage(@Context HttpServletRequest request,
      @PathParam("page") String pageId) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    Page page = (Page)loadResource(request, pageId, Page.TYPE);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!isModified(page, request)) {
      return Response.notModified().build();
    }

    // Create the response
    ResponseBuilder response = Response.ok(page.toXml());
    response.tag(new EntityTag(Long.toString(page.getModificationDate().getTime())));
    response.lastModified(page.getModificationDate());
    return response.build();
  }

  /**
   * Updates the indicated page.
   * 
   * @param request
   *          the http request
   * @param pageId
   *          the page identifier
   * @param pageXml
   *          the updated page
   * @param ifMatchHeader
   *          the page's <code>etag</code> value
   * @return response an empty response
   * @throws WebApplicationException
   *           if the update fails
   */
  @PUT
  @Path("/{page}")
  public Response updatePage(@Context HttpServletRequest request,
      @PathParam("page") String pageId, @FormParam("content") String pageXml,
      @HeaderParam("If-Match") String ifMatchHeader) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();
    if (pageXml == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Extract the site
    Site site = getSite(request);
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    ResourceURI pageURI = new PageURIImpl(site, null, pageId);

    // Does the page exist?
    try {
      if (!contentRepository.exists(pageURI)) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Error lookup up page {} from repository: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Check the value of the If-Match header against the etag
    if (ifMatchHeader != null) {
      try {
        Page currentPage = (Page)contentRepository.get(pageURI);
        String etag = Long.toString(currentPage.getModificationDate().getTime());
        if (!etag.equals(ifMatchHeader)) {
          throw new WebApplicationException(Status.PRECONDITION_FAILED);
        }
      } catch (ContentRepositoryException e) {
        logger.warn("Error reading current page {} from repository: {}", pageURI, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    }

    // Parse the page and update it in the repository
    Page page = null;
    try {
      PageReader pageReader = new PageReader();
      page = pageReader.read(pageURI, IOUtils.toInputStream(pageXml));
      // TODO: Replace this with current user
      User admin = site.getAdministrator();
      User modifier = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
      page.setModified(modifier, new Date());
      contentRepository.put(page);
    } catch (SecurityException e) {
      logger.warn("Tried to update page {} of site '{}' without permission", pageURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error reading udpated page {} from request", pageURI);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ParserConfigurationException e) {
      logger.warn("Error configuring parser to read udpated page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SAXException e) {
      logger.warn("Error parsing udpated page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (IllegalStateException e) {
      logger.warn("Error updating page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error updating page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.ok();
    response.tag(new EntityTag(Long.toString(page.getModificationDate().getTime())));
    return response.build();
  }

  /**
   * Creates a page at the site's content repository and returns the location to
   * post updates to.
   * 
   * @param request
   *          the http request
   * @param pageXml
   *          the new page
   * @param path
   *          the path to store the page at
   * @return response the page location
   */
  @POST
  @Path("/")
  public Response createPage(@Context HttpServletRequest request,
      @FormParam("content") String pageXml, @FormParam("path") String path) {

    Site site = getSite(request);
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);

    // Create the page uri
    ResourceURIImpl pageURI = null;
    String uuid = UUID.randomUUID().toString();
    if (!StringUtils.isBlank(path)) {
      try {
        if (!path.startsWith("/"))
          path = "/" + path;
        WebUrl url = new WebUrlImpl(site, path);
        pageURI = new PageURIImpl(site, url.getPath(), uuid);

        // Make sure the page doesn't exist
        if (contentRepository.exists(new PageURIImpl(site, url.getPath()))) {
          logger.warn("Tried to create already existing page {} in site '{}'", pageURI, site);
          throw new WebApplicationException(Status.CONFLICT);
        }
      } catch (IllegalArgumentException e) {
        logger.warn("Tried to create a page with an invalid path '{}': {}", path, e.getMessage());
        throw new WebApplicationException(Status.BAD_REQUEST);
      } catch (ContentRepositoryException e) {
        logger.warn("Page lookup {} failed for site '{}'", pageURI, site);
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    } else {
      pageURI = new PageURIImpl(site, "/" + uuid.replaceAll("-", ""), uuid);
    }

    // Parse the page and store it
    PageImpl page = null;
    URI uri = null;
    if (!StringUtils.isBlank(pageXml)) {
      logger.debug("Adding page to {}", pageURI);
      try {
        PageReader pageReader = new PageReader();
        page = pageReader.read(pageURI, IOUtils.toInputStream(pageXml));
      } catch (IOException e) {
        logger.warn("Error reading page {} from request", pageURI);
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (ParserConfigurationException e) {
        logger.warn("Error configuring parser to read udpated page {}: {}", pageURI, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (SAXException e) {
        logger.warn("Error parsing udpated page {}: {}", pageURI, e.getMessage());
        throw new WebApplicationException(Status.BAD_REQUEST);
      }
    } else {
      logger.debug("Creating new page at {}", pageURI);
      page = new PageImpl(pageURI);
      page.setTemplate(site.getDefaultTemplate().getIdentifier());
      User admin = site.getAdministrator();
      User creator = new UserImpl(admin.getLogin(), site.getIdentifier(), admin.getName());
      page.setCreated(creator, new Date());
    }

    // Store the new page
    try {
      contentRepository.put(page);
      uri = new URI(UrlSupport.concat(request.getRequestURL().toString(), pageURI.getId()));
    } catch (URISyntaxException e) {
      logger.warn("Error creating a uri for page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SecurityException e) {
      logger.warn("Tried to update page {} of site '{}' without permission", pageURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error writing new page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Error adding new page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error adding new page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.created(uri);
    response.tag(new EntityTag(Long.toString(page.getModificationDate().getTime())));
    return response.build();
  }

  /**
   * Removes the indicated page from the site.
   * 
   * @param request
   *          the http request
   * @param pageId
   *          the page identifier
   * @return response an empty response
   */
  @DELETE
  @Path("/{page}")
  public Response deletePage(@Context HttpServletRequest request,
      @PathParam("page") String pageId) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    Site site = getSite(request);
    ResourceURI pageURI = new PageURIImpl(site, null, pageId);
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);

    // Make sure the page doesn't exist
    try {
      if (!contentRepository.exists(pageURI)) {
        logger.warn("Tried to delete non existing page {} in site '{}'", pageURI, site);
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Page lookup {} failed for site '{}'", pageURI, site);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Delete the page
    try {
      contentRepository.delete(pageURI);
    } catch (SecurityException e) {
      logger.warn("Tried to delete page {} of site '{}' without permission", pageURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error deleting page {} from site '{}': {}", new Object[] {
          pageURI,
          site,
          e.getMessage() });
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ContentRepositoryException e) {
      logger.warn("Error deleting page {} from site '{}': {}", new Object[] {
          pageURI,
          site,
          e.getMessage() });
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return Response.ok().build();
  }

  /**
   * Returns the composer specified by <code>composerId</code> and
   * <code>pageletIndex</code> or a <code>404</code> if either the page or the
   * composer does not exist.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @param composerId
   *          the composer identifier
   * @return the composer
   */
  @GET
  @Path("/{page}/composers/{composer}")
  public Response getComposer(@Context HttpServletRequest request,
      @PathParam("page") String pageId,
      @PathParam("composer") String composerId) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();
    else if (composerId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    Page page = (Page)loadResource(request, pageId, Page.TYPE);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!isModified(page, request)) {
      return Response.notModified().build();
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Return the composer
    return Response.ok(composer.toXml()).lastModified(page.getModificationDate()).build();
  }

  /**
   * Returns the pagelet specified by <code>pageId</code>,
   * <code>composerId</code> and <code>pageletIndex</code> or a <code>404</code>
   * if either of the the page, the composer or the pagelet does not exist.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @param composerId
   *          the composer identifier
   * @param pageletIndex
   *          the pagelet index within the composer
   * @return the pagelet
   */
  @GET
  @Path("/{page}/composers/{composer}/pagelets/{pageletindex}")
  public Response getPagelet(@Context HttpServletRequest request,
      @PathParam("page") String pageId,
      @PathParam("composer") String composerId,
      @PathParam("pageletindex") int pageletIndex) {

    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();
    else if (composerId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    Page page = (Page)loadResource(request, pageId, Page.TYPE);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!isModified(page, request)) {
      return Response.notModified().build();
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null || composer.size() < pageletIndex) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Return the pagelet
    return Response.ok(composer.getPagelet(pageletIndex).toXml()).build();
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
      String endpointUrl = "/system/weblounge/pages";
      // TODO: determine endpoint url
      docs = PagesEndpointDocs.createDocumentation(endpointUrl);
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
    return "pages rest endpoint";
  }

}
