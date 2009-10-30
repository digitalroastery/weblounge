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

import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Test case for the {@link #PageURIImpl}.
 */
public class PageURITest {
  
  /** Mocked site object */
  protected static Site site = null;

  /** The uri instance under test */
  protected PageURI simpleURI = null;

  /** The uri instance under test */
  protected PageURI versionedURI = null;

  /** The uri instance under test */
  protected PageURI identifyableURI = null;

  /** The parent uri instance under test */
  protected PageURI simpleParentURI = null;

  /** The parent uri instance under test */
  protected PageURI versionedParentURI = null;

  /** The parent uri instance under test */
  protected PageURI identifyableParentURI = null;

  /** The default path */
  protected String defaultPath = "/test";

  /** The default path */
  protected String defaultParentPath = "/";

  /** The default work version */
  protected long defaultRandomVersion = 17;

  /** The default id */
  protected String defaultId = "defaultid";

  @BeforeClass
  public static void setupClass() {
    site = createNiceMock(Site.class);
    expect(site.getIdentifier()).andReturn("test");
    replay(site);    
  }
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    simpleURI = new PageURIImpl(site, defaultPath);
    versionedURI = new PageURIImpl(site, defaultPath, defaultRandomVersion);
    identifyableURI = new PageURIImpl(site, defaultPath, defaultRandomVersion, defaultId);
    simpleParentURI = new PageURIImpl(site, defaultParentPath);
    versionedParentURI = new PageURIImpl(site, defaultParentPath, defaultRandomVersion);
    identifyableParentURI = new PageURIImpl(site, defaultParentPath, defaultRandomVersion, defaultId);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageURIImpl#getId()}.
   */
  @Test
  public void testGetId() {
    assertNull(simpleURI.getId());
    assertNull(versionedURI.getId());
    assertEquals(defaultId, identifyableURI.getId());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageURIImpl#getLink()}.
   */
  @Test
  public void testGetLink() {
    WebUrl link = new WebUrlImpl(site, defaultPath);
    WebUrl versionedLink = new WebUrlImpl(site, defaultPath, defaultRandomVersion);
    assertEquals(link, simpleURI.getLink());
    assertEquals(versionedLink, versionedURI.getLink());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageURIImpl#getParentURI()}.
   */
  @Test
  public void testGetParentURI() {
    assertEquals(simpleParentURI, simpleURI.getParentURI());
    assertEquals(versionedParentURI, versionedURI.getParentURI());
    assertEquals(identifyableParentURI, identifyableURI.getParentURI());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageURIImpl#getPath()}.
   */
  @Test
  public void testGetPath() {
    assertEquals(defaultPath, simpleURI.getPath());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageURIImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, simpleURI.getSite());
    assertEquals(site, versionedURI.getSite());
    assertEquals(site, identifyableURI.getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageURIImpl#getVersion()}.
   */
  @Test
  public void testGetVersion() {
    assertEquals(Page.LIVE, simpleURI.getVersion());
    assertEquals(defaultRandomVersion, versionedURI.getVersion());
    assertEquals(defaultRandomVersion, identifyableURI.getVersion());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.page.PageURIImpl#switchToVersion(long)}.
   */
  @Test
  public void testGetVersionLong() {
    assertEquals(7, simpleURI.getVersion(7).getVersion());
  }

  /**
   * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
   */
  @Test
  public void testEquals() {
    assertEquals(simpleURI, simpleURI);
    assertEquals(versionedURI, versionedURI);
    assertEquals(identifyableURI, identifyableURI);
    assertFalse(simpleURI.equals(versionedURI));
    assertFalse(versionedURI.equals(identifyableURI));
  }

}
