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

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.language.Language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MultilingualTreeList<Type> {

  /** The language */
  protected Language language;

  /** The backing array list */
  protected List<Type> members = new ArrayList<Type>();

  /** The multilingual comparator used to sort the set */
  protected MultilingualComparator<Type> comparator;

  /**
   * Creates a new <code>MultilingualTreeList</code> with <code>English</code>
   * as the list language.
   */
  public MultilingualTreeList() {
    this(English.getInstance());
  }

  /**
   * Creates a new <code>MultilingualTreeList</code> with <code>language</code>
   * as the list language.
   * 
   * @param language
   *          the set language
   */
  public MultilingualTreeList(Language language) {
    members = new ArrayList<Type>();
    this.language = language;
    comparator = new MultilingualComparator<Type>(language);
  }

  /**
   * @see ch.o2it.weblounge.common.language.MultilingualList#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * @see ch.o2it.weblounge.common.language.MultilingualList#setLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public void setLanguage(Language language) {
    this.language = language;
    comparator.setLanguage(language);
  }

  /**
   * Returns an ordered iteration of this tree set.
   * 
   * @return the set iterator
   */
  public Iterator<Type> iterator() {
    Collections.sort(members, comparator);
    return members.iterator();
  }

  /**
   * @see ch.o2it.weblounge.common.language.MultilingualList#iterator(ch.o2it.weblounge.common.language.Language)
   */
  public Iterator<Type> iterator(Language language) {
    Collections.sort(members, new MultilingualComparator<Type>(language));
    return members.iterator();
  }

  /**
   * @see java.util.Collection#size()
   */
  public int size() {
    return members.size();
  }

  /**
   * @see java.util.Collection#clear()
   */
  public void clear() {
    members.clear();
  }

  /**
   * @see java.util.Collection#isEmpty()
   */
  public boolean isEmpty() {
    return members.isEmpty();
  }

  /**
   * @see java.util.List#get(int)
   */
  public Type get(int i) {
    return members.get(i);
  }

  /**
   * @see java.util.List#remove(int)
   */
  public Type remove(int i) {
    synchronized (members) {
      return members.remove(i);
    }
  }

  /**
   * @see java.util.List#add(int, java.lang.Object)
   */
  public void add(int index, Type o) {
    synchronized (members) {
      members.add(index, o);
    }
  }

  /**
   * @see java.util.List#indexOf(java.lang.Object)
   */
  public int indexOf(Object o) {
    return members.indexOf(o);
  }

  /**
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  public int lastIndexOf(Object o) {
    return members.lastIndexOf(o);
  }

  /**
   * @see java.util.Collection#add(java.lang.Object)
   */
  public boolean add(Type o) {
    synchronized (members) {
      return members.add(o);
    }
  }

  /**
   * @see java.util.Collection#contains(java.lang.Object)
   */
  public boolean contains(Object o) {
    return members.contains(o);
  }

  /**
   * @see java.util.Collection#remove(java.lang.Object)
   */
  public boolean remove(Object o) {
    synchronized (members) {
      return members.remove(o);
    }
  }

  /**
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  public boolean addAll(int index, Collection<? extends Type> c) {
    synchronized (members) {
      return members.addAll(index, c);
    }
  }

  /**
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  public boolean addAll(Collection<? extends Type> c) {
    synchronized (members) {
      return members.addAll(c);
    }
  }

  /**
   * @see java.util.Collection#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection<?> c) {
    return members.containsAll(c);
  }

  /**
   * @see java.util.Collection#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection<?> c) {
    synchronized (members) {
      return members.removeAll(c);
    }
  }

  /**
   * @see java.util.Collection#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection<?> c) {
    synchronized (members) {
      return members.retainAll(c);
    }
  }

  /**
   * Returns a new multilingual list containing the elements specified by
   * <code>start</code> and <code>end</code>.
   * 
   * @see java.util.List#subList(int, int)
   */
  public MultilingualTreeList<Type> subList(int start, int end) {
    synchronized (members) {
      MultilingualTreeList<Type> list = new MultilingualTreeList<Type>(language);
      list.addAll(members.subList(start, end));
      return list;
    }
  }

  /**
   * @see java.util.List#listIterator()
   */
  public ListIterator<Type> listIterator() {
    synchronized (members) {
      Collections.sort(members, comparator);
      return members.listIterator();
    }
  }

  /**
   * @see ch.o2it.weblounge.common.language.MultilingualList#listIterator(ch.o2it.weblounge.common.language.Language)
   */
  public ListIterator<Type> listIterator(Language language) {
    Collections.sort(members, new MultilingualComparator<Type>(language));
    return members.listIterator();
  }

  /**
   * @see java.util.List#listIterator(int)
   */
  public ListIterator<Type> listIterator(int index) {
    synchronized (members) {
      Collections.sort(members, comparator);
      return members.listIterator(index);
    }
  }

  /**
   * @see ch.o2it.weblounge.common.language.MultilingualList#listIterator(int,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public ListIterator<Type> listIterator(int index, Language language) {
    Collections.sort(members, new MultilingualComparator<Type>(language));
    return members.listIterator();
  }

  /**
   * @see java.util.List#set(int, java.lang.Object)
   */
  public Type set(int index, Type o) {
    synchronized (members) {
      return members.set(index, o);
    }
  }

  /**
   * @see java.util.List#toArray(T[])
   */
  public <T> T[] toArray(T[] a) {
    synchronized (members) {
      Collections.sort(members, comparator);
      return members.toArray(a);
    }
  }

  /**
   * @see java.util.List#toArray()
   */
  public Object[] toArray() {
    return toArray(new Object[members.size()]);
  }

}