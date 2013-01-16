/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.security.sql.impl;

import ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence;
import ch.entwine.weblounge.security.sql.entities.JpaAccount;
import ch.entwine.weblounge.security.sql.entities.JpaSite;

import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * Persistence layer for the SQL server based directory provider implementation.
 */
public class SQLDirectoryProviderPersistenceImpl implements SQLDirectoryProviderPersistence {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SQLDirectoryProviderPersistenceImpl.class);

  /** The entity manager */
  private EntityManager entityManager = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#addAccount(java.lang.String,
   *      java.lang.String, String)
   */
  public JpaAccount addAccount(String site, String login, String password)
      throws Exception {
    TypedQuery<JpaAccount> query = null;
    query = entityManager.createNamedQuery("getAccount", JpaAccount.class);
    query.setParameter("siteId", site);
    query.setParameter("userId", login);

    JpaAccount jpaAccount = null;

    try {
      jpaAccount = query.getSingleResult();
      logger.debug("User account already exists for user '{}'", login);
    } catch (NoResultException e) {
      JpaSite jpaSite = registerSite(site);
      jpaAccount = new JpaAccount(jpaSite, login, password);
      entityManager.persist(jpaAccount);
    }

    return jpaAccount;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#removeAccount(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void removeAccount(String site, String login) throws Exception {
    JpaAccount jpaAccount = getAccount(site, login, false);
    if (jpaAccount == null)
      return;
    entityManager.remove(jpaAccount);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#getAccount(java.lang.String,
   *      java.lang.String, boolean)
   */
  public JpaAccount getAccount(String site, String login, boolean activeOnly)
      throws Exception {
    TypedQuery<JpaAccount> query = null;
    if (activeOnly)
      query = entityManager.createNamedQuery("getActiveAccount", JpaAccount.class);
    else
      query = entityManager.createNamedQuery("getAccount", JpaAccount.class);
    query.setParameter("siteId", site);
    query.setParameter("userId", login);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#getAccounts(java.lang.String)
   */
  @Override
  public List<JpaAccount> getAccounts(String site) throws Exception {
    TypedQuery<JpaAccount> query = null;
    query = entityManager.createNamedQuery("getAccounts", JpaAccount.class);
    query.setParameter("siteId", site);
    return query.getResultList();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#updateAccount(ch.entwine.weblounge.security.sql.entities.JpaAccount)
   */
  @Override
  public void updateAccount(JpaAccount account) throws Exception {
    entityManager.merge(account);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#enableSite(java.lang.String)
   */
  public void enableSite(String site) throws Exception {
    toggleSite(site, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#disableSite(java.lang.String)
   */
  public void disableSite(String site) throws Exception {
    toggleSite(site, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#isSiteEnabled(java.lang.String)
   */
  @Override
  public boolean isSiteEnabled(String site) throws Exception {
    TypedQuery<JpaSite> query = null;
    query = entityManager.createNamedQuery("getSite", JpaSite.class);
    query.setParameter("site", site);

    JpaSite jpaSite = null;
    try {
      jpaSite = query.getSingleResult();
      return jpaSite.isEnabled();
    } catch (NoResultException e) {
      logger.info("Site entry '{}' created in user database", site);
      return false;
    }
  }

  /**
   * Enables or disables the site.
   * 
   * @param site
   *          the site
   * @param enable
   *          <code>true</code> to enable the site
   */
  private void toggleSite(String site, boolean enable) throws Exception {
    TypedQuery<JpaSite> query = null;
    query = entityManager.createNamedQuery("getSite", JpaSite.class);
    query.setParameter("site", site);
    JpaSite jpaSite = query.getSingleResult();
    if (jpaSite == null) {
      jpaSite = registerSite(site);
      logger.info("Site entry '{}' created in user database", site);
    }
    jpaSite.setEnabled(enable);
    entityManager.merge(jpaSite);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#enableAccount(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void enableAccount(String site, String user) throws Exception {
    toggleAccount(site, user, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#disableAccount(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void disableAccount(String site, String user) throws Exception {
    toggleAccount(site, user, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence#isAccountEnabled(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public boolean isAccountEnabled(String site, String user) throws Exception {
    JpaAccount jpaAccount = getAccount(site, user, false);
    if (jpaAccount == null)
      throw new IllegalStateException("Account does not exist");
    return jpaAccount.isEnabled();
  }

  /**
   * Enables or disables the account.
   * 
   * @param site
   *          the site
   * @param user
   *          the login name
   * @param enable
   *          <code>true</code> to enable the account
   */
  private void toggleAccount(String site, String user, boolean enable)
      throws Exception {
    JpaAccount jpaAccount = getAccount(site, user, false);
    if (jpaAccount == null)
      throw new IllegalStateException("Account does not exist");
    jpaAccount.setEnabled(enable);
    entityManager.persist(jpaAccount);
  }

  /**
   * Adds a new site to the database.
   * 
   * @param site
   *          the site
   */
  protected JpaSite registerSite(String site) {
    JpaSite jpaSite = null;
    try {
      jpaSite = entityManager.find(JpaSite.class, site);
      if (jpaSite == null) {
        jpaSite = new JpaSite(site);
        entityManager.persist(jpaSite);
        logger.info("Site entry '{}' created in user database", site);
      }
      return jpaSite;
    } catch (Throwable t) {
      logger.error("Unable to create site representation in user database: {}", t.getMessage());
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.digitalcafe.weblounge.zfv.ZFVPersistence#setEntityManager(javax.persistence.EntityManager)
   */
  public void setEntityManager(EntityManager em) throws ComponentException {
    if (em == null)
      throw new ComponentException("Entity manager must not be null");
    logger.debug("Entity manager for site 'zfv' connected");
    this.entityManager = em;
  }

}
