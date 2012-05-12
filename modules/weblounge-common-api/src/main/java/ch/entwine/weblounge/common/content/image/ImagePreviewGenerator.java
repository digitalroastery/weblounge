/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.common.content.image;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Marker interface for image preview generators.
 */
public interface ImagePreviewGenerator extends PreviewGenerator {

  /**
   * Creates a preview image for a resource and writes the content to the
   * {@link OutputStream}.
   * 
   * @param imageFile
   *          the existing image
   * @param environment
   *          the environment
   * @param language
   *          the preview language
   * @param style
   *          the image style
   * @param format
   *          the output format. If <code>format</code> is <code>null</code>, it
   *          will be taken from the resource's mime type
   * @param is
   *          the resource content stream
   * @param os
   *          the output stream
   * @throws IOException
   *           if the resource content cannot be read
   */
  void createPreview(File imageFile, Environment environment,
      Language language, ImageStyle style, String format, InputStream is,
      OutputStream os) throws IOException;

}
