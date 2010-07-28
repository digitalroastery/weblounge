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

import ch.o2it.weblounge.common.content.Composer;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This class implements the <code>REST</code> endpoint for page data.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class PageEndpoint {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PageEndpoint.class);

  /** The sites that are online */
  private transient SiteManager sites = null;

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
  @Path("/{pageid}")
  public Response getPage(@Context HttpServletRequest request,
      @PathParam("pageid") String pageId) {

    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    try {
      Page page = loadPage(request, pageId);
      if (page == null) {
        return Response.status(Status.NOT_FOUND).build();
      }

      // TODO: Check page for modification date and return NOT_MODIFIED

      return Response.ok(page.toXml()).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
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
  @Path("/{pageid}/composers/{composerid}")
  public Response getComposer(@Context HttpServletRequest request,
      @PathParam("pageid") String pageId,
      @PathParam("composerid") String composerId) {

    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();
    else if (composerId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    try {
      Page page = loadPage(request, pageId);
      if (page == null) {
        return Response.status(Status.NOT_FOUND).build();
      }

      // TODO: Check page for modification date and return NOT_MODIFIED

      // Load the composer
      Composer composer = page.getComposer(composerId);
      if (composer == null) {
        return Response.status(Status.NOT_FOUND).build();
      }

      return Response.ok(composer.toXml()).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
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
  @Path("/{pageid}/composers/{composerid}/pagelets/{pageletindex}")
  public Response getPagelet(@Context HttpServletRequest request,
      @PathParam("pageid") String pageId,
      @PathParam("composerid") String composerId,
      @PathParam("pageletindex") int pageletIndex) {

    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();
    else if (composerId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    try {
      Page page = loadPage(request, pageId);
      if (page == null) {
        return Response.status(Status.NOT_FOUND).build();
      }

      // TODO: Check page for modification date and return NOT_MODIFIED

      // Load the composer
      Composer composer = page.getComposer(composerId);
      if (composer == null) {
        return Response.status(Status.NOT_FOUND).build();
      }

      if (composer.size() < pageletIndex) {
        return Response.status(Status.NOT_FOUND).build();
      }

      return Response.ok(composer.getPagelet(pageletIndex).toXml()).build();
    } catch (Exception e) {
      return Response.serverError().build();
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

    String endpointUrl = "/system/contentrepository/pages";
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "pages");
    docs.setTitle("Weblounge Pages");

    // GET /{pageid}
    Endpoint pageEndpoint = new Endpoint("/{pageid}", Method.GET, "page");
    pageEndpoint.setDescription("Returns the page with the given id");
    pageEndpoint.addFormat(Format.xml());
    pageEndpoint.addStatus(OK("the page was found and is returned as part of the response"));
    pageEndpoint.addStatus(NOT_FOUND("the page was not found or could not be loaded"));
    pageEndpoint.addStatus(BAD_REQUEST("an invalid page identifier was received"));
    pageEndpoint.addPathParameter(new Parameter("pageid", Parameter.Type.STRING, "The page identifier"));
    pageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, pageEndpoint);

    // GET /{pageid}/composers/{composerId}
    Endpoint composerEndpoint = new Endpoint("/{pageid}/composers/{composerid}", Method.GET, "composer");
    composerEndpoint.setDescription("Returns the composer with the given id from the indicated page");
    composerEndpoint.addFormat(Format.xml());
    composerEndpoint.addStatus(OK("the composer was found and is returned as part of the response"));
    composerEndpoint.addStatus(NOT_FOUND("the composer was not found or could not be loaded"));
    composerEndpoint.addStatus(BAD_REQUEST("an invalid page or composer identifier was received"));
    composerEndpoint.addPathParameter(new Parameter("pageid", Parameter.Type.STRING, "The page identifier"));
    composerEndpoint.addPathParameter(new Parameter("composerid", Parameter.Type.STRING, "The composer identifier"));
    composerEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, composerEndpoint);

    // GET /{pageid}/composers/{composerId}/pagelets/{pageletIndex}

    this.docs = EndpointDocumentationGenerator.generate(docs);
    return this.docs;
  }

  /**
   * Returns the page identified by the given request and page identifier or
   * <code>null</code> if either one of the site, the site's content repository
   * or the page itself is not available.
   * 
   * @param request
   *          the servlet request
   * @param pageId
   *          the page identifier
   * @return the page
   * @throws ContentRepositoryException
   *           if accessing the content repository fails
   */
  protected Page loadPage(HttpServletRequest request, String pageId)
      throws ContentRepositoryException {

    if (sites == null) {
      logger.debug("Unable to load page '{}': no sites registered", pageId);
      return null;
    }

    // Extract the site
    Site site = sites.findSiteByName(request.getServerName());
    if (site == null) {
      logger.debug("Unable to load page '{}': site not found", pageId);
      return null;
    }

    // Look for the content repository
    ContentRepository contentRepository = ContentRepositoryFactory.getRepository(site);
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return null;
    }

    PageURI pageURI = new PageURIImpl(site, null, pageId);
    Page page = contentRepository.getPage(pageURI);
    return page;
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
    return "pages rest endpoint";
  }

}
