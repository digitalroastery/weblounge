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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.site.SiteImpl;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestCase.EqualityAssertion;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestCase.ExistenceAssertion;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestCase.StatusCodeAssertion;
import ch.entwine.weblounge.common.impl.util.xml.ValidationErrorHandler;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * Test case for {@link IntegrationTestParserTest}.
 */
public class IntegrationTestParserTest {

  /** Name of the test definition file */
  protected static String testFile = "/integrationtest.xml";

  /** The test group */
  protected static IntegrationTestGroup testGroup = null;

  /** The test cases */
  protected static List<IntegrationTestCase> testCases = null;

  /**
   * Reads the integration test from the resources section.
   * 
   * @throws Exception
   *           if parsing fails
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    // Schema validator setup
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL schemaUrl = SiteImpl.class.getResource("/xsd/test.xsd");
    Schema siteSchema = schemaFactory.newSchema(schemaUrl);

    // Module.xml document builder setup
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setSchema(siteSchema);
    docBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    // Validate and read the module descriptor
    URL testContext = IntegrationTestParserTest.class.getResource(testFile);
    ValidationErrorHandler errorHandler = new ValidationErrorHandler(testContext);
    docBuilder.setErrorHandler(errorHandler);
    Document doc = docBuilder.parse(testContext.openStream());
    assertFalse("Schema validation failed", errorHandler.hasErrors());

    // Finally, parse the test
    testGroup = IntegrationTestParser.fromXml(doc.getFirstChild());
    testCases = testGroup.getTestCases();
  }

  /**
   * Tests parsing of assertions.
   */
  @Test
  public void testCases() {
    assertNotNull(testCases);
    assertEquals(2, testCases.size());
  }

  /**
   * Tests parsing of assertions.
   */
  @Test
  public void testAssertions() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertEquals(6, assertions.size());
  }

  /**
   * Tests parsing of <code>assert-status</code> clauses.
   */
  @Test
  public void testStatusAssertion() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertTrue(assertions.get(0) instanceof StatusCodeAssertion);
    StatusCodeAssertion assertion = (StatusCodeAssertion) assertions.get(0);
    List<Integer> expectedCodes = assertion.getExpectedCodes();
    assertEquals(2, expectedCodes.size());
    assertEquals(200, expectedCodes.get(0));
    assertEquals(304, expectedCodes.get(1));
  }

  /**
   * Tests parsing of <code>assert-exists</code> clauses.
   */
  @Test
  public void testAssertExists() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertTrue(assertions.get(1) instanceof ExistenceAssertion);
    ExistenceAssertion assertion = (ExistenceAssertion) assertions.get(1);
    assertTrue(assertion.isPositive());
    assertEquals("//div[@id='main']/h1", assertion.getXPath());
  }

  /**
   * Tests parsing of <code>assert-not-exists</code> clauses.
   */
  @Test
  public void testAssertNotExists() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertTrue(assertions.get(2) instanceof ExistenceAssertion);
    ExistenceAssertion assertion = (ExistenceAssertion) assertions.get(2);
    assertFalse(assertion.isPositive());
    assertEquals("//div[@id='main']/h2", assertion.getXPath());
  }

  /**
   * Tests parsing of <code>assert-equals</code> clauses.
   */
  @Test
  public void testAssertEquals() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertTrue(assertions.get(3) instanceof EqualityAssertion);
    EqualityAssertion assertion = (EqualityAssertion) assertions.get(3);
    assertTrue(assertion.isPositive());
    assertTrue(assertion.ignoreWhitespace());
    assertTrue(assertion.ignoreCase());
    assertFalse(assertion.regularExpression());
    assertEquals("//div[@id='main']/h1", assertion.getXPath());
    assertEquals("hello world i am happy today", assertion.getExpectedValue());
  }

  /**
   * Tests parsing of <code>assert-not-equals</code> clauses.
   */
  @Test
  public void testAssertNotEquals() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertTrue(assertions.get(5) instanceof EqualityAssertion);
    EqualityAssertion assertion = (EqualityAssertion) assertions.get(5);
    assertFalse(assertion.isPositive());
    assertTrue(assertion.ignoreWhitespace());
    assertFalse(assertion.ignoreCase());
    assertFalse(assertion.regularExpression());
    assertEquals("//div[@id='main']/h2", assertion.getXPath());
    assertEquals("hello world i am happy now", assertion.getExpectedValue());
  }

  /**
   * Tests parsing of <code>assert-equals</code> clauses.
   */
  @Test
  public void testAssertEqualsUsingRegularExpression() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertTrue(assertions.get(4) instanceof EqualityAssertion);
    EqualityAssertion assertion = (EqualityAssertion) assertions.get(4);
    assertTrue(assertion.isPositive());
    assertFalse(assertion.ignoreWhitespace());
    assertFalse(assertion.ignoreCase());
    assertTrue(assertion.regularExpression());
    assertEquals("//div[@id='main']/h2", assertion.getXPath());
    assertEquals("^hello world i am [\\w]* now$", assertion.getExpectedValue());
  }

  /**
   * Tests parsing of parameters.
   */
  @Test
  public void testParameters() {
    Map<String, String[]> parameters = testCases.get(0).getParameters();
    assertEquals(1, parameters.size());
    assertNotNull(parameters.get("language"));
    assertEquals("english", parameters.get("language")[0]);
  }

}
