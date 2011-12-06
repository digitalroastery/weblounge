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
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.security.SecurityUtils;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This class implements the <code>REST</code> endpoint for page data.
 */
@Path("/")
@Produces(MediaType.APPLICATION_XML)
public class IndexEndpoint extends ContentRepositoryEndpoint {

  /** Logging facility */
  static final Logger logger = LoggerFactory.getLogger(IndexEndpoint.class);

  /** The security service */
  protected SecurityService securityService = null;

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Returns the index statistics.
   * 
   * @param request
   *          the request
   * @return a collection of matching pages
   */
  @GET
  @Path("/statistics")
  public Response getstatistics(@Context HttpServletRequest request) {

    // Make sure the user has site administrator rights
    User user = securityService.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      throw new WebApplicationException(Status.FORBIDDEN);

    Site site = getSite(request);
    ContentRepository repository = getContentRepository(site, false);
    if (repository == null)
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

    SearchQuery q = null;
    StringBuffer result = new StringBuffer();

    result.append("<index");
    result.append(" state=\"").append(repository.isIndexing() ? "indexing" : "normal").append("\"");
    result.append(" readonly=\"").append(repository.isReadOnly() ? "true" : "false").append("\"");
    result.append(">");

    result.append("<resources>").append(repository.getResourceCount()).append("</resources>");
    result.append("<revisions>").append(repository.getVersionCount() - repository.getResourceCount()).append("</revisions>");

    try {
      q = new SearchQueryImpl(site).withTypes(Page.TYPE).withPreferredVersion(Resource.LIVE);
      result.append("<pages>").append(repository.find(q).getDocumentCount()).append("</pages>");
      q = new SearchQueryImpl(site).withTypes(FileResource.TYPE).withPreferredVersion(Resource.LIVE);
      result.append("<files>").append(repository.find(q).getDocumentCount()).append("</files>");
      q = new SearchQueryImpl(site).withTypes(ImageResource.TYPE).withPreferredVersion(Resource.LIVE);
      result.append("<images>").append(repository.find(q).getDocumentCount()).append("</images>");
      q = new SearchQueryImpl(site).withTypes(MovieResource.TYPE).withPreferredVersion(Resource.LIVE);
      result.append("<movies>").append(repository.find(q).getDocumentCount()).append("</movies>");
    } catch (ContentRepositoryException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    result.append("</index>");

    return Response.ok(result.toString()).build();
  }

  /**
   * Triggers an index rebuild and returns immediately.
   * 
   * @param request
   *          the request
   */
  @DELETE
  @Path("/")
  public Response reindex(@Context HttpServletRequest request) {

    // Make sure the user has site administrator rights
    User user = securityService.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      throw new WebApplicationException(Status.FORBIDDEN);

    Site site = getSite(request);
    ContentRepository repository = getContentRepository(site, true);
    if (repository == null)
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

    // Make sure this is a writable repository
    if (!(repository instanceof WritableContentRepository))
      throw new WebApplicationException(Status.PRECONDITION_FAILED);
    final WritableContentRepository writableRepository = (WritableContentRepository) repository;

    // Is the repository already being indexed?
    if (repository.isIndexing())
      throw new WebApplicationException(Status.CONFLICT);

    // Start indexing
    new Thread(new Runnable() {
      public void run() {
        try {
          writableRepository.index();
        } catch (ContentRepositoryException e) {
          logger.error("Index operation failed: " + e.getMessage());
        }
      }
    }).start();

    // Return the response
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
  public String getDocumentation(@Context HttpServletRequest request) {
    if (docs == null) {
      String docsPath = request.getRequestURI();
      String docsPathExtension = request.getPathInfo();
      String servicePath = request.getRequestURI().substring(0, docsPath.length() - docsPathExtension.length());
      docs = IndexEndpointDocs.createDocumentation(servicePath);
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
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Index rest endpoint";
  }

}
