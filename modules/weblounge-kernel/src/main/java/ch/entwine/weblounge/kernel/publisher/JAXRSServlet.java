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

package ch.entwine.weblounge.kernel.publisher;

import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteURL;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.json.JSONException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This custom implementation of the <code>CXFNonSpringJaxrsServlet</code> was
 * done following the <a href=
 * "http://cxf.apache.org/docs/jax-rs.html#JAX-RS-WithCXFNonSpringJaxrsServlet"
 * >cxf documentation</a>.
 */
public class JAXRSServlet extends CXFNonSpringJaxrsServlet {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(JAXRSServlet.class);

  /** The serial version uid */
  private static final long serialVersionUID = 7336130764437993613L;

  /** The servlet address */
  private String address = null;

  /** The wrapped endpoint */
  private Object service = null;

  /** The bundle that defined this servlet */
  private Bundle bundle = null;

  /** The associated site (if not a global endpoint) */
  private Site site = null;

  /** The environment */
  private Environment environment = null;

  /**
   * Creates a new servlet that maps <code>service</code> to the given address.
   * 
   * @param address
   *          the servlet address
   * @param service
   *          the service implementation
   * @param bundle
   *          the bundle that defined this service
   */
  JAXRSServlet(String address, Object service, Bundle bundle) {
    if (address == null)
      throw new IllegalArgumentException("Address can't be null");
    if (service == null)
      throw new IllegalArgumentException("Service implementation can't be null");
    if (bundle == null)
      throw new IllegalArgumentException("Defining bundle can't be null");
    this.address = address;
    this.service = service;
    this.bundle = bundle;
  }

  @Override
  protected void handleRequest(HttpServletRequest request,
      HttpServletResponse response) throws ServletException {

    // If a site registered this endpoint, other sites should not be able to
    // access it
    if (site != null) {
      String requestPath = request.getRequestURL().toString();
      SiteURL siteUrl = null;
      logger.trace("Making sure the endpoint should be accessible through site '{}'", site);
      for (SiteURL url : site.getHostnames()) {
        if (environment != null && !environment.equals(url.getEnvironment()))
          continue;
        if (requestPath.startsWith(url.toExternalForm())) {
          siteUrl = url;
          break;
        }
      }
      if (siteUrl == null || !site.isOnline()) {
        try {
          logger.debug("Request to site '{}' cannot acces {}", this);
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        } catch (IOException e) {
          logger.debug("Error sending response back to client: {}", e.getMessage());
        }
      }
      logger.trace("Access through site '{}' to {} granted", site, request.getRequestURI());
    }

    if (requiresJson(request)) {
      JSONResponseWrapper wrappedResponse = new JSONResponseWrapper(response);
      super.handleRequest(request, wrappedResponse);
      try {
        wrappedResponse.finishResponse();
      } catch (IOException e) {
        logger.error("Writing json to response failed: " + e.getMessage());
        throw new ServletException(e);
      } catch (JSONException e) {
        logger.error("Conversion to json failed: " + e.getMessage());
        throw new ServletException(e);
      }
    } else {
      super.handleRequest(request, response);
    }
  }

  /**
   * Returns <code>true</code> if the request is asking for a <code>json</code>
   * response.
   * 
   * @param request
   *          the request
   * @return <code>true</code> if a json response is requested
   */
  private boolean requiresJson(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String parameter = request.getParameter("json");
    String accepts = request.getHeader("Accept");
    if (uri.endsWith(".json") || uri.endsWith("/json"))
      return true;
    if (parameter != null)
      return true;
    if (accepts != null && ("application/json".equals(accepts) || "text/json".equals(accepts)))
      return true;
    return false;
  }

  /**
   * Returns the service that is being wrapped by this servlet.
   * 
   * @return the service implementation
   */
  public Object getService() {
    return service;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.cxf.transport.servlet.AbstractHTTPServlet#service(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse)
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse res)
      throws ServletException, IOException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet#loadBus(javax.servlet.ServletConfig)
   */
  @Override
  public void loadBus(ServletConfig servletConfig) throws ServletException {
    super.loadBus(servletConfig);

    ClassLoader bundleClassLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader delegateClassLoader = JAXRSServerFactoryBean.class.getClassLoader();

    try {

      // The JAXRSServerFactoryBean needs access to resources on it's own
      // bundle classpath (META-INF/cxf.xml, META-INF/cxf/osgi.xml). Therefore
      // we need to adjust the context class loader
      Thread.currentThread().setContextClassLoader(delegateClassLoader);

      // Create the endpoint
      JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
      sf.setAddress(address);
      sf.setResourceClasses(service.getClass());
      sf.setResourceProvider(service.getClass(), new SingletonResourceProvider(service));
      sf.create();

    } finally {
      Thread.currentThread().setContextClassLoader(bundleClassLoader);
    }

  }

  /**
   * {@inheritDoc}
   * 
   * Note that this method is intentionally <i>not</i> calling its super
   * implementation, since this will throw an exception because no service
   * classes have been registered previously.
   * 
   * @see org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet#getServiceClasses(javax.servlet.ServletConfig,
   *      boolean)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected List<Class> getServiceClasses(ServletConfig servletConfig,
      boolean modelAvailable) throws ServletException {
    List<Class> classes = new ArrayList<Class>();
    classes.add(service.getClass());
    return classes;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet#getResourceProviders(javax.servlet.ServletConfig,
   *      java.util.List)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Map<Class, ResourceProvider> getResourceProviders(
      ServletConfig servletConfig, List<Class> resourceClasses)
      throws ServletException {
    Map<Class, ResourceProvider> providers = super.getResourceProviders(servletConfig, resourceClasses);
    providers.put(service.getClass(), new SingletonResourceProvider(service));
    return providers;
  }

  /**
   * Returns the bundle that defined this endpoint.
   * 
   * @return the bundle
   */
  Bundle getBundle() {
    return bundle;
  }

  /**
   * Sets the site that is associated with this REST endpoint.
   * 
   * @param site
   *          the site
   */
  void setSite(Site site) {
    this.site = site;
  }

  /**
   * Sets the current environment.
   * 
   * @param environment
   *          the environment
   */
  void setEnvironment(Environment environment) {
    this.environment = environment;
  }

}
