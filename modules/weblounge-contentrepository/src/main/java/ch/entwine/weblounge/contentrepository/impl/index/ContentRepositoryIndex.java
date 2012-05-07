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

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

  /** The uri index */
  protected URIIndex uriIdx = null;

  /** The id index */
  protected IdIndex idIdx = null;

  /** The path index */
  protected PathIndex pathIdx = null;

  /** The version index */
  protected VersionIndex versionIdx = null;

  /** The search index */
  protected SearchIndex searchIdx = null;

  /** The index root directory */
  protected File idxRootDir = null;

  /**
   * Creates a new index that is located in the indicated folder.
   * 
   * @param rootDir
   *          the root directory
   * @param readOnly
   *          <code>true</code> if the index should be read only
   * @throws IOException
   *           if creating the indices fails
   */
  public ContentRepositoryIndex(File rootDir, boolean readOnly)
      throws IOException {
    this.idxRootDir = rootDir;
    this.uriIdx = new URIIndex(new File(rootDir, "structure"), readOnly);
    this.idIdx = new IdIndex(new File(rootDir, "structure"), readOnly);
    this.pathIdx = new PathIndex(new File(rootDir, "structure"), readOnly);
    this.versionIdx = new VersionIndex(new File(rootDir, "structure"), readOnly);
    this.searchIdx = new SearchIndex(new File(rootDir, "fulltext"), readOnly);
  }

  /**
   * Creates a new content repository index using the provided sub indices.
   * 
   * @param uriIndex
   *          the uri index
   * @param idIndex
   *          the id index
   * @param pathIndex
   *          the path index
   * @param versionIndex
   *          the version index
   * @param languageIndex
   *          the language index
   */
  public ContentRepositoryIndex(URIIndex uriIndex, IdIndex idIndex,
      PathIndex pathIndex, VersionIndex versionIndex) {
    this.uriIdx = uriIndex;
    this.idIdx = idIndex;
    this.pathIdx = pathIndex;
    this.versionIdx = versionIndex;
  }

  /**
   * Returns the index version or <code>-1</code> if the versions differ.
   * 
   * @return the index version
   */
  public int getIndexVersion() {
    int version = uriIdx.getIndexVersion();
    int idIdxVersion = idIdx.getIndexVersion();
    int pathIdxVersion = pathIdx.getIndexVersion();
    int versionIdxVersion = versionIdx.getIndexVersion();
    int searchIdxVersion = searchIdx.getIndexVersion();
    if (idIdxVersion != version || pathIdxVersion != version || versionIdxVersion != version || searchIdxVersion != version) {
      logger.info("Version mismatch detected in structural index");
      return -1;
    }
    return version;
  }

  /**
   * Sets the uri index.
   * 
   * @param uriIndex
   *          the uri index
   */
  protected void setURIIndex(URIIndex uriIndex) {
    this.uriIdx = uriIndex;
  }

  /**
   * Sets the id index.
   * 
   * @param idIndex
   *          the id index
   */
  protected void setIdIndex(IdIndex idIndex) {
    this.idIdx = idIndex;
  }

  /**
   * Sets the path index.
   * 
   * @param pathIndex
   *          the path index
   */
  protected void setPathIndex(PathIndex pathIndex) {
    this.pathIdx = pathIndex;
  }

  /**
   * Sets the version index.
   * 
   * @param versionIndex
   *          the version index
   */
  protected void setVersionIndex(VersionIndex versionIndex) {
    this.versionIdx = versionIndex;
  }

  /**
   * Sets the search index.
   * 
   * @param searchIndex
   *          the search index
   */
  protected void setSearchIndex(SearchIndex searchIndex) {
    this.searchIdx = searchIndex;
  }

  /**
   * Closes the index files. No more read and write operations are allowed.
   * 
   * @throws IOException
   *           if closing the index fails
   */
  public void close() throws IOException {
    if (uriIdx != null)
      uriIdx.close();
    if (idIdx != null)
      idIdx.close();
    if (pathIdx != null)
      pathIdx.close();
    if (versionIdx != null)
      versionIdx.close();
    if (searchIdx != null)
      searchIdx.close();
  }

  /**
   * Returns the number of resources in this index.
   * 
   * @return the number of resources
   */
  public long getResourceCount() {
    return uriIdx.getEntries();
  }

  /**
   * Returns the number of revisions in this index.
   * 
   * @return the number of revisions
   */
  public long getRevisionCount() {
    return versionIdx.getEntries();
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
    String type = uri.getType();
    long version = uri.getVersion();
    long address = -1;

    // Make sure we are not asked to add a resource to the index that has the
    // same id as an existing one
    if (id != null) {
      for (long a : idIdx.locate(id)) {
        String idxId = uriIdx.getId(a);
        if (id.equals(idxId)) {
          if (versionIdx.hasVersion(a, version))
            throw new ContentRepositoryException("Resource id '" + id + "' already exists");
          if (path == null) {
            path = uriIdx.getPath(a);
            resource.getURI().setPath(path);
          }
          address = a;
        }
      }
    }

    // Make sure we are not asked to add a resource to the index that has the
    // same path as an existing one
    if (path != null) {
      for (long a : pathIdx.locate(path)) {
        String idxPath = uriIdx.getPath(a);
        if (path.equals(idxPath)) {
          if (versionIdx.hasVersion(a, version))
            throw new ContentRepositoryException("Resource path '" + path + "' already exists");
          if (id == null) {
            id = uriIdx.getId(a);
            resource.getURI().setIdentifier(id);
          }
          address = a;
        }
      }
    }

    // If there is no address, we are about to add a new resource
    if (address < 0) {

      // Create an id if necessary. A missing id indicates that the resource
      // has never been added to the index before
      if (id == null) {
        id = UUID.randomUUID().toString();
        resource.setIdentifier(id);
        uri.setIdentifier(id);
      }

      try {
        address = uriIdx.add(id, type, path);
        idIdx.set(address, id);
        versionIdx.add(id, version);
        if (path != null)
          pathIdx.set(address, path);
        if (resource.isIndexed())
          searchIdx.add(resource);
        else
          searchIdx.delete(uri);
      } catch (ContentRepositoryException e) {
        throw e;
      } catch (Throwable t) {
        throw new ContentRepositoryException("Error adding " + resource + " to index", t);
      }
    }

    // Otherwise, it's just a new version
    else if (!versionIdx.hasVersion(address, version)) {
      versionIdx.addVersion(address, version);

      // Update the path if we are updating the live version
      if (version == Resource.LIVE) {
        String oldPath = uri.getPath();
        uriIdx.update(address, type, path);
        if (oldPath != null)
          pathIdx.delete(oldPath, address);
        if (path != null)
          pathIdx.set(address, path);
      }

      if (resource.isIndexed())
        searchIdx.add(resource);
      else
        searchIdx.delete(uri);
    }

    // Seems to be an existing resource, so it's an update rather than an
    // addition
    else {
      logger.warn("Existing resource '{}' was passed to add(), redirecting to update()", uri.getIdentifier());
      update(resource);

      // TODO: Why is that needed?
      if (resource.getIdentifier() == null)
        resource.setIdentifier(uriIdx.getId(address));
      if (resource.getPath() == null)
        resource.setPath(uriIdx.getPath(address));
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
    String id = uri.getIdentifier();
    String path = StringUtils.trimToNull(uri.getPath());
    long version = uri.getVersion();

    // Locate the entry in question
    long address = toURIEntry(uri);

    // Does the resource exist?
    if (address == -1) {
      logger.warn("Tried to delete non-existing resource {} from index", uri);
      return false;
    }

    // Load the missing data
    if (id == null)
      id = uriIdx.getId(address);
    path = uriIdx.getPath(address);

    // Are we deleting the one and only version?
    long[] existingVersions = versionIdx.getVersions(address);

    // Adjust/delete the entry. If this is the last version of the file, make
    // sure we remove every evidence
    if (existingVersions.length == 1) {
      searchIdx.delete(uri);
      uriIdx.delete(address);
      idIdx.delete(address, id);
      if (path != null)
        pathIdx.delete(path, address);
      versionIdx.delete(address);
    } else {
      searchIdx.delete(uri);
      versionIdx.delete(address, version);
    }

    return true;
  }

  /**
   * Returns all revisions for the specified resource or <code>null</code> if
   * the resource doesn't exist.
   * 
   * @param uri
   *          the resource uri
   * @return the revisions
   */
  public long[] getRevisions(ResourceURI uri) throws IOException {
    // Locate the entry in question
    long address = toURIEntry(uri);

    // Everything ok?
    if (address == -1)
      throw new IllegalArgumentException("Uri " + uri + " was not found");

    return versionIdx.getVersions(address);
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
   * @throws IllegalArgumentException
   *           if the uri does not refer to the live version of the resource
   * @throws IOException
   *           if accessing the index fails
   */
  public String getIdentifier(ResourceURI uri) throws IOException,
      IllegalArgumentException {

    String path = StringUtils.trimToNull(uri.getPath());
    if (path == null)
      throw new IllegalArgumentException("ResourceURI must contain a path");

    long[] addresses = pathIdx.locate(path);

    // Is the uri part of the index?
    if (addresses.length == 0) {
      logger.debug("Attempt to locate non-existing path {}", path);
      return null;
    }

    // Locate the entry in question
    for (long a : addresses) {
      String idxPath = uriIdx.getPath(a);
      if (idxPath.equals(path)) {
        return uriIdx.getId(a);
      }
    }

    return null;
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
   * @throws IllegalArgumentException
   *           if the uri does not refer to the live version of the resource
   * @throws IOException
   *           if accessing the index fails
   */
  public String getPath(ResourceURI uri) throws IOException,
      IllegalArgumentException {

    String id = uri.getIdentifier();
    if (id == null)
      throw new IllegalArgumentException("ResourceURI must contain an identifier");

    long[] addresses = idIdx.locate(id);

    // Is the uri part of the index?
    if (addresses.length == 0) {
      logger.warn("Attempt to locate non-existing id {}", id);
      return null;
    }

    // Locate the entry in question
    for (long a : addresses) {
      String idxId = uriIdx.getId(a);
      if (idxId.equals(id)) {
        return uriIdx.getPath(a);
      }
    }

    return null;
  }

  /**
   * Returns the resource type or <code>null</code> the resource could not be
   * found.
   * 
   * @param uri
   *          the resource uri
   * @return the resource type
   * @throws IOException
   *           if accessing the index fails
   */
  public String getType(ResourceURI uri) throws IOException {
    long address = toURIEntry(uri);
    if (address == -1)
      return null;
    return uriIdx.getType(address);
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

    if (resource.isIndexed())
      searchIdx.update(resource);
    else
      searchIdx.delete(uri);

    String newPath = StringUtils.trimToNull(uri.getPath());
    long version = resource.getURI().getVersion();
    String type = uri.getType();

    // Find the storage address
    long address = toURIEntry(uri);

    // Update the path
    if (version == Resource.LIVE) {
      String oldPath = uriIdx.getPath(address);
      uriIdx.update(address, type, newPath);
      if (oldPath != null)
        pathIdx.delete(oldPath, address);
      if (newPath != null)
        pathIdx.set(address, newPath);
    }
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

    // Locate the entry in question
    long address = toURIEntry(uri);

    // Everything ok?
    if (address == -1)
      throw new IllegalArgumentException("Uri " + uri + " was not found");

    path = StringUtils.trimToNull(path);
    String oldPath = uri.getPath();

    // If this resource did not have a path in the very beginning we can just
    // use the uri index to look up the path that was used.
    if (oldPath == null) {
      oldPath = getPath(uri);
    }

    // Do it this way to make sure we have identical path trimming
    ResourceURI newURI = new ResourceURIImpl(uri.getType(), uri.getSite(), path, uri.getIdentifier(), uri.getVersion());
    path = newURI.getPath();

    if (uri.getVersion() == Resource.LIVE) {
      uriIdx.update(address, uri.getType(), path);
      if (oldPath != null)
        pathIdx.delete(oldPath, address);
      if (path != null)
        pathIdx.set(address, path);
    }

    searchIdx.move(uri, path);
  }

  /**
   * Removes all entries from the index.
   * 
   * @throws IOException
   *           if clearing the index fails
   */
  public synchronized void clear() throws IOException {
    uriIdx.clear();
    idIdx.clear();
    pathIdx.clear();
    versionIdx.clear();
    searchIdx.clear();
  }

  /**
   * Returns <code>true</code> if the given uri exists in the given version.
   * 
   * @param uri
   *          the uri
   * @return <code>true</code> if the uri exists
   */
  public boolean exists(ResourceURI uri) throws IOException {
    long address = toURIEntry(uri);
    if (address < 0)
      return false;
    if (uri.getType() != null && !uri.getType().equals(uriIdx.getType(address)))
      return false;
    return versionIdx.hasVersion(address, uri.getVersion());
  }

  /**
   * Returns <code>true</code> if the given uri exists in any version.
   * 
   * @param uri
   *          the uri
   * @return <code>true</code> if the uri exists in any version
   */
  public boolean existsInAnyVersion(ResourceURI uri) throws IOException {
    long address = toURIEntry(uri);
    return address > -1;
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

  /**
   * Returns the number of entries in this index.
   * 
   * @return the number of indexed entries
   * @throws IOException
   *           if reading the number of entries fails
   */
  public long size() throws IOException {
    return uriIdx.getEntries();
  }

  /**
   * Returns the position of the uri in the <code>uri</code> index or
   * <code>-1</code> if there is no such entry.
   * 
   * @param uri
   *          the uri
   * @return the id
   * @throws IllegalArgumentException
   *           if the uri contains neither an id nor a path of if the uri is
   *           <code>null</code>
   * @throws IOException
   *           if accessing the index fails
   */
  protected long toURIEntry(ResourceURI uri) throws IOException {
    if (uri == null)
      throw new IllegalArgumentException("Uri must not be null");

    String id = uri.getIdentifier();
    String path = StringUtils.trimToNull(uri.getPath());
    String type = uri.getType();

    if (id == null && path == null)
      throw new IllegalArgumentException("Uri must contain either id or path");

    long[] addresses = null;
    if (id != null) {
      addresses = idIdx.locate(id);
    } else {
      addresses = pathIdx.locate(path);
    }

    // Is the uri part of the index?
    if (addresses.length == 0) {
      return -1;
    }

    // Locate the entry in question
    for (long a : addresses) {
      if (id != null) {
        if (id.equals(uriIdx.getId(a)) && (type == null || type.equals(uriIdx.getType(a))))
          return a;
      } else {
        if (path.equals(uriIdx.getPath(a)) && (type == null || type.equals(uriIdx.getType(a))))
          return a;
      }
    }

    return -1;
  }

}