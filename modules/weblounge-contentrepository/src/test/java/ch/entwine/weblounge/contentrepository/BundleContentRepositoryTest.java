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

package ch.entwine.weblounge.contentrepository;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.impl.bundle.BundleContentRepository;

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
    liveHomeURI = new PageURIImpl(site, homePath, Resource.LIVE);
    workHomeURI = new PageURIImpl(site, homePath, Resource.WORK);
    workSubURI = new PageURIImpl(site, subPath, Resource.WORK);
    bundle = new StandaloneBundle(BundleContentRepositoryTest.class.getClassLoader());
    
    TestUtils.startTesting();
    repository = new BundleContentRepository();
    Dictionary<String, Object> properties = new Hashtable<String, Object>();
    properties.put(Site.class.getName(), site);
    properties.put(Bundle.class.getName(), bundle);
    repository.updated(properties);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#disconnect()}.
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
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#exists(ch.entwine.weblounge.common.content.ResourceURI)}.
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
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#exists(ch.entwine.weblounge.common.content.ResourceURI, ch.entwine.weblounge.common.user.User, ch.entwine.weblounge.common.security.Permission)}.
   */
  @Test
  public void testExistsPageURIUserPermission() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#find(ch.entwine.weblounge.common.content.SearchQuery)}.
   */
  @Test
  public void testFindPages() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#get(ch.entwine.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testGetPagePageURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#get(ch.entwine.weblounge.common.content.ResourceURI, ch.entwine.weblounge.common.user.User, ch.entwine.weblounge.common.security.Permission)}.
   */
  @Test
  public void testGetPagePageURIUserPermission() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#get(ch.entwine.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testGetVersions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#listPages()}.
   */
  @Test
  public void testListPages() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#list(long[])}.
   */
  @Test
  public void testListPagesLongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI)}.
   */
  @Test
  public void testListPagesPageURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI, long[])}.
   */
  @Test
  public void testListPagesPageURILongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI, int)}.
   */
  @Test
  public void testListPagesPageURIInt() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#list(ch.entwine.weblounge.common.content.ResourceURI, int, long[])}.
   */
  @Test
  public void testListPagesPageURIIntLongArray() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#setBundlePathPrefix(java.lang.String)}.
   */
  @Test
  public void testSetURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#setPagesURI(java.lang.String)}.
   */
  @Test
  public void testSetPagesURI() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.BundleContentRepository.BundleContentRepository#setResourcesURI(java.lang.String)}.
   */
  @Test
  public void testSetResourcesURI() {
    fail("Not yet implemented"); // TODO
  }

}
