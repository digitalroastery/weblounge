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

package ch.o2it.weblounge.common.content.page;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.site.Site;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for the {@link #PageURIImpl}.
 */
public class PageURITest {
  
  /** Mocked site object */
  protected static Site site = null;

  /** The uri instance under test */
  protected ResourceURI simpleURI = null;

  /** The uri instance under test */
  protected ResourceURI versionedURI = null;

  /** The uri instance under test */
  protected ResourceURI identifyableVersionedURI = null;

  /** The parent uri instance under test */
  protected ResourceURI simpleParentURI = null;

  /** The parent uri instance under test */
  protected ResourceURI versionedParentURI = null;

  /** The parent uri instance under test */
  protected ResourceURI identifyableParentURI = null;

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
    identifyableVersionedURI = new PageURIImpl(site, defaultPath, defaultId, defaultRandomVersion);
    simpleParentURI = new PageURIImpl(site, defaultParentPath);
    versionedParentURI = new PageURIImpl(site, defaultParentPath, defaultRandomVersion);
    identifyableParentURI = new PageURIImpl(site, defaultParentPath, defaultId, defaultRandomVersion);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceURIImpl#getId()}.
   */
  @Test
  public void testGetId() {
    assertNull(simpleURI.getId());
    assertNull(versionedURI.getId());
    assertEquals(defaultId, identifyableVersionedURI.getId());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceURIImpl#getParentURI()}.
   */
  @Test
  public void testGetParentURI() {
    assertTrue(simpleParentURI.equals(simpleURI.getParentURI()));
    assertTrue(versionedParentURI.equals(versionedURI.getParentURI()));
    assertTrue(identifyableParentURI.equals(identifyableVersionedURI.getParentURI()));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceURIImpl#getPath()}.
   */
  @Test
  public void testGetPath() {
    assertEquals(UrlUtils.trim(defaultPath), simpleURI.getPath());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceURIImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, simpleURI.getSite());
    assertEquals(site, versionedURI.getSite());
    assertEquals(site, identifyableVersionedURI.getSite());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceURIImpl#getVersion()}.
   */
  @Test
  public void testGetVersion() {
    assertEquals(Resource.LIVE, simpleURI.getVersion());
    assertEquals(defaultRandomVersion, versionedURI.getVersion());
    assertEquals(defaultRandomVersion, identifyableVersionedURI.getVersion());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceURIImpl#switchToVersion(long)}.
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
    assertTrue(simpleURI.equals(simpleURI));
    assertTrue(versionedURI.equals(versionedURI));
    assertTrue(identifyableVersionedURI.equals(identifyableVersionedURI));
    assertFalse(simpleURI.equals(versionedURI));
    assertTrue(versionedURI.equals(identifyableVersionedURI));
  }

}
