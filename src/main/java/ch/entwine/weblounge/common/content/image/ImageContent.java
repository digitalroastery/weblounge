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

import ch.entwine.weblounge.common.content.file.FileContent;

/**
 * Describes the contents of an image resource, including the general attributes
 * such as file size and mime type. In addition, it contains technical
 * information like resolution, color model etc.
 */
public interface ImageContent extends FileContent {

  /**
   * Sets the image width in pixels.
   * 
   * @param width
   *          the image width
   */
  void setWidth(int width);

  /**
   * Returns the image width in pixels.
   * 
   * @return the width
   */
  int getWidth();

  /**
   * Sets the image height in pixels.
   * 
   * @param height
   *          the image height
   */
  void setHeight(int height);

  /**
   * Returns the image height in pixels.
   * 
   * @return the height
   */
  int getHeight();

  /**
   * Returns the shooting location
   * 
   * @return the shooting location
   */
  String getLocation();

  /**
   * Sets the shooting location
   * 
   * @param location
   *          the shooting location to set
   */
  void setLocation(String location);

  /**
   * @return the gpsLat
   */
  double getGpsLat();

  /**
   * @return the gpsLong
   */
  double getGpsLong();

  /**
   * Set the gps position
   * 
   * @param gpsLat
   *          the gpsLat to set
   * @param gpsLong
   *          the gpsLong to set
   */
  void setGpsPosition(double gpsLat, double gpsLong);

  /**
   * Returns the film speed used for this picture
   * 
   * @return the filmspeed
   */
  int getFilmspeed();

  /**
   * Sets the film speed used for this picture
   * 
   * @param filmspeed
   *          the filmspeed to set
   */
  void setFilmspeed(int filmspeed);

  /**
   * Returns the f-number used for this picture
   * 
   * @return the fnumber
   */
  float getFNumber();

  /**
   * Sets the f-number used for this picture
   * 
   * @param fnumber
   *          the f-number to set
   */
  void setFNumber(float fnumber);

  /**
   * Returns the focal width used for this picture
   * 
   * @return the focal width
   */
  int getFocalWidth();

  /**
   * Sets the focal width used for this picture
   * 
   * @param focalWidth
   *          the focal width to set
   */
  void setFocalWidth(int focalWidth);

  /**
   * Returns the exposure time used for this picture
   * 
   * @return the exposure time
   */
  float getExposureTime();

  /**
   * Sets the exposure time used for this picture
   * 
   * @param exposureTime
   *          the exposure time to set
   */
  void setExposureTime(float exposureTime);

}
