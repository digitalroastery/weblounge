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

import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.SecurityException;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import java.io.IOException;
import java.util.Iterator;

/**
 * A repository gives access to a site's files and images. Use the sites
 * <code>getRepository()</code> method to obtain a reference.
 */
public interface RepositoryManager {

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Returns the repository's mountpoint relative to the site root.
   * 
   * @return the repository mountpoint
   */
  WebUrl getMountpoint();

  /**
   * Adds a <code>RepositoryListener</code>.
   * 
   * @param listener
   *          the listener to add
   */
  void addRepositoryListener(RepositoryListener listener);

  /**
   * Removes the given <code>RepositoryListener</code>.
   * 
   * @param listener
   *          the listener to remove
   */
  void removeRepositoryListener(RepositoryListener listener);

  /**
   * Adds a <code>RepositoryTransactionListener</code>.
   * 
   * @param listener
   *          the listener to add
   */
  void addTransactionListener(RepositoryTransactionListener listener);

  /**
   * Removes the given <code>RepositoryTransactionListener</code>.
   * 
   * @param listener
   *          the listener to remove
   */
  void removeTransactionListener(RepositoryTransactionListener listener);

  /**
   * Returns the repository's root collection.
   * 
   * @param user
   *          the user
   * @param permission
   *          the permission
   * @return the root collection
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if retreival fails due to a database error
   */
  Collection getRootCollection(User user, Permission permission)
      throws SecurityException, IOException;

  /**
   * Returns the collection which is identified by the given uri or
   * <code>null</code> if no such collection exists.
   * 
   * @param uri
   *          the uri
   * @param user
   *          the user
   * @param permission
   *          the permission
   * @return the collection
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if retreival fails due to a database error
   */
  Collection getCollection(RepositoryURI uri, User user, Permission permission)
      throws SecurityException, IOException;

  /**
   * Creates a collection at the location identified by <code>uri</code>.
   * <p>
   * <strong>Note:</strong> If a collection with this path already exists, then
   * the existing collection is returned.
   * 
   * @param uri
   *          the uri
   * @param user
   *          the user
   * @return the new collection
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if creation fails due to a database error
   */
  Collection createCollection(RepositoryURI uri, User user)
      throws SecurityException, IOException;

  /**
   * Moves collection identified by <code>from</code> to the new location
   * <code>to</code>. Any parent collections are inherently created.
   * 
   * @param from
   *          the source collection
   * @param to
   *          the target location
   * @param user
   *          the user
   * @param uri
   *          the new location
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if moving fails due to a database error
   */
  boolean moveCollection(RepositoryURI from, RepositoryURI to, User user)
      throws SecurityException, IOException;

  /**
   * Removes the collection, including any subcollections and enclosed
   * resources.
   * 
   * @param uri
   *          the collection uri
   * @param user
   *          the user
   * @throws RepositoryException
   *           if the collection cannot be removed
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if removing fails due to a database error
   */
  boolean removeCollection(RepositoryURI uri, User user)
      throws SecurityException, IOException;

  /**
   * Returns the resource which is identified by the given uri or
   * <code>null</code> if no such resource exists.
   * 
   * @param uri
   *          the uri
   * @param user
   *          the user
   * @param permission
   * @return the resource
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if retreival fails due to a database error
   */
  Resource getResource(RepositoryURI uri, User user, Permission permission)
      throws SecurityException, IOException;

  /**
   * Returns all collections contained in the collection which is identified by
   * <code>collectionURI</code>.
   * <p>
   * The iteration only contains collections which are accessible by
   * <code>user</code> with respect to <code>permission</code>.
   * <p>
   * A <code>SecurityException</code> is thrown if the collection identified by
   * <code>collectionURI</code> is not accessible for reading.
   * 
   * @param collectionURI
   *          uri of the parent collection
   * @param user
   *          the user accessing the collection
   * @param the
   *          permission needed
   * @return the resources
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if retreival fails due to a database error
   */
  Iterator<Collection> collections(RepositoryURI collectionURI, User user,
      Permission permission) throws SecurityException, IOException;

  /**
   * Returns a filtered iteration of collections contained in the collection
   * which is identified by <code>collectionURI</code>.
   * <p>
   * The iteration only contains collections which are accessible by
   * <code>user</code> with respect to <code>permission</code>.
   * <p>
   * A <code>SecurityException</code> is thrown if the collection identified by
   * <code>collectionURI</code> is not accessible for reading.
   * 
   * @param collectionURI
   *          uri of the parent collection
   * @param filter
   *          the filter
   * @param user
   *          the user accessing the collection
   * @param the
   *          permission needed
   * @return an iteration of the filtered resources
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if retreival fails due to a database error
   */
  Iterator<Collection> collections(RepositoryURI collectionURI,
      CollectionFilter filter, User user, Permission permission)
      throws SecurityException, IOException;

  /**
   * Returns all resources contained in the collection which is identified by
   * <code>collectionURI</code>.
   * <p>
   * The iteration only contains resources which are accessible by
   * <code>user</code> with respect to <code>permission</code>.
   * <p>
   * A <code>SecurityException</code> is thrown if the collection identified by
   * <code>collectionURI</code> is not accessible for reading.
   * 
   * @param collectionURI
   *          uri of the parent collection
   * @param user
   *          the user accessing the collection
   * @param the
   *          permission needed
   * @return the resources
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if retreival fails due to a database error
   */
  Iterator<Resource> resources(RepositoryURI collectionURI, User user,
      Permission permission) throws SecurityException, IOException;

  /**
   * Returns a filtered iteration of resources contained in the collection which
   * is identified by <code>collectionURI</code>.
   * <p>
   * The iteration only contains resources which are accessible by
   * <code>user</code> with respect to <code>permission</code>.
   * <p>
   * A <code>SecurityException</code> is thrown if the collection identified by
   * <code>collectionURI</code> is not accessible for reading.
   * 
   * @param collectionURI
   *          uri of the parent collection
   * @param filter
   *          the filter
   * @param user
   *          the user accessing the collection
   * @param the
   *          permission needed
   * @return an iteration of the filtered resources
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if retreival fails due to a database error
   */
  Iterator<Resource> resources(RepositoryURI collectionURI,
      ResourceFilter filter, User user, Permission permission)
      throws SecurityException, IOException;

  /**
   * Creates an resource at the location identified by <code>uri</code>. Any
   * parent collections are inherently created.
   * <p>
   * <strong>Note:</strong> If a resource with this path already exists, then
   * the existing resource is returned.
   * 
   * @param uri
   *          the uri
   * @param user
   *          the user
   * @return the new resource
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if creation fails due to a database error
   */
  Resource createResource(RepositoryURI uri, User user)
      throws SecurityException, IOException;

  /**
   * Moves resource <code>from</code> to the new location identified by
   * <code>to</code>.
   * 
   * @param from
   *          the resource to move
   * @param to
   *          the target location
   * @param user
   *          the user
   * @param uri
   *          the new location
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if moving fails due to a database error
   */
  boolean moveResource(RepositoryURI from, RepositoryURI to, User user)
      throws SecurityException, IOException;

  /**
   * Removes the resource, including any subresources and enclosed resources.
   * 
   * @param uri
   *          the resource uri
   * @param user
   *          the user
   * @throws RepositoryException
   *           if the resource cannot be removed
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if removing fails due to a database error
   */
  boolean removeResource(RepositoryURI uri, User user)
      throws SecurityException, IOException;

}