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

package ch.entwine.weblounge.contentrepository.impl.index.solr;

import ch.entwine.weblounge.common.url.PathUtils;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.WebloungeSolrConfig;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.BinaryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The solr connection.
 */
public class Solr {

  /** Logging facility */
  private static Logger logger = LoggerFactory.getLogger(Solr.class);

  /** Solr core container */
  private CoreContainer container = null;

  /** The solr server */
  private EmbeddedSolrServer solrServer = null;

  /**
   * Creates a new solr connection that will live in the specified directory
   * where it expects to find the configuration. The index will be kept in the
   * <code>dataDir</code> directory.
   * 
   * @param solrDir
   *          The directory of the solr instance.
   */
  public Solr(String solrDir, String dataDir) {
    System.setProperty("solr.solr.home", solrDir);

    logger.debug("Loading solr at {}", solrDir);

    try {
      SolrConfig config = new WebloungeSolrConfig(solrDir, SolrConfig.DEFAULT_CONF_FILE, null);
      solrDir = config.getResourceLoader().getInstanceDir();

      container = new CoreContainer(config.getResourceLoader());

      // Initialize SolrCore directly
      String coreName = "weblounge";
      CoreDescriptor coreDescriptor = new CoreDescriptor(container, coreName, solrDir);
      IndexSchema schema = new IndexSchema(config, PathUtils.concat(solrDir, "conf", "schema.xml"), null);

      SolrCore core = new SolrCore(coreName, dataDir, config, schema, coreDescriptor);
      container.register(core, false);
      
      solrServer = new EmbeddedSolrServer(container, coreName);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Closes the solr connection.
   */
  public void shutdown() {
    container.shutdown();
  }

  /**
   * Process a request to query the solr core.
   * 
   * @param query
   *          The solr query as string.
   * @return The query response.
   * @throws Exception
   */
  public QueryResponse request(String query) throws Exception {
    return request(new SolrQuery(query));
  }

  /**
   * Processes a solr request. If the request doesn't specify a handler, it is
   * processed using the default <code>select</code> handler.
   * 
   * @param request
   *          the request
   * @return the query result
   * @throws Exception
   *           if processing the query fails
   */
  public QueryResponse request(SolrQuery query) throws Exception {
    QueryResponse response = solrServer.query(query);
    return response;
  }

  /**
   * Process a request to query the solr core.
   * 
   * @param request
   *          The solr request
   * @return The query response.
   * @throws Exception
   */
  public QueryResponse update(UpdateRequest request) throws Exception {
    String path = request.getPath();
    if (path == null || !path.startsWith("/")) {
      path = "/update";
    }

    SolrParams params = request.getParams();
    if (params == null) {
      params = new ModifiableSolrParams();
    }

    // Remove fields that may have been added through search metadata
    // transformations
    if (request.getDocuments() != null) {
      for (SolrInputDocument doc : request.getDocuments()) {
        doc.removeField("score");
      }
    }

    try {

      // Now write it out
      NamedList<Object> normalized = solrServer.request(request);
      QueryResponse res = new QueryResponse(normalized, solrServer);
      return res;

    } catch (IOException iox) {
      throw iox;
    } catch (Throwable t) {
      throw new SolrServerException(t);
    }

  }

  /**
   * Parse the solr response to named list (need to create solrj query respond).
   * 
   * @param req
   *          The request.
   * @param rsp
   *          The response.
   * @return The named list.
   */
  public NamedList<Object> getParsedResponse(SolrQueryRequest req,
      SolrQueryResponse rsp) {
    try {
      BinaryResponseWriter writer = new BinaryResponseWriter();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      writer.write(bos, req, rsp);
      BinaryResponseParser parser = new BinaryResponseParser();
      return parser.processResponse(new ByteArrayInputStream(bos.toByteArray()), "utf-8");
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

}
