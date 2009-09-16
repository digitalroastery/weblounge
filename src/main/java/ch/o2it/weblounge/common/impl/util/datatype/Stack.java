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

import java.util.ArrayList;

/**
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 * @param <T>
 *          the actual element type
 */

public class Stack<T> extends ArrayList<T> {

  /** Comment for <code>serialVersionUID</code> */
  private static final long serialVersionUID = 6165275507948882426L;

  /**
   * Creates a <code>Stack</code> with a default initial capacity.
   */
  public Stack() { /* nothing to do */
  }

  /**
   * Creates a <code>Stack</code> with the specified initial capacity.
   * 
   * @param capacity
   *          the initial capacity of the stack
   */
  public Stack(int capacity) {
    super(capacity);
  }

  /**
   * Returns the topmost element of the stack.
   * 
   * @return the topmost element of the stack or <code>null</code> if the stack
   *         is empty
   */
  public T top() {
    int size = size();
    if (size > 0)
      return get(size - 1);
    return null;
  }

  /**
   * Removes to topmost element from the stack and returns it.
   * 
   * @return the topmost element of the stack or <code>null</code> if the stack
   *         is empty
   */
  public T pop() {
    int size = size();
    if (size > 0)
      return remove(size - 1);
    return null;
  }

  /**
   * Pushes an element on top of the stack.
   * 
   * @param o
   *          the element to push onto the stack.
   */
  public void push(T o) {
    add(o);
  }

  /**
   * Checks whether the stack is empty.
   * 
   * @return <code>true</code> if he stack is empty, <code>false</code>
   *         otherwise.
   */
  public boolean empty() {
    return isEmpty();
  }
}
