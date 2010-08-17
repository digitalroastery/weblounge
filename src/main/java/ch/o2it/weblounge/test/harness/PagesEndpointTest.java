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
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Integration test to test the content repository's <code>restful</code> page
 * api.
 */
public class PagesEndpointTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(PagesEndpointTest.class);

  /**
   * Creates a new instance of the content repository's page endpoint test.
   */
  public PagesEndpointTest() {
    super("Page Endpoint Test");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of pages endpoint");

    // Include the mountpoint
    // TODO: Make this dynamic
    // serverUrl = UrlSupport.concat(serverUrl, "weblounge");
    String requestUrl = UrlSupport.concat(serverUrl, "system/weblounge/pages");
    String testPath = "/testpath/";

    // Prepare the request
    HttpPost createPageRequest = new HttpPost(requestUrl);
    String[][] params = new String[][] { { "path", testPath } };
    logger.debug("Creating new page at {}", createPageRequest.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    String pageId = null;
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, createPageRequest, params);
      assertEquals(HttpServletResponse.SC_CREATED, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());

      // Extract the id of the new page
      assertNotNull(response.getHeaders("Location"));
      String locationHeader = response.getHeaders("Location")[0].getValue();
      assertTrue(locationHeader.startsWith(serverUrl));
      pageId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
      assertNotNull(pageId);
      logger.debug("Id of the new page is {}", pageId);
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Check if a page has been created
    HttpGet getPageRequest = new HttpGet(UrlSupport.concat(requestUrl, pageId));
    httpClient = new DefaultHttpClient();
    Document pageXml = null;
    String creator = null;
    logger.info("Requesting page at {}", requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, getPageRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageXml = TestSiteUtils.parseXMLResponse(response);
      assertEquals(pageId, XPathHelper.valueOf(pageXml, "/page/@id"));
      assertEquals(testPath, XPathHelper.valueOf(pageXml, "/page/@path"));
      creator = XPathHelper.valueOf(pageXml, "/page/head/created/user");
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Update the page
    HttpPut updatePageRequest = new HttpPut(UrlSupport.concat(requestUrl, pageId));
    String updatedCreator = creator + " (updated)";
    NodeList creatorElements = pageXml.getElementsByTagName("created");
    creatorElements.item(0).getFirstChild().setTextContent(updatedCreator);
    params = new String[][] { { "page", serializeDoc(pageXml) } };
    httpClient = new DefaultHttpClient();
    logger.info("Updating page at {}", requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, updatePageRequest, params);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure the update was successful
    HttpGet getUpdatedPageRequest = new HttpGet(UrlSupport.concat(requestUrl, pageId));
    httpClient = new DefaultHttpClient();
    logger.info("Requesting updated page at {}", requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, getUpdatedPageRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageXml = TestSiteUtils.parseXMLResponse(response);
      assertEquals(pageId, XPathHelper.valueOf(pageXml, "/page/@id"));
      assertEquals(testPath, XPathHelper.valueOf(pageXml, "/page/@path"));
      assertEquals(updatedCreator, XPathHelper.valueOf(pageXml, "/page/head/created/user"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Delete the page
    HttpDelete deletePageRequest = new HttpDelete(UrlSupport.concat(requestUrl, pageId));
    httpClient = new DefaultHttpClient();
    logger.info("Sending delete request to {}", requestUrl);
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, deletePageRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure it's gone
    logger.info("Sending requests to {}", requestUrl);
    httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, updatePageRequest, null);
      Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Returns the serialized version of the given dom node.
   * 
   * @param doc
   *          the xml document
   * @return the serialized version
   */
  private static String serializeDoc(Node doc) {
    StringWriter outText = new StringWriter();
    StreamResult sr = new StreamResult(outText);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = null;
    try {
      t = tf.newTransformer();
      t.transform(new DOMSource(doc), sr);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to serialize dom", e);
    }
    return outText.toString();
  }

}
