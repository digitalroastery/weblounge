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
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.dispatcher.SharedHttpContext;
import ch.entwine.weblounge.kernel.site.SiteManager;
import ch.entwine.weblounge.kernel.site.SiteServiceListener;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.ws.rs.Path;

/**
 * Listens for JAX-RS annotated services and publishes them to the global URL
 * space using a single shared HttpContext.
 */
public class EndpointPublishingService implements ManagedService, SiteServiceListener {

  /** Logging facility */
  static final Logger logger = LoggerFactory.getLogger(EndpointPublishingService.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.entwine.weblounge.restpublisher";

  /** Configuration key prefix for rest publisher configuration */
  public static final String OPT_PREFIX = "restpublisher";

  /** Configuration key for the rest services path prefix */
  public static final String OPT_PATH = OPT_PREFIX + ".path";

  /** Configuration key used to configure the endpoint's alias */
  public static final String DEFAULT_PATH = "/system/weblounge";

  /** The context path option used to override the default context path */
  public static final String OPT_CONTEXTPATH = "rest.path";

  /** Context of this component */
  protected ComponentContext componentContext = null;

  /** The bundle context */
  protected BundleContext bundleContext = null;

  /** The JSR 311 service listener */
  protected ServiceListener jsr311ServiceListener = null;

  /** The mountpoint for REST services */
  protected String defaultContextPathPrefix = DEFAULT_PATH;

  /** Mapping of registered endpoints */
  protected Map<String, ServiceRegistration> endpointRegistrations = null;

  /** Mapping of registered servlets */
  protected Map<String, JAXRSServlet> endpointServlets = null;

  /** The site manager */
  protected SiteManager sites = null;

  /** The environment */
  protected Environment environment = null;

  /**
   * Creates a new publishing service for JSR 311 annotated classes.
   */
  public EndpointPublishingService() {
    endpointRegistrations = new ConcurrentHashMap<String, ServiceRegistration>();
    endpointServlets = new ConcurrentHashMap<String, JAXRSServlet>();
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
    this.bundleContext = componentContext.getBundleContext();

    sites.addSiteListener(this);

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

    // Register JAX-RS services that have already been loaded
    for (Bundle bundle : bundleContext.getBundles()) {

      // Skip bundles that are not active
      if (Bundle.ACTIVE != bundle.getState()) {
        logger.trace("Skipping bundle '{}' in state {} while looking for JAXRS endpoints", bundle, bundle.getState());
        continue;
      }

      // Skip bundles that don't have any services registered
      ServiceReference[] refs = bundle.getRegisteredServices();
      if (refs == null)
        continue;

      // Explicitly register the JAXB service by crafting a manual ServiceEvent
      for (ServiceReference ref : refs) {
        try {
          ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);
          jsr311ServiceListener.serviceChanged(event);
        } catch (Throwable t) {
          logger.error("Error registering JAXRS annotated service {} : {}", ref);
        }
      }
    }

  }

  /**
   * Callback for OSGi's declarative services component inactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext componentContext) throws Exception {
    if (jsr311ServiceListener != null) {
      bundleContext.removeServiceListener(jsr311ServiceListener);
    }

    // Unregister the current jsr311 servlets
    for (String path : endpointRegistrations.keySet()) {
      unregisterEndpoint(path);
    }
    endpointRegistrations.clear();

    // Stop listening to sites
    sites.removeSiteListener(this);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null)
      return;

    boolean changed = configure(properties);
    if (!changed)
      return;

    // Unregister all current endpoints
    for (String path : endpointRegistrations.keySet()) {
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
   * Returns a list of all service endpoints along with their paths.
   * 
   * @return the service paths
   */
  public Map<String, ServiceRegistration> getEndpoints() {
    Map<String, ServiceRegistration> services = new HashMap<String, ServiceRegistration>(endpointRegistrations.size());
    services.putAll(endpointRegistrations);
    return services;
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
   * @param bundle
   *          the registering bundle
   * @param the
   *          endpoint's path
   */
  protected void registerEndpoint(Object service, String contextPath,
      String endpointPath, Bundle bundle) {
    try {
      JAXRSServlet servlet = new JAXRSServlet(endpointPath, service, bundle);
      servlet.setSite(sites.findSiteByBundle(bundle));
      servlet.setEnvironment(environment);
      Dictionary<String, String> initParams = new Hashtable<String, String>();
      initParams.put("alias", contextPath);
      initParams.put("servlet-name", service.toString());
      initParams.put(SharedHttpContext.PROPERTY_OSGI_HTTP_CONTEXT_ID, SharedHttpContext.HTTP_CONTEXT_ID);
      ServiceRegistration reg = bundleContext.registerService(Servlet.class.getName(), servlet, initParams);
      endpointRegistrations.put(contextPath, reg);
      endpointServlets.put(contextPath, servlet);
      logger.debug("Registering {} at {}", service, contextPath);
    } catch (Throwable t) {
      logger.error("Error registering rest service at " + contextPath, t);
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
    logger.debug("Unregistering rest endpoint {}", contextPath);

    // Remove the servlet from the http service
    try {
      ServiceRegistration reg = endpointRegistrations.get(contextPath);
      reg.unregister();
    } catch (IllegalStateException e) {
      // Never mind, the service has been unregistered already
    } catch (Throwable t) {
      logger.error("Unregistering endpoint at '{}' failed: {}", contextPath, t.getMessage());
    }

    // Unregister the servlet
    endpointRegistrations.remove(contextPath);
    endpointServlets.remove(contextPath);
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
      Object service = null;

      try {
        service = bundleContext.getService(ref);
      } catch (IllegalStateException e) {
        // This is happening when the system is going down and the referenced
        // bundle context has already become invalid
        logger.debug("Endpoint publishing service is already down");
        return;
      }

      // Sometimes, there is a service reference without a service
      if (service == null)
        return;

      // Is this a JSR 311 annotated class?
      Path pathAnnotation = service.getClass().getAnnotation(Path.class);
      if (pathAnnotation == null)
        return;

      // Is there a context path?
      Object contextPathProperty = ref.getProperty(OPT_CONTEXTPATH);
      if (contextPathProperty == null)
        return;

      // Adjust relative context paths
      String contextPath = contextPathProperty.toString();
      if (!contextPath.startsWith("/")) {
        contextPath = UrlUtils.concat(defaultContextPathPrefix, contextPath);
      }

      // Find the registering bundle
      Bundle bundle = ref.getBundle();

      // Process the event
      switch (event.getType()) {
        case ServiceEvent.REGISTERED:
          logger.debug("Registering JAX-RS service {} at {}", service, contextPath);

          // Make sure there is no clash in context paths
          ServiceRegistration existingRef = endpointRegistrations.get(contextPath);
          if (existingRef != null) {
            Object s = bundleContext.getService(existingRef.getReference());
            logger.error("Endpoint {} cannot be registered since the context path {} has already been claimed", new Object[] {
                service,
                s,
                contextPath });
            return;
          }

          registerEndpoint(service, contextPath, pathAnnotation.value(), bundle);
          break;
        case ServiceEvent.MODIFIED:
          logger.debug("JAX-RS service {} modified", service);
          break;
        case ServiceEvent.UNREGISTERING:
          logger.debug("Unregistering JAX-RS service {} from {}", service, contextPath);
          unregisterEndpoint(contextPath);
          break;
        default:
          // We don't care about these cases
          break;
      }
    }

  }

  /**
   * OSGi callback to set the sites manager.
   * 
   * @param sites
   *          the sites manager
   */
  void setSites(SiteManager sites) {
    this.sites = sites;
  }

  /**
   * OSGi callback to set the environment.
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
   * @see ch.entwine.weblounge.kernel.site.SiteServiceListener#siteAppeared(ch.entwine.weblounge.common.site.Site,
   *      org.osgi.framework.ServiceReference)
   */
  @Override
  public void siteAppeared(Site site, ServiceReference reference) {
    Bundle siteBundle = reference.getBundle();
    if (siteBundle == null)
      return;
    for (Map.Entry<String, JAXRSServlet> r : endpointServlets.entrySet()) {
      JAXRSServlet servlet = r.getValue();
      if (siteBundle.equals(servlet.getBundle())) {
        servlet.setSite(site);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.site.SiteServiceListener#siteDisappeared(ch.entwine.weblounge.common.site.Site)
   */
  @Override
  public void siteDisappeared(Site site) {
    // Nothing to do, the associated endpoints will soon be gone, too
  }

}
