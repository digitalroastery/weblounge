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

package ch.entwine.weblounge.contentrepository.impl.index;

import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PATH;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.RESOURCE_ID;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.TYPE;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.UID;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.VERSION;

import ch.entwine.weblounge.common.content.ResourceURI;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * updating resource paths.
 */
public class ResourceURIInputDocument extends ResourceMetadataCollection {

  /**
   * Creates an input document for the given uri. On update, only the uri will
   * change (namely the path), and the rest of the resource will remain
   * unchanged.
   * 
   * @param uri
   *          the resource uri
   */
  public ResourceURIInputDocument(ResourceURI uri) {
    init(uri);
  }

  /**
   * Populates this input document with the uri data.
   * 
   * @param uri
   *          the resource uri
   */
  private void init(ResourceURI uri) {
    addField(UID, uri.getUID(), false);
    addField(RESOURCE_ID, uri.getIdentifier(), false);
    addField(PATH, uri.getPath(), true);
    addField(TYPE, uri.getType(), false);
    addField(VERSION, uri.getVersion(), false);
  }

}
