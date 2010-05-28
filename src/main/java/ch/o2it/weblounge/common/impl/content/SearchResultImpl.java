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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.content.SearchResultItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation for a search result.
 */
public class SearchResultImpl implements SearchResult {

  /** The query that led to this search result */
  protected SearchQuery query = null;

  /** The search offset */
  protected long offset = 0;

  /** The search limit */
  protected long limit = 0;

  /** The total number of appearances of the search criteria */
  protected long hitCount = 0;

  /** The total size of the search result set */
  protected long size = 0;

  /** The time it took to do the search in ms */
  protected long time = 0;

  /** The search result */
  protected List<SearchResultItem> result = new ArrayList<SearchResultItem>();

  /**
   * Creates a search result that was created using the given query. Note that
   * <code>hits</code> indicates the overall number of appearances of the search
   * term, while size is equal to the number of documents that contain those
   * <code>hits</code> hits.
   * 
   * @param query
   *          the query
   * @param hitCount
   *          the number of hits
   * @param size
   *          the total size of the result set
   */
  public SearchResultImpl(SearchQuery query, long hitCount, long size) {
    this.query = query;
    this.offset = query.getOffset();
    this.limit = query.getLimit();
    this.hitCount = hitCount;
    this.size = size;
  }

  /**
   * Adds the given search result item to the result set.
   * 
   * @param item
   *          the result item
   */
  public void addResultItem(SearchResultItem item) {
    result.add(item);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResult#getItems()
   */
  public SearchResultItem[] getItems() {
    return result.toArray(new SearchResultItem[result.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResult#getLimit()
   */
  public long getLimit() {
    return limit;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResult#getOffset()
   */
  public long getOffset() {
    return offset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResult#getPage()
   */
  public long getPage() {
    if (offset == 0 || limit == 0)
      return 1;
    return (long) Math.floor(offset / limit) + 1;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResult#getPageSize()
   */
  public long getPageSize() {
    return result.size();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResult#getQuery()
   */
  public SearchQuery getQuery() {
    return query;
  }

  /**
   * Sets the search time in milliseconds.
   * 
   * @param time
   *          the time
   */
  public void setSearchTime(long time) {
    this.time = time;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResult#getSearchTime()
   */
  public long getSearchTime() {
    return time;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.SearchResult#getHitCount()
   */
  public long getHitCount() {
    return hitCount;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResult#size()
   */
  public long size() {
    return size;
  }

}
