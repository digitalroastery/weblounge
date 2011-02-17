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

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_FILENAME;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_MIMETYPE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.FULLTEXT;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.HEADER_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.ID;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_CONTENTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_PROPERTIES;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_TYPE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_TYPE_COMPOSER;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_TYPE_COMPOSER_POSITION;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PATH;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PREVIEW_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_FROM;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.SCORE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.SUBJECT;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TEMPLATE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TITLE_BOOST;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TITLE_LOCALIZED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TYPE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrUtils.clean;

import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.content.page.PageletURI;
import ch.o2it.weblounge.common.impl.content.SearchResultImpl;
import ch.o2it.weblounge.common.impl.content.SearchResultPageItemImpl;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class implementing <code>LookupRequester</code> to provide connection to solr
 * indexing facility.
 */
public class SolrRequester {

  /** Logging facility */
  private static Logger logger = LoggerFactory.getLogger(SolrRequester.class);

  /** The connection to the solr database */
  private SolrConnection solrConnection = null;

  /**
   * Creates a new requester for solr that will be using the given connection
   * object to query the search index.
   * 
   * @param connection
   *          the solr connection
   */
  public SolrRequester(SolrConnection connection) {
    if (connection == null)
      throw new IllegalStateException("Unable to run queries on null connection");
    this.solrConnection = connection;
  }

  /**
   * Executes a search operation using the given query and returns the result
   * range as specified by the parameters <code>offset</code> and
   * <code>limit</code>.
   * 
   * @param query
   *          the search query
   * @return the search result
   * @throws SolrServerException
   *           if executing the search operation fails
   */
  public SearchResult getByQuery(SearchQuery query) throws SolrServerException {
    Site site = query.getSite();

    // Build the solr query string
    StringBuilder solrQuery = new StringBuilder();

    // Id
    if (query.getIdentifier() != null) {
      and(solrQuery, ID, query.getIdentifier(), true, true);
    }

    // Path
    if (query.getPath() != null) {
      and(solrQuery, PATH, query.getPath(), true, true);
    }
    
    // Type
    if (query.getType() != null) {
      and(solrQuery, TYPE, query.getType(), true, true);
    }

    // Subjects
    if (query.getSubjects().length > 0) {
      and(solrQuery, SUBJECT, query.getSubjects(), true, true);
    }

    // Template
    if (query.getTemplate() != null) {
      and(solrQuery, TEMPLATE, query.getTemplate(), true, true);
    }

    // Creator
    if (query.getCreator() != null) {
      and(solrQuery, CREATED_BY, SolrUtils.serializeUser(query.getCreator()), true, true);
    }

    // Creation date
    if (query.getCreationDate() != null) {
      and(solrQuery, CREATED, SolrUtils.selectDay(query.getCreationDate()), false, false);
    }

    // Modifier
    if (query.getModifier() != null) {
      and(solrQuery, MODIFIED_BY, SolrUtils.serializeUser(query.getModifier()), true, true);
    }

    // Modification date
    if (query.getModificationDate() != null) {
      and(solrQuery, MODIFIED, SolrUtils.selectDay(query.getModificationDate()), false, false);
    }

    // Publisher
    if (query.getPublisher() != null) {
      and(solrQuery, PUBLISHED_BY, SolrUtils.serializeUser(query.getPublisher()), true, true);
    }

    // Publication date
    if (query.getPublishingDate() != null) {
      and(solrQuery, PUBLISHED_FROM, SolrUtils.selectDay(query.getPublishingDate()), false, false);
    }

    // Pagelet elements
    for (Map.Entry<String, String> entry : query.getElements().entrySet()) {
      // TODO: Language?
      solrQuery.append(" ").append(PAGELET_CONTENTS).append(":");
      solrQuery.append("\"").append(entry.getKey()).append(":=\"");
      int i = 0;
      for (String contentValue : StringUtils.split(entry.getValue())) {
        solrQuery.append(" \"").append(SolrUtils.clean(contentValue)).append("\"");
        i++;
      }
    }

    // Pagelet properties
    for (Map.Entry<String, String> entry : query.getProperties().entrySet()) {
      StringBuffer searchTerm = new StringBuffer();
      searchTerm.append(entry.getKey());
      searchTerm.append(":=").append(entry.getValue());
      and(solrQuery, PAGELET_PROPERTIES, searchTerm.toString(), true, true);
    }

    // Pagelet types
    for (Pagelet pagelet : query.getPagelets()) {
      StringBuffer searchTerm = new StringBuffer();
      searchTerm.append(pagelet.getModule()).append("/").append(pagelet.getIdentifier());

      // Are we looking for the pagelet in a certain composer or position?
      PageletURI uri = pagelet.getURI();
      if (StringUtils.isNotBlank(uri.getComposer()) || uri.getPosition() >= 0) {
        if (StringUtils.isNotBlank(uri.getComposer())) {
          String field = MessageFormat.format(PAGELET_TYPE_COMPOSER, uri.getComposer());
          and(solrQuery, field, searchTerm.toString(), true, true);
        }
        if (uri.getPosition() >= 0) {
          String field = MessageFormat.format(PAGELET_TYPE_COMPOSER_POSITION, uri.getPosition());
          and(solrQuery, field, searchTerm.toString(), true, true);
        }
      } else {
        and(solrQuery, PAGELET_TYPE, searchTerm.toString(), true, true);
      }
    }

    // Content filenames
    if (query.getFilename() != null) {
      and(solrQuery, CONTENT_FILENAME, query.getFilename(), true, true);
    }

    // Content mime types
    if (query.getMimetype() != null) {
      and(solrQuery, CONTENT_MIMETYPE, query.getMimetype(), true, true);
    }

    // Fulltext
    if (query.getText() != null) {
      and(solrQuery, FULLTEXT, query.getText(), true, true);
    }

    if (solrQuery.length() == 0)
      solrQuery.append("*:*");

    logger.debug("Solr query is {}", solrQuery.toString());

    // Prepare the solr query
    SolrQuery q = new SolrQuery(solrQuery.toString());
    q.setStart(query.getOffset() > 0 ? query.getOffset() : 0);
    q.setRows(query.getLimit() > 0 ? query.getLimit() : Integer.MAX_VALUE);
    
    // Define the fields that should be returned by the query
    List<String> fields = new ArrayList<String>();
    fields.add("*");

    // Order by publishing date
    if (!SearchQuery.Order.None.equals(query.getPublishingDateSortOrder())) {
      switch (query.getPublishingDateSortOrder()) {
        case Ascending:
          q.addSortField(SolrFields.PUBLISHED_FROM, ORDER.asc);
          break;
        case Descending:
          q.addSortField(SolrFields.PUBLISHED_FROM, ORDER.desc);
          break;
        case None:
        default:
          break;
      }
    } 
    
    // Order by modification date
    else if (!SearchQuery.Order.None.equals(query.getModificationDateSortOrder())) {
      switch (query.getModificationDateSortOrder()) {
        case Ascending:
          q.addSortField(SolrFields.MODIFIED, ORDER.asc);
          break;
        case Descending:
          q.addSortField(SolrFields.MODIFIED, ORDER.desc);
          break;
        case None:
        default:
          break;
      }
    }

    // Order by creation date
    else if (!SearchQuery.Order.None.equals(query.getCreationDateSortOrder())) {
      switch (query.getCreationDateSortOrder()) {
        case Ascending:
          q.addSortField(SolrFields.MODIFIED, ORDER.asc);
          break;
        case Descending:
          q.addSortField(SolrFields.MODIFIED, ORDER.desc);
          break;
        case None:
        default:
          break;
      }
    }

    // Order by score
    else {
      q.setSortField(SCORE, SolrQuery.ORDER.desc);
      q.setIncludeScore(true);
      fields.add("score");
    }
    
    // Add the fields to return
    q.setFields(StringUtils.join(fields, " "));
    
    // Execute the query and try to get hold of a query response
    QueryResponse solrResponse = null;
    try {
      solrResponse = solrConnection.request(q.toString());
    } catch (Exception e) {
      throw new SolrServerException(e);
    }

    // Create and configure the query result
    long hits = solrResponse.getResults().getNumFound();
    long size = solrResponse.getResults().size();

    SearchResultImpl result = new SearchResultImpl(query, hits, size);
    result.setSearchTime(solrResponse.getQTime());

    // Walk through response and create new items with title, creator, etc:
    for (SolrDocument doc : solrResponse.getResults()) {
      float score = fields.contains("score") ? (Float) doc.getFieldValue(SCORE) : 0.0f;

      String id = (String) doc.getFieldValue(ID);
      String path = (String) doc.getFieldValue(PATH);
      WebUrl url = new WebUrlImpl(site, path);

      SearchResultPageItemImpl item = new SearchResultPageItemImpl(site, id, url, score, site);
      item.setPageXml((String) doc.getFieldValue(XML));
      item.setPageHeaderXml((String) doc.getFieldValue(HEADER_XML));
      item.setPagePreviewXml((String) doc.getFieldValue(PREVIEW_XML));
      item.setTitle((String) doc.getFieldValue(TITLE_LOCALIZED));

      // Add the item to the result set
      result.addResultItem(item);
    }

    return result;
  }

  /**
   * Encodes field name and value as part of the AND clause of a solr query:
   * <tt>AND fieldName : fieldValue</tt>.
   * 
   * @param buf
   *          the <code>StringBuilder</code> to append to
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the field value
   * @param quote
   *          <code>true</code> to put the field values in quotes
   * @param clean
   *          <code>true</code> to escape solr special characters in the field
   *          value
   * @return the encoded query part
   */
  private StringBuilder and(StringBuilder buf, String fieldName,
      String fieldValue, boolean quote, boolean clean) {
    if (buf.length() > 0)
      buf.append(" AND ");
    buf.append(StringUtils.trim(fieldName));
    buf.append(":");
    if (quote)
      buf.append("\"");
    if (clean)
      buf.append(StringUtils.trim(clean(fieldValue)));
    else
      buf.append(StringUtils.trim(fieldValue));
    if (quote)
      buf.append("\"");
    return buf;
  }

  /**
   * Encodes field name and values as part of a solr query:
   * <tt>AND (fieldName : fieldValue[0] OR fieldName : fieldValue[1] ...)</tt>.
   * 
   * @param buf
   *          the <code>StringBuilder</code> to append to
   * @param fieldName
   *          the field name
   * @param fieldValues
   *          the field value
   * @param quote
   *          <code>true</code> to put the field values in quotes
   * @param clean
   *          <code>true</code> to escape solr special characters in the field
   *          value
   * @return the encoded query part
   */
  private StringBuilder and(StringBuilder buf, String fieldName,
      String[] fieldValues, boolean quote, boolean clean) {
    if (buf.length() > 0)
      buf.append(" AND ");
    buf.append(fieldName);
    buf.append(":(");
    boolean first = true;
    for (String value : fieldValues) {
      if (!first)
        buf.append(" OR ");
      if (quote)
        buf.append("\"");
      if (clean)
        buf.append(StringUtils.trim(clean(value)));
      else
        buf.append(StringUtils.trim(value));
      if (quote)
        buf.append("\"");
      first = false;
    }
    buf.append(")");
    return buf;
  }

  /**
   * Modifies the query such that certain fields are being boosted (meaning they
   * gain some weight).
   * 
   * @param query
   *          The user query.
   * @return The boosted query
   */
  public StringBuffer boost(String query) {
    String uq = SolrUtils.clean(query);
    StringBuffer sb = new StringBuffer();

    sb.append("(");

    sb.append(TITLE_LOCALIZED);
    sb.append(":(");
    sb.append(uq);
    sb.append(")^");
    sb.append(TITLE_BOOST);
    sb.append(" ");

    sb.append(FULLTEXT);
    sb.append(":(");
    sb.append(uq);
    sb.append(") ");

    sb.append(")");

    return sb;
  }

}
