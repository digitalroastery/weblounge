/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link JCRResourceUtils}
 */
public class JCRResourceUtilsTest {

  /** The site mockup */
  private Site site = null;

  private ResourceURI rootPageURI = null;

  private ResourceURI fileURI = null;

  @Before
  public void setUp() {
    // Site
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getIdentifier()).andReturn("test").anyTimes();
    EasyMock.replay(site);

    // URIs
    rootPageURI = new ResourceURIImpl(Page.TYPE, site, "/");
    fileURI = new ResourceURIImpl(FileResource.TYPE, site, "/first-test-file/");
  }

  /**
   * Test for {@link JCRResourceUtils#getAbsNodePath(ResourceURI)}
   */
  @Test
  public void testGetAbsNodePath() {
    // Test null-value handling
    try {
      JCRResourceUtils.getAbsNodePath(null);
      fail("Must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    assertEquals("/sites/test/resources", JCRResourceUtils.getAbsNodePath(rootPageURI));
    assertEquals("/sites/test/resources/first-test-file", JCRResourceUtils.getAbsNodePath(fileURI));
  }

  /**
   * Test for {@link JCRResourceUtils#getAbsParentNodePath(ResourceURI)}
   */
  @Test
  public void testGetAbsParentNodePath() {
    // Test null-value handling
    try {
      JCRResourceUtils.getAbsParentNodePath(null);
      fail("Must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    assertEquals("/sites/test", JCRResourceUtils.getAbsParentNodePath(rootPageURI));
    assertEquals("/sites/test/resources", JCRResourceUtils.getAbsParentNodePath(fileURI));
  }

  /**
   * Test for {@link JCRResourceUtils#getNodeName(ResourceURI)}
   */
  @Test
  public void testGetNodeName() {
 // Test null-value handling
    try {
      JCRResourceUtils.getNodeName(null);
      fail("Must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }
    
    assertEquals("resources", JCRResourceUtils.getNodeName(rootPageURI));
    assertEquals("first-test-file", JCRResourceUtils.getNodeName(fileURI));
  }
}
