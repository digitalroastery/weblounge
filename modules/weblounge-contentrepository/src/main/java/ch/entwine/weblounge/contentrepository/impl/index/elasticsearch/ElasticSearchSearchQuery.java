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

import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.contentrepository.impl.index.IndexSchema;
import ch.entwine.weblounge.contentrepository.impl.index.IndexUtils;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.io.BytesStream;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilderException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.TextQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Weblounge implementation of the elastic search query builder.
 */
public class ElasticSearchSearchQuery implements QueryBuilder {

  /** Term queries on fields */
  private Map<String, Set<Object>> searchTerms = null;

  /** Negative term queries on fields */
  private Map<String, Set<Object>> negativeSearchTerms = null;

  /** Fields that must be empty */
  private Set<String> emptyFields = null;

  /** Fields that must not be empty */
  private Set<String> nonEmptyFields = null;

  /** Fields that query a date range */
  private Set<DateRange> dateRanges = null;

  /** Filter expression */
  private String filter = null;

  /** Text query */
  private String text = null;

  /** Wildcard text query */
  private String wildcardText = null;

  /** The boolean query */
  private QueryBuilder queryBuilder = null;

  /**
   * Creates a new elastic search query based on the Weblounge query.
   * 
   * @param query
   *          the weblounge query
   */
  public ElasticSearchSearchQuery(SearchQuery query) {
    init(query);
    createQuery();
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
      and(IndexSchema.RESOURCE_ID, query.getIdentifier(), true);
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
        and(CREATED, query.getCreationDate(), query.getCreationDateEnd());
      else
        and(CREATED, IndexUtils.beginningOfDay(query.getCreationDate()), IndexUtils.endOfDay(query.getCreationDate()));
    }

    // Modifier
    if (query.getModifier() != null) {
      and(MODIFIED_BY, IndexUtils.serializeUserId(query.getModifier()), true);
    }

    // Modification date
    if (query.getModificationDate() != null) {
      if (query.getCreationDateEnd() != null)
        and(MODIFIED, query.getModificationDate(), query.getModificationDateEnd());
      else {
        and(MODIFIED, IndexUtils.beginningOfDay(query.getModificationDate()), IndexUtils.endOfDay(query.getModificationDate()));
      }
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
        and(PUBLISHED_FROM, query.getPublishingDate(), query.getPublishingDateEnd());
      else
        and(PUBLISHED_FROM, IndexUtils.beginningOfDay(query.getPublishingDate()), IndexUtils.endOfDay(query.getPublishingDate()));
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
    // for (Map.Entry<String, String> entry : query.getElements().entrySet()) {
    // TODO: Language?
    // solrQuery.append(" ").append(PAGELET_CONTENTS).append(":");
    // solrQuery.append("\"").append(entry.getKey()).append("=\"");
    // for (String contentValue : StringUtils.split(entry.getValue())) {
    // solrQuery.append(" \"").append(IndexUtils.clean(contentValue)).append("\"");
    // }
    // }

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
        wildcardText = query.getText() + "*";
      } else {
        text = query.getText();
      }
    }

    // Filter query
    if (query.getFilter() != null) {
      this.filter = query.getFilter();
    }
  }

  /**
   * Create the actual query. We start with a query that matches everything,
   * then move to the boolean conditions, finally add filter queries.
   */
  private void createQuery() {

    queryBuilder = new MatchAllQueryBuilder();

    // The boolean query builder
    BoolQueryBuilder booleanQuery = new BoolQueryBuilder();

    // Terms
    if (searchTerms != null) {
      for (Map.Entry<String, Set<Object>> entry : searchTerms.entrySet()) {
        Set<Object> values = entry.getValue();
        if (values.size() == 1)
          booleanQuery.must(new TermsQueryBuilder(entry.getKey(), values.iterator().next()));
        else
          booleanQuery.must(new TermsQueryBuilder(entry.getKey(), values.toArray(new String[values.size()])));
      }
      this.queryBuilder = booleanQuery;
    }

    // Negative terms
    if (negativeSearchTerms != null) {
      for (Map.Entry<String, Set<Object>> entry : negativeSearchTerms.entrySet()) {
        Set<Object> values = entry.getValue();
        if (values.size() == 1)
          booleanQuery.mustNot(new TermsQueryBuilder(entry.getKey(), values.iterator().next()));
        else
          booleanQuery.mustNot(new TermsQueryBuilder(entry.getKey(), values.toArray(new String[values.size()])));
      }
      this.queryBuilder = booleanQuery;
    }

    // Date ranges
    if (dateRanges != null) {
      for (DateRange dr : dateRanges) {
        booleanQuery.must(dr.getQueryBuilder());
      }
      this.queryBuilder = booleanQuery;
    }

    // Text
    if (text != null) {
      TextQueryBuilder textQueryBuilder = QueryBuilders.textQuery(FULLTEXT, text);
      booleanQuery.must(textQueryBuilder);
      this.queryBuilder = booleanQuery;
    }

    // Wildcard text
    if (wildcardText != null) {
      WildcardQueryBuilder wcQueryBuilder = QueryBuilders.wildcardQuery(FULLTEXT, wildcardText);
      booleanQuery.must(wcQueryBuilder);
      this.queryBuilder = booleanQuery;
    }

    // Non-Empty fields
    if (nonEmptyFields != null) {
      for (String field : nonEmptyFields) {
        this.queryBuilder = QueryBuilders.filteredQuery(queryBuilder, FilterBuilders.existsFilter(field));
      }
    }

    // Empty fields
    if (emptyFields != null) {
      for (String field : emptyFields) {
        this.queryBuilder = QueryBuilders.filteredQuery(queryBuilder, FilterBuilders.missingFilter(field));
      }
    }

    // Filter expressions
    if (filter != null) {
      this.queryBuilder = QueryBuilders.filteredQuery(queryBuilder, FilterBuilders.wrapperFilter(filter));
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
   * Stores <code>fieldValue</code> as a search term on the
   * <code>fieldName</code> field.
   * 
   * @param fieldName
   *          the field name
   * @param startDate
   *          the start date
   * @param endDate
   *          the end date
   */
  protected void and(String fieldName, Date startDate, Date endDate) {

    // Fix the field name, just in case
    fieldName = StringUtils.trim(fieldName);

    // Make sure the data structures are set up accordingly
    if (dateRanges == null)
      dateRanges = new HashSet<DateRange>();

    // Add the term
    DateRange dateRange = new DateRange(fieldName, startDate, endDate);
    dateRanges.add(dateRange);
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

  /**
   * {@inheritDoc}
   * 
   * @see org.elasticsearch.common.xcontent.ToXContent#toXContent(org.elasticsearch.common.xcontent.XContentBuilder,
   *      org.elasticsearch.common.xcontent.ToXContent.Params)
   */
  @Override
  public XContentBuilder toXContent(XContentBuilder builder, Params params)
      throws IOException {
    return queryBuilder.toXContent(builder, params);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.elasticsearch.index.query.QueryBuilder#buildAsBytes()
   */
  @Override
  public BytesStream buildAsBytes() throws QueryBuilderException {
    return queryBuilder.buildAsBytes();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.elasticsearch.index.query.QueryBuilder#buildAsBytes(org.elasticsearch.common.xcontent.XContentType)
   */
  @Override
  public BytesStream buildAsBytes(XContentType contentType) {
    return queryBuilder.buildAsBytes(contentType);
  }

  /**
   * Utility class to hold date range specifications and turn them into elastic
   * search queries.
   */
  private static final class DateRange {

    /** The field name */
    private String field = null;

    /** The start date */
    private Date startDate = null;

    /** The end date */
    private Date endDate = null;

    /**
     * Creates a new date range specification with the given field name, start
     * and end dates. <code>null</code> may be passed in for start or end dates
     * that should remain unspecified.
     * 
     * @param field
     *          the field name
     * @param start
     *          the start date
     * @param end
     *          the end date
     */
    DateRange(String field, Date start, Date end) {
      this.field = field;
      this.startDate = start;
      this.endDate = end;
    }

    /**
     * Returns the range query that is represented by this date range.
     * 
     * @return the range query builder
     */
    QueryBuilder getQueryBuilder() {
      RangeQueryBuilder rqb = new RangeQueryBuilder(field);
      if (startDate != null)
        rqb.from(startDate.getTime());
      if (endDate != null)
        rqb.to(endDate.getTime());
      return rqb;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof DateRange) {
        return ((DateRange) obj).field.equals(field);
      }
      return false;
    }

  }

}
