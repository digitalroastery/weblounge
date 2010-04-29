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

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test for the loading of site resources.
 */
public class JavaServerPagesTest extends IntegrationTestBase {
  
  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(JavaServerPagesTest.class);

  /** The html content type */
  private static final String CONTENT_TYPE_HTML = "text/html";
  
  /** Path to the jsp page */
  private static final String JSP_PATH = "/weblounge-sites/weblounge-test/templates/template.jsp";

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
    
    String requestUrl = UrlSupport.concat(serverUrl, JSP_PATH);
    HttpGet request = new HttpGet(requestUrl);
    logger.info("Sending request to {}", requestUrl);

    // Send and the request and examine the response
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      String contentType = response.getEntity().getContentType().getValue();
      assertEquals(CONTENT_TYPE_HTML, contentType.split(";")[0]);

      // Test template contents
      Document xml = TestSiteUtils.parseXMLResponse(response);
      String greeting = "Welcome to the Weblounge 3.0 testpage!";
      String xpath = "/html/body/h1/text()";
      Assert.assertEquals(greeting, XPathHelper.valueOf(xml, xpath));
      
      // Test site tag libraries
      logger.info("Testing site taglibary on {}", requestUrl);
      xpath = "/html/body/div[@id='greeting']";
      Assert.assertNotNull(XPathHelper.valueOf(xml, xpath));

      // Test site tag libraries
      logger.info("Testing weblounge taglibrary on {}", requestUrl);
      xpath = "/html/head/meta[@name='generator']/@content";
      Assert.assertNotNull(XPathHelper.valueOf(xml, xpath));
      Assert.assertNotNull("Weblounge 3.0", XPathHelper.valueOf(xml, xpath));

    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }
  
}
