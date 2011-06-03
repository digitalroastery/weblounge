/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.common.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.testing.IntegrationTestCase;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestCase.EqualityAssertion;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestCase.ExistenceAssertion;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestCase.StatusCodeAssertion;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestCaseAssertion;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestGroup;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestParser;
import ch.o2it.weblounge.common.impl.util.TestUtils;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
  public static void setup() throws Exception {
    String testXml = TestUtils.loadXmlFromResource(testFile);
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Node doc = docBuilder.parse(new InputSource(new StringReader(testXml)));

    // Finally, parse the test
    testGroup = IntegrationTestParser.fromXml(doc);
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
    assertEquals(5, assertions.size());
  }

  /**
   * Tests parsing of <code>assert-status</code> clauses.
   */
  @Test
  public void testStatusAssertion() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertTrue(assertions.get(0) instanceof StatusCodeAssertion);
    StatusCodeAssertion assertion = (StatusCodeAssertion)assertions.get(0);
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
    ExistenceAssertion assertion = (ExistenceAssertion)assertions.get(1);
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
    ExistenceAssertion assertion = (ExistenceAssertion)assertions.get(2);
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
    EqualityAssertion assertion = (EqualityAssertion)assertions.get(3);
    assertTrue(assertion.isPositive());
    assertTrue(assertion.ignoreWhitespace());
    assertTrue(assertion.ignoreCase());
    assertEquals("//div[@id='main']/h1", assertion.getXPath());
    assertEquals("hello world i am happy today", assertion.getExpectedValue());
  }
  
  /**
   * Tests parsing of <code>assert-not-equals</code> clauses.
   */
  @Test
  public void testAssertNotEquals() {
    List<IntegrationTestCaseAssertion> assertions = testCases.get(0).getAssertions();
    assertTrue(assertions.get(4) instanceof EqualityAssertion);
    EqualityAssertion assertion = (EqualityAssertion)assertions.get(4);
    assertFalse(assertion.isPositive());
    assertTrue(assertion.ignoreWhitespace());
    assertFalse(assertion.ignoreCase());
    assertEquals("//div[@id='main']/h2", assertion.getXPath());
    assertEquals("hello world i am happy now", assertion.getExpectedValue());
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
