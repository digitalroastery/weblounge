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

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.user.User;

import java.util.Dictionary;
import java.util.Iterator;

/**
 * A content repository stores pages and resources that represent the content of
 * <code>Site</code>s.
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
   * Stops the repository. This method will usually be called prior to a call
   * to {@#disconnect()}.
   * 
   * @throws ContentRepositoryException
   *           if starting the repository fails
   */
  void stop() throws ContentRepositoryException;

  /**
   * Returns the page identified by <code>uri</code> or <code>null</code> if no
   * page was found at the specified location or revision.
   * 
   * @param uri
   *          the page uri
   * @throws ContentRepositoryException
   *           if reading the page from the repository fails
   * @return the page
   */
  Page getPage(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns the requested page or <code>null</code> if the page is not
   * available.
   * 
   * @param uri
   *          the page uri
   * @param user
   *          the requesting user
   * @param p
   *          the requested permission
   * 
   * @return the page or <code>null</code>
   * @throws ContentRepositoryException
   *           if reading the page from the repository fails
   * @throws SecurityException
   *           if access is denied for the given user and permission
   */
  Page getPage(ResourceURI uri, User user, Permission p)
      throws ContentRepositoryException, SecurityException;

  /**
   * Returns <code>true</code> if the requested page exists.
   * 
   * @param uri
   *          the page uri
   * @return <code>true</code> if the page exists
   * @throws ContentRepositoryException
   *           if looking up the page from the repository fails
   */
  boolean exists(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns <code>true</code> if the requested page exists for the given user
   * and is accessible with respect to permissions and version.
   * 
   * @param uri
   *          the page uri
   * @param user
   *          the requesting user
   * @param p
   *          the requested permission
   * @return <code>true</code> if the page exists and is accessible for the
   *         given user using permission <code>p</code>.
   * @throws ContentRepositoryException
   *           if looking up the page from the repository fails
   * @throws SecurityException
   *           if access is denied for the given user and permission
   */
  boolean exists(ResourceURI uri, User user, Permission p)
      throws ContentRepositoryException, SecurityException;

  /**
   * Returns a page uri for every available revision of the page.
   * 
   * @param uri
   *          the page uri
   * @return the revisions
   * @throws ContentRepositoryException
   *           if looking up the page versions from the repository fails
   */
  ResourceURI[] getVersions(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns an iteration of all pages with their uri containing
   * <code>uri</code> as a prefix.
   * 
   * @param uri
   *          the root uri
   * @return the page uris
   * @throws ContentRepositoryException
   *           if listing the repository fails
   */
  Iterator<ResourceURI> listPages(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns an iteration of all pages with version <code>version</code> and
   * their uri containing <code>uri</code> as a prefix.
   * <p>
   * Live versions of pages are returned using {@link Page#LIVE}, while work
   * pages are specified using {@link Page#WORK}.
   * 
   * @param uri
   *          the root uri
   * @param version
   *          the page version to list
   * @return the page uris
   * @throws ContentRepositoryException
   *           if listing the repository fails
   */
  Iterator<ResourceURI> listPages(ResourceURI uri, long version)
      throws ContentRepositoryException;

  /**
   * Returns an iteration of all pages with their uri containing
   * <code>uri</code> as a prefix.
   * <p>
   * Only those pages will be returned that are nested <code>level</code> levels
   * deep under <code>uri</code>. Therefore, specifying <code>level</code> as
   * <code>0</code> will return the same result as calling
   * <code>getVersions(uri)</code>, while specifying <code>level</code> as
   * <code>1</code> will return pages located at <code>uri</code> as well as
   * those that are one level below it.
   * 
   * @return the page uris
   * @throws ContentRepositoryException
   *           if listing the repository fails
   */
  Iterator<ResourceURI> listPages(ResourceURI uri, int level)
      throws ContentRepositoryException;

  /**
   * Returns an iteration of all pages with version <code>version</code> and
   * their uri containing <code>uri</code> as a prefix.
   * <p>
   * Only those pages will be returned that are nested <code>level</code> levels
   * deep under <code>uri</code>. Therefore, specifying <code>level</code> as
   * <code>0</code> will return the same result as calling
   * <code>getVersions(uri)</code>, while specifying <code>level</code> as
   * <code>1</code> will return pages located at <code>uri</code> as well as
   * those that are one level below it.
   * <p>
   * Live versions of pages are returned using {@link Page#LIVE}, while work
   * pages are specified using {@link Page#WORK}.
   * 
   * @return the page uris
   * @param versions
   *          the page version to list
   * @throws ContentRepositoryException
   *           if listing the repository fails
   */
  Iterator<ResourceURI> listPages(ResourceURI uri, int level, long versions)
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
  SearchResult findPages(SearchQuery query) throws ContentRepositoryException;

  /**
   * Returns the number of pages in this index.
   * 
   * @return the number of pages
   */
  long getPages();

  /**
   * Returns the number of versions in this index.
   * 
   * @return the number of versions
   */
  long getVersions();

}
