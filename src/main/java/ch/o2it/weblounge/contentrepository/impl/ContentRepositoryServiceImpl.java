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

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.contentrepository.ContentRepositoryService;
import ch.o2it.weblounge.contentrepository.impl.bundle.BundleContentRepository;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
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

  /** Configuration key for the content repository implementation */
  public static final String OPT_REPOSITORY_TYPE = "ch.o2it.weblounge.contentrepository";

  /** Default implementation for a content repository */
  public static final String DEFAULT_REPOSITORY_TYPE = BundleContentRepository.class.getName();

  /** The prototype class */
  protected Class<? extends ContentRepository> prototype = null;

  /** The registered content repositories */
  private Map<Site, ContentRepository> repositories = new HashMap<Site, ContentRepository>();

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
   * Callback from the OSGi environment to activate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  @SuppressWarnings("unchecked")
  public void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();
    Object prototypeClassName = context.getProperties().get(OPT_REPOSITORY_TYPE);

    logger.info("Starting content repository service");

    // Try to load the implementation from the system configuration
    if (prototypeClassName != null) {
      logger.info("Creating content repository prototype from " + prototypeClassName);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      prototype = (Class<? extends ContentRepository>) loader.loadClass((String) prototypeClassName);
    }

    // No luck here. Let's try the default
    else {
      logger.info("No content repository implementation configured");
      prototype = (Class<? extends ContentRepository>) Class.forName(DEFAULT_REPOSITORY_TYPE);
      logger.info("Using default implementation " + DEFAULT_REPOSITORY_TYPE);
    }

    // Test creation of the prototype
    prototype.newInstance();

    siteTracker = new SiteTracker(this, bundleContext);
    siteTracker.open();
    
    factoryTracker = new ContentRepositoryFactoryTracker(this, bundleContext);
    factoryTracker.open();

    logger.debug("Content repository service activated");
  }

  /**
   * Callback from the OSGi environment to deactivate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  public void deactivate(ComponentContext context) {
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
  public void updated(Dictionary properties) throws ConfigurationException {
    Object prototypeClassName = properties.get(OPT_REPOSITORY_TYPE);
    if (prototypeClassName != null) {
      logger.info("Switching content repository implementation to " + prototypeClassName);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try {
        prototype = (Class<? extends ContentRepository>) loader.loadClass((String) prototypeClassName);
      } catch (ClassNotFoundException e) {
        throw new ConfigurationException(OPT_REPOSITORY_TYPE, e.getMessage());
      }
    }
  }

  /**
   * Callback from the OSGi environment when a new site is created. This method
   * will create a <code>ContentRepository</code> using
   * {@link #newContentRepository(Site)} and register it with the site.
   * 
   * @param site
   *          the site
   */
  @SuppressWarnings("unchecked")
  public void registerSite(Site site, Bundle bundle) {
    ContentRepository repository = null;
    Dictionary properties = new Hashtable();
    properties.put(Site.class.getName(), site);
    properties.put(Bundle.class.getName(), bundle);
    
    // Create and set up the repository
    try {
      repository = prototype.newInstance();
      repository.connect(properties);
      repositories.put(site, repository);
    } catch (InstantiationException e) {
      logger.error("Unable to instantiate content repository " + prototype + " for site '" + site + "'", e);
      return;
    } catch (IllegalAccessException e) {
      logger.error("Illegal access while instantiating content repository " + prototype + " for site '" + site + "'", e);
      return;
    } catch (ContentRepositoryException e) {
      logger.error("Unable to connect content repository " + repository + " for site '" + site + "'", e);
      return;
    }

    // Start the repository
    try {
      repository.start();
      repositories.put(site, repository);
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
      factory.registerSite(site, reference.getBundle());
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
    public ContentRepositoryFactoryTracker(ContentRepositoryServiceImpl service,
        BundleContext context) {
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
