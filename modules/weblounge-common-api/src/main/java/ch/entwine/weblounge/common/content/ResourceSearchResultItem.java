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

import java.util.List;

/**
 * This extended <code>SearchResultItem</code> interface is intended for
 * resources that were found in the weblounge search index.
 * 
 * @see ch.entwine.weblounge.common.content.SearchResultItem
 */
public interface ResourceSearchResultItem extends SearchResultItem {

  /**
   * Returns the resource uri.
   * 
   * @return the resource uri
   */
  ResourceURI getResourceURI();

  /**
   * Returns the full set of fields from the search index.
   * 
   * @return
   */
  List<ResourceMetadata<?>> getMetadata();

  /**
   * Returns the metadata item for the given key or <code>null</code> if that
   * piece of metadata is not available.
   * 
   * @param key
   *          the metadata key
   * @return the resource metadata item
   */
  ResourceMetadata<?> getMetadataByKey(String key);

  /**
   * Returns the resource's <code>XML</code> representation.
   * 
   * @return the full resource
   */
  String getResourceXml();

}
