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

package ch.entwine.weblounge.cache.impl.endpoint;

import ch.entwine.weblounge.cache.impl.CacheConfiguration;
import ch.entwine.weblounge.cache.impl.CacheConfigurationFactory;
import ch.entwine.weblounge.cache.impl.CacheServiceImpl;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.site.SiteManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Dictionary;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

/**
 * This class implements the <code>REST</code> endpoint for page data.
 */
@Path("/")
@Produces(MediaType.APPLICATION_XML)
public class CacheEndpoint {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(CacheEndpoint.class);

  /** The endpoint documentation */
  private String docs = null;

  /** The cache configuration factory */
  private CacheConfigurationFactory configFactory = null;

  /** The sites that are online */
  protected transient SiteManager sites = null;

  /**
   * Returns the statistics for the site cache.
   * 
   * @param request
   *          the request
   * @return the statistics
   */
  @GET
  @Path("/")
  public Response getStatistics(@Context HttpServletRequest request) {

    // Get the site's cache
    Site site = getSite(request);
    CacheConfiguration cache = getCache(site);

    StringBuilder stats = new StringBuilder();
    stats.append("<cache id=\"").append(cache.getIdentifier()).append("\">");

    // Status
    stats.append("<enabled>").append(cache.isEnabled()).append("</enabled>");

    stats.append("</cache>");

    Response response = Response.ok(stats.toString()).build();
    return response;
  }

  /**
   * Enables the cache for the site determined by the request.
   * 
   * @param request
   *          the request
   */
  @PUT
  @Path("/")
  public Response startCache(@Context HttpServletRequest request) {

    // Get the site's cache
    Site site = getSite(request);
    CacheConfiguration cache = getCache(site);

    // Is the cache already enabled?
    if (cache.isEnabled()) {
      ResponseBuilder response = Response.notModified();
      return response.build();
    }

    // Enable the cache
    try {
      configFactory.enable(cache);
    } catch (Throwable t) {
      throw new WebApplicationException();
    }

    // Send the response
    ResponseBuilder response = Response.ok();
    return response.build();
  }

  /**
   * Disables the cache for the site determined by the request.
   * 
   * @param request
   *          the request
   */
  @DELETE
  @Path("/")
  public Response stopCache(@Context HttpServletRequest request) {

    // Extract the site
    Site site = getSite(request);
    if (site == null)
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

    // Get the site's cache
    CacheConfiguration cache = getCache(site);
    if (cache == null)
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

    // Is the cache already disabled?
    if (!cache.isEnabled()) {
      ResponseBuilder response = Response.notModified();
      return response.build();
    }

    // Disable the cache
    try {
      configFactory.disable(cache);
    } catch (Throwable t) {
      throw new WebApplicationException();
    }

    // Send the response
    ResponseBuilder response = Response.ok();
    return response.build();
  }

  /**
   * Disables the cache for the site determined by the request.
   * 
   * @param request
   *          the request
   */
  @DELETE
  @Path("/content")
  public Response clear(@Context HttpServletRequest request) {

    // Extract the site
    Site site = getSite(request);
    if (site == null)
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

    // Get the site's cache
    CacheConfiguration cache = getCache(site);
    if (cache == null)
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

    // Clear the cache
    try {
      Dictionary<String, Object> properties = cache.getProperties();
      properties.put(CacheServiceImpl.OPT_CLEAR, "true");

      // Tell the configuration admin service to update the service
      cache.getConfiguration().update(properties);
      properties.remove(CacheServiceImpl.OPT_CLEAR);
      cache.getConfiguration().update(properties);
    } catch (Throwable t) {
      logger.error("Error updating cache '{}': {}", site.getIdentifier(), t.getMessage());
      throw new WebApplicationException();
    }

    // Send the response
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
      docs = CacheEndpointDocs.createDocumentation(servicePath);
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
    URL url = UrlUtils.toURL(request, false, false);
    Site site = sites.findSiteByURL(url);
    if (site == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    } else if (!site.isStarted()) {
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }
    return site;
  }

  /**
   * Returns the configuration of the cache of the given site or
   * <code>null</code> if no such cache was registered.
   * 
   * @param id
   *          the cache identifier
   * @return the cache configuration
   * @throws WebApplicationException
   *           if the cache is not available
   */
  private CacheConfiguration getCache(Site site) throws WebApplicationException {
    CacheConfiguration config = configFactory.getConfiguration(site.getIdentifier());
    if (config == null)
      throw new WebApplicationException(Status.NOT_FOUND);
    return config;
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
   * Callback from the OSGi declarative services environment that will pass in a
   * reference to the cache configuration factory.
   * 
   * @param factory
   *          the configuration factory
   */
  synchronized void setCacheConfigurationFactory(
      CacheConfigurationFactory factory) {
    configFactory = factory;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Cache rest endpoint";
  }

}
