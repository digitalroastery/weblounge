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
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A preview generator will create preview images for a resource.
 */
public interface PreviewGenerator {

  /**
   * Returns <code>true</code> if the preview generator supports creating a
   * preview for the given resource and language.
   * 
   * @param resource
   *          the resource
   * @return <code>true</code> if creating a preview is supported
   */
  boolean supports(Resource<?> resource);

  /**
   * Creates a preview image for a resource and writes the content to the
   * {@link OutputStream}.
   * 
   * @param resource
   *          the resource
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
  void createPreview(Resource<?> resource, Environment environment,
      Language language, ImageStyle style, String format, InputStream is,
      OutputStream os) throws IOException;

  /**
   * Creates a filename for the resource preview based on the resource itself,
   * the language and the image style.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @param style
   *          the image style
   * @return the filename
   */
  String getContentType(Resource<?> resource, Language language,
      ImageStyle style);

  /**
   * Returns the suffix for the preview's filename based on the resource itself,
   * the language and the image style.
   * 
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @param style
   *          the image style
   * @return the filename
   */
  String getSuffix(Resource<?> resource, Language language, ImageStyle style);

  /**
   * Returns the priority that applies if there is more than one preview
   * generator that is capable of generating a certain preview.
   * <p>
   * Higher values are better.
   * 
   * @return the generator's priority
   */
  int getPriority();

}
