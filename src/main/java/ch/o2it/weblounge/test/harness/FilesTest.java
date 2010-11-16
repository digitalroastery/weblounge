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
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.testing.IntegrationTestBase;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test for file serving capabilities.
 */
public class FilesTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(FilesTest.class);

  /** Path to the image resource */
  private static final String path = "/test/document";

  /** File size of the English version */
  private static final long sizeEnglish = 73642L;

  /** Mime type of the English version */
  private static final String mimetypeEnglish = "image/jpeg";

  /** File name of the English version */
  private static final String filenameEnglish = "porsche.jpg";

  /** File size of the German version */
  private static final long sizeGerman = 88723L;

  /** Mime type of the German version */
  private static final String mimetypeGerman = "image/jpeg";

  /** File name of the German version */
  private static final String filenameGerman = "porsche.jpg";

  /** Resource identifier */
  private static final String resourceId = "6bc19990-8f99-4873-a813-71b6dfac22ad";

  /**
   * Creates a new instance of the images test.
   */
  public FilesTest() {
    super("Files Test", WEBLOUNGE_TEST_GROUP);
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
   * @see ch.o2it.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of file request handler");
    try {
      testGetDocument(serverUrl);
      testGetDocumentById(serverUrl);
      testGetDocumentByPathLanguage(serverUrl);
      testGetDocumentByHeaderLanguage(serverUrl);
    } catch (Throwable t) {
      fail("Error occured while testing files request handler: " + t.getMessage());
    }

  }

  /**
   * Tests the whether the original document is returned.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetDocument(String serverUrl) throws Exception {
    HttpClient httpClient = null;
    String url = UrlUtils.concat(serverUrl, path);
    HttpGet request = new HttpGet(url);
    request.setHeader("Accept-Language", "de");
    httpClient = new DefaultHttpClient();
    String eTagValue = null;
    try {
      logger.info("Requesting original document from {}", request.getURI());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      assertEquals("inline; filename=" + filenameGerman, response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request = new HttpGet(url);
      request.setHeader("Accept-Language", "de");
      request.addHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", url);
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Tests for the special <code>/files</code> uri prefix that is provided by
   * the file request handler.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetDocumentById(String serverUrl) throws Exception {
    HttpClient httpClient = null;
    String url = UrlUtils.concat(serverUrl, "files", resourceId);
    HttpGet request = new HttpGet(url);
    request.setHeader("Accept-Language", "de");
    httpClient = new DefaultHttpClient();
    String eTagValue = null;
    try {
      logger.info("Requesting German document version from {}", request.getURI());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      assertEquals("inline; filename=" + filenameGerman, response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request = new HttpGet(url);
      request.addHeader("If-None-Match", eTagValue);
      request.setHeader("Accept-Language", "de");

      logger.info("Sending 'If-None-Match' request to {}", url);
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Tests requests that send the required language as part of the request url.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetDocumentByPathLanguage(String serverUrl) throws Exception {
    HttpClient httpClient = null;

    // English
    String englishUrl = UrlUtils.concat(serverUrl, path, "en");
    HttpGet request = new HttpGet(englishUrl);
    httpClient = new DefaultHttpClient();
    String eTagValue = null;
    try {
      logger.info("Requesting English document from {}", request.getURI());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeEnglish, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeEnglish, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      assertEquals("inline; filename=" + filenameEnglish, response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request = new HttpGet(englishUrl);
      request.addHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", englishUrl);
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // German
    String germanUrl = UrlUtils.concat(serverUrl, path, "de");
    request = new HttpGet(germanUrl);
    httpClient = new DefaultHttpClient();
    try {
      logger.info("Requesting German document from {}", request.getURI());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      assertEquals("inline; filename=" + filenameGerman, response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request = new HttpGet(germanUrl);
      request.addHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", germanUrl);
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

  /**
   * Tests requests that send the required language as part of the
   * <code>Accept-Language</code> header.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetDocumentByHeaderLanguage(String serverUrl)
      throws Exception {
    HttpClient httpClient = null;

    // German
    String englishUrl = UrlUtils.concat(serverUrl, path);
    HttpGet request = new HttpGet(englishUrl);
    request.setHeader("Accept-Language", "en");
    httpClient = new DefaultHttpClient();
    String eTagValue = null;
    try {
      logger.info("Requesting English document from {}", request.getURI());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeEnglish, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeEnglish, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      assertEquals("inline; filename=" + filenameEnglish, response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request = new HttpGet(englishUrl);
      request.addHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", englishUrl);
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // German
    String germanUrl = UrlUtils.concat(serverUrl, path);
    request = new HttpGet(germanUrl);
    request.setHeader("Accept-Language", "de");
    httpClient = new DefaultHttpClient();
    try {
      logger.info("Requesting German document from {}", request.getURI());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      assertEquals("inline; filename=" + filenameGerman, response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request = new HttpGet(germanUrl);
      request.addHeader("If-None-Match", eTagValue);
      request.setHeader("Accept-Language", "de");

      logger.info("Sending 'If-None-Match' request to {}", germanUrl);
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

}
