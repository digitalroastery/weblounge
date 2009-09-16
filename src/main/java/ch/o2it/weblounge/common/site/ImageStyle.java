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

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;

/**
 * The <code>ImageStyle</code> class defines a presentation style for images of
 * a certain size as well as the method to get from the original image verision
 * to scaled ones.
 */
public interface ImageStyle extends Localizable {

  /**
   * This constants defines no scaling.
   */
  public final static int SCALE_NONE = -1;

  /**
   * This constants defines the scaling mode for the generated images of this
   * type such that the image is being scaled to fill the whole rectangle
   * defined by width and height properties. <br>
   * Like this, the image will eventually have to be cropped on the overlapping
   * side but the resulting images are all exactly of the dimensions
   * <tt>(width, height)</tt>.
   */
  public final static int SCALE_TO_FILL = 0;

  /**
   * This constants defines the scaling mode for the generated images of this
   * type such that the image is being scaled to fit the maximum of width and
   * height. Like this, the image will eventually not fill the whole rectangle,
   * but all image data is kept. <br>
   * This is the default scaling mode.
   */
  public final static int SCALE_TO_FIT = 1;

  /**
   * Sets the scale mode for this image style. For the mode, use one out of
   * <ul>
   * <li>{@link #SCALE_TO_FIT} - Scales until either both width and height fit
   * the bounds</li>
   * <li>{@link #SCALE_TO_FILL} - Scales until the smaller of width and height
   * fit and the crops the image</li>
   * </ul>
   * 
   * @param mode
   *          the scaling mode
   */
  void setScalingMode(int mode);

  /**
   * Returns the scaling mode for this image style.
   * 
   * @return the scaling mode
   */
  int getScalingMode();

  /**
   * Returns the image height.
   * 
   * @return the image height
   */
  int getHeight();

  /**
   * Returns the style identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Returns the style name.
   * 
   * @return the style name
   */
  String getName();

  /**
   * Returns the style name in the required language.
   * 
   * @param language
   *          the required language
   * @return the style name
   */
  String getName(Language language);

  /**
   * Returns the image width.
   * 
   * @return the image width
   */
  int getWidth();

  /**
   * Returns <code>true</code> if the style is composeable. Non composeable
   * style may be used internally by image galleries etc.
   * 
   * @return <code>true</code> if the style is composeable
   */
  boolean isComposeable();

}