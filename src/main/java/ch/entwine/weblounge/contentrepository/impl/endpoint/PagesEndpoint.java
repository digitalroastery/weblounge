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
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchQuery.Order;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.content.page.PageSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.security.SecurityUtils;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.common.url.WebUrl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class implements the <code>REST</code> endpoint for page data.
 */
@Path("/")
@Produces(MediaType.APPLICATION_XML)
public class PagesEndpoint extends ContentRepositoryEndpoint {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PagesEndpoint.class);

  /** The security service */
  protected SecurityService securityService = null;

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Returns a collection of pages which match the given criteria.
   * 
   * @param request
   *          the request
   * @param path
   *          the page path (e.g. <code>/my/simple/path</code>)
   * @param subjectstring
   *          one ore more subjects, divided by a comma
   * @param searchterms
   *          fulltext search terms
   * @param sort
   *          sort order, possible values are
   *          <code>created-asc, created-desc, published-asc, published-desc, modified-asc & modified-desc</code>
   * @param limit
   *          search result limit
   * @param offset
   *          search result offset (for paging in combination with limit)
   * @param details
   *          switch for providing pages including their bodies
   * @return a collection of matching pages
   */
  @GET
  @Path("/")
  public Response getAllPages(@Context HttpServletRequest request,
      @QueryParam("path") String path,
      @QueryParam("subjects") String subjectstring,
      @QueryParam("searchterms") String searchterms,
      @QueryParam("filter") String filter,
      @QueryParam("sort") @DefaultValue("modified-desc") String sort,
      @QueryParam("version") @DefaultValue("0") long version,
      @QueryParam("limit") @DefaultValue("10") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("details") @DefaultValue("false") boolean details) {

    // Create search query
    Site site = getSite(request);
    SearchQuery q = new SearchQueryImpl(site);

    q.withType(Page.TYPE);
    q.withVersion(version);

    // Path
    if (StringUtils.isNotBlank(path))
      q.withPath(path);

    // Subjects
    if (StringUtils.isNotBlank(subjectstring)) {
      StringTokenizer subjects = new StringTokenizer(subjectstring, ",");
      while (subjects.hasMoreTokens())
        q.withSubject(subjects.nextToken());
    }

    // Search terms
    if (StringUtils.isNotBlank(searchterms))
      q.withText(searchterms, true);

    // Filter query
    if (StringUtils.isNotBlank(filter))
      q.withFilter(filter);

    // Limit and Offset
    q.withLimit(limit);
    q.withOffset(offset);

    // Sort order
    if (StringUtils.equalsIgnoreCase("modified-asc", sort)) {
      q.sortByModificationDate(Order.Ascending);
    } else if (StringUtils.equalsIgnoreCase("modified-desc", sort)) {
      q.sortByModificationDate(Order.Descending);
    } else if (StringUtils.equalsIgnoreCase("created-asc", sort)) {
      q.sortByCreationDate(Order.Ascending);
    } else if (StringUtils.equalsIgnoreCase("created-desc", sort)) {
      q.sortByCreationDate(Order.Descending);
    } else if (StringUtils.equalsIgnoreCase("published-asc", sort)) {
      q.sortByPublishingDate(Order.Ascending);
    } else if (StringUtils.equalsIgnoreCase("published-desc", sort)) {
      q.sortByPublishingDate(Order.Descending);
    }

    // Load the result
    String result = loadResultSet(q, details);

    return Response.ok(result).build();
  }

  /**
   * Returns a collection of pages that are defined as pending.
   * 
   * @param request
   *          the request
   * @param filter
   *          further search result filtering
   * @param sort
   *          sort order, possible values are
   *          <code>created-asc, created-desc, published-asc, published-desc, modified-asc & modified-desc</code>
   * @param limit
   *          search result limit
   * @param offset
   *          search result offset (for paging in combination with limit)
   * @param details
   *          switch for providing pages including their bodies
   * @return a collection of matching pages
   */
  @GET
  @Path("/pending")
  public Response getPending(@Context HttpServletRequest request,
      @QueryParam("filter") String filter,
      @QueryParam("sort") @DefaultValue("modified-desc") String sort,
      @QueryParam("limit") @DefaultValue("10") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("details") @DefaultValue("false") boolean details) {

    // Create search query
    Site site = getSite(request);
    SearchQuery q = new SearchQueryImpl(site);

    // Only take resources that have not been modified
    q.withoutPublication();

    // Type
    q.withType(Page.TYPE);

    // Filter query
    if (StringUtils.isNotBlank(filter))
      q.withFilter(filter);

    // Limit and Offset
    q.withLimit(limit);
    q.withOffset(offset);

    // Sort order
    if (StringUtils.equalsIgnoreCase("modified-asc", sort)) {
      q.sortByModificationDate(Order.Ascending);
    } else if (StringUtils.equalsIgnoreCase("modified-desc", sort)) {
      q.sortByModificationDate(Order.Descending);
    } else if (StringUtils.equalsIgnoreCase("created-asc", sort)) {
      q.sortByCreationDate(Order.Ascending);
    } else if (StringUtils.equalsIgnoreCase("created-desc", sort)) {
      q.sortByCreationDate(Order.Descending);
    } else if (StringUtils.equalsIgnoreCase("published-asc", sort)) {
      q.sortByPublishingDate(Order.Ascending);
    } else if (StringUtils.equalsIgnoreCase("published-desc", sort)) {
      q.sortByPublishingDate(Order.Descending);
    }

    // Load the result
    String result = loadResultSet(q, details);

    // Return the response
    return Response.ok(result).build();

  }

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
  public Response getPageById(@Context HttpServletRequest request,
      @PathParam("page") String pageId,
      @QueryParam("version") @DefaultValue("0") long version) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Resolve name clash
    if ("docs".equals(pageId)) {
      return Response.ok(getDocumentation(request)).type(MediaType.TEXT_HTML).build();
    }

    // Load the page
    Page page = (Page) loadResource(request, pageId, Page.TYPE, version);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.hasChanged(request, page)) {
      return Response.notModified().build();
    }

    // Create the response
    ResponseBuilder response = Response.ok(page.toXml());
    response.tag(ResourceUtils.getETagValue(page));
    response.lastModified(ResourceUtils.getModificationDate(page));
    return response.build();
  }

  /**
   * Returns child pages of the page with the given identifier or a
   * <code>404</code> if the page could not be found.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @return the child pages
   */
  @GET
  @Path("/{page}/children")
  public Response getChildPagesByURI(@Context HttpServletRequest request,
      @PathParam("page") String pageId) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    Page page = (Page) loadResource(request, pageId, Page.TYPE);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    Site site = getSite(request);
    SearchQuery q = new SearchQueryImpl(site);
    q.withType(Page.TYPE);
    q.withPathPrefix(page.getURI().getPath());

    ContentRepository repository = getContentRepository(site, false);
    SearchResult result = null;
    try {
      result = repository.find(q);
    } catch (ContentRepositoryException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    StringBuffer buf = new StringBuffer("<pages>");
    for (SearchResultItem item : result.getItems()) {
      String headerXml = ((PageSearchResultItemImpl) item).getPageHeaderXml();
      buf.append(headerXml);
    }
    buf.append("</pages>");

    // Create the response
    return Response.ok(buf.toString()).build();
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

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    ResourceURI pageURI = new PageURIImpl(site, null, pageId, Resource.WORK);

    // Does the page exist?
    Page currentPage = null;
    try {
      if (!contentRepository.exists(pageURI)) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      currentPage = (Page) contentRepository.get(pageURI);
    } catch (ContentRepositoryException e) {
      logger.warn("Error lookup up page {} from repository: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Check the value of the If-Match header against the etag
    if (ifMatchHeader != null) {
      String etag = ResourceUtils.getETagValue(currentPage);
      if (!etag.equals(ifMatchHeader)) {
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      }
    }

    // Get the user
    User user = securityService.getUser();
    if (user == null)
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // Make sure the user has editing rights
    if (!SecurityUtils.userHasRole(user, SystemRole.EDITOR))
      throw new WebApplicationException(Status.UNAUTHORIZED);

    boolean isAdmin = SecurityUtils.userHasRole(user, SystemRole.SITEADMIN);

    // If the page is locked by a different user, refuse
    if (currentPage.isLocked() && (!currentPage.getLockOwner().equals(user) && !isAdmin)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    // Parse the page and update it in the repository
    Page page = null;
    try {
      PageReader pageReader = new PageReader();
      page = pageReader.read(IOUtils.toInputStream(pageXml, "utf-8"), site);
      // TODO: Replace this with current user
      page.setModified(user, new Date());
      page.setVersion(Resource.WORK);
      contentRepository.put(page);
      if (!page.getURI().getPath().equals(currentPage.getURI().getPath())) {
        contentRepository.move(currentPage.getURI(), page.getURI());
      }
    } catch (SecurityException e) {
      logger.warn("Tried to update page {} of site '{}' without permission", pageURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error reading updated page {} from request", pageURI);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ParserConfigurationException e) {
      logger.warn("Error configuring parser to read updated page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SAXException e) {
      logger.warn("Error parsing updated page {}: {}", pageURI, e.getMessage());
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
    response.tag(ResourceUtils.getETagValue(page));
    response.lastModified(ResourceUtils.getModificationDate(page));
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

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);

    // Create the page uri
    ResourceURIImpl pageURI = null;
    String uuid = UUID.randomUUID().toString();
    if (!StringUtils.isBlank(path)) {
      try {
        if (!path.startsWith("/"))
          path = "/" + path;
        WebUrl url = new WebUrlImpl(site, path);
        pageURI = new PageURIImpl(site, url.getPath(), uuid, Resource.WORK);
      } catch (IllegalArgumentException e) {
        logger.warn("Tried to create a page with an invalid path '{}': {}", path, e.getMessage());
        throw new WebApplicationException(Status.BAD_REQUEST);
      }
    } else {
      pageURI = new PageURIImpl(site, "/" + uuid.replaceAll("-", ""), uuid, Resource.WORK);
    }

    // Make sure the page doesn't exist
    try {
      if (contentRepository.existsInAnyVersion(pageURI)) {
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

    // Get the user
    User user = securityService.getUser();
    if (user == null)
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // Make sure the user has editing rights
    if (!SecurityUtils.userHasRole(user, SystemRole.EDITOR))
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // Parse the page and store it
    PageImpl page = null;
    URI uri = null;
    if (!StringUtils.isBlank(pageXml)) {
      logger.debug("Adding page to {}", pageURI);
      try {
        PageReader pageReader = new PageReader();
        page = pageReader.read(IOUtils.toInputStream(pageXml, "utf-8"), site);
      } catch (IOException e) {
        logger.warn("Error reading page {} from request", pageURI);
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (ParserConfigurationException e) {
        logger.warn("Error configuring parser to read updated page {}: {}", pageURI, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } catch (SAXException e) {
        logger.warn("Error parsing updated page {}: {}", pageURI, e.getMessage());
        throw new WebApplicationException(Status.BAD_REQUEST);
      }
    } else {
      logger.debug("Creating new page at {}", pageURI);
      page = new PageImpl(pageURI);
      page.setTemplate(site.getDefaultTemplate().getIdentifier());
      page.setCreated(user, new Date());
    }

    // Store the new page
    try {
      contentRepository.put(page);
      uri = new URI(UrlUtils.concat(request.getRequestURL().toString(), pageURI.getIdentifier()));
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
    response.tag(ResourceUtils.getETagValue(page));
    response.lastModified(ResourceUtils.getModificationDate(page));
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

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to write to read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    ResourceURI livePageURI = new PageURIImpl(site, null, pageId, Resource.LIVE);
    ResourceURI workPageURI = new PageURIImpl(site, null, pageId, Resource.WORK);
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);

    try {
      if (!contentRepository.existsInAnyVersion(livePageURI)) {
        logger.warn("Tried to delete non existing page {} in site '{}'", livePageURI, site);
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Page lookup {} failed for site '{}'", livePageURI, site);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    Page page = null;
    try {
      if (contentRepository.exists(livePageURI)) {
        page = (Page) contentRepository.get(livePageURI);
      } else {
        page = (Page) contentRepository.get(workPageURI);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Error lookup up page {} from repository: {}", livePageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Get the user
    User user = securityService.getUser();
    if (user == null)
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // Make sure the user has editing rights
    if (!SecurityUtils.userHasRole(user, SystemRole.EDITOR))
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // If the page is published, the user needs publishing rights
    if (page.isPublished() && !SecurityUtils.userHasRole(user, SystemRole.PUBLISHER))
      throw new WebApplicationException(Status.UNAUTHORIZED);

    boolean isAdmin = SecurityUtils.userHasRole(user, SystemRole.SITEADMIN);

    // If the page is locked by a different user, refuse
    if (page.isLocked() && (!page.getLockOwner().equals(user) && !isAdmin)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    // Delete the page
    try {
      contentRepository.delete(page.getURI(), true);
    } catch (SecurityException e) {
      logger.warn("Tried to delete page {} of site '{}' without permission", livePageURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error deleting page {} from site '{}': {}", new Object[] {
          livePageURI,
          site,
          e.getMessage() });
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ContentRepositoryException e) {
      logger.warn("Error deleting page {} from site '{}': {}", new Object[] {
          livePageURI,
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
      @PathParam("composer") String composerId,
      @QueryParam("version") @DefaultValue("0") long version) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();
    else if (composerId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    Page page = (Page) loadResource(request, pageId, Page.TYPE, version);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.hasChanged(request, page)) {
      return Response.notModified().build();
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Return the composer
    return Response.ok(composer.toXml()).lastModified(ResourceUtils.getModificationDate(page)).build();
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
      @PathParam("pageletindex") int pageletIndex,
      @QueryParam("version") @DefaultValue("0") long version) {

    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();
    else if (composerId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    Page page = (Page) loadResource(request, pageId, Page.TYPE, version);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.hasChanged(request, page)) {
      return Response.notModified().build();
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null || composer.size() < pageletIndex) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Return the pagelet
    ResponseBuilder response = Response.ok(composer.getPagelet(pageletIndex).toXml());
    response.lastModified(ResourceUtils.getModificationDate(page));
    return response.build();
  }

  /**
   * Locks the page and returns with a <code>200</code> status code if the lock
   * operation succeeds, <code>400</code> if the page is not found or
   * <code>403</code> if another user has already locked the page.
   * <p>
   * If <code>user</code> is not specified, then the current user will be used
   * for lock acquisition.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @param userId
   *          the user
   * @return the page
   */
  @PUT
  @Path("/{page}/lock")
  public Response lockPage(@Context HttpServletRequest request,
      @PathParam("page") String pageId, @FormParam("user") String userId,
      @HeaderParam("If-Match") String ifMatchHeader) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Extract the site
    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to lock a page in a read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    ResourceURI workURI = new PageURIImpl(site, null, pageId, Resource.WORK);

    // Does the page exist at all?
    Page page = null;
    try {
      if (!contentRepository.existsInAnyVersion(workURI))
        throw new WebApplicationException(Status.NOT_FOUND);
    } catch (ContentRepositoryException e) {
      logger.warn("Error looking up page {} from repository: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Make sure we have a work page. If it doesn't exist yet, it needs
    // to be created as a result of the lock operation
    try {
      ResourceURI liveURI = new PageURIImpl(site, null, pageId, Resource.LIVE);
      if (!contentRepository.exists(workURI)) {
        logger.debug("Creating work version of {}", liveURI);
        page = (Page) contentRepository.get(liveURI);
        page.setVersion(Resource.WORK);
        contentRepository.put(page);
      } else {
        page = (Page) contentRepository.get(workURI);
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Error lookup up page {} from repository: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Error putting a page work copy {} to repository: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (IOException e) {
      logger.warn("Error putting a page work copy {} to repository: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Check the value of the If-Match header against the etag
    if (ifMatchHeader != null) {
      String etag = ResourceUtils.getETagValue(page);
      if (!etag.equals(ifMatchHeader)) {
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      }
    }

    // Get the user
    User user = securityService.getUser();
    if (user == null)
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // Make sure the user has editing rights
    if (!SecurityUtils.userHasRole(user, SystemRole.EDITOR))
      throw new WebApplicationException(Status.UNAUTHORIZED);

    boolean isAdmin = SecurityUtils.userHasRole(user, SystemRole.SITEADMIN);

    // If the page is locked by a different user, refuse
    if (page.isLocked() && (!page.getLockOwner().equals(user) && ! isAdmin)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    // Finally, perform the lock operation (on all resource versions)
    try {
      contentRepository.lock(workURI, user);
      logger.info("Page {} has been locked by {}", workURI, user);
    } catch (SecurityException e) {
      logger.warn("Tried to lock page {} of site '{}' without permission", workURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error writing page lock {} to repository", workURI);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Error locking page {}: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error locking page {}: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.ok();
    response.tag(ResourceUtils.getETagValue(page));
    response.lastModified(page.getModificationDate());
    return response.build();
  }

  /**
   * Unlocks the page and returns with a <code>200</code> status code if the
   * unlock operation succeeds, <code>400</code> if the page is not found or
   * <code>403</code> if the page is locked by a different user.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @return the page
   */
  @DELETE
  @Path("/{page}/lock")
  public Response unlockPage(@Context HttpServletRequest request,
      @PathParam("page") String pageId,
      @HeaderParam("If-Match") String ifMatchHeader) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Extract the site
    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to unlock a page in a read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    ResourceURI pageURI = new PageURIImpl(site, null, pageId, Resource.WORK);

    // Does the page exist?
    Page page = null;
    try {
      if (!contentRepository.existsInAnyVersion(pageURI))
        throw new WebApplicationException(Status.NOT_FOUND);
      page = (Page) contentRepository.get(contentRepository.getVersions(pageURI)[0]);
    } catch (ContentRepositoryException e) {
      logger.warn("Error lookup up page {} from repository: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Check the value of the If-Match header against the etag
    if (ifMatchHeader != null) {
      String etag = ResourceUtils.getETagValue(page);
      if (!etag.equals(ifMatchHeader)) {
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      }
    }

    // Get the user
    User user = securityService.getUser();
    if (user == null)
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // Make sure the user has editing rights
    if (!SecurityUtils.userHasRole(user, SystemRole.EDITOR))
      throw new WebApplicationException(Status.UNAUTHORIZED);

    boolean isAdmin = SecurityUtils.userHasRole(user, SystemRole.SITEADMIN);

    // If the page is locked by a different user, refuse
    if (page.isLocked() && (!page.getLockOwner().equals(user) && !isAdmin)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    // Finally, perform the lock operation (on all resource versions)
    try {
      contentRepository.unlock(pageURI, user);
      logger.info("Page {} has been unlocked by {}", pageURI, user);
    } catch (SecurityException e) {
      logger.warn("Tried to unlock page {} of site '{}' without permission", pageURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error removing page lock {} from repository", pageURI);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Error unlocking page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error unlocking page {}: {}", pageURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.ok();
    response.tag(ResourceUtils.getETagValue(page));
    response.lastModified(ResourceUtils.getModificationDate(page));
    return response.build();
  }

  /**
   * Publishes the page for the given date range and returns with a
   * <code>200</code> status code if the publish operation succeeds,
   * <code>400</code> if the page is not found or <code>403</code> if the page
   * is currently locked by a different user.
   * <p>
   * If <code>startdate</code> is not specified, then the page will be published
   * immediately. A missing <code>enddate</code> indicates to publish the page
   * forever.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @param startDateText
   *          the optional publishing start date
   * @param endDateText
   *          the optional publishing end date
   * @return the page
   */
  @PUT
  @Path("/{page}/publish")
  public Response publishPage(@Context HttpServletRequest request,
      @PathParam("page") String pageId,
      @FormParam("startdate") String startDateText,
      @FormParam("enddate") String endDateText,
      @HeaderParam("If-Match") String ifMatchHeader) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Extract the site
    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to publish a page in a read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    ResourceURI workURI = new PageURIImpl(site, null, pageId, Resource.WORK);

    // Does the work page exist?
    Page page = null;
    try {
      if (!contentRepository.existsInAnyVersion(workURI))
        throw new WebApplicationException(Status.NOT_FOUND);
      if (!contentRepository.exists(workURI))
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      page = (Page) contentRepository.get(workURI);
    } catch (ContentRepositoryException e) {
      logger.warn("Error looking up page {} from repository: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Check the value of the If-Match header against the etag
    if (ifMatchHeader != null) {
      String etag = ResourceUtils.getETagValue(page);
      if (!etag.equals(ifMatchHeader)) {
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      }
    }

    // Get the user
    User user = securityService.getUser();
    if (user == null)
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // Make sure the user has publishing rights
    if (!SecurityUtils.userHasRole(user, SystemRole.PUBLISHER))
      throw new WebApplicationException(Status.UNAUTHORIZED);

    boolean isAdmin = SecurityUtils.userHasRole(user, SystemRole.SITEADMIN);

    // If the page is locked by a different user, refuse
    if (page.isLocked() && (!page.getLockOwner().equals(user) && !isAdmin)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    // Fix the dates
    Date startDate = null;
    Date endDate = null;
    DateFormat df = new SimpleDateFormat();
    try {
      if (StringUtils.isNotBlank(startDateText))
        startDate = df.parse(startDateText);
      else
        startDate = new Date();
      if (StringUtils.isNotBlank(endDateText)) {
        endDate = df.parse(endDateText);
      }
    } catch (ParseException e) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    // Finally, perform the publish operation
    try {
      page.setPublished(user, startDate, endDate);
      page.setModified(user, new Date());
      page.setVersion(Resource.LIVE);
      contentRepository.put(page);
      contentRepository.delete(workURI);
      logger.info("Page {} has been published by {}", workURI, user);
    } catch (SecurityException e) {
      logger.warn("Tried to publish page {} of site '{}' without permission", workURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error writing published page {} to repository", workURI);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Error publishing page {}: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error publishing page {}: {}", workURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.ok();
    response.tag(ResourceUtils.getETagValue(page));
    response.lastModified(page.getModificationDate());
    return response.build();
  }

  /**
   * Unpublishes the page on the given date and returns with a <code>200</code>
   * status code if the unlock operation succeeds, <code>400</code> if the page
   * is not found or <code>403</code> if the page is locked by a different user.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @param enddate
   *          the optional publishing end date
   * @return the page
   */
  @DELETE
  @Path("/{page}/publish")
  public Response unpublishPage(@Context HttpServletRequest request,
      @PathParam("page") String pageId,
      @HeaderParam("If-Match") String ifMatchHeader) {

    // Check the parameters
    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Extract the site
    Site site = getSite(request);

    // Make sure the content repository is writable
    if (site.getContentRepository().isReadOnly()) {
      logger.warn("Attempt to unlock a page in a read-only content repository {}", site);
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    ResourceURI liveURI = new PageURIImpl(site, null, pageId, Resource.LIVE);

    // Does the page exist?
    Page page = null;
    try {
      if (!contentRepository.existsInAnyVersion(liveURI))
        throw new WebApplicationException(Status.NOT_FOUND);
      page = (Page) contentRepository.get(liveURI);
      if (page == null)
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error lookup up page {} from repository: {}", liveURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Error unpublishing page {}: {}", liveURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    }

    // Check the value of the If-Match header against the etag
    if (ifMatchHeader != null) {
      String etag = ResourceUtils.getETagValue(page);
      if (!etag.equals(ifMatchHeader)) {
        throw new WebApplicationException(Status.PRECONDITION_FAILED);
      }
    }

    // Get the user
    User user = securityService.getUser();
    if (user == null)
      throw new WebApplicationException(Status.UNAUTHORIZED);

    // Make sure the user has publishing rights
    if (!SecurityUtils.userHasRole(user, SystemRole.PUBLISHER))
      throw new WebApplicationException(Status.UNAUTHORIZED);

    boolean isAdmin = SecurityUtils.userHasRole(user, SystemRole.SITEADMIN);

    // If the page is locked by a different user, refuse
    if (page.isLocked() && (!page.getLockOwner().equals(user) && !isAdmin)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    // Finally, perform the unpublish operation, including saving the current
    // live version of the page as the new work version.
    try {
      contentRepository.delete(liveURI);
      ResourceURI workURI = new ResourceURIImpl(liveURI, Resource.WORK);
      if (!contentRepository.exists(workURI)) {
        logger.debug("Creating work version of {}", workURI);
        page.setVersion(Resource.WORK);
        page.setPublished(null, null, null);
        page.setModified(user, new Date());
        contentRepository.put(page);
      }
      logger.info("Page {} has been unpublished by {}", liveURI, user);
    } catch (SecurityException e) {
      logger.warn("Tried to unpublish page {} of site '{}' without permission", liveURI, site);
      throw new WebApplicationException(Status.FORBIDDEN);
    } catch (IOException e) {
      logger.warn("Error removing writing unpublished page {} to repository", liveURI);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IllegalStateException e) {
      logger.warn("Error unpublishing page {}: {}", liveURI, e.getMessage());
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    } catch (ContentRepositoryException e) {
      logger.warn("Error unpublishing page {}: {}", liveURI, e.getMessage());
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Create the response
    ResponseBuilder response = Response.ok();
    response.tag(ResourceUtils.getETagValue(page));
    response.lastModified(ResourceUtils.getModificationDate(page));
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
      docs = PagesEndpointDocs.createDocumentation(servicePath);
    }
    return docs;
  }

  /**
   * Callback from OSGi to set the security service.
   * 
   * @param securityService
   *          the security service
   */
  void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }

  /**
   * Loads the pages from the site's content repository.
   * 
   * @param q
   *          the search query
   * @param details
   *          whether to display detailed information or just the header
   * @return the files
   * @throws WebApplicationException
   *           if the content repository is unavailable or if the content can't
   *           be loaded
   */
  private String loadResultSet(SearchQuery q, boolean details)
      throws WebApplicationException {
    ContentRepository repository = getContentRepository(q.getSite(), false);
    if (repository == null)
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

    SearchResult result = null;
    try {
      result = repository.find(q);
    } catch (ContentRepositoryException e) {
      throw new WebApplicationException();
    }

    StringBuffer buf = new StringBuffer("<pages ");
    buf.append("hits=\"").append(result.getHitCount()).append("\" ");
    buf.append("offset=\"").append(result.getOffset()).append("\" ");
    if (q.getLimit() > 0)
      buf.append("limit=\"").append(result.getLimit()).append("\" ");
    buf.append("page=\"").append(result.getPage()).append("\" ");
    buf.append("pagesize=\"").append(result.getPageSize()).append("\"");
    buf.append(">");
    for (SearchResultItem item : result.getItems()) {
      String xml = null;
      if (details)
        xml = ((PageSearchResultItemImpl) item).getResourceXml();
      else
        xml = ((PageSearchResultItemImpl) item).getPageHeaderXml();
      buf.append(xml);
    }
    buf.append("</pages>");

    return buf.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Pages rest endpoint";
  }

}
