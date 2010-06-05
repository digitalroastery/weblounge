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

package ch.o2it.weblounge.common.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.content.PageletRendererImpl;
import ch.o2it.weblounge.common.impl.content.SearchResultItemImpl;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link SearchResultItemImpl}.
 */
public class SearchResultItemImplTest {
  
  /** The search result item */
  protected SearchResultItemImpl item = null;
  
  /** The relevance */
  protected double relevance = 0.5d;
  
  /** The preview data */
  protected Object previewData = null;
  
  /** The page xml */
  protected String pageXml = null;
  
  /** THe pagelet renderer */
  protected PageletRenderer renderer = null;
  
  /** The source for this result item */
  protected Object source = null;
  
  /** The result title */
  protected String title = null;
  
  /** The page id */
  protected String id = null;
  
  /** The page path */
  protected String path = "/service/test";
  
  /** The url of the search result */
  protected WebUrl url = null;
  
  /** The mock site */
  protected Site site = null;
  
  /**
   * Setup for the test.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    previewData = new Object();
    renderer = new PageletRendererImpl("id");
    source = new Object();
    title = "My search result";
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
    url = new WebUrlImpl(site, path);
    pageXml = IOUtils.toString(getClass().getResourceAsStream("/page.xml"));
    item = new SearchResultItemImpl(site, id, url, relevance, source);
    item.setTitle(title);
    item.setPreview(previewData);
    item.setPreviewRenderer(renderer);
    item.setPageXml(pageXml);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.SearchResultItemImpl#getTitle()}.
   */
  @Test
  public void testGetTitle() {
    assertEquals(title, item.getTitle());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.SearchResultItemImpl#getUrl()}.
   */
  @Test
  public void testGetUrl() {
    assertEquals(url, item.getUrl());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.SearchResultItemImpl#getPreview()}.
   */
  @Test
  public void testGetPreview() {
    assertEquals(previewData, item.getPreview());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.SearchResultItemImpl#getPage()}.
   */
  @Test
  public void testGetPage() {
    assertNotNull(item.getPage());
    assertEquals(UrlSupport.trim(path), item.getPage().getURI().getPath());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.SearchResultItemImpl#getPreviewRenderer()}.
   */
  @Test
  public void testGetPreviewRenderer() {
    assertEquals(renderer, item.getPreviewRenderer());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.SearchResultItemImpl#getRelevance()}.
   */
  @Test
  public void testGetRelevance() {
    assertEquals(relevance, item.getRelevance());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.SearchResultItemImpl#getSource()}.
   */
  @Test
  public void testGetSource() {
    assertEquals(source, item.getSource());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.SearchResultItemImpl#compareTo(ch.o2it.weblounge.common.content.SearchResultItem)}.
   */
  @Test
  public void testCompareToSearchResultItem() {
    SearchResultItem nextItem = new SearchResultItemImpl(site, id, url, relevance + 1.0, source);
    assertTrue(item.compareTo(nextItem) > 0);
  }

}
