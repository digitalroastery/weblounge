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

import static ch.entwine.weblounge.common.impl.util.doc.Status.badRequest;
import static ch.entwine.weblounge.common.impl.util.doc.Status.error;
import static ch.entwine.weblounge.common.impl.util.doc.Status.ok;
import static ch.entwine.weblounge.common.impl.util.doc.Status.serviceUnavailable;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.entwine.weblounge.common.impl.util.doc.Format;
import ch.entwine.weblounge.common.impl.util.doc.Parameter;
import ch.entwine.weblounge.common.impl.util.doc.TestForm;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.site.SiteManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

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
   * @param limit
   *          search result limit
   * @param offset
   *          search result offset (for paging in combination with limit)
   * @return the search result
   */
  @GET
  @Path("/{searchterms:.*}")
  public Response getPage(@Context HttpServletRequest request,
      @PathParam("searchterms") String terms,
      @QueryParam("offset") @DefaultValue("-1") int offset,
      @QueryParam("limit") @DefaultValue("-1") int limit) {

    // Check the search terms
    if (StringUtils.isBlank(terms))
      return Response.status(Status.BAD_REQUEST).build();

    // Find the site
    URL url = UrlUtils.toURL(request, false, false);
    Site site = sites.findSiteByURL(url);
    if (site == null) {
      return Response.status(Status.NOT_FOUND).build();
    } else if (!site.isStarted()) {
      return Response.status(Status.SERVICE_UNAVAILABLE).build();
    }

    // Load the content repository
    ContentRepository repository = site.getContentRepository();
    if (repository == null) {
      return Response.status(Status.SERVICE_UNAVAILABLE).build();
    }

    // Create the search expression and the query
    SearchQuery query = new SearchQueryImpl(site);
    try {
      query.withFulltext(true, URLDecoder.decode(terms, "utf-8"));
      query.withVersion(Resource.LIVE);
      query.withOffset(offset);
      query.withLimit(limit);
      // TODO: Filter out pages that can't be accessed due to security constraints
    } catch (UnsupportedEncodingException e) {
      throw new WebApplicationException(e);
    }

    // Return the result
    try {
      SearchResult result = repository.find(query);
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

    String endpointUrl = "/system/weblounge/search";
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "search");
    docs.setTitle("Weblounge Search");

    // GET /{searchterms:*}
    Endpoint searchEndpoint = new Endpoint("/{searchterms}", Method.GET, "search");
    searchEndpoint.setDescription("Returns the search result");
    searchEndpoint.addFormat(Format.xml());
    searchEndpoint.addStatus(ok("the search query was executed and the result is returned as part of the response"));
    searchEndpoint.addStatus(badRequest("no search terms have been specified"));
    searchEndpoint.addStatus(error("executing the query resulted in an error"));
    searchEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    searchEndpoint.addPathParameter(new Parameter("searchterms", Parameter.Type.String, "The search terms"));
    searchEndpoint.addOptionalParameter(new Parameter("offset", Parameter.Type.String, "Offset within the result set", "-1"));
    searchEndpoint.addOptionalParameter(new Parameter("limit", Parameter.Type.String, "Number of result items to include", "-1"));
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
    return "Search rest endpoint";
  }

}
