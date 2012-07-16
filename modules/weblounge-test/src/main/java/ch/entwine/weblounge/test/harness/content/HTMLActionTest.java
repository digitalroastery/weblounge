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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.test.site.GreeterHTMLAction;
import ch.entwine.weblounge.test.util.TestSiteUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test to test <code>HTML</code> action output.
 */
public class HTMLActionTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(HTMLActionTest.class);

  /** Title of the alternate template */
  private static final String ALTERNATE_TEMPLATE_TITLE = "Weblounge Test Site Alternate Representation";

  /** The paths to test */
  private static final String[] requestPaths = new String[] {
    "/greeting/",
  "/greeting/html" };

  /** The deault path to action */
  private static final String defaultActionPath = "/greeting";

  /** The path to action configured to render on a specific template */
  private static final String templatedActionPath = "/greeting-templated";

  /** The path to targeted action */
  private static final String targetedActionPath = "/greeting-targeted";

  /**
   * Creates a new instance of the <code>HTML</code> action test.
   */
  public HTMLActionTest() {
    super("HTML Action Test", WEBLOUNGE_CONTENT_TEST_GROUP);
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
    testParametersAndLanguage(serverUrl);
    testConfiguredTargetPage(serverUrl);
    testOverridenTargetPage(serverUrl);
    testConfiguredTemplate(serverUrl);
    testOverridenTemplateByParameter(serverUrl);
    testOverridenTemplateByCode(serverUrl);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  private void testParametersAndLanguage(String serverUrl) throws Exception {
    logger.info("Preparing test of greeter action");

    // Load the test data
    Map<String, String> greetings = TestSiteUtils.loadGreetings();
    Set<String> languages = greetings.keySet();

    // Prepare the request
    logger.info("Testing greeter action's html output");
    logger.info("Sending {} requests to {}", languages.size(), UrlUtils.concat(serverUrl, requestPaths[0]));

    for (String path : requestPaths) {
      for (String language : languages) {
        String greeting = greetings.get(language);
        HttpGet request = new HttpGet(UrlUtils.concat(serverUrl, path));
        String[][] params = new String[][] { { "language", language } };

        // Send and the request and examine the response
        logger.debug("Sending request to {}", request.getURI());
        HttpClient httpClient = new DefaultHttpClient();
        try {
          HttpResponse response = TestUtils.request(httpClient, request, params);
          assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

          // Get the document contents
          Document xml = TestUtils.parseXMLResponse(response);

          // Look for page content output
          String templateOutput = XPathHelper.valueOf(xml, "/html/head/title");
          assertNotNull("General template output does not work", templateOutput);
          assertEquals("Template title is not as expected", "Welcome to Weblounge", templateOutput);

          // Make sure it is rendered on the home page
          String testSuiteTitle = XPathHelper.valueOf(xml, "/html/body/h1");
          assertEquals("Action is not rendered on start page", "Welcome to the Weblounge 3.0 testpage!", testSuiteTitle);

          // Look for action parameter handling and direct output of
          // startState()
          String actualGreeting = XPathHelper.valueOf(xml, "/html/body/div[@id='main']/h1");
          assertEquals(greeting, actualGreeting);
          logger.debug("Found greeting");

          // Look for included pagelets
          assertNotNull("JSP include failed", XPathHelper.valueOf(xml, "/html/body/div[@id='main']/div[@class='greeting']"));
          logger.debug("Found pagelet content");

          // Look for action header includes
          assertEquals("Action include failed", "1", XPathHelper.valueOf(xml, "count(/html/head/script[contains(@src, '/scripts/greeting.js')])"));
          logger.debug("Found action javascript include");

          // Look for pagelet header includes
          assertEquals("Pagelet include failed", "1", XPathHelper.valueOf(xml, "count(/html/head/link[contains(@href, 'greeting.css')])"));
          logger.debug("Found pagelet stylesheet include");

          // Test for template replacement
          assertNull("Header tag templating failed", XPathHelper.valueOf(xml, "//@src[contains(., '${module.root}')]"));
          assertNull("Header tag templating failed", XPathHelper.valueOf(xml, "//@src[contains(., '${site.root}')]"));

        } finally {
          httpClient.getConnectionManager().shutdown();
        }
      }
    }
  }

  /**
   * Tests whether actions are rendered on pages as configured in module.xml
   * 
   * @param serverUrl
   *          the server url
   */
  private void testConfiguredTargetPage(String serverUrl) {
    logger.info("Preparing test of greeter action");

    // Prepare the request
    logger.info("Testing action target page configuration");

    HttpGet request = new HttpGet(UrlUtils.concat(serverUrl, targetedActionPath));

    // Send the request and make sure it ends up on the expected page
    logger.info("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the document contents
      Document xml = TestUtils.parseXMLResponse(response);

      // Make sure it is rendered on the home page
      String testSuiteTitle = XPathHelper.valueOf(xml, "/html/body/h1");
      assertNull("Action is not rendered on configured page", testSuiteTitle);

    } catch (Throwable e) {
      fail("Request to " + request.getURI() + " failed" + e.getMessage());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests whether action output can be redirected to certain pages by providing
   * a target url.
   * 
   * @param serverUrl
   *          the server url
   */
  private void testOverridenTargetPage(String serverUrl) {
    logger.info("Preparing test of greeter action");

    // Prepare the request
    logger.info("Testing action target page overriding");

    StringBuffer requestUrl = new StringBuffer(targetedActionPath);
    requestUrl.append("?").append(HTMLAction.TARGET_PAGE).append("=/");
    HttpGet request = new HttpGet(UrlUtils.concat(serverUrl, requestUrl.toString()));

    // Send the request and make sure it ends up on the expected page
    logger.info("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the document contents
      Document xml = TestUtils.parseXMLResponse(response);

      // Make sure it is rendered on the home page
      String testSuiteTitle = XPathHelper.valueOf(xml, "/html/body/h1");
      assertEquals("Action is not rendered on start page", "Welcome to the Weblounge 3.0 testpage!", testSuiteTitle);

    } catch (Throwable e) {
      fail("Request to " + request.getURI() + " failed" + e.getMessage());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests whether actions are rendered using templates as configured in
   * module.xml
   * 
   * @param serverUrl
   *          the server url
   */
  private void testConfiguredTemplate(String serverUrl) {
    logger.info("Preparing test of greeter action with configured template");

    HttpGet request = new HttpGet(UrlUtils.concat(serverUrl, templatedActionPath));

    // Send the request and make sure it ends up on the expected page
    logger.info("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the document contents
      Document xml = TestUtils.parseXMLResponse(response);

      // Make sure it is rendered on the home page
      String templateTitle = XPathHelper.valueOf(xml, "/html/head/title");
      assertEquals("Action is not rendered on alternate template", ALTERNATE_TEMPLATE_TITLE, templateTitle);

    } catch (Throwable e) {
      fail("Request to " + request.getURI() + " failed" + e.getMessage());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests whether action template can be overwritten.
   * 
   * @param serverUrl
   *          the server url
   */
  private void testOverridenTemplateByParameter(String serverUrl) {
    logger.info("Preparing test of greeter action with overridden template by parameter");

    StringBuffer requestUrl = new StringBuffer(defaultActionPath);
    requestUrl.append("?").append(HTMLAction.TARGET_TEMPLATE).append("=alternate");
    HttpGet request = new HttpGet(UrlUtils.concat(serverUrl, requestUrl.toString()));

    // Send the request and make sure it ends up on the expected page
    logger.info("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the document contents
      Document xml = TestUtils.parseXMLResponse(response);

      // Make sure it is rendered on the home page
      String templateTitle = XPathHelper.valueOf(xml, "/html/head/title");
      assertEquals("Action is not rendered on alternate template", ALTERNATE_TEMPLATE_TITLE, templateTitle);

    } catch (Throwable e) {
      fail("Request to " + request.getURI() + " failed" + e.getMessage());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests whether action template can be overwritten. The template is
   * overwritten in the configure() method rather than by a request parameter.
   * 
   * @param serverUrl
   *          the server url
   */
  private void testOverridenTemplateByCode(String serverUrl) {
    logger.info("Preparing test of greeter action with overridden template by code");

    StringBuffer requestUrl = new StringBuffer(defaultActionPath);
    requestUrl.append("?").append(GreeterHTMLAction.CODE_TEMPLATE).append("=alternate");
    HttpGet request = new HttpGet(UrlUtils.concat(serverUrl, requestUrl.toString()));

    // Send the request and make sure it ends up on the expected page
    logger.info("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      // Get the document contents
      Document xml = TestUtils.parseXMLResponse(response);

      // Make sure it is rendered on the home page
      String templateTitle = XPathHelper.valueOf(xml, "/html/head/title");
      assertEquals("Action is not rendered on alternate template", ALTERNATE_TEMPLATE_TITLE, templateTitle);

    } catch (Throwable e) {
      fail("Request to " + request.getURI() + " failed" + e.getMessage());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}
