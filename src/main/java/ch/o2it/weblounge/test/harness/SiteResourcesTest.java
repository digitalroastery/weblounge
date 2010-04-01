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
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test for the loading of site resources.
 */
public class SiteResourcesTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(SiteResourcesTest.class);

  /** The image content type */
  private static final String CONTENT_TYPE_IMAGE = "image/jpeg";

  /** Path to the image */
  private static final String IMAGE_PATH = "/weblounge-sites/weblounge-test/web/images/porsche.jpg";

  /**
   * Creates a new instance of the <code>SiteResources</code> test.
   */
  public SiteResourcesTest() {
    super("Site resources Test");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Testing loading of an image");
    
    String requestUrl = UrlSupport.concat(serverUrl, IMAGE_PATH);
    HttpGet request = new HttpGet(requestUrl);
    logger.info("Sending request to {}", requestUrl);

    // Send and the request and examine the response
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      Assert.assertEquals(200, response.getStatusLine().getStatusCode());
      String contentType = response.getEntity().getContentType().getValue();
      assertEquals(CONTENT_TYPE_IMAGE, contentType.split(";")[0]);
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

  }

}
