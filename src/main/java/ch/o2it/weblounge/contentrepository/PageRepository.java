/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.page.SearchQuery;
import ch.o2it.weblounge.common.page.SearchResult;

/**
 * A <code>PageRepository</code> is a facility that serves pages to weblounge
 * sites.
 */
public interface PageRepository {

  /**
   * Returns the page identified by <code>uri</code> or <code>null</code> if no
   * page was found at the specified location or revision.
   * 
   * @param uri
   *          the page uri
   * @return the page
   */
  Page get(PageURI uri);

  /**
   * Puts the page to the specified location. Depending on whether the page
   * identified by <code>uri</code> already exists, the method either creates a
   * new page or updates the existing one.
   * <p>
   * The returned page contains the same data than the one passed in as the
   * <code>page</code> argument but with an updated uri.
   * 
   * @param uri
   *          the page uri
   * @param page
   *          the page
   * @return the page with the given uri
   */
  Page put(PageURI uri, Page page);

  /**
   * Removes the indicated page and revision from the content repository. If
   * <code>allRevisisions</code> is set to <code>true</code>, all revisions will
   * be removed.
   * 
   * @param uri
   *          the page uri
   * @param allRevisions
   *          <code>true</code> to remove all revisions
   */
  void delete(PageURI uri, boolean allRevisions);

  /**
   * Returns a page uri for every available revision of the page.
   * 
   * @param uri
   *          the page uri
   * @return the revisions
   */
  PageURI[] getVersions(PageURI uri);

  /**
   * Returns the search results for <code>query</code>.
   * 
   * @param query
   *          the query
   * @return the search result
   */
  SearchResult[] findPages(SearchQuery query);

}
