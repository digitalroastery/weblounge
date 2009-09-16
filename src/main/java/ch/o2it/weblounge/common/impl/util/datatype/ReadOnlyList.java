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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReadOnlyList<T> implements List {

  /** the wrapped list */
  private List<T> list;

  /**
   * Creates a new <code>ReadonlyList</code>.
   * 
   * @param list
   *          the list to wrap
   */
  ReadOnlyList(List<T> list) {
    this.list = list;
  }

  /**
   * @see java.util.List#size()
   */
  public int size() {
    return list.size();
  }

  /**
   * @see java.util.List#clear()
   */
  public void clear() {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#isEmpty()
   */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * @see java.util.List#toArray()
   */
  public Object[] toArray() {
    return list.toArray();
  }

  /**
   * @see java.util.List#get(int)
   */
  public T get(int index) {
    return list.get(index);
  }

  /**
   * @see java.util.List#remove(int)
   */
  public T remove(int index) {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#add(int, java.lang.Object)
   */
  public void add(int index, Object element) {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#indexOf(java.lang.Object)
   */
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  /**
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  /**
   * @see java.util.List#add(java.lang.Object)
   */
  public boolean add(Object o) {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#contains(java.lang.Object)
   */
  public boolean contains(Object o) {
    return list.contains(o);
  }

  /**
   * @see java.util.List#remove(java.lang.Object)
   */
  public boolean remove(Object o) {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  public boolean addAll(int index, Collection c) {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#addAll(java.util.Collection)
   */
  public boolean addAll(Collection c) {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection c) {
    return list.containsAll(c);
  }

  /**
   * @see java.util.List#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection c) {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection c) {
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#iterator()
   */
  public Iterator<T> iterator() {
    return new ReadOnlyIterator<T>(list.iterator());
  }

  /**
   * @see java.util.List#subList(int, int)
   */
  public List subList(int fromIndex, int toIndex) {
    // TODO: implement!
    throw new UnsupportedOperationException("list is read only!");
  }

  /**
   * @see java.util.List#listIterator()
   */
  public ListIterator listIterator() {
    return new ReadOnlyListIterator<T>(list.listIterator());
  }

  /**
   * @see java.util.List#listIterator(int)
   */
  public ListIterator listIterator(int index) {
    return new ReadOnlyListIterator<T>(list.listIterator(index));
  }

  /**
   * @see java.util.List#set(int, java.lang.Object)
   */
  public Object set(int index, Object element) {
    throw new UnsupportedOperationException("list is read only!");
  }

  public Object[] toArray(Object[] a) {
    return list.toArray(a);
  }

}