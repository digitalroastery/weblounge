/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.preview.jai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.ImageScalingMode;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

/**
 * Test case for {@link JAIPreviewGenerator}.
 */
public class JAIPreviewGeneratorTest {

  /** Path to the image */
  protected static final String imagePath = "/porsche.jpg";

  /** Image url */
  protected static URL imageURL = null;

  /** The original image's width */
  protected final float originalWidth = 1000;

  /** The original image's height */
  protected final float originalHeight = 666;

  /** The style's width */
  protected static final float styleWidth = 250;

  /** The style's height */
  protected static final float styleHeight = 250;

  /** The image styles to test */
  protected static List<ImageStyle> styles = null;

  /** The resource */
  protected Resource<?> resource = null;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    imageURL = JAIPreviewGeneratorTest.class.getResource(imagePath).toURI().toURL();

    styles = new ArrayList<ImageStyle>();
    styles.add(new ImageStyleImpl("box", (int) styleWidth, (int) styleHeight, ImageScalingMode.Box, false, false));
    styles.add(new ImageStyleImpl("cover", (int) styleWidth, (int) styleHeight, ImageScalingMode.Cover, false, false));
    styles.add(new ImageStyleImpl("fill", (int) styleWidth, (int) styleHeight, ImageScalingMode.Fill, false, false));
    styles.add(new ImageStyleImpl("width", (int) styleWidth, (int) styleHeight, ImageScalingMode.Width, false, false));
    styles.add(new ImageStyleImpl("height", (int) styleWidth, (int) styleHeight, ImageScalingMode.Height, false, false));
    styles.add(new ImageStyleImpl("none", (int) styleWidth, (int) styleHeight, ImageScalingMode.None, false, false));

    // Make sure it's working on headless systems
    System.setProperty("java.awt.headless", "true");
    System.setProperty("weblounge.jai", Boolean.TRUE.toString());
  }

  public void setUp() {
    ResourceContent resourceContent = EasyMock.createNiceMock(ResourceContent.class);
    EasyMock.expect(resourceContent.getMimetype()).andReturn("image/png");
    EasyMock.replay(resourceContent);
    resource = EasyMock.createNiceMock(Resource.class);
    EasyMock.expect(resource.getContent((Language) EasyMock.anyObject())).andStubReturn(resourceContent);
    EasyMock.replay(resource);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#style(java.io.InputStream, java.io.OutputStream, java.lang.String, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * . TODO: This test is off by 1.
   */
  @Test
  @Ignore
  public void testStyle() throws Exception {
    for (ImageStyle style : styles) {
      InputStream is = null;
      ByteArrayOutputStream bos = null;
      ByteArrayInputStream bis = null;
      try {
        is = imageURL.openStream();

        // Style the original image
        bos = new ByteArrayOutputStream();
        JAIPreviewGenerator previewGenerator = new JAIPreviewGenerator();
        previewGenerator.createPreview(resource, Environment.Development, LanguageUtils.getLanguage("en"), style, null, is, bos);

        // Read the image back in
        bis = new ByteArrayInputStream(bos.toByteArray());
        SeekableStream seekableInputStream = new MemoryCacheSeekableStream(bis);
        RenderedOp image = JAI.create("stream", seekableInputStream);

        // Test width and height
        switch (style.getScalingMode()) {
          case Box:
            assertEquals(styleWidth, image.getWidth(), 0);
            assertEquals(originalHeight * (styleWidth / originalWidth), image.getHeight(), 0);
            break;
          case Cover:
            assertEquals(originalWidth * (styleHeight / originalHeight), image.getWidth(), 0);
            assertEquals(styleHeight, image.getHeight(), 0);
            break;
          case Fill:
            assertEquals(styleWidth, image.getWidth(), 0);
            assertEquals(styleHeight, image.getHeight(), 0);
            break;
          case Height:
            assertEquals(originalWidth * (styleHeight / originalHeight), image.getWidth(), 0);
            assertEquals(styleHeight, image.getHeight(), 0);
            break;
          case None:
            assertEquals(originalWidth, image.getWidth(), 0);
            assertEquals(originalHeight, image.getHeight(), 0);
            break;
          case Width:
            assertEquals(styleWidth, image.getWidth(), 0);
            assertEquals(originalHeight * (styleWidth / originalWidth), image.getHeight(), 0);
            break;
          default:
            fail("Unknown scaling mode " + style.getScalingMode());
        }
      } finally {
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(bos);
        IOUtils.closeQuietly(bis);
      }
    }
  }

}
