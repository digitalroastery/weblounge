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
import ch.o2it.weblounge.common.site.Site;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ManagedServiceFactory;
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
 * <code>ch.o2it.weblounge.cache</code>, th will be used as the basis for
 * configuration objects.
 */
public class CacheConfigurationFactory implements ManagedService {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(CacheConfigurationFactory.class);

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.o2it.weblounge.cache";

  /** Service configurations per site */
  private Map<Site, Configuration> configurations = new HashMap<Site, Configuration>();

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
    for (Map.Entry<Site, Configuration> entry : configurations.entrySet()) {
      try {
        Dictionary configuration = createConfiguration(entry.getKey());
        entry.getValue().update(configuration);
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
   * @param site
   *          the associated site
   * @return the configuration
   */
  private Dictionary<?, ?> createConfiguration(Site site) {
    Hashtable<Object, Object> configuration = new Hashtable<Object, Object>();

    // Add the default properties
    if (cacheConfiguration != null) {
      for (Enumeration<Object> e = cacheConfiguration.keys(); e.hasMoreElements();) {
        Object key = e.nextElement();
        configuration.put(key, cacheConfiguration.get(key));
      }
    }

    // Add everything that's site specific
    configuration.put(CacheServiceImpl.OPT_ID, site.getIdentifier());
    configuration.put(CacheServiceImpl.OPT_NAME, site.getName());
    configuration.put(CacheServiceImpl.OPT_DISKSTORE_PATH, PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", site.getIdentifier(), "cache"));

    return configuration;
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
    Configuration configuration = configurationAdmin.createFactoryConfiguration(CacheServiceFactory.SERVICE_PID, null);
    Dictionary<?, ?> properties = createConfiguration(site);
    configuration.update(properties);
    configurations.put(site, configuration);
  }

  /**
   * Removes the associated service configuration from
   * {@link ConfigurationAdmin}, so the cache service's
   * {@link ManagedServiceFactory#deleted(String)} gets called.
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
    Configuration config = configurations.remove(site);
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
      logger.error("Error reading cache configuration from configuraiton admin service: " + e.getMessage());
    }

  }

}
