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

import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.ALTERNATE_VERSION;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.VERSION;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.impl.content.ResourceMetadataImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.SearchResultImpl;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.ResourceSerializer;
import ch.entwine.weblounge.common.repository.ResourceSerializerService;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.index.elasticsearch.ElasticSearchDocument;
import ch.entwine.weblounge.contentrepository.impl.index.elasticsearch.ElasticSearchSearchQuery;
import ch.entwine.weblounge.contentrepository.impl.index.elasticsearch.ElasticSearchUtils;
import ch.entwine.weblounge.contentrepository.impl.index.elasticsearch.SuggestRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A search index implementation based on ElasticSearch.
 */
public class SearchIndex implements VersionedContentRepositoryIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SearchIndex.class);

  /** Identifier of the root entry */
  public static final String ROOT_ID = "root";

  /** Type of the document containing the index version information */
  private static final String VERSION_TYPE = "version";

  /** The local elastic search node */
  private Node elasticSearch = null;

  /** Client for talking to elastic search */
  private Client nodeClient = null;

  /** True if this is a read only index */
  protected boolean isReadOnly = false;

  /** The solr root */
  protected File indexRoot = null;

  /** The site */
  protected Site site = null;

  /** The version number */
  protected int indexVersion = -1;

  /** The resource serializer */
  protected ResourceSerializerService resourceSerializer = null;

  /**
   * Creates a search index.
   * 
   * @param site
   *          the site
   * @param indexRoot
   *          the elastic search root directory
   * @param serializer
   *          the resource serializer
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if either loading or creating the index fails
   */
  public SearchIndex(Site site, File indexRoot,
      ResourceSerializerService serializer, boolean readOnly)
          throws IOException {
    this.site = site;
    this.indexRoot = indexRoot;
    this.resourceSerializer = serializer;
    this.isReadOnly = readOnly;
    try {
      init(site, indexRoot);
    } catch (Throwable t) {
      throw new IOException("Error creating elastic search index", t);
    }

  }

  /**
   * Shuts down the elastic search index node.
   * 
   * @throws IOException
   *           if stopping the index fails
   */
  public void close() throws IOException {
    try {
      if (nodeClient != null)
        nodeClient.close();
      if (elasticSearch != null) {
        elasticSearch.stop();
        elasticSearch.close();
      }
    } catch (Throwable t) {
      throw new IOException("Error stopping the elastic search node", t);
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
   * Makes a request and returns the result set.
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

    // See if the index version exists and check if it matches.
    String indexName = query.getSite().getIdentifier();
    SearchRequestBuilder requestBuilder = new SearchRequestBuilder(nodeClient);
    requestBuilder.setIndices(indexName);
    requestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH);
    requestBuilder.setPreference("_local");

    // Create the actual search query
    QueryBuilder queryBuilder = new ElasticSearchSearchQuery(query);
    requestBuilder.setQuery(queryBuilder);
    logger.debug("Searching for {}", requestBuilder.toString());

    // Make sure all fields are being returned
    requestBuilder.addField("*");

    // Restrict the scope to the given type
    if (query.getTypes().length > 0) {
      requestBuilder.setTypes(query.getTypes());
    } else {
      requestBuilder.setTypes("file", "image", "movie", "page");
    }

    // Pagination
    if (query.getOffset() >= 0)
      requestBuilder.setFrom(query.getOffset());
    if (query.getLimit() >= 0)
      requestBuilder.setSize(query.getLimit());

    // Order by publishing date
    if (!SearchQuery.Order.None.equals(query.getPublishingDateSortOrder())) {
      switch (query.getPublishingDateSortOrder()) {
        case Ascending:
          requestBuilder.addSort(IndexSchema.PUBLISHED_FROM, SortOrder.ASC);
          break;
        case Descending:
          requestBuilder.addSort(IndexSchema.PUBLISHED_FROM, SortOrder.DESC);
          break;
        case None:
        default:
          break;
      }
    }

    // Order by modification date
    else if (!SearchQuery.Order.None.equals(query.getModificationDateSortOrder())) {
      switch (query.getModificationDateSortOrder()) {
        case Ascending:
          requestBuilder.addSort(IndexSchema.MODIFIED, SortOrder.ASC);
          break;
        case Descending:
          requestBuilder.addSort(IndexSchema.MODIFIED, SortOrder.DESC);
          break;
        case None:
        default:
          break;
      }
    }

    // Order by creation date
    else if (!SearchQuery.Order.None.equals(query.getCreationDateSortOrder())) {
      switch (query.getCreationDateSortOrder()) {
        case Ascending:
          requestBuilder.addSort(IndexSchema.CREATED, SortOrder.ASC);
          break;
        case Descending:
          requestBuilder.addSort(IndexSchema.CREATED, SortOrder.DESC);
          break;
        case None:
        default:
          break;
      }
    }

    // Order by score
    // TODO: Order by score
    // else {
    // requestBuilder.addSort(IndexSchema.SCORE, SortOrder.DESC);
    // }

    try {

      // Execute the query and try to get hold of a query response
      SearchResponse response = null;
      try {
        response = nodeClient.search(requestBuilder.request()).actionGet();
      } catch (Throwable t) {
        throw new ContentRepositoryException(t);
      }

      // Create and configure the query result
      long hits = response.getHits().getTotalHits();
      long size = response.getHits().getHits().length;
      SearchResultImpl result = new SearchResultImpl(query, hits, size);
      result.setSearchTime(response.getTookInMillis());

      // Walk through response and create new items with title, creator, etc:
      for (SearchHit doc : response.getHits()) {

        // Get the resource serializer
        String type = doc.getType();
        ResourceSerializer<?, ?> serializer = resourceSerializer.getSerializerByType(type);
        if (serializer == null) {
          logger.warn("Skipping search result due to missing serializer of type {}", type);
          continue;
        }

        // Wrap the search result metadata
        List<ResourceMetadata<?>> metadata = new ArrayList<ResourceMetadata<?>>(doc.getFields().size());
        for (SearchHitField field : doc.getFields().values()) {
          String name = field.getName();
          ResourceMetadata<Object> m = new ResourceMetadataImpl<Object>(name);
          // TODO: Add values with more care (localized, correct type etc.)
          if (field.getValues().size() > 1) {
            for (Object v : field.getValues()) {
              m.addValue(v);
            }
          } else {
            m.addValue(field.getValue());
          }
          metadata.add(m);
        }

        // Get the score for this item
        float score = doc.getScore();

        // Have the serializer in charge create a type-specific search result
        // item
        try {
          SearchResultItem item = serializer.toSearchResultItem(query.getSite(), score, metadata);
          result.addResultItem(item);
        } catch (Throwable t) {
          logger.warn("Error during search result serialization: '{}'. Skipping this search result.", t.getMessage());
          continue;
        }
      }

      return result;

    } catch (Throwable t) {
      throw new ContentRepositoryException("Error querying index", t);
    }
  }

  /**
   * Clears the search index.
   * 
   * @throws IOException
   *           if clearing the index fails
   */
  public void clear() throws IOException {
    try {
      DeleteIndexResponse delete = nodeClient.admin().indices().delete(new DeleteIndexRequest()).actionGet();
      if (!delete.acknowledged())
        logger.error("Indices could not be deleted");
      createIndices();
    } catch (Throwable t) {
      throw new IOException("Cannot clear index", t);
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
  public boolean delete(ResourceURI uri) throws ContentRepositoryException {
    logger.debug("Removing element with id '{}' from searching index", uri.getIdentifier());

    String index = uri.getSite().getIdentifier();
    String type = uri.getType();
    String uid = uri.getUID();

    DeleteRequestBuilder deleteRequest = nodeClient.prepareDelete(index, type, uid);
    deleteRequest.setRefresh(true);
    DeleteResponse delete = deleteRequest.execute().actionGet();
    if (delete.notFound()) {
      logger.trace("Document {} to delete was not found", uri);
      return false;
    }

    // Adjust the version information
    updateVersions(uri);

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
  public boolean add(Resource<?> resource) throws ContentRepositoryException {
    logger.debug("Adding resource {} to search index", resource);
    addToIndex(resource);
    return true;
  }

  /**
   * Posts the updated resource to the search index.
   * 
   * @param resource
   *          the resource to update
   * @throws ContentRepositoryException
   *           if posting the updated resource to solr fails
   */
  public boolean update(Resource<?> resource) throws ContentRepositoryException {
    logger.debug("Updating resource {} in search index", resource);
    addToIndex(resource);
    return true;
  }

  /**
   * Adds the given resource to the search index.
   * 
   * @param resource
   *          the resource
   * @throws ContentRepositoryException
   *           if updating fails
   */
  private void addToIndex(Resource<?> resource)
      throws ContentRepositoryException {

    // Have the serializer create an input document
    ResourceURI uri = resource.getURI();
    String resourceType = uri.getType();
    ResourceSerializer<?, ?> serializer = resourceSerializer.getSerializerByType(resourceType);
    if (serializer == null)
      throw new ContentRepositoryException("Unable to create an input document for " + resource + ": no serializer found");

    // Add the resource to the index
    List<ResourceMetadata<?>> resourceMetadata = serializer.toMetadata(resource);
    ElasticSearchDocument doc = new ElasticSearchDocument(uri, resourceMetadata);
    try {
      update(doc);
    } catch (Throwable t) {
      throw new ContentRepositoryException("Cannot write resource " + resource + " to index", t);
    }

    // Adjust the version information
    updateVersions(uri);
  }

  /**
   * Aligns the information on alternate resource versions in the search index,
   * which is needed to support querying by preferred version.
   * 
   * @param uri
   *          uri of the resource to update
   * @throws ContentRepositoryException
   *           if updating fails
   */
  private void updateVersions(ResourceURI uri)
      throws ContentRepositoryException {

    String resourceType = uri.getType();
    ResourceSerializer<?, ?> serializer = resourceSerializer.getSerializerByType(resourceType);
    if (serializer == null)
      throw new ContentRepositoryException("Unable to create an input document for " + uri + ": no serializer found");

    // List all versions of the resource
    List<Resource<?>> resources = new ArrayList<Resource<?>>();
    Site site = uri.getSite();
    String id = uri.getIdentifier();
    SearchQuery q = new SearchQueryImpl(site).withIdentifier(id);
    for (SearchResultItem existingResource : getByQuery(q).getItems()) {
      List<ResourceMetadata<?>> resourceMetadata = ((ResourceSearchResultItem) existingResource).getMetadata();
      resources.add(serializer.toResource(site, resourceMetadata));
    }

    if (resources.size() == 0)
      return;

    // Add the alternate version information to each resource's metadata and
    // write it back to the search index (including the new one)
    List<ElasticSearchDocument> documents = new ArrayList<ElasticSearchDocument>();
    for (Resource<?> r : resources) {
      List<ResourceMetadata<?>> resourceMetadata = serializer.toMetadata(r);
      ResourceMetadataImpl<Long> alternateVersions = new ResourceMetadataImpl<Long>(ALTERNATE_VERSION);
      alternateVersions.setAddToFulltext(false);

      // Look for alternate versions
      long currentVersion = r.getURI().getVersion();
      for (Resource<?> v : resources) {
        long version = v.getURI().getVersion();
        if (version != currentVersion) {
          alternateVersions.addValue(version);
        }
      }

      // If alternate versions were found, add them
      if (alternateVersions.getValues().size() > 0) {
        resourceMetadata.add(alternateVersions);
      }

      // Write the resource to the index
      documents.add(new ElasticSearchDocument(r.getURI(), resourceMetadata));
    }

    // Now update all documents at once
    try {
      update(documents.toArray(new ElasticSearchDocument[documents.size()]));
    } catch (Throwable t) {
      throw new ContentRepositoryException("Cannot update versions of resource " + uri + " in index", t);
    }
  }

  /**
   * Posts the input document to the search index.
   * 
   * @param site
   *          the site that these documents belong to
   * @param documents
   *          the input documents
   * @return the query response
   * @throws ContentRepositoryException
   *           if posting to the index fails
   */
  protected BulkResponse update(ElasticSearchDocument... documents)
      throws ContentRepositoryException {

    BulkRequestBuilder bulkRequest = nodeClient.prepareBulk();
    for (ElasticSearchDocument doc : documents) {
      String index = doc.getSite().getIdentifier();
      String type = doc.getType();
      String uid = doc.getUID();
      bulkRequest.add(nodeClient.prepareIndex(index, type, uid).setSource(doc));
    }

    // Make sure the operations are searchable immediately
    bulkRequest.setRefresh(true);

    try {
      BulkResponse bulkResponse = bulkRequest.execute().actionGet();

      // Check for errors
      if (bulkResponse.hasFailures()) {
        for (BulkItemResponse item : bulkResponse.items()) {
          if (item.isFailed()) {
            logger.warn("Error updating {}: {}", item, item.failureMessage());
            throw new ContentRepositoryException(item.getFailureMessage());
          }
        }
      }

      return bulkResponse;
    } catch (Throwable t) {
      throw new ContentRepositoryException("Cannot update documents in index", t);
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
    ResourceSerializer<?, ?> serializer = resourceSerializer.getSerializerByType(resourceType);
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

    // Read the current resource and post the updated data to the search
    // index
    try {
      update(new ElasticSearchDocument(resource.getURI(), metadata));
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

    SuggestRequest request = null;
    // TODO: Implement
    // SuggestRequest request = new SuggestRequest(solrServer, dictionary,
    // onlyMorePopular, count, collate);
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
   * @param indexRoot
   *          the solr root directory
   * @throws Exception
   *           if loading or creating solr fails
   */
  private void init(Site site, File indexRoot) throws Exception {
    logger.debug("Setting up elastic search index at {}", indexRoot);

    // Prepare the configuration of the elastic search node
    Settings settings = loadSettings();

    // Configure and start the elastic search node
    NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().settings(settings);
    elasticSearch = nodeBuilder.build();
    elasticSearch.start();

    // Create indices and type definitions
    createIndices();
  }

  /**
   * Prepares elastic search to take Weblounge data.
   * 
   * @throws ContentRepositoryException
   *           if index and type creation fails
   * @throws IOException
   *           if loading of the type definitions fails
   */
  private void createIndices() throws ContentRepositoryException, IOException {

    // Create the client
    nodeClient = elasticSearch.client();

    // Make sure the site index exists
    if (!indexExists(site.getIdentifier())) {
      CreateIndexRequestBuilder siteIdxRequest = nodeClient.admin().indices().prepareCreate(site.getIdentifier());
      logger.info("Creating site index for '{}'", site.getIdentifier());
      CreateIndexResponse siteidxResponse = siteIdxRequest.execute().actionGet();
      if (!siteidxResponse.acknowledged()) {
        throw new ContentRepositoryException("Unable to create site index for '" + site.getIdentifier() + "'");
      }
    }

    // Store the correct mapping
    // TODO: Use resource serializers
    for (String type : new String[] {
        "version",
        "page",
        "file",
        "image",
    "movie" }) {
      PutMappingRequest siteMappingRequest = new PutMappingRequest(site.getIdentifier());
      siteMappingRequest.source(loadMapping(type));
      siteMappingRequest.type(type);
      PutMappingResponse siteMappingResponse = nodeClient.admin().indices().putMapping(siteMappingRequest).actionGet();
      if (!siteMappingResponse.acknowledged()) {
        throw new ContentRepositoryException("Unable to install '" + type + "' mapping for index '" + site.getIdentifier() + "'");
      }
    }

    // See if the index version exists and check if it matches. The request will
    // fail if there is no version index
    boolean versionIndexExists = false;
    GetRequestBuilder getRequestBuilder = nodeClient.prepareGet(site.getIdentifier(), VERSION_TYPE, ROOT_ID);
    try {
      GetResponse response = getRequestBuilder.execute().actionGet();
      if (response.field(VERSION) != null) {
        indexVersion = Integer.parseInt((String) response.field(VERSION).getValue());
        versionIndexExists = true;
        logger.debug("Search index version is {}", indexVersion);
      }
    } catch (ElasticSearchException e) {
      logger.debug("Version index has not been created");
    }

    // The index does not exist, let's create it
    if (!versionIndexExists) {
      indexVersion = VersionedContentRepositoryIndex.INDEX_VERSION;
      logger.debug("Creating version index for site '{}'", site.getIdentifier());
      IndexRequestBuilder requestBuilder = nodeClient.prepareIndex(site.getIdentifier(), VERSION_TYPE, ROOT_ID);
      logger.debug("Index version of site '{}' is {}", site.getIdentifier(), indexVersion);
      requestBuilder = requestBuilder.setSource(VERSION, Integer.toString(indexVersion));
      requestBuilder.execute().actionGet();
    }

  }

  /**
   * Loads the settings for the elastic search configuration. An initial attempt
   * is made to get the configuration from
   * <code>${weblounge.home}/etc/index/settings.yml</code>.
   * 
   * @return the elastic search settings
   * @throws IOException
   *           if the index cannot be created in case it is not there already
   */
  private Settings loadSettings() throws IOException {
    Settings settings = null;

    // Try to determine the default index location
    String webloungeHome = System.getProperty("weblounge.home");
    if (StringUtils.isBlank(webloungeHome)) {
      logger.warn("Unable to locate elasticsearch settings, weblounge.home not specified");
      webloungeHome = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
    }

    // Check if a local configuration file is present
    File configFile = new File(PathUtils.concat(webloungeHome, "/etc/index/settings.yml"));
    if (!configFile.isFile()) {
      logger.warn("Configuring elastic search node from the bundle resources");
      ElasticSearchUtils.createIndexConfigurationAt(new File(webloungeHome));
    }

    // Finally, try and load the index settings
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(configFile);
      settings = ImmutableSettings.settingsBuilder().loadFromStream(configFile.getName(), fis).build();
    } catch (FileNotFoundException e) {
      throw new IOException("Unable to load elasticsearch settings from " + configFile.getAbsolutePath());
    } finally {
      IOUtils.closeQuietly(fis);
    }

    return settings;
  }

  /**
   * Loads the mapping configuration. An initial attempt is made to get the
   * configuration from
   * <code>${weblounge.home}/etc/index/&lt;index name&gt;-mapping.json</code>.
   * If this file can't be found, the default mapping loaded from the classpath.
   * 
   * @param idxName
   *          name of the index
   * @return the string containing the configuration
   * @throws IOException
   *           if reading the index mapping fails
   */
  private String loadMapping(String idxName) throws IOException {
    String mapping = null;

    // First, check if a local configuration file is present
    String webloungeHome = System.getProperty("weblounge.home");
    if (StringUtils.isNotBlank(webloungeHome)) {
      File configFile = new File(PathUtils.concat(webloungeHome, "/etc/index/", idxName + "-mapping.json"));
      if (configFile.isFile()) {
        FileInputStream fis = null;
        try {
          fis = new FileInputStream(configFile);
          mapping = IOUtils.toString(fis);
        } catch (IOException e) {
          logger.warn("Unable to load index mapping from {}: {}", configFile.getAbsolutePath(), e.getMessage());
        } finally {
          IOUtils.closeQuietly(fis);
        }
      }
    } else {
      logger.warn("Unable to locate elasticsearch settings, weblounge.home not specified");
    }

    // If no local settings were found, read them from the bundle resources
    if (mapping == null) {
      InputStream is = null;
      String resourcePath = PathUtils.concat("/elasticsearch/", idxName + "-mapping.json");
      try {
        is = this.getClass().getResourceAsStream(resourcePath);
        if (is != null) {
          logger.debug("Reading elastic search index mapping '{}' from the bundle resource", idxName);
          mapping = IOUtils.toString(is);
        }
      } finally {
        IOUtils.closeQuietly(is);
      }
    }

    return mapping;
  }

  /**
   * Returns <code>true</code> if the given index exists.
   * 
   * @param indexName
   *          the index name
   * @return <code>true</code> if the index exists
   */
  private boolean indexExists(String indexName) {
    IndicesExistsRequest indexExistsRequest = new IndicesExistsRequest(indexName);
    return nodeClient.admin().indices().exists(indexExistsRequest).actionGet().exists();
  }

}
