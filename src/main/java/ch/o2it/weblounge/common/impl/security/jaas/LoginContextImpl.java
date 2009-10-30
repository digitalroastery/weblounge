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

package ch.o2it.weblounge.common.impl.security.jaas;

import ch.o2it.weblounge.common.security.AuthenticationModule;
import ch.o2it.weblounge.common.security.LoginContext;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public final class LoginContextImpl implements LoginContext {

  /** The "required" relevance value */
  public static final String RELVANCE_REQUIRED = "required";

  /** The "requisite" relevance value */
  public static final String RELVANCE_REQUISITE = "requisite";

  /** The "sufficient" relevance value */
  public static final String RELVANCE_SUFFICIENT = "sufficient";

  /** The "optional" relevance value */
  public static final String RELVANCE_OPTIONAL = "optional";

  /** The username */
  private String username_ = null;

  /** The associated site */
  private Site site_ = null;

  /** The subject */
  private Subject subject_ = null;

  /** The callback used to gather username and password */
  private CallbackHandler callback_ = null;

  /** The authentication modules */
  private AuthenticationModule[] moduleDefinitions_;

  /** The instantiated login modules */
  private Map<LoginModule, AuthenticationModule> modules_ = null;

  /** True if login succeeded */
  private boolean success_ = false;

  /** True if at least one of the required modules has failed */
  private boolean failedRequired_ = false;

  /** True if at least one of the requisite modules has failed */
  private boolean failedRequisite_ = false;

  /** True if at least one of the sufficient modules has failed */
  private boolean failedSufficient_ = false;

  /** True if at least one of the optional modules has failed */
  private boolean failedOptional_ = false;

  // Logging

  /** the class name, used for the logging facility */
  private final static String loggerClass = LoginContextImpl.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(loggerClass);

  /**
   * Creates a new authentication context for the given site and
   * HttpAuthCallback.
   * 
   * @param site
   *          the associated site
   */
  public LoginContextImpl(String username, Site site, CallbackHandler callback) {
    username_ = username;
    site_ = site;
    subject_ = new Subject();
    callback_ = callback;
  }

  /**
   * Tries to log the subject in.
   * 
   * @throws LoginException
   */
  public void login() throws LoginException {
    Map<String, ?> sharedState = new HashMap<String, Object>();
    List<LoginFailure> loginFailures = null;
    moduleDefinitions_ = site_.getAuthenticationModules();
    modules_ = new HashMap<LoginModule, AuthenticationModule>();

    // If there is not a single login module, then we are unable to login
    // a user:

    if (moduleDefinitions_.length == 0) {
      throw new LoginException("No login modules have been defined!");
    }

    // Instantiate and initialize all modules
    for (AuthenticationModule definition : moduleDefinitions_) {
      Class c = null;
      try {
        c = Class.forName(definition.getModuleClass());
        LoginModule module = (LoginModule) c.newInstance();
        module.initialize(subject_, callback_, sharedState, definition.getOptions());
        modules_.put(module, definition);
      } catch (ClassNotFoundException e) {
        throw new LoginException("Login module class " + definition.getModuleClass() + " not found");
      } catch (NoClassDefFoundError e) {
        String msg = "Class '" + e.getMessage() + "' which is required by login module class '" + definition.getModuleClass() + "' was not found";
        throw new LoginException(msg);
      } catch (InstantiationException e) {
        throw new LoginException("Unable to instantiate login module class " + definition.getModuleClass());
      } catch (IllegalAccessException e) {
        throw new LoginException("Unable to access login module class " + definition.getModuleClass());
      }
    }

    // Try to login in using all of the registered modules

    for (Map.Entry<LoginModule, AuthenticationModule> m : modules_.entrySet()) {
      LoginModule module = m.getKey();
      AuthenticationModule definition = m.getValue();
      try {
        if (module.login()) {
          success_ = true;
          if (RELVANCE_SUFFICIENT.equals(definition.getRelevance())) {
            break;
          }
        }
      } catch (LoginException ex) {
        if (loginFailures == null)
          loginFailures = new ArrayList<LoginFailure>();
        loginFailures.add(new LoginFailure(module, ex));
        if (RELVANCE_REQUIRED.equals(definition.getRelevance())) {
          log_.info("Login to required authentication module " + definition.getModuleClass() + " failed");
          failedRequired_ = true;
        } else if (RELVANCE_REQUISITE.equals(definition.getRelevance())) {
          log_.info("Login to requisite authentication module " + definition.getModuleClass() + " failed");
          failedRequisite_ = true;
          break;
        } else if (RELVANCE_SUFFICIENT.equals(definition.getRelevance())) {
          log_.info("Login to sufficient authentication module " + definition.getModuleClass() + " failed");
          failedSufficient_ = true;
        } else if (RELVANCE_OPTIONAL.equals(definition.getRelevance())) {
          log_.info("Login to optional authentication module " + definition.getModuleClass() + " failed");
          failedOptional_ = true;
        }
      }
    }

    // Check whether the login procedure was successful

    if (!success_ || failedRequired_ || failedRequisite_) {
      log_.info("Authentication process failed because of required or requisite module");
      for (LoginModule module : modules_.keySet()) {
        module.abort();
      }
      if (loginFailures != null)
        if (loginFailures.size() == 1)
          throw loginFailures.get(0).getCause();
        else {
          for (LoginFailure failure : loginFailures) {
            log_.warn("Login for '" + username_ + "' through module '" + failure.getModule() + "' failed: " + failure.getCause().getMessage());
          }
          throw new FailedLoginException("All login modules failed");
        }
      else
        throw new FailedLoginException("User unknown");
    } else if (failedSufficient_) {
      log_.info("Authentication process succeeded despite sufficient module failure");
    } else if (failedOptional_) {
      log_.info("Authentication process succeeded despite optional module failure");
    } else {
      log_.info("Authentication process succeeded without failure");
    }

    // Have modules commit the login

    try {
      for (LoginModule module : modules_.keySet()) {
        module.commit();
      }
    } catch (LoginException e) {
      log_.info("Authentication commit failed");
      for (LoginModule module : modules_.keySet()) {
        module.abort();
      }
    }
    moduleDefinitions_ = null;
  }

  /**
   * Returns the subject.
   * 
   * @return the subject
   */
  public Subject getSubject() {
    return subject_;
  }

  /**
   * Logs the user out of the site.
   */
  public void logout() {
    for (LoginModule module : modules_.keySet()) {
      try {
        module.logout();
      } catch (Exception e) {
        site_.getLogger().warn("Logout of " + subject_ + " failed for " + module);
      }
    }
  }

  /**
   * This utility class is used to keep pairs of login modules and the
   * exceptions that were thrown when the module failed to log in the current
   * user.
   * 
   * @author Tobias Wunden
   * @version 1.0
   */
  private class LoginFailure {

    /** The failing module */
    LoginModule module = null;

    /** Reason of failure */
    LoginException exception = null;

    /**
     * Creates a new login failure for the given module and reason.
     * 
     * @param module
     *          the failling module
     * @reason the reason of failure
     */
    LoginFailure(LoginModule module, LoginException exception) {
      this.module = module;
      this.exception = exception;
    }

    /**
     * Returns the exception which was the reason for the failure.
     * 
     * @return the exception
     */
    public LoginException getCause() {
      return exception;
    }

    /**
     * Returns the login module which failed to log in.
     * 
     * @return the module
     */
    public LoginModule getModule() {
      return module;
    }

  }

}