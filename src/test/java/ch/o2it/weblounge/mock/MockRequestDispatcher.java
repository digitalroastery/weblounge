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

package ch.o2it.weblounge.mock;

import static org.junit.Assert.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mock implementation of the {@link javax.servlet.RequestDispatcher} interface.
 */
public class MockRequestDispatcher implements RequestDispatcher {

  /** The logging facility */
  private static final Log logger = LogFactory.getLog(MockRequestDispatcher.class);

  /** The dispatch url */
  private final String url;

  /**
   * Create a new MockRequestDispatcher for the given URL.
   * 
   * @param url
   *          the URL to dispatch to.
   */
  public MockRequestDispatcher(String url) {
    assertNotNull(url, "URL must not be null");
    this.url = url;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse)
   */
  public void forward(ServletRequest request, ServletResponse response) {
    assertNotNull("Request must not be null", request);
    assertNotNull("Response must not be null", response);
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
    assertNotNull("Request must not be null", request);
    assertNotNull("Response must not be null", response);
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
