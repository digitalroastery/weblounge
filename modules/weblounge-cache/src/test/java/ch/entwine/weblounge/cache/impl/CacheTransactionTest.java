/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.cache.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.cache.StreamFilter;
import ch.entwine.weblounge.cache.impl.filter.NullFilter;
import ch.entwine.weblounge.cache.impl.handle.TaggedCacheHandle;
import ch.entwine.weblounge.common.impl.request.CacheTagImpl;
import ch.entwine.weblounge.common.impl.request.CacheTagSet;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.entwine.weblounge.common.request.CacheHandle;
import ch.entwine.weblounge.common.request.CacheTag;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Test case for the implementation at {@link CacheTransaction}.
 */
public class CacheTransactionTest {

  /** The transaction under test */
  protected CacheTransaction transaction = null;
  
  /** The cache handle */
  protected CacheHandle handle = null;
  
  /** The stream filter */
  protected StreamFilter filter = new NullFilter();
  
  /** Milliseconds until the cache entry expires */
  protected long expirationTime = 1000;

  /** Milliseconds until clients should recheck */
  protected long recheckTime = 500;

  /** The first cache tag */
  protected CacheTag tag = new CacheTagImpl("a", "a-value");

  /** The first cache tag */
  protected CacheTag otherTag = new CacheTagImpl("b", "b-value");

  /** The cache tags */
  protected CacheTagSet tags = new CacheTagSet();
  
  /** The cached response */
  protected CacheableHttpServletResponse response = null;
  
  /** The content */
  protected String content = "Hello world!";

  /** The test header name */
  protected String headerName = "X-Test-Header";

  /** The test header value */
  protected String headerValue = content;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    tags.add(tag);
    tags.add(otherTag);
    handle = new TaggedCacheHandle(tags.getTags(), expirationTime, recheckTime);
    
    response = new CacheableHttpServletResponse(new MockHttpServletResponse());
    transaction = response.startTransaction(handle, filter);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheTransaction#getHandle()}.
   */
  @Test
  public void testGetHandle() {
    assertNotNull(transaction.getHandle());
    assertEquals(handle, transaction.getHandle());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheTransaction#getOutputStream()}.
   */
  @Test
  public void testGetOutputStream() {
    assertNotNull(transaction.getOutputStream());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheTransaction#getContent()}.
   */
  @Test
  public void testGetContent() throws Exception {
    assertNotNull(transaction.getContent());
    assertEquals(0, transaction.getContent().length);

    // Write something to the response
    response.getOutputStream().write(content.getBytes());
    assertEquals(content, new String(transaction.getContent(), "utf-8"));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheTransaction#getHeaders()}.
   */
  @Test
  public void testGetHeaders() {
    assertNotNull(transaction.getHeaders());
    assertEquals(0, transaction.getHeaders().size());

    // Write something to the response
    response.addHeader(headerName, headerValue);
    assertEquals(1, transaction.getHeaders().size());
    assertEquals(headerValue, transaction.getHeaders().getHeaders().get(headerName));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheTransaction#getTags()}.
   */
  @Test
  public void testGetTags() {
    assertNotNull(transaction.getTags());
    Set<CacheTag> tags = new HashSet<CacheTag>(Arrays.asList(transaction.getTags()));
    assertEquals(this.tags.size(), tags.size());
    assertTrue(tags.contains(tag));
    assertTrue(tags.contains(otherTag));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheTransaction#getFilter()}.
   */
  @Test
  public void testGetFilter() {
    assertNotNull(transaction.getFilter());
    assertEquals(filter, transaction.getFilter());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheTransaction#invalidate()}.
   */
  @Test
  public void testInvalidate() {
    assertTrue(transaction.isValid());
    transaction.invalidate();
    assertFalse(transaction.isValid());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheTransaction#isValid()}.
   */
  @Test
  public void testIsValid() {
    assertTrue(transaction.isValid());
  }

}
