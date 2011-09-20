package ch.entwine.weblounge.test.harness.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration test to test the content repository's <code>restful</code> page
 * api.
 */
public class FilesEndpointTest extends IntegrationTestBase {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(FilesEndpointTest.class);

  /** The path to the files endpoint */
  private static final String FILES_ENDPOINT_PATH = "system/weblounge/files";
  
  /** File id */
  protected String fileId = null;

  /** File path */
  protected String filePath = null;

  protected FilesEndpointTest() {
    super("Files Endpoint Test", WEBLOUNGE_ENDPOINT_TEST_GROUP);
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
   * @see ch.entwine.weblounge.common.impl.testing.IntegrationTestBase#execute(java.lang.String)
   */
  @Override
  public void execute(String serverUrl) throws Exception {
    logger.info("Preparing test of files endpoint");

    filePath = "/" + System.currentTimeMillis() + "/";

    testUploadFile(serverUrl);
    testUpdateFile(serverUrl);
    testUpdateFileContents(serverUrl);
    testDeleteFileContents(serverUrl);
    testDeleteFile(serverUrl);
  }

  /**
   * Creates a new file on the server.
   * 
   * @param serverUrl
   *          the base url
   * @param return the resource identifier of the new file
   * @throws Exception
   *           if file creation fails
   */
  private String testUploadFile(String serverUrl) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpPost createFileRequest = new HttpPost(requestUrl);
    String[][] params = new String[][] { { "language", "de" } };
    logger.debug("Creating new file at {}", createFileRequest.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, createFileRequest, params);
      assertEquals(HttpServletResponse.SC_CREATED, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());

      // Extract the id of the new page
      assertNotNull(response.getHeaders("Location"));
      String locationHeader = response.getHeaders("Location")[0].getValue();
      assertTrue(locationHeader.startsWith(serverUrl));
      fileId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
      assertEquals("Identifier doesn't have correct length", 36, fileId.length());
      logger.debug("Id of the new file is {}", fileId);
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    // TODO: Test automatic mime type extraction

    // TODO: Test existing path

    // TODO: Test exceeding of size limit
    return null;
  }

  /**
   * @param serverUrl
   *          the base url
   */
  private void testUpdateFileContents(String serverUrl) {
    // TODO Auto-generated method stub

  }

  /**
   * @param serverUrl
   *          the base url
   */
  private void testUpdateFile(String serverUrl) {
    // TODO Auto-generated method stub

  }

  /**
   * @param serverUrl
   *          the base url
   */
  private void testDeleteFileContents(String serverUrl) {
    // TODO Auto-generated method stub

  }

  /**
   * @param serverUrl
   *          the base url
   */
  private void testDeleteFile(String serverUrl) {
    // TODO Auto-generated method stub

  }

}
