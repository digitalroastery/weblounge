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
import java.util.Iterator;

/**
 * This class implements a linked list. In this list, each link may be attached
 * an arbitrary object. The list currently supports up to
 * <code>Integer.MAX_VALUE</code> elements.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class LinkedList {

  /** The list's head element */
  private Link list_;

  /** Number of elements in list */
  private int listSize_ = 0;

  /**
   * Returns the number of elements that are currently part of this list.
   * 
   * @return the list size
   */
  public int getSize() {
    return listSize_;
  }

  /**
   * Returns the head element and, depending on the <code>remove</code>
   * parameter, removes it from the list.
   * 
   * @param remove
   *          <code>true</code> to remove the object from the list
   * @return the head element
   */
  public Object getHeadElement(boolean remove) {
    if (list_ == null) {
      return null;
    } else {
      Object obj = list_.getObject();
      if (remove)
        remove(list_);
      return obj;
    }
  }

  /**
   * Returns the head element.
   * 
   * @return the head element
   */
  public Object getHeadElement() {
    if (list_ == null) {
      return null;
    } else {
      return list_.getObject();
    }
  }

  /**
   * Returns the tail element and, depending on the <code>remove</code>
   * parameter, removes it from the list.
   * 
   * @param remove
   *          <code>true</code> to remove the object from the list
   * @return the tail element
   */
  public Object getTailElement(boolean remove) {
    if (list_ == null) {
      return null;
    } else {
      Link link = list_;
      while (link.next != null) {
        link = link.next;
      }
      Object obj = link.getObject();
      if (remove)
        remove(link);
      return obj;
    }
  }

  /**
   * Returns the tail element.
   * 
   * @return the tail element
   */
  public Object getTailElement() {
    if (list_ == null) {
      return null;
    } else {
      Link link = list_;
      while (link.next != null) {
        link = link.next;
      }
      return link.getObject();
    }
  }

  /**
   * Adds <code>obj</code> to the the head of the list.
   * 
   * @param obj
   *          the object to add
   */
  public synchronized void prepend(Object obj) {
    if (listSize_ == Integer.MAX_VALUE) {
      throw new IllegalStateException("The maximum number of elements in list has been reached!");
    }
    Link link = new Link(obj);
    if (list_ == null)
      list_ = link;
    else {
      link.setNext(list_);
      list_ = link;
    }
    listSize_++;
  }

  /**
   * Adds <code>obj</code> to the the tail of the list.
   * 
   * @param obj
   *          the object to add
   */
  public synchronized void append(Object obj) {
    if (listSize_ == Integer.MAX_VALUE) {
      throw new IllegalStateException("The maximum number of elements in list has been reached!");
    }
    Link newLink = new Link(obj);
    Link link = list_;
    while (link.next != null) {
      link = link.next;
    }
    link.setNext(newLink);
    listSize_++;
  }

  /**
   * Returns the removed object or <code>null</code> if the object was not
   * found.
   * 
   * @param obj
   *          the object to remove
   * @return the removed object
   */
  public synchronized Object remove(Object obj) {
    Link link = findLink(obj);
    return remove(link);
  }

  /**
   * Returns the removed object or <code>null</code> if the object was not
   * found.
   * 
   * @param link
   *          the link to remove
   * @return the removed object
   */
  private synchronized Object remove(Link link) {
    if (link != null) {
      if (link != list_) {
        link.prev.setNext(link.next);
      } else {
        if (list_.next != null)
          list_ = list_.next;
        else
          list_ = null;
      }
      listSize_--;
      return link.getObject();
    }
    return null;
  }

  /**
   * Returns an iteration of the cached pages.
   * 
   * @return an iteration of pages
   */
  public Iterator iterator() {
    return (list_ != null) ? list_ : (new ArrayList()).iterator();
  }

  /**
   * Returns the link with <code>obj</code> attached if it exists.
   * 
   * @param obj
   *          the object to look up
   * @return the associated link
   */
  private Link findLink(Object obj) {
    Link link = list_;
    while (link != null) {
      if (link.getObject().equals(obj))
        return link;
      link = link.next;
    }
    return null;
  }

  /**
   * Inner class used to build a double linked chain of elements. The chain
   * start is identified by <code>getPrevious() == null</code>, the last element
   * has a next element of <code>null</code>.
   * 
   * @author Tobias Wunden
   * @version 1.0
   * @since Weblounge 2.0
   */
  private class Link implements Iterator {

    /** References used to maintain the chain */
    Link prev, next;

    /** The linked object */
    Object obj;

    /**
     * Creates a new link element.
     * 
     * @param obj
     *          the attached data object
     */
    Link(Object obj) {
      this.obj = obj;
    }

    /**
     * Links this element to <code>hdl</code> as its next element.
     * 
     * @param hdl
     *          the next element
     */
    void setNext(Link hdl) {
      if (hdl != null) {
        hdl.prev = this;
      }
      next = hdl;
    }

    /**
     * Links this element to <code>hdl</code> as its previous element.
     * 
     * @param hdl
     *          the previous element
     */
    void setPrevious(Link hdl) {
      if (hdl != null) {
        hdl.next = this;
      }
      prev = hdl;
    }

    /**
     * Returns the object that is attached to this link.
     * 
     * @return the object
     */
    Object getObject() {
      return obj;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return (next != null);
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() {
      return next;
    }

    /**
     * This method is not allowed for this iterator and will therefore throw a
     * <code>OperationNotSupportedException</code>
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() {
    }
  }
}