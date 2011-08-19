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

import ch.entwine.weblounge.cache.impl.handle.TaggedCacheHandle;
import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.request.CacheTagImpl;
import ch.entwine.weblounge.common.impl.request.CacheTagSet;
import ch.entwine.weblounge.common.request.CacheHandle;
import ch.entwine.weblounge.common.request.CacheTag;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the implementation at {@link CacheEntry}.
 */
public class CacheEntryTest {

  /** The cache entry */
  protected CacheEntry entry = null;
    
  /** The cache handle */
  protected CacheHandle handle = null;
  
  /** Response body */
  protected String content = "Hello World!";
  
  /** The cached response headers */
  protected CacheableHttpServletResponseHeaders headers = null;
  
  /** The expiration time */
  protected long expirationTime = 1000; 

  /** The recheck time */
  protected long recheckTime = 1000; 

  /** The first cache tag */
  protected CacheTag tag = new CacheTagImpl("a", "a-value");

  /** The first cache tag */
  protected CacheTag otherTag = new CacheTagImpl("b", "b-value");

  /** The cache tags */
  protected CacheTagSet tags = new CacheTagSet();
  
  /** Name of the test header */
  protected String testHeaderName = "Test-Header";

  /** Test header value */
  protected String testHeaderValue = "header-value";

  /** Content type */
  protected String contentType = "text/xml";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    tags.add(tag);
    tags.add(otherTag);
    headers = new CacheableHttpServletResponseHeaders();
    headers.setHeader(testHeaderName, testHeaderValue);
    headers.setHeader("Content-Type", contentType);
    handle = new TaggedCacheHandle(tags.getTags(), expirationTime, recheckTime);
    entry = new CacheEntry(handle, content.getBytes(), headers);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#getKey()}.
   */
  @Test
  public void testGetKey() {
    StringBuffer calculatedKey = new StringBuffer();
    calculatedKey.append(tag.getName()).append("=").append(tag.getValue()).append("; ");
    calculatedKey.append(otherTag.getName()).append("=").append(otherTag.getValue());
    assertEquals(calculatedKey.toString(), entry.getKey());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#getETag()}.
   */
  @Test
  public void testGetETag() throws Exception {
    String eTag = entry.getETag();
    assertNotNull(eTag);
    Thread.sleep(100);
    CacheHandle newHandle = new TaggedCacheHandle(tags.getTags(), expirationTime, recheckTime);
    String newETag = new CacheEntry(newHandle, content.getBytes(), headers).getETag();
    assertFalse(eTag.equals(newETag));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#getLastModified()}.
   */
  @Test
  public void testGetCreationDate() throws Exception {
    Thread.sleep(100);
    assertTrue(System.currentTimeMillis() >= entry.getLastModified());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#containsTag(ch.entwine.weblounge.common.request.CacheTag)}.
   */
  @Test
  public void testContainsTag() {
    assertTrue(entry.containsTag(tag));
    assertTrue(entry.containsTag(otherTag));
    assertFalse(entry.containsTag(new CacheTagImpl("a", "x")));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#getHeaders()}.
   */
  @Test
  public void testGetHeaders() {
    CacheableHttpServletResponseHeaders h = entry.getHeaders();
    assertNotNull(h);
    assertEquals(2, h.size());
    assertTrue(h.getHeaders().containsKey(testHeaderName));
    assertEquals(testHeaderValue, h.getHeaders().get(testHeaderName));
    assertEquals(contentType, h.getHeaders().get("Content-Type"));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#getContent()}.
   */
  @Test
  public void testGetContent() throws Exception {
    assertNotNull(entry.getContent());
    assertEquals(content, new String(entry.getContent(), "utf-8"));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#getContentType()}.
   */
  @Test
  public void testGetContentType() {
    assertNotNull(entry.getContentType());
    assertEquals(contentType, entry.getContentType());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#notModified(long)}.
   */
  @Test
  public void testNotModified() throws Exception {
    Thread.sleep(100);
    assertTrue(entry.notModified(System.currentTimeMillis()));
    assertFalse(entry.notModified(System.currentTimeMillis() - Times.MS_PER_HOUR));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.cache.impl.CacheEntry#matches(java.lang.String)}.
   */
  @Test
  public void testMatches() {
    String eTag = entry.getETag();
    assertTrue(entry.matches(eTag));
  }

}
