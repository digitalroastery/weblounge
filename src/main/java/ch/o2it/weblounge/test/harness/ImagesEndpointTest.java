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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.site.ScalingMode;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.servlet.http.HttpServletResponse;

/**
 * Integration test to test the content repository's <code>restful</code> image
 * api.
 */
public class ImagesEndpointTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(ImagesEndpointTest.class);

  /** The endpoint's url */
  private static final String baseURL = "system/weblounge/images";

  /** The scaling modes to test */
  private List<ScalingMode> modes = new ArrayList<ScalingMode>();

  /** The original image's width */
  private static final float originalWidth = 1000;

  /** The original image's height */
  private static final float originalHeight = 666;

  /** File size of the English version */
  private static final long sizeEnglish = 73642L;

  /** Mime type of the English version */
  private static final String mimetypeEnglish = "image/jpeg";

  /** File name of the English version */
  private static final String filenameEnglish = "image.jpg";

  /** File size of the German version */
  private static final long sizeGerman = 262889L;

  /** Mime type of the German version */
  private static final String mimetypeGerman = "image/png";

  /** File name of the German version */
  private static final String filenameGerman = "image.png";

  /** The style's width */
  private static final float width = 250;

  /** The style's height */
  private static final float height = 250;

  /** Image resource identifier */
  private static final String imageId = "5bc19990-8f99-4873-a813-71b6dfac22ad";

  /**
   * Creates a new instance of the content repository's image endpoint test.
   */
  public ImagesEndpointTest() {
    super("Images Endpoint Test");
    modes.add(ScalingMode.Box);
    modes.add(ScalingMode.Cover);
    modes.add(ScalingMode.Fill);
    modes.add(ScalingMode.Width);
    modes.add(ScalingMode.Height);
    modes.add(ScalingMode.None);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of images endpoint");

    // TODO: Make this dynamic
    // serverUrl = UrlSupport.concat(serverUrl, "weblounge");
    String requestUrl = UrlSupport.concat(serverUrl, baseURL);

    try {
      testGetImageStyles(requestUrl);
      testGetImageStyle(requestUrl);
      testGetOriginalImage(requestUrl);
      testGetOriginalImageLanguage(requestUrl);
      testGetStyledImage(requestUrl);
      testGetStyledImageLanguage(requestUrl);
    } catch (Exception e) {
      fail("Error occured while testing endpoint: " + e.getMessage());
    }

  }

  /**
   * Tests the <code>/styles</code> method of the endpoint.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetImageStyles(String serverUrl) throws Exception {
    String url = UrlSupport.concat(serverUrl, "styles");
    HttpGet getStylesRequest = new HttpGet(url);
    HttpClient httpClient = new DefaultHttpClient();
    try {
      logger.debug("Requesting list of image styles from {}", url);
      HttpResponse response = TestSiteUtils.request(httpClient, getStylesRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);
      Document stylesXml = TestSiteUtils.parseXMLResponse(response);
      assertEquals(7, Integer.parseInt(XPathHelper.valueOf(stylesXml, "count(//imagestyle)")));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests the <code>/style/{style}</code> method of the endpoint.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetImageStyle(String serverUrl) throws Exception {
    HttpClient httpClient = null;
    for (ScalingMode mode : modes) {
      String styleId = mode.toString().toLowerCase();
      String url = UrlSupport.concat(new String[] {
          serverUrl,
          "styles",
          styleId });
      HttpGet getStyleRequest = new HttpGet(url);
      httpClient = new DefaultHttpClient();
      try {
        logger.info("Requesting image style '{}' from {}", styleId, url);
        HttpResponse response = TestSiteUtils.request(httpClient, getStyleRequest, null);
        assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(response.getEntity().getContentLength() > 0);
        Document styleXml = TestSiteUtils.parseXMLResponse(response);
        assertEquals(1, Integer.parseInt(XPathHelper.valueOf(styleXml, "count(//imagestyle)")));
        assertNotNull(styleId, XPathHelper.valueOf(styleXml, "//imagestyle/@id"));
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }
  }

  /**
   * Tests the <code>/{id}/styles/original</code> method of the endpoint.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetOriginalImage(String serverUrl) throws Exception {
    HttpClient httpClient = null;
    String url = UrlSupport.concat(new String[] {
        serverUrl,
        imageId,
        "styles",
        "original" });
    HttpGet getStyleRequest = new HttpGet(url);
    httpClient = new DefaultHttpClient();
    try {
      logger.info("Requesting original image version from {}", url);
      HttpResponse response = TestSiteUtils.request(httpClient, getStyleRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);

      // Test headers
      assertEquals(mimetypeEnglish, response.getHeaders("Content-Type"));
      assertEquals(sizeEnglish, response.getEntity().getContentType());
      assertEquals("inline; filename=\"" + filenameEnglish + "\"", response.getHeaders("Content-Disposition"));

      // Test content
      SeekableStream seekableInputStream = new MemoryCacheSeekableStream(response.getEntity().getContent());
      RenderedOp image = JAI.create("stream", seekableInputStream);
      assertEquals(originalWidth, image.getWidth());
      assertEquals(originalHeight, image.getHeight());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests the <code>/{id}/{language}/styles/original</code> method of the
   * endpoint.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetOriginalImageLanguage(String serverUrl) throws Exception {
    HttpClient httpClient = null;

    // English
    String englishUrl = UrlSupport.concat(new String[] {
        serverUrl,
        imageId,
        "en",
        "styles",
        "original" });
    HttpGet getEnglishOriginalRequest = new HttpGet(englishUrl);
    httpClient = new DefaultHttpClient();
    try {
      logger.info("Requesting original english image from {}", englishUrl);
      HttpResponse response = TestSiteUtils.request(httpClient, getEnglishOriginalRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);

      // Test headers
      assertEquals(mimetypeEnglish, response.getHeaders("Content-Type"));
      assertEquals(sizeEnglish, response.getEntity().getContentLength());
      assertEquals("inline; filename=\"" + filenameEnglish + "\"", response.getHeaders("Content-Disposition"));

      // Test content
      SeekableStream seekableInputStream = new MemoryCacheSeekableStream(response.getEntity().getContent());
      RenderedOp image = JAI.create("stream", seekableInputStream);
      assertEquals(originalWidth, image.getWidth());
      assertEquals(originalHeight, image.getHeight());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // German
    String germanUrl = UrlSupport.concat(new String[] {
        serverUrl,
        imageId,
        "de",
        "styles",
        "original" });
    HttpGet getGermanOriginalRequest = new HttpGet(germanUrl);
    httpClient = new DefaultHttpClient();
    try {
      logger.info("Requesting original german image from {}", englishUrl);
      HttpResponse response = TestSiteUtils.request(httpClient, getGermanOriginalRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);

      // Test headers
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type"));
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals("inline; filename=\"" + filenameGerman + "\"", response.getHeaders("Content-Disposition"));

      // Test content
      SeekableStream seekableInputStream = new MemoryCacheSeekableStream(response.getEntity().getContent());
      RenderedOp image = JAI.create("stream", seekableInputStream);
      assertEquals(originalWidth, image.getWidth());
      assertEquals(originalHeight, image.getHeight());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests the <code>/{id}/styles/{style}</code> method of the endpoint.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetStyledImage(String serverUrl) throws Exception {
    HttpClient httpClient = null;
    for (ScalingMode mode : modes) {
      String styleId = mode.toString().toLowerCase();
      String url = UrlSupport.concat(new String[] {
          serverUrl,
          imageId,
          "styles",
          styleId });
      HttpGet getStyleRequest = new HttpGet(url);
      httpClient = new DefaultHttpClient();
      try {
        logger.info("Requesting scaled default image '{}' from ", styleId, url);
        HttpResponse response = TestSiteUtils.request(httpClient, getStyleRequest, null);
        assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(response.getEntity().getContentLength() > 0);
        
        // Test headers
        assertEquals(mimetypeEnglish, response.getHeaders("Content-Type"));
        assertEquals("inline; filename=\"" + filenameEnglish + "\"", response.getHeaders("Content-Disposition"));

        // Test content
        SeekableStream seekableInputStream = new MemoryCacheSeekableStream(response.getEntity().getContent());
        RenderedOp image = JAI.create("stream", seekableInputStream);
        assertTrue(checkSize(image, mode));
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }
  }

  /**
   * Tests the <code>/{id}/{language}/styles/{style}</code> method of the
   * endpoint.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetStyledImageLanguage(String serverUrl) throws Exception {
    HttpClient httpClient = null;

    for (ScalingMode mode : modes) {
      String styleId = mode.toString().toLowerCase();

      // English
      String englishUrl = UrlSupport.concat(new String[] {
          serverUrl,
          imageId,
          "en",
          "styles",
          styleId });
      HttpGet getEnglishOriginalRequest = new HttpGet(englishUrl);
      httpClient = new DefaultHttpClient();
      try {
        logger.info("Requesting scaled english image '{}' from ", styleId, englishUrl);
        HttpResponse response = TestSiteUtils.request(httpClient, getEnglishOriginalRequest, null);
        assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(response.getEntity().getContentLength() > 0);

        // Test headers
        assertEquals(mimetypeEnglish, response.getHeaders("Content-Type"));
        assertEquals("inline; filename=\"" + filenameEnglish + "\"", response.getHeaders("Content-Disposition"));

        // Test content
        SeekableStream seekableInputStream = new MemoryCacheSeekableStream(response.getEntity().getContent());
        RenderedOp image = JAI.create("stream", seekableInputStream);
        assertTrue(checkSize(image, mode));
      } finally {
        httpClient.getConnectionManager().shutdown();
      }

      // German
      String germanUrl = UrlSupport.concat(new String[] {
          serverUrl,
          imageId,
          "de",
          "styles",
          styleId });
      HttpGet getGermanOriginalRequest = new HttpGet(germanUrl);
      httpClient = new DefaultHttpClient();
      try {
        logger.info("Requesting scaled english image '{}' from ", styleId, englishUrl);
        HttpResponse response = TestSiteUtils.request(httpClient, getGermanOriginalRequest, null);
        assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(response.getEntity().getContentLength() > 0);

        // Test headers
        assertEquals(mimetypeGerman, response.getHeaders("Content-Type"));
        assertEquals("inline; filename=\"" + filenameGerman + "\"", response.getHeaders("Content-Disposition"));

        // Test content
        SeekableStream seekableInputStream = new MemoryCacheSeekableStream(response.getEntity().getContent());
        RenderedOp image = JAI.create("stream", seekableInputStream);
        assertTrue(checkSize(image, mode));
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }
  }

  /**
   * Checks the given image for the correct size.
   * 
   * @param image
   *          the image
   * @param mode
   *          the scaling mode
   * @return <code>true</code> if the image size is in line with the scaling
   *         mode
   */
  private boolean checkSize(RenderedOp image, ScalingMode mode) {
    switch (mode) {
      case Box:
        assertEquals(originalWidth, image.getWidth());
        assertEquals(originalHeight * (originalWidth / width), image.getHeight());
        break;
      case Cover:
        assertEquals(height, image.getHeight());
        assertEquals(originalWidth * (originalHeight / height), image.getWidth());
        break;
      case Fill:
        assertEquals(originalWidth, image.getWidth());
        assertEquals(height, image.getHeight());
        break;
      case Height:
        assertEquals(height, image.getHeight());
        assertEquals(originalWidth * (originalHeight / height), image.getWidth());
        break;
      case None:
        assertEquals(originalWidth, image.getWidth());
        assertEquals(originalHeight, image.getHeight());
        break;
      case Width:
        assertEquals(width, image.getWidth());
        assertEquals(originalHeight * (originalWidth / width), image.getHeight());
        break;
    }
    return true;
  }

}
