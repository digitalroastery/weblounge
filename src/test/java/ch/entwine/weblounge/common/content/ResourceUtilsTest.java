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

package ch.entwine.weblounge.common.content;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.impl.content.ResourceUtils;

import org.junit.Test;

/**
 * Test cases for class {@link ResourceUtilsTest}.
 */
public class ResourceUtilsTest {
  
  /** The file size */
  protected double fileSize = 1265389524L;

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.ResourceUtils#formatFileSize(long)}.
   */
  @Test
  public void testFormatFileSizeLong() {
    // Test special values
    assertEquals("0B", ResourceUtils.formatFileSize(0));
    assertEquals("1B", ResourceUtils.formatFileSize(1));
    assertEquals("1kB", ResourceUtils.formatFileSize(1024));
    
    assertEquals("1.3GB", ResourceUtils.formatFileSize((long)fileSize));
    fileSize /= 1000.0d;
    assertEquals("1.3MB", ResourceUtils.formatFileSize((long)fileSize));
    fileSize /= 1000.0d;
    assertEquals("1.3kB", ResourceUtils.formatFileSize((long)fileSize));
    fileSize /= 1000.0d;
    assertEquals("1B", ResourceUtils.formatFileSize((long)fileSize));
  }

}
