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
import ch.o2it.weblounge.contentrepository.ResourceSerializer;
import ch.o2it.weblounge.contentrepository.ResourceSerializerFactory;
import ch.o2it.weblounge.contentrepository.impl.index.solr.ResourceURIInputDocument;
import ch.o2it.weblounge.contentrepository.impl.index.solr.SolrConnection;
import ch.o2it.weblounge.contentrepository.impl.index.solr.SolrRequester;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest.ACTION;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * A Solr-based search index implementation.
 */
public class SearchIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SearchIndex.class);

  /** Directory name of the solr configuration directory */
  private static final String CONF_DIR = "conf";

  /** Directory name of the solr configuration directory */
  private static final String DATA_DIR = "data";

  /** Connection to the solr database */
  private SolrConnection solrConnection = null;

  /** Solr query execution */
  private SolrRequester solrRequester = null;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /** The solr root */
  protected File solrRoot = null;

  /**
   * Creates a search index that puts solr into the given root directory. If the
   * directory doesn't exist, it will be created.
   * 
   * @param solrRoot
   *          the solr root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if either loading or creating the index fails
   */
  public SearchIndex(File solrRoot, boolean readOnly) throws IOException {
    this.solrRoot = solrRoot;
    this.isReadOnly = readOnly;
    try {
      loadSolr(solrRoot);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException(e);
    }

  }

  /**
   * Closes this index.
   * 
   * @throws IOException
   *           if closing the index file fails
   */
  public void close() throws IOException {
    try {
      solrConnection.destroy();
    } catch (Throwable t) {
      logger.error("Error closing the solr connection");
    }
  }

  /**
   * Makes a request to solr and returns the result set.
   * 
   * @param query
   *          the search query
   * @return the result set
   * @throws IOException
   *           if executing the search operation fails
   */
  public SearchResult getByQuery(SearchQuery query) throws IOException {
    logger.debug("Searching index using query '{}'", query);
    try {
      return solrRequester.getByQuery(query);
    } catch (SolrServerException e) {
      throw new IOException(e);
    }
  }

  /**
   * Clears the search index. Make sure you know what you are doing.
   * 
   * @throws IOException
   *           if an errors occurs while talking to solr
   */
  public void clear() throws IOException {
    try {
      solrConnection.destroy();
      initSolr(solrRoot);
      loadSolr(solrRoot);
    } catch (Exception e) {
      logger.error("Cannot clear solr index", e);
    }
  }

  /**
   * Removes the entry with the given <code>id</code> from the database. The
   * entry can either be a resource or a resource.
   * 
   * @param id
   *          identifier of the resource or resource
   * @throws IOException
   *           if an errors occurs while talking to solr
   */
  public boolean delete(ResourceURI uri) throws IOException {
    logger.debug("Removing element with id '{}' from searching index", uri.getId());
    UpdateRequest solrRequest = new UpdateRequest();
    solrRequest.deleteById(uri.getId());
    solrRequest.setAction(ACTION.COMMIT, true, true);
    try {
      solrConnection.update(solrRequest);
      return true;
    } catch (Exception e) {
      logger.error("Unable to clear solr index", e);
      return false;
    }
  }

  /**
   * Posts the resource to the search index.
   * 
   * @param resource
   *          the resource to add to the index
   * @throws IOException
   *           if an errors occurs while talking to solr
   */
  public boolean add(Resource resource) throws IOException {
    logger.debug("Adding resource {} to search index", resource);
    UpdateRequest solrRequest = new UpdateRequest();
    solrRequest.setAction(ACTION.COMMIT, true, true);

    // Have the serializer create an input document
    String resourceType = resource.getURI().getType();
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializer(resourceType);
    if (serializer == null) {
      logger.error("Unable to create an input document for {}: no serializer found", resource.getURI());
      return false;
    }

    // Post everything to the search index
    try {
      SolrInputDocument inputDoc = serializer.getInputDocument(resource);
      solrRequest.add(inputDoc);
      solrConnection.update(solrRequest);
      return true;
    } catch (Exception e) {
      logger.error("Cannot add resource " + resource + " to index", e);
      return false;
    }
  }

  /**
   * Posts the updated resource to the search index.
   * 
   * @param resource
   *          the resource to update
   */
  public boolean update(Resource<?> resource) {
    logger.debug("Updating resource {} in search index", resource);

    // Have the serializer create an input document
    String resourceType = resource.getURI().getType();
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializer(resourceType);
    if (serializer == null) {
      logger.error("Unable to create an input document for {}: no serializer found", resource.getURI());
      return false;
    }

    // Post the updated data to the search index
    try {
      SolrInputDocument inputDoc = serializer.getInputDocument(resource);
      update(inputDoc);
      return true;
    } catch (Exception e) {
      logger.error("Cannot update resource " + resource + " in index", e);
      return false;
    }
  }

  /**
   * Posts the input document to the search index.
   * 
   * @param document
   *          the input document
   * @return the query response
   * @throws Exception
   *           posting to solr fails
   */
  protected QueryResponse update(SolrInputDocument document) throws Exception {
    UpdateRequest solrRequest = new UpdateRequest();
    solrRequest.setAction(ACTION.COMMIT, true, true);
    solrRequest.add(document);
    return solrConnection.update(solrRequest);
  }

  /**
   * Move the resource identified by <code>uri</code> to the new location.
   * 
   * @param uri
   *          the resource uri
   * @param path
   *          the new path
   * @return
   */
  public boolean move(ResourceURI uri, String path) {
    logger.debug("Updating path {} in search index to ", uri.getPath(), path);
    try {
      ResourceURIImpl newURI = new ResourceURIImpl(uri.getType(), uri.getSite(), path, uri.getId());
      update(new ResourceURIInputDocument(newURI));
      return true;
    } catch (Exception e) {
      logger.error("Cannot update resource uri " + uri + " in index", e);
      return false;
    }
  }

  /**
   * Tries to load solr from the specified directory. If that directory is not
   * there, or in the case where either one of solr config or data directory is
   * missing, a preceding call to <code>initSolr()</code> is made.
   * 
   * @param solrRoot
   *          the solr root directory
   * @throws Exception
   *           if loading or creating solr fails
   */
  private void loadSolr(File solrRoot) throws Exception {
    logger.debug("Setting up solr search index at {}", solrRoot);
    File configDir = new File(solrRoot, CONF_DIR);
    File dataDir = new File(solrRoot, DATA_DIR);

    boolean configExists = configDir.exists() && configDir.list().length >= 6;
    boolean dataExists = dataDir.exists() && dataDir.list().length > 0;

    // Create the configuration directory
    if (configExists && dataExists) {
      logger.debug("Using solr search index at {}", solrRoot);
    } else {
      initSolr(solrRoot);
    }

    SolrCore.log.getParent().setLevel(Level.SEVERE);
    solrConnection = new SolrConnection(solrRoot.getAbsolutePath(), dataDir.getAbsolutePath());
    solrRequester = new SolrRequester(solrConnection);
  }

  /**
   * Prepares the solr environment by creating the necessary directories and
   * copying the configuration files into place.
   * 
   * @param solrRoot
   *          the solr root directory
   */
  private void initSolr(File solrRoot) {
    try {
      logger.debug("Creating search index at {}", solrRoot);
      File solrConfigDir = new File(solrRoot, CONF_DIR);
      File solrDataDir = new File(solrRoot, DATA_DIR);

      // Delete and re-create the config directory
      FileUtils.deleteQuietly(solrConfigDir);
      FileUtils.forceMkdir(solrConfigDir);

      // Delete and re-create the data directory
      FileUtils.deleteQuietly(solrDataDir);
      FileUtils.forceMkdir(solrDataDir);

      // Make sure there is a configuration in place
      copyClasspathResourceToFile("/solr/protwords.txt", solrConfigDir);
      copyClasspathResourceToFile("/solr/schema.xml", solrConfigDir);
      copyClasspathResourceToFile("/solr/scripts.conf", solrConfigDir);
      copyClasspathResourceToFile("/solr/solrconfig.xml", solrConfigDir);
      copyClasspathResourceToFile("/solr/stopwords.txt", solrConfigDir);
      copyClasspathResourceToFile("/solr/synonyms.txt", solrConfigDir);
    } catch (IOException e) {
      throw new RuntimeException("Error setting up solr index at " + solrRoot, e);
    }
  }

  /**
   * Utility method that will copy the specified class path resource to the
   * config directory.
   * 
   * @param classpath
   *          the path inside the bundle
   * @param dir
   *          the config directory
   */
  private void copyClasspathResourceToFile(String classpath, File dir) {
    InputStream in = SearchIndex.class.getResourceAsStream(classpath);
    try {
      File file = new File(dir, FilenameUtils.getName(classpath));
      logger.debug("copying inputstream " + in + " to file to " + file);
      IOUtils.copy(in, new FileOutputStream(file));
    } catch (IOException e) {
      throw new RuntimeException("Error copying solr classpath resource to the filesystem", e);
    }
  }

}
