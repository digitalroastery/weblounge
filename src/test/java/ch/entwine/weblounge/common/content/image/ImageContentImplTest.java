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

  /** The source file */
  protected String source = "http://entwinemedia.com/imagexyz.ogg";

  /** The image width */
  protected int width = 2188;

  /** The image width */
  protected int height = 1446;

  /** photographer who has shot the picture */
  protected String photographer = "Hans Muster";

  /** date where the picture was taken */
  protected Date dateTaken = new Date(1232358721000L);

  /** location where the picture was taken */
  protected String location = "Zürich";

  /** GPS latitude of the place where the picture was taken */
  protected double gpsLat = 47.376409;

  /** GPS longitude of the place where the picture was taken */
  protected double gpsLong = 8.547750;

  /** film speed with which the picture was taken */
  protected int filmspeed = 23;

  /** f-number with which the picture was taken */
  protected float fnumber = 4.0F;

  /** focal width with which the picture was taken */
  protected int focalWidth = 18;

  /** exposure time used while taking the picture */
  protected float exposureTime = 1.3F;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    image = new ImageContentImpl(filename, german, mimetype, width, height, size);
    image.setAuthor(photographer);
    image.setDateTaken(dateTaken);
    image.setSource(source);
    image.setLocation(location);
    image.setGpsPosition(gpsLat, gpsLong);
    image.setFilmspeed(filmspeed);
    image.setFNumber(fnumber);
    image.setFocalWidth(focalWidth);
    image.setExposureTime(exposureTime);
    ((ImageContentImpl) image).setCreated(creationDate, amelie);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getLanguage()}
   * .
   */
  @Test
  public void testGetLanguage() {
    assertEquals(german, image.getLanguage());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getMimetype()}
   * .
   */
  @Test
  public void testGetMimetype() {
    assertEquals(mimetype, image.getMimetype());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getSource()}
   * .
   */
  @Test
  public void testGetSource() {
    assertEquals(source, image.getSource());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getFilename()}
   * .
   */
  @Test
  public void testGetFilename() {
    assertEquals(filename, image.getFilename());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getAuthor()}
   * .
   */
  @Test
  public void testGetAuthor() {
    assertEquals(photographer, image.getAuthor());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getSize()}
   * .
   */
  @Test
  public void testGetSize() {
    assertEquals(size, image.getSize());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getWidth()}
   * .
   */
  @Test
  public void testGetWidth() {
    assertEquals(width, image.getWidth());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getHeight()}
   * .
   */
  @Test
  public void testGetHeight() {
    assertEquals(height, image.getHeight());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getDateTaken()}
   * .
   */
  @Test
  public void testGetDateTaken() {
    assertEquals(dateTaken, image.getDateTaken());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getLocation()}
   * .
   */
  @Test
  public void testGetLocation() {
    assertEquals(location, image.getLocation());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getGpsLat()}
   * .
   */
  @Test
  public void testGetGpsLat() {
    assertEquals(gpsLat, image.getGpsLat());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getGpsLong()}
   * .
   */
  @Test
  public void testGetGpsLong() {
    assertEquals(gpsLong, image.getGpsLong());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getFilmspeed()}
   * .
   */
  @Test
  public void testGetFilmspeed() {
    assertEquals(filmspeed, image.getFilmspeed());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getFNumber()}
   * .
   */
  @Test
  public void testGetFNumber() {
    assertEquals(fnumber, image.getFNumber());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getFocalWidth()}
   * .
   */
  @Test
  public void testGetFocalWidth() {
    assertEquals(focalWidth, image.getFocalWidth());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.image.ImageContentImpl#getExposureTime()}
   * .
   */
  @Test
  public void testGetExposureTime() {
    assertEquals(exposureTime, image.getExposureTime());
  }

}
