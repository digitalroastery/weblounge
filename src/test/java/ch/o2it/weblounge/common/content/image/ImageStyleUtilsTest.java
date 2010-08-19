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

import ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.o2it.weblounge.common.site.ScalingMode;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

/**
 * Test case for {@link ImageStyleUtilsTest}.
 */
public class ImageStyleUtilsTest {

  /** Path to the image */
  protected static final String imagePath = "/porsche.jpg";

  /** Image url */
  protected static URL imageURL = null;

  /** The original image's width */
  protected final float originalWidth = 1000;

  /** The original image's height */
  protected final float originalHeight = 666;

  /** The style's width */
  protected static final float width = 250;

  /** The style's height */
  protected static final float height = 250;

  /** The image styles to test */
  protected static List<ImageStyle> styles = null;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    imageURL = ImageStyleUtilsTest.class.getResource(imagePath).toURI().toURL();

    styles = new ArrayList<ImageStyle>();
    styles.add(new ImageStyleImpl("box", (int)width, (int)height, ScalingMode.Box, false));
    styles.add(new ImageStyleImpl("cover", (int)width, (int)height, ScalingMode.Cover, false));
    styles.add(new ImageStyleImpl("fill", (int)width, (int)height, ScalingMode.Fill, false));
    styles.add(new ImageStyleImpl("width", (int)width, (int)height, ScalingMode.Width, false));
    styles.add(new ImageStyleImpl("height", (int)width, (int)height, ScalingMode.Height, false));
    styles.add(new ImageStyleImpl("none", (int)width, (int)height, ScalingMode.None, false));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils#getScale(int, int, ch.o2it.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetScale() {
    float scaleToWidth = width / originalWidth;
    float scaleToHeight = height / originalHeight;
    for (ImageStyle style : styles) {
      float scale = ImageStyleUtils.getScale((int)originalWidth, (int)originalHeight, style);
      switch (style.getScalingMode()) {
        case Box:
          assertEquals(scaleToWidth, scale);
          break;
        case Cover:
          assertEquals(scaleToHeight, scale);
          break;
        case Fill:
          assertEquals(scaleToHeight, scale);
          break;
        case Width:
          assertEquals(scaleToWidth, scale);
          break;
        case Height:
          assertEquals(scaleToHeight, scale);
          break;
        case None:
          assertEquals(1.0f, scale);
          break;
      }    
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils#getCropX(int, int, ch.o2it.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetCropX() {
    for (ImageStyle style : styles) {
      float cropX = ImageStyleUtils.getCropX((int)originalWidth, (int)originalHeight, style);
      float scaleToHeight = height / originalHeight;
      switch (style.getScalingMode()) {
        case Fill:
          assertEquals((originalWidth * scaleToHeight) - width, cropX);
          break;
        case Box:
        case Cover:
        case Width:
        case Height:
        case None:
          assertEquals(0.0f, cropX);
          break;
      }    
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils#getCropY(int, int, ch.o2it.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetCropY() {
    for (ImageStyle style : styles) {
      float cropY = ImageStyleUtils.getCropY((int)originalWidth, (int)originalHeight, style);
      switch (style.getScalingMode()) {
        case Fill:
          assertEquals(0.0f, cropY);
          break;
        case Box:
        case Cover:
        case Width:
        case Height:
        case None:
          assertEquals(0.0f, cropY);
          break;
      }    
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils#style(java.io.InputStream, java.io.OutputStream, java.lang.String, ch.o2it.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testStyle() throws Exception {
    for (ImageStyle style : styles) {
      InputStream is = null;
      ByteArrayOutputStream bos = null;
      ByteArrayInputStream bis = null;
      try {
        is = imageURL.openStream();
        
        // Style the original image
        bos = new ByteArrayOutputStream();
        ImageStyleUtils.style(is, bos, "jpeg", style);

        // Read the image back in
        bis = new ByteArrayInputStream(bos.toByteArray());
        SeekableStream seekableInputStream = new MemoryCacheSeekableStream(bis);
        RenderedOp image = JAI.create("stream", seekableInputStream);
        
        // Test width and height
        switch (style.getScalingMode()) {
          case Box:
            assertEquals(width, image.getWidth());
            assertEquals(originalHeight * (width / originalWidth), image.getHeight());
            break;
          case Cover:
            assertEquals(originalWidth * (height / originalHeight), image.getWidth());
            assertEquals(height, image.getHeight());
            break;
          case Fill:
            assertEquals(width + 1, image.getWidth());
            assertEquals(height, image.getHeight());
            break;
          case Height:
            assertEquals(originalWidth * (height / originalHeight), image.getWidth());
            assertEquals(height, image.getHeight());
            break;
          case None:
            assertEquals(originalWidth, image.getWidth());
            assertEquals(originalHeight, image.getHeight());
            break;
          case Width:
            assertEquals(width, image.getWidth());
            assertEquals(originalHeight * (width / originalWidth), image.getHeight());
            break;
        }
      } finally {
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(bos);
        IOUtils.closeQuietly(bis);
      }
    }
  }

}
