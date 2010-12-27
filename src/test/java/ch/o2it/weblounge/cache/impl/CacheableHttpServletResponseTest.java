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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.cache.impl.handle.TaggedCacheHandle;
import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.request.CacheTagImpl;
import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.o2it.weblounge.common.request.CacheHandle;
import ch.o2it.weblounge.common.request.CacheTag;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for the implementation at {@link CacheableHttpServletResponse}.
 */
@Ignore
public class CacheableHttpServletResponseTest {

  /** The response under test */
  protected CacheableHttpServletResponse response = null;
  
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
    long time = System.currentTimeMillis();
    String cache = "cache";
    CacheTagSet tags = new CacheTagSet(new CacheTag[] { new CacheTagImpl("a", "b") });
    CacheHandle hdl = new TaggedCacheHandle(tags.getTags(), time + Times.MS_PER_DAY, time + Times.MS_PER_HOUR);
    CacheTransaction tx = response.startTransaction(hdl, cache, null);
    assertNotNull(tx);
    assertEquals(hdl, tx.getHandle());
    assertEquals(cache, tx.getCache());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#getWriter()}
   * .
   */
  @Test
  public void testGetWriter() throws Exception {
    assertNotNull(response.getWriter());
    response.getOutputStream();
    fail();
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#getOutputStream()}
   * .
   */
  @Test
  public void testGetOutputStream() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#endEntry(ch.o2it.weblounge.common.request.CacheHandle)}
   * .
   */
  @Test
  public void testEndEntry() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#endOutput()}
   * .
   */
  @Test
  public void testEndOutput() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#invalidate()}
   * .
   */
  @Test
  public void testInvalidate() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#isValid()}
   * .
   */
  @Test
  public void testIsValid() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#setContentType(java.lang.String)}
   * .
   */
  @Test
  public void testSetContentTypeString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#addHeader(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testAddHeaderStringString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#setHeader(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testSetHeaderStringString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#addDateHeader(java.lang.String, long)}
   * .
   */
  @Test
  public void testAddDateHeaderStringLong() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#addIntHeader(java.lang.String, int)}
   * .
   */
  @Test
  public void testAddIntHeaderStringInt() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#setDateHeader(java.lang.String, long)}
   * .
   */
  @Test
  public void testSetDateHeaderStringLong() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.cache.impl.CacheableHttpServletResponse#setIntHeader(java.lang.String, int)}
   * .
   */
  @Test
  public void testSetIntHeaderStringInt() {
    fail("Not yet implemented"); // TODO
  }

}
