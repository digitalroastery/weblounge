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

package ch.entwine.weblounge.common.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.content.SearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.page.PageSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.url.UrlUtils;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link SearchResultItemPageImpl}.
 */
public class PageSearchResultItemImplTest extends SearchResultItemImplTest {
  
  /** The specialized search result item */
  protected PageSearchResultItemImpl pageItem = null;
  
  /** The page xml */
  protected String pageXml = null;

  /**
   * Setup for the test.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPrerequisites();
    pageXml = IOUtils.toString(getClass().getResourceAsStream("/page.xml"));
    ResourceURI uri = new PageURIImpl(site, path, id, Resource.LIVE);
    pageItem = new PageSearchResultItemImpl(uri, url, relevance, source);
    item = pageItem;
    item.setTitle(title);
    item.setPreview(previewData);
    item.setPreviewRenderer(renderer);
    pageItem.setResourceXml(pageXml);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.SearchResultItemImpl#getPage()}.
   */
  @Test
  public void testGetPage() {
    assertNotNull(pageItem.getPage());
    assertEquals(UrlUtils.trim(path), pageItem.getPage().getURI().getPath());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.SearchResultItemImpl#compareTo(ch.entwine.weblounge.common.content.SearchResultItem)}.
   */
  @Test
  public void testCompareToSearchResultItem() {
    SearchResultItem nextItem = new SearchResultItemImpl(id, url, relevance + 1.0, source);
    assertTrue(item.compareTo(nextItem) > 0);
  }

}
