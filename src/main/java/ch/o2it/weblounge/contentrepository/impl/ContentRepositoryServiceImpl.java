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

package ch.o2it.weblounge.contentrepository.impl;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.contentrepository.ContentRepositoryService;
import ch.o2it.weblounge.contentrepository.impl.bundle.BundleContentRepository;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Service that will watch out for sites, associate them with a content
 * repository if needed and then register it with the
 * <code>ContentRepositoryFactory</code> to allow for static lookup of content
 * repositories.
 * <p>
 * Using the key <code>ch.o2it.weblounge.contentrepository</code>, the concrete
 * implementation can be specified. If this property cannot be found, an
 * instance of the <code>BundleContentRepository</code> will be created which
 * will serve pages and resources from the site's bundle.
 */
public class ContentRepositoryServiceImpl implements ContentRepositoryService, ManagedService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ContentRepositoryServiceImpl.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.o2it.weblounge.contentrepository";

  /** Configuration key prefix for content repository configuration */
  public static final String OPT_PREFIX = "contentrepository";

  /** Configuration key for the content repository implementation */
  public static final String OPT_REPOSITORY_TYPE = OPT_PREFIX + ".type";

  /** Default implementation for a content repository */
  public static final String DEFAULT_REPOSITORY_TYPE = BundleContentRepository.class.getName();

  /** The prototype class */
  protected Class<? extends ContentRepository> prototype = null;

  /** The registered content repositories */
  private Map<Site, ContentRepository> repositories = new HashMap<Site, ContentRepository>();

  /** The registered bundles */
  private Map<Site, Bundle> bundles = new HashMap<Site, Bundle>();

  /** Extended configuration properties */
  private Map<String, String> extendedProperties = new HashMap<String, String>();

  /** The site tracker */
  private SiteTracker siteTracker = null;

  /** Tracker for content repository factories */
  private ContentRepositoryFactoryTracker factoryTracker = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.ContentRepositoryService#getRepository(ch.o2it.weblounge.common.site.Site)
   */
  public ContentRepository getRepository(Site site) {
    return repositories.get(site);
  }

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component activation fails
   */
  @SuppressWarnings("unchecked")
  void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();

    logger.info("Starting content repository service");

    // Try to get hold of the service configuration
    ServiceReference configAdminRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
    if (configAdminRef != null) {
      ConfigurationAdmin configAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminRef);
      Dictionary<?, ?> config = configAdmin.getConfiguration(SERVICE_PID).getProperties();
      if (config != null) {
        configure(config);
      } else {
        logger.warn("Unable to load content repository service configuration");
      }
    } else {
      logger.warn("Unable to get service reference for class ConfigurationAdmin");
    }

    // Check the configuration of the repository implementation
    if (prototype == null) {
      logger.info("No content repository implementation configured");
      prototype = (Class<? extends ContentRepository>) Class.forName(DEFAULT_REPOSITORY_TYPE);
      logger.info("Using default implementation " + DEFAULT_REPOSITORY_TYPE);
    }

    // Test creation of the prototype
    try {
      prototype.newInstance();
    } catch (Exception e) {
      logger.error("Error creating instance of content repository implementation {}", e);
      throw e;
    }

    siteTracker = new SiteTracker(this, bundleContext);
    siteTracker.open();

    factoryTracker = new ContentRepositoryFactoryTracker(this, bundleContext);
    factoryTracker.open();

    logger.debug("Content repository service activated");
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) throws Exception {
    logger.debug("Deactivating content repository service");
    siteTracker.close();
    factoryTracker.close();
    siteTracker = null;
    factoryTracker = null;
    logger.info("Content repository service stopped");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary properties) {
    if (properties == null)
      return;
    try {
      if (configure(properties)) {
        for (Site site : new ArrayList<Site>(repositories.keySet())) {
          Bundle bundle = bundles.get(site);
          unregisterSite(site);
          registerSite(site, bundle, properties);
        }
      }
    } catch (ConfigurationException e) {
      logger.error("Error configuring " + this + ": ", e.getMessage());
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
  @SuppressWarnings("unchecked")
  private synchronized boolean configure(Dictionary<?, ?> config)
      throws ConfigurationException {
    boolean configurationChanged = false;

    // Repository type
    Object prototypeClassName = config.get(OPT_REPOSITORY_TYPE);
    if (prototypeClassName != null && (prototype == null || !prototypeClassName.equals(prototype.getName()))) {
      logger.info("Content repository implementation is " + prototypeClassName);
      ClassLoader loader = ContentRepositoryServiceImpl.class.getClassLoader();
      try {
        prototype = (Class<? extends ContentRepository>) loader.loadClass((String) prototypeClassName);
        configurationChanged = true;
      } catch (ClassNotFoundException e) {
        throw new ConfigurationException(e.getMessage());
      }
    }

    // Extended properties
    if (config.size() != extendedProperties.size())
      configurationChanged = true;
    else {
      for (Map.Entry<String, String> entry : extendedProperties.entrySet()) {
        if (!entry.getValue().equals(config.get(entry.getKey()))) {
          configurationChanged = true;
          break;
        }
      }
    }

    // Did we find a noticeable change?
    if (!configurationChanged) {
      logger.info("Received updated but identical content repository service configuration");
      return false;
    }

    // First, update the connection properties
    extendedProperties.clear();
    for (Enumeration<?> keys = config.keys(); keys.hasMoreElements();) {
      String key = keys.nextElement().toString();
      String value = config.get(key).toString();

      // Do variable replacement using the system properties
      for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
        StringBuffer envKey = new StringBuffer("\\$\\{").append(entry.getKey()).append("\\}");
        value = value.replaceAll(envKey.toString(), entry.getValue().toString());
      }

      // Do variable replacement using the system environment
      for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
        StringBuffer envKey = new StringBuffer("\\$\\{").append(entry.getKey()).append("\\}");
        value = value.replaceAll(envKey.toString(), entry.getValue());
      }

      logger.debug("Registering extended property {}={}", key, value);
      extendedProperties.put(key, value);
    }

    return true;
  }

  /**
   * Callback from the OSGi environment when a new site is created. This method
   * will create a <code>ContentRepository</code> using
   * {@link #newContentRepository(Site)} and register it with the site.
   * 
   * @param site
   *          the site
   * @param bundle
   *          the bundle containing the site
   * @param properties
   *          the site properties
   */
  @SuppressWarnings("unchecked")
  public void registerSite(Site site, Bundle bundle,
      Dictionary<String, Object> properties) {

    ContentRepository repository = null;

    // Add well-known properties
    properties.put(Site.class.getName(), site);
    properties.put(Bundle.class.getName(), bundle);

    // Determine the repository type, if configured
    String repositoryType = (String) properties.get(OPT_REPOSITORY_TYPE);

    // Add the extended properties from either the bundle or the service
    // configuration
    for (Map.Entry<String, String> p : extendedProperties.entrySet()) {
      properties.put(p.getKey(), p.getValue());
    }

    // Get the repository class
    Class<? extends ContentRepository> repositoryClass = null;
    if (repositoryType != null) {
      try {
        repositoryClass = (Class<? extends ContentRepository>) getClass().getClassLoader().loadClass(repositoryType);
      } catch (ClassNotFoundException e) {
        logger.error("Repository implementation class '" + repositoryType + "' not found for site '" + site + "'", e);
        return;
      }
    } else {
      repositoryClass = prototype;
    }

    // Create and set up the repository
    try {
      repository = repositoryClass.newInstance();
      repositories.put(site, repository);
      bundles.put(site, bundle);
      repository.connect(properties);
    } catch (InstantiationException e) {
      logger.error("Unable to instantiate content repository " + repositoryClass + " for site '" + site + "'", e);
      return;
    } catch (IllegalAccessException e) {
      logger.error("Illegal access while instantiating content repository " + repositoryClass + " for site '" + site + "'", e);
      return;
    } catch (ContentRepositoryException e) {
      logger.error("Unable to connect content repository " + repository + " for site '" + site + "'", e);
      return;
    }

    // Start the repository
    try {
      repository.start();
    } catch (ContentRepositoryException e) {
      logger.error("Unable to start content repository " + repository + " for site '" + site + "'", e);
    }
  }

  /**
   * Callback from the OSGi environment when a site is destroyed. This method
   * will close the <code>ContentRepository</code> an dispose of any references.
   * 
   * @param site
   *          the site
   */
  public void unregisterSite(Site site) {
    ContentRepository repository = repositories.remove(site);
    bundles.remove(site);
    if (repository == null)
      return;
    try {
      repository.stop();
    } catch (ContentRepositoryException e) {
      logger.error("Unable to stop content repository " + repository + " for site '" + site + "'", e);
    }
    try {
      repository.disconnect();
    } catch (ContentRepositoryException e) {
      logger.error("Unable to disconnect content repository " + repository + " for site '" + site + "'", e);
    }
  }

  /**
   * Service tracker for {@link Site} instances.
   */
  private static class SiteTracker extends ServiceTracker {

    /** The enclosing content repository factory */
    private ContentRepositoryServiceImpl factory = null;

    /**
     * Creates a new site tracker which will call back to the
     * <code>ContentRepositoryFactory</code> that created it.
     * 
     * @param factory
     *          the content repository factory
     * @param context
     *          the bundle context
     */
    public SiteTracker(ContentRepositoryServiceImpl factory,
        BundleContext context) {
      super(context, Site.class.getName(), null);
      this.factory = factory;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
      Site site = (Site) super.addingService(reference);
      Dictionary<String, Object> properties = new Hashtable<String, Object>();
      for (String key : reference.getPropertyKeys())
        properties.put(key, reference.getProperty(key));
      factory.registerSite(site, reference.getBundle(), properties);
      return site;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
      factory.unregisterSite((Site) service);
      super.removedService(reference, service);
    }

  }

  /**
   * Service tracker for {@link ContentRepositoryFactoryTracker} instances.
   */
  private static class ContentRepositoryFactoryTracker extends ServiceTracker {

    /** The enclosing content repository factory */
    private ContentRepositoryServiceImpl service = null;

    /**
     * Creates a new site tracker which will call back to the
     * <code>ContentRepositoryService</code> that created it.
     * 
     * @param service
     *          the content repository service
     * @param context
     *          the bundle context
     */
    public ContentRepositoryFactoryTracker(
        ContentRepositoryServiceImpl service, BundleContext context) {
      super(context, ContentRepositoryFactory.class.getName(), null);
      this.service = service;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
      ContentRepositoryFactory factory = (ContentRepositoryFactory) super.addingService(reference);
      factory.setContentRepositoryService(service);
      return factory;
    }

  }

}
