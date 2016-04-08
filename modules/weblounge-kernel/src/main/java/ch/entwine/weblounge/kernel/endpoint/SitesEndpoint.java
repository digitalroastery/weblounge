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

package ch.entwine.weblounge.kernel.endpoint;

import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteException;
import ch.entwine.weblounge.kernel.site.SiteManager;

import org.apache.commons.lang3.StringUtils;

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

  /** The request environment */
  protected Environment environment = Environment.Production;

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
      siteXml = siteXml.replaceAll("<domains.*</domains>", "");
      siteXml = siteXml.replaceAll("<languages.*</languages>", "");
      siteXml = siteXml.replaceAll("<options.*</options>", "");
      siteXml = siteXml.replaceAll("<security.*</security>", "");
      siteXml = siteXml.replaceAll("<templates.*</templates>", "");
      siteXml = siteXml.replaceAll("( xmlns.*?>)", ">");
      buf.append(siteXml);
    }
    buf.append("</sites>");
    ResponseBuilder response = Response.ok(buf.toString());
    return response.build();
  }

  /**
   * Returns the site with the given identifier or a <code>404</code> if the
   * site could not be found.
   * 
   * @param request
   *          the request
   * @param siteId
   *          the site identifier
   * @return the site
   */
  @GET
  @Path("/{site}")
  public Response getSite(@Context HttpServletRequest request,
      @PathParam("site") String siteId) {

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
    siteXml = siteXml.replaceAll("<password.*</password>", "");
    siteXml = siteXml.replaceAll("( xmlns.*?>)", ">");
    siteXml = ConfigurationUtils.processTemplate(siteXml, site, environment);
    ResponseBuilder response = Response.ok(siteXml);
    return response.build();
  }

  /**
   * Updates the indicated site. If the site was not found, <code>404</code> is
   * returned.
   * 
   * @param siteId
   *          the site identifier
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
      if (!site.isStarted() && ConfigurationUtils.isEnabled(status)) {
        try {
          site.start();
        } catch (IllegalStateException e) {
          throw new WebApplicationException(Status.PRECONDITION_FAILED);
        } catch (SiteException e) {
          throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
      } else if (site.isStarted() && ConfigurationUtils.isDisabled(status)) {
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
   * Returns the modules of the site with the given identifier or a
   * <code>404</code> if the site could not be found.
   * 
   * @param siteId
   *          the site identifier
   * @return the site
   */
  @GET
  @Path("/{site}/modules")
  public Response getModules(@PathParam("site") String siteId) {

    // Check the parameters
    if (siteId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Load the site
    Site site = sites.findSiteByIdentifier(siteId);
    if (site == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    StringBuffer buf = new StringBuffer();
    buf.append("<modules>");
    for (Module m : site.getModules()) {
      String moduleXml = m.toXml();
      moduleXml = moduleXml.replaceAll("<actions.*</actions>", "");
      moduleXml = moduleXml.replaceAll("<jobs.*</jobs>", "");
      moduleXml = moduleXml.replaceAll("<imagestyles.*</imagestyles>", "");
      moduleXml = moduleXml.replaceAll("<options.*</options>", "");
      moduleXml = moduleXml.replaceAll("<pagelets.*</pagelets>", "");
      moduleXml = moduleXml.replaceAll("( xmlns.*?>)", ">");
      buf.append(moduleXml);
    }
    buf.append("</modules>");
    ResponseBuilder response = Response.ok(buf.toString());
    return response.build();
  }

  /**
   * Returns the modules of the site with the given identifier or a
   * <code>404</code> if the site could not be found.
   * 
   * @param request
   *          the request
   * @param siteId
   *          the site identifier
   * @return the site
   */
  @GET
  @Path("/{site}/modules/{module}")
  public Response getModules(@Context HttpServletRequest request,
      @PathParam("site") String siteId, @PathParam("module") String moduleId) {

    // Check the parameters
    if (siteId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Check the parameters
    if (moduleId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Load the site
    Site site = sites.findSiteByIdentifier(siteId);
    if (site == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    Module m = site.getModule(moduleId);
    if (m == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    // Create the response
    String moduleXml = m.toXml();
    moduleXml = moduleXml.replaceAll("( xmlns.*?>)", ">");
    moduleXml = ConfigurationUtils.processTemplate(moduleXml, m, environment);
    ResponseBuilder response = Response.ok(moduleXml);
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
   * Callback from the OSGi environment when the environment becomes published.
   * 
   * @param environment
   *          the environment
   */
  void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Sites rest endpoint";
  }

}
