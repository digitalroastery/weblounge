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

package ch.entwine.weblounge.common.content.audiovisual;

import ch.entwine.weblounge.common.content.file.FileContent;

import java.util.Date;

/**
 * Describes the contents of an audio visual resource, including the general
 * attributes such as file size and mime type. In addition, it contains
 * technical information like resolution, bit rate, frame rate etc.
 */
public interface AudiovisualContent extends FileContent {

  /**
   * Sets the video width in pixels.
   * 
   * @param width
   *          the video width
   */
  void setWidth(int width);

  /**
   * Returns the video width in pixels.
   * 
   * @return the video width
   */
  int getWidth();

  /**
   * Sets the video height in pixels.
   * 
   * @param height
   *          the video height
   */
  void setHeight(int height);

  /**
   * Returns the video height in pixels.
   * 
   * @return the video height
   */
  int getHeight();

  /**
   * Returns the date the audio visual was recorded.
   * 
   * @return the recording date
   */
  Date getRecordingDate();

  /**
   * Sets the date the audio visual was recorded.
   * 
   * @param recordingDate
   *          the recording date
   */
  void setRecordingDate(Date recordingDate);

  /**
   * Returns the name of the shooting location.
   * 
   * @return the shooting location
   */
  String getLocation();

  /**
   * Sets the name of the shooting location.
   * 
   * @param location
   *          the shooting location
   */
  void setLocation(String location);

  /**
   * Returns the latitude of the GPS coordinates.
   * 
   * @return the latitude
   */
  double getGpsLat();

  /**
   * Returns the longitude of the GPS coordinates.
   * 
   * @return the longitude
   */
  double getGpsLong();

  /**
   * Sets the GPS position.
   * 
   * @param latitude
   *          the latitude
   * @param longitude
   *          the longitude
   */
  void setGpsPosition(double latitude, double longitude);

}
