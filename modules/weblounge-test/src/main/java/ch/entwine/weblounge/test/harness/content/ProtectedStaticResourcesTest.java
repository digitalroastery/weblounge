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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test for the loading of site resources.
 */
public class ProtectedStaticResourcesTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(ProtectedStaticResourcesTest.class);

  /** Path to the site */
  private static final String SITE_ROOT_PATH = "/weblounge-sites/weblounge-test";

  /** Paths to the protected resources */
  private static final String[] protectedResources = {
      "/site.xml",
      "/modules/test/module.xml" };

  /**
   * Creates a new instance of the <code>SiteResources</code> test.
   */
  public ProtectedStaticResourcesTest() {
    super("Protected Static Resources Test", WEBLOUNGE_CONTENT_TEST_GROUP);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  @Override
  public void execute(String serverUrl) throws Exception {
    for (String resource : protectedResources) {
      HttpClient httpClient = new DefaultHttpClient();
      String requestUrl = UrlUtils.concat(serverUrl, SITE_ROOT_PATH, resource);
      HttpGet request = new HttpGet(requestUrl);
      try {
        logger.info("Testing loading of the protected resource {}", resource);
        HttpResponse response = TestUtils.request(httpClient, request, null);
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }
  }

}
