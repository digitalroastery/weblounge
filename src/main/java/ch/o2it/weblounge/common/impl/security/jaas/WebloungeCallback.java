/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.security.jaas;

import ch.o2it.weblounge.common.site.Site;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.http.HttpSession;

public abstract class WebloungeCallback implements CallbackHandler {

  /** The site */
  private Site site_;

  /** The users's session */
  private HttpSession session_;

  /**
   * Creates a new callback handler with a reference to the current site and
   * session.
   * 
   * @param site
   *          the site
   * @param session
   *          the users's session
   */
  public WebloungeCallback(Site site, HttpSession session) {
    if (site == null)
      throw new IllegalArgumentException("Site must not be null!");
    if (session == null)
      throw new IllegalArgumentException("Session must not be null!");
    site_ = site;
    session_ = session;
  }

  /**
   * Returns the site.
   * 
   * @return the site
   */
  public Site getSite() {
    return site_;
  }

  /**
   * Returns the user's session.
   * 
   * @return the session
   */
  public HttpSession getSession() {
    return session_;
  }

  /**
   * Handles the varous callbacks used to perform a user login.
   * 
   * @param callbacks
   *          the callbacks
   * @throws IOException
   * @throws UnsupportedCallbackException
   */
  public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
    if (callbacks == null)
      return;
    for (int i = 0; i < callbacks.length; i++) {
      if (callbacks[i] instanceof NameCallback) {
        ((NameCallback) callbacks[i]).setName(getLogin());
      } else if (callbacks[i] instanceof PasswordCallback) {
        String password = getPassword();
        if (password != null) {
          ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
        }
      } else {
        throw new UnsupportedCallbackException(callbacks[i]);
      }
    }
  }

  /**
   * Returns the login.
   * 
   * @return the login
   */
  public abstract String getLogin();

  /**
   * Returns the password.
   * 
   * @return the password
   */
  public abstract String getPassword();

}