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

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrUtils.*;

import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.impl.content.SearchResultImpl;
import ch.o2it.weblounge.common.impl.content.SearchResultPageItemImpl;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
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
    if (query.getId() != null) {
      and(solrQuery, SolrFields.ID, query.getId(), true, true);
    }

    // Path
    if (query.getPath() != null) {
      and(solrQuery, SolrFields.PATH, query.getPath(), true, true);
    }

    // Subjects
    if (query.getSubjects().length > 0) {
      and(solrQuery, SolrFields.SUBJECTS, query.getSubjects(), true, true);
    }

    // Template
    if (query.getTemplate() != null) {
      and(solrQuery, SolrFields.TEMPLATE, query.getTemplate(), true, true);
    }

    // Creator
    if (query.getCreator() != null) {
      and(solrQuery, SolrFields.CREATED_BY, SolrUtils.serializeUser(query.getCreator()), true, true);
    }

    // Creation date
    if (query.getCreationDate() != null) {
      and(solrQuery, SolrFields.CREATED, SolrUtils.selectDay(query.getCreationDate()), false, false);
    }

    // Modifier
    if (query.getModifier() != null) {
      and(solrQuery, SolrFields.MODIFIED_BY, SolrUtils.serializeUser(query.getModifier()), true, true);
    }

    // Modification date
    if (query.getModificationDate() != null) {
      and(solrQuery, SolrFields.MODIFIED, SolrUtils.selectDay(query.getModificationDate()), false, false);
    }

    // Publisher
    if (query.getPublisher() != null) {
      and(solrQuery, SolrFields.PUBLISHED_BY, SolrUtils.serializeUser(query.getPublisher()), true, true);
    }

    // Publication date
    if (query.getPublishingDate() != null) {
      and(solrQuery, SolrFields.PUBLISHED_FROM, SolrUtils.selectDay(query.getPublishingDate()), false, false);
    }

    // Pagelet properties
    for (Map.Entry<String, String> entry : query.getProperties().entrySet()) {
      StringBuffer searchTerm = new StringBuffer(entry.getKey()).append("=").append(entry.getValue());
      andAllLanguagesOf(solrQuery, SolrFields.PAGELET_PROPERTIES, searchTerm.toString(), query.getSite().getLanguages(), true, true);
    }

    // Fulltext
    if (query.getText() != null) {
      and(solrQuery, SolrFields.FULLTEXT, query.getText(), true, true);
    }

    if (solrQuery.length() == 0)
      solrQuery.append("*:*");

    logger.debug("Solr query is {}", solrQuery.toString());

    // Prepare the solr query
    SolrQuery q = new SolrQuery(solrQuery.toString());
    q.setStart(query.getOffset() > 0 ? query.getOffset() : 0);
    q.setRows(query.getLimit() > 0 ? query.getLimit() : Integer.MAX_VALUE);
    q.setSortField(SolrFields.SCORE, SolrQuery.ORDER.desc);
    q.setIncludeScore(true);
    q.setFields("* score");

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
      float score = (Float) doc.getFieldValue(SolrFields.SCORE);

      String id = (String) doc.getFieldValue(SolrFields.ID);
      String path = (String) doc.getFieldValue(SolrFields.PATH);
      WebUrl url = new WebUrlImpl(site, path);

      SearchResultPageItemImpl item = new SearchResultPageItemImpl(site, id, url, score, site);
      item.setPageXml((String) doc.getFieldValue(SolrFields.XML));
      item.setPageHeaderXml((String) doc.getFieldValue(SolrFields.HEADER_XML));
      item.setPagePreviewXml((String) doc.getFieldValue(SolrFields.PREVIEW_XML));
      item.setTitle((String) doc.getFieldValue(SolrFields.TITLE));

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
   * Encodes field name and value as part of the AND clause of a solr query:
   * <tt>AND fieldName : fieldValue</tt>.
   * 
   * @param buf
   *          the <code>StringBuilder</code> to append to
   * @param fieldName
   *          the field name
   * @param index
   *          the indexed field value
   * @param fieldValue
   *          the field value
   * @param quote
   *          <code>true</code> to put the field values in quotes
   * @param clean
   *          <code>true</code> to escape solr special characters in the field
   *          value
   * @return the encoded query part
   */
  private StringBuilder andAllLanguagesOf(StringBuilder buf, String fieldName,
      String fieldValue, Language[] languages, boolean quote, boolean clean) {
    if (buf.length() > 0)
      buf.append(" AND ");
    if (languages.length > 1)
      buf.append(" (");
    int i = 0;
    for (Language l : languages) {
      if (i > 0)
        buf.append(" OR ");
      buf.append(StringUtils.trim(MessageFormat.format(fieldName, l.getIdentifier())));
      buf.append(":");
      if (quote)
        buf.append("\"");
      if (clean)
        buf.append(StringUtils.trim(clean(fieldValue)));
      else
        buf.append(StringUtils.trim(fieldValue));
      if (quote)
        buf.append("\"");
      i++;
    }
    if (languages.length > 1)
      buf.append(") ");
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

    sb.append(SolrFields.TITLE);
    sb.append(":(");
    sb.append(uq);
    sb.append(")^");
    sb.append(SolrFields.TITLE_BOOST);
    sb.append(" ");

    sb.append(SolrFields.FULLTEXT);
    sb.append(":(");
    sb.append(uq);
    sb.append(") ");

    sb.append(")");

    return sb;
  }

}
