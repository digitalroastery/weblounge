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

package ch.o2it.weblounge.contentrepository.impl.index.solr;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.WebloungeSolrConfig;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryRequestBase;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.BinaryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.servlet.SolrRequestParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The solr connection.
 */
public class SolrRequester {

  /** Logging facility */
  private static Logger logger = LoggerFactory.getLogger(SolrRequester.class);

  /** The solr core */
  private SolrCore core = null;

  /** The solr request parser */
  private SolrRequestParsers parser = null;

  /**
   * Creates a new solr connection that will live in the specified directory
   * where it expects to find the configuration. The index will be kept in the
   * <code>dataDir</code> directory.
   * 
   * @param solrDir
   *          The directory of the solr instance.
   * @param dataDir
   *          The directory of the solr index data.
   */
  public SolrRequester(String solrDir, String dataDir) {
    // Initialize SolrConfig
    SolrConfig config = null;
    try {
      config = new WebloungeSolrConfig(solrDir, SolrConfig.DEFAULT_CONF_FILE, null);
      solrDir = config.getResourceLoader().getInstanceDir();

      // Initialize SolrCore directly
      IndexSchema schema = new IndexSchema(config, solrDir + "/conf/schema.xml", null);
      core = new SolrCore(null, dataDir, config, schema, null);
      parser = new SolrRequestParsers(config);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Closes the solr connection.
   */
  public void destroy() {
    if (core != null)
      core.close();
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
   * Processes a solr request. If the request doesn't specify a handler, it
   * is processed using the default <code>select</code> handler.
   * 
   * @param request
   *          the request
   * @return the query result
   * @throws Exception
   *           if processing the query fails
   */
  public QueryResponse request(SolrQuery query) throws Exception {

    // Determine the handler to use
    String handlerName = query.getQueryType();
    if (StringUtils.isBlank(handlerName))
      handlerName = "search";
    
    // Make sure the handler has been configured
    SolrRequestHandler handler = core.getRequestHandler(handlerName);
    if (handler == null)
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "unknown handler: " + handlerName);

    SolrQueryResponse rsp = new SolrQueryResponse();
    SolrParams params = SolrParams.toSolrParams(query.toNamedList());
    SolrQueryRequest request = new SolrQueryRequestBase(core, params) { };
    core.execute(handler, request, rsp);

    if (rsp.getException() != null) {
      logger.warn(rsp.getException().toString());
      throw rsp.getException();
    }

    // Create the solrj response.
    QueryResponse qrsp = new QueryResponse();
    qrsp.setResponse(getParsedResponse(request, rsp));

    return qrsp;
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

    if (core == null) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Solr core is null. ");
    }

    SolrParams params = request.getParams();
    if (params == null) {
      params = new ModifiableSolrParams();
    }

    // Extract the handler from the path or params
    SolrRequestHandler handler = core.getRequestHandler(path);
    if (handler == null) {
      if ("/select".equals(path) || "/select/".equalsIgnoreCase(path)) {
        String qt = params.get(CommonParams.QT);
        handler = core.getRequestHandler(qt);
        if (handler == null) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "unknown handler: " + qt);
        }
      }
    }

    if (handler == null) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "unknown handler: " + path);
    }

    try {
      SolrQueryRequest req = parser.buildRequestFrom(core, params, request.getContentStreams());
      req.getContext().put("path", path);
      SolrQueryResponse rsp = new SolrQueryResponse();
      core.execute(handler, req, rsp);
      if (rsp.getException() != null) {
        throw new SolrServerException(rsp.getException());
      }

      // Now write it out
      NamedList<Object> normalized = getParsedResponse(req, rsp);
      req.close();
      QueryResponse res = new QueryResponse();
      res.setResponse(normalized);
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

  /**
   * The solr core, used by this connection.
   * 
   * @return The solr core.
   */
  public SolrCore getCore() {
    return this.core;
  }

}
