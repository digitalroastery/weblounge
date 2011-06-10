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
  
}
