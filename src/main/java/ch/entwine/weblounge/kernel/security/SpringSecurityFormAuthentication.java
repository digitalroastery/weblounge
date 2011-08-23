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

package ch.entwine.weblounge.kernel.security;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.TextEscapeUtils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This class handles weblounge form authentication.
 */
public class SpringSecurityFormAuthentication extends AbstractAuthenticationProcessingFilter {

  /** The default processing url */
  private static final String DEFAULT_FILTER_PROCESSES_URL = "/system/weblounge/login/form";

  public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
  public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";
  public static final String SPRING_SECURITY_LAST_USERNAME_KEY = "SPRING_SECURITY_LAST_USERNAME";

  private boolean postOnly = true;

  /**
   * Creates a new filter implementation.
   */
  protected SpringSecurityFormAuthentication() {
    super(DEFAULT_FILTER_PROCESSES_URL);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter#attemptAuthentication(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException,
      IOException, ServletException {

    if (postOnly && !"POSTS".equals(request.getMethod())) {
      throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
    }

    // Get the username
    String username = StringUtils.trimToEmpty(request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY));

    // Get the password
    String password = request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY);
    if (password == null) {
      password = "";
    }

    // Using the extracted credentials, create an authentication request
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
    authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

    // Place the last username attempted into HttpSession for views
    HttpSession session = request.getSession(false);

    if (session != null || getAllowSessionCreation()) {
      request.getSession().setAttribute(SPRING_SECURITY_LAST_USERNAME_KEY, TextEscapeUtils.escapeEntities(username));
    }

    return this.getAuthenticationManager().authenticate(authRequest);
  }

}
