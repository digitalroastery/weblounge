/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.contentrepository.impl.index.solr;

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.ID;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PATH;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TYPE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.VERSION;

import ch.o2it.weblounge.common.content.ResourceURI;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * updating resource paths.
 */
public class ResourceURIInputDocument extends AbstractInputDocument {

  /** Serial version uid */
  private static final long serialVersionUID = 1812364663819822016L;

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
    setField(ID, uri.getIdentifier());
    setField(PATH, uri.getPath());
    setField(TYPE, uri.getType());
    setField(VERSION, uri.getVersion());
  }

}
