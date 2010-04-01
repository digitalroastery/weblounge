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

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Integration test for the loading of site resources.
 */
public class JavaServerPagesTest extends IntegrationTestBase {
  
  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(JavaServerPagesTest.class);

  /** The html content type */
  private static final String HTML_CONTENT_TYPE = "text/html";

  /**
   * Creates a new instance of the <code>JavaServerPagesTest</code> test.
   */
  public JavaServerPagesTest() {
    super("Java Server Pages Test");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Testing loading of java server page");
    
    String imagePath = "/weblounge-sites/weblounge-test/templates/template.jsp";
    HttpGet request = new HttpGet(UrlSupport.concat(serverUrl, imagePath));

    // Send and the request and examine the response
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      
      // Test response state and headers
      assertEquals(200, response.getStatusLine().getStatusCode());
      assertEquals(HTML_CONTENT_TYPE, response.getEntity().getContentType().getValue());

      // Test contents
      Document xml = TestSiteUtils.parseXMLResponse(response);
      String greeting = "Hello World!";
      String xpath = "/html/body/h1/text()";
      Assert.assertEquals(greeting, XPathHelper.valueOf(xml, xpath));    
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }
  
}
