/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.page;

import static org.junit.Assert.fail;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.TestUtils;
import ch.o2it.weblounge.common.impl.content.page.PageImpl;
import ch.o2it.weblounge.common.impl.content.page.PageReader;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Test case to test {@link PageImpl}.
 */
public class PageImplXmlTest extends PageImplTest {
  
  /** Name of the test file */
  protected String testFile = "/page.xml";
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
    URL testContext = this.getClass().getResource(testFile);
    PageReader reader = new PageReader();
    page = reader.read(testContext.openStream(), pageURI);
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.page.PageImpl#toXml()}.
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromResource(testFile);
    try {
      assertEquals(testXml, new String(page.toXml().getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}