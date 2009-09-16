/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.security.ExtendedWebloungeUser;
import ch.o2it.weblounge.common.security.MalformedLoginException;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.security.UserListener;
import ch.o2it.weblounge.common.security.UserManager;
import ch.o2it.weblounge.common.security.WebloungeUser;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Manager used to add and remove users.
 * 
 * TODO: Decouple from xmldb database
 * 
 * @author Tobias Wunden <tobias.wunden@o2it.ch>
 * @version 2.0
 */

public final class UserManagerImpl implements UserManager {

  /** The associated site */
  private Site site;

  /** The user listeners */
  private List<UserListener> userListeners;

  /** The currently active users */
  private Map<String, WebloungeUser> activeUsers;

  /** The currently authenticated users */
  private Map<String, WebloungeUser> authenticatedUsers;

  // /** Query to create a user database */
  // private static XQuery createUserDBQuery_;
  //
  // /** Query to load users */
  // private static XQuery loadUserQuery_;
  //
  // /** Query to load users */
  // private static XQuery loadUserListQuery_;
  //
  // /** Query to add a user */
  // private static XQuery addUserQuery_;
  //
  // /** Query to enable a user */
  // private static XQuery enableUserQuery_;
  //
  // /** Query to disable a user */
  // private static XQuery disableUserQuery_;
  //
  // /** Query to update a user */
  // private static XQuery updateUserQuery_;
  //
  // /** Query to remove a user */
  // private static XQuery removeUserQuery_;

  /** Pattern used to match login names */
  // TODO: Fix pattern
  private final static Pattern loginPattern = Pattern.compile("^[A-Za-z0-9._%-]*");

  /** Minimum length for login names */
  private final static int LOGIN_LENGTH = 4;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String loggerClass = UserManagerImpl.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(loggerClass);

  static {
    // createUserDBQuery_ = XQuery.load(UserManagerImpl.class,
    // "/ch/o2it/weblounge/core/security/xq/CreateUserDB.xq");
    // loadUserQuery_ = XQuery.load(UserManagerImpl.class,
    // "/ch/o2it/weblounge/core/security/xq/GetUser.xq");
    // loadUserListQuery_ = XQuery.load(UserManagerImpl.class,
    // "/ch/o2it/weblounge/core/security/xq/GetUserList.xq");
    // addUserQuery_ = XQuery.load(UserManagerImpl.class,
    // "/ch/o2it/weblounge/core/security/xq/AddUser.xq");
    // enableUserQuery_ = XQuery.load(UserManagerImpl.class,
    // "/ch/o2it/weblounge/core/security/xq/EnableUser.xq");
    // disableUserQuery_ = XQuery.load(UserManagerImpl.class,
    // "/ch/o2it/weblounge/core/security/xq/DisableUser.xq");
    // updateUserQuery_ = XQuery.load(UserManagerImpl.class,
    // "/ch/o2it/weblounge/core/security/xq/UpdateUser.xq");
    // removeUserQuery_ = XQuery.load(UserManagerImpl.class,
    // "/ch/o2it/weblounge/core/security/xq/RemoveUser.xq");
  }

  /**
   * Creates a new user manager for the given site.
   * 
   * @param site
   *          the associated site
   */
  public UserManagerImpl(Site site) {
    this.site = site;
    this.userListeners = new ArrayList<UserListener>();
    this.activeUsers = new HashMap<String, WebloungeUser>();
    this.authenticatedUsers = new HashMap<String, WebloungeUser>();
  }

  /**
   * This method resets all cached user date.
   */
  public void reset() {
    // Do nothing
  }

  /**
   * Adds the listener to the list of user listeners.
   * 
   * @param listener
   *          the user listener to add
   */
  public void addUserListener(UserListener listener) {
    userListeners.add(listener);
  }

  /**
   * Removes the listener from the list of user listeners.
   * 
   * @param listener
   *          the listener to remove
   */
  public void removeUserListener(UserListener listener) {
    userListeners.remove(listener);
  }

  /**
   * Returns the user with the given login name or <code>null</code> if no such
   * user exists.
   * 
   * @param login
   *          the user's login name
   * @return the user
   */
  public WebloungeUser getUser(String login) {
    return new WebloungeUserProxy(login, site);
  }

  /**
   * Returns <code>true</code> if a registered user with the given login exists
   * in the database.
   * 
   * @param login
   *          the login
   * @return <code>true</code> if the user exists
   */
  public boolean exists(String login) {
    return loadUser(login) != null;
  }

  /**
   * Adds the given user to the user database. Make sure that the login is
   * unique over the given site.
   * 
   * @param user
   *          the user to add
   * @return <code>true</code> if the user could be added
   * @throws MalformedLoginException
   *           if the login name is malformed
   */
  public boolean addUser(WebloungeUser user) throws MalformedLoginException {
    // if (!exists(user.getLogin())) {
    // if (!checkLogin(user.getLogin())) {
    // log_.info("Attempt to create user with invalid login name '" +
    // user.getLogin() + "'");
    // throw new MalformedLoginException(user.getLogin(),
    // "Login must be at least " + LOGIN_LENGTH + " characters and match " +
    // loginPattern.pattern());
    // }
    // try {
    // addUserQuery_.execute(new Object[][] {
    // { "site", site.getIdentifier() },
    // { "user", user.toXml() },
    // { "context", "weblounge" },
    // { "dbuser", WebloungeAdmin.getInstance().getLogin() },
    // { "dbpass", WebloungeAdmin.getInstance().getPassword() }
    // });
    // } catch (Exception e) {
    // String msg = "Exception when trying to add user '" + user +
    // "' to the user database: " + e.getMessage();
    // log_.warn(msg, e);
    // return false;
    // }
    // } else {
    // site.getLogger().warn("Attempt to recreate existing user '" +
    // user.getLogin() + "'");
    // return false;
    // }
    // return true;
    return false;
  }

  /**
   * Enables the user.
   * 
   * @param user
   *          the user to enable
   * @return <code>true</code> if the user could be enabled
   */
  public boolean enableUser(WebloungeUser user) {
    // try {
    // enableUserQuery_.execute(new Object[][] {
    // { "site", site.getIdentifier() },
    // { "context", user.getLoginContext() },
    // { "userid", user.getLogin() },
    // });
    // return true;
    // } catch (Exception e) {
    // String msg = "Exception when trying to enable user '" + user + "': " +
    // e.getMessage();
    // site.getLogger().error(msg);
    // return false;
    // }
    return false;
  }

  /**
   * Disables the user.
   * 
   * @param user
   *          the user to disable
   * @return <code>true</code> if the user could be disabled
   */
  public boolean disableUser(WebloungeUser user) {
    // try {
    // disableUserQuery_.execute(new Object[][] {
    // { "site", site.getIdentifier() },
    // { "userid", user.getLogin() },
    // });
    // return true;
    // } catch (Exception e) {
    // String msg = "Exception when trying to disable user '" + user + "': " +
    // e.getMessage();
    // site.getLogger().error(msg);
    // return false;
    // }
    return false;
  }

  /**
   * Updates the given user in the user database.
   * 
   * @param user
   *          the user to update
   * @return the user
   */
  public boolean updateUser(WebloungeUser user) {
    // if (exists(user.getLogin())) {
    // try {
    // updateUserQuery_.execute(new Object[][] {
    // { "site", site.getIdentifier() },
    // { "context", WebloungeLoginModule.WEBLOUNGE_NS },
    // { "userid", user.getLogin() },
    // { "user", user.toXml() }
    // });
    // } catch (Exception e) {
    // String msg = "Exception when trying to update user '" + user + "': " +
    // e.getMessage();
    // log_.debug(msg, e);
    // site.getLogger().error(msg);
    // return false;
    // }
    // } else {
    // site.getLogger().warn("Attempt to update non existing user '" +
    // user.getLogin() + "'");
    // return false;
    // }
    // return true;
    return false;
  }

  /**
   * Removes the given user from the user database. All files belonging to this
   * user will be owned by the administrator.
   * 
   * @param user
   *          the user to be removed
   * @return the removed user
   */
  public WebloungeUser removeUser(WebloungeUser user) {
    // try {
    // removeUserQuery_.execute(new Object[][] {
    // { "site", site.getIdentifier() },
    // { "context", WebloungeLoginModule.WEBLOUNGE_NS },
    // { "userid", user.getLogin() }
    // });
    // } catch (Exception e) {
    // String msg = "Exception when trying to remove user '" + user + "': " +
    // e.getMessage();
    // log_.debug(msg, e);
    // site.getLogger().error(msg);
    // return null;
    // }
    // return user;
    return null;
  }

  /**
   * Loads the given user from the user database and returns it. If no such user
   * exists, <code>null</code> is returned.
   * 
   * @param login
   *          the user login
   * @param site
   *          the site
   * @return the user
   */
  public WebloungeUser loadUser(String login) {
    // WebloungeUser user = null;
    // try {
    // ResourceSet result = loadUserQuery_.execute(new Object[][] {
    // {"site", site.getIdentifier() },
    // {"context", WebloungeLoginModule.WEBLOUNGE_NS },
    // {"userid", login}
    // });
    // if (result == null || result.getSize() == 0) {
    // String msg = "No user with login '" + login +
    // "' was found in the database!";
    // log_.info(msg);
    // return null;
    // }
    // user = new WebloungeUserImpl((XMLResource)result.getResource(0), login,
    // site);
    // } catch (Exception e) {
    // String msg = "Exception when trying to load user '" + login + "': " +
    // e.getMessage();
    // log_.debug(msg, e);
    // site.getLogger().error(msg);
    // return null;
    // }
    // return user;
    return null;
  }

  /**
   * Loads the given user from the user database and returns it. If no such user
   * exists, <code>null</code> is returned.
   * 
   * @param login
   *          the user login
   * @return the user
   */
  public ExtendedWebloungeUser getExtendedUser(String login) {
    // ExtendedWebloungeUser user = null;
    // try {
    // ResourceSet result = loadUserQuery_.execute(new Object[][] {
    // {"site", site.getIdentifier() },
    // {"context", WebloungeLoginModule.WEBLOUNGE_NS },
    // {"userid", login}
    // });
    // if (result == null || result.getSize() == 0) {
    // String msg = "No user with login '" + login +
    // "' was found in the database!";
    // log_.info(msg);
    // return null;
    // }
    // user = new ExtendedWebloungeUserImpl((XMLResource)result.getResource(0),
    // login, site);
    // } catch (Exception e) {
    // String msg = "Exception when trying to load extended user '" + login +
    // "': " + e.getMessage();
    // log_.debug(msg, e);
    // site.getLogger().error(msg);
    // return null;
    // }
    // return user;
    return null;
  }

  /**
   * Loads the given user from the user database and returns it. If no such user
   * exists, <code>null</code> is returned.
   * 
   * @param login
   *          the user login
   * @return the user
   */
  public WebloungeUser[] getUserList() {
    // long start = System.currentTimeMillis();
    // try {
    // ResourceSet result = loadUserListQuery_.execute(new String[][] {
    // {"site", site.getIdentifier() },
    // {"context", WebloungeLoginModule.WEBLOUNGE_NS }
    // });
    // long time = System.currentTimeMillis() - start;
    // log_.debug("Loading users for site '" + site + "' took " + time + " ms");
    // if (result == null || result.getSize() == 0) {
    // String msg = "No users were found for site '" + site + "'";
    // log_.info(msg);
    // return new WebloungeUser[] {};
    // }
    // WebloungeUserListReader reader = new WebloungeUserListReader(site);
    // return reader.read((XMLResource)result.getResource(0));
    // } catch (Exception e) {
    // String msg = "Exception when trying to load user list: " +
    // e.getMessage();
    // log_.debug(msg, e);
    // site.getLogger().error(msg);
    // return null;
    // }
    return null;
  }

  /**
   * Creates a new user database.
   * 
   * @param site
   *          the site
   */
  public void createUserDatabase() throws Exception {
    // try {
    // createUserDBQuery_.execute(new String[][] {
    // { "dbuser", WebloungeAdmin.getInstance().getLogin() },
    // { "dbpass", WebloungeAdmin.getInstance().getPassword() },
    // { "site", site.getIdentifier() },
    // });
    // log_.info("User database created");
    // } catch (Exception e) {
    // String msg = "Exception when creating user database: " + e.getMessage();
    // log_.debug(msg, e);
    // site.getLogger().error(msg);
    // throw e;
    // }
  }

  /**
   * Returns <code>true</code> if the given login name matches the minimum
   * criterias for weblounge login names, such as:
   * <ul>
   * <li>The login name must start with a letter [a-z]</li>
   * <li>The login name must be at least 4 characters in length</li>
   * <li>The login name must consist of characters [a-z], [A-Z], [ . | - | _ | @
   * ] and digits</li>
   * </ul>
   * // DOCUMENT
   * 
   * @param login
   *          the login to test
   * @return <code>true</code> for valid login names
   */
  public boolean checkLogin(String login) {
    if (login == null)
      throw new IllegalArgumentException("Login may not be null");
    return login.length() >= LOGIN_LENGTH && loginPattern.matcher(login).matches();
  }

  /**
   * @see ch.o2it.weblounge.common.security.UserManager#getActiveUsers()
   */
  public WebloungeUser[] getActiveUsers() {
    synchronized (activeUsers) {
      return activeUsers.values().toArray(new WebloungeUser[activeUsers.size()]);
    }
  }

  /**
   * @see ch.o2it.weblounge.common.security.UserManager#isLoggedIn(java.lang.String)
   */
  public boolean isLoggedIn(String login) {
    WebloungeUser user = activeUsers.get(login);
    return user != null && user.isAuthenticated();
  }

  /**
   * Called if a new user is being noticed on the site.
   * 
   * @param user
   *          the user
   */
  public void userActivated(WebloungeUser user) {
    synchronized (activeUsers) {
      activeUsers.put(user.getLogin(), user);
    }
  }

  /**
   * Called if a new user is being noticed on the site.
   * 
   * @param user
   *          the user
   */
  public void userDeactivated(WebloungeUser user) {
    synchronized (activeUsers) {
      activeUsers.remove(user.getLogin());
    }
  }

  /**
   * This method is called if a user logged in.
   * 
   * @param user
   *          the user
   */
  public void login(WebloungeUser user) {
    authenticatedUsers.put(user.getLogin(), user);
    fireUserLoggedIn(user);
  }

  /**
   * This method is called if a user logged out.
   * 
   * @param user
   *          the user
   */
  public void logout(WebloungeUser user) {
    user.logout();
    authenticatedUsers.remove(user.getLogin());
    fireUserLoggedOut(user);
  }

  /**
   * This method is called if the user moves to another url.
   * 
   * @param user
   *          the user
   * @param url
   *          the new url
   */
  public void userMoved(User user, WebUrl url) {
    fireUserMoved(user, url);
  }

  /**
   * This method is called if a user moved around.
   * 
   * @param user
   *          the user that moved
   * @param url
   *          the url where the user moved to
   */
  protected void fireUserMoved(User user, WebUrl url) {
    for (int i = 0; i < userListeners.size(); i++) {
      userListeners.get(i).userMoved(user, url);
    }
  }

  /**
   * This method is called if a user is logged in.
   * 
   * @param user
   *          the user that logged in
   */
  protected void fireUserLoggedIn(User user) {
    for (int i = 0; i < userListeners.size(); i++) {
      userListeners.get(i).userLoggedIn(user);
    }
  }

  /**
   * This method is called if a user is logged out.
   * 
   * @param user
   *          the user that logged out
   */
  protected void fireUserLoggedOut(User user) {
    for (int i = 0; i < userListeners.size(); i++) {
      userListeners.get(i).userLoggedOut(user);
    }
  }

}