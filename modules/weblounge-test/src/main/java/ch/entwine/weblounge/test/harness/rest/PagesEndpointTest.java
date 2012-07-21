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

import ch.entwine.weblounge.common.content.Resource;
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
  @Override
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of pages endpoint");

    pagePath = "/" + System.currentTimeMillis() + "/";

    // Create a new page
    testCreate(serverUrl);

    // Test retrieving the work version of the page through the rest endpoint
    testGetPageById(serverUrl, Resource.WORK, pageId);

    // Lock the page
    testLockPage(serverUrl, pageId);

    // Update the page
    testUpdatePage(serverUrl, pageId);

    // Lock the page
    testUnlockPage(serverUrl, pageId);

    // Publish the page and test retrieval using the page request handler
    testPublishPage(serverUrl, pageId, pagePath);

    // Test retrieving the page through the rest endpoint
    testGetPageById(serverUrl, Resource.LIVE, pageId);
    testGetPageByPath(serverUrl, pagePath);

    // Move the page
    testMovePage(serverUrl, pageId);

    // Test referrer
    testGetReferrer(serverUrl, pageId);

    // Unpublish the page
    testUnpublishPage(serverUrl, pageId);

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

    // Make sure the page is not yet publicly reachable
    String requestByIdUrl = UrlUtils.concat(serverUrl, "/weblounge-pages/", pageId);
    HttpGet getPageRequest = new HttpGet(requestByIdUrl);
    httpClient = new DefaultHttpClient();
    logger.info("Requesting published page at {}", requestByIdUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Tries to retrieve a page by its id.
   * 
   * @param serverUrl
   *          the server url
   * @param version
   *          the page version
   * @param id
   *          the page identifier
   * @throws Exception
   *           if retrieval failed
   */
  private void testGetPageById(String serverUrl, long version, String id)
      throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages", id);
    HttpGet getPageRequest = new HttpGet(requestUrl);
    String[][] params = new String[][] { { "version", Long.toString(version) } };
    HttpClient httpClient = new DefaultHttpClient();
    Document pageXml = null;
    String eTagValue;
    String modifiedValue;
    logger.info("Requesting page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(id, XPathHelper.valueOf(pageXml, "/page/@id"));

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("ETag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();

      Header modifiedHeader = response.getFirstHeader("Last-Modified");
      assertNotNull(modifiedHeader);
      assertNotNull(modifiedHeader.getValue());
      modifiedValue = modifiedHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    TestSiteUtils.testETagHeader(getPageRequest, eTagValue, logger, params);

    httpClient = new DefaultHttpClient();
    try {
      getPageRequest.removeHeaders("If-None-Match");
      getPageRequest.setHeader("If-Modified-Since", modifiedValue);
      logger.info("Sending 'If-Modified-Since' request to {}", requestUrl);
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, params);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    httpClient = new DefaultHttpClient();
    try {
      getPageRequest.removeHeaders("If-None-Match");
      getPageRequest.setHeader("If-Modified-Since", "Wed, 10 Feb 1999 21:06:40 GMT");
      logger.info("Sending 'If-Modified-Since' request to {}", requestUrl);
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertNotNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
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
   * Tests listing of referrer.
   * 
   * @param serverUrl
   *          the server url
   * @param pageId
   *          the page identifier
   * @throws Exception
   *           if page creation fails
   */
  private void testGetReferrer(String serverUrl, String pageId)
      throws Exception {

    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages");
    HttpPost createPageRequest = new HttpPost(requestUrl);
    String referringPageId = null;

    // Create a new page and add a pagelet with a link to the original page
    logger.debug("Creating new page");
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, createPageRequest, null);
      assertEquals(HttpServletResponse.SC_CREATED, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());

      // Extract the id of the new page
      assertNotNull(response.getHeaders("Location"));
      String locationHeader = response.getHeaders("Location")[0].getValue();
      assertTrue(locationHeader.startsWith(serverUrl));
      referringPageId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
      logger.debug("Id of the new page is {}", referringPageId);

      httpClient.getConnectionManager().shutdown();
      httpClient = new DefaultHttpClient();

      // Load the created page
      String referringPageUrl = UrlUtils.concat(requestUrl, referringPageId, "?version=1");
      HttpGet getPageRequest = new HttpGet(referringPageUrl);
      response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      String pageXml = TestUtils.parseTextResponse(response);

      httpClient.getConnectionManager().shutdown();
      httpClient = new DefaultHttpClient();

      // Lock the page
      HttpPut lockPageRequest = new HttpPut(UrlUtils.concat(requestUrl, referringPageId, "lock"));
      logger.info("Locking the page at {}", requestUrl);
      response = TestUtils.request(httpClient, lockPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      httpClient.getConnectionManager().shutdown();
      httpClient = new DefaultHttpClient();

      // Add a pagelet and post the updated page
      String pageletSecurity = "<security><owner><user id=\"hans\" realm=\"testland\"><![CDATA[Hans Muster]]></user></owner></security>";
      String pageletCreation = "<created><user id=\"hans\" realm=\"testland\"><![CDATA[Hans Muster]]></user><date>2009-01-07T19:05:41Z</date></created>";
      String pageletPublication = "<published><user id=\"hans\" realm=\"testland\"><![CDATA[Hans Muster]]></user><from>2009-01-07T19:05:41Z</from></published>";
      String pageletProperties = "<properties><property id=\"resourceid\"><![CDATA[" + pageId + "]]></property></properties>";
      pageXml = pageXml.replaceAll("<body/>", "<body><composer id=\"main\"><pagelet module=\"navigation\" id=\"link\">" + pageletSecurity + pageletCreation + pageletPublication + pageletProperties + "</pagelet></composer></body>");
      HttpPut updatePageRequest = new HttpPut(referringPageUrl);
      String[][] params = new String[][] { { "content", pageXml } };
      logger.info("Updating page at {}", referringPageUrl);
      response = TestUtils.request(httpClient, updatePageRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Publish it
      HttpPut publishPageRequest = new HttpPut(UrlUtils.concat(requestUrl, referringPageId, "publish"));
      httpClient = new DefaultHttpClient();
      logger.info("Publishing the page at {}", requestUrl);
      response = TestUtils.request(httpClient, publishPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure the page is being issued as a referrer of the original page
    String requestReferrerUrl = UrlUtils.concat(requestUrl, pageId, "referrer");
    HttpGet getPageRequest = new HttpGet(requestReferrerUrl);
    logger.info("Requesting referrer of {} at {}", pageId, requestReferrerUrl);
    // Wait as long as 10s for the asynchronous processing
    boolean success = false;
    for (int i = 0; i < 5; i++) {
      httpClient = new DefaultHttpClient();
      try {
        HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
        assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        Document referrer = TestUtils.parseXMLResponse(response);
        assertEquals(1, XPathHelper.selectList(referrer, "/pages/page").getLength());
        assertEquals(referringPageId, XPathHelper.valueOf(referrer, "/pages/page/@id"));
        success = true;
        break;
      } catch (AssertionError a) {
        logger.info("Waiting, then retrying due to asynchronous processing");
        Thread.sleep(2000);
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }
    assertTrue(success);

    // Delete the new page
    HttpDelete deleteRequest = new HttpDelete(UrlUtils.concat(requestUrl, referringPageId));
    httpClient = new DefaultHttpClient();

    // Delete the page
    logger.info("Sending delete request to {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, deleteRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
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
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages/", id);

    // Lock the page
    HttpPut lockRequest = new HttpPut(UrlUtils.concat(requestUrl, "lock"));
    HttpClient httpClient = new DefaultHttpClient();
    logger.info("Locking the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, lockRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
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
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages/", id);

    // Unlock the page
    HttpDelete unlockRequest = new HttpDelete(UrlUtils.concat(requestUrl, "lock"));
    HttpClient httpClient = new DefaultHttpClient();
    logger.info("Unlocking the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, unlockRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure the update was successful
    HttpGet getUpdatedPageRequest = new HttpGet(requestUrl);
    httpClient = new DefaultHttpClient();
    String[][] params = null;
    params = new String[][] { { "version", Long.toString(Resource.WORK) } };
    logger.info("Requesting locked page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getUpdatedPageRequest, params);
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
    String[][] params = null;
    params = new String[][] { { "version", Long.toString(Resource.WORK) } };
    Document pageXml = null;
    String creator = null;

    // Read what's currently stored
    logger.info("Requesting page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, params);
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
    params = new String[][] { { "content", serializeDoc(pageXml) } };
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
    params = new String[][] { { "version", Long.toString(Resource.WORK) } };
    logger.info("Requesting updated page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getUpdatedPageRequest, params);
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

    // Lock the page
    HttpPut lockPageRequest = new HttpPut(UrlUtils.concat(requestUrl, id, "lock"));
    httpClient = new DefaultHttpClient();
    logger.info("Locking the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, lockPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Move the page
    HttpPut movePageRequest = new HttpPut(UrlUtils.concat(requestUrl, id));
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

    // Unlock the page
    HttpDelete unlockRequest = new HttpDelete(UrlUtils.concat(requestUrl, id, "lock"));
    httpClient = new DefaultHttpClient();
    logger.info("Unlocking the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, unlockRequest, null);
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
      Document pagesXml = TestUtils.parseXMLResponse(response);
      assertNotNull(XPathHelper.valueOf(pageXml, "/pages/page[0]"));
      assertEquals(id, XPathHelper.valueOf(pagesXml, "/pages/page[0]/@id"));
      assertEquals(newPath, XPathHelper.valueOf(pagesXml, "/pages/page[0]/@path"));
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
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertNull(XPathHelper.select(TestUtils.parseXMLResponse(response), "/pages/page"));
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
   * Publishes the page.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page identifier
   * @param path
   *          the page path
   * @throws Exception
   *           if publishing failed
   */
  private void testPublishPage(String serverUrl, String id, String path)
      throws Exception {
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

    // Publish it
    HttpPut publishPageRequest = new HttpPut(UrlUtils.concat(requestUrl, id, "publish"));
    httpClient = new DefaultHttpClient();
    logger.info("Publishing the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, publishPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Unlock the page
    HttpDelete unlockRequest = new HttpDelete(UrlUtils.concat(requestUrl, id, "lock"));
    httpClient = new DefaultHttpClient();
    logger.info("Unlocking the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, unlockRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Make sure the published page is reachable

    // Wait a second, since when publishing the page, we cut off the millisecond
    // portion of the publishing start date
    Thread.sleep(1000);

    // Get the page using its id
    String requestByIdUrl = UrlUtils.concat(serverUrl, "/weblounge-pages/", id);
    HttpGet getPageRequest = new HttpGet(requestByIdUrl);
    httpClient = new DefaultHttpClient();
    logger.info("Requesting published page at {}", requestByIdUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Get the page using its path
    String requestByPathUrl = UrlUtils.concat(serverUrl, path);
    getPageRequest = new HttpGet(requestByPathUrl);
    httpClient = new DefaultHttpClient();
    logger.info("Requesting published page at {}", requestByPathUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Publishes the page.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page identifier
   * @throws Exception
   *           if publishing failed
   */
  private void testUnpublishPage(String serverUrl, String id) throws Exception {
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

    // Unpublish it
    HttpDelete unpublishPageRequest = new HttpDelete(UrlUtils.concat(requestUrl, id, "publish"));
    httpClient = new DefaultHttpClient();
    logger.info("Unpublishing the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, unpublishPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Unlock the page
    HttpDelete unlockRequest = new HttpDelete(UrlUtils.concat(requestUrl, id, "lock"));
    httpClient = new DefaultHttpClient();
    logger.info("Unlocking the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, unlockRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    String requestByIdUrl = UrlUtils.concat(serverUrl, "/weblounge-pages/", id);
    HttpGet getPageRequest = new HttpGet(requestByIdUrl);
    httpClient = new DefaultHttpClient();
    logger.info("Requesting published page at {}", requestByIdUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
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
