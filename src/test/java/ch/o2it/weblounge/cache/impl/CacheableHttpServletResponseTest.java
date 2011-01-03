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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.cache.impl.handle.CacheHandleImpl;
import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.o2it.weblounge.common.request.CacheHandle;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;

/**
 * Test case for the implementation at {@link CacheableHttpServletResponse}.
 */
public class CacheableHttpServletResponseTest {

  /** The response under test */
  protected CacheableHttpServletResponse response = null;
  
  /** The current time */
  protected long time = System.currentTimeMillis();

  /** The expiration time */
  protected long expirationTime = time + Times.MS_PER_DAY;

  /** The recheck time */
  protected long recheckTime = time + Times.MS_PER_HOUR;

  /** The cache handle */
  protected CacheHandle handle = new CacheHandleImpl("/a/b/c", expirationTime, recheckTime);
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    response = new CacheableHttpServletResponse(new MockHttpServletResponse());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#startTransaction(ch.o2it.weblounge.common.request.CacheHandle, java.lang.String, ch.o2it.weblounge.cache.StreamFilter)}
   * .
   */
  @Test
  public void testStartTransaction() {
    CacheHandle hdl = new CacheHandleImpl("/a/b/c", expirationTime, recheckTime);
    CacheTransaction tx = response.startTransaction(hdl, null);
    assertNotNull(tx);
    assertEquals(hdl, tx.getHandle());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#getWriter()}
   * .
   */
  @Test
  public void testGetWriter() throws Exception {
    assertNotNull(response.getWriter());
    try {
      response.getOutputStream();
      fail();
    } catch (IllegalStateException e) {
      // This is expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#getOutputStream()}
   * .
   */
  @Test
  public void testGetOutputStream() throws Exception {
    assertNotNull(response.getOutputStream());
    try {
      response.getWriter();
      fail();
    } catch (IllegalStateException e) {
      // This is expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#endEntry(ch.o2it.weblounge.common.request.CacheHandle)}
   * .
   */
  @Test
  public void testEndEntry() throws Exception {
    response.endEntry(handle);
    ServletOutputStream os = response.getOutputStream();
    assertNotNull(os);
    os.write("Write test".getBytes());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#endOutput()}
   * .
   */
  @Test
  public void testEndOutput() {
    CacheTransaction tx = response.startTransaction(handle, null);
    CacheTransaction txEnd = response.endOutput();
    assertEquals(tx, txEnd);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#invalidate()}
   * .
   */
  @Test
  public void testInvalidate() {
    response.startTransaction(handle, null);
    assertTrue(response.isValid());
    response.invalidate();
    assertFalse(response.isValid());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#isValid()}
   * .
   */
  @Test
  public void testIsValid() {
    assertFalse(response.isValid());
    response.startTransaction(handle, null);
    assertTrue(response.isValid());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#setContentType(java.lang.String)}
   * .
   */
  @Test
  public void testSetContentTypeString() {
    response.startTransaction(handle, null);
    assertTrue(response.isValid());

  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#addHeader(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testAddHeaderStringString() {
    CacheTransaction tx = response.startTransaction(handle, null);
    String headerName = "Test-Header";
    response.addHeader(headerName, "testvalue");
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof String);
    response.addHeader(headerName, "othertestvalue");
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof List);
    assertEquals(2, ((List<?>)tx.getHeaders().getHeaders().get(headerName)).size());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#setHeader(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testSetHeaderStringString() {
    CacheTransaction tx = response.startTransaction(handle, null);
    String headerName = "Test-Header";
    String headerValue = "testvalue";
    String otherHeaderValue = "testvalue";

    response.setHeader(headerName, headerValue);
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof String);
    assertEquals(headerValue, tx.getHeaders().getHeaders().get(headerName));

    response.setHeader(headerName, otherHeaderValue);
    assertTrue(response.containsHeader(headerName));
    assertFalse(tx.getHeaders().getHeaders().get(headerName) instanceof List);
    assertEquals(otherHeaderValue, tx.getHeaders().getHeaders().get(headerName));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#addDateHeader(java.lang.String, long)}
   * .
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testAddDateHeaderStringLong() {
    CacheTransaction tx = response.startTransaction(handle, null);
    String headerName = "Test-Date-Header";
    Date headerValue = new Date();
    Date otherHeaderValue = new Date(123456789);
    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));

    response.addDateHeader(headerName, headerValue.getTime());
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof String);

    response.addDateHeader(headerName, otherHeaderValue.getTime());
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof List);
    List<String> headers = (List<String>)tx.getHeaders().getHeaders().get(headerName);
    assertEquals(2, headers.size());
    assertTrue(headers.contains(df.format(headerValue)));
    assertTrue(headers.contains(df.format(otherHeaderValue)));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#addIntHeader(java.lang.String, int)}
   * .
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testAddIntHeaderStringInt() {
    CacheTransaction tx = response.startTransaction(handle, null);
    String headerName = "Test-Header";
    int headerValue = 12345;
    int otherHeaderValue = 6789;

    response.addIntHeader(headerName, headerValue);
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof String);

    response.addIntHeader(headerName, otherHeaderValue);
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof List);
    List<String> headers = (List<String>)tx.getHeaders().getHeaders().get(headerName);
    assertEquals(2, headers.size());
    assertTrue(headers.contains(Integer.toString(headerValue)));
    assertTrue(headers.contains(Integer.toString(otherHeaderValue)));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#setDateHeader(java.lang.String, long)}
   * .
   */
  @Test
  public void testSetDateHeaderStringLong() {
    CacheTransaction tx = response.startTransaction(handle, null);
    String headerName = "Test-Date-Header";
    Date headerValue = new Date();
    Date otherHeaderValue = new Date(123456789);
    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));

    response.setDateHeader(headerName, headerValue.getTime());
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof String);
    String cachedDate = (String)tx.getHeaders().getHeaders().get(headerName);
    assertNotNull(cachedDate);
    assertEquals(df.format(headerValue), cachedDate);

    response.setDateHeader(headerName, otherHeaderValue.getTime());
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof String);
    cachedDate = (String)tx.getHeaders().getHeaders().get(headerName);
    assertEquals(df.format(otherHeaderValue), cachedDate);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#setIntHeader(java.lang.String, int)}
   * .
   */
  @Test
  public void testSetIntHeaderStringInt() {
    CacheTransaction tx = response.startTransaction(handle, null);
    String headerName = "Test-Header";
    int headerValue = 12345;
    int otherHeaderValue = 6789;

    response.setIntHeader(headerName, headerValue);
    assertTrue(response.containsHeader(headerName));
    assertTrue(tx.getHeaders().getHeaders().get(headerName) instanceof String);
    assertEquals(Integer.toString(headerValue), tx.getHeaders().getHeaders().get(headerName));

    response.setIntHeader(headerName, otherHeaderValue);
    assertTrue(response.containsHeader(headerName));
    assertFalse(tx.getHeaders().getHeaders().get(headerName) instanceof List);
    assertEquals(Integer.toString(otherHeaderValue), tx.getHeaders().getHeaders().get(headerName));
  }

}
