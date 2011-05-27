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
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110_1301, USA.
 */

package ch.o2it.weblounge.contentrepository.impl.index.solr;

/**
 * Interface defining the mapping between data and field names in solr.
 */
public interface SolrFields {

  String ID = "id";

  /** Path field name */
  String PATH = "path";

  /** Type field name */
  String TYPE = "type";

  /** Version field name */
  String VERSION = "version";

  /** Subjects field name */
  String SUBJECT = "subjects";

  /** Template field name */
  String TEMPLATE = "template";

  /** Page field name */
  String XML = "page_xml";

  /** Page header field name */
  String HEADER_XML = "page_header_xml";

  /** Preview field name */
  String PREVIEW_XML = "preview_xml";

  /** Description field name */
  String DESCRIPTION = "description";

  /** Description field name (localized) */
  String DESCRIPTION_LOCALIZED = "description_{0}";

  /** Coverage field name */
  String COVERAGE = "coverage";

  /** Coverage field name (localized) */
  String COVERAGE_LOCALIZED = "coverage_{0}";

  /** Rights field name */
  String RIGHTS = "rights";

  /** Rights field name (localized) */
  String RIGHTS_LOCALIZED = "rights_{0}";

  /** Title field name */
  String TITLE = "title";

  /** Title field name (localized) */
  String TITLE_LOCALIZED = "title_{0}";

  /** Created field name */
  String CREATED = "created";

  /** Creator field name */
  String CREATED_BY = "created_by";

  /** Modified field name */
  String MODIFIED = "modified";

  /** Modifier field name */
  String MODIFIED_BY = "modified_by";

  /** Publishing start date field name */
  String PUBLISHED_FROM = "published_from";

  /** Publishing end date field name */
  String PUBLISHED_TO = "published_to";

  /** Publisher field name */
  String PUBLISHED_BY = "published_by";

  /** Pagelet text and properties values */
  String PAGELET_CONTENTS = "pagelet_contents";

  /** Pagelet text and properties values (localized) */
  String PAGELET_CONTENTS_LOCALIZED = "pagelet_contents_language_{0}";

  /** Pagelet properties field name */
  String PAGELET_PROPERTIES = "pagelet_properties";

  /** Pagelet xml field name (composer) */
  String PAGELET_XML_COMPOSER = "pagelet_xml_composer_{0}";

  /** Pagelet xml field name (position within composer) */
  String PAGELET_XML_COMPOSER_POSITION = "pagelet_xml_position_{0}";

  /** Pagelet type field name */
  String PAGELET_TYPE = "pagelet_type";

  /** Pagelet type field name (composer) */
  String PAGELET_TYPE_COMPOSER = "pagelet_type_composer_{0}";

  /** Pagelet type field name (position within composer) */
  String PAGELET_TYPE_COMPOSER_POSITION = "pagelet_type_position_{0}";

  /** Resource content filename field name */
  String CONTENT_FILENAME = "content_filename";

  /** Resource content filename field name (localized) */
  String CONTENT_FILENAME_LOCALIZED = "content_filename_{0}";

  /** Resource content mime type field name */
  String CONTENT_MIMETYPE = "content_mimetype";

  /** Resource content mime type field name (localized) */
  String CONTENT_MIMETYPE_LOCALIZED = "content_mimetype_{0}";

  /** Resource content creation date field name */
  String CONTENT_CREATED = "content_created_{0}";

  /** Resource content creator field name */
  String CONTENT_CREATED_BY = "content_created_by_{0}";

  /** Resource content xml field name */
  String CONTENT_XML = "content_xml_{0}";

  /** Solr ranking score */
  String SCORE = "score";
  
  /** Accumulative, language_sensitive fulltext field */
  String LOCALIZED_FULLTEXT = "fulltext_{0}";

  /** Accumulative fulltext field */
  String FULLTEXT = "fulltext";

  /** The solr date format string tag. */
  String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /** The solr highlighting tag to use. */
  String HIGHLIGHT_MATCH = "b";

  /** Boost values for ranking */
  double TITLE_BOOST = 6.0;

}
