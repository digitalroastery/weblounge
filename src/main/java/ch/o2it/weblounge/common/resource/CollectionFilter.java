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

package ch.o2it.weblounge.common.resource;

/**
 * Interface for a class that is able to filter a list of repository
 * collections.
 */
public interface CollectionFilter {

  /**
   * Whether to accept the collection, i.e. whether the collection should be
   * included in the filtered collection list.
   * 
   * @param collection
   *          the collection to test
   * @return <code>true</code> if the collection passes the filter <code>false
	 * </code>
   *         otherwise.
   */
  boolean accept(Collection collection);

}