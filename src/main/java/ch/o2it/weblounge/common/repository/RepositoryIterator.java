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

package ch.o2it.weblounge.common.repository;

/**
 * TODO: Comment RepositoryIterator
 */
public interface RepositoryIterator {

  /**
   * Callback for the visitor. After calling <code>accept</code> on the visited
   * <code>RepositoryCollection</code>, the collection will call this method, so
   * that the iterator may perform some operations on it.
   * 
   * @param collection
   *          the visited repository collection
   */
  void visit(Collection collection);

  /**
   * Callback for the visitor. After calling <code>accept</code> on the visited
   * <code>RepositoryItem</code>, the item will call this method, so that the
   * iterator may perform some operations on it.
   * 
   * @param item
   *          the visited repository item
   */
  void visit(Resource item);

}