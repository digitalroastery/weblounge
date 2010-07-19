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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Integration test to test <code>HTML</code> action output.
 */
public class HTMLActionTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(HTMLActionTest.class);

  /** The paths to test */
  private static final String[] requestPaths = new String[] { "greeting/html", "greeting" };
  
  /**
   * Creates a new instance of the <code>HTML</code> action test.
   */
  public HTMLActionTest() {
    super("HTML Action Test");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of greeter action");

    // Include the mountpoint
    // TODO: Make this dynamic
    //serverUrl = UrlSupport.concat(serverUrl, "weblounge");

    // Load the test data
    Map<String, String> greetings = TestSiteUtils.loadGreetings();
    Set<String> languages = greetings.keySet();

    // Prepare the request
    logger.info("Testing greeter action's html output");
    logger.info("Sending {} requests to {}", languages.size(), UrlSupport.concat(serverUrl, requestPaths[0]));
    
    for (String path : requestPaths) {
      for (String language : languages) {
        String greeting = greetings.get(language);
        HttpGet request = new HttpGet(UrlSupport.concat(serverUrl, path));
        String[][] params = new String[][] {{"language", language}};
    
        // Send and the request and examine the response
        logger.debug("Sending request to {}", request.getURI());
        HttpClient httpClient = new DefaultHttpClient();
        try {
          HttpResponse response = TestSiteUtils.request(httpClient, request, params);
          assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
          
          // Look at the document contents
          String responseHTML = IOUtils.toString(response.getEntity().getContent());
          String responseXML = TestSiteUtils.unescapeHtml(responseHTML);
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setCoalescing(true);
          factory.setIgnoringComments(true);
          factory.setIgnoringElementContentWhitespace(true);
          factory.setNamespaceAware(true);
          DocumentBuilder builder = factory.newDocumentBuilder();
          Document xml = builder.parse(new ByteArrayInputStream(responseXML.getBytes("utf-8")));
          
          // Look for template output
          String templateOutput = XPathHelper.valueOf(xml, "/html/body/h1[. = 'Welcome to the Weblounge 3.0 testpage!']");
          assertNotNull("General template output does not work", templateOutput);

          // Look for action parameter handling and direct output of startState()
          String actualGreeting = XPathHelper.valueOf(xml, "/html/body/div[@class='vcomposer']/h1");
          assertEquals(greeting, actualGreeting);
          logger.debug("Found greeting");
          
          // Look for included pagelets
          assertNotNull("JSP include failed", XPathHelper.valueOf(xml, "/html/body/div[@class='vcomposer']/div[@class='greeting']"));
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

}
