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

import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.security.User;

import org.apache.felix.webconsole.WebConsoleSecurityProvider2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

/**
 * Weblounge implementation that registers a security provider which will manage
 * access to the system console using the weblounge user directory.
 */
public class WebloungeWebConsoleSecurityProvider implements WebConsoleSecurityProvider2 {

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.webconsole.WebConsoleSecurityProvider2#authenticate(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public boolean authenticate(HttpServletRequest request,
      HttpServletResponse response) {
    User webloungeUser = SecurityUtils.getUser();
    boolean authenticated = SecurityUtils.userHasRole(webloungeUser, SystemRole.SYSTEMADMIN);
    if (!authenticated) {
      try {
        response.sendError(Status.FORBIDDEN.getStatusCode());
      } catch (IOException e) {
        // Never mind, the client is no longer interested
      }
    }
    return authenticated;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.webconsole.WebConsoleSecurityProvider#authenticate(java.lang.String,
   *      java.lang.String)
   */
  public Object authenticate(String username, String password) {
    User webloungeUser = SecurityUtils.getUser();
    return SecurityUtils.userHasRole(webloungeUser, SystemRole.SYSTEMADMIN);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.webconsole.WebConsoleSecurityProvider#authorize(java.lang.Object,
   *      java.lang.String)
   */
  public boolean authorize(Object user, String role) {
    User webloungeUser = SecurityUtils.getUser();
    return SecurityUtils.userHasRole(webloungeUser, SystemRole.SYSTEMADMIN);
  }

}
