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

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Set;

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
    serverUrl = UrlSupport.concat(serverUrl, "weblounge");

    // Load the test data
    Map<String, String> greetings = TestSiteUtils.loadGreetings();
    Set<String> languages = greetings.keySet();

    // Prepare the request
    logger.info("Testing greeter action's html output");
    logger.info("Sending request to {}", UrlSupport.concat(serverUrl, requestPaths[0]));
    
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
          Assert.assertEquals(200, response.getStatusLine().getStatusCode());
          String responseHTML = IOUtils.toString(response.getEntity().getContent());
          String responseXML = StringEscapeUtils.unescapeHtml(responseHTML);
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setNamespaceAware(true);
          DocumentBuilder builder = factory.newDocumentBuilder();
          Document xml = builder.parse(new ByteArrayInputStream(responseXML.getBytes("utf-8")));
          String found = XPathHelper.valueOf(xml, "/HTML/BODY/H1");
          logger.debug("Found greeting " + found);
          Assert.assertEquals(greeting, found);
        } finally {
          httpClient.getConnectionManager().shutdown();
        }
      }
    }

  }

}
