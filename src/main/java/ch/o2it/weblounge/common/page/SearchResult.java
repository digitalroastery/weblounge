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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.renderer.Renderer;

/**
 * This interface defines methods for a search result.
 */
public interface SearchResult extends Localizable, Comparable<SearchResult> {

  /**
   * Returns the uri that will lead to the location where the hit occurred.
   * 
   * @return the hit location
   */
  PageURI getURI();

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
  String getPreview();

  /**
   * Returns the search result's content type. By default, this will be
   * <code>text/html</code>.
   * 
   * @return the content type
   */
  String getContentType();

  /**
   * Returns the renderer that is used to render the search result.
   * 
   * @return the renderer
   */
  Renderer getPreviewRenderer();

  /**
   * Returns the relevance of this hit regarding the term that was looked up.
   * Greater values mean increased relevance, a 1.0 signifies a direct hit while
   * 0.0 means a very unlikely hit.
   * 
   * @return the relevance
   */
  float getRelevance();

  /**
   * Returns the source of this search result. This will usually be the site or
   * a site's module.
   * 
   * @return the source
   */
  Object getSource();

}