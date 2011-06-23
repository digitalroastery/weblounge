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

package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.content.image.ImageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A preview generator will create preview images for a resource.
 */
public interface PreviewGenerator {

  /**
   * Creates a preview image for a resource and returns the preview contents as
   * an {@link OutputStream} or <code>null</code> if the resource cannot be
   * handled.
   * 
   * @param resource
   *          the resource
   * @param style
   *          the image style
   * @param format
   *          the image format
   * @return the generated image
   * @throws IOException
   *           if the resource content cannot be read
   */
  OutputStream createPreview(InputStream resource, ImageStyle style, String format)
      throws IOException;

}
