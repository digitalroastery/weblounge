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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.common.impl.url.PathUtils;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.site.Site;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * This factory is listening for sites, while registering a cache configuration
 * for each site that is targeted at the {@link CacheConfigurationFactory}.
 * <p>
 * When registered with the system using the pid
 * <code>ch.o2it.weblounge.cache</code>, it will be used as the basis for
 * configuration objects.
 */
public class CacheConfigurationFactory implements ManagedService {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(CacheConfigurationFactory.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.o2it.weblounge.cache";

  /** Service configurations per site */
  private Map<String, CacheConfiguration> configurations = new HashMap<String, CacheConfiguration>();

  /** The sites */
  private Map<String, Site> sites = new HashMap<String, Site>();

  /** Reference to the configuration admin service */
  private ConfigurationAdmin configurationAdmin = null;

  /** The cache configuration */
  private Dictionary<Object, Object> cacheConfiguration = null;

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void updated(Dictionary properties) throws ConfigurationException {
    cacheConfiguration = properties;

    // Loop over all configurations and update them accordingly
    for (Map.Entry<String, CacheConfiguration> entry : configurations.entrySet()) {
      CacheConfiguration configHolder = entry.getValue();
      Configuration config = entry.getValue().getConfiguration();

      // If there is no configuration, then there is no configuration admin.
      // Highly unlikely, since we are inside the updated() method, but you
      // never know :-)
      if (config == null)
        continue;

      boolean previouslyEnabled = configHolder.isEnabled();
      boolean nowEnabled = !ConfigurationUtils.isFalse((String) properties.get(CacheServiceImpl.OPT_ENABLE));

      try {
        Dictionary configuration = createConfiguration(configHolder.getIdentifier(), configHolder.getName());
        if (nowEnabled && !previouslyEnabled) {
          config = configurationAdmin.createFactoryConfiguration(CacheServiceFactory.SERVICE_PID);
          config.update(properties);
          configHolder.setConfiguration(config);
        } else if (nowEnabled) {
          config.update(configuration);
        } else if (previouslyEnabled) {
          config.delete();
          configHolder.setConfiguration(null);
        }
      } catch (IOException e) {
        logger.error("Error updating cache configuration in persistent store", e);
      }
    }
  }

  /**
   * Creates a configuration for the cache service associated with
   * <code>site</code> and returns it.
   * <p>
   * The configuration is compiled by taking the base configuration that was
   * earlier obtained using the configuration admin service and adding the site
   * specific properties to it.
   * 
   * @param id
   *          the site identifier
   * @param name
   *          the site name
   * @return the configuration
   */
  private Dictionary<Object, Object> createConfiguration(String id, String name) {
    Hashtable<Object, Object> configuration = new Hashtable<Object, Object>();

    // Add the default properties
    if (cacheConfiguration != null) {
      for (Enumeration<Object> e = cacheConfiguration.keys(); e.hasMoreElements();) {
        Object key = e.nextElement();
        configuration.put(key, cacheConfiguration.get(key));
      }
    }

    // Add everything that's site specific
    configuration.put(CacheServiceImpl.OPT_ID, id);
    configuration.put(CacheServiceImpl.OPT_NAME, name);
    configuration.put(CacheServiceImpl.OPT_DISKSTORE_PATH, PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", id, "cache"));

    return configuration;
  }

  /**
   * Returns the configuration for the cache with the given identifier or
   * <code>null</code> if no such cache was registered.
   * 
   * @param id
   *          the cache identifier
   * @return the cache configuration
   */
  public CacheConfiguration getConfiguration(String id) {
    for (CacheConfiguration config : configurations.values()) {
      if (id.equals(config.getIdentifier()))
        return config;
    }
    return null;
  }

  /**
   * Returns an array of all registered cache configurations.
   * 
   * @return the cache configurations
   */
  public CacheConfiguration[] getConfigurations() {
    return configurations.values().toArray(new CacheConfiguration[configurations.size()]);
  }

  /**
   * Asks the factory to register the given configuration with the configuration
   * admin service.
   * 
   * @param configuration
   *          the cache configuration
   * @throws IOException
   *           if registering the configuration failed
   */
  public void enable(CacheConfiguration configuration) throws IOException {
    if (!configurations.containsValue(configuration))
      return;
    
    if (!configuration.isEnabled()) {
      Configuration c = configurationAdmin.createFactoryConfiguration(CacheServiceFactory.SERVICE_PID);
      c.update(configuration.getProperties());
      configuration.setConfiguration(c);
    }
  }

  /**
   * Asks the factory to withdraw the given configuration from the configuration
   * admin service.
   * 
   * @param configuration
   *          the cache configuration
   * @throws IOException
   *           if withdrawing the configuration failed
   */
  public void disable(CacheConfiguration configuration) throws IOException {
    if (!configurations.containsValue(configuration))
      return;

    if (configuration.isEnabled()) {
      configuration.getConfiguration().delete();
      configuration.setConfiguration(null);
    }
  }

  /**
   * Creates and publishes configurations for this site using the
   * {@link ConfigurationAdmin}.
   * <p>
   * This method is called by the OSGi framework for every site that is
   * registered in the service registry.
   * 
   * @param site
   *          the site
   * @throws IOException
   *           if access to the persistent store fails
   */
  void addSite(Site site) throws IOException {
    sites.put(site.getIdentifier(), site);

    if (configurationAdmin == null)
      return;
    
    CacheConfiguration configHolder = new CacheConfiguration(site.getIdentifier(), site.getName());
    configHolder.setProperties(createConfiguration(site.getIdentifier(), site.getName()));

    // Create the initial properties
    Configuration configuration = configurationAdmin.createFactoryConfiguration(CacheServiceFactory.SERVICE_PID, null);
    configuration.update(configHolder.getProperties());
    configHolder.setConfiguration(configuration);

    configurations.put(site.getIdentifier(), configHolder);
  }

  /**
   * Removes the associated service configuration from
   * {@link ConfigurationAdmin}, so the cache service's
   * {@link org.osgi.service.cm.ManagedServiceFactory#deleted(String)} gets
   * called.
   * <p>
   * This method is called by the OSGi framework for every site that disappears
   * from the service registry.
   * 
   * @param site
   *          the site
   * @throws IOException
   *           if access to the persistent store fails
   */
  void removeSite(Site site) throws IOException {
    sites.remove(site.getIdentifier());

    // Delete the configuration
    CacheConfiguration configHolder = configurations.remove(site.getIdentifier());
    if (configHolder == null)
      return;
    
    Configuration config = configHolder.getConfiguration();
    config.delete();
  }

  /**
   * Sets the reference to the OSGi farmework's configuration admin service.
   * Once the service reference is obtained, we use it to load the default
   * configuration for cache instances and store it for further use.
   * 
   * @param configurationAdmin
   *          the configuration admin service
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
    this.configurationAdmin = configurationAdmin;

    // Try to get hold of the service configuration
    try {
      Configuration serviceConfig = configurationAdmin.getConfiguration(CacheConfigurationFactory.SERVICE_PID);
      if (serviceConfig != null && serviceConfig.getProperties() != null) {
        cacheConfiguration = serviceConfig.getProperties();
      } else {
        logger.debug("No customized cache configuration found");
        cacheConfiguration = new Hashtable();
      }
    } catch (IOException e) {
      logger.error("Error reading cache configuration from configuration admin service: " + e.getMessage());
    }

    // Process sites that appeared while there was no configuration admin
    // service around
    for (Site site : sites.values()) {
      if (!configurations.containsKey(site.getIdentifier())) {
        try {
          addSite(site);
        } catch (IOException e) {
          logger.error("Error adding cache configuration to the configuration admin service: " + e.getMessage());
        }
      }
    }

  }

}
