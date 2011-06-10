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

package ch.entwine.weblounge.common.content.page;

/**
 * The composer describes a content area on a page template, that can take a
 * number of pagelets which form the composer's content.
 */
public interface Composer {

  /**
   * Returns the composer identifier.
   * 
   * @return the identifier
   */
  String getIdentifier();

  /**
   * Returns the pagelets contained in the composer.
   * 
   * @return the pagelets
   */
  Pagelet[] getPagelets();

  /**
   * Returns the list of pagelets contained in the composer that match the type
   * specified by <code>module</code> and <code>renderer</code>. If there are no
   * matching pagelets, the method return an empty array.
   * 
   * @param module
   *          the module
   * @param renderer
   *          the renderer
   * @return the list of matching pagelets
   */
  Pagelet[] getPagelets(String module, String renderer);

  /**
   * Returns the pagelet at index <code>index</code>.
   * 
   * @param index
   *          the pagelet's position
   * @return the pagelet
   * @throws IndexOutOfBoundsException
   *           if there is no element at position <code>index</code>
   */
  Pagelet getPagelet(int index);

  /**
   * Returns the number of pagelets in the composer.
   * 
   * @return the composer's size
   */
  int size();

  /**
   * Returns an XML representation of this composer.
   * 
   * @return an XML representation of this composer
   */
  String toXml();

}
