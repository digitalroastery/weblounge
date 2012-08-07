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

import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.url.UrlUtils;

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
 * Integration test for i18n.
 */
public class I18nTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(I18nTest.class);

  /** Path to the i18n test page */
  private static final String TEST_URL = "/test/i18n/en";

  /**
   * Creates a new instance of the <code>I18nTest</code> test.
   */
  public I18nTest() {
    super("I18n Test", WEBLOUNGE_CONTENT_TEST_GROUP);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  @Override
  public void execute(String serverUrl) throws Exception {
    logger.info("Testing i18n dictionary and tag");

    String requestUrl = UrlUtils.concat(serverUrl, TEST_URL);
    HttpGet request = new HttpGet(requestUrl);
    logger.info("Sending request to {}", requestUrl);

    // Send and the request and examine the response
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      Document xml = TestUtils.parseXMLResponse(response);

      // Test i18n values defined at the module level
      String i18nModuleValue = "I18n Module Value";
      String xpath = "/html/body//div[@id='i18n-module']";
      Assert.assertEquals(i18nModuleValue, XPathHelper.valueOf(xml, xpath));

      // Test i18n values defined at the page level
      String i18nPageValue = "I18n Page Value";
      xpath = "/html/body//div[@id='i18n-page']";
      Assert.assertEquals(i18nPageValue, XPathHelper.valueOf(xml, xpath));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

}
