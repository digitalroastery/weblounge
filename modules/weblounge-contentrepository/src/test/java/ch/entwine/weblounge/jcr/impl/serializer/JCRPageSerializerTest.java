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

import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.jcr.RepositoryTestUtils;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link JCRPageResourceSerializer}
 */
public class JCRPageSerializerTest {

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
   * Test for
   * {@link JCRPageResourceSerializer#store(javax.jcr.Node, ch.entwine.weblounge.common.content.Resource)}
   */
  @Test
  public void testStore() throws Exception {
    try {
      serializer.store(null, null);
      fail("Providing null for Node and Resource parameters must throw an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Nothing to do
    }
  }

}
