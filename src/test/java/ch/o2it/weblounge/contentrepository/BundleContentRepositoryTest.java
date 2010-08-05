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

package ch.o2it.weblounge.contentrepository;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.impl.bundle.BundleContentRepository;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Test class for {@link BundleContentRepository}.
 */
@Ignore
public class BundleContentRepositoryTest {

  /** The bundle content repository */
  protected BundleContentRepository repository = null;
  
  /** The bundle */
  protected Bundle bundle = null;
  
  /** The site */
  protected Site site = null;
  
  /** Path to the home page */
  protected String homePath = "/";

  /** Path to the sub page */
  protected String subPath = "/sub";

  /** Page URI for the home page's live version */
  protected ResourceURI liveHomeURI = null;

  /** Page URI for the home page's work version */
  protected ResourceURI workHomeURI = null;

  /** Page URI for the sub page's work version */
  protected ResourceURI workSubURI = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
    liveHomeURI = new ResourceURIImpl(site, homePath, Page.LIVE);
    workHomeURI = new ResourceURIImpl(site, homePath, Page.WORK);
    workSubURI = new ResourceURIImpl(site, subPath, Page.WORK);
    bundle = new StandaloneBundle(BundleContentRepositoryTest.class.getClassLoader());
    
    repository = new BundleContentRepository();
    Dictionary<String, Object> properties = new Hashtable<String, Object>();
    properties.put(Site.class.getName(), site);
    properties.put(Bundle.class.getName(), bundle);
    repository.connect(properties);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#connect(ch.o2it.weblounge.common.site.Site, java.util.Dictionary)}.
   */
  @Test
  public void testConnect() {
    Dictionary<String, Object> properties = new Hashtable<String, Object>();
    try {
      repository.connect(properties);
      fail("Repository should not start without bundle in the connect properties");
    } catch (ContentRepositoryException e) {
      // this is excepted
    }
    try {
      properties.put(Bundle.class.getName(), bundle);
      repository.connect(properties);
    } catch (ContentRepositoryException e) {
      fail("Bundle repository was not able to connect despite of the bundle being part of the connect properties");
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#disconnect()}.
   */
  @Test
  public void testDisconnect() {
    try {
      repository.disconnect();
    } catch (ContentRepositoryException e) {
      fail("Error while disconnecting bundle repository");
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#exists(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testExistsPageURI() {
    try {
      assertTrue(repository.exists(liveHomeURI));
      assertTrue(repository.exists(workSubURI));
    } catch (ContentRepositoryException e) {
      fail("Error checking for the existence of pages");
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#exists(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.user.User, ch.o2it.weblounge.common.security.Permission)}.
   */
  @Test
  public void testExistsPageURIUserPermission() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#findPages(ch.o2it.weblounge.common.content.SearchQuery)}.
   */
  @Test
  public void testFindPages() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#getPage(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testGetPagePageURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#getPage(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.user.User, ch.o2it.weblounge.common.security.Permission)}.
   */
  @Test
  public void testGetPagePageURIUserPermission() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#getVersions(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testGetVersions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#listPages()}.
   */
  @Test
  public void testListPages() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#listPages(long[])}.
   */
  @Test
  public void testListPagesLongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#listPages(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testListPagesPageURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#listPages(ch.o2it.weblounge.common.content.ResourceURI, long[])}.
   */
  @Test
  public void testListPagesPageURILongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#listPages(ch.o2it.weblounge.common.content.ResourceURI, int)}.
   */
  @Test
  public void testListPagesPageURIInt() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#listPages(ch.o2it.weblounge.common.content.ResourceURI, int, long[])}.
   */
  @Test
  public void testListPagesPageURIIntLongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#setURI(java.lang.String)}.
   */
  @Test
  public void testSetURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#setPagesURI(java.lang.String)}.
   */
  @Test
  public void testSetPagesURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.repository.BundleContentRepository#setResourcesURI(java.lang.String)}.
   */
  @Test
  public void testSetResourcesURI() {
    fail("Not yet implemented"); // TODO
  }

}
