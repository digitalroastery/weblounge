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

import org.apache.solr.common.SolrInputDocument;

/**
 * Solr input document which adds not existing fields.
 */
public class SolrUpdateableInputDocument extends SolrInputDocument {

  /** Serial version id */
  private static final long serialVersionUID = -1984560839468950690L;

  /**
   * Update or add solr field.
   * 
   * @param name
   *          The field name.
   * @param value
   *          The value.
   */
  public void addField(String name, Object value) {
    if (!getFieldNames().contains(name))
      super.addField(name, value);
    else
      super.setField(name, value);
  }

}
