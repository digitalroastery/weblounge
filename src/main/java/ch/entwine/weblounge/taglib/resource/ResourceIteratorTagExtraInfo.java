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

package ch.entwine.weblounge.taglib.resource;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * This class provides runtime information about the {@link ResourceIteratorTag}
 * jsp tag.
 */
public class ResourceIteratorTagExtraInfo extends TagExtraInfo {

  /** Variable that will hold the resource */
  public static final String RESOURCE = "resource";

  /** Variable that will hold the resource content */
  public static final String RESOURCE_CONTENT = "resourcecontent";

  /** The current index */
  public static final String INDEX = "index";

  /** The total number of values */
  public static final String ITERATIONS = "iterations";

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#getVariableInfo(javax.servlet.jsp.tagext.TagData)
   */
  @Override
  public VariableInfo[] getVariableInfo(TagData data) {
    return new VariableInfo[] {
        new VariableInfo(INDEX, Integer.class.getName(), true, VariableInfo.NESTED),
        new VariableInfo(ITERATIONS, Long.class.getName(), true, VariableInfo.NESTED),
        new VariableInfo(RESOURCE, Resource.class.getName(), true, VariableInfo.NESTED),
        new VariableInfo(RESOURCE_CONTENT, ResourceContent.class.getName(), true, VariableInfo.NESTED) };
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#isValid(javax.servlet.jsp.tagext.TagData)
   */
  @Override
  public boolean isValid(TagData data) {
    return true;
  }

}
