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

import ch.o2it.weblounge.common.impl.testing.IntegrationTestBase;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Test;
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
    super("Protected Static Resources Test", WEBLOUNGE_TEST_GROUP);
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
    for (String resource : protectedResources) {
      HttpClient httpClient = new DefaultHttpClient();
      String requestUrl = UrlUtils.concat(serverUrl, SITE_ROOT_PATH, resource);
      HttpGet request = new HttpGet(requestUrl);
      try {
        logger.info("Testing loading of the protected resource {}", resource);
        HttpResponse response = TestSiteUtils.request(httpClient, request, null);
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }
  }

}
