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

package ch.o2it.weblounge.test.harness;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.test.util.TestSiteUtils;

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
    super("Search Endpoint Test");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of search rest api");

    // Include the mountpoint
    // TODO: Make this dynamic
    //serverUrl = UrlSupport.concat(serverUrl, "weblounge");
    String requestUrl = UrlSupport.concat(serverUrl, "system/weblounge/search");
    
    // Prepare the request
    logger.info("Searching for a page");
    HttpGet searchRequest = new HttpGet(requestUrl);
    
    // Send the request. The response should be a 400 (bad request)
    logger.debug("Sending empty get request to {}", searchRequest.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, searchRequest, null);
      assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    
    // Check for search terms that don't yield a result
    String searchTerms = "xyz";
    httpClient = new DefaultHttpClient();
    searchRequest = new HttpGet(UrlSupport.concat(requestUrl, searchTerms));
    logger.info("Sending search request for '{}' to {}", searchTerms, requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, searchRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      Document xml = TestSiteUtils.parseXMLResponse(response);
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
    searchRequest = new HttpGet(UrlSupport.concat(requestUrl, URLEncoder.encode(searchTerms, "UTF-8")));
    String[][] params = new String[][] {{"limit", "5"}};
    logger.info("Sending search request for '{}' to {}", searchTerms, requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, searchRequest, params);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      Document xml = TestSiteUtils.parseXMLResponse(response);
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@documents"));
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@hits"));
      assertEquals("0", XPathHelper.valueOf(xml, "/searchresult/@offset"));
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@page"));
      assertEquals("1", XPathHelper.valueOf(xml, "/searchresult/@pagesize"));
      assertEquals("1", XPathHelper.valueOf(xml, "count(/searchresult/result)"));
      assertEquals("4bb19980-8f98-4873-a813-71b6dfab22ad", XPathHelper.valueOf(xml, "/searchresult/result/id"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

}
