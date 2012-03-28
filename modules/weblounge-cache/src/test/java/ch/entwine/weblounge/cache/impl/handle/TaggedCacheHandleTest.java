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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.request.CacheTagImpl;
import ch.entwine.weblounge.common.request.CacheTag;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link TaggedCacheHandle}.
 */
public class TaggedCacheHandleTest {

  /** The handle to be tested */
  protected TaggedCacheHandle handle = null;

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

  /** The primary tag set */
  protected CacheTag[] primaryTags = { tag, otherTag };

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    handle = new TaggedCacheHandle(primaryTags, expirationTime, recheckTime);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.TaggedCacheHandle#testGetPrimaryTags()}
   * .
   */
  @Test
  public void testGetPrimaryTags() {
    assertTrue(primaryTags == handle.getPrimaryTags());
    handle.addTag("u", "v");
    assertTrue(primaryTags == handle.getPrimaryTags());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.cache.impl.handle.TaggedCacheHandle#createKey(ch.entwine.weblounge.common.request.CacheTag[])}
   * .
   */
  @Test
  public void testCreateKey() {
    String key = handle.getKey();
    assertNotNull(key);
    for (CacheTag tag : primaryTags) {
      assertTrue(key.contains(tag.getName()));
      assertTrue(key.contains(tag.getValue()));
    }
  }

}
