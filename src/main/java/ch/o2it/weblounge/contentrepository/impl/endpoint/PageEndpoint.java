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

import ch.o2it.weblounge.common.content.Composer;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.dispatcher.SiteRegistrationService;

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
  private transient SiteRegistrationService sites = null;

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

  @GET
  @Path("/{pageid}/composer/{composerid}")
  public Response getComposer(@Context HttpServletRequest request,
      @PathParam("pageid") String pageId, @PathParam("composerid") String composerId) {

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

  @GET
  @Path("/{pageid}/composer/{composerid}/pagelet/{pageletindex}")
  public Response getPagelet(@Context HttpServletRequest request,
      @PathParam("pageid") String pageId, @PathParam("composerid") String composerId, @PathParam("pageletindex") int pageletIndex) {

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
    Site site = sites.findSiteByRequest(request);
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
   * Callback for OSGi to set the site locator.
   * 
   * @param siteLocator
   *          the site locator
   */
  void setSiteLocator(SiteRegistrationService siteLocator) {
    this.sites = siteLocator;
  }

  /**
   * Callback for OSGi to remove the site locator.
   * 
   * @param siteLocator
   *          the site locator
   */
  void removeSiteLocator(SiteRegistrationService siteLocator) {
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
