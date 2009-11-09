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

package ch.o2it.weblounge.common.impl.util.datatype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An efficient priority queue implemented as a heap:
 * <ul>
 * <li>insert: O(log n)
 * <li>removeMin: O(log n)
 * <li>getMin: O(1)
 * <li>remove: O(n)
 * </ul>
 */
public class PriorityQueue<T extends Comparable<T>> {

  /** the default size of a priority queue */
  private static final int DEFAULT_SIZE = 32;

  /** the actual heap represented as an array */
  private List<T> heap;

  /** the initial size of the queue */
  private int initialSize;

  /**
   * Creates a new <code>PriorityQueue</code> with a default initial size.
   */
  public PriorityQueue() {
    this(DEFAULT_SIZE);
  }

  /**
   * Creates a new <code>PriorityQueue</code> with the given initial size.
   * 
   * @param initialSize
   *          the initial size of the queue
   */
  public PriorityQueue(int initialSize) {
    heap = new ArrayList<T>(initialSize);
    this.initialSize = initialSize;
  }

  /**
   * Checks whether the queue is empty.
   * 
   * @return <code>true</code> if the queue is empty, <code>fale</code>
   *         otherwise
   */
  public boolean isEmpty() {
    return heap.size() == 0;
  }

  /**
   * Returns the number of elements in the queue.
   * 
   * @return the actual size of the queue
   */
  public int size() {
    return heap.size();
  }

  /**
   * Returns the capacity of the queue.
   * 
   * @return the actual capacity of the queue
   */
  public int capacity() {
    return heap.size();
  }

  /**
   * Removes all elements from the queue.
   */
  public void clear() {
    heap = new ArrayList<T>(initialSize);
  }

  /**
   * Inerts a new element into the queue.
   * 
   * @param c
   *          the new element
   */
  public void insert(T c) {
    heap.add(c);
    Collections.sort(heap);
  }

  /**
   * Extracts the element minimal element.
   * 
   * @return the element with the highest priority
   */
  public T getMin() {
    if (heap.size() == 0) {
      throw new NoSuchElementException();
    }
    return heap.get(0);
  }

  /**
   * Removes the minimal element and returns it.
   * 
   * @return the element with the highest priority
   */
  public T removeMin() {
    if (heap.size() == 0) {
      throw new NoSuchElementException();
    }
    return heap.remove(0);
  }

  /**
   * Removed the given element from the queue.
   * 
   * @param c
   *          the element that should be removed
   * @return the removed element or <code>null</code>, if the element could not
   *         be removed
   */
  public T remove(T c) {
    T result = null;
    int index = -1;
    for (int i = 0; i < heap.size(); i++) {
      if (heap.get(i).equals(c)) {
        result = c;
        index = i;
        break;
      }
    }
    heap.remove(index);
    return result;
  }

}