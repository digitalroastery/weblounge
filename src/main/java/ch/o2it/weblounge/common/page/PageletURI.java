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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.site.Site;

/**
 * This interface describes a pagelet's position with respect to its url,
 * composer and position within this composer.
 */
public interface PageletURI extends Comparable<PageletURI> {

  /** String identifier for the url */
  String URL = "url";

  /** String identifier for the composer */
  String COMPOSER = "composer";

  /** String identifier for the position */
  String POSITION = "position";

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
  void setURI(PageURI uri);

  /**
   * Returns the page where this pagelet is situated.
   * 
   * @return the pagelet uri
   */
  PageURI getPageURI();

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