/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.kernel.endpoint;

import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider;
import ch.entwine.weblounge.kernel.site.SiteManager;

import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This service provides runtime information to users of the system, such as
 * <ul>
 * <li>the<code>REST</code> services available for consumption</li>
 * <li>the current user</li>
 * <li>the current site</li>
 * <li>mountpoint of shared resources</li>
 * <li>mountpoint of the ui</li>
 * </ul>
 * <p>
 * In addition, the service offers a plugin architecture which allows other
 * system components to expose runtime information through this service.
 */
@Path("/")
public class RuntimeInformationEndpoint {

  /** The sites that are online */
  protected transient SiteManager sites = null;

  /** List of runtime information provider */
  protected Map<String, RuntimeInformationProvider> runtimeInfoProviders = null;

  /** The request environment */
  protected Environment environment = Environment.Production;

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Creates a new instance of the runtime information endpoint.
   */
  public RuntimeInformationEndpoint() {
    runtimeInfoProviders = new ConcurrentHashMap<String, RuntimeInformationProvider>();
  }

  /**
   * Returns the runtime information.
   * 
   * @return the runtime information
   */
  @GET
  @Path("/")
  @Produces(MediaType.TEXT_XML)
  public Response getRuntimeInformation(@Context HttpServletRequest request) {
    StringBuffer xml = new StringBuffer();
    xml.append("<runtime>");

    if (sites != null) {
      Site site = getSite(request);
      User user = SecurityUtils.getUser();
      Language language = LanguageUtils.getPreferredLanguage(request, site);

      for (Map.Entry<String, RuntimeInformationProvider> entry : runtimeInfoProviders.entrySet()) {
        String component = entry.getKey();
        RuntimeInformationProvider provider = entry.getValue();
        String runtimeInformation = provider.getRuntimeInformation(site, user, language, environment);
        if (StringUtils.isNotBlank(runtimeInformation)) {
          if (StringUtils.isNotBlank(component))
            xml.append("<").append(component).append(">");
          xml.append(runtimeInformation);
          if (StringUtils.isNotBlank(component))
            xml.append("</").append(component).append(">");
        }
      }
    }

    xml.append("</runtime>");

    return Response.ok(xml.toString()).build();
  }

  /**
   * Returns the runtime information for a given component.
   * 
   * @return the runtime information
   */
  @GET
  @Path("/{component}")
  @Produces(MediaType.TEXT_XML)
  public Response getRuntimeInformationComponent(
      @Context HttpServletRequest request,
      @PathParam("component") String component) {
    StringBuffer xml = new StringBuffer();
    xml.append("<runtime>");

    if (sites != null) {
      Site site = getSite(request);
      User user = SecurityUtils.getExtendedUser();
      Language language = LanguageUtils.getPreferredLanguage(request, site);
      RuntimeInformationProvider provider = runtimeInfoProviders.get(component);
      if (provider == null)
        throw new WebApplicationException(Status.NOT_FOUND);

      String runtimeInformation = provider.getRuntimeInformation(site, user, language, environment);
      if (StringUtils.isNotBlank(runtimeInformation)) {
        xml.append("<").append(component).append(">");
        xml.append(runtimeInformation);
        xml.append("</").append(component).append(">");
      } else {
        xml.append("<").append(component).append("/>");
      }
    }

    xml.append("</runtime>");

    return Response.ok(xml.toString()).build();
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
      docs = RuntimeInformationEndpointDocs.createDocumentation(servicePath);
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
    } else if (!site.isOnline()) {
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }
    return site;
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
   * Callback for OSGi to add a runtime information provider.
   * 
   * @param provider
   *          the runtime information provider
   */
  void addRuntimeInformationProvider(RuntimeInformationProvider provider) {
    String component = provider.getComponentId();
    if (StringUtils.isBlank(component))
      throw new IllegalStateException("Runtime component identifier of " + provider + " is null");
    runtimeInfoProviders.put(component, provider);
  }

  /**
   * Callback for OSGi to remove a runtime information provider.
   * 
   * @param provider
   *          the runtime information provider
   */
  void removeRuntimeInformationProvider(RuntimeInformationProvider provider) {
    runtimeInfoProviders.remove(provider);
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
    return "Runtime information rest endpoint";
  }

}
