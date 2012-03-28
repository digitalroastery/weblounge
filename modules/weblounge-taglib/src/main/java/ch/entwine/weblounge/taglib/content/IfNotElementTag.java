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

package ch.entwine.weblounge.taglib.content;

/**
 * The tag body of this tag is executed if the element with the specified name
 * does not exist or equals the empty string.
 */
public class IfNotElementTag extends ElementCheckTag {

  /** Serial version uid */
  private static final long serialVersionUID = -2595592995249254903L;

  /**
   * Returns <code>true</code> if the element is not <code>null</code> and is
   * not equal to the empty string.
   * 
   * @param element
   *          the element to check
   * @see ch.entwine.weblounge.taglib.content.ElementCheckTag#skip(java.lang.String)
   */
  protected boolean skip(String element) {
    return (element != null && !element.equals("") && !element.equalsIgnoreCase("false"));
  }

}
