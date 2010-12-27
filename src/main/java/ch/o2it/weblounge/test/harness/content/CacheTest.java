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

package ch.o2it.weblounge.test.harness.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestBase;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.test.util.TestSiteUtils;

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

  /** The page's content length */
  private long contentLength = 567L;
  
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
   * @see ch.o2it.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of response caching");
    
    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));

    // Prepare the request
    logger.info("Testing response cache");

    String requestUrl = UrlUtils.concat(serverUrl, requestPath, language.getIdentifier());

    logger.info("Sending request to the {} version of {}", language.getLocale().getDisplayName(), requestUrl);
    HttpGet request = new HttpGet(requestUrl);
    String[][] params = new String[][] { {
        "language",
        language.getIdentifier() } };

    // Send and the request and examine the response. The first request might
    // not come out of the cache
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, request, params);
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

      // Get the Cache header
      assertNotNull(response.getHeaders("X-Cache-Key"));
      assertEquals(1, response.getHeaders("X-Cache-Key").length);
      String cacheKey = response.getHeaders("X-Cache-Key")[0].getValue();

      // Get the content size
      assertEquals(contentLength, response.getEntity().getContentLength());
      
      // Prepare the second request
      request.setHeader("If-Match", eTag);
      request.setHeader("If-Not-Modified", df.format(System.currentTimeMillis()));
      
      response = TestSiteUtils.request(httpClient, request, params);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());

      // Get the Expires header
      assertNotNull(response.getHeaders("Expires"));
      assertEquals(1, response.getHeaders("Expires").length);
      // We are explicitly not checking for equality with the previously received
      // value, since on first request, that value is not yet correct

      // Get the Etag header
      assertNotNull(response.getHeaders("Etag"));
      assertEquals(1, response.getHeaders("Etag").length);
      assertEquals(eTag, response.getHeaders("Etag")[0].getValue());

      // Test cache key
      assertNotNull(response.getHeaders("X-Cache-Key"));
      assertEquals(1, response.getHeaders("X-Cache-Key").length);
      assertEquals(cacheKey, response.getHeaders("X-Cache-Key")[0].getValue());

      // Test the content length
      assertEquals(contentLength, response.getEntity().getContentLength());

      // Test the expires header
      Date newExpires = df.parse(response.getHeaders("Expires")[0].getValue());
      assertTrue(expires.before(newExpires) || expires.equals(newExpires));
      
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}
