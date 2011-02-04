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

package ch.o2it.weblounge.workbench;

/**
 * A suggestion is an object that is returned to a user based on an initial hint
 * that was given, usually used in the context of auto complete fields.
 */
public interface Suggestion {

  /**
   * Returns an <code>XML</code> encoded and well-formed version of the
   * suggestion.
   * <p>
   * If <code>highlightTag</code> is not <code>null</code>, the implementation
   * is asked to add match highlighting to the suggested data.
   * 
   * @param hint
   *          the original hint
   * @param highlightTag
   *          the tag name used for highlighting
   * @return the suggestion as an <code>XML</code> string
   */
  String toXml(String hint, String highlightTag);

}
