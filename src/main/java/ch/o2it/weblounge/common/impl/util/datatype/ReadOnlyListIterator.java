/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.datatype;

import java.util.ListIterator;

public class ReadOnlyListIterator<T> implements ListIterator {

  /** the wrapped iterator */
  private ListIterator<T> iter;

  /**
   * Creates a new <code>ReadonlyListIterator</code>.
   * 
   * @param iter
   *          the wrapped iterator
   */
  ReadOnlyListIterator(ListIterator<T> iter) {
    this.iter = iter;
  }

  /**
   * @see java.util.ListIterator#nextIndex()
   */
  public int nextIndex() {
    return iter.nextIndex();
  }

  /**
   * @see java.util.ListIterator#previousIndex()
   */
  public int previousIndex() {
    return iter.previousIndex();
  }

  /**
   * @see java.util.ListIterator#remove()
   */
  public void remove() {
    throw new UnsupportedOperationException("iterator is read only!");
  }

  /**
   * @see java.util.ListIterator#hasNext()
   */
  public boolean hasNext() {
    return iter.hasNext();
  }

  /**
   * @see java.util.ListIterator#hasPrevious()
   */
  public boolean hasPrevious() {
    return iter.hasPrevious();
  }

  /**
   * @see java.util.ListIterator#next()
   */
  public T next() {
    return iter.next();
  }

  /**
   * @see java.util.ListIterator#previous()
   */
  public T previous() {
    return iter.previous();
  }

  /**
   * @see java.util.ListIterator#add(java.lang.Object)
   */
  public void add(Object o) {
    throw new UnsupportedOperationException("iterator is read only!");
  }

  /**
   * @see java.util.ListIterator#set(java.lang.Object)
   */
  public void set(Object o) {
    throw new UnsupportedOperationException("iterator is read only!");
  }

}