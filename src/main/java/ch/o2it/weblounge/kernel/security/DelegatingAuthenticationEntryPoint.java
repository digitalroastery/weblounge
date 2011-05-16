/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.kernel.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A Spring Security authentication entry point implementation that delegates to
 * a default implementation unless a <code>X-Requested-Auth</code> header with a
 * value of <code>Digest</code> is present, in which case the request is
 * delegated to the digest authentication implementation.
 */
public final class DelegatingAuthenticationEntryPoint implements AuthenticationEntryPoint {

  /** HTTP header that identifies the requested authentication mechanism */
  public static final String REQUESTED_AUTH_HEADER = "X-Requested-Auth";

  /** Digest authentication header value */
  public static final String DIGEST_AUTH = "Digest";

  /** Constant identifier for the oauth signature */
  public static final String OAUTH_SIGNATURE = "oauth_signature";

  /** The authentication entry point to delegate regular users to */
  protected AuthenticationEntryPoint userEntryPoint = null;

  /** The authentication entry point to delegate digest users to */
  protected DigestAuthenticationEntryPoint digestEntryPoint = null;

  /**
   * {@inheritDoc}
   * 
   * @see org.springframework.security.web.AuthenticationEntryPoint#commence(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.springframework.security.core.AuthenticationException)
   */
  public void commence(HttpServletRequest request,
      HttpServletResponse response, AuthenticationException authException)
      throws IOException, ServletException {
    String authHeader = request.getHeader(REQUESTED_AUTH_HEADER);
    if (DIGEST_AUTH.equals(authHeader)) {
      digestEntryPoint.commence(request, response, authException);
    } else {
      userEntryPoint.commence(request, response, authException);
    }
  }

  /**
   * Sets the user entry point to delegate regular users to.
   * <p>
   * This method will be called by the Spring Security bean configuration
   * mechanism when the entry point definition is read from the security
   * configuration.
   * 
   * @param entryPoint
   *          the user entry point
   */
  public void setUserEntryPoint(AuthenticationEntryPoint entryPoint) {
    this.userEntryPoint = entryPoint;
  }

  /**
   * Sets the digest entry point to delegate users or systems to.
   * <p>
   * This method will be called by the Spring Security bean configuration
   * mechanism when the entry point definition is read from the security
   * configuration.
   * 
   * @param entryPoint
   *          the digest authentication entry point
   */
  public void setDigestAuthenticationEntryPoint(
      DigestAuthenticationEntryPoint entryPoint) {
    this.digestEntryPoint = entryPoint;
  }

}
