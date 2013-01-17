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

import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.security.SecurityUtils;
import ch.entwine.weblounge.common.security.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This handler processes the login request once spring security is satisfied
 * with the credentials that have been provided.
 */
public class RoleBasedLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  /** The logging facility */
  private static Logger logger = LoggerFactory.getLogger(RoleBasedLoginSuccessHandler.class);

  /** Saved request key. Unfortunately, the spring constant is not accessible */
  private static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

  /** The security service */
  protected SecurityService securityService = null;

  /** The maps of roles to welcome pages */
  protected Map<String, String> welcomePages = new HashMap<String, String>();

  /** The default welcome page */
  protected String defaultWelcomePage = "/";

  /**
   * {@inheritDoc}
   * 
   * @see org.springframework.security.web.authentication.AuthenticationSuccessHandler#onAuthenticationSuccess(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.springframework.security.core.Authentication)
   */
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof SpringSecurityUser)) {
      super.onAuthenticationSuccess(request, response, authentication);
      return;
    }

    // Try to process login based on the user's role
    User user = ((SpringSecurityUser) principal).getUser();
    boolean isEditor = SecurityUtils.userHasRole(user, SystemRole.EDITOR);

    logger.info("User '{}' logged in", user);

    // Try to redirect the user to the initial url
    HttpSession session = request.getSession(false);
    if (session != null) {
      SavedRequest savedRequest = (SavedRequest) session.getAttribute(SAVED_REQUEST);
      if (savedRequest != null) {
        response.sendRedirect(addTimeStamp(savedRequest.getRedirectUrl()));
        return;
      }
    }

    // If the user was intending to edit a page, let him do just that
    if (isEditor && StringUtils.isNotBlank(request.getParameter("edit"))) {
      super.onAuthenticationSuccess(request, response, authentication);
      return;
    }

    // Try to send users to an appropriate welcome page based on their roles
    for (Map.Entry<String, String> entry : welcomePages.entrySet()) {
      String roleId = entry.getKey();
      String welcomePage = entry.getValue();
      if (SecurityUtils.userHasRole(user, new RoleImpl(roleId))) {
        response.sendRedirect(addTimeStamp(welcomePage));
        return;
      }
    }

    // No idea what the user wants or who he/she is. Send them back
    response.sendRedirect(addTimeStamp(defaultWelcomePage));

  }

  /**
   * Add a timestamp parameter to the url location
   * 
   * @param location
   *          the url
   * @return the page with a timestamp
   */
  private String addTimeStamp(String location) {
    long timeStamp = new Date().getTime();
    if (location.contains("?")) {
      return location.concat("&_=" + timeStamp);
    } else {
      return location.concat("?_=" + timeStamp);
    }
  }

  /**
   * Sets the welcome pages that have been configured in spring security.
   * 
   * @param welcomePages
   *          the welcomePages
   */
  public void setWelcomePages(Map<String, String> welcomePages) {
    this.welcomePages = welcomePages;
  }

  /**
   * Callback from spring security which will set the security service.
   * 
   * @param securityService
   *          the security service
   */
  public void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }

}
