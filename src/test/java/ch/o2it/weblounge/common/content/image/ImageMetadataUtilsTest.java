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

import org.junit.Test;

import java.io.File;

/**
 * Test case for class {@link ImageMetadataUtils}.
 */
public class ImageMetadataUtilsTest {

  /** name of the test image with IPTC data */
  private String iptcImage = "/unihockey.jpg";
  
  /** name of the test image with GPS data */
  private String gpsImage = "/berg.jpg";

  /** Text of the sample legend */
  protected String legend = "Doppeltorschütze Nico Scalvinoni wird von Jonas Racine und Lukas Allamand beglückwünscht.";
  
  /** Sample photographer */
  protected String photographer = "Markus Jauss";
  
  @Test
  public void testExtractMetadata() {
    // test IPTC metadata
    File iptcImg = new File(ImageMetadataUtilsTest.class.getResource(iptcImage).getPath());
    ImageMetadata iptcMeta = ImageMetadataUtils.extractMetadata(iptcImg);
    assertNull(iptcMeta.getCaption());
    // assertEquals(legend, iptcMeta.getLegend());
    assertEquals(photographer, iptcMeta.getPhotographer());
    assertEquals(2.8, iptcMeta.getFNumber());
    assertEquals(200, iptcMeta.getFocalWidth());
    assertEquals(1250, iptcMeta.getFilmspeed());
    assertEquals(0.002, iptcMeta.getExposureTime());
    
    // test GPS metadata
    File gpsImg = new File(ImageMetadataUtilsTest.class.getResource(gpsImage).getPath());
    ImageMetadata gpsMeta = ImageMetadataUtils.extractMetadata(gpsImg);
    assertEquals(46.9338333333, gpsMeta.getGpsLat());
    assertEquals(9.1316666667, gpsMeta.getGpsLong());
  }

}
