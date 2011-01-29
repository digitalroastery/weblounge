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

package ch.o2it.weblounge.common.content.image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import ch.o2it.weblounge.common.impl.content.image.ImageMetadata;
import ch.o2it.weblounge.common.impl.content.image.ImageMetadataUtils;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Test case for class {@link ImageMetadataUtils}.
 */
public class ImageMetadataUtilsTest {

  /** name of the test image with IPTC data */
  private String iptcImage = "/unihockey.jpg";

  /** Text of the sample legend */
  protected String legend = "Doppeltorschütze Nico Scalvinoni wird von Jonas Racine und Lukas Allamand beglückwünscht.";
  
  /** Sample photographer */
  protected String photographer = "Markus Jauss";
  
  @Ignore
  @Test
  public void testExtractMetadata() {
    File img = new File(ImageMetadataUtilsTest.class.getResource(iptcImage).getPath());
    ImageMetadata meta = ImageMetadataUtils.extractMetadata(img);
    assertNull(meta.getCaption());
    assertEquals(legend, meta.getLegend());
    assertEquals(photographer, meta.getPhotographer());
    assertEquals(2.8, meta.getFNumber());
    assertEquals(200, meta.getFocalWidth());
    assertEquals(1250, meta.getFilmspeed());
    assertEquals(0.002, meta.getExposureTime());
  }

}
