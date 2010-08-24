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

package ch.o2it.weblounge.contentrepository.impl.index;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

/**
 * This index into the repository is used to map resource and resource urls into
 * repository indices and vice versa. In addition, it will facilitate listing
 * url hierarchies.
 */
public class ContentRepositoryIndex {

  /** Logging facility */
  private final static Logger logger = LoggerFactory.getLogger(ContentRepositoryIndex.class);

  /** The uri index */
  protected URIIndex uriIdx = null;

  /** The id index */
  protected IdIndex idIdx = null;

  /** The path index */
  protected PathIndex pathIdx = null;

  /** The version index */
  protected VersionIndex versionIdx = null;

  /** The language index */
  protected LanguageIndex languageIdx = null;

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
    this.languageIdx = new LanguageIndex(new File(rootDir, "structure"), readOnly);
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
      PathIndex pathIndex, VersionIndex versionIndex,
      LanguageIndex languageIndex) {
    this.uriIdx = uriIndex;
    this.idIdx = idIndex;
    this.pathIdx = pathIndex;
    this.versionIdx = versionIndex;
    this.languageIdx = languageIndex;
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
    int languageIdxVersion = languageIdx.getIndexVersion();
    if (idIdxVersion != version || pathIdxVersion != version || versionIdxVersion != version || languageIdxVersion != version) {
      logger.warn("Version mismatch detected in structural index");
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
   * Sets the language index.
   * 
   * @param languageIndex
   *          the language index
   */
  protected void setLanguageIndex(LanguageIndex languageIndex) {
    this.languageIdx = languageIndex;
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
    if (languageIdx != null)
      languageIdx.close();
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
   * Returns the number of versions in this index.
   * 
   * @return the number of versions
   */
  public long getVersions() {
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
    if (uri.getPath() == null)
      throw new IllegalArgumentException("Uri must contain a path");

    long address = toURIEntry(uri);

    // If there is no address, we are about to add a new resource
    if (address < 0) {
      String id = uri.getId();
      if (id == null) {
        id = UUID.randomUUID().toString();
        uri = new ResourceURIImpl(uri.getType(), uri.getSite(), uri.getPath(), uri.getVersion(), id);
        ((ResourceURIImpl) resource.getURI()).setIdentifier(id);
      }
      String type = uri.getType();
      String path = uri.getPath();

      try {
        if (uri.getVersion() == Resource.LIVE) {
          if (resource.isIndexed())
            searchIdx.add(resource);
          else
            searchIdx.delete(uri);
        } else {
          languageIdx.add(id, null);
        }
        address = uriIdx.add(id, type, path);
        idIdx.add(id, address);
        pathIdx.add(path, address);
        versionIdx.add(id, uri.getVersion());
        languageIdx.setLanguages(id, resource.languages());
      } catch (ContentRepositoryException e) {
        throw e;
      } catch (Throwable t) {
        throw new ContentRepositoryException("Error adding " + resource + " to index", t);
      }
    }

    // Otherwise, it's just a new version
    else if (!versionIdx.hasVersion(address, uri.getVersion())) {
      versionIdx.add(address, uri.getVersion());
      if (uri.getVersion() == Resource.LIVE) {
        languageIdx.setLanguages(address, resource.languages());
        if (resource.isIndexed())
          searchIdx.add(resource);
        else
          searchIdx.delete(uri);
      }
    }

    // Seems to be an existing resource, so it's an update rather than an
    // addition
    else {
      logger.warn("Existing resource '{}' was passed to add(), redirecting to update()", uri.getId());
      update(resource);
    }

    return uri;
  }

  /**
   * Removes all entries for the given resource uri from the index.
   * 
   * @param uri
   *          the resource uri
   * @throws IOException
   *           if updating the index fails
   * @throws ContentRepositoryException
   *           if deleting the resource fails
   */
  public synchronized void delete(ResourceURI uri) throws IOException,
      ContentRepositoryException {
    String id = uri.getId();
    String path = uri.getPath();

    // Locate the entry in question
    long address = toURIEntry(uri);

    // Everything ok?
    if (address == -1)
      throw new IllegalStateException("Inconsistencies found in index. Uri " + uri + " cannot be located");

    // Load the missing data
    if (id == null)
      id = uriIdx.getId(address);
    if (path == null)
      path = uriIdx.getPath(address);

    // Delete the entry
    searchIdx.delete(uri);
    idIdx.delete(id, address);
    pathIdx.delete(path, address);
    uriIdx.delete(address);
    versionIdx.delete(address);
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
      throw new IllegalStateException("Inconsistencies found in index. Uri " + uri + " cannot be located");

    return versionIdx.getVersions(address);
  }

  /**
   * Returns all languages for the specified resource or <code>null</code> if
   * the resource doesn't exist.
   * 
   * @param uri
   *          the resource uri
   * @return the languages
   */
  public Language[] getLanguages(ResourceURI uri) throws IOException {
    // Locate the entry in question
    long address = toURIEntry(uri);

    // Everything ok?
    if (address == -1)
      throw new IllegalStateException("Inconsistencies found in index. Uri " + uri + " cannot be located");

    return languageIdx.getLanguages(address);
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
   * @throws IOException
   *           if accessing the index fails
   */
  public String getIdentifier(ResourceURI uri) throws IOException {
    String path = uri.getPath();
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
   * @throws IOException
   *           if accessing the index fails
   */
  public String getPath(ResourceURI uri) throws IOException {
    String id = uri.getId();
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
   * Updates the contents of the given resource.
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
    if (uri.getVersion() == Resource.LIVE) {
      if (resource.isIndexed())
        searchIdx.update(resource);
      else
        searchIdx.delete(resource.getURI());

      long address = toURIEntry(resource.getURI());
      languageIdx.setLanguages(address, null);
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
   */
  public synchronized void move(ResourceURI uri, String path)
      throws IOException, ContentRepositoryException {
    String oldPath = uri.getPath();

    // Locate the entry in question
    long address = toURIEntry(uri);

    // Everything ok?
    if (address == -1)
      throw new IllegalStateException("Inconsistencies found in index. Uri " + uri + " cannot be located");

    // Do it this way to make sure we have identical path trimming
    uri = new ResourceURIImpl(uri.getType(), uri.getSite(), path, uri.getVersion(), uri.getId());

    pathIdx.delete(oldPath, address);
    pathIdx.add(uri.getPath(), address);
    uriIdx.update(address, uri.getType(), uri.getPath());
    if (uri.getVersion() == Resource.LIVE) {
      searchIdx.move(uri, path);
    }
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
    languageIdx.clear();
    searchIdx.clear();
  }

  /**
   * Returns <code>true</code> if the given uri exists.
   * 
   * @param uri
   *          the uri
   * @return <code>true</code> if the uri exists
   */
  public boolean exists(ResourceURI uri) throws IOException {
    long address = toURIEntry(uri);
    if (address == -1)
      return false;
    return versionIdx.hasVersion(address, uri.getVersion());
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
   *           if the uri contains neither an id nor a path
   * @throws IOException
   *           if accessing the index fails
   */
  protected long toURIEntry(ResourceURI uri) throws IOException {
    String id = uri.getId();
    String path = uri.getPath();

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
        if (id.equals(uriIdx.getId(a)))
          return a;
      } else {
        if (path.equals(uriIdx.getPath(a)))
          return a;
      }
    }

    return -1;
  }

}
