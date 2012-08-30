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

package ch.entwine.weblounge.common.repository;

import ch.entwine.weblounge.common.site.Site;

/**
 * This interface defines a fluent api for a query object used to list resources
 * from a <code>ContentRepository</code>.
 */
public interface ResourceSelector {

  /**
   * Returns the contextual site for this selector.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Sets the number of results that are returned.
   * 
   * @param limit
   *          the number of results
   * @return the search query
   */
  ResourceSelector withLimit(int limit);

  /**
   * Returns the number of results that are returned, starting at the offset
   * returned by <code>getOffset()</code>. If no limit was specified, this
   * method returns <code>-1</code>.
   * 
   * @return the maximum number of results
   */
  int getLimit();

  /**
   * Sets the starting offset. Search results will be returned starting at that
   * offset and until the limit is reached, as specified by
   * <code>getLimit()</code>.
   * 
   * @param offset
   *          the starting offset
   * @return the search query
   * @see
   */
  ResourceSelector withOffset(int offset);

  /**
   * Returns the starting offset within the search result or <code>0</code> if
   * no offset was specified.
   * 
   * @return the offset
   */
  int getOffset();

  /**
   * Return the resources with the given identifier.
   * 
   * @param resourceId
   *          the identifier to look up
   * @return the query extended by this criterion
   */
  ResourceSelector withIdentifier(String resourceId);

  /**
   * Returns the identifier or <code>null</code> if no identifier was specified.
   * 
   * @return the identifier
   */
  String[] getIdentifiers();

  /**
   * Return the resources with the given types.
   * 
   * @param types
   *          the resource types to look up
   * @return the query extended by this criterion
   */
  ResourceSelector withTypes(String... types);

  /**
   * Returns the resource types or or an empty array if no types was specified.
   * 
   * @return the type
   */
  String[] getTypes();

  /**
   * Returns the resources except the ones with the given types.
   * 
   * @param types
   *          the resource types to block
   * @return the query extended by this criterion
   */
  ResourceSelector withoutTypes(String... types);

  /**
   * Returns the blocked resource types or or an empty array if no types was
   * specified.
   * 
   * @return the type
   */
  String[] getWithoutTypes();

  /**
   * Asks the search index to return only resources with the indicated version.
   * 
   * @param version
   *          the version
   * @return the search query
   */
  ResourceSelector withVersion(long version);

  /**
   * Returns the resource versions.
   * 
   * @return the versions
   */
  Long[] getVersions();

}
