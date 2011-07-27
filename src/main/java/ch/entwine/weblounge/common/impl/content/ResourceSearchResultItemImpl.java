/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.url.WebUrl;

/**
 * Base implementation for search result items that represent resources.
 */
public class ResourceSearchResultItemImpl extends SearchResultItemImpl implements ResourceSearchResultItem {

  /** The resource uri */
  protected ResourceURI uri = null;

  /**
   * Creates a new resource search result item.
   * 
   * @param uri
   *          the resource uri
   * @param url
   *          the resource url
   * @param relevance
   *          the relevance inside the current search
   * @param source
   *          the source of this search result item
   */
  public ResourceSearchResultItemImpl(ResourceURI uri, WebUrl url,
      double relevance, Object source) {
    super(uri.getIdentifier(), url, relevance, source);
    this.uri = uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSearchResultItem#getResourceURI()
   */
  public ResourceURI getResourceURI() {
    return uri;
  }

}
