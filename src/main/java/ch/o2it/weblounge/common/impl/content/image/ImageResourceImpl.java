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

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.impl.content.file.FileResourceImpl;

import java.net.URL;

/**
 * Default implementation of an image resource.
 */
public class ImageResourceImpl extends FileResourceImpl implements ImageResource {

  /** The width in pixels */
  protected int width = 0;

  /** The width in pixels */
  protected int height = 0;

  /**
   * Creates a new image with the given uri.
   * 
   * @param uri
   *          the image uri
   */
  public ImageResourceImpl(ResourceURI uri) {
    super(uri, null);
  }

  /**
   * Creates a new image with the given uri.
   * 
   * @param uri
   *          the image uri
   * @param contentUrl
   *          <code>URL</code> to the file's content
   */
  public ImageResourceImpl(ResourceURI uri, URL contentUrl) {
    super(uri, contentUrl);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.image.ImageResource#setWidth(int)
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.image.ImageResource#getWidth()
   */
  public int getWidth() {
    return width;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.image.ImageResource#setHeight(int)
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.image.ImageResource#getHeight()
   */
  public int getHeight() {
    return height;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.ResourceImpl#toXmlHead(java.lang.StringBuffer)
   */
  @Override
  protected void toXmlHead(StringBuffer buffer) {
    super.toXmlHead(buffer);

    // Image resolution
    if (width > 0 && height > 0) {
      buffer.append("<width>").append(width).append("</width>");
      buffer.append("<height>").append(height).append("</height>");
    }
  }

}
