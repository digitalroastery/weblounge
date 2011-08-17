package ch.entwine.weblounge.test.harness.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.site.ImageScalingMode;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.test.util.TestSiteUtils;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
 * Integration test to test the content repository's <code>restful</code>
 * previews api.
 */
public class PreviewsEndpointTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(PreviewsEndpointTest.class);

  /** The scaling modes to test */
  private static final List<ImageStyle> styles = new ArrayList<ImageStyle>();

  /** The endpoint's url */
  private static final String baseURL = "system/weblounge/previews";

  /** The original image's width */
  private static final int originalWidth = 1000;

  /** The original image's height */
  private static final int originalHeight = 666;

  /** File name of the English version */
  private static final String filename = "porsche.jpg";

  /** The style's width */
  private static final int BOX_WIDTH = 250;

  /** The style's height */
  private static final int BOX_HEIGHT = 250;
  
  /** The style's width */
  private static final int PREVIEW_WIDTH = 300;
  
  /** The style's height */
  private static final int PREVIEW_HEIGHT = 200;

  /** Mime type of the English version */
  private static final String mimetypeJpeg = "image/jpeg";
  
  /** Mime type of the English version */
  private static final String mimetypePng = "image/png";

  /** Image resource identifier */
  private static final String imageId = "5bc19990-8f99-4873-a813-71b6dfac22ad";

  /** Image resource identifier */
  private static final String pageId = "4bb19980-8f98-4873-a813-000000000001";

  static {
    styles.add(new ImageStyleImpl("box", BOX_WIDTH, BOX_HEIGHT, ImageScalingMode.Box, false));
    styles.add(new ImageStyleImpl("cover", BOX_WIDTH, BOX_HEIGHT, ImageScalingMode.Cover, false));
    styles.add(new ImageStyleImpl("fill", BOX_WIDTH, BOX_HEIGHT, ImageScalingMode.Fill, false));
    styles.add(new ImageStyleImpl("width", BOX_WIDTH, -1, ImageScalingMode.Width, false));
    styles.add(new ImageStyleImpl("height", -1, BOX_HEIGHT, ImageScalingMode.Height, false));
    styles.add(new ImageStyleImpl("none", -1, -1, ImageScalingMode.None, false));
    styles.add(new ImageStyleImpl("weblounge-ui:preview", PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageScalingMode.Cover, false));
  }

  public PreviewsEndpointTest() {
    super("Previews Endpoint Test", WEBLOUNGE_ENDPOINT_TEST_GROUP);
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
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of previews endpoint");

    String requestUrl = UrlUtils.concat(serverUrl, baseURL);

    testGetImageStyles(requestUrl);
    testGetImageStyle(requestUrl);

    // Test to preview a image
    for (ImageStyle style : styles) {
      testImagePreview(requestUrl, imageId, style);
    }

    // Test to preview a page
    for (ImageStyle style : styles) {
      testPagePreview(requestUrl, pageId, style);
    }

    // Test for non exist resource
    testNoneExist(requestUrl, styles.get(0));
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

  private void testImagePreview(String serverUrl, String resourceId,
      ImageStyle imageStyle) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, resourceId, "locales", "en", "styles", imageStyle.getIdentifier());
    HttpGet getPreviewRequest = new HttpGet(requestUrl);
    HttpClient httpClient = new DefaultHttpClient();
    String eTagValue = null;
    logger.info("Requesting image preview at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPreviewRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue("No content received", response.getEntity().getContentLength() > 0);

      // Test general headers
      assertEquals(mimetypeJpeg, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(1, response.getHeaders("Content-Type").length);
      assertEquals(1, response.getHeaders("Content-Disposition").length);

      SeekableStream seekableInputStream = null;
      StringBuilder fileName = new StringBuilder(FilenameUtils.getBaseName(filename));
      try {
        // Test file size
        if (!ImageScalingMode.None.equals(imageStyle.getScalingMode())) {
          float scale = ImageStyleUtils.getScale(originalWidth, originalHeight, imageStyle);
          float scaledWidth = originalWidth * scale - ImageStyleUtils.getCropX(originalWidth * scale, originalHeight * scale, imageStyle);
          float scaledHeight = originalHeight * scale - ImageStyleUtils.getCropY(originalWidth * scale, originalHeight * scale, imageStyle);

          // Load the image from the given input stream
          seekableInputStream = new MemoryCacheSeekableStream(response.getEntity().getContent());
          RenderedOp image = JAI.create("stream", seekableInputStream);
          if (image == null)
            throw new IOException("Error reading image from input stream");

          // Get the original image size
          int imageWidth = image.getWidth();
          int imageHeight = image.getHeight();
          assertTrue((int)(scaledHeight) == imageHeight || (int)(scaledHeight) + 1 == imageHeight || (int)(scaledHeight) - 1 == imageHeight);
          assertTrue((int)(scaledWidth) == imageWidth || (int)(scaledWidth) + 1 == imageWidth || (int)(scaledWidth) - 1 == imageWidth);
          fileName.append("-en");
        } else {
          response.getEntity().consumeContent();
        }
      } finally {
        IOUtils.closeQuietly(seekableInputStream);
      }

      // Test filename
      fileName.append(".").append(FilenameUtils.getExtension(filename));
      String contentDisposition = response.getHeaders("Content-Disposition")[0].getValue();
      assertTrue(contentDisposition.startsWith("inline; filename=" + fileName.toString()));

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();

    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    TestSiteUtils.testETagHeader(getPreviewRequest, eTagValue, logger);
    TestSiteUtils.testModifiedHeader(getPreviewRequest, logger);
  }
  
  private void testPagePreview(String serverUrl, String resourceId,
      ImageStyle imageStyle) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, resourceId, "locales", "fr", "styles", imageStyle.getIdentifier());
    HttpGet getPreviewRequest = new HttpGet(requestUrl);
    HttpClient httpClient = new DefaultHttpClient();
    String eTagValue = null;
    logger.info("Requesting image preview at {}", requestUrl);
    HttpResponse response = TestUtils.request(httpClient, getPreviewRequest, null);
      if (ImageScalingMode.None.equals(imageStyle.getScalingMode())) {
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
        assertTrue("No content received", response.getEntity().getContentLength() < 1);
        response.getEntity().consumeContent();
      } else {
        try {
          assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
          assertTrue("No content received", response.getEntity().getContentLength() > 0);
          
          // Test general headers
          assertEquals(mimetypePng, response.getHeaders("Content-Type")[0].getValue());
          assertEquals(1, response.getHeaders("Content-Type").length);
          assertEquals(1, response.getHeaders("Content-Disposition").length);
          
          // Test filename
          StringBuilder fileName = new StringBuilder(pageId).append("-fr.png");
          String contentDisposition = response.getHeaders("Content-Disposition")[0].getValue();
          assertTrue(contentDisposition.startsWith("inline; filename=" + fileName.toString()));
          
          // Test ETag header
          Header eTagHeader = response.getFirstHeader("Etag");
          assertNotNull(eTagHeader);
          assertNotNull(eTagHeader.getValue());
          eTagValue = eTagHeader.getValue();
      } finally {
        httpClient.getConnectionManager().shutdown();
      }
      TestSiteUtils.testETagHeader(getPreviewRequest, eTagValue, logger);
      TestSiteUtils.testModifiedHeader(getPreviewRequest, logger);
    }
  }

  private void testNoneExist(String serverUrl, ImageStyle imageStyle)
      throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, "121083183-adf032", "locales", "en", "styles", imageStyle.getIdentifier());
    HttpGet getPreviewRequest = new HttpGet(requestUrl);
    HttpClient httpClient = new DefaultHttpClient();
    logger.info("Requesting non existing resource preview at {}", requestUrl);
    try {
      HttpResponse response = TestUtils.request(httpClient, getPreviewRequest, null);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}
