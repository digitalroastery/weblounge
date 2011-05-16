/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.impl.request.CacheTagImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for cache tags.
 */
public class CacheTagImplTest {

  /** A test tag instances */
  protected CacheTagImpl tag = null;

  /** The tag name */
  protected static final String tagName = "test";

  /** The tag value */
  protected static final String tagValue = "testvalue";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    tag = new CacheTagImpl(tagName, tagValue);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.request.CacheTagImpl#hashCode()}.
   */
  @Test
  public void testHashCode() {
    assertEquals(tagName.hashCode(), tag.hashCode());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.request.CacheTagImpl#CacheTagImpl(java.lang.String)}
   * .
   */
  @Test
  public void testCacheTagImplString() {
    Tag t = new CacheTagImpl(tagName);
    assertEquals(tagName, t.getName());
    assertEquals(CacheTag.ANY, t.getValue());
    try {
      new CacheTagImpl(null);
      fail("Managed to initialize tag without name");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.request.CacheTagImpl#CacheTagImpl(java.lang.String, java.lang.Object)}
   * .
   */
  @Test
  public void testCacheTagImplStringObject() {
    Tag t = null;

    // Test null value
    t = new CacheTagImpl(tagName, null);
    assertEquals("test", t.getName());
    assertEquals(CacheTag.ANY, t.getValue());

    // Test non-null value
    t = new CacheTagImpl(tagName, tagValue);
    assertEquals("test", t.getName());
    assertEquals(tagValue, t.getValue());

    try {
      new CacheTagImpl(null, tagValue);
      fail("Managed to initialize tag without name");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.request.CacheTagImpl#getName()} .
   */
  @Test
  public void testGetName() {
    assertEquals(tagName, tag.getName());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.request.CacheTagImpl#getValue()}.
   */
  @Test
  public void testGetValue() {
    assertEquals(tagValue, tag.getValue());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.request.CacheTagImpl#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObject() {
    CacheTagImpl strictTag = null;

    // Test equals -> true
    strictTag = new CacheTagImpl(tagName, tagValue);
    assertTrue(tag.equals(strictTag));

    // Test differing value
    strictTag = new CacheTagImpl(tagName, "othervalue");
    assertFalse(tag.equals(strictTag));

    // Test differing name
    strictTag = new CacheTagImpl("othername", tagValue);
    assertFalse(tag.equals(strictTag));

    // Test null value
    strictTag = new CacheTagImpl(tagName);
    assertFalse(tag.equals(strictTag));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.request.CacheTagImpl#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testSetMatchAny() {
    tag.setMatchAny();
    assertTrue(tag.matchesAny());
  }

}