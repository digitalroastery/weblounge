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

import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;

import org.apache.commons.codec.binary.Base64;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This is an implementation of a JAAS callback handler which is able to extract
 * the needed login information from a <code>WebloungeRequest</code>.
 */
public class HttpAuthCallback extends WebloungeCallback {

  /** login parameter name */
  public static final String PARAM_LOGIN = "login";

  /** password parameter name */
  public static final String PARAM_PASSWORD = "password";

  /** The weblounge request containing the login information */
  private WebloungeRequest request;

  /** The weblounge response */
  private WebloungeResponse response;

  /** The username */
  protected String login;

  /** The password */
  protected String password;

  /**
   * Creates a new handler for the given request.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   */
  public HttpAuthCallback(WebloungeRequest request, WebloungeResponse response) {
    this(null, null, request, response);
  }

  /**
   * Creates a new handler for the given request, given login and password.
   * 
   * @param login
   *          the login
   * @param request
   *          the request
   * @param response
   *          the weblounge response
   */
  public HttpAuthCallback(String login, WebloungeRequest request,
      WebloungeResponse response) {
    this(login, null, request, response);
  }

  /**
   * Creates a new handler for the given request, given login and password.
   * 
   * @param login
   *          the login
   * @param password
   *          the password
   * @param request
   *          the request
   * @param response
   *          the weblounge response
   */
  public HttpAuthCallback(String login, String password,
      WebloungeRequest request, WebloungeResponse response) {
    super(request.getSite(), request.getSession());
    this.request = request;
    this.response = response;
    this.login = login;
    this.password = password;
  }

  /**
   * Handles the various callbacks used to perform a user login.
   * 
   * @param callbacks
   *          the callbacks
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
   * Returns the request.
   * 
   * @return the request
   */
  public WebloungeRequest getRequest() {
    return request;
  }

  /**
   * Returns the response.
   * 
   * @return the response
   */
  public WebloungeResponse getResponse() {
    return response;
  }

  /**
   * Returns the login or <code>null</code> if no login is stored in the
   * context.
   * 
   * @return the login
   */
  public String getLogin() {
    if (login != null) {
      return login;
    } else if (request.getParameter(PARAM_LOGIN) != null) {
      login = request.getParameter(PARAM_LOGIN);
      return login;
    } else {
      getLoginFromHeader();
      return login;
    }
  }

  /**
   * Returns the password or <code>null</code> if no pasword is stored in the
   * context.
   * 
   * @return the password
   */
  public String getPassword() {
    if (password != null) {
      return password;
    } else if (request != null && request.getParameter(PARAM_PASSWORD) != null) {
      password = request.getParameter(PARAM_PASSWORD);
      return password;
    } else {
      getLoginFromHeader();
      return password;
    }
  }

  /**
   * Extracts the login information from the <code>Authentication</code> header
   * of the request.
   */
  protected void getLoginFromHeader() {
    String header = getAuthorizationHeader();
    if (header != null) {
      int colon = header.indexOf(":");
      if (colon > 0 && colon < header.length()) {
        login = header.substring(0, colon);
        password = header.substring(colon + 1);
      }
    }
  }

  /**
   * Returns the decoded <code>Authorization</code> header or <code>null</code>
   * if no such header exists.
   * 
   * @return the authorization http header
   */
  public String getAuthorizationHeader() {
    if (request != null) {
      Base64 base64codec = new Base64();
      String base64 = request.getHeader("Authorization");
      if (base64 != null && base64.startsWith("Basic ") && base64.length() > 6) {
        base64 = base64.substring(6);
        return new String(base64codec.decode(base64.getBytes()));
      }
    }
    return null;
  }

  /**
   * Returns the remote ip address of the user trying to log in or the string
   * <code>unknown</code> if the information is not available.
   * 
   * @return the remote address
   */
  public String getRemoteAddress() {
    if (request != null) {
      String name = request.getRemoteHost();
      if (name != null) {
        return name;
      } else {
        return request.getRemoteAddr();
      }
    } else {
      return "<ip unknown>";
    }
  }

}