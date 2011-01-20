/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.cache.impl.filter;

import ch.o2it.weblounge.cache.StreamFilter;

/**
 * <code>FilterChain</code> wraps a chain of {@link StreamFilter} objects. It is
 * itself a <code>StreamFilter</code> that filters the input stream by passing
 * it to each of the wrapped filters in turn.
 */
public class FilterChain implements StreamFilter {

  /** the wrapped filters */
  private StreamFilter[] filters = null;

  /**
   * Creates a new <code>FilterChain</code>.
   * 
   * @param filters
   *          the wrapped filters.
   */
  public FilterChain(StreamFilter[] filters) {
    if (filters == null)
      throw new NullPointerException("filters must not be null");
    this.filters = filters;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.cache.StreamFilter#filter(java.lang.StringBuffer, java.lang.String)
   */
  public StringBuffer filter(StringBuffer b, String contentType) {
    for (int i = 0; i < filters.length && b != null; i++)
      b = filters[i].filter(b, contentType);
    return b;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.cache.StreamFilter#flush()
   */
  public StringBuffer flush() {
    StringBuffer b = new StringBuffer();
    StringBuffer tmp = null;
    for (int i = 0; i < filters.length; i++) {
      tmp = filters[i].flush();
      if (tmp != null)
        b.append(tmp);
    }
    return b;
  }

  /**
   * @see ch.o2it.weblounge.api.request.StreamFilter#close()
   */
  public void close() {
    for (int i = 0; i < filters.length; i++)
      filters[i].close();
  }

}