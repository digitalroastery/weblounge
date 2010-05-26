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

import ch.o2it.weblounge.common.content.Page;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge pages to solr.
 */
public class PageInputDocument extends SolrUpdateableInputDocument {

  /** Serial version uid */
  private static final long serialVersionUID = 1812364663819822015L;

  /** The solr supported date format. **/
  private DateFormat dateFormat = new SimpleDateFormat(SolrFields.SOLR_DATE_FORMAT);

  /**
   * Creates an input document for the given page.
   * 
   * @param page
   *          the page
   */
  public PageInputDocument(Page page) {
    init(page);
  }

  /**
   * Populates this input document with the page data.
   * 
   * @param page
   *          the page
   */
  private void init(Page page) {

  }

}
