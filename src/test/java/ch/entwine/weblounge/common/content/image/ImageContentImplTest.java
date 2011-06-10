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

package ch.entwine.weblounge.common.content.image;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.impl.content.image.ImageContentImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test case for class {@link ImageContentImpl}.
 */
public class ImageContentImplTest {
  
  /** The image content object to test */
  protected ImageContent image = null;

  /** The filename */
  protected String filename = "Stadt.jpg";
  
  /** The German language */
  protected Language german = LanguageUtils.getLanguage("de");
  
  /** The file size */
  protected long size = 1408338L;
  
  /** The mime type */
  protected String mimetype = "image/jpeg";
  
  /** The creation date */
  protected Date creationDate = new Date(1231358741000L);

  /** Some date after the latest modification date */
  protected Date futureDate = new Date(2000000000000L);

  /** The creation date */
  protected User amelie = new UserImpl("amelie", "testland", "Amélie Poulard");

  /** The image width */
  protected int width = 2188;

  /** The image width */
  protected int height = 1446;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    image = new ImageContentImpl(filename, german, mimetype, width, height, size);
    ((ImageContentImpl)image).setCreated(creationDate, amelie);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.file.ImageContentImpl#getLanguage()}.
   */
  @Test
  public void testGetLanguage() {
    assertEquals(german, image.getLanguage());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.file.ImageContentImpl#getMimetype()}.
   */
  @Test
  public void testGetMimetype() {
    assertEquals(mimetype, image.getMimetype());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.file.ImageContentImpl#getFilename()}.
   */
  @Test
  public void testGetFilename() {
    assertEquals(filename, image.getFilename());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.file.ImageContentImpl#getSize()}.
   */
  @Test
  public void testGetSize() {
    assertEquals(size, image.getSize());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.file.ImageContentImpl#getWidth()}.
   */
  @Test
  public void testGetWidth() {
    assertEquals(width, image.getWidth());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.file.ImageContentImpl#getHeight()}.
   */
  @Test
  public void testGetHeight() {
    assertEquals(height, image.getHeight());
  }

}
