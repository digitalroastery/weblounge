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

package ch.entwine.weblounge.common.impl.testing;

import ch.entwine.weblounge.common.impl.url.UrlUtils;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This integration test is automatically created from a test definition using
 * the {@link IntegrationTestParser}.
 */
public class IntegrationTestCase {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(IntegrationTestCase.class);

  /** Name of the test case */
  protected String name = null;

  /** The request url and query */
  protected String path = null;

  /** The list of assertions */
  protected List<IntegrationTestCaseAssertion> assertions = new ArrayList<IntegrationTestCaseAssertion>();

  /** The request parameters */
  protected Map<String, String[]> parameters = new HashMap<String, String[]>();

  /**
   * Creates a test case with the given name and url. The parameters will be
   * added to the request, <code>null</code> is an acceptable value if no
   * parameters are needed.
   * 
   * @param name
   *          the test name
   * @param path
   *          the test path
   * @param the
   *          query parameters
   * @throws IllegalArgumentException
   *           if <code>name</code> or <code>url</code> are empty
   */
  public IntegrationTestCase(String name, String path,
      Map<String, String[]> parameters) {
    if (StringUtils.isBlank(name))
      throw new IllegalArgumentException("Name of test case cannot be empty");
    if (StringUtils.isBlank(path))
      throw new IllegalArgumentException("Url cannot be empty");
    this.name = name;
    this.path = path;
    this.parameters = parameters;
  }

  /**
   * Executes this test case using the given server url. The server url will be
   * prepended to the test case's url and then used in an http <code>GET</code>
   * request. The response is then analyzed using the given assertions.
   */
  public void execute(String serverUrl) throws Exception {

    HttpGet request = new HttpGet(UrlUtils.concat(serverUrl, path));
    String[][] params = new String[][] {};
    if (this.parameters != null) {
      int parameterCount = 0;
      for (String[] parameterValues : this.parameters.values()) {
        parameterCount += parameterValues.length;
      }
      params = new String[parameterCount][2];
      int i = 0;
      for (Map.Entry<String, String[]> param : this.parameters.entrySet()) {
        for (String value : param.getValue()) {
          params[i][0] = param.getKey();
          params[i][1] = value;
          i++;
        }
      }
    }

    // Send and the request and examine the response
    logger.debug("Sending request to {}", request.getURI());
    HttpClient httpClient = new DefaultHttpClient();
    try {
      HttpResponse response = TestUtils.request(httpClient, request, params);

      // Prepare status code and response body
      int code = response.getStatusLine().getStatusCode();
      Document xml = TestUtils.parseXMLResponse(response);

      // Prepare the headers collection
      Map<String, String> headers = new HashMap<String, String>();
      Header[] responseHeaders = response.getAllHeaders();
      if (responseHeaders != null) {
        for (Header header : responseHeaders) {
          headers.put(header.getName(), header.getValue());
        }
      }

      // Verify the assertions
      for (IntegrationTestCaseAssertion assertion : assertions) {
        assertion.verify(code, headers, xml);
      }

    } finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

  /**
   * Returns the assertions that have been registered.
   * 
   * @return the assertions
   */
  public List<IntegrationTestCaseAssertion> getAssertions() {
    return assertions;
  }

  /**
   * Returns the request parameters.
   * 
   * @return the parameters
   */
  public Map<String, String[]> getParameters() {
    return parameters;
  }

  /**
   * Tests if the http response code matches any of the given values. By
   * default, the test requires a status code of <code>200</code>.
   * 
   * @param statusCodes
   *          the acceptable status codes
   * @throws IllegalArgumentException
   *           if no status codes are given
   */
  public void assertResponseStatus(int[] statusCodes)
      throws IllegalArgumentException {
    if (statusCodes == null || statusCodes.length == 0)
      throw new IllegalArgumentException("At least one code si required");
    assertions.add(new StatusCodeAssertion(statusCodes));
  }

  /**
   * Adds a test that asserts that the given path exists and does not contain an
   * empty element.
   * 
   * @param xpath
   *          path to the element in question
   * @throws IllegalArgumentException
   *           if <code>xpath</code> is blank
   */
  public void assertExists(String xpath) throws IllegalArgumentException {
    if (StringUtils.isBlank(xpath))
      throw new IllegalArgumentException("Path must not be blank");
    assertions.add(new ExistenceAssertion(xpath, true));
  }

  /**
   * Adds a test that asserts that there is no (or an empty) element at the
   * given path.
   * 
   * @param xpath
   *          path to the element in question
   * @throws IllegalArgumentException
   *           if <code>xpath</code> is blank
   */
  public void assertNotExists(String xpath) throws IllegalArgumentException {
    if (StringUtils.isBlank(xpath))
      throw new IllegalArgumentException("Path must not be blank");
    assertions.add(new ExistenceAssertion(xpath, false));
  }

  /**
   * Adds a test that asserts that there is an element at the given path and
   * that the element content matches the given value.
   * 
   * @param xpath
   *          path to the element in question
   * @param value
   *          the value
   * @param ignoreWhitespace
   *          <code>true</code> to ignore whitespace when matching
   * @param ignoreCase
   *          <code>true</code> to ignore character case when matching
   * @throws IllegalArgumentException
   *           if <code>xpath</code> is blank
   */
  public void assertEquals(String xpath, String value,
      boolean ignoreWhitespace, boolean ignoreCase)
      throws IllegalArgumentException {
    if (StringUtils.isBlank(xpath))
      throw new IllegalArgumentException("Path must not be blank");
    if (StringUtils.isBlank(value))
      throw new IllegalArgumentException("Value must not be blank");
    assertions.add(new EqualityAssertion(xpath, value, ignoreWhitespace, ignoreCase, true));
  }

  /**
   * Adds a test that asserts that there is an element at the given path and
   * that the element content does not matches the given value.
   * 
   * @param xpath
   *          path to the element in question
   * @param value
   *          the value
   * @param ignoreWhitespace
   *          <code>true</code> to ignore whitespace when matching
   * @param ignoreCase
   *          <code>true</code> to ignore character case when matching
   * @throws IllegalArgumentException
   *           if <code>xpath</code> is blank
   */
  public void assertNotEquals(String xpath, String value,
      boolean ignoreWhitespace, boolean ignoreCase)
      throws IllegalArgumentException {
    if (StringUtils.isBlank(xpath))
      throw new IllegalArgumentException("Path must not be blank");
    if (StringUtils.isBlank(value))
      throw new IllegalArgumentException("Value must not be blank");
    assertions.add(new EqualityAssertion(xpath, value, ignoreWhitespace, ignoreCase, false));
  }

  /**
   * Implementation of an assertion that tests the status code to be in a
   * predefined list of codes.
   */
  public class StatusCodeAssertion implements IntegrationTestCaseAssertion {

    /** The list of acceptable status codes */
    protected List<Integer> statusCodes = new ArrayList<Integer>();

    /**
     * Creates an assertion that verifies whether the response status matches
     * one of the status codes given.
     * 
     * @param statusCodes
     *          the expected status codes
     */
    StatusCodeAssertion(int[] statusCodes) {
      for (int code : statusCodes) {
        this.statusCodes.add(code);
      }
    }

    /**
     * {@inheritDoc}
     * 
     * @see ch.entwine.weblounge.common.impl.testing.IntegrationTestCaseAssertion#verify(int,
     *      java.util.Map, org.w3c.dom.Node)
     */
    public void verify(int statusCode, Map<String, String> headers,
        Node response) throws Exception {
      if (!statusCodes.contains(statusCode))
        throw new IllegalStateException("Unexpected response code " + statusCode);
    }

    /**
     * Returns a list of expected status codes.
     * 
     * @return the status codes
     */
    public List<Integer> getExpectedCodes() {
      return statusCodes;
    }

  }

  /**
   * Assertion that will verify that the response contains given
   * <code>xpath</code> expression.
   */
  public class ExistenceAssertion implements IntegrationTestCaseAssertion {

    /** The path to test for */
    private String xpath = null;

    /** <code>true</code> to test for existence */
    private boolean testPositive = true;

    /**
     * Creates a new assertion that tests for existence (
     * <code>testPositive</code> is <code>true</code>) or non-existence (
     * <code>testPositive</code> is <code>false</code>) of the element defined
     * by <code>xpath</code>.
     * 
     * @param xpath
     *          the xpath to the element
     * @param testPositive
     *          <code>true</code> to test for existence, <code>false</code> for
     *          non-existence
     */
    ExistenceAssertion(String xpath, boolean testPositive) {
      this.xpath = xpath;
      this.testPositive = testPositive;
    }

    /**
     * {@inheritDoc}
     * 
     * @see ch.entwine.weblounge.common.impl.testing.IntegrationTestCaseAssertion#verify(int,
     *      java.util.Map, org.w3c.dom.Node)
     */
    public void verify(int statusCode, Map<String, String> headers,
        Node response) throws Exception {
      boolean isBlank = StringUtils.isBlank(XPathHelper.valueOf(response, xpath));
      if (testPositive && isBlank)
        throw new IllegalStateException("Expected content at " + xpath + " not found");
      else if (!testPositive && !isBlank)
        throw new IllegalStateException("Found unexpected content at " + xpath);
    }

    /**
     * Returns the xpath expression.
     * 
     * @return the xpath
     */
    public String getXPath() {
      return xpath;
    }

    /**
     * Returns <code>true</code> if the test is testing for positive outcome.
     * 
     * @return <code>true</code> for testing of positive outcome
     */
    public boolean isPositive() {
      return testPositive;
    }

  }

  /**
   * Assertion that will verify that the response contains an element at the
   * given <code>xpath</code> expression that matches an expected value.
   */
  public class EqualityAssertion implements IntegrationTestCaseAssertion {

    /** The path to test for */
    private String xpath = null;

    /** The value to look for */
    private String expectedValue = null;

    /** True to ignore whitespace */
    private boolean ignoreWhitespace = true;

    /** True to ignore the case when matching */
    private boolean ignoreCase = true;

    /** <code>true</code> to test for existence */
    private boolean testPositive = true;

    /**
     * Creates a new assertion that tests for equality (
     * <code>testPositive</code> is <code>true</code>) or mismatch (
     * <code>testPositive</code> is <code>false</code>) of the element defined
     * by <code>xpath</code>.
     * 
     * @param xpath
     *          the xpath to the element
     * @param value
     *          the content to match
     * @param ignoreWhitespace
     *          <code>true</code> to ignore whitespace when matching
     * @param ignoreCase
     *          <code>true</code> to ignore case when matching
     * @param testPositive
     *          <code>true</code> to test for equality, <code>false</code> for
     *          mismatch
     */
    EqualityAssertion(String xpath, String value, boolean ignoreWhitespace,
        boolean ignoreCase, boolean testPositive) {
      this.xpath = xpath;
      this.ignoreWhitespace = ignoreWhitespace;
      this.ignoreCase = ignoreCase;
      this.testPositive = testPositive;
      this.expectedValue = value;
    }

    /**
     * {@inheritDoc}
     * 
     * @see ch.entwine.weblounge.common.impl.testing.IntegrationTestCaseAssertion#verify(int,
     *      java.util.Map, org.w3c.dom.Node)
     */
    public void verify(int statusCode, Map<String, String> headers,
        Node response) throws Exception {
      String actualValue = XPathHelper.valueOf(response, xpath);
      String expected = this.expectedValue;
      String found = actualValue;
      if (actualValue == null)
        throw new IllegalStateException("Expected content at " + xpath + " not found");
      if (ignoreWhitespace) {
        found = StringUtils.deleteWhitespace(found);
        expected = StringUtils.deleteWhitespace(expected);
      }
      boolean matches = ignoreCase ? found.equalsIgnoreCase(expected) : found.equals(expected);
      if (testPositive && !matches)
        throw new IllegalStateException("Expected '" + this.expectedValue + "' at " + xpath + " but found '" + actualValue + "'");
      if (!testPositive && matches)
        throw new IllegalStateException("Found unexpected content '" + actualValue + "' at " + xpath);
    }

    /**
     * Returns the xpath expression.
     * 
     * @return the xpath
     */
    public String getXPath() {
      return xpath;
    }

    /**
     * Returns <code>true</code> if the test should ignore whitespace.
     * 
     * @return <code>true</code> if the test ignores whitespace
     */
    public boolean ignoreWhitespace() {
      return ignoreWhitespace;
    }

    /**
     * Returns <code>true</code> if the test should ignore case.
     * 
     * @return <code>true</code> if the test ignores case
     */
    public boolean ignoreCase() {
      return ignoreCase;
    }

    /**
     * Returns <code>true</code> if the test is testing for positive outcome.
     * 
     * @return <code>true</code> for testing of positive outcome
     */
    public boolean isPositive() {
      return testPositive;
    }

    /**
     * Returns the expected value.
     * 
     * @return the expected value
     */
    public String getExpectedValue() {
      return expectedValue;
    }

  }

}
