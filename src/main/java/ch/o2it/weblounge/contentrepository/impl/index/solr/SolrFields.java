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

  /** Path field name */
  public static final String PATH = "path";

  /** Preview field name */
  public static final String PREVIEW_XML = "preview-xml";

  /** Description field name */
  public static final String DESCRIPTION = "description";

  /** Coverage field name */
  public static final String COVERAGE = "coverage";

  /** Rights field name */
  public static final String RIGHTS = "rights";

  /** Title field name */
  public static final String TITLE = "title";

  /** Created field name */
  public static final String CREATED = "created";

  /** Creator field name */
  public static final String CREATED_BY = "created-by";

  /** Modified field name */
  public static final String MODIFIED = "modified";

  /** Modifier field name */
  public static final String MODIFIED_BY = "modified-by";

  /** Publishing start date field name */
  public static final String PUBLISHED_FROM = "published-from";

  /** Publishing end date field name */
  public static final String PUBLISHED_TO = "published-to";

  /** Publisher field name */
  public static final String PUBLISHED_BY = "published-by";

  /** Pagelet text field name */
  public static final String PAGELET_TEXT = "pagelet-text-{0}-{1}";

  /** Pagelet properties field name */
  public static final String PAGELET_PROPERTIES = "pagelet-properties-{0}";

  /** Pagelet xml field name */
  public static final String PAGELET_XML = "pagelet-xml-{0}";

  /** Solr ranking score */
  public static final String SCORE = "score";
  
  /** Accumulative fulltext field */
  public static final String FULLTEXT = "fulltext";

  /** The solr date format string tag. */
  public static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /** The solr highlighting tag to use. */
  public static final String HIGHLIGHT_MATCH = "b";

  /** Boost values for ranking */
  public static final double TITLE_BOOST = 6.0;

}
