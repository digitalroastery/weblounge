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

import ch.o2it.weblounge.common.user.User;

/**
 * Interface for listeners that are interested in changes within the repository.
 */
public interface RepositoryListener {

  /**
   * Callback for registered <code>RepositoryListeners</code> if a new resource
   * has been added to the repository.
   * 
   * @param resource
   *          the new resource
   * @param user
   *          the user
   */
  void resourceCreated(Resource resource, User user);

  /**
   * Callback for registered <code>RepositoryListeners</code> if an resource has
   * been removed from the repository.
   * 
   * @param resource
   *          the removed resource
   * @param user
   *          the user
   */
  void resourceRemoved(Resource resource, User user);

  /**
   * Callback for registered <code>RepositoryListeners</code> if an resource has
   * been removed from the repository.
   * 
   * @param resource
   *          the removed resource
   * @param oldURI
   *          the old resource uri
   * @param user
   *          the user
   */
  void resourceMoved(Resource resource, RepositoryURI oldURI, User user);

  /**
   * Callback for registered <code>RepositoryListeners</code> if an resource has
   * been updated.
   * 
   * @param resource
   *          the updated resource
   * @param user
   *          the user
   */
  void resourceChanged(Resource resource, User user);

  /**
   * Callback for registered <code>RepositoryListeners</code> if a new
   * collection has been added to the repository.
   * 
   * @param collection
   *          the new collection
   * @param user
   *          the user
   */
  void collectionCreated(Collection collection, User user);

  /**
   * Callback for registered <code>RepositoryListeners</code> if a collection
   * has been removed from the repository.
   * 
   * @param collection
   *          the removed collection
   * @param user
   *          the user
   */
  void collectionRemoved(Collection collection, User user);

  /**
   * Callback for registered <code>RepositoryListeners</code> if a collection
   * has been moved.
   * 
   * @param collection
   *          the moved collection
   * @param oldURI
   *          the old collection uri
   * @param user
   *          the user
   */
  void collectionMoved(Collection collection, RepositoryURI oldURI, User user);

  /**
   * Callback for registered <code>RepositoryListeners</code> if a collection
   * has been updated.
   * 
   * @param collection
   *          the updated collection
   * @param user
   *          the user
   */
  void collectionChanged(Collection collection, User user);

}