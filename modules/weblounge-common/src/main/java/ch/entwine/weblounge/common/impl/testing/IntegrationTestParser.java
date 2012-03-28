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

import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.impl.util.xml.XPathNamespaceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This class parses integration test definitions and creates the corresponding
 * integration test instances.
 */
public final class IntegrationTestParser {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(IntegrationTestParser.class);

  /** Xml namespace for the module */
  public static final String TEST_XMLNS = "http://www.entwinemedia.com/weblounge/3.0/test";

  /**
   * This utility class is not intended to be instantiated.
   */
  private IntegrationTestParser() {
    // Nothing to do
  }

  /**
   * Initializes an integration test object from the given test definition.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param config
   *          the test node
   * @throws IllegalStateException
   *           if the test cannot be parsed
   * @see #fromXml(Node, XPath)
   */
  public static IntegrationTestGroup fromXml(Node config)
      throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();

    // Define the xml namespace
    XPathNamespaceContext nsCtx = new XPathNamespaceContext(false);
    nsCtx.defineNamespaceURI("m", TEST_XMLNS);
    xpath.setNamespaceContext(nsCtx);

    return fromXml(config, xpath);
  }

  /**
   * Initializes an integration test object from the given test definition.
   * 
   * @param config
   *          the test node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the test cannot be parsed
   */
  public static IntegrationTestGroup fromXml(Node config, XPath xpathProcessor)
      throws IllegalStateException {

    // Create the test group
    IntegrationTestGroup testGroup = null;
    String name = XPathHelper.valueOf(config, "/test/name", xpathProcessor);
    if (name == null)
      throw new IllegalStateException("Unable to create test without identifier");
    testGroup = new IntegrationTestGroup(name);

    // Get the test cases
    NodeList testCaseNodes = XPathHelper.selectList(config, "/test/test-case");
    if (testCaseNodes == null || testCaseNodes.getLength() == 0) {
      logger.warn("Found test definition without test cases");
      return testGroup;
    }

    for (int i = 0; i < testCaseNodes.getLength(); i++) {
      Node testCaseNode = testCaseNodes.item(i);

      // Name, url and query
      String testCaseName = XPathHelper.valueOf(testCaseNode, "name", xpathProcessor);
      String url = XPathHelper.valueOf(testCaseNode, "url", xpathProcessor);

      // Parameters
      Map<String, String[]> parameters = new HashMap<String, String[]>();
      NodeList parameterNodes = XPathHelper.selectList(testCaseNode, "parameter");
      if (parameterNodes != null) {
        for (int j = 0; j < parameterNodes.getLength(); j++) {
          Node parameterNode = parameterNodes.item(j);
          String parameterName = XPathHelper.valueOf(parameterNode, "@name");
          String parameterValue = XPathHelper.valueOf(parameterNode, "text()");
          String[] values = parameters.get(parameterName);
          if (values == null) {
            parameters.put(parameterName, new String[] { parameterValue });
          } else {
            String[] newValues = new String[values.length + 1];
            for (int v = 0; v < values.length; v++)
              newValues[v] = values[v];
            newValues[newValues.length - 1] = parameterValue;
          }
        }
      }

      IntegrationTestCase testCase = new IntegrationTestCase(testCaseName, url, parameters);

      // Status codes
      String expectedCodes = XPathHelper.valueOf(testCaseNode, "assert-status", xpathProcessor);
      if (StringUtils.isNotBlank(expectedCodes)) {
        String[] codeTexts = expectedCodes.split(",");
        int[] codes = new int[codeTexts.length];
        int v = 0;
        for (String code : codeTexts) {
          codes[v++] = Integer.parseInt(StringUtils.trim(code));
        }
        testCase.assertResponseStatus(codes);
      }

      // Assert existence
      NodeList existenceNodes = XPathHelper.selectList(testCaseNode, "assert-exists");
      if (existenceNodes != null) {
        for (int j = 0; j < existenceNodes.getLength(); j++) {
          Node node = existenceNodes.item(j);
          String path = XPathHelper.valueOf(node, "path");
          testCase.assertExists(path);
        }
      }

      // Assert non-existence
      NodeList missingNodes = XPathHelper.selectList(testCaseNode, "assert-not-exists");
      if (missingNodes != null) {
        for (int j = 0; j < missingNodes.getLength(); j++) {
          Node node = missingNodes.item(j);
          String path = XPathHelper.valueOf(node, "path");
          testCase.assertNotExists(path);
        }
      }

      // Assert equality
      NodeList matchNodes = XPathHelper.selectList(testCaseNode, "assert-equals");
      if (matchNodes != null) {
        for (int j = 0; j < matchNodes.getLength(); j++) {
          Node node = matchNodes.item(j);
          String path = XPathHelper.valueOf(node, "path");
          String value = XPathHelper.valueOf(node, "value");
          String whitespace = XPathHelper.valueOf(node, "@ignorewhitespace");
          String casesensitivity = XPathHelper.valueOf(node, "@ignorecase");
          String regex = XPathHelper.valueOf(node, "@regularexpression");
          boolean ignoreWhitespace = ConfigurationUtils.isTrue(whitespace);
          boolean ignoreCase = ConfigurationUtils.isTrue(casesensitivity);
          boolean regularExpression = ConfigurationUtils.isTrue(regex);
          testCase.assertEquals(path, value, ignoreWhitespace, ignoreCase, regularExpression);
        }
      }

      // Assert non-equality
      NodeList mismatchNodes = XPathHelper.selectList(testCaseNode, "assert-not-equals");
      if (mismatchNodes != null) {
        for (int j = 0; j < mismatchNodes.getLength(); j++) {
          Node node = mismatchNodes.item(j);
          String path = XPathHelper.valueOf(node, "path");
          String value = XPathHelper.valueOf(node, "value");
          String whitespace = XPathHelper.valueOf(node, "@ignorewhitespace");
          String casesensitivity = XPathHelper.valueOf(node, "@ignorecase");
          String regex = XPathHelper.valueOf(node, "@regularexpression");
          boolean ignoreWhitespace = ConfigurationUtils.isTrue(whitespace);
          boolean ignoreCase = ConfigurationUtils.isTrue(casesensitivity);
          boolean regularExpression = ConfigurationUtils.isTrue(regex);
          testCase.assertNotEquals(path, value, ignoreWhitespace, ignoreCase, regularExpression);
        }
      }

      testGroup.addTestCase(testCase);
    }

    return testGroup;
  }

}
