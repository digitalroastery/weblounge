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

  /** Type field name */
  public static final String TYPE = "type";

  /** Subjects field name */
  public static final String SUBJECT = "subjects";

  /** Template field name */
  public static final String TEMPLATE = "template";

  /** Page field name */
  public static final String XML = "page-xml";

  /** Page header field name */
  public static final String HEADER_XML = "page-header-xml";

  /** Preview field name */
  public static final String PREVIEW_XML = "preview-xml";

  /** Description field name */
  public static final String DESCRIPTION = "description";

  /** Description field name (localized) */
  public static final String DESCRIPTION_LOCALIZED = "description-{0}";

  /** Coverage field name */
  public static final String COVERAGE = "coverage";

  /** Coverage field name (localized) */
  public static final String COVERAGE_LOCALIZED = "coverage-{0}";

  /** Rights field name */
  public static final String RIGHTS = "rights";

  /** Rights field name (localized) */
  public static final String RIGHTS_LOCALIZED = "rights-{0}";

  /** Title field name */
  public static final String TITLE = "title";

  /** Title field name (localized) */
  public static final String TITLE_LOCALIZED = "title-{0}";

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

  /** Pagelet text and properties values */
  public static final String PAGELET_CONTENTS = "pagelet-contents";

  /** Pagelet text and properties values (localized) */
  public static final String PAGELET_CONTENTS_LOCALIZED = "pagelet-contents-{0}";

  /** Pagelet properties field name */
  public static final String PAGELET_PROPERTIES = "pagelet-properties";

  /** Pagelet xml field name (located) */
  public static final String PAGELET_XML_LOCATED = "pagelet-xml-{0}";

  /** Pagelet xml field name */
  public static final String PAGELET_TYPE = "pagelet-type";

  /** Pagelet xml field name (located) */
  public static final String PAGELET_TYPE_LOCATED = "pagelet-type-{0}";

  /** Resource content filename field name */
  public static final String CONTENT_FILENAME = "content-filename";

  /** Resource content filename field name (localized) */
  public static final String CONTENT_FILENAME_LOCALIZED = "content-filename-{0}";

  /** Resource content mime type field name */
  public static final String CONTENT_MIMETYPE = "content-mimetype";

  /** Resource content mime type field name (localized) */
  public static final String CONTENT_MIMETYPE_LOCALIZED = "content-mimetype-{0}";

  /** Resource content creation date field name */
  public static final String CONTENT_CREATED = "content-created-{0}";

  /** Resource content creator field name */
  public static final String CONTENT_CREATED_BY = "content-created-by-{0}";

  /** Resource content xml field name */
  public static final String CONTENT_XML = "content-xml-{0}";

  /** Solr ranking score */
  public static final String SCORE = "score";
  
  /** Accumulative, language-sensitive fulltext field */
  public static final String LOCALIZED_FULLTEXT = "fulltext-{0}";

  /** Accumulative fulltext field */
  public static final String FULLTEXT = "fulltext";

  /** The solr date format string tag. */
  public static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /** The solr highlighting tag to use. */
  public static final String HIGHLIGHT_MATCH = "b";

  /** Boost values for ranking */
  public static final double TITLE_BOOST = 6.0;

}
