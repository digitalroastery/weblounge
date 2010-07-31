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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test to test the content repository's <code>restful</code> api.
 */
public class ContentRepositoryEndpointTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(ContentRepositoryEndpointTest.class);

  /**
   * Creates a new instance of the content repository's endpoint test.
   */
  public ContentRepositoryEndpointTest() {
    super("Content Repository Endpoint Test");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of content repository rest api");

    // Include the mountpoint
    // TODO: Make this dynamic
    //serverUrl = UrlSupport.concat(serverUrl, "weblounge");
    String requestUrl = UrlSupport.concat(serverUrl, "system/pages");
    String testPath = "/testpath";
    
    // Prepare the request
    logger.info("Creating a new page");
    HttpPost createPageRequest = new HttpPost(requestUrl);
    String[][] params = new String[][] {{"path", testPath}};
    
    // Send the request and examine the response
    logger.debug("Sending post request to {}", createPageRequest.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    String pageId = null;
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, createPageRequest, params);
      assertEquals(HttpServletResponse.SC_CREATED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
      
      // Extract the id of the new page
      assertNotNull(response.getHeaders("Location"));
      String locationHeader = response.getHeaders("Location")[0].getValue();
      assertTrue(locationHeader.startsWith(serverUrl));
      pageId = locationHeader.substring(locationHeader.lastIndexOf("/"));
      assertNotNull(pageId);
      logger.debug("Id of the new page is {}", pageId);
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    
    // Check if a page has been created
    HttpGet getPageRequest = new HttpGet(UrlSupport.concat(requestUrl, pageId));
    logger.info("Sending get request to {}", requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, getPageRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      Document xml = TestSiteUtils.parseXMLResponse(response);
      assertEquals(pageId, XPathHelper.valueOf(xml, "/page/@id"));    
      assertEquals(testPath, XPathHelper.valueOf(xml, "/page/@path"));    
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    
    // Delete the page
    // Check if a page has been created
    HttpDelete deletePageRequest = new HttpDelete(UrlSupport.concat(requestUrl, pageId));
    logger.info("Sending delete request to {}", requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, deletePageRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    
    // Make sure it's gone
    logger.info("Sending requests to {}", requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, getPageRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    
  }

}
