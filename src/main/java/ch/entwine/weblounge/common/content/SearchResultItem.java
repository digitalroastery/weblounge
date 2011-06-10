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

import ch.entwine.weblounge.common.url.WebUrl;

/**
 * The search result is what is delivered by the search function of weblounge.
 * Search results can be delivered by two sources: First of all, the site is
 * searched and hits may include pages or resources. Second, the site's modules
 * will be queried adding the ability to add search results that are found
 * outside of the site's content repository. It is up to the module to decide
 * what the relevance value should be for the search results that it adds.
 */
public interface SearchResultItem extends Comparable<SearchResultItem> {

  /**
   * Returns the page identifier.
   * 
   * @return the identifier
   */
  String getId();

  /**
   * Returns the url that will lead to the location where the hit occurred.
   * 
   * @return the hit location
   */
  WebUrl getUrl();

  /**
   * Returns the type of the result item.
   * 
   * @return the type
   */
  String getType();

  /**
   * Returns the title of this search result.
   * 
   * @return the title
   */
  String getTitle();

  /**
   * Returns a preview of the search result.
   * 
   * @return the preview
   */
  Object getPreview();

  /**
   * Returns the renderer that is used to render the search result. Depending on
   * who delivered the result (could be either weblounge or a custom module),
   * the result might be rendered by a simple pagelet or some more sophisticated
   * renderer.
   * 
   * @return the renderer
   */
  Renderer getPreviewRenderer();

  /**
   * Returns the relevance of this hit with respect to the search terms. Greater
   * values mean increased relevance, a 1.0 signifies a direct hit while 0.0
   * means a very unlikely hit.
   * 
   * @return the relevance
   */
  double getRelevance();

  /**
   * Returns the source of this search result. This will usually be the site or
   * a site's module.
   * 
   * @return the source
   */
  Object getSource();

  /**
   * Returns the xml version of this search result item.
   * 
   * @return the serialized result item
   */
  String toXml();

}