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

package ch.entwine.weblounge.search.impl;

import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.content.movie.MovieResource;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge audio visual objects to solr.
 */
public class MovieInputDocument extends ResourceInputDocument {

  /**
   * Creates an input document for the given audio visual object.
   * 
   * @param audioVisual
   *          the audio visual object
   */
  public MovieInputDocument(MovieResource audioVisual) {
    init(audioVisual);
  }

  /**
   * Populates this input document with the resource data.
   * 
   * @param resource
   *          the resource
   */
  protected void init(FileResource resource) {
    super.init(resource);

    // TODO: Create preview and add it to the index

  }

}
