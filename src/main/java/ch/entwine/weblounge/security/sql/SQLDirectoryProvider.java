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
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
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
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * TODO: Comment SQLDirectoryProvider
 */
public class SQLDirectoryProvider implements DirectoryProvider {

  /** The logging facility */
  private static final Logger log = LoggerFactory.getLogger(SQLDirectoryProvider.class);

  /** Identifier of this directory provider */
  private static final String PROVIDER_IDENTIFIER = "weblounge-sql-database";

  /** SQL query used to load a user for a specific site */
  private static final String SQL_QUERY_USER_ACCOUNT = "SELECT u.id, u.firstname, u.lastname, u.email, u.password FROM users u INNER JOIN user_accounts ua ON u.id = ua.user_id WHERE ua.enabled = true AND u.id = ? AND ua.site_id = ?";

  /** SQL query used to load the roles of a user account */
  private static final String SQL_QUERY_UA_ROLES = "SELECT r.context, r.rolename FROM user_account_roles r INNER JOIN user_accounts ua ON r.user_account_id =  ua.id WHERE ua.user_id = ? AND  ua.site_id = ?";

  private BundleContext context = null;

  /**
   * Callback to activate the component.
   * 
   * @param cc
   *          the declarative services component context
   */
  public void activate(ComponentContext cc) {
    context = cc.getBundleContext();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#loadUser(java.lang.String,
   *      ch.entwine.weblounge.common.site.Site)
   */
  public User loadUser(String login, Site site) {
    Connection conn = getDBConnection();
    User user = null;
    try {
      user = loadUser(login, site, conn);
      loadUserRoles(user, site, conn);
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        log.error(e.getMessage());
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
      }
    } catch (SQLException e) {
      log.error(e.getMessage());
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
      log.error(e.getMessage());
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
   * @see ch.entwine.weblounge.common.security.DirectoryProvider#getIdentifier()
   */
  public String getIdentifier() {
    return PROVIDER_IDENTIFIER;
  }

  /**
   * Returns a <code>ServiceReference</code> to the
   * <code>EntityManagerFactory</code>
   * 
   * @return The service reference
   * @throws Exception
   *           If no service reference could be found
   */
  private ServiceReference getEntityManagerFactoryServiceReference()
      throws Exception {
    String filter = "(" + EntityManagerFactoryBuilder.JPA_UNIT_NAME + "=security.sql)";
    ServiceReference[] sr;
    try {
      sr = context.getServiceReferences(EntityManagerFactory.class.getName(), filter);
    } catch (InvalidSyntaxException e) {
      log.error(e.getMessage());
      throw new Exception("EntityManagerFactory service could not be fetched.");
    }
    if (sr != null && sr.length > 0)
      return sr[0];
    else
      throw new Exception("EntityManagerFactory service is not available.");
  }

  private EntityManager getEntityManager() throws Exception {
    ServiceReference sr = getEntityManagerFactoryServiceReference();
    EntityManagerFactory emf = (EntityManagerFactory) context.getService(sr);
    return emf.createEntityManager();
  }

  private Connection getDBConnection() {
    String filter = "(osgi.jdbc.driver.class=com.mysql.jdbc.Driver)";
    ServiceReference[] sr;
    DataSourceFactory dsf = null;
    try {
      sr = context.getServiceReferences(DataSourceFactory.class.getName(), filter);
      if (sr != null && sr.length > 0) {
        dsf = (DataSourceFactory) context.getService(sr[0]);
      } else {
        log.error("No DataSourceFactory found.");
      }

    } catch (InvalidSyntaxException e) {
      log.error(e.getMessage());
      return null;
    }

    Driver driver = null;

    Properties driverProps = new Properties();
    // driverProps.put(DataSourceFactory.JDBC_URL,
    // "jdbc:mysql:/web.cce043z6xgme.eu-west-1.rds.amazonaws.com:3306/weblounge");
    driverProps.put(DataSourceFactory.JDBC_USER, "admin");
    driverProps.put(DataSourceFactory.JDBC_PASSWORD, "4gewinnt");

    try {
      driver = dsf.createDriver(null);
      return driver.connect("jdbc:mysql://web.cce043z6xgme.eu-west-1.rds.amazonaws.com:3306/weblounge", driverProps);
    } catch (SQLException e) {
      log.error(e.getMessage());
      return null;
    }
  }
}
