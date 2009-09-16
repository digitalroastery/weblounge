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
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * This interface is used to locate and point to pages within a site.
 */
public interface PageURI {

  /**
   * Returns the id of the page that this uri is pointing to.
   * 
   * @return the id
   * @throws MalformedPageURIException
   */
  long getId() throws MalformedPageURIException;

  /**
   * Returns the path of the page that this uri is pointing to.
   * 
   * @return the path
   * @throws MalformedPageURIException
   */
  String getPath() throws MalformedPageURIException;

  /**
   * Returns the page version.
   * 
   * @return the version
   * @throws MalformedPageURIException
   */
  long getVersion() throws MalformedPageURIException;

  /**
   * Returns the page uri in the given version.
   * 
   * @return the new page uri
   * @throws MalformedPageURIException
   */
  PageURI getVersion(long version) throws MalformedPageURIException;

  /**
   * Returns a link to the page that this uri is pointing to.
   * 
   * @return a link
   * @throws MalformedPageURIException
   */
  WebUrl getLink() throws MalformedPageURIException;

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  Site getSite() throws MalformedPageURIException;

  /**
   * Returns <code>true</code> if the page described by this resource exists.
   * 
   * @return <code>true</code> if the item or page exists
   */
  boolean exists() throws MalformedPageURIException;

  /**
   * Returns the URI for the parent page.
   * 
   * @return the parent uri
   */
  PageURI getParentURI() throws MalformedPageURIException;

}