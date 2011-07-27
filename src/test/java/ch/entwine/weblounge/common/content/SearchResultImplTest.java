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
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.SearchResultImpl;
import ch.entwine.weblounge.common.impl.content.SearchResultItemImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link SearchResultImpl}.
 */
public class SearchResultImplTest {

  /** The search result */
  protected SearchResultImpl result = null;

  /** The search query */
  protected SearchQueryImpl query = null;

  /** The mock site */
  protected Site site = null;

  /** The total number of hits */
  protected long hitCount = 100;

  /** The number of items included in this result set */
  protected long documentCount = 10;

  /** The first result item */
  protected int offset = 30;

  /** The maximum number of result items to include */
  protected int limit = 10;

  /** The search time in milliseconds */
  protected long searchTime = 23;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
    query = new SearchQueryImpl(site);
    query.withOffset(offset);
    query.withLimit(limit);
    result = new SearchResultImpl(query, hitCount, documentCount);
    result.setSearchTime(searchTime);

    Object source = new Object();
    String id = "4bb19980-8f98-4873-a813-71b5dfac22af";
    WebUrl url = new WebUrlImpl(site, "/");
    for (int i = 0; i < limit; i++) {
      double relevance = Math.random();
      SearchResultItemImpl item = new SearchResultItemImpl(id, url, relevance, source);
      result.addResultItem(item);
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getItems()}.
   */
  @Test
  public void testGetItems() {
    assertEquals(limit, result.getItems().length);
    double relevance = Double.MAX_VALUE;
    for (SearchResultItem item : result.getItems()) {
      if (item.getRelevance() > relevance)
        fail("Result items are not ordered");
      relevance = item.getRelevance();
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getLimit()}.
   */
  @Test
  public void testGetLimit() {
    assertEquals(limit, result.getLimit());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getOffset()}.
   */
  @Test
  public void testGetOffset() {
    assertEquals(offset, result.getOffset());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getPage()}.
   */
  @Test
  public void testGetPage() {
    long page = (long) Math.floor(offset / limit) + 1;
    assertEquals(page, result.getPage());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getPageSize()}
   * .
   */
  @Test
  public void testGetPageSize() {
    assertEquals(limit, result.getPageSize());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getQuery()}.
   */
  @Test
  public void testGetQuery() {
    assertEquals(query, result.getQuery());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getSearchTime()}
   * .
   */
  @Test
  public void testGetSearchTime() {
    assertEquals(searchTime, result.getSearchTime());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getHitCount()}
   * .
   */
  @Test
  public void testGetHitCount() {
    assertEquals(hitCount, result.getHitCount());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.SearchResultImpl#getDocumentCount()}
   * .
   */
  @Test
  public void testGetDocumentCount() {
    assertEquals(documentCount, result.getDocumentCount());
  }

}
