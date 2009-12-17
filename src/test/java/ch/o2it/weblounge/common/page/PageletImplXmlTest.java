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
import ch.o2it.weblounge.common.impl.page.PageletImpl;
import ch.o2it.weblounge.common.impl.page.PageletReader;
import ch.o2it.weblounge.common.impl.user.SiteAdminImpl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Test case for the implementation at {@link PageletImpl}.
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
    EasyMock.expect(site.getAdministrator()).andReturn(new SiteAdminImpl("admin", "test"));
    EasyMock.replay(site);
    URL testContext = this.getClass().getResource(testFile);
    PageletReader reader = new PageletReader(site);
    reader.setPageletLocation(location);
    pagelet = reader.read(testContext.openStream());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.page.PageletImpl#toXml()}.
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromFile(testFile);
    try {
      assertEquals(testXml, new String(pagelet.toXml().getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
