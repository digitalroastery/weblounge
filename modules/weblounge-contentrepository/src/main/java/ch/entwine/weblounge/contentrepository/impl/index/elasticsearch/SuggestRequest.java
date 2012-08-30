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

import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Request for suggestions based on existing values in the search index. This
 * request needs a matching requestHandler definition in
 * <code>solrconfig.xml</code>:
 * 
 * <pre>
 *  <searchComponent class="solr.SpellCheckComponent" name="suggest-path">
 *    <lst name="spellchecker">
 *      <str name="name">suggest_path</str>
 *      <str name="classname">org.apache.solr.spelling.suggest.Suggester</str>
 *      <str name="lookupImpl">org.apache.solr.spelling.suggest.tst.TSTLookup</str>
 *      <str name="field">path</str>
 *      <float name="threshold">0.005</float>
 *      <str name="buildOnCommit">true</str>
 *    </lst>
 *  </searchComponent>
 * 
 *  <requestHandler class="org.apache.solr.handler.component.SearchHandler" name="/suggest/path">
 *    <lst name="defaults">
 *      <str name="spellcheck">true</str>
 *      <str name="spellcheck.dictionary">suggest-path</str>
 *     <str name="spellcheck.onlyMorePopular">false</str>
 *      <str name="spellcheck.count">25</str>
 *      <str name="spellcheck.collate">true</str>
 *    </lst>
 *    <arr name="components">
 *      <str>suggest-path</str>
 *    </arr>
 *  </requestHandler>
 * </pre>
 * 
 * In this example, the suggest request would need to be creates with
 * <code>/suggest/path</code> as the name.
 */
public class SuggestRequest {

  /** The dictionaries available for suggestions */
  public enum Dictionary {
    Default, Path, Subject
  }

  /** Name of the solr request handler */
  protected String suggestHandler = "/suggest";

  /** Name of the dictionary component that we configured above */
  protected String dictionary = null;

  /** Whether suggestions will be sorted by weight ("popularity") */
  protected boolean onlyMorePopular = true;

  /** Specifies to return up to 5 suggestions */
  protected int count = 5;

  /** To provide a query collated with the first matching suggestion */
  protected boolean collate = true;

  // /** The connection to the solr database */
  // private Solr solrConnection = null;

  /**
   * Creates a new suggest request which uses the given connection to solr and a
   * predefined dictionary named <code>dictionary</code> to return suggestions.
   * 
   * @param connection
   *          the solr connection
   * @param dictionary
   *          name of the dictionary
   * @param onlyMorePopular
   *          <code>true</code> to return suggestions sorted by popularity
   * @param count
   *          number of suggestions to return
   * @param collate
   *          whether to provide a query collated with the first matching
   *          suggestion
   */
  // public SuggestRequest(Solr connection, String dictionary,
  // boolean onlyMorePopular, int count, boolean collate) {
  // this.solrConnection = connection;
  // this.dictionary = dictionary.toLowerCase();
  // this.onlyMorePopular = onlyMorePopular;
  // this.count = count;
  // this.collate = collate;
  // }

  /**
   * Returns a list of suggestions based on the seed value.
   * 
   * @param seed
   *          the value to base suggestions on
   * @return the suggestions
   * @throws IllegalArgumentException
   *           if <code>seed</code> is blank
   * @throws SolrServerException
   *           if querying solr fails
   */
  public List<String> getSuggestions(String seed)
      throws IllegalArgumentException, ContentRepositoryException {
    if (StringUtils.isBlank(seed))
      throw new IllegalArgumentException("Seed cannot be blank");

    // TODO: Implement
    return Collections.EMPTY_LIST;
  }

}
