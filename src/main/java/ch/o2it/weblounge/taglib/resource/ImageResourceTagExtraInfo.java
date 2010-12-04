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

package ch.o2it.weblounge.taglib.resource;

import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * This class provides runtime information about the {@link ImageResourceTag}
 * jsp tag.
 */
public class ImageResourceTagExtraInfo extends TagExtraInfo {

  /** Variable that will hold the image resource */
  public static final String IMAGE = "image";

  /** Variable that will hold the image resource content */
  public static final String IMAGE_CONTENT = "imagecontent";

  /**
   * Returns the information on the exported tag variables.
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#getVariableInfo(javax.servlet.jsp.tagext.TagData)
   */
  public VariableInfo[] getVariableInfo(TagData tagData) {
    return new VariableInfo[] {
        new VariableInfo(IMAGE, ImageResource.class.getName(), true, VariableInfo.NESTED),
        new VariableInfo(IMAGE_CONTENT, ImageContent.class.getName(), true, VariableInfo.NESTED)
    };
  }

  /**
   * @see javax.servlet.jsp.tagext.TagExtraInfo#isValid(javax.servlet.jsp.tagext.TagData)
   */
  public boolean isValid(TagData tagData) {
    return true;
  }

}
