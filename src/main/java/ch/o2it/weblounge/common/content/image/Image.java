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

import ch.o2it.weblounge.common.content.resource.File;

/**
 * Object representation of an image resource.
 */
public interface Image extends File {

  /**
   * Sets the image's width in pixels.
   * 
   * @param width
   *          the image width
   */
  void setWidth(int width);

  /**
   * Returns the image width in pixels.
   * 
   * @return the image width
   */
  int getWidth();

  /**
   * Sets the image's height in pixels.
   * 
   * @param height
   *          the image height
   */
  void setHeight(int height);

  /**
   * Returns the image height in pixels.
   * 
   * @return the image height
   */
  int getHeight();

}
