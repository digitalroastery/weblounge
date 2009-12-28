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

package ch.o2it.weblounge.common.site;

/**
 * The <code>ScalingMode</code> defines how an image will be scaled with respect
 * to the <code>ImageStyle</code> that is used.
 */
public enum ScalingMode {

  /**
   * Scales the image until the width defined by the image style is reached.
   * <p>
   * If after scaling, the image height is larger than defined by the image
   * style, it is cropped. If it is smaller, then the resulting image will be of
   * less height than specified.
   */
  Width,

  /**
   * Scales the image until the height defined by the image style is reached.
   * <p>
   * If after scaling, the image width is larger than defined by the image
   * style, it is cropped. If it is smaller, then the resulting image will be of
   * less width than specified.
   */
  Height,

  /**
   * The image is scaled in such a way that the complete rectangle as defined by
   * the image style's width and height are covered.
   * <p>
   * Any overlapping parts of the image in either horizontal or vertical
   * direction are cropped.
   */
  Fill,

  /**
   * The image is scaled in such a way that the complete rectangle as defined by
   * the image style's width and height are covered.
   * <p>
   * Any overlapping parts of the image in either horizontal or vertical
   * direction are kept, and so the image will either be perfectly, matching,
   * larger or wider than the rectangle defined by the image style.
   */
  Cover,

  /**
   * No scaling will occur.
   */
  None;

  /**
   * Returns a scaling mode by matching <code>value</code> against the available
   * scaling modes.
   * 
   * @param value
   *          the value
   * @return the scaling mode
   */
  public static ScalingMode parseString(String value) {
    if (Width.toString().equalsIgnoreCase(value))
      return Width;
    else if (Height.toString().equalsIgnoreCase(value))
      return Height;
    else if (Fill.toString().equalsIgnoreCase(value))
      return Fill;
    else if (Cover.toString().equalsIgnoreCase(value))
      return Cover;
    else if (None.toString().equalsIgnoreCase(value))
      return None;
    throw new IllegalArgumentException("Scaling mode " + value + " is unknown");
  }

}
