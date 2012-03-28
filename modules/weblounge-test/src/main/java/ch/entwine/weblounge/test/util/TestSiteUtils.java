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

package ch.entwine.weblounge.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.impl.util.TestUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

/**
 * Utility class used to facilitate testing.
 */
public final class TestSiteUtils {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(TestSiteUtils.class);

  /** Name of the properties file that defines the greetings */
  public static final String GREETING_PROPS = "/greetings.properties";

  /**
   * This class is not intended to be instantiated.
   */
  private TestSiteUtils() {
    // Nothing to be done here.
  }

  /**
   * Test for the correct response when etag header is set.
   * 
   * @param request
   *          the http request
   * @param eTagValue
   *          the expected etag value
   * @param logger
   *          used to log test output
   * @param params
   *          the request parameters
   * @throws Exception
   *           if processing the request fails
   */
  public static void testETagHeader(HttpUriRequest request, String eTagValue,
      Logger logger, String[][] params) throws Exception {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      request.removeHeaders("If-Modified-Since");
      request.setHeader("If-None-Match", eTagValue);
      logger.info("Sending 'If-None-Match' request to {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, params);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    httpClient = new DefaultHttpClient();
    try {
      request.removeHeaders("If-Modified-Since");
      request.setHeader("If-None-Match", "\"abcdefghijklmt\"");
      logger.info("Sending 'If-None-Match' request to {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertNotNull(response.getEntity());
      response.getEntity().consumeContent();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Test for the correct response when modified since header is set.
   * 
   * @param request
   *          the http request
   * @param date
   *          the expected modification date
   * @param logger
   *          used to log test output
   * @param params
   *          the request parameters
   * @throws Exception
   *           if processing the request fails
   */
  public static void testModifiedHeader(HttpUriRequest request,
      Date modificationDate, Logger logger, String[][] params) throws Exception {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    Date before = new Date(modificationDate.getTime() - Times.MS_PER_DAY);
    Date after = new Date(modificationDate.getTime() + Times.MS_PER_DAY);
    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    try {
      request.removeHeaders("If-None-Match");
      request.setHeader("If-Modified-Since", format.format(after));
      logger.info("Sending 'If-Modified-Since' request to {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, params);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    httpClient = new DefaultHttpClient();
    try {
      request.removeHeaders("If-None-Match");
      request.setHeader("If-Modified-Since", format.format(before));
      logger.info("Sending 'If-Modified-Since' request to {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertNotNull(response.getEntity());
      response.getEntity().consumeContent();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Loads the greetings from <code>/greetings.properties</code> into a
   * <code>Map</code> and returns them.
   * <p>
   * Note that we need to do a conversion from the <code>ISO-LATIN-1</code>
   * (which is assumed by the <code>Properties</code> implementation) to
   * <code>utf-8</code>.
   * 
   * @return the greetings
   * @throws Exception
   *           if loading fails
   */
  public static Map<String, String> loadGreetings() {
    Map<String, String> greetings = new HashMap<String, String>();
    ClassLoader c = TestSiteUtils.class.getClassLoader();
    InputStream is = c.getResourceAsStream(GREETING_PROPS);
    try {
      Properties props = new Properties();
      props.load(is);
      for (Entry<Object, Object> entry : props.entrySet()) {
        try {
          String isoLatin1Value = entry.getValue().toString();
          String utf8Value = new String(isoLatin1Value.getBytes("ISO-8859-1"), "utf-8");
          greetings.put((String) entry.getKey(), utf8Value);
        } catch (UnsupportedEncodingException e) {
          logger.error("I can't believe the platform does not support encoding {}", e.getMessage());
        }
      }
    } catch (IOException e) {
      logger.error("Error reading greetings from " + GREETING_PROPS, e);
      return null;
    } finally {
      IOUtils.closeQuietly(is);
    }
    return greetings;
  }

}
