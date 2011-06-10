/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.security.jaas;

import ch.entwine.weblounge.common.security.AuthenticatedUser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * Abstract base class for login modules.
 */
public abstract class AbstractLoginModule implements LoginModule {

  /** Initial state */
  protected Subject subject = null;

  /** The callback handler */
  protected CallbackHandler callbackHandler = null;

  /** Authentication status */
  protected boolean succeeded = false;

  /** Commit status */
  protected boolean commitSucceeded = false;

  /** Login */
  protected String username = null;

  /** Password */
  protected byte[] password = null;

  /** The user */
  protected AuthenticatedUser user = null;

  /** The shared state information */
  protected Map<?,?> sharedState = null;

  /** The options map */
  protected Map<?,?> options = null;

  /**
   * Returns a namespace for this login context. The namespace is used to store
   * and identify user profile data in the weblounge database.
   * <p>
   * <b>Note</b>: the identifier has to consist of letters and characters that
   * are valid for database collection names. Use letters and digits to be on
   * the safe side.
   * 
   * @return the login module namespace
   */
  public abstract String getNamespace();

  /**
   * Initialize this <code>LoginModule</code>.
   * 
   * @param subject
   *          the <code>Subject</code> to be authenticated.
   * @param callbackHandler
   *          a <code>CallbackHandler</code> for communicating with the end user
   *          (prompting for user names and passwords, for example).
   * @param sharedState
   *          shared <code>LoginModule</code> state.
   * @param options
   *          options specified in the login <code>Configuration</code> for this
   *          particular <code>LoginModule</code>.
   */
  @SuppressWarnings("rawtypes")
  public void initialize(Subject subject, CallbackHandler callbackHandler,
      Map sharedState, Map options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;
    this.options = options;
    if (this.sharedState == null)
      this.sharedState = new HashMap<Object, Object>();
    if (this.options == null) {
      this.options = new HashMap<Object, Object>();
    }
  }

  /**
   * Authenticate the user by prompting for a user name and password.
   * 
   * @return true in all cases since this <code>LoginModule</code> should not be
   *         ignored.
   * @exception LoginException
   *              if this <code>LoginModule</code> is unable to perform the
   *              authentication.
   */
  public boolean login() throws LoginException {
    try {
      executeCallbacks();
    } catch (IOException e) {
      throw new LoginException(e.toString());
    } catch (LoginException e) {
      throw e;
    } catch (UnsupportedCallbackException e) {
      throw new LoginException("Error: " + e.getCallback() + " not available supported");
    }
    if (checkUserAndPassword()) {
      succeeded = true;
    }
    return succeeded;
  }

  /**
   * This method performs the actual login test. Return <code>true</code> if the
   * login succeeded, <code>false</code> if the user does not exist. Throw an
   * <code>LoginException</code> if the login operation failed.
   * 
   * @return the login state
   */
  protected abstract boolean checkUserAndPassword() throws LoginException;

  /**
   * Requests username and password using the login module's callback handler.
   */
  protected void executeCallbacks() throws IOException, LoginException,
      UnsupportedCallbackException {
    if (callbackHandler == null) {
      throw new LoginException("Error: no CallbackHandler available");
    }
    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("user name: ");
    callbacks[1] = new PasswordCallback("password: ", false);
    callbackHandler.handle(callbacks);

    // Get username
    username = ((NameCallback) callbacks[0]).getName();
    if (username == null) {
      throw new LoginException("Missing username");
    }

    // Get password
    char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
    if (tmpPassword == null) {
      tmpPassword = new char[0];
    }
    password = new byte[tmpPassword.length];
    System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
    ((PasswordCallback) callbacks[1]).clearPassword();
  }

  /**
   * <p>
   * This method is called if the LoginContext's overall authentication
   * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
   * LoginModules succeeded).
   * <p>
   * If this LoginModule's own authentication attempt succeeded (checked by
   * retrieving the private state saved by the <code>login</code> method), then
   * this method associates a <code>User</code> with the <code>Subject</code>
   * located in the <code>LoginModule</code>. If this LoginModule's own
   * authentication attempted failed, then this method removes any state that
   * was originally saved.
   * 
   * @exception LoginException
   *              if the commit fails.
   * @return true if this LoginModule's own login and commit attempts succeeded,
   *         or false otherwise.
   */
  public boolean commit() throws LoginException {
    if (!succeeded) {
      return false;
    } else {
      if (user != null && !subject.getPrincipals().contains(user)) {
        subject.getPrincipals().add(user);
      }
      // in any case, clean out state
      username = null;
      if (password != null) {
        for (int i = 0; i < password.length; i++)
          password[i] = ' ';
        password = null;
      }

      // save commit state
      commitSucceeded = true;
      return true;
    }
  }

  /**
   * This method is called if the LoginContext's overall authentication failed.
   * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did
   * not succeed).
   * <p>
   * If this LoginModule's own authentication attempt succeeded (checked by
   * retrieving the private state saved by the <code>login</code> and
   * <code>commit</code> methods), then this method cleans up any state that was
   * originally saved.
   * 
   * @exception LoginException
   *              if the abort fails.
   * @return false if this LoginModule's own login and/or commit attempts
   *         failed, and true otherwise.
   */
  public boolean abort() throws LoginException {
    if (!succeeded) {
      return false;
    } else if (succeeded && !commitSucceeded) {
      // login succeeded but overall authentication failed
      succeeded = false;
      username = null;
      if (password != null) {
        for (int i = 0; i < password.length; i++)
          password[i] = ' ';
        password = null;
      }
      user = null;
    } else {
      // overall authentication succeeded and commit succeeded,
      // but someone else's commit failed
      logout();
    }
    return true;
  }

  /**
   * Logout the user.
   * <p>
   * This method removes the <code>Principal</code> that was added by the
   * <code>commit</code> method.
   * 
   * @exception LoginException
   *              if the logout fails.
   * @return true in all cases since this <code>LoginModule</code> should not be
   *         ignored.
   */
  public boolean logout() throws LoginException {
    succeeded = commitSucceeded;
    username = null;
    if (password != null) {
      for (int i = 0; i < password.length; i++)
        password[i] = ' ';
      password = null;
    }

    // Remove user principal
    subject.getPrincipals().remove(user);
    user = null;
    return true;
  }

  /**
   * Returns the login name.
   * 
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the namespace as the module's string representation.
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getNamespace();
  }

}