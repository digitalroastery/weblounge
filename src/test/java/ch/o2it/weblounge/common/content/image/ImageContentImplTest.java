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

package ch.o2it.weblounge.common.content.image;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.impl.content.image.ImageContentImpl;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.language.Language;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for class {@link ImageContentImpl}.
 */
public class ImageContentImplTest {
  
  /** The image content object to test */
  protected ImageContent image = null;

  /** The filename */
  protected String filename = "Stadt.jpg";
  
  /** The German language */
  protected Language german = LanguageSupport.getLanguage("de");
  
  /** The file size */
  protected long size = 1408338L;
  
  /** The mime type */
  protected String mimetype = "image/jpeg";
  
  /** The image width */
  protected int width = 2188;

  /** The image width */
  protected int height = 1446;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    image = new ImageContentImpl(filename, german, size);
    image.setMimetype(mimetype);
    image.setWidth(width);
    image.setHeight(height);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.ImageContentImpl#getLanguage()}.
   */
  @Test
  public void testGetLanguage() {
    assertEquals(german, image.getLanguage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.ImageContentImpl#getMimetype()}.
   */
  @Test
  public void testGetMimetype() {
    assertEquals(mimetype, image.getMimetype());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.ImageContentImpl#getFilename()}.
   */
  @Test
  public void testGetFilename() {
    assertEquals(filename, image.getFilename());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.ImageContentImpl#getSize()}.
   */
  @Test
  public void testGetSize() {
    assertEquals(size, image.getSize());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.ImageContentImpl#getWidth()}.
   */
  @Test
  public void testGetWidth() {
    assertEquals(width, image.getWidth());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.file.ImageContentImpl#getHeight()}.
   */
  @Test
  public void testGetHeight() {
    assertEquals(height, image.getHeight());
  }

}
