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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.test.util.TestSiteUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
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

  /** Page id */
  protected String pageId = null;

  /** Page path */
  protected String pagePath = null;

  /**
   * Creates a new instance of the content repository's page endpoint test.
   */
  public PagesEndpointTest() {
    super("Page Endpoint Test", WEBLOUNGE_ENDPOINT_TEST_GROUP);
  }

  /**
   * Runs this test on the instance running at
   * <code>http://127.0.0.1:8080</code>.
   * 
   * @throws Exception
   *           if the test fails
   */
  @Test
  public void execute() throws Exception {
    execute("http://127.0.0.1:8080");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of pages endpoint");

    pagePath = "/" + System.currentTimeMillis() + "/";

    // Create a new page
    testCreate(serverUrl);

    // Make sure the page can be retrieved both by its id and path
    testGetPageById(serverUrl, pageId);
    testGetPageByPath(serverUrl, pagePath);

    // Lock the page
    testLockPage(serverUrl, pageId);

    // Update the page
    testUpdatePage(serverUrl, pageId);

    // Move the page
    testMovePage(serverUrl, pageId);

    // Lock the page
    testUnlockPage(serverUrl, pageId);

    // Test page deletion
    testDeletePage(serverUrl, pageId);
  }

  /**
   * Tests the creation of a page.
   * 
   * @param serverUrl
   *          the server url
   * @throws Exception
   *           if page creation fails
   */
  private void testCreate(String serverUrl) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages");
    HttpPost createPageRequest = new HttpPost(requestUrl);
    String[][] params = new String[][] { { "path", pagePath } };
    logger.debug("Creating new page at {}", createPageRequest.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, createPageRequest, params);
      assertEquals(HttpServletResponse.SC_CREATED, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());

      // Extract the id of the new page
      assertNotNull(response.getHeaders("Location"));
      String locationHeader = response.getHeaders("Location")[0].getValue();
      assertTrue(locationHeader.startsWith(serverUrl));
      pageId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
      assertEquals("Identifier doesn't have correct length", 36, pageId.length());
      logger.debug("Id of the new page is {}", pageId);
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tries to retrieve a page by its id.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page identifier
   * @throws Exception
   *           if retrieval failed
   */
  private void testGetPageById(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages");
    HttpGet getPageRequest = new HttpGet(UrlUtils.concat(requestUrl, id));
    HttpClient httpClient = new DefaultHttpClient();
    Document pageXml = null;
    String eTagValue;
    logger.info("Requesting page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(id, XPathHelper.valueOf(pageXml, "/page/@id"));
      
      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    
    TestSiteUtils.testETagHeader(getPageRequest, eTagValue, logger);
    TestSiteUtils.testModifiedHeader(getPageRequest, logger);
  }

  /**
   * Tries to retrieve a page by its id.
   * 
   * @param serverUrl
   *          the server url
   * @param path
   *          the page path
   * @throws Exception
   *           if retrieval failed
   */
  private void testGetPageByPath(String serverUrl, String path)
      throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages");
    HttpGet getPageByPathRequest = new HttpGet(requestUrl);
    String[][] params = new String[][] { { "path", pagePath } };
    HttpClient httpClient = new DefaultHttpClient();
    Document pageByPathXml = null;
    logger.info("Requesting page by path at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageByPathRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageByPathXml = TestUtils.parseXMLResponse(response);
      assertNotNull(XPathHelper.valueOf(pageByPathXml, "/pages/page/@path"));
      assertEquals(path, XPathHelper.valueOf(pageByPathXml, "/pages/page/@path"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Locks the page.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page identifier
   * @throws Exception
   *           if updating failed
   */
  private void testLockPage(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages/");

    // Lock the page
    HttpPut lockPageRequest = new HttpPut(UrlUtils.concat(requestUrl, id, "lock"));
    HttpClient httpClient = new DefaultHttpClient();
    logger.info("Locking the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, lockPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Try to relock with a different user and make sure it fails
    HttpPut relockPageRequest = new HttpPut(UrlUtils.concat(requestUrl, id, "lock"));
    String[][] params = new String[][] { { "user", "amelie" } };
    httpClient = new DefaultHttpClient();
    logger.info("Trying to relock the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, relockPageRequest, params);
      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Locks the page.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page identifier
   * @throws Exception
   *           if updating failed
   */
  private void testUnlockPage(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages/");

    // Lock the page
    HttpDelete lockPageRequest = new HttpDelete(UrlUtils.concat(requestUrl, id, "lock"));
    HttpClient httpClient = new DefaultHttpClient();
    logger.info("Unlocking the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, lockPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure the update was successful
    HttpGet getUpdatedPageRequest = new HttpGet(UrlUtils.concat(requestUrl, id));
    httpClient = new DefaultHttpClient();
    logger.info("Requesting locked page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getUpdatedPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      Document pageXml = TestUtils.parseXMLResponse(response);
      assertNull(XPathHelper.valueOf(pageXml, "/page/head/locked"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Updates the creator field of the page.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page identifier
   * @throws Exception
   *           if updating failed
   */
  private void testUpdatePage(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages");

    HttpGet getPageRequest = new HttpGet(UrlUtils.concat(requestUrl, id));
    HttpClient httpClient = new DefaultHttpClient();
    Document pageXml = null;
    String creator = null;

    // Read what's currently stored
    logger.info("Requesting page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageXml = TestUtils.parseXMLResponse(response);
      creator = XPathHelper.valueOf(pageXml, "/page/head/created/user");
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Update the creator field
    HttpPut updatePageRequest = new HttpPut(UrlUtils.concat(requestUrl, id));
    String updatedCreator = creator + " (updated)";
    NodeList creatorElements = pageXml.getElementsByTagName("created");
    creatorElements.item(0).getFirstChild().setTextContent(updatedCreator);
    String[][] params = new String[][] { { "content", serializeDoc(pageXml) } };
    httpClient = new DefaultHttpClient();
    logger.info("Updating page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, updatePageRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure the update was successful
    HttpGet getUpdatedPageRequest = new HttpGet(UrlUtils.concat(requestUrl, id));
    httpClient = new DefaultHttpClient();
    logger.info("Requesting updated page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getUpdatedPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(pageId, XPathHelper.valueOf(pageXml, "/page/@id"));
      assertEquals(updatedCreator, XPathHelper.valueOf(pageXml, "/page/head/created/user"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Moves the page to a different url and back.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page id
   * @throws Exception
   *           if moving fails
   */
  private void testMovePage(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages");

    HttpGet getPageRequest = new HttpGet(UrlUtils.concat(requestUrl, id));
    HttpClient httpClient = new DefaultHttpClient();
    Document pageXml = null;
    String oldPath = null;

    // Read what's currently stored
    logger.info("Requesting page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageXml = TestUtils.parseXMLResponse(response);
      assertNotNull(XPathHelper.valueOf(pageXml, "/page"));
      oldPath = XPathHelper.valueOf(pageXml, "/page/@path");
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Move the page
    HttpPut movePageRequest = new HttpPut(UrlUtils.concat(requestUrl, pageId));
    String newPath = PathUtils.concat(oldPath, "/new/");
    pageXml.getFirstChild().getAttributes().getNamedItem("path").setNodeValue(newPath);
    String[][] params = new String[][] { { "content", serializeDoc(pageXml) } };
    httpClient = new DefaultHttpClient();
    logger.info("Moving page from {} to {}", oldPath, newPath);
    try {
      HttpResponse response = TestUtils.request(httpClient, movePageRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure the page is gone from the original path but can be found on
    // the new one
    HttpGet getPageByPathRequest = new HttpGet(requestUrl);
    params = new String[][] { { "path", newPath } };
    httpClient = new DefaultHttpClient();
    logger.info("Requesting page by path at {}", newPath);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageByPathRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageXml = TestUtils.parseXMLResponse(response);
      assertNotNull(XPathHelper.select(pageXml, "/pages/page"));
      assertEquals(id, XPathHelper.valueOf(pageXml, "/pages/page/@id"));
      assertEquals(newPath, XPathHelper.valueOf(pageXml, "/pages/page/@path"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Check that the page is gone from the old path
    getPageByPathRequest = new HttpGet(requestUrl);
    params = new String[][] { { "path", oldPath } };
    httpClient = new DefaultHttpClient();
    logger.info("Requesting page by path at {}", oldPath);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageByPathRequest, params);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Move the page back
    movePageRequest = new HttpPut(UrlUtils.concat(requestUrl, id));
    pageXml.getFirstChild().getAttributes().getNamedItem("path").setNodeValue(oldPath);
    params = new String[][] { { "content", serializeDoc(pageXml) } };
    httpClient = new DefaultHttpClient();
    logger.info("Moving page back from {} to {}", newPath, oldPath);
    try {
      HttpResponse response = TestUtils.request(httpClient, movePageRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Deletes the page.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page id
   * @throws Exception
   *           if deletion failed
   */
  private void testDeletePage(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages");
    HttpDelete deleteRequest = new HttpDelete(UrlUtils.concat(requestUrl, pageId));
    HttpClient httpClient = new DefaultHttpClient();

    // Delete the page
    logger.info("Sending delete request to {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, deleteRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure it's gone
    logger.info("Make sure page {} is gone", id);
    HttpGet getPageRequest = new HttpGet(UrlUtils.concat(requestUrl, id));
    httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
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
    } catch (Throwable th) {
      throw new IllegalStateException("Unable to serialize dom", th);
    }
    return outText.toString();
  }

}
