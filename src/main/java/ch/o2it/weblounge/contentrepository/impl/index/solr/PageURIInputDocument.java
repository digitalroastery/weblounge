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

import ch.o2it.weblounge.common.content.PageURI;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * updating page paths.
 */
public class PageURIInputDocument extends AbstractInputDocument {

  /** Serial version uid */
  private static final long serialVersionUID = 1812364663819822016L;

  /**
   * Creates an input document for the given uri. On update, only the uri will
   * change (namely the path), and the rest of the page will remain empty.
   * 
   * @param uri
   *          the page uri
   */
  public PageURIInputDocument(PageURI uri) {
    init(uri);
  }

  /**
   * Populates this input document with the page data.
   * 
   * @param uri
   *          the page
   */
  private void init(PageURI uri) {
    setField(ID, uri.getId());
    setField(PATH, uri.getPath());
  }

}
