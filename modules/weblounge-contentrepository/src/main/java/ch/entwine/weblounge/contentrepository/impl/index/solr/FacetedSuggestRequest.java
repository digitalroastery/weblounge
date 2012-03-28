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

package ch.entwine.weblounge.contentrepository.impl.index.solr;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for suggestions based on existing values in the search index. This
 * request uses the faceting mechanism of Solr and does not need additional
 * components on <code>solrconnfig.xml</code>.
 */
public class FacetedSuggestRequest {

  /** The dictionaries available for suggestions */
  public enum Dictionary {
    Default, Path, Subject
  }

  /** Name of the dictionary component that we configured above */
  protected String dictionary = null;

  /** Specifies to return up to 5 suggestions */
  protected int count = 10;

  /** The connection to the solr database */
  private Solr solrConnection = null;

  /**
   * Creates a new suggest request which uses the given connection to solr and a
   * predefined dictionary named <code>dictionary</code> to return suggestions,
   * using facets.
   * 
   * @param connection
   *          the solr connection
   * @param dictionary
   *          name of the dictionary
   * @param count
   *          number of suggestions to return
   */
  public FacetedSuggestRequest(Solr connection, String dictionary,
      int count) {
    this.solrConnection = connection;
    this.dictionary = dictionary.toLowerCase();
    this.count = count;
  }

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
      throws IllegalArgumentException, SolrServerException {
    if (StringUtils.isBlank(seed))
      throw new IllegalArgumentException("Seed cannot be blank");

    String field = dictionary + "_suggest";

    SolrQuery query = new SolrQuery();
    query.setQuery("*:*");

    ModifiableSolrParams solrParams = new ModifiableSolrParams();
    solrParams.set("facet", "true");
    solrParams.set("facet.field", field);
    solrParams.set("facet.prefix", seed);
    query.add(solrParams);

    // Execute the query and try to get hold of a query response
    QueryResponse solrResponse = null;
    try {
      solrResponse = solrConnection.request(query);
    } catch (Throwable t) {
      throw new SolrServerException(t);
    }

    List<String> result = new ArrayList<String>();

    // Extract the facet values
    FacetField facetField = solrResponse.getFacetField(field);
    if (facetField == null || facetField.getValueCount() == 0)
      return result;

    // Extract the facet names and add them to the result set
    for (Count facetCount : facetField.getValues()) {
      result.add(facetCount.getName());
    }

    return result;
  }

}
