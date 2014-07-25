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

package ch.entwine.weblounge.common.impl.content.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.security.SystemAction;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test case to test
 * {@link ch.entwine.weblounge.common.impl.content.page.PageReader}.
 */
public class PageReaderTest {

  /** The page that was read in */
  protected Page page = null;

  /** The page's site */
  protected Site site = null;

  /** The page uri */
  protected ResourceURIImpl pageURI = null;

  /** Name of the test file */
  protected String testFile = "/page.xml";

  /** Name of the other test file */
  protected String otherTestFile = "/page2.xml";

  /** The page reader */
  protected PageReader reader = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
    pageURI = new PageURIImpl(site, "/test", Resource.LIVE);
    reader = new PageReader();
    page = reader.read(this.getClass().getResource(testFile).openStream(), site);
  }

  @Test
  public void testSecurity() throws Exception {
    assertEquals(4, page.actions().length);
    assertFalse(page.isAllowed(SystemAction.READ, SystemRole.GUEST));
    assertTrue(page.isAllowed(SystemAction.READ, SystemRole.SITEADMIN));
  }
  
  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.page.PageReader#read(ch.entwine.weblounge.common.content.PageURI, java.io.InputStream)}
   * .
   */
  @Test
  public void testResetPageReader() throws Exception {
    URL testContext = this.getClass().getResource(testFile);
    String testXml = TestUtils.loadXmlFromResource(testFile);

    URL otherTestContext = this.getClass().getResource(otherTestFile);
    String otherTestXml = TestUtils.loadXmlFromResource(otherTestFile);

    // Read test page
    assertEquals(testXml, new String(page.toXml().getBytes("utf-8"), "utf-8"));

    // Read other test page
    page = reader.read(otherTestContext.openStream(), site);
    assertEquals(otherTestXml, new String(page.toXml().getBytes("utf-8"), "utf-8"));

    // Read test page again
    page = reader.read(testContext.openStream(), site);
    assertEquals(testXml, new String(page.toXml().getBytes("utf-8"), "utf-8"));
  }

}