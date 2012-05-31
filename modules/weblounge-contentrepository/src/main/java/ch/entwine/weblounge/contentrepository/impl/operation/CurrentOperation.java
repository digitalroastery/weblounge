/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.contentrepository.impl.operation;

import ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation;

/**
 * This static class is holding on to the {@link ContentRepositoryOperation}
 * that the current thread is working on.
 */
public class CurrentOperation {

  /** The operation */
  public static ThreadLocal<ContentRepositoryOperation<?>> operation = null;

  /**
   * Sets the {@link ThreadLocal} that is holding the current content repository
   * operation.
   * 
   * @param op
   *          the current operation
   */
  public static void set(ContentRepositoryOperation<?> op) {
    operation.set(op);
  }

  /**
   * Returns the current content repository operation.
   * 
   * @return the current operation
   */
  public static ContentRepositoryOperation<?> get() {
    return operation.get();
  }

  /**
   * Removes the {@link ThreadLocal} that is holding the current content
   * repository operation.
   */
  public static void remove() {
    operation.remove();
  }

}
