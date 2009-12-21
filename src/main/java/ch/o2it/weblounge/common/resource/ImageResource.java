/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.resource;

import java.io.File;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * This interface defines the fields and methods for an image resource.
 */
public interface ImageResource extends Resource {

  /**
   * Returns the image size in bytes for the image depending on the given image
   * style.
   * 
   * @param style
   *          the image style to use
   * @return the image size in bytes.
   */
  public abstract long getSize(ImageStyle style, Language language);

  /**
   * Returns the link to the image.
   * 
   * @param style
   *          the style to use
   * @param language
   *          the language
   * @return the image link
   */
  public abstract WebUrl getLink(ImageStyle style, Language language);

  /**
   * Returns the image's file handle.
   * 
   * @param style
   *          the style to use
   * @return the file handle
   */
  public abstract File getFile(ImageStyle style, Language language);

  /**
   * Returns the image width in pixels.
   * 
   * @return the image width
   */
  public abstract int getWidth();

  /**
   * Returns the image width in pixels for the image rendererd with the given
   * style.
   * 
   * @param style
   *          the image style
   * @param language
   *          the language
   * @return the image width
   */
  public abstract int getWidth(ImageStyle style, Language language);

  /**
   * Returns the image height in pixels.
   * 
   * @return the image height
   */
  public abstract int getHeight();

  /**
   * Returns the image height in pixels for the image rendered with the given
   * style.
   * 
   * @param style
   *          the image style
   * @param language
   *          the language
   * @return the image height
   */
  public abstract int getHeight(ImageStyle style, Language language);

}