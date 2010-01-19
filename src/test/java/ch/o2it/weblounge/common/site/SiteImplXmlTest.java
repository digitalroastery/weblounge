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

package ch.o2it.weblounge.common.site;

import ch.o2it.weblounge.common.TestUtils;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO: Comment SiteImplXmlTest
 */
@Ignore
public class SiteImplXmlTest extends SiteImplTest {

  /** Name of the test file */
  protected String testFile = "/site.xml";
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
//    URL testSite = this.getClass().getResource(testFile);
//    site = SiteImpl.fromXml(testSite);
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageImpl#toXml()}.
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromFile(testFile);
//    try {
//      assertEquals(testXml, new String(site.toXml().getBytes("UTF-8")));
//    } catch (UnsupportedEncodingException e) {
//      fail("Encoding to utf-8 failed");
//    }
  }

}
