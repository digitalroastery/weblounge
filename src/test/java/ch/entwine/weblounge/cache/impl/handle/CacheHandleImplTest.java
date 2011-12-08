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

package ch.entwine.weblounge.cache.impl.handle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.request.CacheTagImpl;
import ch.entwine.weblounge.common.request.CacheTag;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test case for {@link CacheHandleImpl}.
 */
public class CacheHandleImplTest {

  /** The cache handle to test */
  protected CacheHandleImpl handle = null;

  /** The key */
  protected String key = "/a/b/c";
  
  /** The current time */
  protected long time = System.currentTimeMillis();

  /** The expiration time */
  protected long expirationTime = time + Times.MS_PER_DAY;

  /** The recheck time */
  protected long recheckTime = time + Times.MS_PER_HOUR;

  /** The first cache tag */
  protected CacheTag tag = new CacheTagImpl("a", "a-value");

  /** The first cache tag */
  protected CacheTag otherTag = new CacheTagImpl("b", "b-value");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    handle = new CacheHandleImpl(key, expirationTime, recheckTime);
    handle.addTag(tag);
    handle.addTag(otherTag);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(key.hashCode(), handle.hashCode());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#setKey(java.lang.String)}
   * .
   */
  @Test
  public void testSetKey() {
    String newKey = "/u/v/w";
    handle.setKey(newKey);
    assertEquals(newKey, handle.getKey());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#getKey()}.
   */
  @Test
  public void testGetKey() {
    assertEquals(key, handle.getKey());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#getCacheExpirationTime()}.
   */
  @Test
  public void testGetExpires() {
    assertEquals(expirationTime, handle.getCacheExpirationTime());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#setCacheExpirationTime(long)}
   * .
   */
  @Test
  public void testSetExpires() {
    long t = System.currentTimeMillis();
    handle.setCacheExpirationTime(t);
    assertEquals(t, handle.getCacheExpirationTime());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#getClientRevalidationTime()}.
   */
  @Test
  public void testGetRecheck() {
    assertEquals(recheckTime, handle.getClientRevalidationTime());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#setClientRevalidationTime(long)}
   * .
   */
  @Test
  public void testSetRecheck() {
    long t = System.currentTimeMillis();
    handle.setClientRevalidationTime(t);
    assertEquals(t, handle.getClientRevalidationTime());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#getTagSet()}.
   */
  @Test
  public void testGetTagSet() {
    CacheTag[] tags = handle.getTags();
    assertNotNull(tags);
    assertEquals(2, tags.length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObject() {
    assertTrue(handle.equals(new CacheHandleImpl(key, expirationTime, recheckTime)));
    assertFalse(handle.equals(new CacheHandleImpl("/u/v/w", expirationTime, recheckTime)));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#addTag(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testAddTagStringString() {
    String tagName = "u";
    handle.addTag(tagName, "value");
    assertTrue(handle.containsTag(tagName));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#addTags(java.util.Collection)}
   * .
   */
  @Test
  public void testAddTags() {
    List<CacheTag> tags = new ArrayList<CacheTag>();
    CacheTag t = new CacheTagImpl("u", "uvalue");
    tags.add(t);
    handle.addTags(tags);
    assertTrue(handle.containsTag(t));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#removeTags(java.lang.String)}
   * .
   */
  @Test
  public void testRemoveTags() {
    handle.removeTag(tag);
    assertEquals(1, handle.getTags().length);
    assertFalse(handle.containsTag(tag));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#removeTag(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testRemoveTagStringString() {
    handle.removeTag(tag.getName(), tag.getValue());
    assertEquals(1, handle.getTags().length);
    assertFalse(handle.containsTag(tag));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#clearTags()}.
   */
  @Test
  public void testClearTags() {
    handle.clearTags();
    assertEquals(0, handle.getTags().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#containsTag(ch.entwine.weblounge.common.request.CacheTag)}
   * .
   */
  @Test
  public void testContainsTagCacheTag() {
    assertTrue(handle.containsTag(tag));
    assertTrue(handle.containsTag(otherTag));
    assertFalse(handle.containsTag(new CacheTagImpl("u", "v")));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#containsTag(java.lang.String)}
   * .
   */
  @Test
  public void testContainsTagString() {
    assertTrue(handle.containsTag(tag.getName()));
    assertTrue(handle.containsTag(otherTag.getName()));
    assertFalse(handle.containsTag("u"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#containsTag(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testContainsTagStringString() {
    assertTrue(handle.containsTag(tag.getName(), tag.getValue()));
    assertTrue(handle.containsTag(otherTag.getName(), otherTag.getValue()));
    assertFalse(handle.containsTag(tag.getName(), "x"));
    assertFalse(handle.containsTag("u", "v"));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#isTagged()}.
   */
  @Test
  public void testIsTagged() {
    assertTrue(handle.isTagged());
    assertFalse(new CacheHandleImpl(key, expirationTime, recheckTime).isTagged());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#removeTag(ch.entwine.weblounge.common.request.CacheTag)}
   * .
   */
  @Test
  public void testRemoveTagCacheTag() {
    handle.removeTag(tag.getName(), tag.getValue());
    assertEquals(1, handle.getTags().length);
    handle.removeTag(otherTag.getName(), "x");
    assertEquals(1, handle.getTags().length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#getTags()}.
   */
  @Test
  public void testGetTags() {
    CacheTag[] tags = handle.getTags();
    assertNotNull(tags);
    assertEquals(2, tags.length);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.CacheHandleImpl#tags()}.
   */
  @Test
  public void testTags() {
    Iterator<CacheTag> i = handle.tags();
    assertTrue(i.hasNext());
    assertNotNull(i.next());
    assertTrue(i.hasNext());
    assertNotNull(i.next());
    assertFalse(i.hasNext());
  }

}
