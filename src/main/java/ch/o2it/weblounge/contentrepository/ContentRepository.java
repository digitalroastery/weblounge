/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.contentrepository;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.user.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Iterator;

/**
 * A content repository stores resources and resources that represent the
 * content of <code>Site</code>s.
 * <p>
 * A repository that also supports update and delete operations on the content
 * will also implement <code>{@link WritableContentRepository}</code>.
 */
public interface ContentRepository {

  /**
   * Sets the repository uri.
   * 
   * @param repositoryURI
   *          the repository URI
   */
  void setURI(String repositoryURI);

  /**
   * Returns the repository uri.
   * 
   * @return the uri
   */
  String getURI();

  /**
   * Opens the repository. Depending on the type of the repository
   * implementation, this might involve mounting network volumes, opening
   * database connections etc.
   * <p>
   * The <code>site</code> argument identifies the site that this content
   * repository is serving while the connection properties are expected to
   * contain everything that is needed to open a connection to the content
   * repository.
   * 
   * @param properties
   *          the connection properties
   * @throws ContentRepositoryException
   *           if connecting to the repository fails
   */
  void connect(Dictionary<?, ?> properties) throws ContentRepositoryException;

  /**
   * Disconnects from the content repository.
   * 
   * @throws ContentRepositoryException
   *           if disconnecting from the repository fails
   */
  void disconnect() throws ContentRepositoryException;

  /**
   * Starts the repository. This is the perfect place to create or load indices,
   * pre-populate caches etc.
   * 
   * @throws ContentRepositoryException
   *           if starting the repository fails
   */
  void start() throws ContentRepositoryException;

  /**
   * Stops the repository. This method will usually be called prior to a call to
   * {@#disconnect()}.
   * 
   * @throws ContentRepositoryException
   *           if starting the repository fails
   */
  void stop() throws ContentRepositoryException;

  /**
   * Returns the resource identified by <code>uri</code> or <code>null</code> if
   * no resource was found at the specified location or revision.
   * 
   * @param uri
   *          the resource uri
   * @throws ContentRepositoryException
   *           if reading the resource from the repository fails
   * @return the resource
   */
  Resource get(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns the requested resource or <code>null</code> if the resource is not
   * available.
   * 
   * @param uri
   *          the resource uri
   * @param user
   *          the requesting user
   * @param p
   *          the requested permission
   * 
   * @return the resource or <code>null</code>
   * @throws ContentRepositoryException
   *           if reading the resource from the repository fails
   * @throws SecurityException
   *           if access is denied for the given user and permission
   */
  Resource get(ResourceURI uri, User user, Permission p)
      throws ContentRepositoryException, SecurityException;

  /**
   * Returns the resource content identified by <code>uri</code> and
   * <code>language</code> or <code>null</code> if no content was found.
   * 
   * @param uri
   *          the resource uri
   * @param language
   *          the language
   * @return the resource
   * @throws ContentRepositoryException
   *           if reading the content from the repository fails
   * @throws IOException
   *           if reading the resource contents fails
   */
  InputStream getContent(ResourceURI uri, Language language)
      throws ContentRepositoryException, IOException;

  /**
   * Returns the requested resource or <code>null</code> if the resource is not
   * available.
   * 
   * @param uri
   *          the resource uri
   * @param language
   *          the language
   * @param user
   *          the requesting user
   * @param p
   *          the requested permission
   * 
   * @return the resource or <code>null</code>
   * @throws ContentRepositoryException
   *           if reading the resource from the repository fails
   * @throws SecurityException
   *           if access is denied for the given user and permission
   * @throws IOException
   *           if reading the resource contents fails
   */
  InputStream getContent(ResourceURI uri, Language language, User user,
      Permission p) throws ContentRepositoryException, SecurityException,
      IOException;

  /**
   * Returns <code>true</code> if the requested resource exists.
   * 
   * @param uri
   *          the resource uri
   * @return <code>true</code> if the resource exists
   * @throws ContentRepositoryException
   *           if looking up the resource from the repository fails
   */
  boolean exists(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns <code>true</code> if the requested resource exists for the given
   * user and is accessible with respect to permissions and version.
   * 
   * @param uri
   *          the resource uri
   * @param user
   *          the requesting user
   * @param p
   *          the requested permission
   * @return <code>true</code> if the resource exists and is accessible for the
   *         given user using permission <code>p</code>.
   * @throws ContentRepositoryException
   *           if looking up the resource from the repository fails
   * @throws SecurityException
   *           if access is denied for the given user and permission
   */
  boolean exists(ResourceURI uri, User user, Permission p)
      throws ContentRepositoryException, SecurityException;

  /**
   * Returns a resource uri for every available revision of the resource.
   * 
   * @param uri
   *          the resource uri
   * @return the revisions
   * @throws ContentRepositoryException
   *           if looking up the resource versions from the repository fails
   */
  ResourceURI[] getVersions(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns an iteration of all resources with their uri containing
   * <code>uri</code> as a prefix.
   * 
   * @param uri
   *          the root uri
   * @return the resource uris
   * @throws ContentRepositoryException
   *           if listing the repository fails
   */
  Iterator<ResourceURI> list(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns an iteration of all resources with version <code>version</code> and
   * their uri containing <code>uri</code> as a prefix.
   * <p>
   * Live versions of resources are returned using {@link Resource#LIVE}, while
   * work resources are specified using {@link Resource#WORK}.
   * 
   * @param uri
   *          the root uri
   * @param version
   *          the resource version to list
   * @return the resource uris
   * @throws ContentRepositoryException
   *           if listing the repository fails
   */
  Iterator<ResourceURI> list(ResourceURI uri, long version)
      throws ContentRepositoryException;

  /**
   * Returns an iteration of all resources with their uri containing
   * <code>uri</code> as a prefix.
   * <p>
   * Only those resources will be returned that are nested <code>level</code>
   * levels deep under <code>uri</code>. Therefore, specifying
   * <code>level</code> as <code>0</code> will return the same result as calling
   * <code>getVersions(uri)</code>, while specifying <code>level</code> as
   * <code>1</code> will return resources located at <code>uri</code> as well as
   * those that are one level below it.
   * 
   * @return the resource uris
   * @throws ContentRepositoryException
   *           if listing the repository fails
   */
  Iterator<ResourceURI> list(ResourceURI uri, int level)
      throws ContentRepositoryException;

  /**
   * Returns an iteration of all resources with version <code>version</code> and
   * their uri containing <code>uri</code> as a prefix.
   * <p>
   * Only those resources will be returned that are nested <code>level</code>
   * levels deep under <code>uri</code>. Therefore, specifying
   * <code>level</code> as <code>0</code> will return the same result as calling
   * <code>getVersions(uri)</code>, while specifying <code>level</code> as
   * <code>1</code> will return resources located at <code>uri</code> as well as
   * those that are one level below it.
   * <p>
   * Live versions of resources are returned using {@link Resource#LIVE}, while
   * work resources are specified using {@link Resource#WORK}.
   * 
   * @return the resource uris
   * @param versions
   *          the resource version to list
   * @throws ContentRepositoryException
   *           if listing the repository fails
   */
  Iterator<ResourceURI> list(ResourceURI uri, int level, long versions)
      throws ContentRepositoryException;

  /**
   * Returns the search results for <code>query</code>.
   * 
   * @param query
   *          the query
   * @return the search result
   * @throws ContentRepositoryException
   *           if performing the search query fails
   */
  SearchResult find(SearchQuery query) throws ContentRepositoryException;

  /**
   * Returns the number of resources in this index.
   * 
   * @return the number of resources
   */
  long getResourceCount();

  /**
   * Returns the number of versions in this index.
   * 
   * @return the number of versions
   */
  long getVersionCount();

}
