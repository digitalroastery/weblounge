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

import static ch.o2it.weblounge.common.impl.util.doc.Status.BAD_REQUEST;
import static ch.o2it.weblounge.common.impl.util.doc.Status.NOT_FOUND;
import static ch.o2it.weblounge.common.impl.util.doc.Status.OK;
import static ch.o2it.weblounge.common.impl.util.doc.Status.SERVICE_UNAVAILABLE;
import static ch.o2it.weblounge.common.impl.util.doc.Status.ERROR;

import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.impl.util.doc.Endpoint;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.o2it.weblounge.common.impl.util.doc.Format;
import ch.o2it.weblounge.common.impl.util.doc.Parameter;
import ch.o2it.weblounge.common.impl.util.doc.TestForm;
import ch.o2it.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.kernel.SiteManager;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This class implements the <code>REST</code> endpoint for the search service.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class SearchEndpoint {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SearchEndpoint.class);

  /** The sites that are online */
  private transient SiteManager sites = null;

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Executes a search for the given search terms and returns the corresponding
   * search result.
   * 
   * @param request
   *          the request
   * @param searchterms
   *          the search terms
   * @return the search result
   */
  @GET
  @Path("/{searchterms:.*}")
  public Response getPage(
      @Context HttpServletRequest request,
      @PathParam("searchterms") List<String> terms,
      @QueryParam("offset") @DefaultValue("-1") int offset,
      @QueryParam("limit") @DefaultValue("-1") int limit
    ) {

    // Check the search terms
    if (terms == null || terms.size() == 0)
      return Response.status(Status.BAD_REQUEST).build();

    // Find the site
    Site site = sites.findSiteByName(request.getServerName());
    if (site == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    
    // Load the content repository
    ContentRepository repository = ContentRepositoryFactory.getRepository(site);
    if (repository == null) {
      return Response.status(Status.SERVICE_UNAVAILABLE).build();
    }
    
    // Create the search expression and the query
    SearchQuery query = new SearchQueryImpl(site);
    query.withText(StringUtils.join(terms, " "));
    query.withOffset(offset);
    query.withLimit(limit);

    // Return the result
    try {
      SearchResult result = repository.findPages(query);
      return Response.ok(result.toXml()).build();
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to access the content repository", e);
      throw new WebApplicationException(e);
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
  public String getDocumentation() {
    if (docs != null)
      return docs;

    String endpointUrl = "/system/contentrepository/search";
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "search");
    docs.setTitle("Weblounge Search");

    // GET /{searchterms:*}
    Endpoint searchEndpoint = new Endpoint("/{searchterms}", Method.GET, "search");
    searchEndpoint.setDescription("Returns the search result");
    searchEndpoint.addFormat(Format.xml());
    searchEndpoint.addStatus(OK("the search query was executed and the result is returned as part of the response"));
    searchEndpoint.addStatus(BAD_REQUEST("no search terms have been specified"));
    searchEndpoint.addStatus(ERROR("executing the query resulted in an error"));
    searchEndpoint.addStatus(SERVICE_UNAVAILABLE("the content repository for the site is temporarily offline"));
    searchEndpoint.addPathParameter(new Parameter("searchterms", Parameter.Type.STRING, "The search terms"));
    searchEndpoint.addOptionalParameter(new Parameter("offset", Parameter.Type.STRING, "Offset within the result set", "-1"));
    searchEndpoint.addOptionalParameter(new Parameter("limit", Parameter.Type.STRING, "Number of result items to include", "-1"));
    searchEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, searchEndpoint);

    this.docs = EndpointDocumentationGenerator.generate(docs);
    return this.docs;
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
    return "search rest endpoint";
  }

}
