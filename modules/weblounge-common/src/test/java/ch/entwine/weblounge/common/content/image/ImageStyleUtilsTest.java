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

import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.site.ImageScalingMode;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Test case for {@link ImageStyleUtilsTest}.
 */
public class ImageStyleUtilsTest {

  /** Path to the image */
  protected static final String imagePath = "/porsche.jpg";

  /** Image url */
  protected static URL imageURL = null;

  /** The style's width */
  protected static final float styleWidth = 300;

  /** The style's height */
  protected static final float styleHeight = 300;

  /** The image styles to test */
  protected static List<ImageStyle> styles = null;

  protected static Map<ImageScalingMode, Map<String, int[]>> expectedResults = null;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    imageURL = ImageStyleUtilsTest.class.getResource(imagePath).toURI().toURL();

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

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getScale(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetScale() {

    for (ImageStyle style : styles) {
      switch (style.getScalingMode()) {
        case Box:
          float[] expectedBoxValues = {
              0.75f,
              0.75f,
              0.75f,
              0.75f,
              1,
              1,
              0.75f,
              1,
              1 };
          testGetScale(style, expectedBoxValues);
          break;
        case Fill:
          float[] expectedFillValues = {
              0.75f,
              1,
              1.5f,
              1,
              1,
              1.5f,
              1.5f,
              1.5f,
              1.5f };
          testGetScale(style, expectedFillValues);
          break;
        case Cover:
          float[] expectedCoverValues = {
              0.75f,
              1,
              1.5f,
              1,
              1,
              1.5f,
              1.5f,
              1.5f,
              1.5f };
          testGetScale(style, expectedCoverValues);
          break;
        case Width:
          float[] expectedWidthValues = {
              0.75f,
              0.75f,
              0.75f,
              1,
              1,
              1,
              1.5f,
              1.5f,
              1.5f };
          testGetScale(style, expectedWidthValues);
          break;
        case Height:
          float[] expectedHeightValues = {
              0.75f,
              1,
              1.5f,
              0.75f,
              1,
              1.5f,
              0.75f,
              1,
              1.5f };
          testGetScale(style, expectedHeightValues);
          break;
        case None:
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  private void testGetScale(ImageStyle style, float[] expectedValues) {
    assertEquals(expectedValues[0], ImageStyleUtils.getScale(400, 400, style), 0.1f);
    assertEquals(expectedValues[1], ImageStyleUtils.getScale(400, 300, style), 0.1f);
    assertEquals(expectedValues[2], ImageStyleUtils.getScale(400, 200, style), 0.1f);
    assertEquals(expectedValues[3], ImageStyleUtils.getScale(300, 400, style), 0.1f);
    assertEquals(expectedValues[4], ImageStyleUtils.getScale(300, 300, style), 0.1f);
    assertEquals(expectedValues[5], ImageStyleUtils.getScale(300, 200, style), 0.1f);
    assertEquals(expectedValues[6], ImageStyleUtils.getScale(200, 400, style), 0.1f);
    assertEquals(expectedValues[7], ImageStyleUtils.getScale(200, 300, style), 0.1f);
    assertEquals(expectedValues[8], ImageStyleUtils.getScale(200, 200, style), 0.1f);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getCropX(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetCropX() {
    for (ImageStyle style : styles) {
      switch (style.getScalingMode()) {
        case Box:
          int[] expectedBoxValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
          testGetCropX(style, expectedBoxValues);
          break;
        case Fill:
          int[] expectedFillValues = { 0, 100, 300, 0, 0, 150, 0, 0, 0 };
          testGetCropX(style, expectedFillValues);
          break;
        case Cover:
          int[] expectedCoverValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
          testGetCropX(style, expectedCoverValues);
          break;
        case Width:
          int[] expectedWidthValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
          testGetCropX(style, expectedWidthValues);
          break;
        case Height:
          int[] expectedHeightValues = { 0, 100, 300, 0, 0, 150, 0, 0, 0 };
          testGetCropX(style, expectedHeightValues);
          break;
        case None:
          int[] expectedNoneValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
          testGetCropX(style, expectedNoneValues);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  private void testGetCropX(ImageStyle style, int[] expectedValues) {
    assertEquals(expectedValues[0], ImageStyleUtils.getCropX(400, 400, style));
    assertEquals(expectedValues[1], ImageStyleUtils.getCropX(400, 300, style));
    assertEquals(expectedValues[2], ImageStyleUtils.getCropX(400, 200, style));
    assertEquals(expectedValues[3], ImageStyleUtils.getCropX(300, 400, style));
    assertEquals(expectedValues[4], ImageStyleUtils.getCropX(300, 300, style));
    assertEquals(expectedValues[5], ImageStyleUtils.getCropX(300, 200, style));
    assertEquals(expectedValues[6], ImageStyleUtils.getCropX(200, 400, style));
    assertEquals(expectedValues[7], ImageStyleUtils.getCropX(200, 300, style));
    assertEquals(expectedValues[8], ImageStyleUtils.getCropX(200, 200, style));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getCropY(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   * .
   */
  @Test
  public void testGetCropY() {
    for (ImageStyle style : styles) {
      switch (style.getScalingMode()) {
        case Box:
          int[] expectedBoxValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
          testGetCropY(style, expectedBoxValues);
          break;
        case Fill:
          int[] expectedFillValues = { 0, 0, 0, 100, 0, 0, 300, 150, 0 };
          testGetCropY(style, expectedFillValues);
          break;
        case Cover:
          int[] expectedCoverValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
          testGetCropY(style, expectedCoverValues);
          break;
        case Width:
          int[] expectedWidthValues = { 0, 0, 0, 100, 0, 0, 300, 150, 0 };
          testGetCropY(style, expectedWidthValues);
          break;
        case Height:
          int[] expectedHeightValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
          testGetCropY(style, expectedHeightValues);
          break;
        case None:
          int[] expectedNoneValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
          testGetCropY(style, expectedNoneValues);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  private void testGetCropY(ImageStyle style, int[] expectedValues) {
    assertEquals(expectedValues[0], ImageStyleUtils.getCropY(400, 400, style));
    assertEquals(expectedValues[1], ImageStyleUtils.getCropY(400, 300, style));
    assertEquals(expectedValues[2], ImageStyleUtils.getCropY(400, 200, style));
    assertEquals(expectedValues[3], ImageStyleUtils.getCropY(300, 400, style));
    assertEquals(expectedValues[4], ImageStyleUtils.getCropY(300, 300, style));
    assertEquals(expectedValues[5], ImageStyleUtils.getCropY(300, 200, style));
    assertEquals(expectedValues[6], ImageStyleUtils.getCropY(200, 400, style));
    assertEquals(expectedValues[7], ImageStyleUtils.getCropY(200, 300, style));
    assertEquals(expectedValues[8], ImageStyleUtils.getCropY(200, 200, style));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getStyledWidth(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   */
  @Test
  public void testGetStyledWidth() {
    for (ImageStyle style : styles) {
      switch (style.getScalingMode()) {
        case Box:
          int[] expectedBoxValues = {
              300,
              300,
              300,
              225,
              300,
              300,
              150,
              200,
              200 };
          testGetStyledWidth(style, expectedBoxValues);
          break;
        case Fill:
          int[] expectedFillValues = {
              300,
              300,
              300,
              300,
              300,
              300,
              300,
              300,
              300 };
          testGetStyledWidth(style, expectedFillValues);
          break;
        case Cover:
          int[] expectedCoverValues = {
              300,
              400,
              600,
              300,
              300,
              450,
              300,
              300,
              300 };
          testGetStyledWidth(style, expectedCoverValues);
          break;
        case Width:
          int[] expectedWidthValues = {
              300,
              300,
              300,
              300,
              300,
              300,
              300,
              300,
              300 };
          testGetStyledWidth(style, expectedWidthValues);
          break;
        case Height:
          int[] expectedHeightValues = {
              300,
              300,
              300,
              225,
              300,
              300,
              150,
              200,
              300 };
          testGetStyledWidth(style, expectedHeightValues);
          break;
        case None:
          int[] expectedNoneValues = {
              400,
              400,
              400,
              300,
              300,
              300,
              200,
              200,
              200 };
          testGetStyledWidth(style, expectedNoneValues);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  private void testGetStyledWidth(ImageStyle style, int[] expectedValues) {
    assertEquals(expectedValues[0], ImageStyleUtils.getStyledWidth(400, 400, style));
    assertEquals(expectedValues[1], ImageStyleUtils.getStyledWidth(400, 300, style));
    assertEquals(expectedValues[2], ImageStyleUtils.getStyledWidth(400, 200, style));
    assertEquals(expectedValues[3], ImageStyleUtils.getStyledWidth(300, 400, style));
    assertEquals(expectedValues[4], ImageStyleUtils.getStyledWidth(300, 300, style));
    assertEquals(expectedValues[5], ImageStyleUtils.getStyledWidth(300, 200, style));
    assertEquals(expectedValues[6], ImageStyleUtils.getStyledWidth(200, 400, style));
    assertEquals(expectedValues[7], ImageStyleUtils.getStyledWidth(200, 300, style));
    assertEquals(expectedValues[8], ImageStyleUtils.getStyledWidth(200, 200, style));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils#getStyledHeight(int, int, ch.entwine.weblounge.common.content.image.ImageStyle)}
   */
  @Test
  public void testGetStyledHeight() {
    for (ImageStyle style : styles) {
      switch (style.getScalingMode()) {
        case Box:
          int[] expectedBoxValues = {
              300,
              225,
              150,
              300,
              300,
              200,
              300,
              300,
              200 };
          testGetStyledHeight(style, expectedBoxValues);
          break;
        case Fill:
          int[] expectedFillValues = {
              300,
              300,
              300,
              300,
              300,
              300,
              300,
              300,
              300 };
          testGetStyledHeight(style, expectedFillValues);
          break;
        case Cover:
          int[] expectedCoverValues = {
              300,
              300,
              300,
              400,
              300,
              300,
              600,
              450,
              300 };
          testGetStyledHeight(style, expectedCoverValues);
          break;
        case Width:
          int[] expectedWidthValues = {
              300,
              225,
              150,
              300,
              300,
              200,
              300,
              300,
              300 };
          testGetStyledHeight(style, expectedWidthValues);
          break;
        case Height:
          int[] expectedHeightValues = {
              300,
              300,
              300,
              300,
              300,
              300,
              300,
              300,
              300 };
          testGetStyledHeight(style, expectedHeightValues);
          break;
        case None:
          int[] expectedNoneValues = {
              400,
              300,
              200,
              400,
              300,
              200,
              400,
              300,
              200 };
          testGetStyledHeight(style, expectedNoneValues);
          break;
        default:
          fail("Unknown scaling mode " + style.getScalingMode());
      }
    }
  }

  private void testGetStyledHeight(ImageStyle style, int[] expectedValues) {
    assertEquals(expectedValues[0], ImageStyleUtils.getStyledHeight(400, 400, style));
    assertEquals(expectedValues[1], ImageStyleUtils.getStyledHeight(400, 300, style));
    assertEquals(expectedValues[2], ImageStyleUtils.getStyledHeight(400, 200, style));
    assertEquals(expectedValues[3], ImageStyleUtils.getStyledHeight(300, 400, style));
    assertEquals(expectedValues[4], ImageStyleUtils.getStyledHeight(300, 300, style));
    assertEquals(expectedValues[5], ImageStyleUtils.getStyledHeight(300, 200, style));
    assertEquals(expectedValues[6], ImageStyleUtils.getStyledHeight(200, 400, style));
    assertEquals(expectedValues[7], ImageStyleUtils.getStyledHeight(200, 300, style));
    assertEquals(expectedValues[8], ImageStyleUtils.getStyledHeight(200, 200, style));
  }

}
