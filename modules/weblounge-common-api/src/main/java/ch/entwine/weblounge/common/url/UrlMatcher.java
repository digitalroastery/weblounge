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

package ch.entwine.weblounge.common.url;

import ch.entwine.weblounge.common.request.RequestFlavor;

/**
 * The matcher is used to define a space of urls within a certain domain. For
 * example, matchers are used to register an action with a certain url, e. g.
 * <code>/action/</code>, possibly including subsequent urls like
 * <code>/action/test</code> as well.
 */
public interface UrlMatcher {

  /**
   * Returns <code>true</code> if <code>url</code> matches the url space that is
   * defined by this matcher.
   * 
   * @param url
   *          the url to match against this url space
   * @param flavor
   *          the request flavor
   * @return <code>true</code> if <code>url</code> is within the defined url
   *         space
   */
  boolean matches(WebUrl url, RequestFlavor flavor);

  /**
   * Returns the url's mountpoint.
   * 
   * @return the mountpoint
   */
  String getMountpoint();

  /**
   * Returns the url's extension.
   * 
   * @return the extension
   */
  String getExtension();

}
