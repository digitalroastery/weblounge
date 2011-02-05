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

package ch.o2it.weblounge.contentrepository.impl;

import ch.o2it.weblounge.common.content.repository.ContentRepository;
import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.impl.fs.FileSystemContentRepository;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Service factory that will return a content repository for each configuration
 * that is published to the {@link org.osgi.service.cm.ConfigurationAdmin}.
 * <p>
 * The following properties need to be present in order for a content repository
 * instance to be started.
 * <ul>
 * <li><code>repository.id</code> - identifier that needs to match the site
 * identifier</li>
 * <li><code>repository.class</code> - class name of the repository
 * implementation</li>
 * </ul>
 * <p>
 * For additional configuration, take a look at the sample configuration that
 * comes with Weblounge. When registered with the system using the pid
 * <code>ch.o2it.weblounge.contentrepository</code>, it will be used as the
 * basis for configuration objects.
 */
public class ContentRepositoryServiceFactory implements ManagedServiceFactory, ManagedService {

  /** The factory's service pid */
  static final String SERVICE_PID = "ch.o2it.weblounge.contentrepository.factory";

  /** Name of the repository implementation option */
  static final String OPT_TYPE = "contentrepository.type";

  /** Default implementation of the content repository */
  static final String DEFAULT_REPOSITORY_TYPE = FileSystemContentRepository.class.getName();

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ContentRepositoryServiceFactory.class);

  /** Service registrations per configuration pid */
  private Map<String, ServiceRegistration> services = new HashMap<String, ServiceRegistration>();

  /** Default content repository type */
  private String repositoryType = null;

  /** This service factory's bundle context */
  private BundleContext bundleCtx = null;

  /**
   * Sets a reference to the service factory's component context.
   * <p>
   * This method is called from the OSGi context upon service creation.
   * 
   * @param ctx
   *          the component context
   */
  protected void activate(ComponentContext ctx) {
    this.bundleCtx = ctx.getBundleContext();

    // Configure the service
    Dictionary<?, ?> configuration = loadConfiguration(SERVICE_PID);
    if (configuration != null) {
      try {
        updated(configuration);
      } catch (ConfigurationException e) {
        logger.error("Error configuring content repository service factory", e);
      }
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#getName()
   */
  public String getName() {
    return "Content repository service factory";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {
    String repositoryType = (String) properties.get(OPT_TYPE);
    if (StringUtils.isBlank(repositoryType))
      repositoryType = DEFAULT_REPOSITORY_TYPE;
    if (!StringUtils.trimToEmpty(this.repositoryType).equals(repositoryType)) {
      logger.info("Default content repository implementation is {}", repositoryType);
      this.repositoryType = repositoryType;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String,
   *      java.util.Dictionary)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void updated(String pid, Dictionary properties)
      throws ConfigurationException {

    // is this an update to an existing service?
    if (services.containsKey(pid)) {
      ServiceRegistration registration = services.get(pid);
      ManagedService service = (ManagedService) bundleCtx.getService(registration.getReference());
      service.updated(properties);
    }

    // Create a new content repository service instance
    else {
      String className = (String) properties.get(OPT_TYPE);
      if (StringUtils.isBlank(className)) {
        className = repositoryType;
      }
      Class<ContentRepository> repositoryImplementation;
      ContentRepository repository = null;
      try {
        repositoryImplementation = (Class<ContentRepository>) Class.forName(className);
        repository = repositoryImplementation.newInstance();

        // If this is a managed service, make sure it's configured properly
        // before the site is connected
        
        if (repository instanceof ManagedService) {
          Dictionary<Object, Object> finalProperties = new Hashtable<Object, Object>();

          // Add the default configuration according to the repository type
          Dictionary<Object, Object> configuration = loadConfiguration(repository.getType());
          if (configuration != null) {
            for (Enumeration<Object> keys = configuration.keys(); keys.hasMoreElements();) {
              Object key = keys.nextElement();
              finalProperties.put(key, processPropertyTemplates(configuration.get(key)));
            }
          }

          // Overwrite the default configuration with what was passed in
          for (Enumeration<Object> keys = properties.keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            finalProperties.put(key, processPropertyTemplates(properties.get(key)));
          }

          // push the repository configuration
          ((ManagedService)repository).updated(finalProperties);
        }

        // Register the service
        String serviceType = ContentRepository.class.getName();
        properties.put("service.pid", pid);
        services.put(pid, bundleCtx.registerService(serviceType, repository, properties));
      } catch (ClassNotFoundException e) {
        throw new ConfigurationException(OPT_TYPE, "Repository implementation class " + className + " not found", e);
      } catch (InstantiationException e) {
        throw new ConfigurationException(OPT_TYPE, "Error instantiating repository implementation class " + className + " not found", e);
      } catch (IllegalAccessException e) {
        throw new ConfigurationException(OPT_TYPE, "Error accessing repository implementation class " + className + " not found", e);
      }

    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
   */
  public void deleted(String pid) {
    ServiceRegistration registration = services.remove(pid);
    ContentRepository repository = (ContentRepository) bundleCtx.getService(registration.getReference());
    registration.unregister();
    try {
      repository.disconnect();
    } catch (ContentRepositoryException e) {
      logger.warn("Error during content repository shutdown", e);
    }
  }

  /**
   * Connects to the configuration admin service and asks for the configuration
   * identified by <code>pid</code>. If the configuration exists, it's
   * properties will be returned, <code>null</code> otherwise.
   * 
   * @param pid
   *          the service pid
   * @return the configuration properties
   */
  @SuppressWarnings({ "unchecked", "cast" })
  private Dictionary<Object, Object> loadConfiguration(String pid) {
    if (StringUtils.isBlank(pid))
      return null;

    ServiceReference ref = bundleCtx.getServiceReference(ConfigurationAdmin.class.getName());
    if (ref != null) {
      ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleCtx.getService(ref);
      Configuration config;
      try {
        config = configurationAdmin.getConfiguration(SERVICE_PID);
        if (config != null)
          return (Dictionary<Object, Object>) config.getProperties();
      } catch (IOException e) {
        logger.error("Error trying to look up content repository service factory configuration", e);
      }
    }

    return null;
  }

  /**
   * Replaces templates inside the property value with their corresponding value
   * from the system properties and environment.
   * 
   * @param value
   *          the original property value
   * @return the processed value
   */
  private Object processPropertyTemplates(Object v) {
    if (v == null || !(v instanceof String))
      return v;
    
    String value = (String)v;
    
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

    return value;
  }

}
