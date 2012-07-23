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

package ch.entwine.weblounge.common.content.repository;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * A content repository stores resources and resources that represent the
 * content of <code>Site</code>s.
 * <p>
 * A repository that also supports update and delete operations on the content
 * will also implement <code>{@link WritableContentRepository}</code>.
 */
public interface ContentRepository {

  /**
   * Returns the repository type, which is used to associate a repository
   * instance with a configuration using the OSGi configuration admin service.
   * <p>
   * An example of such a type declaration would be
   * <code>ch.entwine.weblounge.contentrepository.filesystem</code>.
   * 
   * @return the repository type
   */
  String getType();

  /**
   * Returns <code>true</code> if this repository is read only. If
   * <code>false</code> is returned, the repository can safely be casted to a
   * {@link WritableContentRepository}.
   * 
   * @return <code>true</code> if the repository is read only
   */
  boolean isReadOnly();

  /**
   * Returns <code>true</code> if the content repository is currently indexing
   * the contents.
   * 
   * @return <code>true</code> if the repository content is being index
   */
  boolean isIndexing();

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
   * @param site
   *          the associated site
   * @throws ContentRepositoryException
   *           if connecting to the repository fails
   * @throws IllegalStateException
   *           if the content repository is already connected
   */
  void connect(Site site) throws ContentRepositoryException,
  IllegalStateException;

  /**
   * Disconnects from the content repository.
   * 
   * @throws ContentRepositoryException
   *           if disconnecting from the repository fails
   * @throws IllegalStateException
   *           if the content repository has never been connected
   */
  void disconnect() throws ContentRepositoryException, IllegalStateException;

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
  <R extends Resource<?>> R get(
      ResourceURI uri)
          throws ContentRepositoryException;

  /**
   * Returns the resource content identified by <code>uri</code> and
   * <code>language</code> or <code>null</code> if no content was found.
   * 
   * @param uri
   *          the resource uri
   * @param language
   *          the content language
   * @return the resource
   * @throws ContentRepositoryException
   *           if reading the content from the repository fails
   * @throws IOException
   *           if reading the resource contents fails
   */
  InputStream getContent(ResourceURI uri, Language language)
      throws ContentRepositoryException, IOException;

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
   * Returns <code>true</code> if the requested resource exists in any version.
   * 
   * @param uri
   *          the resource uri
   * @return <code>true</code> if the resource exists
   * @throws ContentRepositoryException
   *           if looking up the resource from the repository fails
   */
  boolean existsInAnyVersion(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Returns the resource uri for the resource specified by
   * <code>resourceId</code> or <code>null</code> if the resource does not
   * exist.
   * 
   * @param resourceId
   *          the resource identifier
   * @return the resource uri or <code>null</code> if the resource doesn't exist
   * @throws ContentRepositoryException
   *           if looking up the resource from the repository fails
   */
  ResourceURI getResourceURI(String resourceId)
      throws ContentRepositoryException;

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
   * Suggests a maximum of <code>count</code> entries using <code>seed</code>
   * from the specified dictionary.
   * 
   * @param dictionary
   *          the dictionary
   * @param seed
   *          the seed
   * @param count
   *          the maximum number of suggestions
   * @return the suggestions
   * @throws ContentRepositoryException
   *           if suggesting fails
   */
  List<String> suggest(String dictionary, String seed, int count)
      throws ContentRepositoryException;

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
