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

import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

/**
 * TODO: Comment SQLDirectoryProvider
 */
public class SQLDirectoryProvider implements DirectoryProvider {

  /** The logging facility */
  private static final Logger log = LoggerFactory.getLogger(SQLDirectoryProvider.class);

  private static final String PERSISTENCE_UNIT_NAME = "security-sql";
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
    EntityManager em;
    try {
      em = getEntityManager();
    } catch (Exception e) {
      log.error("Error loading user from database.");
      return null;
    }
    Query usersQuery = em.createQuery("SELECT u FROM users u");
    List users = usersQuery.getResultList();

    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
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
    String filter = EntityManagerFactoryBuilder.JPA_UNIT_NAME + PERSISTENCE_UNIT_NAME;
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

}
