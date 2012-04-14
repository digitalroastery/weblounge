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
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.content.image.ImageContentImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.site.ImageScalingMode;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
  protected static final float styleWidth = 250;

  /** The style's height */
  protected static final float styleHeight = 250;

  /** The image styles to test */
  protected static List<ImageStyle> styles = null;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    imageURL = ImageStyleUtilsTest.class.getResource(imagePath).toURI().toURL();

    styles = new ArrayList<ImageStyle>();
    styles.add(new ImageStyleImpl("box", (int) styleWidth, (int) styleHeight, ImageScalingMode.Box, false));
    styles.add(new ImageStyleImpl("cover", (int) styleWidth, (int) styleHeight, ImageScalingMode.Cover, false));
    styles.add(new ImageStyleImpl("fill", (int) styleWidth, (int) styleHeight, ImageScalingMode.Fill, false));
    styles.add(new ImageStyleImpl("width", (int) styleWidth, (int) styleHeight, ImageScalingMode.Width, false));
    styles.add(new ImageStyleImpl("height", (int) styleWidth, (int) styleHeight, ImageScalingMode.Height, false));
    styles.add(new ImageStyleImpl("none", (int) styleWidth, (int) styleHeight, ImageScalingMode.None, false));

    // Make sure it's working on headless systems
    System.setProperty("java.awt.headless", "true");
    System.setProperty("weblounge.jai", Boolean.TRUE.toString());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getScale(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetScale() {
    float scaleToWidth = styleWidth / originalWidth;
    float scaleToHeight = styleHeight / originalHeight;
    for (ImageStyle style : styles) {
      float scale = ImageStyleUtils.getScale((int) originalWidth, (int) originalHeight, style);
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
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getCropX(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetCropX() {
    for (ImageStyle style : styles) {
      float cropX = ImageStyleUtils.getCropX((int) originalWidth, (int) originalHeight, style);
      switch (style.getScalingMode()) {
        case Fill:
          assertEquals(originalWidth - styleWidth, cropX);
          break;
        case Box:
        case Cover:
        case Width:
        case Height:
        case None:
          assertEquals(0.0f, cropX);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getCropY(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetCropY() {
    for (ImageStyle style : styles) {
      float cropY = ImageStyleUtils.getCropY((int) originalWidth, (int) originalHeight, style);
      switch (style.getScalingMode()) {
        case Fill:
          assertEquals(originalHeight - styleHeight, cropY);
          break;
        case Box:
        case Cover:
        case Width:
        case Height:
        case None:
          assertEquals(0.0f, cropY);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getStyledWidth(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   */
  @Test
  public void testGetStyledWidth() {
    for (ImageStyle style : styles) {
      int scaledWidth = ImageStyleUtils.getStyledWidth((int) originalWidth, (int) originalHeight, style);
      float scale = ImageStyleUtils.getScale(originalWidth, originalHeight, style);
      switch (style.getScalingMode()) {
        case Fill:
        case Box:
          assertEquals(styleWidth, scaledWidth);
          break;
        case Cover:
          assertEquals(Math.max(scale * originalWidth, styleWidth), scaledWidth);
          break;
        case Width:
          assertEquals(styleWidth, scaledWidth);
          break;
        case Height:
          assertEquals(Math.round(originalWidth * scale), scaledWidth);
          break;
        case None:
          assertEquals(originalWidth, scaledWidth);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getStyledHeight(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   */
  @Test
  public void testGetStyledHeight() {
    for (ImageStyle style : styles) {
      int scaledHeight = ImageStyleUtils.getStyledHeight((int) originalWidth, (int) originalHeight, style);
      float scale = ImageStyleUtils.getScale(originalWidth, originalHeight, style);
      switch (style.getScalingMode()) {
        case Fill:
          assertEquals(styleHeight, scaledHeight);
          break;
        case Box:
          assertEquals(Math.round(scale * originalHeight), scaledHeight);
          break;
        case Cover:
          assertEquals(styleHeight, scaledHeight);
          break;
        case Width:
          assertEquals(Math.round(scale * originalHeight), scaledHeight);
          break;
        case Height:
          assertEquals(styleHeight, scaledHeight);
          break;
        case None:
          assertEquals(originalHeight, scaledHeight);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getWidth(ImageContent, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * and
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getWidth(ImageContent, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetWidthAndHeigth() {
    for (ImageStyle style : styles) {
      ImageContent imageContent = new ImageContentImpl();
      imageContent.setWidth((int) originalWidth);
      imageContent.setHeight((int) originalHeight);

      int scaledWidth = ImageStyleUtils.getWidth(imageContent, style);
      int scaledHeight = ImageStyleUtils.getHeight(imageContent, style);

      switch (style.getScalingMode()) {
        case Box:
          assertEquals(styleWidth, scaledWidth);
          assertEquals(originalHeight * (styleWidth / originalWidth), scaledHeight);
          break;
        case Cover:
          assertEquals(originalWidth * (styleHeight / originalHeight), scaledWidth);
          assertEquals(styleHeight, scaledHeight);
          break;
        case Fill:
          assertEquals(styleWidth, scaledWidth);
          assertEquals(styleHeight, scaledHeight);
          break;
        case Height:
          assertEquals(originalWidth * (styleHeight / originalHeight), scaledWidth);
          assertEquals(styleHeight, scaledHeight);
          break;
        case None:
          assertEquals(originalWidth, scaledWidth);
          assertEquals(originalHeight, scaledHeight);
          break;
        case Width:
          assertEquals(styleWidth, scaledWidth);
          assertEquals(originalHeight * (styleWidth / originalWidth), scaledHeight);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

}
