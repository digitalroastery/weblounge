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

import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.ALTERNATE_VERSION;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.ID;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.LOCALIZED_FULLTEXT;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.RESOURCE_ID;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.TYPE;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema.VERSION;
import static org.apache.solr.client.solrj.request.AbstractUpdateRequest.ACTION.COMMIT;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.ResourceMetadataImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;
import ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.index.solr.SearchRequest;
import ch.entwine.weblounge.contentrepository.impl.index.solr.SolrRequester;
import ch.entwine.weblounge.contentrepository.impl.index.solr.SolrSchema;
import ch.entwine.weblounge.contentrepository.impl.index.solr.SuggestRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest.ACTION;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Solr-based search index implementation.
 */
public class SearchIndex implements VersionedContentRepositoryIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SearchIndex.class);

  /** Directory name of the solr configuration directory */
  private static final String CONF_DIR = "conf";

  /** Directory name of the solr configuration directory */
  private static final String DATA_DIR = "data";

  /** Identifier of the root entry */
  public static final long ROOT_ID = 0L;

  /** Type of the root entry */
  private static final String ROOT_TYPE = "index";

  /** Connection to the solr database */
  private SolrRequester solrConnection = null;

  /** Solr query execution */
  private SearchRequest solrRequester = null;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /** The solr root */
  protected File solrRoot = null;

  /** The version number */
  protected int indexVersion = -1;

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
    } catch (Throwable t) {
      throw new IOException("Error loading solr index", t);
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
      throw new IOException("Error closing the solr connection", t);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex#getIndexVersion()
   */
  public int getIndexVersion() {
    return indexVersion;
  }

  /**
   * Makes a request to solr and returns the result set.
   * 
   * @param query
   *          the search query
   * @return the result set
   * @throws ContentRepositoryException
   *           if executing the search operation fails
   */
  public SearchResult getByQuery(SearchQuery query)
      throws ContentRepositoryException {
    logger.debug("Searching index using query '{}'", query);
    try {
      return solrRequester.getByQuery(query);
    } catch (Throwable t) {
      throw new ContentRepositoryException("Error querying solr index", t);
    }
  }

  /**
   * Clears the search index. Make sure you know what you are doing.
   * 
   * @throws IOException
   *           if clearing the solr index fails
   */
  public void clear() throws IOException {
    try {
      solrConnection.destroy();
      initSolr(solrRoot);
      loadSolr(solrRoot);
    } catch (Throwable t) {
      throw new IOException("Cannot clear solr index", t);
    }
  }

  /**
   * Removes the entry with the given <code>id</code> from the database.
   * 
   * @param resourceId
   *          identifier of the resource or resource
   * @throws ContentRepositoryException
   *           if removing the resource from solr fails
   */
  @SuppressWarnings("unchecked")
  public boolean delete(ResourceURI uri) throws ContentRepositoryException {
    logger.debug("Removing element with id '{}' from searching index", uri.getIdentifier());

    UpdateRequest solrRequest = new UpdateRequest();
    StringBuilder query = new StringBuilder();
    query.append(RESOURCE_ID).append(":").append(uri.getIdentifier()).append(" AND ").append(VERSION).append(":").append(uri.getVersion());
    solrRequest.setAction(ACTION.COMMIT, true, true);
    solrRequest.deleteByQuery(query.toString());
    try {
      solrConnection.update(solrRequest);
    } catch (Throwable t) {
      throw new ContentRepositoryException("Unable to clear solr index", t);
    }

    // Have the serializer create an input document
    String resourceType = uri.getType();
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
    if (serializer == null)
      throw new ContentRepositoryException("No serializer found for " + uri);

    // Add information on this version to existing variants of this resource
    Site site = uri.getSite();
    String id = uri.getIdentifier();
    SearchQuery q = new SearchQueryImpl(site).withIdentifier(id);
    for (SearchResultItem existingResource : getByQuery(q).getItems()) {
      List<ResourceMetadata<?>> solrFields = ((ResourceSearchResultItem) existingResource).getMetadata();
      ResourceMetadata<Long> versionField = null;
      for (ResourceMetadata<?> field : solrFields) {
        if (field.getName().equals(ALTERNATE_VERSION)) {
          versionField = (ResourceMetadata<Long>) field;
          List<Long> versions = ((ResourceMetadata<Long>) field).getValues();
          versionField.clear();

          // Add the remaining versions back
          for (Long l : versions) {
            if (l.longValue() != uri.getVersion()) {
              versionField.addValue(l);
            }
          }
        }
      }
      if (versionField != null && versionField.getValues().size() == 0)
        solrFields.remove(versionField);
      SolrInputDocument doc = updateDocument(new SolrInputDocument(), solrFields);
      update(doc);
    }

    return true;
  }

  /**
   * Posts the resource to the search index.
   * 
   * @param resource
   *          the resource to add to the index
   * @throws ContentRepositoryException
   *           if posting the new resource to solr fails
   */
  @SuppressWarnings("unchecked")
  public boolean add(Resource<?> resource) throws ContentRepositoryException {
    logger.debug("Adding resource {} to search index", resource);

    // Have the serializer create an input document
    String resourceType = resource.getURI().getType();
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
    if (serializer == null)
      throw new ContentRepositoryException("Unable to create an input document for " + resource.getURI() + ": no serializer found");

    // Add information on this version to existing variants of this resource
    ResourceURI uri = resource.getURI();
    Site site = uri.getSite();
    String id = uri.getIdentifier();
    SearchQuery q = new SearchQueryImpl(site).withIdentifier(id);
    List<Long> existingVersions = new ArrayList<Long>();
    for (SearchResultItem existingResource : getByQuery(q).getItems()) {
      List<ResourceMetadata<?>> existingResourceMetadata = ((ResourceSearchResultItem) existingResource).getMetadata();
      boolean alternateVersionsFound = false;
      for (ResourceMetadata<?> field : existingResourceMetadata) {
        if (field.getName().equals(ALTERNATE_VERSION)) {
          alternateVersionsFound = true;
          ((ResourceMetadata<Long>) field).addValue(uri.getVersion());
        } else if (field.getName().equals(VERSION)) {
          existingVersions.add(((ResourceMetadata<Long>) field).getValues().get(0));
        }
      }
      if (!alternateVersionsFound) {
        ResourceMetadata<Long> altVersions = new ResourceMetadataImpl<Long>(ALTERNATE_VERSION);
        altVersions.addValue(uri.getVersion());
        existingResourceMetadata.add(altVersions);
      }
      SolrInputDocument doc = updateDocument(new SolrInputDocument(), existingResourceMetadata);
      update(doc);
    }

    // Post the updated data to the search index
    try {
      List<ResourceMetadata<?>> metadata = serializer.toMetadata(resource);
      metadata.add(new ResourceMetadataImpl<Long>(ALTERNATE_VERSION, existingVersions, null, false));
      SolrInputDocument doc = updateDocument(new SolrInputDocument(), metadata);
      update(doc);
      return true;
    } catch (Throwable t) {
      throw new ContentRepositoryException("Cannot update resource " + resource + " in index", t);
    }
  }

  /**
   * Posts the updated resource to the search index.
   * 
   * @param resource
   *          the resource to update
   * @throws ContentRepositoryException
   *           if posting the updated resource to solr fails
   */
  @SuppressWarnings("unchecked")
  public boolean update(Resource<?> resource) throws ContentRepositoryException {
    logger.debug("Updating resource {} in search index", resource);

    // Have the serializer create an input document
    String resourceType = resource.getURI().getType();
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
    if (serializer == null) {
      logger.error("Unable to create an input document for {}: no serializer found", resource.getURI());
      return false;
    }

    // Add information on this version to existing variants of this resource
    ResourceURI uri = resource.getURI();
    Site site = uri.getSite();
    String id = uri.getIdentifier();
    SearchQuery q = new SearchQueryImpl(site).withIdentifier(id);
    List<Long> alternateVersions = new ArrayList<Long>();
    for (SearchResultItem existingResource : getByQuery(q).getItems()) {
      List<ResourceMetadata<?>> existingResourceMetadata = ((ResourceSearchResultItem) existingResource).getMetadata();
      for (ResourceMetadata<?> field : existingResourceMetadata) {
        if (field.getName().equals(VERSION)) {
          Long version = ((ResourceMetadata<Long>) field).getValues().get(0);
          if (version != uri.getVersion())
            alternateVersions.add(version);
        }
      }
    }

    // Post the updated data to the search index
    try {
      List<ResourceMetadata<?>> metadata = serializer.toMetadata(resource);
      metadata.add(new ResourceMetadataImpl<Long>(ALTERNATE_VERSION, alternateVersions, null, false));
      SolrInputDocument doc = updateDocument(new SolrInputDocument(), metadata);
      update(doc);
      return true;
    } catch (Throwable t) {
      throw new ContentRepositoryException("Cannot update resource " + resource + " in index", t);
    }
  }

  /**
   * Adds <code>metadata</code> as fields to the input document.
   * 
   * @param doc
   *          the solr input document
   * @param metadata
   *          the metadata
   * @return the enriched input document
   */
  private SolrInputDocument updateDocument(SolrInputDocument doc,
      List<ResourceMetadata<?>> metadata) {
    for (ResourceMetadata<?> entry : metadata) {
      String metadataKey = entry.getName();

      // Add language neutral metadata values
      for (Object value : entry.getValues()) {
        doc.addField(metadataKey, value);

        // Add to fulltext?
        if (entry.addToFulltext()) {
          String fulltext = StringUtils.trimToEmpty((String) doc.getFieldValue(SolrSchema.FULLTEXT));
          if (value.getClass().isArray()) {
            Object[] fieldValues = (Object[]) value;
            for (Object v : fieldValues) {
              fulltext = StringUtils.join(new Object[] { fulltext, v.toString() }, " ");
            }
          } else {
            fulltext = StringUtils.join(new Object[] {
                fulltext,
                value.toString() }, " ");
          }
          doc.setField(SolrSchema.FULLTEXT, fulltext);
        }
      }

      // Add localized metadata values
      for (Language language : entry.getLocalizedValues().keySet()) {
        List<?> values = entry.getLocalizedValues().get(language);
        for (Object value : values) {
          doc.addField(metadataKey, value);

          // Add to fulltext
          if (entry.addToFulltext()) {

            // Update the localized fulltext
            String localizedFieldName = MessageFormat.format(LOCALIZED_FULLTEXT, language.getIdentifier());
            String localizedFulltext = StringUtils.trimToEmpty((String) doc.getFieldValue(localizedFieldName));
            if (value.getClass().isArray()) {
              Object[] fieldValues = (Object[]) value;
              for (Object v : fieldValues) {
                localizedFulltext = StringUtils.join(new Object[] {
                    localizedFulltext,
                    v.toString() }, " ");
              }
            } else {
              localizedFulltext = StringUtils.join(new Object[] {
                  localizedFulltext,
                  value.toString() }, " ");
            }
            doc.setField(localizedFieldName, localizedFulltext);
          }
        }
      }

    }

    return doc;
  }

  /**
   * Posts the input document to the search index.
   * 
   * @param document
   *          the input document
   * @return the query response
   * @throws ContentRepositoryException
   *           if posting to solr fails
   */
  protected QueryResponse update(SolrInputDocument document)
      throws ContentRepositoryException {
    UpdateRequest solrRequest = new UpdateRequest();
    solrRequest.setAction(ACTION.COMMIT, true, true);
    solrRequest.add(document);
    try {
      return solrConnection.update(solrRequest);
    } catch (Throwable t) {
      throw new ContentRepositoryException("Cannot update document " + document + " in index", t);
    }
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
  public boolean move(ResourceURI uri, String path)
      throws ContentRepositoryException {
    logger.debug("Updating path {} in search index to ", uri.getPath(), path);

    SearchQuery q = new SearchQueryImpl(uri.getSite()).withVersion(uri.getVersion()).withIdentifier(uri.getIdentifier());
    SearchResultItem[] searchResult = getByQuery(q).getItems();
    if (searchResult.length != 1) {
      logger.warn("Resource to be moved not found: {}", uri);
      return false;
    }

    // Have the serializer create an input document
    String resourceType = uri.getType();
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
    if (serializer == null) {
      logger.error("Unable to create an input document for {}: no serializer found", uri);
      return false;
    }

    // Prepare the search metadata as a map, keep a reference to the path
    List<ResourceMetadata<?>> metadata = ((ResourceSearchResultItem) searchResult[0]).getMetadata();
    Map<String, ResourceMetadata<?>> metadataMap = new HashMap<String, ResourceMetadata<?>>();
    for (ResourceMetadata<?> m : metadata) {
      metadataMap.put(m.getName(), m);
    }

    // Add the updated metadata, keep the rest
    Resource<?> resource = serializer.toResource(uri.getSite(), metadata);
    resource.setPath(path);
    for (ResourceMetadata<?> m : serializer.toMetadata(resource)) {
      metadataMap.put(m.getName(), m);
    }
    metadata = new ArrayList<ResourceMetadata<?>>(metadataMap.values());

    // Read the current resource and post the updated data to the search index
    try {
      SolrInputDocument doc = updateDocument(new SolrInputDocument(), metadata);
      update(doc);
      return true;
    } catch (Throwable t) {
      throw new ContentRepositoryException("Cannot update resource " + uri + " in index", t);
    }
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
   */
  public List<String> suggest(String dictionary, String seed,
      boolean onlyMorePopular, int count, boolean collate)
      throws ContentRepositoryException {
    if (StringUtils.isBlank(seed))
      throw new IllegalArgumentException("Seed must not be blank");
    if (StringUtils.isBlank(dictionary))
      throw new IllegalArgumentException("Dictionary must not be blank");

    SuggestRequest request = new SuggestRequest(solrConnection, dictionary, onlyMorePopular, count, collate);
    try {
      return request.getSuggestions(seed);
    } catch (Throwable t) {
      throw new ContentRepositoryException(t);
    }
  }

  /**
   * Tries to load solr from the specified directory. If that directory is not
   * there, or in the case where either one of solr configuration or data
   * directory is missing, a preceding call to <code>initSolr()</code> is made.
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

    solrConnection = new SolrRequester(solrRoot.getAbsolutePath(), dataDir.getAbsolutePath());
    solrRequester = new SearchRequest(solrConnection);

    // Determine the index version
    if (configExists && dataExists) {
      try {
        StringBuffer q = new StringBuffer(ID).append(":").append(ROOT_ID);
        QueryResponse r = solrConnection.request(q.toString());
        if (r.getResults().isEmpty()) {
          logger.warn("Index does not contain version information, triggering reindex");
          indexVersion = -1;
        } else {
          indexVersion = Integer.parseInt(r.getResults().get(0).getFieldValue(VERSION).toString());
          logger.info("Search index version is {}", indexVersion);
        }
      } catch (Throwable e) {
        logger.warn("Index version information cannot be determined ({}), triggering reindex", e.getMessage());
        indexVersion = -1;
      }
    } else {
      indexVersion = INDEX_VERSION;
      SolrInputDocument doc = new SolrInputDocument();
      doc.put(ID, createSolrInputField(ID, Long.toString(ROOT_ID)));
      doc.put(RESOURCE_ID, createSolrInputField(RESOURCE_ID, Long.toString(ROOT_ID)));
      doc.put(TYPE, createSolrInputField(TYPE, ROOT_TYPE));
      doc.put(VERSION, createSolrInputField(VERSION, Integer.toString(indexVersion)));
      UpdateRequest updateRequest = new UpdateRequest();
      updateRequest.add(doc);
      updateRequest.setAction(COMMIT, false, false);
      solrConnection.update(updateRequest);
    }
  }

  /**
   * Creates an ad-hoc solr input field.
   * 
   * @param name
   *          the field name
   * @param value
   *          the field value
   * @return the field
   */
  private SolrInputField createSolrInputField(String name, Object value) {
    SolrInputField field = new SolrInputField(name);
    field.setValue(value, 0.0f);
    return field;
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

      // Delete and re-create the configuration directory
      FileUtils.deleteQuietly(solrConfigDir);
      FileUtils.forceMkdir(solrConfigDir);

      // Delete and re-create the data directory
      FileUtils.deleteQuietly(solrDataDir);
      FileUtils.forceMkdir(solrDataDir);

      // Make sure there is a configuration in place
      copyBundleResourceToFile("/solr/elevate.xml", solrConfigDir);
      copyBundleResourceToFile("/solr/protwords.txt", solrConfigDir);
      copyBundleResourceToFile("/solr/schema.xml", solrConfigDir);
      copyBundleResourceToFile("/solr/scripts.conf", solrConfigDir);
      copyBundleResourceToFile("/solr/solrconfig.xml", solrConfigDir);
      copyBundleResourceToFile("/solr/stopwords.txt", solrConfigDir);
      copyBundleResourceToFile("/solr/synonyms.txt", solrConfigDir);

    } catch (IOException e) {
      throw new RuntimeException("Error setting up solr index at " + solrRoot, e);
    }
  }

  /**
   * Utility method that will copy the specified class path resource to the
   * configuration directory.
   * 
   * @param classpath
   *          the path inside the bundle
   * @param dir
   *          the configuration directory
   */
  private void copyBundleResourceToFile(String classpath, File dir) {
    InputStream is = null;
    FileOutputStream fos = null;
    try {
      is = SearchIndex.class.getResourceAsStream(classpath);
      File file = new File(dir, FilenameUtils.getName(classpath));
      fos = new FileOutputStream(file);
      IOUtils.copy(is, fos);
    } catch (IOException e) {
      throw new RuntimeException("Error copying solr classpath resource to the filesystem", e);
    } finally {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(fos);
    }
  }

}
