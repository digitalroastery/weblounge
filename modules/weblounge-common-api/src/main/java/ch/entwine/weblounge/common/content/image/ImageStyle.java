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

import ch.entwine.weblounge.common.content.Composeable;
import ch.entwine.weblounge.common.site.ImageScalingMode;

import java.util.List;

/**
 * The <code>ImageStyle</code> defines a presentation style for images of a
 * certain size as well as the method to get from the original image version to
 * scaled ones.
 */
public interface ImageStyle extends Composeable {

  /**
   * Returns the list of resources that this image style is associated with or
   * an empty list if the image style applies to all possible resources.
   * 
   * @return the list of contexts
   */
  List<String> getContexts();

  /**
   * Returns <code>true</code> if previews are automatically being generated for
   * this context.
   * 
   * @param context
   *          the context
   * @return <code>true</code> to automatically create previews
   * @throws IllegalArgumentException
   *           if <code>context</code> is blank
   */
  boolean createPreview(String context);

  /**
   * Sets the scaling mode for this image style. The scaling mode defines which
   * of the dimensions need to be strictly respected when the image is scaled.
   * 
   * @param mode
   *          the scaling mode
   */
  void setScalingMode(ImageScalingMode mode);

  /**
   * Returns the scaling mode for this image style.
   * 
   * @return the scaling mode
   */
  ImageScalingMode getScalingMode();

  /**
   * Sets the image height.
   * 
   * @param height
   *          the height
   */
  void setHeight(int height);

  /**
   * Returns the image height.
   * 
   * @return the image height
   */
  int getHeight();

  /**
   * Sets the image width.
   * 
   * @param width
   *          the width
   */
  void setWidth(int width);

  /**
   * Returns the image width.
   * 
   * @return the image width
   */
  int getWidth();

  /**
   * Returns the scale for the original <code>width</code> and
   * <code>height</code>.
   * 
   * @param width
   *          the original width
   * @param height
   *          the original height
   * @return the scaling factor
   */
  float scale(int width, int height);

  /**
   * Returns an xml representation of this image style.
   * 
   * @return the xml representation
   */
  String toXml();

}