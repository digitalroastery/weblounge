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

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.site.Site;

/**
 * A pagelet uri describes the position of a pagelet with regard to the page
 * it's on, the composer it's in and the position within that composer.
 */
public interface PageletURI extends Comparable<PageletURI> {

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Sets the page uri.
   * 
   * @param uri
   *          the page uri
   */
  void setURI(ResourceURI uri);

  /**
   * Returns the page where this pagelet is situated.
   * 
   * @return the pagelet uri
   */
  ResourceURI getPageURI();

  /**
   * Sets the composer containing the pagelet.
   * 
   * @param composer
   *          the composer
   */
  void setComposer(String composer);

  /**
   * Returns the pagelet's composer
   * 
   * @return the composer
   */
  String getComposer();

  /**
   * Sets the pagelets position within the composer.
   * 
   * @param position
   *          the position within the composer
   */
  void setPosition(int position);

  /**
   * Returns the pagelet's position within the composer.
   * 
   * @return the position
   */
  int getPosition();

}