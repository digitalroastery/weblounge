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

package ch.o2it.weblounge.test.harness.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestBase;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.impl.util.TestUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.site.ImageScalingMode;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.IOException;
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

  /** The scaling modes to test */
  private static final List<ImageStyle> styles = new ArrayList<ImageStyle>();

  /** The endpoint's url */
  private static final String baseURL = "system/weblounge/images";

  /** The original image's width */
  private static final int originalWidth = 1000;

  /** The original image's height */
  private static final int originalHeight = 666;

  /** File size of the English version */
  private static final long sizeEnglish = 73642L;

  /** Mime type of the English version */
  private static final String mimetypeEnglish = "image/jpeg";

  /** File name of the English version */
  private static final String filenameEnglish = "porsche.jpg";

  /** File size of the German version */
  private static final long sizeGerman = 88723L;

  /** Mime type of the German version */
  private static final String mimetypeGerman = "image/jpeg";

  /** File name of the German version */
  private static final String filenameGerman = "porsche.jpg";

  /** The style's width */
  private static final int BOX_WIDTH = 250;

  /** The style's height */
  private static final int BOX_HEIGHT = 250;

  /** Image resource identifier */
  private static final String imageId = "5bc19990-8f99-4873-a813-71b6dfac22ad";

  static {
    styles.add(new ImageStyleImpl("box", BOX_WIDTH, BOX_HEIGHT, ImageScalingMode.Box, false));
    styles.add(new ImageStyleImpl("cover", BOX_WIDTH, BOX_HEIGHT, ImageScalingMode.Cover, false));
    styles.add(new ImageStyleImpl("fill", BOX_WIDTH, BOX_HEIGHT, ImageScalingMode.Fill, false));
    styles.add(new ImageStyleImpl("width", BOX_WIDTH, -1, ImageScalingMode.Width, false));
    styles.add(new ImageStyleImpl("height", -1, BOX_HEIGHT, ImageScalingMode.Height, false));
    styles.add(new ImageStyleImpl("none", -1, -1, ImageScalingMode.None, false));
  }

  /**
   * Creates a new instance of the content repository's image endpoint test.
   */
  public ImagesEndpointTest() {
    super("Images Endpoint Test", WEBLOUNGE_ENDPOINT_TEST_GROUP);
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
    logger.info("Preparing test of images endpoint");

    String requestUrl = UrlUtils.concat(serverUrl, baseURL);

    try {
      testGetImageStyles(requestUrl);
      testGetImageStyle(requestUrl);
      testGetImageMetadata(requestUrl);
      testGetOriginalImage(requestUrl);
      testGetOriginalImageByLanguage(requestUrl);
      testGetStyledImageByLanguage(requestUrl);
    } catch (Throwable t) {
      fail("Error occured while testing endpoint: " + t.getMessage());
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
    String url = UrlUtils.concat(serverUrl, "styles");

    logger.info("");
    logger.info("Testing image styles");
    logger.info("");

    HttpGet getStylesRequest = new HttpGet(url);
    HttpClient httpClient = new DefaultHttpClient();
    try {
      logger.debug("Requesting list of image styles");
      HttpResponse response = TestUtils.request(httpClient, getStylesRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("Endpoint returned no content", response.getEntity().getContentLength() > 0);
      Document stylesXml = TestUtils.parseXMLResponse(response);
      assertEquals(6, Integer.parseInt(XPathHelper.valueOf(stylesXml, "count(//imagestyle)")));
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
    
    logger.info("");
    logger.info("Testing single image styles");
    logger.info("");

    for (ImageStyle style : styles) {
      String styleId = style.getScalingMode().toString().toLowerCase();
      String url = UrlUtils.concat(serverUrl, "styles", styleId);
      HttpGet getStyleRequest = new HttpGet(url);
      httpClient = new DefaultHttpClient();
      try {
        logger.info("Requesting image style definition '{}'", styleId);
        HttpResponse response = TestUtils.request(httpClient, getStyleRequest, null);
        assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue("Endpoint returned no content", response.getEntity().getContentLength() > 0);
        Document styleXml = TestUtils.parseXMLResponse(response);
        assertEquals(1, Integer.parseInt(XPathHelper.valueOf(styleXml, "count(//imagestyle)")));
        assertNotNull(styleId, XPathHelper.valueOf(styleXml, "//imagestyle/@id"));
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
    }
  }

  /**
   * Tests the <code>/{id}/metadata</code> method of the endpoint.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetImageMetadata(String serverUrl) throws Exception {
    HttpClient httpClient = null;
    
    logger.info("");
    logger.info("Testing image metadata");
    logger.info("");

    String url = UrlUtils.concat(serverUrl, imageId, "metadata");
    HttpGet getStyleRequest = new HttpGet(url);
    httpClient = new DefaultHttpClient();
    try {
      logger.info("Requesting image metadata");
      HttpResponse response = TestUtils.request(httpClient, getStyleRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("Endpoint returned no content", response.getEntity().getContentLength() > 0);
      Document styleXml = TestUtils.parseXMLResponse(response);
      assertEquals(1, Integer.parseInt(XPathHelper.valueOf(styleXml, "count(//image)")));
      assertEquals(imageId, XPathHelper.valueOf(styleXml, "//image/@id"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests the whether the original image is returned.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetOriginalImage(String serverUrl) throws Exception {
    
    logger.info("");
    logger.info("Testing original image");
    logger.info("");

    String url = UrlUtils.concat(serverUrl, imageId, "original");
    HttpGet request = new HttpGet(url);
    testGermanOriginal(request);
  }

  /**
   * Tests for the special <code>/images</code> uri prefix that is provided by
   * the file request handler.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetOriginalImageByLanguage(String serverUrl) throws Exception {
    
    logger.info("");
    logger.info("Testing original localized image by id");
    logger.info("");

    String url = UrlUtils.concat(serverUrl, imageId, "locales", "en", "original");
    HttpGet request = new HttpGet(url);
    testEnglishOriginal(request);
  }

  /**
   * Tests for the special <code>/images</code> uri prefix that is provided by
   * the file request handler.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetStyledImageByLanguage(String serverUrl) throws Exception {
    
    logger.info("");
    logger.info("Testing styled, localized images");
    logger.info("");

    List<String> eTags = new ArrayList<String>();
    for (ImageStyle style : styles) {
      String url = null;
      HttpGet request = null;
      
      // English
      url = UrlUtils.concat(serverUrl, imageId, "locales", "en", "styles", style.getIdentifier());
      request = new HttpGet(url);
      testEnglishScaled(request, style, eTags);

      // German
      url = UrlUtils.concat(serverUrl, imageId, "locales", "de", "styles", style.getIdentifier());
      request = new HttpGet(url);
      testGermanScaled(request, style, eTags);
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
  private boolean checkSize(HttpResponse response, ImageScalingMode mode) {
    SeekableStream imageInputStream = null;
    try {
      imageInputStream = new MemoryCacheSeekableStream(response.getEntity().getContent());
      RenderedOp image = JAI.create("stream", imageInputStream);
      if (mode == null) {
        assertEquals(originalWidth, image.getWidth());
        assertEquals(originalHeight, image.getHeight());
      } else {
        switch (mode) {
          case Box:
            assertTrue(BOX_WIDTH == image.getWidth() || BOX_HEIGHT == image.getHeight());
            assertTrue("Image width is too large", image.getWidth() <= BOX_WIDTH);
            assertTrue("Image height is too large", image.getHeight() <= BOX_HEIGHT);
            break;
          case Cover:
            assertTrue(BOX_WIDTH == image.getWidth() || BOX_HEIGHT == image.getHeight());
            assertTrue("Image width is too small", image.getWidth() >= BOX_WIDTH);
            assertTrue("Image height is too small", image.getHeight() >= BOX_HEIGHT);
            break;
          case Fill:
            assertEquals("Image width is wrong", BOX_WIDTH, image.getWidth());
            assertEquals("Image height is wrong", BOX_HEIGHT, image.getHeight());
            break;
          case Height:
            assertEquals("Image height is wrong", BOX_HEIGHT, image.getHeight());
            break;
          case None:
            assertEquals("Image width is wrong", originalWidth, image.getWidth());
            assertEquals("Image height is wrong", originalHeight, image.getHeight());
            break;
          case Width:
            assertEquals("Image width is wrong", BOX_WIDTH, image.getWidth());
            break;
          default:
            fail("Unknown image style detected");
            break;
        }
      }
    } catch (IOException e) {
      fail("Unable to read " + mode + " image from response");
    } finally {
      IOUtils.closeQuietly(imageInputStream);
    }

    return true;
  }

  /**
   * Tests for the correctness of the English original image response.
   * 
   * @param response
   *          the http response
   */
  private void testEnglishOriginal(HttpUriRequest request) throws Exception {
    logger.info("Requesting original English image at {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    String eTagValue = null;
    try {
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeEnglish, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeEnglish, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);

      // Test filename
      assertEquals("inline; filename=" + filenameEnglish, response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
      
      // Read the content
      response.getEntity().consumeContent();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request.setHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests for the correctness of the German original image response.
   * 
   * @param response
   *          the http response
   */
  private void testGermanOriginal(HttpUriRequest request) throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    String eTagValue = null;
    try {
      logger.info("Requesting original German image at {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      
      // Test filename
      assertEquals("inline; filename=" + filenameGerman, response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
      
      // Read the content
      response.getEntity().consumeContent();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request.setHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests for the correctness of the English scaled image response.
   * 
   * @param response
   *          the http response
   * @param style
   *          the image style
   */
  private void testEnglishScaled(HttpUriRequest request, ImageStyle style,
      List<String> eTags) throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    String eTagValue = null;
    try {
      logger.info("Requesting scaled English image '{}' at {}", style.getIdentifier(), request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("Response did not contain any content", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeEnglish, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(1, response.getHeaders("Content-Disposition").length);

      // Test filename
      StringBuffer fileName = new StringBuffer(FilenameUtils.getBaseName(filenameEnglish));
      StringBuffer fileNamePrefix = new StringBuffer(fileName);
      if (!ImageScalingMode.None.equals(style.getScalingMode())) {
        float scale = ImageStyleUtils.getScale(originalWidth, originalHeight, style);
        float scaledWidth = originalWidth * scale - ImageStyleUtils.getCropX(originalWidth, originalHeight, style);
        float scaledHeight = originalHeight * scale - ImageStyleUtils.getCropY(originalWidth, originalHeight, style) + 1;
        fileName.append("_").append((int)scaledWidth).append("x").append((int)scaledHeight);
        fileNamePrefix.append("_").append((int)scaledWidth).append("x");
      }
      fileName.append(".").append(FilenameUtils.getExtension(filenameEnglish));
      String contentDisposition = response.getHeaders("Content-Disposition")[0].getValue();
      assertTrue(contentDisposition.startsWith("inline; filename=" + fileNamePrefix.toString()));

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();

      // Make sure ETags are created in a proper way (no duplicates)
      assertFalse("Duplicate ETag returned", eTags.contains(eTagValue));
      if (!"none".equals(style.getIdentifier())) {
        eTags.add(eTagValue);
      }

      // Test content
      assertTrue("Image size mismatch", checkSize(response, style.getScalingMode()));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request.setHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Tests for the correctness of the German scaled image response.
   * 
   * @param response
   *          the http response
   * @param style
   *          the image style
   */
  private void testGermanScaled(HttpUriRequest request, ImageStyle style,
      List<String> eTags) throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    String eTagValue = null;
    try {
      logger.info("Requesting scaled German image '{}' at {}", style.getIdentifier(), request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("Response did not contain any content", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(1, response.getHeaders("Content-Disposition").length);

      // Test filename
      StringBuffer fileName = new StringBuffer(FilenameUtils.getBaseName(filenameGerman));
      StringBuffer fileNamePrefix = new StringBuffer(fileName);
      if (!ImageScalingMode.None.equals(style.getScalingMode())) {
        float scale = ImageStyleUtils.getScale(originalWidth, originalHeight, style);
        float scaledWidth = originalWidth * scale - ImageStyleUtils.getCropX(originalWidth, originalHeight, style);
        float scaledHeight = originalHeight * scale - ImageStyleUtils.getCropY(originalWidth, originalHeight, style) + 1;
        fileName.append("_").append((int)scaledWidth).append("x").append((int)scaledHeight);
        fileNamePrefix.append("_").append((int)scaledWidth).append("x");
      }
      fileName.append(".").append(FilenameUtils.getExtension(filenameGerman));
      String contentDisposition = response.getHeaders("Content-Disposition")[0].getValue();
      assertTrue(contentDisposition.startsWith("inline; filename=" + fileNamePrefix.toString()));

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();

      // Make sure ETags are created in a proper way (no duplicates)
      assertFalse("Duplicate ETag returned", eTags.contains(eTagValue));
      if (!"none".equals(style.getIdentifier())) {
        eTags.add(eTagValue);
      }

      // Test content
      assertTrue("Image size mismatch", checkSize(response, style.getScalingMode()));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request.setHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", request.getURI());
      HttpResponse response = TestUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}
