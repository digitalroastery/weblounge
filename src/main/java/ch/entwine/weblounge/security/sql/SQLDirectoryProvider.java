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
package ch.entwine.weblounge.security.sql;

import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * This directory provider connects to a central database that can hold users
 * for multiple sites.
 * <p>
 * The database connection can be defined in
 * <code>sqldirectory.properties</code> and should be set up using the following
 * scheme:
 * 
 * <pre>
 * TODO: Add CREATE TABLE and CREATE INDEX statements
 * </pre>
 */
public class SQLDirectoryProvider implements DirectoryProvider, ManagedService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SQLDirectoryProvider.class);

  /** Identifier of this directory provider */
  private static final String PROVIDER_IDENTIFIER = "weblounge-sql-database";

  /** Service pid, used to look up the service configuration */
  public static final String SERVICE_PID = "ch.entwine.weblounge.sqldirectory";

  /** SQL query used to load a user for a specific site */
  private static final String SQL_QUERY_USER_ACCOUNT = "SELECT u.id, u.firstname, u.lastname, u.email, u.password FROM users u INNER JOIN user_accounts ua ON u.id = ua.user_id WHERE ua.enabled = true AND u.id = ? AND ua.site_id = ?";

  /** SQL query used to load the roles of a user account */
  private static final String SQL_QUERY_UA_ROLES = "SELECT r.context, r.rolename FROM user_account_roles r INNER JOIN user_accounts ua ON r.user_account_id =  ua.id WHERE ua.user_id = ? AND  ua.site_id = ?";

  /** Configuration option for the database driver */
  private static final String OPT_DB_DRIVER = "sqldirectory.driver";

  /** Configuration option for the database server and database */
  private static final String OPT_DB_URL = "sqldirectory.url";

  /** Configuration option for the database user */
  private static final String OPT_DB_USER = "sqldirectory.user";

  /** Configuration option for the database user */
  private static final String OPT_DB_PASSWORD = "sqldirectory.password";

  /** The bundle context */
  private BundleContext bundleCtx = null;

  /** The connection properties */
  private Properties connectionProperties = null;

  /** The driver configured to the directory database */
  private Driver databaseDriver = null;

  /**
   * Callback to activate the component.
   * 
   * @param cc
   *          the declarative services component context
   */
  public void activate(ComponentContext cc) {
    bundleCtx = cc.getBundleContext();
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

    // Disconnect from the current database
    disconnect(databaseDriver);
    databaseDriver = null;
    connectionProperties = null;

    // Read the updated properties
    String driver = StringUtils.trimToNull((String) properties.get(OPT_DB_DRIVER));
    String url = StringUtils.trimToNull((String) properties.get(OPT_DB_URL));
    String user = StringUtils.trimToNull((String) properties.get(OPT_DB_USER));
    String password = StringUtils.trimToNull((String) properties.get(OPT_DB_PASSWORD));

    if (driver == null || url == null) {
      logger.info("Database provider is not configured");
      return;
    }

    // Store url and properties
    connectionProperties = new Properties();
    connectionProperties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, driver);
    connectionProperties.put(DataSourceFactory.JDBC_URL, url);
    if (user != null)
      connectionProperties.put(DataSourceFactory.JDBC_USER, user);
    if (password != null)
      connectionProperties.put(DataSourceFactory.JDBC_PASSWORD, password);

    // Connect to the new database
    databaseDriver = connect(connectionProperties);
  }

  /**
   * Connects to the database and returns the database driver, which can be used
   * to create individual database connections.
   * 
   * @param properties
   *          the connection properties
   * @return the database driver
   */
  private Driver connect(Properties properties) {
    String url = (String) properties.get(DataSourceFactory.JDBC_URL);
    String driverClass = (String) properties.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);

    ServiceReference[] sr;
    DataSourceFactory dsf = null;
    String filter = "(osgi.jdbc.driver.class=" + driverClass + ")";
    try {
      sr = bundleCtx.getServiceReferences(DataSourceFactory.class.getName(), filter);
      if (sr == null || sr.length == 0) {
        logger.warn("No Data source factory found for class {} and user directory {}", driverClass, url);
        return null;
      }
      dsf = (DataSourceFactory) bundleCtx.getService(sr[0]);
    } catch (InvalidSyntaxException e) {
      logger.error(e.getMessage());
      return null;
    }

    // Create the driver
    Driver driver = null;
    try {
      driver = dsf.createDriver(properties);
      logger.info("Connected to user directory {}", url);
    } catch (SQLException e) {
      logger.debug("Connection to user directory {} failed: {}", url, e.getMessage());
      try {
        driver = dsf.createDriver(null);
        logger.info("Connected to user directory {}", url);
      } catch (SQLException e1) {
        logger.warn("Connection to user directory {} failed: {}", url, e.getMessage());
      }
    }

    return driver;
  }

  /**
   * Disconnects from the datasource.
   * 
   * @param driver
   *          the database driver
   */
  private void disconnect(Driver driver) {
    if (driver == null)
      return;
    logger.info("Weblounge database provider disconnected");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#loadUser(java.lang.String,
   *      ch.entwine.weblounge.common.site.Site)
   */
  public User loadUser(String login, Site site) {
    Connection conn = getDBConnection();
    if (conn == null) {
      return null;
    }

    User user = null;
    try {
      user = loadUser(login, site, conn);
      if (user != null)
        loadUserRoles(user, site, conn);
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        logger.error(e.getMessage());
      }
    }

    return user;
  }

  private User loadUser(String login, Site site, Connection conn) {
    ResultSet rs = null;
    User user = null;
    try {
      PreparedStatement ps = conn.prepareStatement(SQL_QUERY_USER_ACCOUNT);
      ps.setString(1, login);
      ps.setString(2, site.getIdentifier());
      rs = ps.executeQuery();

      if (rs.next()) {
        user = new UserImpl(login, "weblounge", rs.getString(2) + " " + rs.getString(3));
        user.addPrivateCredentials(new PasswordImpl(rs.getString(5), DigestType.plain));
        user.addPublicCredentials(SystemRole.GUEST);
      }
    } catch (SQLException e) {
      logger.error(e.getMessage());
      return null;
    }
    return user;
  }

  private void loadUserRoles(User user, Site site, Connection conn) {
    ResultSet rs = null;
    try {
      PreparedStatement ps = conn.prepareStatement(SQL_QUERY_UA_ROLES);
      ps.setString(1, user.getLogin());
      ps.setString(2, site.getIdentifier());
      rs = ps.executeQuery();

      while (rs.next()) {
        user.addPublicCredentials(new RoleImpl(rs.getString(1), rs.getString(2)));
      }
    } catch (SQLException e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getRoles()
   */
  public Role[] getRoles() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getLocalRole(ch.entwine.weblounge.common.security.Role)
   */
  public Role getLocalRole(Role role) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getSystemRoles(ch.entwine.weblounge.common.security.Role)
   */
  public Role[] getSystemRoles(Role role) {
    return new Role[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryProvider#getIdentifier()
   */
  public String getIdentifier() {
    return PROVIDER_IDENTIFIER;
  }

  /**
   * Returns the directory's entity manager or <code>null</code> if the entity
   * manager is not available.
   * 
   * @return the entity manager
   */
  protected EntityManager getEntityManager() {
    String filter = "(" + EntityManagerFactoryBuilder.JPA_UNIT_NAME + "=security.sql)";
    ServiceReference[] sr;

    try {
      sr = bundleCtx.getServiceReferences(EntityManagerFactory.class.getName(), filter);
    } catch (InvalidSyntaxException e) {
      throw new IllegalStateException("EntityManagerFactory service could not be fetched using filter '" + filter + "'");
    }

    if (sr == null || sr.length == 0) {
      logger.warn("EntityManagerFactory service is not available.");
      return null;
    }

    EntityManagerFactory emf = (EntityManagerFactory) bundleCtx.getService(sr[0]);
    return emf.createEntityManager();
  }

  /**
   * Gets a connection from the database and returns it. If no connection is
   * available, <code>null</code> is returned instead.
   * 
   * TODO: Add pooling
   * 
   * @return the connection or <code>null</code> if no connection is available
   */
  protected Connection getDBConnection() {
    if (databaseDriver == null)
      return null;
    String url = connectionProperties.getProperty(DataSourceFactory.JDBC_URL);
    try {
      return databaseDriver.connect(url, connectionProperties);
    } catch (SQLException e) {
      logger.error(e.getMessage());
      return null;
    }
  }
}
