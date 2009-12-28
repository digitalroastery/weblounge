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
 * The <code>ImageStyle</code> defines a presentation style for images of a
 * certain size as well as the method to get from the original image version to
 * scaled ones.
 */
public interface ImageStyle extends Localizable {

  /**
   * Sets the scaling mode for this image style. The scaling mode defines which
   * of the dimensions need to be strictly respected when the image is scaled.
   * 
   * @param mode
   *          the scaling mode
   */
  void setScalingMode(ScalingMode mode);

  /**
   * Returns the scaling mode for this image style.
   * 
   * @return the scaling mode
   */
  ScalingMode getScalingMode();

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
   * Set to <code>true</code> if this image style should available to the user
   * when it comes to placing images on a page.
   * 
   * @param composeable
   *          <code>true</code> if the image style might be selected by the user
   */
  void setComposeable(boolean composeable);

  /**
   * Returns <code>true</code> if the style is composeable. Non composeable
   * style may be used internally by image galleries etc.
   * 
   * @return <code>true</code> if the style is composeable
   */
  boolean isComposeable();

  /**
   * Returns the style identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Sets the name in the specified language.
   * 
   * @param name
   *          the name
   * @param language
   *          the language
   */
  void setName(String name, Language language);

  /**
   * Returns the style name.
   * 
   * @return the style name
   */
  String getName();

  /**
   * Returns the style name in the required language. If not available, the name
   * will be returned in the default language.
   * 
   * @param language
   *          the required language
   * @return the style name
   */
  String getName(Language language);

  /**
   * Returns the style name in the required language. If not available, either
   * <code>null</code> or the name in a fallback language will be returned,
   * depending on the value of <code>force</code>.
   * 
   * @param language
   *          the required language
   * @param force
   *          <code>true</code> to force a <code>null</code> value rather then a
   *          fallback language
   * @return the style name
   */
  String getName(Language language, boolean force);

  /**
   * Returns an xml representation of this image style.
   * 
   * @return the xml representation
   */
  String toXml();

}