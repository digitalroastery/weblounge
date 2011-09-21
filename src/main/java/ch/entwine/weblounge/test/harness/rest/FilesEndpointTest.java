package ch.entwine.weblounge.test.harness.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.testing.IntegrationTestBase;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.test.util.TestSiteUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

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

  /** XML Image File to test */
  private static final String imageXMLFile = "/repository/images/test/image/index.xml";

  /** Image File to test */
  private static final String imageFile = "/repository/files/test/document/de.jpg";

  /** Resource identifier */
  private static final String resourceId = "6bc19990-8f99-4873-a813-71b6dfac22ad";

  /** File size of the German version */
  private static final long sizeGerman = 88723L;

  /** Mime type of the German version */
  private static final String mimetypeGerman = "image/jpeg";

  /** File name of the German version */
  private static final String filenameGerman = "porsche.jpg";

  /** File id */
  protected String fileId = null;

  /** File path */
  protected String filePath = null;

  public FilesEndpointTest() {
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

    testGetFileById(serverUrl, resourceId);
    testGetFileByWrongId(serverUrl, "xlkdhfasehfoadui");

    testGetFileContent(serverUrl, resourceId);
    testGetWrongFileContent(serverUrl, resourceId);

    // testCreateFile(serverUrl);
    testUploadFile(serverUrl);
    testUploadFileByPath(serverUrl);

    testUpdateFile(serverUrl);
    testUpdateFileContents(serverUrl);

    testDeleteFileContents(serverUrl);
    testDeleteFile(serverUrl);
  }

  /**
   * Test a file request by id on the server
   * 
   * @param serverUrl
   *          the base url
   * @param id
   *          the file identifier
   * @throws Exception
   *           if get request fails
   */
  private void testGetFileById(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, id));
    logger.debug("Requesting file at {}", getFileRequest.getURI());
    DefaultHttpClient httpClient = new DefaultHttpClient();
    Document pageXml = null;
    String eTagValue;
    String modifiedValue;
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(id, XPathHelper.valueOf(pageXml, "/file/@id"));

      // Test ETag header
      Header eTagHeader = response.getFirstHeader("Etag");
      assertNotNull(eTagHeader);
      assertNotNull(eTagHeader.getValue());
      eTagValue = eTagHeader.getValue();

      Header modifiedHeader = response.getFirstHeader("Last-Modified");
      assertNotNull(modifiedHeader);
      assertNotNull(modifiedHeader.getValue());
      modifiedValue = modifiedHeader.getValue();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    TestSiteUtils.testETagHeader(getFileRequest, eTagValue, logger, null);

    httpClient = new DefaultHttpClient();
    try {
      getFileRequest.removeHeaders("If-None-Match");
      getFileRequest.setHeader("If-Modified-Since", modifiedValue);
      logger.info("Sending 'If-Modified-Since' request to {}", requestUrl);
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusLine().getStatusCode());
      assertNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    httpClient = new DefaultHttpClient();
    try {
      getFileRequest.removeHeaders("If-None-Match");
      getFileRequest.setHeader("If-Modified-Since", "Wed, 10 Feb 1999 21:06:40 GMT");
      logger.info("Sending 'If-Modified-Since' request to {}", requestUrl);
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertNotNull(response.getEntity());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Test a wrong file request by id on the server
   * 
   * @param serverUrl
   *          the base url
   * @param id
   *          the wrong file identifier
   * @throws Exception
   *           if wrong file will be found
   */
  private void testGetFileByWrongId(String serverUrl, String id)
      throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, id));
    logger.debug("Requesting wrong file at {}", getFileRequest.getURI());
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Test a file content request on the server
   * 
   * @param serverUrl
   *          the base url
   * @param id
   *          the file identifier
   * @throws Exception
   *           if wrong file content will be found
   */
  private void testGetFileContent(String serverUrl, String id) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, id, "content", "de"));
    logger.debug("Requesting filecontent at {}", getFileRequest.getURI());
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      assertEquals("inline; filename=" + filenameGerman, response.getHeaders("Content-Disposition")[0].getValue());

      response.getEntity().consumeContent();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Test a wrong file content request on the server
   * 
   * @param serverUrl
   *          the base url
   * @param id
   *          the wrong file identifier
   * @throws Exception
   * 
   */
  private void testGetWrongFileContent(String serverUrl, String id)
      throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, id, "content", "fr"));
    logger.debug("Requesting wrong filecontent at {}", getFileRequest.getURI());
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Creates a new file resource on the server.
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if file creation fails
   */
  private void testCreateFile(String serverUrl) throws Exception {
    String fileXML = IOUtils.toString(getClass().getResourceAsStream(imageXMLFile));

    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpPost createFileRequest = new HttpPost(requestUrl);
    String[][] params = new String[][] { { "resource", fileXML } };
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

    // TODO Test Conflict by path
  }

  /**
   * Upload a new file on the server
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if the file upload fails
   */
  private void testUploadFile(String serverUrl) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpPost uploadFileRequest = new HttpPost(UrlUtils.concat(requestUrl, "/uploads"));

    MultipartEntity multipartEntity = new MultipartEntity();
    multipartEntity.addPart(requestUrl, new InputStreamBody(getClass().getResourceAsStream(imageFile), mimetypeGerman, "de.jpg"));
    multipartEntity.addPart("language", new StringBody("de"));
    uploadFileRequest.setEntity(multipartEntity);

    logger.debug("Creating new file at path {}", uploadFileRequest.getURI());
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = httpClient.execute(uploadFileRequest);
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

    requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId));
    logger.debug("Requesting file at {}", getFileRequest.getURI());
    httpClient = new DefaultHttpClient();
    Document pageXml = null;
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);

      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(fileId, XPathHelper.valueOf(pageXml, "/image/@id"));
      assertNull(XPathHelper.valueOf(pageXml, "/image/@path"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId, "content", "de"));
    logger.debug("Requesting file content at {}", getFileRequest.getURI());
    httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());

      response.getEntity().consumeContent();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Upload a new file to a path on the server
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if the file upload to a path fails
   */
  private void testUploadFileByPath(String serverUrl) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpPost uploadFileRequest = new HttpPost(UrlUtils.concat(requestUrl, "/uploads"));
    MultipartEntity multipartEntity = new MultipartEntity();
    multipartEntity.addPart(requestUrl, new InputStreamBody(getClass().getResourceAsStream(imageFile), mimetypeGerman, "de.jpg"));
    multipartEntity.addPart("language", new StringBody("de"));
    multipartEntity.addPart("path", new StringBody(filePath));
    multipartEntity.addPart("mimeType", new StringBody("image/png"));
    uploadFileRequest.setEntity(multipartEntity);
    logger.debug("Uploading file at path {}", uploadFileRequest.getURI());
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = httpClient.execute(uploadFileRequest);
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

    requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId));
    logger.debug("Requesting file at {}", getFileRequest.getURI());
    httpClient = new DefaultHttpClient();
    Document pageXml = null;
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);

      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(fileId, XPathHelper.valueOf(pageXml, "/image/@id"));
      assertEquals(filePath, XPathHelper.valueOf(pageXml, "/image/@path"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId, "content", "de"));
    logger.debug("Requesting file content at {}", getFileRequest.getURI());
    httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals("image/png", response.getHeaders("Content-Type")[0].getValue());

      response.getEntity().consumeContent();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Update a file resource
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if the file update fails
   */
  private void testUpdateFile(String serverUrl) throws Exception {
    String fileXML = IOUtils.toString(getClass().getResourceAsStream(imageXMLFile));

    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpPut updateFileRequest = new HttpPut(UrlUtils.concat(requestUrl, fileId));
    HttpClient httpClient = new DefaultHttpClient();
    String[][] params = new String[][] { { "content", fileXML } };
    logger.info("Updating file at {}", updateFileRequest.getURI());
    try {
      HttpResponse response = TestUtils.request(httpClient, updateFileRequest, params);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId));
    logger.debug("Requesting file at {}", getFileRequest.getURI());
    httpClient = new DefaultHttpClient();
    Document pageXml = null;
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);

      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(fileId, XPathHelper.valueOf(pageXml, "/image/@id"));
      assertNull(XPathHelper.valueOf(pageXml, "/image/@path"));
      assertNotNull(XPathHelper.valueOf(pageXml, "/image/body/content"));
      assertNull(XPathHelper.valueOf(pageXml, "/image/body/content/@fr"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Update the file content from a resource
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if the file content update fails
   */
  private void testUpdateFileContents(String serverUrl) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpPut updateFileRequest = new HttpPut(UrlUtils.concat(requestUrl, fileId, "content", "fr"));
    HttpClient httpClient = new DefaultHttpClient();
    // TODO
    BasicHttpEntity myEntity = new BasicHttpEntity();
    myEntity.setContent(getClass().getResourceAsStream(imageFile));
    updateFileRequest.setEntity(myEntity);
    logger.info("Updating filecontent at {}", updateFileRequest.getURI());
    try {
      HttpResponse response = TestUtils.request(httpClient, updateFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId));
    logger.debug("Requesting file at {}", getFileRequest.getURI());
    httpClient = new DefaultHttpClient();
    Document pageXml = null;
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);

      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(fileId, XPathHelper.valueOf(pageXml, "/image/@id"));
      assertNotNull(XPathHelper.valueOf(pageXml, "/image/body/content@fr"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    HttpGet getFileContentRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId, "content", "fr"));
    logger.debug("Requesting filecontent at {}", getFileContentRequest.getURI());
    httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileContentRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertEquals(sizeGerman, response.getEntity().getContentLength());
      assertEquals(mimetypeGerman, response.getEntity().getContentType());
      assertEquals(1, response.getHeaders("Content-Disposition").length);
      assertEquals("inline; filename=" + filenameGerman, response.getHeaders("Content-Disposition")[0].getValue());
      response.getEntity().consumeContent();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Delete a file content from a file resource
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if the file content deletion fails
   */
  private void testDeleteFileContents(String serverUrl) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpDelete deleteContentRequest = new HttpDelete(UrlUtils.concat(requestUrl, fileId, "content", "de"));
    logger.debug("Deleting filecontent at {}", deleteContentRequest.getURI());
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, deleteContentRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId));
    logger.debug("Requesting file at {}", getFileRequest.getURI());
    httpClient = new DefaultHttpClient();
    Document pageXml = null;
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertTrue(response.getEntity().getContentLength() > 0);
      assertEquals(mimetypeGerman, response.getHeaders("Content-Type")[0].getValue());

      pageXml = TestUtils.parseXMLResponse(response);
      assertEquals(fileId, XPathHelper.valueOf(pageXml, "/image/@id"));
      assertNull(XPathHelper.valueOf(pageXml, "/image/body/content"));
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Delete a file resource
   * 
   * @param serverUrl
   *          the base url
   * @throws Exception
   *           if the file deletion fails
   */
  private void testDeleteFile(String serverUrl) throws Exception {
    String requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpDelete deleteContentRequest = new HttpDelete(UrlUtils.concat(requestUrl, fileId, "content", "de"));
    logger.debug("Deleting file at {}", deleteContentRequest.getURI());
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, deleteContentRequest, null);
      assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    requestUrl = UrlUtils.concat(serverUrl, FILES_ENDPOINT_PATH);
    HttpGet getFileRequest = new HttpGet(UrlUtils.concat(requestUrl, fileId));
    logger.debug("Requesting file at {}", getFileRequest.getURI());
    httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, getFileRequest, null);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
      assertEquals(0, response.getEntity().getContentLength());
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}
