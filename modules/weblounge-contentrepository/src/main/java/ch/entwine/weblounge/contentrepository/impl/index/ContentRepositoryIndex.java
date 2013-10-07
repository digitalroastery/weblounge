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

package ch.entwine.weblounge.contentrepository.impl.index;

import static ch.entwine.weblounge.search.impl.IndexSchema.PATH;
import static ch.entwine.weblounge.search.impl.IndexSchema.RESOURCE_ID;
import static ch.entwine.weblounge.search.impl.IndexSchema.TYPE;
import static ch.entwine.weblounge.search.impl.IndexSchema.VERSION;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.ResourceSerializerService;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.search.impl.SearchIndexImpl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * This index into the repository is used to map resource and resource urls into
 * repository indices and vice versa. In addition, it will facilitate listing
 * url hierarchies.
 */
public class ContentRepositoryIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ContentRepositoryIndex.class);

  /** The search index */
  protected SearchIndexImpl searchIdx = null;

  /** The site */
  protected Site site = null;

  /**
   * Creates a new index that is located in the indicated folder.
   * 
   * @param site
   *          the site
   * @param rootDir
   *          the root directory
   * @param readOnly
   *          <code>true</code> if the index should be read only
   * @param serializer
   *          the resource serializer
   * @throws IOException
   *           if creating the indices fails
   */
  public ContentRepositoryIndex(Site site,
      ResourceSerializerService serializer, boolean readOnly)
          throws IOException {
    this.site = site;
    this.searchIdx = new SearchIndexImpl(site, serializer, readOnly);
  }

  /**
   * Returns the index version or <code>-1</code> if the versions differ.
   * 
   * @return the index version
   */
  public int getIndexVersion() {
    int version = searchIdx.getIndexVersion();
    return version;
  }

  /**
   * Sets the search index.
   * 
   * @param searchIndex
   *          the search index
   */
  protected void setSearchIndex(SearchIndexImpl searchIndex) {
    this.searchIdx = searchIndex;
  }

  /**
   * Closes the index files. No more read and write operations are allowed.
   * 
   * @throws IOException
   *           if closing the index fails
   */
  public void close() throws IOException {
    if (searchIdx != null)
      searchIdx.close();
  }

  /**
   * Returns the number of resources in this index.
   * 
   * @return the number of resources
   * @throws ContentRepositoryException
   *           if querying for the resource count fails
   */
  public long getResourceCount() throws ContentRepositoryException {
    SearchQuery q = new SearchQueryImpl(site).withPreferredVersion(Resource.LIVE).withField(RESOURCE_ID);
    return searchIdx.getByQuery(q).getHitCount();
  }

  /**
   * Returns the number of revisions in this index.
   * 
   * @return the number of revisions
   * @throws ContentRepositoryException
   *           if querying for the resource count fails
   */
  public long getRevisionCount() throws ContentRepositoryException {
    SearchQuery q = new SearchQueryImpl(site).withField(RESOURCE_ID);
    return searchIdx.getByQuery(q).getHitCount();
  }

  /**
   * Adds all relevant entries for the given resource uri to the index and
   * returns it, probably providing a newly created uri identifier.
   * 
   * @param url
   *          the resource uri
   * @return the uri
   * @throws IOException
   *           if writing to the index fails
   * @throws ContentRepositoryException
   *           if adding to the index fails
   */
  public synchronized ResourceURI add(Resource<?> resource) throws IOException,
  ContentRepositoryException {

    ResourceURI uri = resource.getURI();
    String id = uri.getIdentifier();
    String path = StringUtils.trimToNull(uri.getPath());
    long version = uri.getVersion();

    // Make sure we are not asked to add a resource to the index that has the
    // same id as an existing one
    if (id != null) {
      SearchQuery q = new SearchQueryImpl(site).withIdentifier(id).withPreferredVersion(version).withLimit(1).withField(PATH);
      SearchResultItem[] items = searchIdx.getByQuery(q).getItems();
      if (items.length > 0) {
        long versionInIndex = (Long) ((ResourceSearchResultItem) items[0]).getMetadataByKey(VERSION).getValue();
        if (items.length == 1 && versionInIndex == version)
          throw new ContentRepositoryException("Resource '" + id + "' already exists in version " + version);
        if (path == null) {
          path = (String) ((ResourceSearchResultItem) items[0]).getMetadataByKey(PATH).getValue();
          resource.getURI().setPath(path);
        }
      }
    }

    // Make sure we are not asked to add a resource to the index that has the
    // same path as an existing one
    if (path != null) {
      SearchQuery q = new SearchQueryImpl(site).withPath(path).withPreferredVersion(version).withLimit(1).withField(RESOURCE_ID);
      SearchResultItem[] items = searchIdx.getByQuery(q).getItems();
      if (items.length > 0) {
        long versionInIndex = (Long) ((ResourceSearchResultItem) items[0]).getMetadataByKey(VERSION).getValue();
        if (items.length == 1 && versionInIndex == version)
          throw new ContentRepositoryException("Resource '" + id + "' already exists in version " + version);
        if (id == null) {
          id = (String) ((ResourceSearchResultItem) items[0]).getMetadataByKey(RESOURCE_ID).getValue();
          resource.getURI().setIdentifier(id);
        }
      }
    }

    // Create an id if necessary. A missing id indicates that the resource
    // has never been added to the index before
    if (id == null) {
      id = UUID.randomUUID().toString();
      resource.setIdentifier(id);
      uri.setIdentifier(id);
    }

    try {
      searchIdx.add(resource);
    } catch (ContentRepositoryException e) {
      throw e;
    } catch (Throwable t) {
      throw new ContentRepositoryException("Error adding " + resource + " to index", t);
    }

    return uri;
  }

  /**
   * Removes all entries for the given resource uri from the index and returns
   * <code>true</code>. If the resource is not part of the index,
   * <code>false</code> is returned.
   * 
   * @param uri
   *          the resource uri
   * @return <code>true</code> if the resource was deleted
   * @throws IOException
   *           if updating the index fails
   * @throws ContentRepositoryException
   *           if deleting the resource fails
   */
  public synchronized boolean delete(ResourceURI uri) throws IOException,
  ContentRepositoryException, IllegalArgumentException {
    getIdentifier(uri);
    StringUtils.trimToNull(uri.getPath());
    uri.getVersion();

    // Finally, delete the entry
    return searchIdx.delete(uri);
  }

  /**
   * Returns all revisions for the specified resource or an empty array if the
   * resource doesn't exist.
   * 
   * @param uri
   *          the resource uri
   * @return the revisions
   */
  public long[] getRevisions(ResourceURI uri) throws ContentRepositoryException {
    String id = getIdentifier(uri);
    if (id == null)
      return new long[] {};
    SearchQuery q = new SearchQueryImpl(site).withIdentifier(id).withField(VERSION);
    SearchResultItem[] items = searchIdx.getByQuery(q).getItems();
    long[] versions = new long[items.length];
    for (int i = 0; i < items.length; i++) {
      versions[i] = (Long) ((ResourceSearchResultItem) items[i]).getMetadataByKey(VERSION).getValue();
    }
    return versions;
  }

  /**
   * Returns the identifier of the resource with uri <code>uri</code> or
   * <code>null</code> if the uri is not part of the index.
   * 
   * @param uri
   *          the uri
   * @return the id
   * @throws IllegalArgumentException
   *           if the uri does not contain a path
   * @throws ContentRepositoryException
   *           if accessing the index fails
   */
  public String getIdentifier(ResourceURI uri)
      throws ContentRepositoryException, IllegalArgumentException {

    if (uri.getIdentifier() != null)
      return uri.getIdentifier();

    String path = StringUtils.trimToNull(uri.getPath());
    if (path == null)
      throw new IllegalArgumentException("ResourceURI must contain a path");

    // Load the identifier from the index
    SearchQuery q = new SearchQueryImpl(site).withPath(path);
    if (uri.getType() != null)
      q.withTypes(uri.getType());
    SearchResultItem[] items = searchIdx.getByQuery(q).getItems();
    if (items.length == 0) {
      logger.debug("Attempt to locate id for non-existing path {}", path);
      return null;
    }

    String id = (String) ((ResourceSearchResultItem) items[0]).getMetadataByKey(RESOURCE_ID).getValue();
    uri.setIdentifier(id);
    return id;
  }

  /**
   * Returns the path of the resource with uri <code>uri</code> by looking it up
   * using the uri's identifier or <code>null</code> if the uri is not part of
   * the index.
   * 
   * @param uri
   *          the uri
   * @return the path
   * @throws IllegalArgumentException
   *           if the uri does not contain an identifier
   * @throws ContentRepositoryException
   *           if accessing the index fails
   */
  public String getPath(ResourceURI uri) throws ContentRepositoryException,
  IllegalArgumentException {
    if (uri.getPath() != null)
      return uri.getPath();

    String id = uri.getIdentifier();
    if (id == null)
      throw new IllegalArgumentException("ResourceURI must contain an identifier");

    // Load the path from the index
    SearchQuery q = new SearchQueryImpl(site).withIdentifier(id);
    if (uri.getType() != null)
      q.withTypes(uri.getType());
    SearchResultItem[] items = searchIdx.getByQuery(q).getItems();
    if (items.length == 0) {
      logger.debug("Attempt to locate path for non existing resource '{}'", id);
      return null;
    }

    String path = (String) ((ResourceSearchResultItem) items[0]).getMetadataByKey(RESOURCE_ID).getValue();
    uri.setPath(path);
    return path;
  }

  /**
   * Returns the resource type or <code>null</code> the resource could not be
   * found.
   * 
   * @param uri
   *          the resource uri
   * @return the resource type
   * @throws ContentRepositoryException
   *           if loading the type from the index fails
   */
  public String getType(ResourceURI uri) throws ContentRepositoryException {
    if (uri.getType() != null)
      return uri.getType();

    String id = uri.getIdentifier();
    String path = uri.getPath();
    SearchQuery q = null;

    if (id != null)
      q = new SearchQueryImpl(site).withIdentifier(id).withLimit(1);
    else if (path != null)
      q = new SearchQueryImpl(site).withPath(path).withLimit(1);
    else
      throw new IllegalArgumentException("URI must have either id or path");

    // Load the path from the index
    SearchResultItem[] items = searchIdx.getByQuery(q).getItems();
    if (items.length == 0) {
      logger.debug("Attempt to locate path for non existing resource '{}'", id);
      return null;
    }

    String type = (String) ((ResourceSearchResultItem) items[0]).getMetadataByKey(TYPE).getValue();
    uri.setType(type);
    return type;
  }

  /**
   * Updates the resource in the search index.
   * 
   * @param resource
   *          the resource to update
   * @throws IOException
   *           if writing to the index fails
   * @throws ContentRepositoryException
   *           if updating the index fails
   */
  public synchronized void update(Resource<?> resource) throws IOException,
  ContentRepositoryException {
    ResourceURI uri = resource.getURI();

    // Make sure the uri has an identifier
    if (uri.getIdentifier() == null) {
      uri.setIdentifier(getIdentifier(uri));
    }

    searchIdx.update(resource);
  }

  /**
   * Updates the path of the given resource.
   * 
   * @param uir
   *          the resource uri
   * @param path
   *          the new path
   * @throws IOException
   *           if writing to the index fails
   * @throws ContentRepositoryException
   *           if moving the resource fails
   * @throws IllegalStateException
   *           if the resource to be moved could not be found in the index
   */
  public synchronized void move(ResourceURI uri, String path)
      throws IOException, ContentRepositoryException, IllegalStateException {

    // Do it this way to make sure we have identical path trimming
    ResourceURI newURI = new ResourceURIImpl(uri.getType(), uri.getSite(), StringUtils.trimToNull(path), uri.getIdentifier(), uri.getVersion());
    path = newURI.getPath();

    searchIdx.move(uri, path);
  }

  /**
   * Removes all entries from the index.
   * 
   * @throws IOException
   *           if clearing the index fails
   */
  public synchronized void clear() throws IOException {
    searchIdx.clear();
  }

  /**
   * Returns <code>true</code> if the given uri exists in the given version.
   * 
   * @param uri
   *          the uri
   * @return <code>true</code> if the uri exists
   * @throws ContentRepositoryException
   *           if looking up the uri fails
   */
  public boolean exists(ResourceURI uri) throws ContentRepositoryException {
    String id = getIdentifier(uri);
    if (id == null)
      return false;
    SearchQuery q = new SearchQueryImpl(site).withIdentifier(id).withVersion(uri.getVersion()).withField(RESOURCE_ID);
    if (uri.getType() != null)
      q.withTypes(uri.getType());
    return searchIdx.getByQuery(q).getDocumentCount() > 0;
  }

  /**
   * Returns <code>true</code> if the given uri exists in any version.
   * 
   * @param uri
   *          the uri
   * @return <code>true</code> if the uri exists in any version
   * @throws ContentRepositoryException
   *           if looking up the uri fails
   */
  public boolean existsInAnyVersion(ResourceURI uri)
      throws ContentRepositoryException {
    String id = getIdentifier(uri);
    if (id == null)
      return false;
    SearchQuery q = new SearchQueryImpl(site).withIdentifier(id).withLimit(1).withField(RESOURCE_ID);
    if (uri.getType() != null)
      q.withTypes(uri.getType());
    return searchIdx.getByQuery(q).getDocumentCount() > 0;
  }

  /**
   * Returns all URIs that share the common root <code>uri</code>, are no more
   * than <code>level</code> levels deep nested and feature the indicated
   * version.
   * <p>
   * 
   * @param uri
   *          the root uri
   * @param level
   *          the maximum nesting, <code>0</code> to return direct children only
   * @return an iteration of the resulting uris
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level) {
    return list(uri, level, -1);
  }

  /**
   * Returns all uris that share the common root <code>uri</code>, are no more
   * than <code>level</code> levels deep nested and feature the indicated
   * version.
   * <p>
   * 
   * @param uri
   *          the root uri
   * @param level
   *          the maximum nesting, <code>0</code> to return direct children only
   * @param version
   *          the requested version, <code>-1</code> for any version
   * @return an iteration of the resulting uris
   */
  public Iterator<ResourceURI> list(ResourceURI uri, int level, long version) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  /**
   * Issues a search query and returns the matches in the result set.
   * 
   * @param query
   *          the search query
   * @return the search result
   * @throws IllegalArgumentException
   *           if the query is <code>null</code>
   * @throws ContentRepositoryException
   *           if processing the query fails
   */
  public SearchResult find(SearchQuery query)
      throws ContentRepositoryException, IllegalArgumentException {
    if (query == null)
      throw new IllegalArgumentException("Query cannot be null");
    return searchIdx.getByQuery(query);
  }

  /**
   * Returns the suggestions as returned from the selected dictionary based on
   * <code>seed</code>.
   * 
   * @param dictionary
   *          the dictionary
   * @param seed
   *          the seed used for suggestions
   * @param onlyMorePopular
   *          whether to return only more popular results
   * @param count
   *          the maximum number of suggestions
   * @param collate
   *          whether to provide a query collated with the first matching
   *          suggestion
   * @throws ContentRepositoryException
   *           if suggesting fails
   */
  public List<String> suggest(String dictionary, String seed,
      boolean onlyMorePopular, int count, boolean collate)
          throws ContentRepositoryException {
    if (StringUtils.isBlank(dictionary))
      throw new IllegalArgumentException("Dictionary cannot be null");
    if (StringUtils.isBlank(seed))
      throw new IllegalArgumentException("Seed cannot be null");
    return searchIdx.suggest(dictionary, seed, onlyMorePopular, count, collate);
  }

}
