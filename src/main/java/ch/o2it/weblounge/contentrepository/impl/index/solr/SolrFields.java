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

/**
 * Interface defining the mapping between data and field names in solr.
 */
public interface SolrFields {

  public static final String ID = "id";

  /** Solr ranking score */
  public static final String SCORE = "score";

  /** Title field name */
  public static final String TITLE = "title";
  
  /** Accumulative fulltext field */
  public static final String FULLTEXT = "fulltext";

  /** The solr date format string tag. */
  public static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /** The solr highlighting tag to use. */
  public static final String HIGHLIGHT_MATCH = "b";

  /** Boost values for ranking */
  public static final double TITLE_BOOST = 6.0;

}
