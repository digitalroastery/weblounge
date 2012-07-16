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

package ch.entwine.weblounge.workbench.endpoint;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.site.SiteManager;
import ch.entwine.weblounge.workbench.PageletEditor;
import ch.entwine.weblounge.workbench.WorkbenchService;
import ch.entwine.weblounge.workbench.suggest.SimpleSuggestion;
import ch.entwine.weblounge.workbench.suggest.SuggestionList;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
 * This class exposes the web methods of the workbench.
 */
@Path("/")
public class WorkbenchEndpoint {

  /** The workbench */
  protected transient WorkbenchService workbench = null;

  /** The sites that are online */
  protected transient SiteManager sites = null;

  /** The request environment */
  protected Environment environment = Environment.Production;

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Returns an editor.
   * 
   * @param request
   *          the request
   * @param imageId
   *          the resource identifier
   * @return the image
   */
  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("/edit/{page}/{composer}/{pageletindex}")
  public Response getPageletEditor(@Context HttpServletRequest request,
      @PathParam("page") String pageURI,
      @PathParam("composer") String composerId,
      @PathParam("pageletindex") int pagelet,
      @QueryParam("language") String language) {

    // Load the site
    Site site = getSite(request);
    if (site == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    // Return the editor
    ResourceURI uri = new PageURIImpl(site, null, pageURI, Resource.WORK);
    PageletEditor editor;
    try {
      editor = workbench.getEditor(site, uri, composerId, pagelet, language, environment);
    } catch (IOException e) {
      throw new WebApplicationException(e);
    }
    if (editor == null)
      throw new WebApplicationException(Status.NOT_FOUND);
    return Response.ok(editor.toXml()).build();
  }

  @POST
  @Produces(MediaType.TEXT_HTML)
  @Path("/renderer/{page}/{composer}/{pageletindex}")
  public Response getRenderer(@Context HttpServletRequest request,
      @PathParam("page") String pageURI,
      @PathParam("composer") String composerId,
      @PathParam("pageletindex") int pageletIndex,
      @QueryParam("language") String language, @FormParam("page") String pageXml) {

    // Load the site
    Site site = getSite(request);
    if (site == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    ResourceURI uri = new PageURIImpl(site, null, pageURI, Resource.WORK);
    String renderedPagelet;
    try {
      renderedPagelet = workbench.getRenderer(site, uri, composerId, pageletIndex, pageXml, language, environment);
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }

    if (renderedPagelet == null)
      throw new WebApplicationException(Status.NOT_FOUND);
    return Response.ok(renderedPagelet).build();

  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/renderer/{page}/{composer}/{pageletindex}")
  public Response getRenderer(@Context HttpServletRequest request,
      @PathParam("page") String pageURI,
      @PathParam("composer") String composerId,
      @PathParam("pageletindex") int pageletIndex,
      @QueryParam("language") String language) {

    // Load the site
    Site site = getSite(request);
    if (site == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    ResourceURI uri = new PageURIImpl(site, null, pageURI, Resource.WORK);
    String renderedPagelet;
    try {
      renderedPagelet = workbench.getRenderer(site, uri, composerId, pageletIndex, language, environment);
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }

    if (renderedPagelet == null)
      throw new WebApplicationException(Status.NOT_FOUND);
    return Response.ok(renderedPagelet).build();
  }

  /**
   * Returns a list of suggested subjects based on an initial hint. The number
   * of suggestions returned can be specified using the <code>limit</code>
   * parameter.
   * 
   * @return the endpoint documentation
   */
  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("/suggest/subjects/{seed}")
  public Response suggestSubjects(@Context HttpServletRequest request,
      @PathParam("seed") String seed,
      @QueryParam("highlight") String highlightTag,
      @QueryParam("limit") int limit) {
    SuggestionList<SimpleSuggestion> list = new SuggestionList<SimpleSuggestion>("subjects", seed, highlightTag);
    try {
      list.addAll(workbench.suggestTags(getSite(request), seed, limit));
      return Response.ok(list.toXml()).build();
    } catch (IllegalStateException e) {
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
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
  public String getDocumentation(@Context HttpServletRequest request) {
    if (docs == null) {
      String docsPath = request.getRequestURI();
      String docsPathExtension = request.getPathInfo();
      String servicePath = request.getRequestURI().substring(0, docsPath.length() - docsPathExtension.length());
      docs = WorkbenchEndpointDocs.createDocumentation(servicePath);
    }
    return docs;
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
    Site site = sites.findSiteByURL(UrlUtils.toURL(request, false, false));
    if (site == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    } else if (!site.isOnline()) {
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }
    return site;
  }

  /**
   * Callback for OSGi to set the workbench.
   * 
   * @param workbench
   *          the workbench implementation
   */
  void setWorkbench(WorkbenchService workbench) {
    this.workbench = workbench;
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
    return "Workbench rest endpoint";
  }

}
