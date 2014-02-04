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

package ch.entwine.weblounge.kernel.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This filter is wrapping <code>HttpServletRequest</code>s in such a way that they feature the https scheme.
 */
public class HttpsRequestFilter implements Filter {

  /** Request header that is set when behind an SSL proxy */
  public static final String X_FORWARDED_SSL = "X-Forwarded-SSL";

  /** Value of the X-Forwarded-SSL header that activates request wrapping */
  public static final String X_FORWARDED_SSL_VALUE = "on";

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(HttpsRequestFilter.class);

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
   *      javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
  ServletException {
    HttpServletRequest httpReqquest = (HttpServletRequest) request;

    // Check if the forwarded SSL header is set
    if (X_FORWARDED_SSL_VALUE.equalsIgnoreCase(httpReqquest.getHeader(X_FORWARDED_SSL))) {
      logger.trace("Wrapping request to {} to force https", httpReqquest.getRequestURL());
      httpReqquest = new HttpsRequestWrapper(httpReqquest);
    }
    chain.doFilter(httpReqquest, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "HTTPS request filter";
  }

}
