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

package ch.entwine.weblounge.search.impl.elasticsearch;

import static ch.entwine.weblounge.search.impl.IndexSchema.ALTERNATE_VERSION;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_EXTERNAL_REPRESENTATION;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_FILENAME;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_MIMETYPE;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_SOURCE;
import static ch.entwine.weblounge.search.impl.IndexSchema.CREATED;
import static ch.entwine.weblounge.search.impl.IndexSchema.CREATED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.FULLTEXT;
import static ch.entwine.weblounge.search.impl.IndexSchema.FULLTEXT_FUZZY;
import static ch.entwine.weblounge.search.impl.IndexSchema.LOCKED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.MODIFIED;
import static ch.entwine.weblounge.search.impl.IndexSchema.MODIFIED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.PAGELET_PROPERTIES;
import static ch.entwine.weblounge.search.impl.IndexSchema.PAGELET_TYPE;
import static ch.entwine.weblounge.search.impl.IndexSchema.PAGELET_TYPE_COMPOSER;
import static ch.entwine.weblounge.search.impl.IndexSchema.PAGELET_TYPE_COMPOSER_POSITION;
import static ch.entwine.weblounge.search.impl.IndexSchema.PAGELET_TYPE_POSITION;
import static ch.entwine.weblounge.search.impl.IndexSchema.PATH;
import static ch.entwine.weblounge.search.impl.IndexSchema.PATH_PREFIX;
import static ch.entwine.weblounge.search.impl.IndexSchema.PUBLISHED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.PUBLISHED_FROM;
import static ch.entwine.weblounge.search.impl.IndexSchema.SERIES;
import static ch.entwine.weblounge.search.impl.IndexSchema.STATIONARY;
import static ch.entwine.weblounge.search.impl.IndexSchema.SUBJECT;
import static ch.entwine.weblounge.search.impl.IndexSchema.TEMPLATE;
import static ch.entwine.weblounge.search.impl.IndexSchema.TEXT;
import static ch.entwine.weblounge.search.impl.IndexSchema.TEXT_FUZZY;
import static ch.entwine.weblounge.search.impl.IndexSchema.TYPE;
import static ch.entwine.weblounge.search.impl.IndexSchema.VERSION;

import static ch.entwine.weblounge.common.content.SearchQuery.Quantifier.All;

import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchQuery.Quantifier;
import ch.entwine.weblounge.common.content.SearchTerms;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.SystemAuthorities;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.search.impl.IndexSchema;
import ch.entwine.weblounge.search.impl.IndexUtils;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FuzzyLikeThisQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilderException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsFilterBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Weblounge implementation of the elastic search query builder.
 */
public class ElasticSearchSearchQuery implements QueryBuilder {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ElasticSearchSearchQuery.class);

  /** The access control actions to check for */
  private Set<String> actions = new HashSet<>();;

  /**
   * The set of authorities against which the access control lists have to be
   * checked
   */
  private Set<String> authorities = new HashSet<>();;

  /** Term queries on fields */
  private Map<String, Set<Object>> searchTerms = null;

  /** Negative term queries on fields */
  private Map<String, Set<Object>> negativeSearchTerms = null;

  /** Fields that must be empty */
  private Set<String> emptyFields = null;

  /** Fields that need to match all values */
  private List<ValueGroup> groups = null;

  /** Fields that must not be empty */
  private Set<String> nonEmptyFields = null;

  /** Fields that query a date range */
  private Set<DateRange> dateRanges = null;

  /** Filter expression */
  private String filter = null;

  /** Text query */
  private String text = null;

  /** Fuzzy text query */
  private String fuzzyText = null;

  /** Fulltext query */
  private String fulltext = null;

  /** Fuzzy fulltext query */
  private String fuzzyFulltext = null;

  /** True if more recent documents should be boosted */
  private boolean recencyBoost = false;

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

    // Subjects
    if (query.getSubjects() != null) {
      for (SearchTerms<String> terms : query.getSubjects()) {
        for (String subject : terms.getTerms()) {
          and(SUBJECT, subject, true);
        }
        if (Quantifier.All.equals(terms.getQuantifier())) {
          if (groups == null)
            groups = new ArrayList<ValueGroup>();
          groups.add(new ValueGroup(SUBJECT, (Object[]) terms.getTerms().toArray(new String[terms.size()])));
        }
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

    // Access rights
    // -------------
    // Take all required actions of the query into account for building the
    // request
    for (Action action : query.getActions()) {
      actions.add(action.getContext() + action.getIdentifier());
    }
    // Make sure we always check for the 'any'-authority
    authorities.add(SystemAuthorities.ANY.getAuthorityId());
    // Take all other authorities of the current user into account for building
    // the request
    if (SecurityUtils.getUser() != null) {
      for (Role role : SecurityUtils.getRoles(SecurityUtils.getUser())) {
        authorities.add(role.getAuthorityId());
      }
    }

    // Pagelet properties
    for (Map.Entry<String, String> entry : query.getProperties().entrySet()) {
      StringBuffer searchTerm = new StringBuffer();
      searchTerm.append(entry.getKey());
      searchTerm.append("=").append(entry.getValue());
      and(PAGELET_PROPERTIES, searchTerm.toString(), true);
    }

    // Pagelet types
    if (query.getPagelets() != null) {
      for (SearchTerms<Pagelet> terms : query.getPagelets()) {
        String field = null;
        for (Pagelet pagelet : terms.getTerms()) {
          String oldField = field;

          StringBuffer searchTerm = new StringBuffer();
          searchTerm.append(pagelet.getModule()).append("/").append(pagelet.getIdentifier());

          // Are we looking for the pagelet in a certain composer or position?
          PageletURI uri = pagelet.getURI();
          if (uri != null) {
            if (StringUtils.isNotBlank(uri.getComposer()) && uri.getPosition() >= 0) {
              field = MessageFormat.format(PAGELET_TYPE_COMPOSER_POSITION, uri.getComposer(), uri.getPosition());
              and(field, searchTerm.toString(), true);
            } else if (StringUtils.isNotBlank(uri.getComposer())) {
              field = MessageFormat.format(PAGELET_TYPE_COMPOSER, uri.getComposer());
              and(field, searchTerm.toString(), true);
            } else if (uri.getPosition() >= 0) {
              field = MessageFormat.format(PAGELET_TYPE_POSITION, uri.getPosition());
              and(field, searchTerm.toString(), true);
            } else {
              field = PAGELET_TYPE;
              and(field, searchTerm.toString(), true);
            }
          } else {
            field = PAGELET_TYPE;
            and(field, searchTerm.toString(), true);
          }

          if (All.equals(terms.getQuantifier()) && oldField != null && !oldField.equals(field)) {
            logger.warn("Queries based on pagelets and the 'all' quantifier need to use the same field");
          }
        }

        // Add filters to support AND queries
        if (All.equals(terms.getQuantifier())) {
          if (groups == null)
            groups = new ArrayList<ValueGroup>();
          List<String> pagelets = new ArrayList<String>(terms.size());
          for (Pagelet p : terms.getTerms())
            pagelets.add(p.toString());
          groups.add(new ValueGroup(field, (Object[]) pagelets.toArray(new String[pagelets.size()])));
        }
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
    if (query.getFulltext() != null) {
      for (SearchTerms<String> terms : query.getFulltext()) {
        StringBuffer queryText = new StringBuffer();
        for (String term : terms.getTerms()) {
          if (queryText.length() > 0)
            queryText.append(" ");
          queryText.append(term);
        }
        if (query.isFuzzySearch())
          fuzzyFulltext = queryText.toString();
        else
          fulltext = queryText.toString();
        if (All.equals(terms.getQuantifier())) {
          if (groups == null)
            groups = new ArrayList<ValueGroup>();
          if (query.isFuzzySearch()) {
            logger.warn("All quantifier not supported in conjunction with wildcard fulltext");
          }
          groups.add(new ValueGroup(FULLTEXT, (Object[]) terms.getTerms().toArray(new String[terms.size()])));
        }
      }
    }

    // Text
    if (query.getTerms() != null) {
      for (SearchTerms<String> terms : query.getTerms()) {
        StringBuffer queryText = new StringBuffer();
        for (String term : terms.getTerms()) {
          if (queryText.length() > 0)
            queryText.append(" ");
          queryText.append(term);
        }
        if (query.isFuzzySearch())
          fuzzyText = queryText.toString();
        else
          this.text = queryText.toString();
        if (All.equals(terms.getQuantifier())) {
          if (groups == null)
            groups = new ArrayList<ValueGroup>();
          if (query.isFuzzySearch()) {
            logger.warn("All quantifier not supported in conjunction with wildcard text");
          }
          groups.add(new ValueGroup(TEXT, (Object[]) terms.getTerms().toArray(new String[terms.size()])));
        }
      }
    }

    // Filter query
    if (query.getFilter() != null) {
      this.filter = query.getFilter();
    }

    // Recency boost
    this.recencyBoost = query.getRecencyPriority();

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
      MatchQueryBuilder textQueryBuilder = QueryBuilders.matchQuery(TEXT, text);
      booleanQuery.must(textQueryBuilder);
      this.queryBuilder = booleanQuery;
    }

    // Fuzzy text
    if (fuzzyText != null) {
      FuzzyLikeThisQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyLikeThisQuery(TEXT_FUZZY).likeText(fuzzyText);
      booleanQuery.must(fuzzyQueryBuilder);
      this.queryBuilder = booleanQuery;
    }

    // Fulltext
    if (fulltext != null) {
      MatchQueryBuilder textQueryBuilder = QueryBuilders.matchQuery(FULLTEXT, fulltext);
      booleanQuery.must(textQueryBuilder);
      this.queryBuilder = booleanQuery;
    }

    // Fuzzy fulltext
    if (fuzzyFulltext != null) {
      FuzzyLikeThisQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyLikeThisQuery(FULLTEXT_FUZZY).likeText(fuzzyFulltext);
      booleanQuery.must(fuzzyQueryBuilder);
      this.queryBuilder = booleanQuery;
    }

    // Recency boost. We differentiate between various (random) levels of
    // recency
    if (recencyBoost) {
      Calendar date = Calendar.getInstance();
      // Last week
      RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(MODIFIED);
      date.add(Calendar.WEEK_OF_YEAR, -1);
      rangeQueryBuilder.gt(date.getTimeInMillis()).boost(10);
      booleanQuery.should(rangeQueryBuilder);
      // Last month
      rangeQueryBuilder = QueryBuilders.rangeQuery(MODIFIED);
      date.add(Calendar.WEEK_OF_YEAR, -3);
      rangeQueryBuilder.gt(date.getTimeInMillis()).boost(5);
      booleanQuery.should(rangeQueryBuilder);
      // Last year
      rangeQueryBuilder = QueryBuilders.rangeQuery(MODIFIED);
      date.add(Calendar.MONTH, -5);
      rangeQueryBuilder.gt(date.getTimeInMillis()).boost(2);
      booleanQuery.should(rangeQueryBuilder);
    }

    QueryBuilder unfilteredQuery = queryBuilder;
    List<FilterBuilder> filters = new ArrayList<FilterBuilder>();

    // Access control
    // --------------
    // Build general access control filter which may consists of many of several
    // filters combined by logical AND
    boolean filterByAccessControl = false;
    AndFilterBuilder actionsFilter = new AndFilterBuilder();

    // Add filter for each action to check if the user's authorities are part of
    // it
    for (String action : actions) {
      actionsFilter.add(new TermsFilterBuilder(MessageFormat.format(IndexSchema.ALLOWDENY_ALLOW_BY_ACTION, action), authorities));
      filterByAccessControl = true;
    }
    if (filterByAccessControl)
      filters.add(actionsFilter);

    // Add filtering for AND terms
    if (groups != null) {
      for (ValueGroup group : groups) {
        filters.addAll(group.getFilterBuilders());
      }
    }

    // Non-Empty fields
    if (nonEmptyFields != null) {
      for (String field : nonEmptyFields) {
        filters.add(FilterBuilders.existsFilter(field));
      }
    }

    // Empty fields
    if (emptyFields != null) {
      for (String field : emptyFields) {
        filters.add(FilterBuilders.missingFilter(field));
      }
    }

    // Filter expressions
    if (filter != null) {
      filters.add(FilterBuilders.termFilter(FULLTEXT, filter));
    }

    // Apply the filters
    if (filters.size() == 1) {
      this.queryBuilder = QueryBuilders.filteredQuery(unfilteredQuery, filters.get(0));
    } else if (filters.size() > 1) {
      FilterBuilder andFilter = FilterBuilders.andFilter(filters.toArray(new FilterBuilder[filters.size()]));
      this.queryBuilder = QueryBuilders.filteredQuery(unfilteredQuery, andFilter);
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
  public BytesReference buildAsBytes() throws QueryBuilderException {
    return queryBuilder.buildAsBytes();
  }

  /**
   * {@inheritDoc}
   *
   * @see org.elasticsearch.index.query.QueryBuilder#buildAsBytes(org.elasticsearch.common.xcontent.XContentType)
   */
  @Override
  public BytesReference buildAsBytes(XContentType contentType) {
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

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return field.hashCode();
    }

  }

  /**
   * Stores a group of values which will later be added to the query using AND.
   */
  private static final class ValueGroup {

    /** The field name */
    private String field = null;

    /** The values to store */
    private Object[] values = null;

    /**
     * Creates a new value group for the given field and values.
     *
     * @param field
     *          the field name
     * @param values
     *          the values
     */
    ValueGroup(String field, Object... values) {
      this.field = field;
      this.values = values;
    }

    /**
     * Returns the filter that will make sure only documents are returned that
     * match all of the values at once.
     *
     * @return the filter builder
     */
    List<FilterBuilder> getFilterBuilders() {
      List<FilterBuilder> filters = new ArrayList<FilterBuilder>(values.length);
      for (Object v : values) {
        filters.add(FilterBuilders.termFilter(field, v.toString()));
      }
      return filters;
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

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return field.hashCode();
    }

  }

}
