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
package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.content.SearchQuery.Quantifier;

import java.util.Collection;

/**
 * Interface to hold a group of search terms.
 */
public interface SearchTerms<T extends Object> {

  /**
   * Adds a term to this list of terms.
   * 
   * @param term
   *          the new term
   */
  void add(T term);

  /**
   * Returns the terms.
   * 
   * @return the terms
   */
  Collection<T> getTerms();

  /**
   * Returns <code>true</code> if <code>term</code> is contained in the list of
   * terms.
   * 
   * @param term
   *          the term
   * @return <code>true</code> if <code>term</code> is contained
   */
  boolean contains(T term);

  /**
   * Returns the number of terms.
   * 
   * @return the number of terms
   */
  int size();

  /**
   * Returns this group's quantifier.
   * 
   * @return the quantifier
   */
  Quantifier getQuantifier();

}
