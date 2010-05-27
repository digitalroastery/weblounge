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

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.contentrepository.impl.index.solr.PageInputDocument;
import ch.o2it.weblounge.contentrepository.impl.index.solr.SolrConnection;
import ch.o2it.weblounge.contentrepository.impl.index.solr.SolrRequester;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest.ACTION;
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

  /** Connection to the solr database */
  private SolrConnection solrConnection = null;

  /** Solr query execution */
  private SolrRequester solrRequester = null;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /**
   * Creates a search index that puts solr into the given root directory. If the
   * directory doesn't exist, it will be created.
   * 
   * @param solrRoot
   *          the solr root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   */
  public SearchIndex(File solrRoot, boolean readOnly) {
    setupSolr(solrRoot);
    this.isReadOnly = readOnly;
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
    UpdateRequest solrRequest = new UpdateRequest();
    solrRequest.deleteByQuery("*:*");
    solrRequest.setAction(ACTION.COMMIT, true, true);
    try {
      solrConnection.update(solrRequest);
    } catch (Exception e) {
      logger.error("Cannot clear solr index", e);
    }
  }

  /**
   * Removes the entry with the given <code>id</code> from the database. The
   * entry can either be a page or a resource.
   * 
   * @param id
   *          identifier of the page or resource
   * @throws IOException
   *           if an errors occurs while talking to solr
   */
  public boolean delete(String id) throws IOException {
    logger.debug("Removing element with id '{}' from searching index", id);
    UpdateRequest solrRequest = new UpdateRequest();
    solrRequest.deleteById(id);
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
   * Posts the page to the search index.
   * 
   * @param page
   *          the page to add to the index
   * @throws IOException
   *           if an errors occurs while talking to solr
   */
  public boolean add(Page page) throws IOException {
    logger.debug("Adding page {} to searching index", page);
    UpdateRequest solrRequest = new UpdateRequest();
    solrRequest.setAction(ACTION.COMMIT, true, true);
    solrRequest.add(new PageInputDocument(page));

    // Post everything to the search index
    try {
      solrConnection.update(solrRequest);
      return true;
    } catch (Exception e) {
      logger.error("Cannot clear solr index");
      return false;
    }
  }

  /**
   * Prepares the solr environment.
   * 
   * @param solrRoot
   *          the solr root directory
   */
  private void setupSolr(File solrRoot) {
    try {
      logger.info("Setting up solr search index at {}", solrRoot);
      File solrConfigDir = new File(solrRoot, "conf");

      // Create the config directory
      if (solrConfigDir.exists()) {
        logger.info("Using solr search index at {}", solrRoot);
      } else {
        logger.info("Solr config directory doesn't exist, creating one at {}", solrConfigDir);
        FileUtils.forceMkdir(solrConfigDir);
      }

      // Make sure there is a configuration in place
      copyClasspathResourceToFile("/solr/protwords.txt", solrConfigDir);
      copyClasspathResourceToFile("/solr/schema.xml", solrConfigDir);
      copyClasspathResourceToFile("/solr/scripts.conf", solrConfigDir);
      copyClasspathResourceToFile("/solr/solrconfig.xml", solrConfigDir);
      copyClasspathResourceToFile("/solr/stopwords.txt", solrConfigDir);
      copyClasspathResourceToFile("/solr/synonyms.txt", solrConfigDir);

      // Test for the existence of the index. Note that an empty index directory
      // will prevent solr from completing normal setup.
      File solrIndexDir = new File(solrRoot, "index");
      if (solrIndexDir.exists() && solrIndexDir.list().length == 0) {
        FileUtils.deleteDirectory(solrIndexDir);
      }

      SolrCore.log.getParent().setLevel(Level.WARNING);
      solrConnection = new SolrConnection(solrRoot.getAbsolutePath(), solrRoot.getAbsolutePath());
      solrRequester = new SolrRequester(solrConnection);
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
