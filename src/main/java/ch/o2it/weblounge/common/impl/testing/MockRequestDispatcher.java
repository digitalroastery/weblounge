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

package ch.o2it.weblounge.common.impl.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Mock implementation of the {@link javax.servlet.RequestDispatcher} interface.
 */
public class MockRequestDispatcher implements RequestDispatcher {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(MockRequestDispatcher.class);

  /** The dispatch url */
  private final String url;

  /**
   * Create a new MockRequestDispatcher for the given URL.
   * 
   * @param url
   *          the URL to dispatch to.
   */
  public MockRequestDispatcher(String url) {
    if (url == null)
      throw new IllegalArgumentException("URL must not be null");
    this.url = url;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse)
   */
  public void forward(ServletRequest request, ServletResponse response) {
    if (request == null)
      throw new IllegalArgumentException("Request must not be null");
    if (response == null)
      throw new IllegalArgumentException("Response must not be null");
    if (response.isCommitted()) {
      throw new IllegalStateException("Cannot perform forward - response is already committed");
    }
    getMockHttpServletResponse(response).setForwardedUrl(this.url);
    if (logger.isDebugEnabled()) {
      logger.debug("MockRequestDispatcher: forwarding to URL [" + this.url + "]");
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse)
   */
  public void include(ServletRequest request, ServletResponse response) {
    if (request == null)
      throw new IllegalArgumentException("Request must not be null");
    if (response == null)
      throw new IllegalArgumentException("Response must not be null");
    getMockHttpServletResponse(response).setIncludedUrl(this.url);
    if (logger.isDebugEnabled()) {
      logger.debug("MockRequestDispatcher: including URL [" + this.url + "]");
    }
  }

  /**
   * Obtain the underlying MockHttpServletResponse, unwrapping
   * {@link HttpServletResponseWrapper} decorators if necessary.
   */
  protected MockHttpServletResponse getMockHttpServletResponse(
      ServletResponse response) {
    if (response instanceof MockHttpServletResponse) {
      return (MockHttpServletResponse) response;
    }
    if (response instanceof HttpServletResponseWrapper) {
      return getMockHttpServletResponse(((HttpServletResponseWrapper) response).getResponse());
    }
    throw new IllegalArgumentException("MockRequestDispatcher requires MockHttpServletResponse");
  }

}
