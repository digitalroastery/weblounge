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

package ch.entwine.weblounge.common.site;

import java.net.URL;

/**
 * A site url specifies the url under which a site can be reached in a given
 * environment.
 */
public interface SiteURL {

  /**
   * Returns the site's url.
   * 
   * @return the url
   */
  URL getURL();

  /**
   * Returns <code>true</code> if this is the site's default url.
   * 
   * @return <code>true</code> if this is the default url
   */
  boolean isDefault();

  /**
   * Returns the environment that is associated with the given url.
   * 
   * @return the environment
   */
  Environment getEnvironment();

  /**
   * Returns the external form as returned by
   * {@link java.net.URL#toExternalForm()}.
   * 
   * @return the external form
   */
  String toExternalForm();

}
