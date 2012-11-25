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

package ch.entwine.weblounge.test.harness.rest;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test to test the content repository's <code>restful</code> search
 * api.
 */
public class SearchEndpointTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(SearchEndpointTest.class);

  /**
   * Creates a new instance of the content repository's search endpoint test.
   */
  public SearchEndpointTest() {
    super("Search Endpoint Test", WEBLOUNGE_ENDPOINT_TEST_GROUP);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  @Override
  public void execute(String serverUrl) throws Exception {
    searchExisting(serverUrl);
    searchNonExisting(serverUrl);
  }

  /**
   * Performs a search request for existing content.
   * 
   * @param serverUrl
   *          the server url
   * @throws Exception
   *           if the test fails
   */
  private void searchExisting(String serverUrl) throws Exception {
    logger.info("Preparing test of search rest api");

    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/search");

    // Prepare the request
    logger.info("Searching for a page");
    HttpGet searchRequest = new HttpGet(requestUrl);

    // Send the request. The response should be a 400 (bad request)
    logger.debug("Sending empty get request to {}", searchRequest.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, searchRequest, null);
      assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Check for search terms that don't yield a result
    String searchTerms = "xyz";
    httpClient = new DefaultHttpClient();
    searchRequest = new HttpGet(UrlUtils.concat(requestUrl, searchTerms));
    logger.info("Sending search request for '{}' to {}", searchTerms, requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, searchRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      Document xml = TestUtils.parseXMLResponse(response);
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@documents"));
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@hits"));
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@offset"));
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@page"));
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@pagesize"));
      assertEquals("0", XPathHelper.valueOf(xml, "count(/searchresult/result)"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Check for search terms that should yield a result
    searchTerms = "Friedrich Nietzsche Suchresultat";
    httpClient = new DefaultHttpClient();
    searchRequest = new HttpGet(UrlUtils.concat(requestUrl, URLEncoder.encode(searchTerms, "utf-8")));
    String[][] params = new String[][] { { "limit", "5" } };
    logger.info("Sending search request for '{}' to {}", searchTerms, requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, searchRequest, params);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      Document xml = TestUtils.parseXMLResponse(response);
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@documents"));
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@hits"));
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@offset"));
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@page"));
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@pagesize"));
      assertEquals("1", XPathHelper.valueOf(xml, "count(/searchresult/result)"));
      assertEquals("4bb19980-8f98-4873-a813-000000000006", XPathHelper.valueOf(xml, "/searchresult/result/id"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Check for exact matches on subjects
    String[] exactMatches = { "Search Topic A", "Search Topic B" };
    for (String searchTerm : exactMatches) {

      // Full match
      httpClient = new DefaultHttpClient();
      searchRequest = new HttpGet(UrlUtils.concat(requestUrl, URLEncoder.encode(searchTerms, "utf-8")));
      params = new String[][] { { "limit", "5" } };
      logger.info("Sending search request for exact match of '{}' to {}", searchTerm, requestUrl);
      try {
        HttpResponse response = TestUtils.request(httpClient, searchRequest, params);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        Document xml = TestUtils.parseXMLResponse(response);
        int documentCount = Integer.parseInt(XPathHelper.valueOf(xml, "/searchresult/@documents"));
        int hitCount = Integer.parseInt(XPathHelper.valueOf(xml, "/searchresult/@hits"));
        Assert.assertTrue(documentCount == 1);
        Assert.assertTrue(hitCount == 1);
      } finally {
        httpClient.getConnectionManager().shutdown();
      }

    }

    // Check for partial matches in fields that should be supporting partial
    // matches
    String[] partialSearchTerms = { "Kurzer Seitentitel", // German title
        "Il titre de la page", // French title
        "Lange Beschreibung", // German description
        "Déscription longue", // French description
        "Hans Muster", // creator, publisher
        "Amélie Poulard", // modifier
        "Friedrich Nietzsche Suchresultat", // element text
        "Ein amüsanter Titel", // German element text
        "Un titre joyeux" // French element text
    };

    for (String searchTerm : partialSearchTerms) {
      int fullMatchDocumentCount = 0;
      int fullMatchHitCount = 0;

      // Full match
      httpClient = new DefaultHttpClient();
      searchRequest = new HttpGet(UrlUtils.concat(requestUrl, URLEncoder.encode(searchTerms, "utf-8")));
      params = new String[][] { { "limit", "5" } };
      logger.info("Sending search request for full match of '{}' to {}", searchTerm, requestUrl);
      try {
        HttpResponse response = TestUtils.request(httpClient, searchRequest, params);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        Document xml = TestUtils.parseXMLResponse(response);
        fullMatchDocumentCount = Integer.parseInt(XPathHelper.valueOf(xml, "/searchresult/@documents"));
        fullMatchHitCount = Integer.parseInt(XPathHelper.valueOf(xml, "/searchresult/@hits"));
        Assert.assertTrue(fullMatchDocumentCount >= 1);
        Assert.assertTrue(fullMatchHitCount >= fullMatchDocumentCount);
      } finally {
        httpClient.getConnectionManager().shutdown();
      }

      // Full match lowercase
      httpClient = new DefaultHttpClient();
      String lowerCaseSearchTerm = searchTerm.toLowerCase();
      searchRequest = new HttpGet(UrlUtils.concat(requestUrl, URLEncoder.encode(lowerCaseSearchTerm, "utf-8")));
      params = new String[][] { { "limit", "5" } };
      logger.info("Sending search request for lowercase match of '{}' to {}", searchTerm, requestUrl);
      try {
        HttpResponse response = TestUtils.request(httpClient, searchRequest, params);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        Document xml = TestUtils.parseXMLResponse(response);
        int documentCount = Integer.parseInt(XPathHelper.valueOf(xml, "/searchresult/@documents"));
        int hitCount = Integer.parseInt(XPathHelper.valueOf(xml, "/searchresult/@hits"));
        Assert.assertTrue(documentCount >= fullMatchDocumentCount);
        Assert.assertTrue(hitCount >= fullMatchHitCount);
      } finally {
        httpClient.getConnectionManager().shutdown();
      }

      // Partial match
      for (String partialSearchTerm : StringUtils.split(searchTerm)) {
        httpClient = new DefaultHttpClient();
        searchRequest = new HttpGet(UrlUtils.concat(requestUrl, URLEncoder.encode(partialSearchTerm, "utf-8")));
        params = new String[][] { { "limit", "5" } };
        logger.info("Sending search request for partial match of '{}' to {}", searchTerm, requestUrl);
        try {
          HttpResponse response = TestUtils.request(httpClient, searchRequest, params);
          Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
          Document xml = TestUtils.parseXMLResponse(response);
          int documentCount = Integer.parseInt(XPathHelper.valueOf(xml, "/searchresult/@documents"));
          int hitCount = Integer.parseInt(XPathHelper.valueOf(xml, "/searchresult/@hits"));
          Assert.assertTrue(documentCount >= fullMatchDocumentCount);
          Assert.assertTrue(hitCount >= fullMatchHitCount);
        } finally {
          httpClient.getConnectionManager().shutdown();
        }
      }
    }

  }

  /**
   * Performs a search request for non-existing content.
   * 
   * @param serverUrl
   *          the server url
   * @throws Exception
   *           if the test fails
   */
  private void searchNonExisting(String serverUrl) throws Exception {
    logger.info("Preparing test of search rest api");

    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/search");

    // Prepare the request
    logger.info("Searching for a page");
    HttpGet searchRequest = new HttpGet(requestUrl);

    // Send the request. The response should be a 400 (bad request)
    logger.debug("Sending empty get request to {}", searchRequest.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, searchRequest, null);
      assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Check for search terms that don't yield a result
    String searchTerms = "xyz";
    httpClient = new DefaultHttpClient();
    searchRequest = new HttpGet(UrlUtils.concat(requestUrl, searchTerms));
    logger.info("Sending search request for '{}' to {}", searchTerms, requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, searchRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      Document xml = TestUtils.parseXMLResponse(response);
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@documents"));
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@hits"));
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@offset"));
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@page"));
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@pagesize"));
      assertEquals("0", XPathHelper.valueOf(xml, "count(/searchresult/result)"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}
