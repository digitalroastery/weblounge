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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
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
  private static final String requestPath = "/test/pagecontent";

  /** The request language */
  private static final Language language = LanguageUtils.getLanguage(Locale.GERMAN);

  /**
   * Creates a new instance of the <code>HTML</code> page test.
   */
  public CacheTest() {
    super("Response Cache Test", WEBLOUNGE_CONTENT_TEST_GROUP);
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
    logger.info("Testing if response cache is activated");

    boolean responseCacheIsActivated = false;

    HttpGet request = new HttpGet(serverUrl);
    HttpClient httpClient = null;
    HttpResponse response = null;
    for (int i = 0; i < 2; i++) {
      try {
        httpClient = new DefaultHttpClient();
        response = TestUtils.request(httpClient, request, null);
      } finally {
        if (httpClient != null)
          httpClient.getConnectionManager().shutdown();
      }
    }

    if (response == null)
      throw new IllegalStateException();

    // Test the Cache header
    Header[] cacheHeaders = response.getHeaders("X-Cache-Key");
    responseCacheIsActivated = cacheHeaders != null && cacheHeaders.length > 0;

    // Is the cache active?
    if (!responseCacheIsActivated) {
      logger.warn("Response cache is not available and won't be tested");
      return;
    }
    
    testCacheHeaders(serverUrl);
  }

  private void testCacheHeaders(String serverUrl) throws Exception {
    logger.info("Preparing test of response caching");

    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));

    // Prepare the request
    logger.info("Testing response cache");

    String requestUrl = UrlUtils.concat(serverUrl, requestPath, language.getIdentifier());

    logger.info("Sending request to the {} version of {}", language.getLocale().getDisplayName(), requestUrl);
    HttpGet request = new HttpGet(requestUrl);
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

      // Get the Etag header
      assertNotNull(response.getHeaders("Etag"));
      assertEquals(1, response.getHeaders("Etag").length);
      String eTag = response.getHeaders("Etag")[0].getValue();

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
      // received
      // value, since on first request, that value is not yet correct

      // Get the Etag header
      assertNotNull(response.getHeaders("Etag"));
      assertEquals(1, response.getHeaders("Etag").length);
      assertEquals(eTag, response.getHeaders("Etag")[0].getValue());

      // Test the Cache header
      assertNotNull(response.getHeaders("X-Cache-Key"));
      assertEquals(1, response.getHeaders("X-Cache-Key").length);
      String cacheKey = response.getHeaders("X-Cache-Key")[0].getValue();
      assertNotNull(cacheKey);

      // Test the expires header
      Date newExpires = df.parse(response.getHeaders("Expires")[0].getValue());
      assertTrue(expires.before(newExpires) || expires.equals(newExpires));

    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}
