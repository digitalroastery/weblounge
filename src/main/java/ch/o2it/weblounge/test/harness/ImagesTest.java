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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.image.ImageStyle;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestBase;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.site.ImageScalingMode;
import ch.o2it.weblounge.test.util.TestSiteUtils;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.servlet.http.HttpServletResponse;

/**
 * Integration test for image serving capabilities.
 */
public class ImagesTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(ImagesTest.class);

  /** The scaling modes to test */
  private static final List<ImageStyle> styles = new ArrayList<ImageStyle>();

  /** The original image's width */
  private static final float originalWidth = 1000;

  /** The original image's height */
  private static final float originalHeight = 666;

  /** File size of the English version */
  private static final long sizeEnglish = 73642L;

  /** Path to the image resource */
  private static final String imagePath = "/test/image";

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
  private static final float width = 250;

  /** The style's height */
  private static final float height = 250;

  /** Image resource identifier */
  private static final String imageId = "5bc19990-8f99-4873-a813-71b6dfac22ad";

  static {
    styles.add(new ImageStyleImpl("box", 250, 250, ImageScalingMode.Box, false));
    styles.add(new ImageStyleImpl("cover", 250, 250, ImageScalingMode.Cover, false));
    styles.add(new ImageStyleImpl("fill", 250, 250, ImageScalingMode.Fill, false));
    styles.add(new ImageStyleImpl("width", 250, -1, ImageScalingMode.Width, false));
    styles.add(new ImageStyleImpl("height", -1, 250, ImageScalingMode.Height, false));
    styles.add(new ImageStyleImpl("none", -1, -1, ImageScalingMode.None, false));
  }

  /**
   * Creates a new instance of the images test.
   */
  public ImagesTest() {
    super("Images Test", WEBLOUNGE_TEST_GROUP);
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

    String requestUrl = serverUrl;

    try {
      testGetOriginalImage(requestUrl);
      testGetOriginalImageById(requestUrl);
      testGetOriginalImageByPathLanguage(requestUrl);
      testGetOriginalImageByHeaderLanguage(requestUrl);
      testGetStyledImageById(requestUrl);
      testGetStyledImageByPathLanguage(requestUrl);
      testGetStyledImageByHeaderLanguage(requestUrl);
    } catch (Throwable t) {
      fail("Error occured while testing endpoint: " + t.getMessage());
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
    String url = UrlUtils.concat(serverUrl, imagePath);
    HttpGet request = new HttpGet(url);
    request.setHeader("Accept-Language", "de");
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
  private void testGetOriginalImageById(String serverUrl) throws Exception {
    String url = UrlUtils.concat(serverUrl, "images", imageId);
    HttpGet request = new HttpGet(url);
    request.setHeader("Accept-Language", "de");
    testGermanOriginal(request);
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
  private void testGetOriginalImageByPathLanguage(String serverUrl)
      throws Exception {

    // English
    String englishUrl = UrlUtils.concat(serverUrl, imagePath, "en");
    HttpGet request = new HttpGet(englishUrl);
    testEnglishOriginal(request);

    // German
    String germanUrl = UrlUtils.concat(serverUrl, imagePath, "de");
    request = new HttpGet(germanUrl);
    testGermanOriginal(request);
  }

  /**
   * Tests requests that send the required language as part of the
   * <code>Accept-Language</code> header.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if an exception occurs
   */
  private void testGetOriginalImageByHeaderLanguage(String serverUrl)
      throws Exception {

    // English
    String url = UrlUtils.concat(serverUrl, imagePath);
    HttpGet request = new HttpGet(url);
    request.setHeader("Accept-Language", "en");
    testEnglishOriginal(request);

    // German
    request = new HttpGet(url);
    request.setHeader("Accept-Language", "de");
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
  private void testGetStyledImageById(String serverUrl) throws Exception {
    List<String> eTags = new ArrayList<String>();
    for (ImageStyle style : styles) {
      String url = UrlUtils.concat(serverUrl, "images", imageId);
      HttpGet getStyleRequest = new HttpGet(url);
      getStyleRequest.getParams().setParameter("style", style.getIdentifier());

      // English
      getStyleRequest.setHeader("Accept-Language", "en");
      testEnglishScaled(getStyleRequest, style, eTags);

      // German
      getStyleRequest.setHeader("Accept-Language", "de");
      testGermanScaled(getStyleRequest, style, eTags);
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
  private void testGetStyledImageByPathLanguage(String serverUrl)
      throws Exception {

    List<String> eTags = new ArrayList<String>();
    for (ImageStyle style : styles) {

      // English
      String englishUrl = UrlUtils.concat(serverUrl, imagePath);
      HttpGet request = new HttpGet(englishUrl);
      request.setHeader("Accept-Language", "en");
      request.getParams().setParameter("style", style.getIdentifier());
      testEnglishScaled(request, style, eTags);

      // German
      String GermanUrl = UrlUtils.concat(serverUrl, imagePath);
      request.setHeader("Accept-Language", "de");
      request = new HttpGet(GermanUrl);
      request.getParams().setParameter("style", style.getIdentifier());
      testGermanScaled(request, style, eTags);
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
  private void testGetStyledImageByHeaderLanguage(String serverUrl)
      throws Exception {
    
    List<String> eTags = new ArrayList<String>();
    for (ImageStyle style : styles) {

      // English
      String englishUrl = UrlUtils.concat(serverUrl, imagePath, "en");
      HttpGet request = new HttpGet(englishUrl);
      request.getParams().setParameter("style", style.getIdentifier());
      testEnglishScaled(request, style, eTags);

      // German
      String GermanUrl = UrlUtils.concat(serverUrl, imagePath, "de");
      request = new HttpGet(GermanUrl);
      request.getParams().setParameter("style", style.getIdentifier());
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
            assertEquals(width, image.getWidth());
            assertEquals(originalHeight * (width / originalWidth), image.getHeight());
            break;
          case Cover:
            assertEquals(originalWidth * (height / originalHeight), image.getWidth());
            assertEquals(height, image.getHeight());
            break;
          case Fill:
            assertEquals(width + 1, image.getWidth());
            assertEquals(height, image.getHeight());
            break;
          case Width:
            assertEquals(width, image.getWidth());
            assertEquals(originalHeight * (width / originalWidth), image.getHeight());
            break;
          case Height:
            assertEquals(originalWidth * (height / originalHeight), image.getWidth());
            assertEquals(height, image.getHeight());
            break;
          case None:
            assertEquals(originalWidth, image.getWidth());
            assertEquals(originalHeight, image.getHeight());
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
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeEnglish, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(sizeEnglish, response.getEntity().getContentLength());
      assertEquals(1, response.getHeaders("Content-Disposition").length);

      // Test filename
      StringBuffer fileName = new StringBuffer(FilenameUtils.getBaseName(filenameEnglish));
      fileName.append("_").append((int)originalWidth).append("x").append((int)originalHeight);
      fileName.append(".").append(FilenameUtils.getExtension(filenameEnglish));
      assertEquals("inline; filename=" + fileName.toString(), response.getHeaders("Content-Disposition")[0].getValue());

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request.addHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", request.toString());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
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
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
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
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // Test ETag support
    httpClient = new DefaultHttpClient();
    try {
      request.addHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", request.toString());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
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
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("Response did not contain any content", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeEnglish, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(1, response.getHeaders("Content-Disposition").length);

      // Test filename
      assertEquals("inline; filename=" + filenameEnglish, response.getHeaders("Content-Disposition")[0].getValue());

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
      request.addHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", request.toString());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
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
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("Response did not contain any content", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(1, response.getHeaders("Content-Disposition").length);

      // Test filename
      StringBuffer fileName = new StringBuffer(FilenameUtils.getBaseName(filenameGerman));
      fileName.append("_").append(style.getWidth()).append("x").append(style.getHeight());
      fileName.append(".").append(FilenameUtils.getExtension(filenameGerman));
      assertEquals("inline; filename=" + fileName.toString(), response.getHeaders("Content-Disposition")[0].getValue());

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
      request.addHeader("If-None-Match", eTagValue);

      logger.info("Sending 'If-None-Match' request to {}", request.toString());
      HttpResponse response = TestSiteUtils.request(httpClient, request, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}
