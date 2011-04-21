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

package ch.o2it.weblounge.kernel.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ch.o2it.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.o2it.weblounge.common.impl.util.TestUtils;
import ch.o2it.weblounge.kernel.publisher.JSONResponseWrapper;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the wrapper implementation at {@link JSONResponseWrapper}.
 */
public class JSONResponseWrapperTest {

  /** The response wrapper under test */
  protected JSONResponseWrapper jsonResponse = null;

  /** The mocked response */
  protected MockHttpServletResponse response = new MockHttpServletResponse();

  /** Path to the sample xml response */
  protected static final String XML = "/response.xml";

  /** Path to the sample json response */
  protected static final String JSON = "/response.json";

  /** Xml content */
  protected String xml = null;

  /** Expected json content */
  protected String json = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    jsonResponse = new JSONResponseWrapper(response);
    xml = TestUtils.loadXmlFromResource(XML);
    json = TestUtils.loadJsonFromResource(JSON);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.kernel.publisher.JSONResponseWrapperTest#finishResponse()}
   * .
   */
  @Test
  public void testXmlToJson() throws Exception {
    jsonResponse.getWriter().write(xml);
    jsonResponse.finishResponse();
    assertNotNull(response.getContentAsString());
    assertEquals(json, response.getContentAsString());
  }

}
