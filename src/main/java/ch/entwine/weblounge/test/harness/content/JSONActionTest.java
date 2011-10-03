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

import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.test.util.TestSiteUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test to test JSON action output.
 */
public class JSONActionTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(JSONActionTest.class);

  /**
   * Creates a new instance of the json action test.
   */
  public JSONActionTest() {
    super("JSON Action Test", WEBLOUNGE_CONTENT_TEST_GROUP);
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
    logger.info("Preparing test of greeter action");

    String requestUrl = UrlUtils.concat(serverUrl, "greeting/index.json");

    // Load the test data
    Map<String, String> greetings = TestSiteUtils.loadGreetings();
    Set<String> languages = greetings.keySet();

    // Prepare the request
    logger.info("Testing greeter action's json output");
    logger.info("Sending requests to {}", requestUrl);

    for (String language : languages) {
      String greeting = greetings.get(language);
      HttpGet request = new HttpGet(requestUrl);
      String[][] params = new String[][] { { "language", language } };

      // Send and the request and examine the response
      logger.debug("Sending request to {}", request.getURI());
      HttpClient httpClient = new DefaultHttpClient();
      try {
        HttpResponse response = TestUtils.request(httpClient, request, params);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

        ObjectMapper jsonMapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };

        // Find the content encoding
        Assert.assertNotNull(response.getFirstHeader("Content-Type"));
        Assert.assertNotNull(response.getFirstHeader("Content-Type").getElements()[0].getParameterByName("charset"));
        String charset = response.getFirstHeader("Content-Type").getElements()[0].getParameterByName("charset").getValue();

        String responseJson = EntityUtils.toString(response.getEntity());
        HashMap<String, Object> json = jsonMapper.readValue(responseJson, typeRef);

        String greetingEncoded = new String(greeting.getBytes(charset));
        Assert.assertEquals(greetingEncoded, json.get(language));
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }
  }

}
