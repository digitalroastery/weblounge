/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.content.image;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.impl.content.image.ImageContentReader;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

/**
 * Test case for the implementation of {@link ImageContentReader}.
 */
public class ImageContentReaderTest {

  /** The image content reader to test */
  protected ImageContentReader reader = null;

  /** The user */
  protected User user = new UserImpl("admin");

  /** The language */
  protected Language language = LanguageUtils.getLanguage("de");

  protected String fileName = "porsche.jpg";

  /** Image size */
  protected long size = 73642;

  /** The mime type */
  protected String mimeType = "image/jpeg";

  /** The image width */
  protected long imageWidth = 1000;

  /** The image height */
  protected long imageHeight = 666;

  /**
   * Sets up all the members.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    reader = new ImageContentReader();
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentReader#createFromContent(java.io.InputStream, ch.entwine.weblounge.common.security.User, ch.entwine.weblounge.common.language.Language, long, java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testCreateFromContent() throws Exception {
    InputStream is = ImageContentReaderTest.class.getResourceAsStream("/" + fileName);
    ImageContent content = reader.createFromContent(is, user, language, size, fileName, mimeType);
    assertEquals(imageWidth, content.getWidth());
    assertEquals(imageHeight, content.getHeight());
  }

}
