/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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
package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.content.SearchQuery.Quantifier;
import ch.entwine.weblounge.common.content.SearchTerms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of a list of search terms.
 */
public class SearchTermsImpl<T extends Object> implements SearchTerms<T> {

  /** The quantifier */
  protected Quantifier quantifier = Quantifier.Any;

  /** The search terms */
  protected List<T> terms = new ArrayList<T>();

  /**
   * Creates a list of search terms, to be queried using the given quantifier.
   * 
   * @param quantifier
   *          the quantifier
   */
  public SearchTermsImpl(Quantifier quantifier) {
    this.quantifier = quantifier;
  }

  /**
   * Creates a list of search terms, to be queried using the given quantifier.
   * 
   * @param quantifier
   *          the quantifier
   * @param values
   *          the initial values
   */
  public SearchTermsImpl(Quantifier quantifier, T... values) {
    this.quantifier = quantifier;
    for (T value : values) {
      if (!this.terms.contains(value))
        this.terms.add(value);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchTerms#add(java.lang.Object)
   */
  @Override
  public void add(T term) {
    if (!this.terms.contains(term))
      this.terms.add(term);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchTerms#getTerms()
   */
  @Override
  public Collection<T> getTerms() {
    return terms;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchTerms#contains(java.lang.Object)
   */
  @Override
  public boolean contains(T term) {
    return terms.contains(term);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchTerms#size()
   */
  @Override
  public int size() {
    return terms.size();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.SearchTerms#getQuantifier()
   */
  @Override
  public Quantifier getQuantifier() {
    return quantifier;
  }

}
