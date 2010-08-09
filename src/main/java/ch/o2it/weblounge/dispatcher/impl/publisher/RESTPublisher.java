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

package ch.o2it.weblounge.dispatcher.impl.publisher;

import ch.o2it.weblounge.common.impl.url.UrlSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.GenericServlet;
import javax.ws.rs.Path;

/**
 * Listens for JAX-RS annotated services and publishes them to the global URL
 * space using a single shared HttpContext.
 */
public class RESTPublisher implements ManagedService {

  /** Logging facility */
  static final Logger logger = LoggerFactory.getLogger(RESTPublisher.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.o2it.weblounge.restpublisher";

  /** Configuration key prefix for rest publisher configuration */
  public static final String OPT_PREFIX = "restpublisher";

  /** Configuration key for the rest services path prefix */
  public static final String OPT_PATH = OPT_PREFIX + ".path";

  /** Configuration key used to configure the endpoint's alias */
  public static final String DEFAULT_PATH = "/system";

  /** The context path option used to override the default context path */
  public static final String OPT_CONTEXTPATH = "rest.path";

  /** The OSGi http service */
  protected HttpService httpService = null;

  /** The default http context */
  protected HttpContext httpContext = null;

  /** Context of this component */
  protected ComponentContext componentContext = null;

  /** The JSR 311 service listener */
  protected ServiceListener jsr311ServiceListener = null;

  /** The mountpoint for REST services */
  protected String defaultContextPathPrefix = DEFAULT_PATH;

  /** Mapping of registered endpoints */
  protected Map<String, GenericServlet> servletMap = null;

  /**
   * Creates a new publishing service for JSR 311 annotated classes.
   */
  public RESTPublisher() {
    servletMap = new ConcurrentHashMap<String, GenericServlet>();
    jsr311ServiceListener = new JSR311AnnotatedServiceListener();
  }

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component activation fails
   */
  void activate(ComponentContext componentContext) throws Exception {
    this.componentContext = componentContext;
    BundleContext bundleContext = componentContext.getBundleContext();

    logger.info("Starting rest publishing service");

    // Try to get hold of the service configuration
    ServiceReference configAdminRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
    if (configAdminRef != null) {
      ConfigurationAdmin configAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminRef);
      Dictionary<?, ?> config = configAdmin.getConfiguration(SERVICE_PID).getProperties();
      if (config != null) {
        configure(config);
      } else {
        logger.debug("No customized configuration for rest publisher found");
      }
    } else {
      logger.debug("No configuration admin service found while looking for rest publisher configuration");
    }

    // Make sure we are notified in case of new services
    bundleContext.addServiceListener(jsr311ServiceListener);

    // Register any existing JAX-RS services that have already been loaded
    for (Bundle bundle : bundleContext.getBundles()) {
      ServiceReference[] refs = bundle.getRegisteredServices();
      if (refs == null)
        continue;
      for (ServiceReference ref : refs) {
        ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);
        jsr311ServiceListener.serviceChanged(event);
      }
    }

  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext componentContext) throws Exception {
    if (jsr311ServiceListener != null) {
      componentContext.getBundleContext().removeServiceListener(jsr311ServiceListener);
    }

    // Unregister the current jsr311 servlets
    for (String contextPath : servletMap.keySet()) {
      unregisterEndpoint(contextPath);
    }
    servletMap.clear();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;

    boolean changed = configure(properties);
    if (!changed)
      return;

    // Unregister all current endpoints
    for (String path : servletMap.keySet()) {
      unregisterEndpoint(path);
    }
    
    // Register any existing JAX-RS services that have already been loaded
    for (Bundle bundle : componentContext.getBundleContext().getBundles()) {
      ServiceReference[] refs = bundle.getRegisteredServices();
      if (refs == null)
        continue;
      for (ServiceReference ref : refs) {
        ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);
        jsr311ServiceListener.serviceChanged(event);
      }
    }

  }

  /**
   * Configures this service using the given configuration properties.
   * 
   * @param config
   *          the service configuration
   * @throws ConfigurationException
   *           if configuration fails
   */
  private synchronized boolean configure(Dictionary<?, ?> config)
      throws ConfigurationException {

    boolean changed = false;

    // context path
    String updatedRestMountpoint = StringUtils.trim((String) config.get(OPT_PATH));
    if (updatedRestMountpoint != null) {
      if (!updatedRestMountpoint.startsWith("/"))
        throw new IllegalArgumentException("Context path (" + OPT_PATH + ") must start with a '/'");
      changed |= !updatedRestMountpoint.equals(defaultContextPathPrefix);
      defaultContextPathPrefix = updatedRestMountpoint;
    }

    return changed;
  }

  /**
   * Creates a REST endpoint for the JAX-RS annotated service.
   * 
   * @param service
   *          The jsr311 annotated service
   * @param contextPath
   *          the http context
   * @param the
   *          endpoint's path
   */
  protected void registerEndpoint(Object service, String contextPath,
      String endpointPath) {

    // Register a new servlet with the http service
    CXFNonSpringServlet servlet = new CXFNonSpringServlet();
    logger.info("Registering {} at {}", service, contextPath);
    try {
      httpService.registerServlet(contextPath, servlet, new Hashtable<String, String>(), httpContext);
      servletMap.put(contextPath, servlet);

      // Register the servlet with the JSR 311 annotated class
      Bus bus = servlet.getBus();
      JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
      factory.setBus(bus);
      factory.setServiceClass(service.getClass());
      factory.setResourceProvider(service.getClass(), new SingletonResourceProvider(service));
      factory.setAddress(endpointPath);
      ClassLoader bundleClassLoader = Thread.currentThread().getContextClassLoader();
      ClassLoader delegateClassLoader = JAXRSServerFactoryBean.class.getClassLoader();
      try {
        Thread.currentThread().setContextClassLoader(delegateClassLoader);
        factory.create();
      } finally {
        Thread.currentThread().setContextClassLoader(bundleClassLoader);
      }
    } catch (Exception e) {
      logger.error("Error registering rest service at " + contextPath, e);
      return;
    }

  }

  /**
   * Removes an endpoint from the OSGi http service.
   * 
   * @param contextPath
   *          The endpoint's url space
   */
  protected void unregisterEndpoint(String contextPath) {
    logger.info("Unregistering rest endpoint {}", contextPath);

    // Remove the servlet from the http service
    try {
      httpService.unregister(contextPath);
    } catch (Exception e) {
      logger.error("Unable to unregister rest endpoint " + contextPath, e);
    }

    // Destroy the servlet
    GenericServlet servlet = servletMap.remove(contextPath);
    if (servlet != null) {
      servlet.destroy();
    }
  }

  /**
   * OSGi callback to set a reference to the <code>HttpService</code>.
   * 
   * @param httService
   *          the http service
   */
  void setHttpService(HttpService httpService) {
    this.httpService = httpService;
    this.httpContext = httpService.createDefaultHttpContext();
  }

  /**
   * Implementation of a service listener which looks for services featuring
   * JSR311 <code>@Path</code> annotations.
   */
  class JSR311AnnotatedServiceListener implements ServiceListener {

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
     */
    public void serviceChanged(ServiceEvent event) {
      ServiceReference ref = event.getServiceReference();
      Object service = ref.getBundle().getBundleContext().getService(ref);

      // Is this a JSR 311 annotated class?
      Path pathAnnotation = service.getClass().getAnnotation(Path.class);
      if (pathAnnotation == null)
        return;

      // Is there a context path
      Object contextPathProperty = ref.getProperty(OPT_CONTEXTPATH);
      if (contextPathProperty == null)
        return;

      // Adjust relative context paths
      String contextPath = contextPathProperty.toString();
      if (!contextPath.startsWith("/")) {
        contextPath = UrlSupport.concat(defaultContextPathPrefix, contextPath);
      }

      // Process the event
      switch (event.getType()) {
        case ServiceEvent.REGISTERED:
          logger.debug("Registering JAX-RS service {} at {}", service, contextPath);
          registerEndpoint(service, contextPath, pathAnnotation.value());
          break;
        case ServiceEvent.MODIFIED:
          logger.debug("JAX-RS service {} modified", service);
          break;
        case ServiceEvent.UNREGISTERING:
          logger.debug("Unregistering JAX-RS service {} from {}", service, contextPath);
          unregisterEndpoint(contextPath);
          break;
      }
    }

  }

}
