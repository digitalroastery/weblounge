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
package ch.entwine.weblounge.security.sql.impl;

import static ch.entwine.weblounge.common.impl.security.SystemRole.EDITOR;
import static ch.entwine.weblounge.common.impl.security.SystemRole.GUEST;
import static ch.entwine.weblounge.common.impl.security.SystemRole.PUBLISHER;
import static ch.entwine.weblounge.common.impl.security.SystemRole.SITEADMIN;
import static ch.entwine.weblounge.common.impl.security.SystemRole.SYSTEMADMIN;

import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.WebloungeUserImpl;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.DirectoryService;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.Security;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.security.UserExistsException;
import ch.entwine.weblounge.common.security.UserShadowedException;
import ch.entwine.weblounge.common.security.WebloungeUser;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.security.sql.SQLDirectoryProvider;
import ch.entwine.weblounge.security.sql.SQLDirectoryProviderPersistence;
import ch.entwine.weblounge.security.sql.entities.JpaAccount;
import ch.entwine.weblounge.security.sql.entities.JpaRole;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This directory provider connects to a central database that can hold users
 * for multiple sites.
 */
public class SQLDirectoryProviderImpl implements SQLDirectoryProvider {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SQLDirectoryProviderImpl.class);

  /** Identifier of this directory provider */
  private static final String PROVIDER_IDENTIFIER = "weblounge-sql-database";

  /** The bundle context */
  private BundleContext bundleCtx = null;

  /** The persistence layer */
  private SQLDirectoryProviderPersistence persistence = null;

  /**
   * OSGi callback on component activation.
   * 
   * @param context
   *          the context
   */
  void activate(ComponentContext context) {
    this.bundleCtx = context.getBundleContext();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#loadUser(java.lang.String,
   *      ch.entwine.weblounge.common.site.Site)
   */
  public User loadUser(String login, Site site) {
    JpaAccount jpaAccount = null;

    // Load the user account and the user
    try {
      jpaAccount = persistence.getAccount(site.getIdentifier(), login, true);
    } catch (Throwable e) {
      logger.error("Error loading user '{}' from the database: {}", login, e.getMessage());
      return null;
    }

    // Is that user known
    if (jpaAccount == null) {
      logger.debug("User '{}' is not known in site '{}'", login, site.getIdentifier());
      return null;
    }

    // Create the weblounge user

    WebloungeUser user = new WebloungeUserImpl(login, site.getIdentifier());

    // Standard attributes like first name, name, ...
    if (StringUtils.isNotBlank(jpaAccount.getFirstname()))
      user.setFirstName(jpaAccount.getFirstname());
    if (StringUtils.isNotBlank(jpaAccount.getLastname()))
      user.setLastName(jpaAccount.getLastname());
    if (StringUtils.isNotBlank(jpaAccount.getEmail()))
      user.setEmail(jpaAccount.getEmail());
    if (StringUtils.isNotBlank(jpaAccount.getInitials()))
      user.setInitials(jpaAccount.getInitials());

    // Password
    user.addPrivateCredentials(new PasswordImpl(jpaAccount.getPassword(), DigestType.md5));

    // Roles
    for (JpaRole r : jpaAccount.getRoles()) {

      // Make sure weblounge roles get special treatment in order
      // to support role inheritance. Other directories will need
      // to implement this through a LoginListener implementation
      if (Security.SYSTEM_CONTEXT.equals(r.getContext())) {
        if (SYSTEMADMIN.getIdentifier().equals(r.getRolename())) {
          user.addPublicCredentials(SYSTEMADMIN);
        } else if (SITEADMIN.getIdentifier().equals(r.getRolename())) {
          user.addPublicCredentials(SITEADMIN);
        } else if (PUBLISHER.getIdentifier().equals(r.getRolename())) {
          user.addPublicCredentials(PUBLISHER);
        } else if (EDITOR.getIdentifier().equals(r.getRolename())) {
          user.addPublicCredentials(EDITOR);
        } else if (GUEST.getIdentifier().equals(r.getRolename())) {
          user.addPublicCredentials(GUEST);
        }
      } else {
        user.addPublicCredentials(new RoleImpl(r.getContext(), r.getRolename()));
      }
    }

    return user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getRoles()
   */
  public Role[] getRoles() {
    return new Role[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.DirectoryService#getLocalRole(ch.entwine.weblounge.common.security.Role)
   */
  public Role getLocalRole(Role role) {
    return role;
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#addAccount(ch.entwine.weblounge.common.site.Site,
   *      java.lang.String, String)
   */
  public JpaAccount addAccount(Site site, String user, String password)
      throws Exception {

    // Check for existing administrative accounts with the same login
    ServiceReference userDirectoryRef = bundleCtx.getServiceReference(DirectoryService.class.getName());
    if (userDirectoryRef != null) {
      DirectoryService systemDirectory = (DirectoryService) bundleCtx.getService(userDirectoryRef);
      logger.debug("Checking new site '{}' user '{}' for shadowing of site or system account");
      User shadowedUser = systemDirectory.loadUser(user, site);
      if (shadowedUser != null) {
        if (SecurityUtils.userHasRole(shadowedUser, SYSTEMADMIN))
          throw new UserShadowedException("Site '" + site.getIdentifier() + "' account '" + user + "' is shadowing the system account");
        else if (SecurityUtils.userHasRole(shadowedUser, SITEADMIN))
          throw new UserShadowedException("Site '" + site.getIdentifier() + "' account '" + user + "' is shadowing the site account");
        else
          throw new UserExistsException("Site '" + site.getIdentifier() + "' account '" + user + "' already exists");
      }
    } else {
      logger.warn("Directory service not found, site '{}' user '{}' cannot be checked for user shadowing", site.getIdentifier(), user);
    }
    
    return persistence.addAccount(site.getIdentifier(), user, password);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#removeAccount(ch.entwine.weblounge.common.site.Site,
   *      java.lang.String)
   */
  public void removeAccount(Site site, String login) throws Exception {
    persistence.removeAccount(site.getIdentifier(), login);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#updateAccount(ch.entwine.weblounge.security.sql.entities.JpaAccount)
   */
  public void updateAccount(JpaAccount account) throws Exception {
    persistence.updateAccount(account);
    logger.info("Account '{}@{}' has been updated", account.getLogin(), account.getSite().getName());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#getAccount(ch.entwine.weblounge.common.site.Site,
   *      java.lang.String)
   */
  public JpaAccount getAccount(Site site, String login) throws Exception {
    return getAccount(site, login, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#getAccount(ch.entwine.weblounge.common.site.Site,
   *      java.lang.String, boolean)
   */
  @Override
  public JpaAccount getAccount(Site site, String login, boolean enabledOnly)
      throws Exception {
    return persistence.getAccount(site.getIdentifier(), login, enabledOnly);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#getAccounts(Site)
   */
  @Override
  public List<JpaAccount> getAccounts(Site site) throws Exception {
    return persistence.getAccounts(site.getIdentifier());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#activateAccount(ch.entwine.weblounge.common.site.Site,
   *      java.lang.String, java.lang.String)
   */
  public boolean activateAccount(Site site, String login, String activationCode)
      throws Exception {
    if (StringUtils.isBlank(login))
      throw new IllegalArgumentException("Login must not be blank");
    if (StringUtils.isBlank(activationCode))
      throw new IllegalArgumentException("Activation code must not be blank");
    JpaAccount account = persistence.getAccount(site.getIdentifier(), login, true);
    if (account == null)
      return false;
    if (!activationCode.equals(account.getActivationCode()))
      return false;

    account.setEnabled(true);
    account.setActivationCode(null);
    persistence.updateAccount(account);
    logger.info("Account '{}' has been activated", login);
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#enableSite(ch.entwine.weblounge.common.site.Site)
   */
  public void enableSite(Site site) throws Exception {
    persistence.enableSite(site.getIdentifier());
    logger.info("Logins into site '{}' have been enabled", site.getIdentifier());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#disableSite(ch.entwine.weblounge.common.site.Site)
   */
  public void disableSite(Site site) throws Exception {
    persistence.disableSite(site.getIdentifier());
    logger.info("Logins into site '{}' have been disabled", site.getIdentifier());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#isSiteEnabled(ch.entwine.weblounge.common.site.Site)
   */
  @Override
  public boolean isSiteEnabled(Site site) throws Exception {
    return persistence.isSiteEnabled(site.getIdentifier());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#enableAccount(ch.entwine.weblounge.common.site.Site,
   *      java.lang.String)
   */
  public void enableAccount(Site site, String user) throws Exception {
    persistence.enableAccount(site.getIdentifier(), user);
    logger.info("Logins into account '{}@{}' have been enabled", user, site.getIdentifier());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#disableAccount(ch.entwine.weblounge.common.site.Site,
   *      java.lang.String)
   */
  public void disableAccount(Site site, String user) throws Exception {
    persistence.disableAccount(site.getIdentifier(), user);
    logger.info("Logins into account '{}@{}' have been disabled", user, site.getIdentifier());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.security.sql.SQLDirectoryProvider#isAccountEnabled(ch.entwine.weblounge.common.site.Site,
   *      java.lang.String)
   */
  @Override
  public boolean isAccountEnabled(Site site, String user) throws Exception {
    return persistence.isAccountEnabled(site.getIdentifier(), user);
  }

  /**
   * OSGi Declarative Services callback to set the persistence layer that is
   * instantiated using Blueprint Services.
   * 
   * @param persistence
   *          the persistence layer
   */
  void setPersistence(SQLDirectoryProviderPersistence persistence) {
    this.persistence = persistence;
  }

}
