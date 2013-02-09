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

package ch.entwine.weblounge.test.harness.content;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test for the response cache.
 */
public class CacheTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(CacheTest.class);

  /** The paths to test */
  private static final String contentTestPage = "/test/pagecontent";

  /** The paths to test */
  private static final String modificationTestPage = "/test/modificationDate";

  /** The id of the page that is hosting the modification date pagelet */
  private static final String modificationTestPageId = "4bb19980-8f98-4873-a813-000000000015";

  /** The paths to host page */
  private static final String hostPage = "/test/host";

  /** The id of the page that is linked to by the host page */
  private static final String linkedPageId = "4bb19980-8f98-4873-a813-a00000000002";

  /** The request language */
  private static final Language language = LanguageUtils.getLanguage(Locale.GERMAN);

  /**
   * Creates a new instance of the <code>HTML</code> page test.
   */
  public CacheTest() {
    super("Response Cache Test", WEBLOUNGE_CONTENT_TEST_GROUP);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  @Override
  public void execute(String serverUrl) throws Exception {
    logger.info("Testing if response cache is activated");

    // Test if the cache is active
    logger.info("Sending request to determine cache status to {}", serverUrl);
    HttpGet request = new HttpGet(serverUrl);
    request.addHeader("X-Cache-Debug", "yes");
    String[][] params = new String[][] { {} };

    // Send and the request and examine the response twice to make sure to get
    // a cached response is available
    boolean cacheIsActive = false;
    for (int i = 0; i < 2; i++) {
      HttpClient httpClient = new DefaultHttpClient();
      try {
        HttpResponse response = TestUtils.request(httpClient, request, params);
        cacheIsActive = response.getHeaders("X-Cache-Key").length > 0;
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }

    if (!cacheIsActive) {
      logger.warn("Response cache is not available and won't be tested");
      return;
    }

    logger.warn("Response cache is active");
    testCacheHeaders(serverUrl);
    testInheritedModifcation(serverUrl);
    testPageletModifcationDate(serverUrl);
  }

  /**
   * Test if the cache is returning proper header to enable caching on the
   * client side, such as <code>Last-Modified</code>, <code>Expires</code> or
   * <code>ETag</code>.
   * 
   * @param serverUrl
   *          the server url
   * @throws Exception
   *           if the test fails
   */
  private void testCacheHeaders(String serverUrl) throws Exception {
    logger.info("Preparing test of response caching");

    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));

    // Prepare the request
    logger.info("Testing response cache");

    String requestUrl = UrlUtils.concat(serverUrl, contentTestPage, language.getIdentifier());

    logger.info("Sending request to the {} version of {}", language.getLocale().getDisplayName(), requestUrl);
    HttpGet request = new HttpGet(requestUrl);
    request.addHeader("X-Cache-Debug", "yes");
    String[][] params = new String[][] { { "language", language.getIdentifier() } };

    // Send and the request and examine the response. The first request might
    // not come out of the cache
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, params);
      int statusCode = response.getStatusLine().getStatusCode();
      boolean okOrNotModified = statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_NOT_MODIFIED;
      assertTrue(okOrNotModified);

      // Get the ETag header
      assertNotNull(response.getHeaders("ETag"));
      assertEquals(1, response.getHeaders("ETag").length);
      String eTag = response.getHeaders("ETag")[0].getValue();

      // Get the Expires header
      assertNotNull(response.getHeaders("Expires"));
      assertEquals(1, response.getHeaders("Expires").length);
      Date expires = df.parse(response.getHeaders("Expires")[0].getValue());

      // Prepare the second request
      response.getEntity().consumeContent();
      httpClient.getConnectionManager().shutdown();

      // Give the cache time to persist the entry
      Thread.sleep(1000);

      httpClient = new DefaultHttpClient();

      request.setHeader("If-None-Match", eTag);
      request.setHeader("If-Modified-Since", df.format(System.currentTimeMillis()));

      response = TestUtils.request(httpClient, request, params);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());

      // Get the Expires header
      assertNotNull(response.getHeaders("Expires"));
      assertEquals(1, response.getHeaders("Expires").length);

      // We are explicitly not checking for equality with the previously
      // received value, since on first request, that value is not yet correct

      // Get the ETag header
      assertNotNull(response.getHeaders("ETag"));
      assertEquals(0, response.getHeaders("ETag").length);

      // Test the Cache header
      assertNotNull(response.getHeaders("X-Cache-Key"));
      assertEquals(1, response.getHeaders("X-Cache-Key").length);

      // Test the expires header
      Date newExpires = df.parse(response.getHeaders("Expires")[0].getValue());
      assertTrue(expires.before(newExpires) || expires.equals(newExpires));

    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests if the modification date matches that of the most recent element on a
   * page rather than the page's modification date.
   * 
   * @param serverUrl
   *          the server url
   * @throws Exception
   *           if the test fails
   */
  private void testInheritedModifcation(String serverUrl) throws Exception {
    logger.info("Preparing test of cache headers influenced by inherited updated content");

    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));

    // Prepare the request
    logger.info("Gathering original modification date");

    String requestUrl = UrlUtils.concat(serverUrl, hostPage, language.getIdentifier());

    logger.info("Sending request to {}", requestUrl);
    HttpGet request = new HttpGet(requestUrl);
    request.addHeader("X-Cache-Debug", "yes");
    String[][] params = new String[][] { {} };

    // Send and the request and examine the response. Keep the modification
    // date.
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, params);

      int statusCode = response.getStatusLine().getStatusCode();
      boolean okOrNotModified = statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_NOT_MODIFIED;
      assertTrue(okOrNotModified);

      // Get the ETag header
      assertNotNull(response.getHeaders("ETag"));
      assertEquals(1, response.getHeaders("ETag").length);
      String hostOrignalETag = response.getHeaders("ETag")[0].getValue();

      // Get the Modified header
      assertNotNull(response.getHeaders("Last-Modified"));
      assertEquals(1, response.getHeaders("Last-Modified").length);
      Date hostModified = df.parse(response.getHeaders("Last-Modified")[0].getValue());
      response.getEntity().consumeContent();
      httpClient.getConnectionManager().shutdown();

      logger.info("Updating linked page");
      update(serverUrl, linkedPageId);

      // Give the cache time to persist the entry
      Thread.sleep(1000);

      httpClient = new DefaultHttpClient();

      // Test using ETag and modified header
      request.setHeader("If-None-Match", hostOrignalETag);
      request.setHeader("If-Modified-Since", df.format(hostModified));

      response = TestUtils.request(httpClient, request, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the Expires header
      assertNotNull(response.getHeaders("Expires"));
      assertEquals(1, response.getHeaders("Expires").length);
      // We are explicitly not checking for equality with the previously
      // received value, since on first request, that value is not yet correct

      // Get the ETag header and make sure it has been updated
      assertNotNull(response.getHeaders("ETag"));
      assertEquals(1, response.getHeaders("ETag").length);
      assertFalse(hostOrignalETag.equals(response.getHeaders("ETag")[0].getValue()));

      // Test the modified header and make sure it has been updated
      assertNotNull(response.getHeaders("Last-Modified"));
      assertEquals(1, response.getHeaders("Last-Modified").length);
      Date newModified = df.parse(response.getHeaders("Last-Modified")[0].getValue());
      assertTrue(hostModified.before(newModified));

    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests if the modification date of a page can properly be adjusted by a
   * pagelet that is using the <code>&lt;modified&gt;</code> tag.
   * 
   * @param serverUrl
   *          the server url
   * @throws Exception
   *           if the test fails
   */
  private void testPageletModifcationDate(String serverUrl) throws Exception {
    logger.info("Preparing test of cache headers influenced by the 'modified' tag");

    // Load the page's modification date

    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages", modificationTestPageId);
    HttpGet getPageRequest = new HttpGet(requestUrl);
    HttpClient httpClient = new DefaultHttpClient();
    Page page = null;
    logger.info("Requesting the page's modification date at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPageRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      PageReader reader = new PageReader();
      page = reader.read(response.getEntity().getContent(), site);
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));

    // Regularly load the page

    requestUrl = UrlUtils.concat(serverUrl, modificationTestPage);

    logger.info("Sending request to {}", requestUrl);
    HttpGet request = new HttpGet(requestUrl);
    request.addHeader("X-Cache-Debug", "yes");
    String[][] params = new String[][] { {} };

    // Send and the request and examine the response. Keep the modification
    // date.
    httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, params);

      int statusCode = response.getStatusLine().getStatusCode();
      boolean okOrNotModified = statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_NOT_MODIFIED;
      assertTrue(okOrNotModified);

      // Get the Modified header
      assertNotNull(response.getHeaders("Last-Modified"));
      assertEquals(1, response.getHeaders("Last-Modified").length);
      Date hostModified = df.parse(response.getHeaders("Last-Modified")[0].getValue());
      response.getEntity().consumeContent();

      // Make sure the page is advertised as being more recent than the page's
      // modification date
      assertTrue(hostModified.after(page.getModificationDate()));

    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Updates the page with the given identifier.
   * 
   * @param serverUrl
   *          the server url
   * @param id
   *          the page identifier
   * @throws Exception
   *           if updating the page fails
   */
  private void update(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "system/weblounge/pages");

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
    String[][] params = new String[][] { { "modified", "true" } };
    logger.info("Publishing the page at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, publishPageRequest, params);
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

  }

}
