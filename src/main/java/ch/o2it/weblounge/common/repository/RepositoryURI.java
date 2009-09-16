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

package ch.o2it.weblounge.common.repository;

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * This interface is used to locate and point to collections and items within
 * the repository.
 */
public interface RepositoryURI {

  /**
   * Returns the id of the repository collection or item that this uri is
   * pointing to.
   * 
   * @return the id
   */
  long getId() throws MalformedRepositoryURIException;

  /**
   * Returns the path of the repository collection or item that this uri is
   * pointing to.
   * 
   * @return the path
   */
  String getPath() throws MalformedRepositoryURIException;

  /**
   * Returns the collection or item name, which is denoted by the last path
   * element.
   * 
   * @return the collection or item name
   * @throws MalformedRepositoryURIException
   */
  String getName() throws MalformedRepositoryURIException;

  /**
   * Returns a link to the collection or item that this uri is pointing to.
   * 
   * @return a link
   * @throws MalformedRepositoryURIException
   */
  WebUrl getLink() throws MalformedRepositoryURIException;

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  Site getSite() throws MalformedRepositoryURIException;

  /**
   * Returns <code>true</code> if this uri points to a
   * <code>RepositoryCollection</code>.
   * 
   * @return <code>true</code> if the uri points to a collection
   * @throws MalformedRepositoryURIException
   */
  public boolean isCollectionURI() throws MalformedRepositoryURIException;

  /**
   * Returns <code>true</code> if this uri points to a
   * <code>RepositoryItem</code>.
   * 
   * @return <code>true</code> if the uri points to an item
   * @throws MalformedRepositoryURIException
   */
  boolean isItemURI() throws MalformedRepositoryURIException;

  /**
   * Returns <code>true</code> if the collection or item described by this
   * resource exists.
   * 
   * @return <code>true</code> if the item or collection exists
   */
  boolean exists() throws MalformedRepositoryURIException;

  /**
   * Returns the URI for the parent collection.
   * 
   * @return the parent uri
   */
  RepositoryURI getParentURI() throws MalformedRepositoryURIException;

}