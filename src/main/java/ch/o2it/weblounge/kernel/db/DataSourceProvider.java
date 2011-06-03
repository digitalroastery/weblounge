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

package ch.o2it.weblounge.kernel.db;

import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.sql.DataSource;

/**
 * JPAProvider registers a pooled {@link javax.sql.DataSource} in the service
 * registry on bundle startup, which can then be used throughout the system.
 * The datasource can be identified by the <code>issuer</code> service property
 * which is set to <code>weblounge</code>.
 * <p>
 * The provider can be configured using the OSGi configuration admin service
 * through the service pid <code>ch.o2it.weblounge.datasource</code> and listens
 * to these properties:
 * <ul>
 * <li><code>jdbc.driver</code></li>
 * <li><code>jdbc.url</code></li>
 * <li><code>jdbc.user</code></li>
 * <li><code>jdbc.password</code></li>
 * </ul>
 */
public class DataSourceProvider implements ManagedService {

  /** The logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(DataSourceProvider.class);

  /** Service pid */
  public static final String PID = "ch.o2it.weblounge.datasource";

  /** Prefix for jdbc configuration keys */
  public static final String JDBC_OPT_PREFIX = "jdbc.";

  /** Prefix for javax.persistence configuration keys */
  public static final String JAVAX_PERSISTANCE_OPT_PREFIX = "javax.persistence.";

  /** Prefix for EclipseLink configuration keys */
  public static final String ECLIPSELINK_OPT_PREFIX = "eclipselink.";

  /** Name of the jap vendor key */
  public static final String OPT_JPA_VENDOR = JDBC_OPT_PREFIX + "vendor";

  /** Name of the jap vendor key */
  public static final String OPT_JDBC_DRIVER = JDBC_OPT_PREFIX + "driver";

  /** Name of the jap vendor key */
  public static final String OPT_JDBC_URL = JDBC_OPT_PREFIX + "url";

  /** Name of the jap vendor key */
  public static final String OPT_JDBC_USER = JDBC_OPT_PREFIX + "username";

  /** Name of the jap vendor key */
  public static final String OPT_JDBC_PASSWORD = JDBC_OPT_PREFIX + "password";

  /** Default vendor */
  public static final String DEFAULT_VENDOR = "H2";

  /** Default jdbc driver */
  public static final String DEFAULT_JDBC_DRIVER = "org.h2.Driver";

  /** Default jdbc url */
  public static final String DEFAULT_JDBC_URL = "jdbc:h2:" + System.getProperty("java.io.tmpdir") + "/h2";

  /** Default jdbc username */
  public static final String DEFAULT_JDBC_USER = "sa";

  /** Default jdbc password */
  public static final String DEFAULT_JDBC_PASSWORD = "sa";

  /** The persistence properties service registration */
  protected ServiceRegistration propertiesRegistration = null;

  /** The datasource registration */
  protected ServiceRegistration datasourceRegistration = null;

  /** The registered datasource */
  protected ComboPooledDataSource dataSource = null;

  /** The bundle context */
  protected BundleContext bundleCtx = null;

  /** The jdbc vendor */
  protected String vendor = DEFAULT_VENDOR;

  /** The jdbc driver */
  protected String jdbcDriver = DEFAULT_JDBC_DRIVER;

  /** The jdbc url */
  protected String jdbcUrl = DEFAULT_JDBC_URL;

  /** The jdbc username */
  protected String jdbcUser = DEFAULT_JDBC_USER;

  /** The jdbc password */
  protected String jdbcPass = DEFAULT_JDBC_PASSWORD;

  /**
   * Callback from OSGi that is executed on service activation.
   * 
   * @param bundleContext
   *          the bundle context
   * @throws Exception
   *           if datasource creation fails
   */
  public void activate(BundleContext bundleContext) throws Exception {
    this.bundleCtx = bundleContext;
    Dictionary<?, ?> configuration = loadConfiguration(PID);
    if (configuration == null)
      configuration = new Hashtable<Object, Object>();
    updated(configuration);
  }

  /**
   * Callback from OSGi that is executed on service inactivation.
   * 
   * @param context
   * @throws Exception
   */
  public void deactivate(BundleContext context) throws Exception {
    unregisterDatasource();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  public void updated(Dictionary properties) throws ConfigurationException {

    jdbcDriver = (String) ConfigurationUtils.getValue(properties.get(OPT_JDBC_DRIVER), DEFAULT_JDBC_DRIVER);
    jdbcUrl = (String) ConfigurationUtils.getValue(properties.get(OPT_JDBC_URL), DEFAULT_JDBC_URL);
    jdbcUser = (String) ConfigurationUtils.getValue(properties.get(OPT_JDBC_USER), DEFAULT_JDBC_USER);
    jdbcPass = (String) ConfigurationUtils.getValue(properties.get(OPT_JDBC_PASSWORD), DEFAULT_JDBC_PASSWORD);

    try {
      unregisterDatasource();
      registerDatasource(properties);
    } catch (SQLException e) {
      throw new ConfigurationException("<unknown>", e.getMessage());
    } catch (PropertyVetoException e) {
      throw new ConfigurationException(OPT_JDBC_DRIVER, e.getMessage());
    }
  }

  /**
   * Creates a connection to the database configured database and registers a
   * pooled datasource in the service registry. Additionally, the relevant
   * properties as required by EclipseLink are registered as well.
   * 
   * @throws PropertyVetoException
   *           if the jdbc driver is unknown
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private synchronized void registerDatasource(Dictionary properties) throws SQLException,
      PropertyVetoException {

    // Create the datasource
    dataSource = new ComboPooledDataSource();
    dataSource.setDriverClass(jdbcDriver);
    dataSource.setJdbcUrl(jdbcUrl);
    dataSource.setUser(jdbcUser);
    dataSource.setPassword(jdbcPass);

    // Test if the connection is actually working
    Connection connection = null;
    try {
      logger.debug("Testing datasource at {}", jdbcUrl);
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      logger.error("Connection attempt to {} failed", jdbcUrl);
      logger.error("Exception: ", e);
      throw e;
    } finally {
      if (connection != null)
        connection.close();
    }

    // Register the connection in the service registry
    Dictionary dataSourceProperties = new Hashtable<String, String>();
    dataSourceProperties.put("issuer", "weblounge");
    datasourceRegistration = bundleCtx.registerService(DataSource.class.getName(), dataSource, dataSourceProperties);

    // Register the persistence properties
    Dictionary props = new Hashtable();
    props.put("type", "persistence");
    Enumeration<?> keys = properties.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      if (key.startsWith(JAVAX_PERSISTANCE_OPT_PREFIX) || key.startsWith(ECLIPSELINK_OPT_PREFIX)) {
        String value = (String) properties.get(key);
        logger.debug("Setting {} to '{}'", key, value);
        props.put(key, value);
      }
    }

    // Add the datasource
    props.put("javax.persistence.nonJtaDataSource", dataSource);

    // EclipseLink is looking for the properties map
    logger.debug("Registering eclipselink properties");
    propertiesRegistration = bundleCtx.registerService(Map.class.getName(), props, props);
    logger.info("Datasource {} established", jdbcUrl);
  }

  /**
   * Removes the datasource registration from the service registry.
   * 
   * @throws SQLException
   *           if destroying the datasource fails
   */
  private synchronized void unregisterDatasource() throws SQLException {
    if (propertiesRegistration != null) {
      logger.debug("Unregistering EclipseLink properties");
      propertiesRegistration.unregister();
      propertiesRegistration = null;
    }

    if (datasourceRegistration != null) {
      logger.info("Unregistering jpa datasource");
      datasourceRegistration.unregister();
      datasourceRegistration = null;
    }

    if (dataSource != null) {
      logger.debug("Releasing jpa datasource");
      dataSource.close();
      DataSources.destroy(dataSource);
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
        config = configurationAdmin.getConfiguration(pid);
        if (config != null)
          return (Dictionary<Object, Object>) config.getProperties();
      } catch (IOException e) {
        logger.error("Error trying to look up datasource configuration", e);
      }
    }

    return null;
  }

}
