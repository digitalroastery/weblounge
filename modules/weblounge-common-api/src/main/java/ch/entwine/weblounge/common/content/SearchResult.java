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

/**
 * A result as returned by a search operation.
 */
public interface SearchResult {

  /**
   * The search items as returned by the search operation or an empty array in
   * case the search query did not yield any results.
   * 
   * @return the items
   */
  SearchResultItem[] getItems();

  /**
   * Returns the original query that yielded this result set.
   * 
   * @return the query.
   */
  SearchQuery getQuery();

  /**
   * Returns the total number of appearances of the search criteria in the
   * search result, spread over <code>getDocumentCount</code> number of
   * documents.
   * 
   * @return the overall number of hits
   */
  long getHitCount();

  /**
   * Returns the total number of items in the search result.
   * <p>
   * Note that this number might not match the size of the array as returned by
   * {@link #getItems()}, which is likely to be limited by the value returned by
   * {@link #getLimit()}.
   * 
   * @return the number of documents containing the hits
   */
  long getDocumentCount();

  /**
   * Returns the number of items in this search result, possibly limited with
   * respect to the total number of result items by <code>offset</code> and
   * <code>limit</code>.
   * 
   * @return the total number of hits.
   * @see #getOffset()
   * @see #getLimit()
   */
  long getPageSize();

  /**
   * Get the offset within the search result or <code>-1</code> if no limit has
   * been specified.
   * 
   * @return the offset
   */
  long getOffset();

  /**
   * Returns the limit of this search results or <code>-1</code> if no limit has
   * been specified.
   * 
   * @return the limit
   */
  long getLimit();

  /**
   * Returns the page of the current result items within the complete search
   * result. This number is influenced by the <code>offset</code> and the page
   * size <code>limit</code>.
   * <p>
   * Note that the page size is one-based
   * 
   * @return the page number
   */
  long getPage();

  /**
   * Returns the search time in milliseconds.
   * 
   * @return the time
   */
  long getSearchTime();

  /**
   * Returns the xml version of this search result.
   * 
   * @return the serialized search result
   */
  String toXml();

}
