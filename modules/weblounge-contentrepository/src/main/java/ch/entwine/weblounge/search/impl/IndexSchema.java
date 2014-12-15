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
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110_1301, USA.
 */

package ch.entwine.weblounge.search.impl;

/**
 * Interface defining the mapping between data and field names in solr.
 */
public interface IndexSchema {

  /** The unique identifier */
  String UID = "uid";

  /** Resource identifier */
  String RESOURCE_ID = "id";

  /** Path field name */
  String PATH = "path";

  /** Path elements */
  String PATH_PREFIX = "path_prefix";

  /** Type field name */
  String TYPE = "type";

  /** Version field name */
  String VERSION = "version";

  /** Alternate version field name */
  String ALTERNATE_VERSION = "alternate_version";

  /** Subjects field name */
  String SUBJECT = "subjects";

  /** Granted permissions field name */
  String ALLOWDENY_ALLOW_BY_ACTION = "allowdeny_allow_{0}";

  /** Template field name */
  String TEMPLATE = "template";

  /** Stationary field name */
  String STATIONARY = "stationary";

  /** Series field name */
  String SERIES = "series";

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

  /** Creator name field name */
  String CREATED_BY_NAME = "created_by_name";

  /** Modified field name */
  String MODIFIED = "modified";

  /** Modifier field name */
  String MODIFIED_BY = "modified_by";

  /** Modifier name field name */
  String MODIFIED_BY_NAME = "modified_by_name";

  /** Publishing start date field name */
  String PUBLISHED_FROM = "published_from";

  /** Publishing end date field name */
  String PUBLISHED_TO = "published_to";

  /** Publisher field name */
  String PUBLISHED_BY = "published_by";

  /** Publisher name field name */
  String PUBLISHED_BY_NAME = "published_by_name";

  /** Editor field name */
  String LOCKED_BY = "locked_by";

  /** Editor name field name */
  String LOCKED_BY_NAME = "locked_by_name";

  /** Owner field name */
  String OWNED_BY = "owned_by";

  /** Owner name field name */
  String OWNED_BY_NAME = "owned_by_name";

  /** Pagelet text and properties values */
  String PAGELET_CONTENTS = "pagelet_contents";

  /** Pagelet text and properties values (localized) */
  String PAGELET_CONTENTS_LOCALIZED = "pagelet_contents_language_{0}";

  /** Pagelet properties field name */
  String PAGELET_PROPERTIES = "pagelet_properties";

  /** Pagelet property value field name */
  String PAGELET_PROPERTY_VALUE = "pagelet_property_value";

  /** Pagelet xml field name (composer) */
  String PAGELET_XML_COMPOSER = "pagelet_xml_composer_{0}";

  /** Pagelet xml field name (position within composer) */
  String PAGELET_XML_COMPOSER_POSITION = "pagelet_xml_position_{0}";

  /** Pagelet type field name */
  String PAGELET_TYPE = "pagelet_type";

  /** Pagelet type field name (composer) */
  String PAGELET_TYPE_COMPOSER = "pagelet_type_composer_{0}";

  /** Pagelet type field name (position) */
  String PAGELET_TYPE_POSITION = "pagelet_type_position_{0}";

  /** Pagelet type field name (position within composer) */
  String PAGELET_TYPE_COMPOSER_POSITION = "pagelet_type_composer_{0}_position_{1}";

  /** Resource content source field name */
  String CONTENT_SOURCE = "content_source";

  /** Resource content external representation field name */
  String CONTENT_EXTERNAL_REPRESENTATION = "content_external_representation";

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

  /** Accumulative, language_sensitive fulltext field designed for backend use */
  String LOCALIZED_FULLTEXT = "fulltext_{0}";

  /** Accumulative fulltext field designed for backend use */
  String FULLTEXT = "fulltext";

  /** Accumulative fulltext field with analysis designed for fuzzy search backend use */
  String FULLTEXT_FUZZY = "fulltext_fuzzy";

  /** Accumulative, language_sensitive text field targeted at frontend use */
  String LOCALIZED_TEXT = "text_{0}";

  /** Accumulative text field targeted for frontend use */
  String TEXT = "text";

  /** Accumulative text field with analysis targeted for fuzzy search frontend use */
  String TEXT_FUZZY = "text_fuzzy";

  /** The date format */
  String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /** The solr highlighting tag to use. */
  String HIGHLIGHT_MATCH = "b";

  /** Boost values for ranking */
  double TITLE_BOOST = 6.0;

}
