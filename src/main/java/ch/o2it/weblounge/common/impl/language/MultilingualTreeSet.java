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

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.language.Language;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * TODO: Comment MultilingualTreeSet
 */
public class MultilingualTreeSet<Type> implements Set<Type> {

  /** The language */
  protected Language language;

  /** The backing array list */
  protected SortedSet<Type> members = null;

  /** The multilingual comparator used to sort the set */
  protected MultilingualComparator<Type> comparator;

  /**
   * Creates a new <code>MultilingualTreeSet</code> with <code>English</code> as
   * the set language.
   */
  public MultilingualTreeSet() {
    this(LanguageSupport.getLanguage(Locale.getDefault()));
  }

  /**
   * Creates a new <code>MultilingualTreeSet</code> with <code>language</code>
   * as the set language.
   * 
   * @param language
   *          the set language
   */
  public MultilingualTreeSet(Language language) {
    comparator = new MultilingualComparator<Type>(language);
    members = new TreeSet<Type>(comparator);
    this.language = language;
  }

  /**
   * @see ch.o2it.weblounge.common.language.MultilingualSet#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * @see ch.o2it.weblounge.common.language.MultilingualSet#switchTo(ch.o2it.weblounge.common.language.Language)
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
    return members.iterator();
  }

  /**
   * @see ch.o2it.weblounge.common.language.MultilingualSet#iterator(ch.o2it.weblounge.common.language.Language)
   */
  public Iterator<Type> iterator(Language language) {
    TreeSet<Type> set = new TreeSet<Type>(new MultilingualComparator<Type>(language));
    set.addAll(members);
    return set.iterator();
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
   * @see java.util.List#toArray(T[])
   */
  public <T> T[] toArray(T[] a) {
    synchronized (members) {
      return members.toArray(a);
    }
  }

  /**
   * @see java.util.List#toArray()
   */
  public Object[] toArray() {
    return toArray(new Object[members.size()]);
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
   * @see java.util.SortedSet#first()
   */
  public Type first() {
    return members.first();
  }

  /**
   * @see java.util.SortedSet#last()
   */
  public Type last() {
    return members.last();
  }

  /**
   * @see java.util.SortedSet#headSet(java.lang.Object)
   */
  public SortedSet<Type> headSet(Type o) {
    return members.headSet(o);
  }

  /**
   * @see java.util.SortedSet#tailSet(java.lang.Object)
   */
  public SortedSet<Type> tailSet(Type o) {
    return members.tailSet(o);
  }

  /**
   * @see java.util.SortedSet#subSet(java.lang.Object, java.lang.Object)
   */
  public SortedSet<Type> subSet(Type a, Type b) {
    return members.subSet(a, b);
  }

  /**
   * @see java.util.SortedSet#comparator()
   */
  public Comparator<? super Type> comparator() {
    return comparator;
  }
}