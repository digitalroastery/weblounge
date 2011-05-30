/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Suggestions contains a number of suggested values based on a seed.
 */
public class Suggestions extends ArrayList<String> {
  
  /** Serial version UID */
  private static final long serialVersionUID = 6321362298440637912L;

  /** The dictionaries available for suggestions */
  public enum Dictionary { Path }

  /** The seed used for suggestions */
  protected String seed = null;

  /** Whether suggestions will be sorted by weight ("popularity") */
  protected boolean onlyMorePopular = true;

  /** Specifies to return up to 5 suggestions */
  protected int count = 5;

  /** To provide a query collated with the first matching suggestion */
  protected boolean collate = true;

  /** The list of suggestions */
  protected List<String> suggestions = null;
  
  /** The collation */
  protected String collation = null;
  
  /**
   * Creates a list of suggestions based on the seed.
   * @param solrResponse
   *          the response from solr
   * @param seed
   *          the value used to create the suggestions
   * @param onlyMorePopular
   *          <code>true</code> to return suggestions sorted by popularity
   * @param count
   *          number of suggestions to return
   * @param collate
   *          whether to provide a query collated with the first matching
   *          suggestion
   */
  Suggestions(QueryResponse solrResponse, String seed, boolean onlyMorePopular, int count,
      boolean collate) {
    this.seed = seed;
    this.onlyMorePopular = onlyMorePopular;
    this.count = count;
    this.collate = collate;
    
    SolrDocumentList documents = solrResponse.getResults();
    suggestions = new ArrayList<String>(documents.size());
    for (Iterator<SolrDocument> di = documents.iterator(); di.hasNext();) {
      SolrDocument doc = di.next();
      // super.add()
    }
  }

}
