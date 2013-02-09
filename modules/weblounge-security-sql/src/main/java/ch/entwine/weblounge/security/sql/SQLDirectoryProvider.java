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
package ch.entwine.weblounge.security.sql;

import ch.entwine.weblounge.common.security.DirectoryProvider;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.security.sql.entities.JpaAccount;

import java.util.List;

/**
 * Definition of the directory provider that is backed by a SQL database engine.
 */
public interface SQLDirectoryProvider extends DirectoryProvider {

  /**
   * Adds an account for the given user in the specified site.
   * 
   * @param site
   *          the site
   * @param password
   *          the password
   * @param user
   *          the user login
   * @throws IllegalStateException
   *           if no user account with the given login exists
   * @throws Exception
   *           if creation of the user account fails
   */
  JpaAccount addAccount(Site site, String login, String password)
      throws Exception;

  /**
   * Removes the account for the given user in the specified site.
   * 
   * @param site
   *          the site
   * @param login
   *          the login
   * @throws Exception
   *           if removing the account from the database fails
   */
  void removeAccount(Site site, String login) throws Exception;

  /**
   * Loads the account from the given site if and only if the user identified by
   * <code>login</code> has an active account in that site and the site itself
   * has login enabled.
   * 
   * @param site
   *          the site
   * @param login
   *          the username
   * @return the account
   * @throws Exception
   *           if loading of the account fails
   */
  JpaAccount getAccount(Site site, String login) throws Exception;

  /**
   * Loads the account from the given site. If <code>enabledOnly</code> is set
   * to <code>true</code>, the account is returned if and only if the account is
   * enabled for logins as is the site.
   * 
   * @param site
   *          the site
   * @param login
   *          the username
   * @param enabledOnly
   *          only return accounts that are enabled for login
   * @return the account
   * @throws Exception
   *           if loading of the account fails
   */
  JpaAccount getAccount(Site site, String login, boolean enabledOnly)
      throws Exception;

  /**
   * Returns the site's accounts.
   * 
   * @param site
   *          TODO
   * 
   * @return the accounts
   * @throws Exception
   *           if loading of the accounts fails
   */
  List<JpaAccount> getAccounts(Site site) throws Exception;

  /**
   * Activates the given account using the activation code that was generated
   * when the account was created.
   * 
   * @param site
   *          the site
   * @param login
   *          the login
   * @param code
   *          the activation code
   * @return <code>true</code> if activation succeeded
   * @throws Exception
   *           if activation fails
   */
  boolean activateAccount(Site site, String login, String code)
      throws Exception;

  /**
   * Persists the updated account in the database.
   * 
   * @param account
   *          the account
   */
  void updateAccount(JpaAccount account) throws Exception;

  /**
   * Enables logins into the given site where the respective account login is
   * enabled, too.
   * 
   * @param site
   *          the site
   * @throws Exception
   *           if the site logins cannot be disabled
   */
  void enableSite(Site site) throws Exception;

  /**
   * Disables all logins into the site, regardless of the account's enabled
   * state.
   * 
   * @param site
   *          the site
   * @throws Exception
   *           if the site logins cannot be disabled
   */
  void disableSite(Site site) throws Exception;

  /**
   * Returns <code>true</code> if login into the given site is enabled.
   * 
   * @param site
   *          the site
   * @return <code>true</code> if the site is accepting logins
   * @throws Exception
   *           if the login status cannot be determined
   */
  boolean isSiteEnabled(Site site) throws Exception;

  /**
   * Enables logins into the given account.
   * 
   * @param site
   *          the site
   * @param user
   *          the login name
   * @throws Exception
   *           if the account cannot be disabled
   */
  void enableAccount(Site site, String user) throws Exception;

  /**
   * Disables login into the given account.
   * 
   * @param site
   *          the site
   * @param user
   *          the login name
   * @throws Exception
   *           if the account cannot be disabled
   */
  void disableAccount(Site site, String user) throws Exception;

  /**
   * Returns <code>true</code> if login into the given user account is enabled.
   * 
   * @param site
   *          the site
   * @param user
   *          the login name
   * @return <code>true</code> if the account is accepting logins
   * @throws Exception
   *           if the login status cannot be determined
   */
  boolean isAccountEnabled(Site site, String user) throws Exception;

}