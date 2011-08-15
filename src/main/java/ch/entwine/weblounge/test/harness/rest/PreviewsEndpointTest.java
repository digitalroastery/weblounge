package ch.entwine.weblounge.test.harness.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.site.ImageScalingMode;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test to test the content repository's <code>restful</code>
 * previews api.
 */
public class PreviewsEndpointTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(PreviewsEndpointTest.class);
  
  /** The endpoint's url */
  private static final String baseURL = "system/weblounge/previews";
  
  /** Mime type of the English version */
  private static final String mimetype = "image/jpeg";
  
  /** Image resource identifier */
  private static final String imageId = "5bc19990-8f99-4873-a813-71b6dfac22ad";
  
  /** Image resource identifier */
  private static final String pageId = "4bb19980-8f98-4873-a813-000000000001";

  protected PreviewsEndpointTest() {
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
    
    ImageStyleImpl imageStyle = new ImageStyleImpl("box", 250, 250, ImageScalingMode.Box, false);

    // Test to preview an image
    testPreview(requestUrl, imageId, imageStyle);

    // Test to preview a page
    testPreview(requestUrl, pageId, imageStyle);
    
    // Test for non exist resource
    testNoneExist(requestUrl, imageStyle);
  }

  private void testPreview(String serverUrl, String resourceId, ImageStyleImpl imageStyle) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, resourceId, "locales", "en", "styles", imageStyle.getIdentifier());
    HttpGet getPreviewRequest = new HttpGet(requestUrl);
    HttpClient httpClient = new DefaultHttpClient();
    String eTagValue = null;
    logger.info("Requesting image preview at {}", requestUrl);
    try {
       HttpResponse response = TestUtils.request(httpClient, getPreviewRequest, null);
       assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
       assertTrue("No content received", response.getEntity().getContentLength() > 0);
       assertEquals(mimetype, response.getHeaders("Content-Type")[0].getValue());
       
       // Test ETag header
       Header eTagHeader = response.getFirstHeader("Etag");
       assertNotNull(eTagHeader);
       assertNotNull(eTagHeader.getValue());
       eTagValue = eTagHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    
    TestUtils.testETagHeader(getPreviewRequest, eTagValue, logger);
    TestUtils.testModifiedHeader(getPreviewRequest, logger);
  }

  private void testNoneExist(String serverUrl, ImageStyleImpl imageStyle) throws Exception {
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
