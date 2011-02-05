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

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.impl.bundle.BundleContentRepositoryImpl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Test class for {@link BundleContentRepositoryImpl}.
 */
@Ignore
public class BundleContentRepositoryTest {

  /** The bundle content repository */
  protected BundleContentRepositoryImpl repository = null;
  
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
    liveHomeURI = new PageURIImpl(site, homePath, Resource.LIVE);
    workHomeURI = new PageURIImpl(site, homePath, Resource.WORK);
    workSubURI = new PageURIImpl(site, subPath, Resource.WORK);
    bundle = new StandaloneBundle(BundleContentRepositoryTest.class.getClassLoader());
    
    repository = new BundleContentRepositoryImpl();
    Dictionary<String, Object> properties = new Hashtable<String, Object>();
    properties.put(Site.class.getName(), site);
    properties.put(Bundle.class.getName(), bundle);
    repository.updated(properties);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#disconnect()}.
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
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#exists(ch.o2it.weblounge.common.content.ResourceURI)}.
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
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#exists(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.user.User, ch.o2it.weblounge.common.security.Permission)}.
   */
  @Test
  public void testExistsPageURIUserPermission() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#find(ch.o2it.weblounge.common.content.SearchQuery)}.
   */
  @Test
  public void testFindPages() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#get(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testGetPagePageURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#get(ch.o2it.weblounge.common.content.ResourceURI, ch.o2it.weblounge.common.user.User, ch.o2it.weblounge.common.security.Permission)}.
   */
  @Test
  public void testGetPagePageURIUserPermission() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#get(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testGetVersions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#listPages()}.
   */
  @Test
  public void testListPages() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#list(long[])}.
   */
  @Test
  public void testListPagesLongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testListPagesPageURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI, long[])}.
   */
  @Test
  public void testListPagesPageURILongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI, int)}.
   */
  @Test
  public void testListPagesPageURIInt() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#list(ch.o2it.weblounge.common.content.ResourceURI, int, long[])}.
   */
  @Test
  public void testListPagesPageURIIntLongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#setBundlePathPrefix(java.lang.String)}.
   */
  @Test
  public void testSetURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#setPagesURI(java.lang.String)}.
   */
  @Test
  public void testSetPagesURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.BundleContentRepositoryImpl.BundleContentRepository#setResourcesURI(java.lang.String)}.
   */
  @Test
  public void testSetResourcesURI() {
    fail("Not yet implemented"); // TODO
  }

}
