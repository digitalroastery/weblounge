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

package ch.entwine.weblounge.kernel.site;

import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteException;
import ch.entwine.weblounge.common.site.SiteURL;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * The site manager watches site services coming and going and makes them
 * available by id, server name etc.
 */
public class SiteManager {

  /** Logging facility */
  static final Logger logger = LoggerFactory.getLogger(SiteManager.class);

  /** The configuration admin service */
  private ConfigurationAdmin configurationAdmin = null;

  /** The site tracker */
  private SiteTracker siteTracker = null;

  /** The content repository tracker */
  private ContentRepositoryTracker repositoryTracker = null;

  /** The sites */
  private List<Site> sites = new ArrayList<Site>();

  /** Maps server names to sites */
  private Map<String, Site> sitesByServerName = new HashMap<String, Site>();

  /** Maps sites to osgi bundles */
  private Map<Site, Bundle> siteBundles = new HashMap<Site, Bundle>();

  /** The environment */
  private Environment environment = null;

  /** Maps content repositories to site identifier */
  private Map<String, ContentRepository> repositoriesBySite = new HashMap<String, ContentRepository>();

  /** Maps content repository configurations to site identifier */
  private Map<String, Configuration> repositoryConfigurations = new HashMap<String, Configuration>();

  /** Registered site listeners */
  private List<SiteServiceListener> listeners = new ArrayList<SiteServiceListener>();

  /**
   * Adds <code>listener</code> to the list of site listeners.
   * 
   * @param listener
   *          the site listener
   */
  public void addSiteListener(SiteServiceListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  /**
   * Removes <code>listener</code> from the list of site listeners.
   * 
   * @param listener
   *          the site listener
   */
  public void removeSiteListener(SiteServiceListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component activation fails
   */
  void activate(ComponentContext context) throws Exception {
    logger.debug("Starting site dispatcher");

    BundleContext bundleContext = context.getBundleContext();
    siteTracker = new SiteTracker(this, bundleContext);
    siteTracker.open();

    repositoryTracker = new ContentRepositoryTracker(this, bundleContext);
    repositoryTracker.open();

    logger.debug("Site manager activated");
  }

  /**
   * Callback for OSGi's declarative services component dactivation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component inactivation fails
   */
  void deactivate(ComponentContext context) {
    logger.debug("Deactivating site manager");

    siteTracker.close();
    siteTracker = null;

    repositoryTracker.open();
    repositoryTracker = null;

    logger.info("Site manager stopped");
  }

  /**
   * Returns the site with the given site identifier or <code>null</code> if no
   * such site is currently registered.
   * 
   * @param identifier
   *          the site identifier
   * @return the site
   */
  public Site findSiteByIdentifier(String identifier) {
    for (Site site : sites) {
      if (site.getIdentifier().equals(identifier)) {
        return site;
      }
    }
    return null;
  }

  /**
   * Returns the site associated with the given server name.
   * <p>
   * Note that the server name is expected to not end with a trailing slash, so
   * please pass in <code>www.entwinemedia.com</code> instead of
   * <code>www.entwinemedia.com/</code>.
   * 
   * @param url
   *          the site url, e.g. <code>http://www.entwinemedia.com</code>
   * @return the site
   */
  public Site findSiteByURL(URL url) {
    String hostName = url.getHost();
    Site site = sitesByServerName.get(hostName);
    if (site != null)
      return site;

    // There is obviously no direct match. Therefore, try to find a
    // wildcard match
    for (Map.Entry<String, Site> e : sitesByServerName.entrySet()) {
      String siteUrl = e.getKey();

      try {
        // convert the host wildcard (ex. *.domain.tld) to a valid regex (ex.
        // .*\.domain\.tld)
        String alias = siteUrl.replace(".", "\\.");
        alias = alias.replace("*", ".*");
        if (hostName.matches(alias)) {
          site = e.getValue();
          logger.info("Registering {} for site ", url, site);
          sitesByServerName.put(hostName, site);
          return site;
        }
      } catch (PatternSyntaxException ex) {
        logger.warn("Error while trying to find a host wildcard match: ".concat(ex.getMessage()));
      }
    }

    logger.debug("Lookup for {} did not match any site", url);
    return null;
  }

  /**
   * Returns the OSGi bundle that contains the given site or <code>null</code>
   * if no such site has been registered.
   * 
   * @param site
   *          the site
   * @return the site's OSGi bundle
   */
  public Bundle getSiteBundle(Site site) {
    if (site == null)
      throw new IllegalArgumentException("Parameter 'site' must not be null");
    return siteBundles.get(site);
  }

  /**
   * Returns an iteration of all currently registered sites.
   * 
   * @return the sites
   */
  public Iterator<Site> sites() {
    List<Site> site = new ArrayList<Site>();
    site.addAll(sites);
    return site.iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.SiteDispatcherService#addSite(ch.entwine.weblounge.common.site.Site,
   *      org.osgi.framework.ServiceReference)
   */
  synchronized void addSite(Site site, ServiceReference reference) {
    sites.add(site);
    siteBundles.put(site, reference.getBundle());

    // Make sure we have an environment
    Environment env = environment;
    if (env == null) {
      logger.warn("No environment has been defined. Falling back to {}", Environment.Production);
      env = environment;
    }

    // Register the site urls and make sure we don't double book
    site.initialize(env);
    for (SiteURL url : site.getHostnames()) {
      if (!env.equals(url.getEnvironment()))
        continue;
      String hostName = url.getURL().getHost();
      Site registeredFirst = sitesByServerName.get(hostName);
      if (registeredFirst != null && !site.equals(registeredFirst)) {
        logger.error("Another site is already registered to {}. Site is not registered", url);
        continue;
      }
      sitesByServerName.put(hostName, site);
    }

    logger.debug("Site '{}' registered", site);

    // Look for content repositories
    ContentRepository repository = repositoriesBySite.get(site.getIdentifier());
    if (repository != null && site.getContentRepository() == null) {
      try {
        repository.connect(site);
        site.setContentRepository(repository);
        logger.info("Site '{}' connected to content repository at {}", site, repository);
      } catch (ContentRepositoryException e) {
        logger.warn("Error connecting content repository " + repository + " to site '" + site + "'", e);
      }
    } else {
      try {
        Configuration config = configurationAdmin.createFactoryConfiguration("ch.entwine.weblounge.contentrepository.factory", null);
        Dictionary<Object, Object> properties = new Hashtable<Object, Object>();
        properties.put(Site.class.getName().toLowerCase(), site.getIdentifier());
        for (Map.Entry<String, List<String>> option : site.getOptions().entrySet()) {
          String key = option.getKey();
          if (option.getValue().size() == 1) {
            properties.put(key, option.getValue().get(0));
          } else {
            properties.put(key, option.getValue().toArray(new String[option.getValue().size()]));
          }
        }
        config.update(properties);
        repositoryConfigurations.put(site.getIdentifier(), config);
      } catch (IOException e) {
        logger.error("Unable to create configuration for content repository of site '" + site + "'", e);
      }
    }

    // Inform site listeners
    synchronized (listeners) {
      for (SiteServiceListener listener : listeners) {
        listener.siteAppeared(site, reference);
      }
    }

    // Start the site
    if (site.isStartedAutomatically()) {
      try {
        logger.debug("Starting site '{}'", site);
        // TODO: Make sure there is a *running* content repository for this site
        // Alternatively, have the site implementation use a reference to the
        // repository and start itself once the repository switches to "running"
        // state (requires a repository listener)
        site.start();
      } catch (IllegalStateException e) {
        logger.error("Site '{}' could not be started: {}", e.getMessage(), e);
      } catch (SiteException e) {
        logger.error("Site '{}' could not be started: {}", e.getMessage(), e);
      }
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.SiteDispatcherService#removeSite(ch.entwine.weblounge.common.site.Site)
   */
  synchronized void removeSite(Site site) {

    // Inform site listeners
    synchronized (listeners) {
      for (SiteServiceListener listener : listeners) {
        listener.siteDisappeared(site);
      }
    }

    // Stop the site if it's running
    try {
      if (site.isOnline()) {
        site.stop();
      }
    } catch (Throwable t) {
      logger.error("Error stopping site '{}'", site.getIdentifier(), t);
    }

    // Remove the site's content repository. Note that the content repository
    // will be disconnected by the content repository tracker
    site.setContentRepository(null);

    // Tell the content repository factory to remove the repository
    Configuration configuration = repositoryConfigurations.get(site.getIdentifier());
    if (configuration != null) {
      try {
        configuration.delete();
      } catch (IOException e) {
        logger.error("Error deleting repository configuration for site '" + site.getIdentifier() + "'", e);
      }
    } else {
      logger.debug("No connected content repository found to shutdown for site '{}'", site.getIdentifier());
    }

    // Remove it from the registry
    sites.remove(site);
    siteBundles.remove(site);
    Iterator<Site> si = sitesByServerName.values().iterator();
    while (si.hasNext()) {
      Site s = si.next();
      if (site.equals(s)) {
        si.remove();
      }
    }

    logger.debug("Site {} unregistered", site);
  }

  /**
   * Adds the content repository to the list of registered repositories.
   * 
   * @param siteIdentifier
   *          the site identifier
   * @param repository
   *          the content repository
   * @throws ContentRepositoryException
   *           if connecting the content repository to the site fails
   */
  synchronized void addContentRepository(String siteIdentifier,
      ContentRepository repository) throws ContentRepositoryException {
    if (StringUtils.isBlank(siteIdentifier))
      throw new IllegalArgumentException("Site identifier must not be null");
    if (repository == null)
      throw new IllegalArgumentException("Content repository must not be null");

    Site site = findSiteByIdentifier(siteIdentifier);
    if (site != null) {
      try {
        repository.connect(site);
        logger.info("Site '{}' connected to content repository at {}", site, repository);
        site.setContentRepository(repository);
      } catch (ContentRepositoryException e) {
        logger.warn("Error connecting content repository " + repository + " to site '" + site + "'", e);
        throw e;
      }
    }

    repositoriesBySite.put(siteIdentifier, repository);
  }

  /**
   * Adds the content repository to the list of registered repositories.
   * 
   * @param repository
   *          the content repository
   */
  synchronized void removeContentRepository(ContentRepository repository) {
    if (repository == null)
      throw new IllegalArgumentException("Content repository must not be null");

    // Find the site that is associated with the content repository
    String siteIdentifier = null;
    for (Map.Entry<String, ContentRepository> entry : repositoriesBySite.entrySet()) {
      if (entry.getValue().equals(repository)) {
        siteIdentifier = entry.getKey();
        break;
      }
    }

    // Tell the site to no longer use it
    if (siteIdentifier != null) {
      repositoriesBySite.remove(siteIdentifier);
      Site site = findSiteByIdentifier(siteIdentifier);
      if (site != null) {
        site.setContentRepository(null);
      }
    }

    // Tell the repository to clean up
    try {
      repository.disconnect();
    } catch (ContentRepositoryException e) {
      logger.warn("Error disconnecting content repository " + repository, e);
    }

  }

  /**
   * OSGi environment callback that passes in the configuration admin service.
   * 
   * @param configurationAdmin
   *          the configuration admin service
   */
  void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
    this.configurationAdmin = configurationAdmin;
  }

  /**
   * OSGi callback that passes in the environment.
   * 
   * @param environment
   *          the environment
   */
  void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * OSGi callback that removes the environment.
   * 
   * @param environment
   *          the environment
   */
  void removeEnvironment(Environment environment) {
    if (Environment.Production.equals(this.environment)) {
      logger.info("Changing site environments to {}", Environment.Production);
      for (Site site : sites) {
        site.initialize(Environment.Production);
      }
    }
    this.environment = null;
  }

  /**
   * This tracker is used to track <code>Site</code> services. Once a site is
   * detected, it registers that site with the
   * <code>SiteDispatcherService</code>.
   */
  private final class SiteTracker extends ServiceTracker {

    /** The site dispatcher */
    private SiteManager siteManager = null;

    /**
     * Creates a new <code>SiteTracker</code>.
     * 
     * @param siteManager
     *          the site dispatcher
     * @param context
     *          the site dispatcher's bundle context
     */
    public SiteTracker(SiteManager siteManager, BundleContext context) {
      super(context, Site.class.getName(), null);
      this.siteManager = siteManager;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public Object addingService(ServiceReference reference) {
      Site site = (Site) super.addingService(reference);
      siteManager.addSite(site, reference);
      return site;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    public void removedService(ServiceReference reference, Object service) {
      Site site = (Site) service;
      siteManager.removeSite(site);
      if (reference.getBundle() != null) {
        super.removedService(reference, service);
      }
    }

  }

  /**
   * This tracker is used to track <code>ContentRepository</code> services. When
   * a repository either shows up or disappears, the associated site as updated
   * accordingly.
   */
  private final class ContentRepositoryTracker extends ServiceTracker {

    /** The site dispatcher */
    private SiteManager siteManager = null;

    /**
     * Creates a new <code>ContentRepositoryTracker</code>.
     * 
     * @param siteManager
     *          the site dispatcher
     * @param context
     *          the site dispatcher's bundle context
     */
    public ContentRepositoryTracker(SiteManager siteManager,
        BundleContext context) {
      super(context, ContentRepository.class.getName(), null);
      this.siteManager = siteManager;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    public Object addingService(ServiceReference reference) {
      String siteIdentifier = (String) reference.getProperty(Site.class.getName().toLowerCase());
      if (siteIdentifier == null) {
        logger.warn("Found content repository without site property");
        return super.addingService(reference);
      }

      // Register the content repository
      ContentRepository repository = (ContentRepository) super.addingService(reference);
      try {
        siteManager.addContentRepository(siteIdentifier, repository);
      } catch (ContentRepositoryException e) {
        return null;
      }

      return repository;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    public void removedService(ServiceReference reference, Object service) {
      ContentRepository repository = (ContentRepository) service;
      siteManager.removeContentRepository(repository);
    }

  }

}