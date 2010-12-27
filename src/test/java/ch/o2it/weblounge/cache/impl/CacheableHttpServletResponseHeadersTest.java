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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.common.impl.testing.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * Test case for the implementation at {@link CacheableHttpServletResponseHeaders}.
 */
public class CacheableHttpServletResponseHeadersTest {

  /** The response headers to be tested */
  protected CacheableHttpServletResponseHeaders headers = null;

  /** The header name */
  protected String headerName = "Test-Header";

  /** The header name for the multi value */
  protected String multivalueHeaderName = "Test-Multivalue-Header";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    headers = new CacheableHttpServletResponseHeaders();
    headers.setHeader(headerName, "headervalue");

    // Add single header value
    headers.setHeader(headerName, "testvalue");

    // Add multiple header values
    headers.addHeader(multivalueHeaderName, "testvalue");
    headers.addHeader(multivalueHeaderName, "othertestvalue");
  }

  /**
   * Test method for {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponseHeaders#apply(javax.servlet.http.HttpServletResponse)}.
   */
  @Test
  public void testApply() {
    HttpServletResponse response = new MockHttpServletResponse();
    headers.apply(response);
    assertTrue(response.containsHeader(headerName));
    assertTrue(response.containsHeader(multivalueHeaderName));
    assertFalse(response.containsHeader("Non-Existing-Header"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponseHeaders#containsHeader(java.lang.String)}.
   */
  @Test
  public void testContainsHeader() {
    assertTrue(headers.containsHeader(headerName));
    assertTrue(headers.containsHeader(multivalueHeaderName));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponseHeaders#getHeaders()}.
   */
  @Test
  public void testGetHeaders() {
    assertTrue(headers.containsHeader(headerName));
    assertTrue(headers.getHeaders().get(headerName) instanceof String);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponseHeaders#getHeaders()}.
   */
  @Test
  public void testGetMultivaluedHeaders() {
    assertTrue(headers.containsHeader(multivalueHeaderName));
    assertTrue(headers.getHeaders().get(multivalueHeaderName) instanceof List);
    assertEquals(2, ((List<?>)headers.getHeaders().get(multivalueHeaderName)).size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponseHeaders#size()}.
   */
  @Test
  public void testSize() {
    assertEquals(2, headers.getHeaders().size());
  }

}
