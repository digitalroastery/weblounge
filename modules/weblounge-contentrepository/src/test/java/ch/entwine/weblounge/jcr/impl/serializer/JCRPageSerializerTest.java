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
package ch.entwine.weblounge.jcr.impl.serializer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.file.FileResourceImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.jcr.RepositoryTestUtils;
import ch.entwine.weblounge.jcr.WebloungeJCRTestBase;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;

/**
 * Unit tests for {@link JCRPageResourceSerializer}
 * 
 * TODO Not sure, if we realy need this test class, see issue
 * https://github.com/entwinemedia/weblounge/issues/308
 */
public class JCRPageSerializerTest extends WebloungeJCRTestBase {

  /** The page serializer */
  private JCRPageResourceSerializer serializer = null;

  /** The mock site */
  private Site site = null;

  /** The page */
  private Page page = null;

  @Before
  public void setUp() throws Exception {
    serializer = new JCRPageResourceSerializer();

    // Setup mock site
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getIdentifier()).andReturn("test").anyTimes();
    EasyMock.expect(site.getDefaultLanguage()).andReturn(LanguageUtils.getLanguage("en")).anyTimes();
    EasyMock.replay(site);

    // Load page
    page = RepositoryTestUtils.loadPageFromXml("/page1.xml", site);
  }

  /**
   * Test for {@link JCRPageResourceSerializer#store(Node, ch.entwine.weblounge.common.content.Resource)}
   */
  @Test
  public void testStore() throws Exception {
    // Test null-value handling
    try {
      serializer.store(null, null);
      fail("Providing null for Node or Resource parameters must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    Node pageNode = getTestRootNode();

    // Test adding
    try {
      serializer.store(pageNode, new FileResourceImpl(page.getURI()));
      fail("Storing an unsupported resource type must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    // Test correctness of data storage
    serializer.store(pageNode, page);
  }

  /**
   * Test for {@link JCRPageResourceSerializer#read(Node, ch.entwine.weblounge.common.content.ResourceURI)}
   */
  @Test
  public void testRead() throws Exception {
    // Test null-value handling
    try {
      serializer.read(null, null);
      fail("Providing null for Node or ResourceURI parameter must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }

    page.addSeries("my series");
    page.addSeries("an other series");

    Node pageNode = getTestRootNode();
    serializer.store(pageNode, page);

    Page pageRead = serializer.read(pageNode, page.getURI());
    assertEquals(page.getIdentifier(), pageRead.getIdentifier());
    assertEquals(page.getPath(), pageRead.getPath());
    assertEquals(page.getType(), pageRead.getType());
    assertEquals(page.isPromoted(), pageRead.isPromoted());
    assertFalse(pageRead.isLocked());
    assertNull(pageRead.getLockOwner());
    assertArrayEquals(page.getSubjects(), pageRead.getSubjects());
    assertArrayEquals(page.getSeries(), pageRead.getSeries());
  }

}
