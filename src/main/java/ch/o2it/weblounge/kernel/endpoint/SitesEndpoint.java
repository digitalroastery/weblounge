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

package ch.o2it.weblounge.kernel.endpoint;

import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteException;
import ch.o2it.weblounge.kernel.SiteManager;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

/**
 * This class implements the <code>REST</code> endpoint for site data.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class SitesEndpoint {

  /** The sites that are online */
  protected transient SiteManager sites = null;

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Returns all sites.
   * 
   * @return the sites
   */
  @GET
  @Path("/")
  public Response getSites() {
    StringBuffer buf = new StringBuffer();
    buf.append("<sites>");
    Iterator<Site> si = sites.sites();
    while (si.hasNext()) {
      String siteXml = si.next().toXml();
      siteXml = siteXml.replaceAll("( xmlns.*?>)", ">");
      buf.append(siteXml);
    }
    buf.append("</sites>");
    ResponseBuilder response = Response.ok(buf.toString());
    return response.build();
  }

  /**
   * Returns the site with the given identifier or a <code>404</code> if the
   * page could not be found.
   * 
   * @param siteId
   *          the site identifier
   * @return the site
   */
  @GET
  @Path("/{site}")
  public Response getSite(@PathParam("site") String siteId) {

    // Check the parameters
    if (siteId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Load the site
    Site site = sites.findSiteByIdentifier(siteId);
    if (site == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Create the response
    String siteXml = site.toXml();
    siteXml = siteXml.replaceAll("( xmlns.*?>)", ">");
    ResponseBuilder response = Response.ok(siteXml);
    return response.build();
  }

  /**
   * Updates the indicated site. If the site was not found, <code>404</code> is
   * returned.
   * 
   * @param siteId
   *          the page identifier
   * @param siteXml
   *          the updated site
   * @return response an empty response
   * @throws WebApplicationException
   *           if the update fails
   */
  @PUT
  @Path("/{site}")
  public Response updateSite(@PathParam("site") String siteId,
      @FormParam("status") String status) {

    // Check the parameters
    if (siteId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Extract the site
    Site site = sites.findSiteByIdentifier(siteId);
    if (site == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Process changes in site
    if (StringUtils.isNotBlank(status)) {
      if (!site.isRunning() && ConfigurationUtils.isEnabled(status)) {
        try {
          site.start();
        } catch (IllegalStateException e) {
          throw new WebApplicationException(Status.PRECONDITION_FAILED);
        } catch (SiteException e) {
          throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
      } else if (site.isRunning() && ConfigurationUtils.isDisabled(status)) {
        site.stop();
      } else {
        throw new WebApplicationException(Status.BAD_REQUEST);
      }
    }
    
    // Create the response
    ResponseBuilder response = Response.ok();
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
      docs = SitesEndpointDocs.createDocumentation(servicePath);
    }
    return docs;
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
    return "sites rest endpoint";
  }

}
