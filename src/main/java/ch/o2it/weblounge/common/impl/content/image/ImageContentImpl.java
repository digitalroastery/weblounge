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

package ch.o2it.weblounge.common.impl.content.image;

import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.impl.content.file.FileContentImpl;
import ch.o2it.weblounge.common.language.Language;

/**
 * Default implementation of an image resource content.
 */
public class ImageContentImpl extends FileContentImpl implements ImageContent {

  /** The image width in pixels */
  protected int width = -1;

  /** The image height in pixels */
  protected int height = -1;

  /**
   * Creates a new image content representation.
   * 
   * @param language
   *          the language
   */
  public ImageContentImpl(Language language) {
    super(language);
  }

  /**
   * Creates a new image content representation.
   * 
   * @param filename
   *          the original filename
   * @param language
   *          the language
   * @param filesize
   *          the file size in bytes
   */
  public ImageContentImpl(String filename, Language language, long filesize) {
    super(filename, language, filesize);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.image.ImageContent#setWidth(int)
   */
  public void setWidth(int width) {
    if (width <= 0)
      throw new IllegalArgumentException("Image must be wider than 0 pixels");
    this.width = width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.image.ImageContent#getWidth()
   */
  public int getWidth() {
    return width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.image.ImageContent#setHeight(int)
   */
  public void setHeight(int height) {
    if (height <= 0)
      throw new IllegalArgumentException("Image must be taller than 0 pixels");
    this.height = height;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.image.ImageContent#getHeight()
   */
  public int getHeight() {
    return height;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.ResourceContentImpl#addXml(java.lang.StringBuffer)
   */
  @Override
  protected StringBuffer addXml(StringBuffer xml) {
    if (width <= 0)
      throw new IllegalArgumentException("Image must be wider than 0 pixels");
    if (height <= 0)
      throw new IllegalArgumentException("Image must be taller than 0 pixels");
    xml.append("<width>").append(width).append("</width>");
    xml.append("<height>").append(height).append("</height>");
    return xml;
  }

}
