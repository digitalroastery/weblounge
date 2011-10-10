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

package ch.entwine.weblounge.common.content.movie;

/**
 * A video stream identifies a video track inside a movie container.
 */
public interface VideoStream extends Stream {

  /**
   * Returns the number of bits per second.
   * 
   * @return the bit rate
   */
  Float getBitRate();

  /**
   * Returns the number of frames per second.
   * 
   * @return the number of frames per second
   */
  Float getFrameRate();

  /**
   * Returns the frame width in pixel.
   * 
   * @return the frame width
   */
  Integer getFrameWidth();

  /**
   * Returns the frame height in pixel.
   * 
   * @return the frame height in pixel
   */
  Integer getFrameHeight();

  /**
   * Returns whether the video requires interlaced or progressive scanning.
   * 
   * @return the scan type
   */
  ScanType getScanType();

  /**
   * Sets the bit rate.
   * 
   * @param bitRate
   *          the bit rate
   */
  void setBitRate(float bitRate);

  /**
   * Sets the frame rate.
   * 
   * @param frameRate
   *          the frame rate
   */
  void setFrameRate(float frameRate);

  /**
   * Sets the frame width.
   * 
   * @param width
   *          the frame width
   */
  void setFrameWidth(int width);

  /**
   * Sets the frame height.
   * 
   * @param height
   *          the frame height
   */
  void setFrameHeight(int height);

  /**
   * Sets the scan type.
   * 
   * @param scanType
   *          the scan type
   */
  void setScanType(ScanType scanType);

  /**
   * Sets the encoding format.
   * 
   * @param format
   *          the format
   */
  void setFormat(String format);

}
