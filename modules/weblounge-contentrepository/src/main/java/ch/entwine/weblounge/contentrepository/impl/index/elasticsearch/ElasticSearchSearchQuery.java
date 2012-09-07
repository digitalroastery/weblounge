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

package ch.entwine.weblounge.contentrepository.impl.index.elasticsearch;

import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.ALTERNATE_VERSION;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.CONTENT_EXTERNAL_REPRESENTATION;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.CONTENT_FILENAME;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.CONTENT_MIMETYPE;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.CONTENT_SOURCE;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.CREATED;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.CREATED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.FULLTEXT;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.LOCKED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.MODIFIED;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.MODIFIED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PAGELET_PROPERTIES;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PAGELET_TYPE;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PAGELET_TYPE_COMPOSER;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PAGELET_TYPE_COMPOSER_POSITION;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PATH;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PATH_PREFIX;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PUBLISHED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.PUBLISHED_FROM;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.SERIES;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.STATIONARY;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.SUBJECT;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.TEMPLATE;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.TYPE;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexSchema.VERSION;
import static ch.entwine.weblounge.contentrepository.impl.index.IndexUtils.clean;

import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.contentrepository.impl.index.IndexSchema;
import ch.entwine.weblounge.contentrepository.impl.index.IndexUtils;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Weblounge implementation of the elastic search query builder.
 */
public class ElasticSearchSearchQuery extends BoolQueryBuilder {

  /** Term queries on fields */
  private Map<String, Set<Object>> searchTerms = null;

  /** Negative term queries on fields */
  private Map<String, Set<Object>> negativeSearchTerms = null;

  /** Fields that must be empty */
  private Set<String> emptyFields = null;

  /** Fields that must not be empty */
  private Set<String> nonEmptyFields = null;

  /**
   * Creates a new elastic search query based on the Weblounge query.
   * 
   * @param query
   *          the weblounge query
   */
  public ElasticSearchSearchQuery(SearchQuery query) {
    init(query);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.elasticsearch.index.query.BaseQueryBuilder#doXContent(org.elasticsearch.common.xcontent.XContentBuilder,
   *      org.elasticsearch.common.xcontent.ToXContent.Params)
   */
  protected void init(SearchQuery query) {

    // Resource id
    if (query.getIdentifier().length > 0) {
      and(IndexSchema.ID, query.getIdentifier(), true);
    }

    // Version / Preferred version
    if (query.getPreferredVersion() >= 0) {
      andNot(ALTERNATE_VERSION, Long.toString(query.getPreferredVersion()), false);
    } else if (query.getVersion() >= 0) {
      and(VERSION, Long.toString(query.getVersion()), false);
    }

    // Path
    if (query.getPath() != null) {
      and(PATH, query.getPath(), true);
    }

    if (query.getPathPrefix() != null) {
      and(PATH_PREFIX, query.getPathPrefix(), true);
    }

    // Type
    if (query.getTypes().length > 0) {
      and(TYPE, query.getTypes(), true);
    }

    // Without type
    if (query.getWithoutTypes().length > 0) {
      andNot(TYPE, query.getWithoutTypes(), true);
    }

    // Subjects (OR)
    if (query.getSubjects().length > 0) {
      and(SUBJECT, query.getSubjects(), true);
    }

    // Subjects (AND)
    if (query.getANDSubjects().length > 0) {
      for (String subject : query.getANDSubjects()) {
        and(SUBJECT, subject, true);
      }
    }

    // Series
    if (query.getSeries().length > 0) {
      and(SERIES, query.getSeries(), true);
    }

    // Template
    if (query.getTemplate() != null) {
      and(TEMPLATE, query.getTemplate(), true);
    }

    // Stationary
    if (query.isStationary()) {
      and(STATIONARY, Boolean.TRUE.toString(), false);
    }

    // Creator
    if (query.getCreator() != null) {
      and(CREATED_BY, IndexUtils.serializeUserId(query.getCreator()), true);
    }

    // Creation date
    if (query.getCreationDate() != null) {
      if (query.getCreationDateEnd() != null)
        and(CREATED, IndexUtils.serializeDateRange(query.getCreationDate(), query.getCreationDateEnd()), false);
      else
        and(CREATED, IndexUtils.selectDay(query.getCreationDate()), false);
    }

    // Modifier
    if (query.getModifier() != null) {
      and(MODIFIED_BY, IndexUtils.serializeUserId(query.getModifier()), true);
    }

    // Modification date
    if (query.getModificationDate() != null) {
      if (query.getCreationDateEnd() != null)
        and(MODIFIED, IndexUtils.serializeDateRange(query.getModificationDate(), query.getModificationDateEnd()), false);
      else
        and(MODIFIED, IndexUtils.selectDay(query.getModificationDate()), false);
    }

    // Without Modification
    if (query.getWithoutModification()) {
      andEmpty(MODIFIED);
    }

    // Publisher
    if (query.getPublisher() != null) {
      and(PUBLISHED_BY, IndexUtils.serializeUserId(query.getPublisher()), true);
    }

    // Publication date
    if (query.getPublishingDate() != null) {
      if (query.getCreationDateEnd() != null)
        and(PUBLISHED_FROM, IndexUtils.serializeDateRange(query.getPublishingDate(), query.getPublishingDateEnd()), false);
      else
        and(PUBLISHED_FROM, IndexUtils.selectDay(query.getPublishingDate()), false);
    }

    // Without Publication
    if (query.getWithoutPublication()) {
      andEmpty(PUBLISHED_FROM);
    }

    // Lock owner
    if (query.getLockOwner() != null) {
      User user = query.getLockOwner();
      if (SearchQueryImpl.ANY_USER.equals(user.getLogin()))
        andNotEmpty(LOCKED_BY);
      else
        and(LOCKED_BY, IndexUtils.serializeUserId(user), true);
    }

    // Pagelet elements
    for (Map.Entry<String, String> entry : query.getElements().entrySet()) {
      // TODO: Language?
      // solrQuery.append(" ").append(PAGELET_CONTENTS).append(":");
      // solrQuery.append("\"").append(entry.getKey()).append("=\"");
      // for (String contentValue : StringUtils.split(entry.getValue())) {
      // solrQuery.append(" \"").append(IndexUtils.clean(contentValue)).append("\"");
      // }
    }

    // Pagelet properties
    for (Map.Entry<String, String> entry : query.getProperties().entrySet()) {
      StringBuffer searchTerm = new StringBuffer();
      searchTerm.append(entry.getKey());
      searchTerm.append("=").append(entry.getValue());
      and(PAGELET_PROPERTIES, searchTerm.toString(), true);
    }

    // Pagelet types
    for (Pagelet pagelet : query.getPagelets()) {
      StringBuffer searchTerm = new StringBuffer();
      searchTerm.append(pagelet.getModule()).append("/").append(pagelet.getIdentifier());

      // Are we looking for the pagelet in a certain composer or position?
      PageletURI uri = pagelet.getURI();
      if (uri != null && (StringUtils.isNotBlank(uri.getComposer()) || uri.getPosition() >= 0)) {
        if (StringUtils.isNotBlank(uri.getComposer())) {
          String field = MessageFormat.format(PAGELET_TYPE_COMPOSER, uri.getComposer());
          and(field, searchTerm.toString(), true);
        }
        if (uri.getPosition() >= 0) {
          String field = MessageFormat.format(PAGELET_TYPE_COMPOSER_POSITION, uri.getPosition());
          and(field, searchTerm.toString(), true);
        }
      } else {
        and(PAGELET_TYPE, searchTerm.toString(), true);
      }
    }

    // Content filenames
    if (query.getFilename() != null) {
      and(CONTENT_FILENAME, query.getFilename(), true);
    }

    // Content source
    if (query.getSource() != null) {
      and(CONTENT_SOURCE, query.getSource(), true);
    }

    // Content external location
    if (query.getExternalLocation() != null) {
      and(CONTENT_EXTERNAL_REPRESENTATION, query.getExternalLocation().toExternalForm(), true);
    }

    // Content mime types
    if (query.getMimetype() != null) {
      and(CONTENT_MIMETYPE, query.getMimetype(), true);
    }

    // Fulltext
    if (query.getText() != null) {
      if (query.isWildcardSearch()) {
        and(FULLTEXT, clean(query.getText()) + "*", false);
      } else {
        for (String s : StringUtils.split(query.getText())) {
          and(FULLTEXT, s, true);
        }
      }
    }

    // Create the actual query

    // Terms
    if (searchTerms != null) {
      for (Map.Entry<String, Set<Object>> entry : searchTerms.entrySet()) {
        Set<Object> values = entry.getValue();
        if (values.size() == 1)
          must(new TermsQueryBuilder(entry.getKey(), values.iterator().next()));
        else
          must(new TermsQueryBuilder(entry.getKey(), values));
      }
    }

    // Negative terms
    if (negativeSearchTerms != null) {
      for (Map.Entry<String, Set<Object>> entry : negativeSearchTerms.entrySet()) {
        Set<Object> values = entry.getValue();
        if (values.size() == 1)
          mustNot(new TermsQueryBuilder(entry.getKey(), values.iterator().next()));
        else
          mustNot(new TermsQueryBuilder(entry.getKey(), values));
      }
    }

    // Non-Empty fields
    if (nonEmptyFields != null) {
      for (String field : nonEmptyFields) {
        QueryBuilders.filteredQuery(this, FilterBuilders.existsFilter(field));
      }
    }

    // Empty fields
    if (emptyFields != null) {
      for (String field : emptyFields) {
        QueryBuilders.filteredQuery(this, FilterBuilders.missingFilter(field));
      }
    }

    // Filter query
    if (query.getFilter() != null) {
      QueryBuilders.filteredQuery(this, FilterBuilders.wrapperFilter(query.getFilter()));
    }

  }

  /**
   * Stores <code>fieldValue</code> as a search term on the
   * <code>fieldName</code> field.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the field value
   * @param clean
   *          <code>true</code> to escape solr special characters in the field
   *          value
   */
  protected void and(String fieldName, Object fieldValue, boolean clean) {

    // Fix the field name, just in case
    fieldName = StringUtils.trim(fieldName);

    // Make sure the data structures are set up accordingly
    if (searchTerms == null)
      searchTerms = new HashMap<String, Set<Object>>();
    Set<Object> termValues = searchTerms.get(fieldName);
    if (termValues == null) {
      termValues = new HashSet<Object>();
      searchTerms.put(fieldName, termValues);
    }

    // Add the term
    termValues.add(fieldValue);
  }

  /**
   * Stores <code>fieldValues</code> as search terms on the
   * <code>fieldName</code> field.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValues
   *          the field value
   * @param clean
   *          <code>true</code> to escape solr special characters in the field
   *          value
   */
  protected void and(String fieldName, Object[] fieldValues, boolean clean) {
    for (Object v : fieldValues) {
      and(fieldName, v, clean);
    }
  }

  /**
   * Stores <code>fieldValue</code> as a negative search term on the
   * <code>fieldName</code> field.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the field value
   * @param clean
   *          <code>true</code> to escape solr special characters in the field
   *          value
   */
  protected void andNot(String fieldName, Object fieldValue, boolean clean) {

    // Fix the field name, just in case
    fieldName = StringUtils.trim(fieldName);

    // Make sure the data structures are set up accordingly
    if (negativeSearchTerms == null)
      negativeSearchTerms = new HashMap<String, Set<Object>>();
    Set<Object> termValues = negativeSearchTerms.get(fieldName);
    if (termValues == null) {
      termValues = new HashSet<Object>();
      negativeSearchTerms.put(fieldName, termValues);
    }

    // Add the term
    termValues.add(fieldValue);
  }

  /**
   * Stores <code>fieldValues</code> as negative search terms on the
   * <code>fieldName</code> field.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValues
   *          the field value
   * @param clean
   *          <code>true</code> to escape solr special characters in the field
   *          value
   */
  protected void andNot(String fieldName, Object[] fieldValues, boolean clean) {
    for (Object v : fieldValues) {
      andNot(fieldName, v, clean);
    }
  }

  /**
   * Encodes the field name as part of the AND clause of a solr query:
   * <tt>AND -fieldName : [* TO *]</tt>.
   * 
   * @param fieldName
   *          the field name
   */
  protected void andEmpty(String fieldName) {
    if (emptyFields == null)
      emptyFields = new HashSet<String>();
    emptyFields.add(StringUtils.trim(fieldName));
  }

  /**
   * Encodes the field name as part of the AND clause of a solr query:
   * <tt>AND fieldName : ["" TO *]</tt>.
   * 
   * @param fieldName
   *          the field name
   */
  protected void andNotEmpty(String fieldName) {
    if (nonEmptyFields == null)
      nonEmptyFields = new HashSet<String>();
    nonEmptyFields.add(StringUtils.trim(fieldName));
  }

}
