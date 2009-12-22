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

import ch.o2it.weblounge.common.impl.request.CacheTag;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link CacheTag}.
 */
public class CacheTagTest {
  
  /** The tag instance under test */
  protected CacheTag tagWithValue = null;
  protected CacheTag tagWithoutValue = null;
  
  /** The default key */
  protected String name = "key";
  
  /** The default value */
  protected Object value = new Object();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    tagWithValue = new CacheTag(name, value);
    tagWithoutValue = new CacheTag(name);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.request.CacheTag#getName()}.
   */
  @Test
  public void testGetName() {
    assertEquals(name, tagWithValue.getName());
    assertEquals(name, tagWithoutValue.getName());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.request.CacheTag#getValue()}.
   */
  @Test
  public void testGetValue() {
    assertEquals(value, tagWithValue.getValue());
    assertEquals(CacheTag.ANY, tagWithoutValue.getValue());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.request.CacheTag#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    assertTrue(tagWithValue.equals(new CacheTag(name, value)));
    assertTrue(tagWithoutValue.equals(new CacheTag(name)));
    assertFalse(tagWithValue.equals(tagWithoutValue));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.request.CacheTag#setMatchAny()}.
   */
  @Test
  public void testSetMatchAny() {
    tagWithValue.setMatchAny();
    assertTrue(tagWithValue.matchesAny());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.request.CacheTag#matchesAny()}.
   */
  @Test
  public void testMatchesAny() {
    assertTrue(tagWithoutValue.matchesAny());
    assertFalse(tagWithValue.matchesAny());
  }

}
