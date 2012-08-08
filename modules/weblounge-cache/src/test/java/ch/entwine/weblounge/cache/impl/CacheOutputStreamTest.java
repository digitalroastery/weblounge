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

import ch.entwine.weblounge.common.impl.request.CachedOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the implementation at {@link CachedOutputStream}.
 */
public class CacheOutputStreamTest {

  /** The output stream to test */
  protected CachedOutputStream outputStream = null;
  
  /** The content length */
  protected int contentLength = 4096;
  
  /** The content */
  protected String content = "Hello world!";
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    outputStream = new CachedOutputStream();
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.request.CachedOutputStream#write(int)}.
   */
  @Test
  public void testWriteInt() {
    for (int i = 0; i < contentLength; i++) {
      int b = i % 128;
      outputStream.write(b);
      assertEquals(i + 1, outputStream.getContent().length);
      assertEquals(b, outputStream.getContent()[i]);
    }
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.request.CachedOutputStream#write(byte[], int, int)}.
   */
  @Test
  public void testWriteByteArrayIntInt() {
    outputStream.write(5);
    assertEquals(1, outputStream.getContent().length);
    assertEquals(5, outputStream.getContent()[0]);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.request.CachedOutputStream#getContent()}.
   */
  @Test
  public void testGetContent() throws Exception {
    assertNotNull(outputStream.getContent());
    outputStream.write(content.getBytes());
    assertEquals(content.length(), outputStream.getContent().length);
    assertEquals(content, new String(outputStream.getContent(), "utf-8"));
  }

}
