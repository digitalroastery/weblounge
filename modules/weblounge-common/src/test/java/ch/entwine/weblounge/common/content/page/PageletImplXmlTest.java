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

package ch.entwine.weblounge.common.content.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.content.page.PageletReader;
import ch.entwine.weblounge.common.impl.util.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Test case for the implementation at
 * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl}.
 */
public class PageletImplXmlTest extends PageletImplTest {

  /** File path and name */
  protected String testFile = "/pagelet.xml";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPreliminaries();
    URL testContext = this.getClass().getResource(testFile);
    PageletReader reader = new PageletReader();
    reader.setPageletLocation(location);
    pagelet = reader.read(testContext.openStream());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageletImpl#toXml()}.
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromResource(testFile);
    try {
      assertEquals(testXml, new String(pagelet.toXml().getBytes("utf-8"), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
